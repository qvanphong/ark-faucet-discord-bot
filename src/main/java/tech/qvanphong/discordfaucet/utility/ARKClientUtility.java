package tech.qvanphong.discordfaucet.utility;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.arkecosystem.client.Connection;
import org.arkecosystem.crypto.identities.Address;
import org.arkecosystem.crypto.transactions.builder.TransferBuilder;
import org.arkecosystem.crypto.transactions.types.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.qvanphong.discordfaucet.config.ApplicationConfig;
import tech.qvanphong.discordfaucet.config.TokenConfig;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ARKClientUtility {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private ApplicationConfig applicationConfig;
    private Map<String, Connection> networkConnection;

    public ARKClientUtility(ApplicationConfig applicationConfig, Map<String, Connection> networkConnections) {
        this.applicationConfig = applicationConfig;
        this.networkConnection = networkConnections;
    }

    public Connection createConnection(String chainName) {
        TokenConfig tokenInfo= applicationConfig.getTokenConfigFromChainName(chainName);
        if (!tokenInfo.getBackupApiUrl().isEmpty()) {
            OkHttpClient okHttpClient = new OkHttpClient();

            // Add default api to list. In order to reconnect to default api url
            ArrayList<String> apiUrls = new ArrayList<>(tokenInfo.getBackupApiUrl());
            apiUrls.add(tokenInfo.getApiUrl());

            for (String apiUrl : apiUrls) {
                Request request = new Request.Builder().url(apiUrl + "node/configuration").build();
                Response response = null;
                try {
                    response = okHttpClient.newCall(request).execute();
                } catch (SocketTimeoutException ignore) {}
                catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                if (response != null && response.isSuccessful()) {
                    Map<String, Object> connectionConfig = new HashMap<>();
                    connectionConfig.put("host", apiUrl);
                    Connection connection = new Connection(connectionConfig);

                    return connection;
                }
            }

        }

        return null;
    }

    public boolean validateAddress(String address, String chainName) {
        TokenConfig tokenInfo= this.applicationConfig.getTokenConfigFromChainName(chainName);
        try {
            return Address.validate(address, tokenInfo.getNetwork());
        } catch (Exception e) {
            LOGGER.error(e.toString());
            return false;
        }
    }

    public Map<String, Object> getAddressInfo(String address, String chainName) {
        Connection connection = networkConnection.get(chainName);
        if (connection != null) {
            try {
                return connection.api().wallets.show(address);

            } catch (SocketTimeoutException e) {
                Connection recreatedConnection = this.createConnection(chainName);
                if (recreatedConnection != null) {
                    networkConnection.replace(chainName, recreatedConnection);
                    return this.getAddressInfo(address, chainName);

                } else {
                    LOGGER.error(e.toString());
                    e.printStackTrace();
                }
            } catch (IOException e) {
                LOGGER.error(e.toString());
                e.printStackTrace();
            }
        }

        return null;
    }

    public Transaction createTransaction(ApplicationConfig config, String chainName, String recipientAddress, long nonce) {
        TokenConfig tokenInfo= config.getTokenConfigFromChainName(chainName);
        if (!tokenInfo.isAslp()) {
            return new TransferBuilder()
                    .network(tokenInfo.getNetwork())
                    .recipient(recipientAddress)
                    .amount(tokenInfo.getRewardAmount())
                    .vendorField(tokenInfo.isAllowVendorField() ? tokenInfo.getVendorField() : null)
                    .fee(tokenInfo.getFee())
                    .nonce(nonce + 1)
                    .sign(tokenInfo.getPassphrase())
                    .transaction;
        } else {
            OkHttpClient okHttpClient = new OkHttpClient();
            try {
                Request request = new Request.Builder()
                        .url(tokenInfo.getAslpApiUrl() + "vendor_aslp1_send?tokenid=8259ce077b1e767227e5e0fce590d26d&quantity=" + tokenInfo.getAslpReward() + "&notes=" + tokenInfo.getVendorField())
                        .build();
                Response response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    String vendorField = response.body().string();
                    Transaction transaction = new TransferBuilder()
                            .network(tokenInfo.getNetwork())
                            .recipient(recipientAddress)
                            .amount(tokenInfo.getRewardAmount())
                            .vendorField(vendorField)
                            .fee(tokenInfo.getFee())
                            .nonce(nonce + 1)
                            .sign(tokenInfo.getPassphrase())
                            .transaction;
                    return transaction;
                }
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error(e.toString());

                return null;
            }
        }
    }

    public Map<String, Object> broadcastTransaction(Transaction transaction, String chainName) {
        Connection connection = networkConnection.get(chainName);
        if (connection != null) {
            ArrayList<Map<String, ?>> transactionPayload = new ArrayList<>();
            transactionPayload.add(transaction.toHashMap());

            try {
                return connection.api().transactions.create(transactionPayload);
            } catch (IOException e) {
                LOGGER.error(e.toString());
                e.printStackTrace();
            }
        }

        return null;
    }
}
