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
import tech.qvanphong.discordfaucet.config.TokenConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ARKClientUtility {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final TokenConfig tokenConfig;
    private final Connection connection;
    private boolean isConnectionClosed = false;

    public ARKClientUtility(TokenConfig tokenConfig) {
        this.tokenConfig = tokenConfig;
        Map<String, Object> connectionConfig = new HashMap<>();
        connectionConfig.put("host", tokenConfig.getApiUrl());
        connection = new Connection(connectionConfig);
    }

    //
//    public Connection createConnection(String chainName) {
//        TokenConfig tokenConfig= faucetConfig.getTokenConfigFromChainName(chainName);
//        if (!tokenConfig.getBackupApiUrls().isEmpty()) {
//            OkHttpClient okHttpClient = new OkHttpClient();
//
//            // Add default api to list. In order to reconnect to default api url
//            ArrayList<String> apiUrls = new ArrayList<>(tokenConfig.getBackupApiUrls());
//            apiUrls.add(tokenConfig.getApiUrl());
//
//            for (String apiUrl : apiUrls) {
//                Request request = new Request.Builder().url(apiUrl + "node/configuration").build();
//                Response response = null;
//                try {
//                    response = okHttpClient.newCall(request).execute();
//                } catch (SocketTimeoutException ignore) {}
//                catch (IOException e) {
//                    e.printStackTrace();
//                    continue;
//                }
//
//                if (response != null && response.isSuccessful()) {
//                    Map<String, Object> connectionConfig = new HashMap<>();
//                    connectionConfig.put("host", apiUrl);
//                    Connection connection = new Connection(connectionConfig);
//
//                    return connection;
//                }
//            }
//
//        }
//
//        return null;
//    }

    public boolean validateAddress(String address, TokenConfig tokenConfig) {
        try {
            return Address.validate(address, tokenConfig.getNetwork());
        } catch (Exception e) {
            LOGGER.error(e.toString());
            return false;
        }
    }

    public Map<String, Object> getAddressInfo(String address) {
        try {
            return connection.api().wallets.show(address);

        } catch (IOException e) {
            LOGGER.error(e.toString());
            e.printStackTrace();
//                Connection recreatedConnection = this.createConnection(chainName);
//                if (recreatedConnection != null) {
//                    networkConnection.replace(chainName, recreatedConnection);
//                    return this.getAddressInfo(address, chainName);
//
//                } else {
//                    LOGGER.error(e.toString());
//                    e.printStackTrace();
//                }
        }

        return null;
    }

    public Transaction createTransaction(String recipientAddress, long nonce) {
        if (!tokenConfig.isAslp()) {
            return new TransferBuilder()
                    .network(tokenConfig.getNetwork())
                    .recipient(recipientAddress)
                    .amount(tokenConfig.getRewardAmount())
                    .vendorField(tokenConfig.isAllowVendorField() ? tokenConfig.getVendorField() : null)
                    .fee(tokenConfig.getFee())
                    .nonce(nonce + 1)
                    .sign(tokenConfig.getPassphrase())
                    .transaction;
        } else {

            OkHttpClient okHttpClient = connection.client().getClient();
            try {
                Request request = new Request.Builder()
                        .url(tokenConfig.getAslpApiUrl() + "vendor_aslp1_send?tokenid=8259ce077b1e767227e5e0fce590d26d&quantity=" + tokenConfig.getAslpReward() + "&notes=" + tokenConfig.getVendorField())
                        .build();
                Response response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    String vendorField = response.body().string();
                    Transaction transaction = new TransferBuilder()
                            .network(tokenConfig.getNetwork())
                            .recipient(recipientAddress)
                            .amount(tokenConfig.getRewardAmount())
                            .vendorField(vendorField)
                            .fee(tokenConfig.getFee())
                            .nonce(nonce + 1)
                            .sign(tokenConfig.getPassphrase())
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

    public Map<String, Object> broadcastTransaction(Transaction transaction) {
        ArrayList<Map<String, ?>> transactionPayload = new ArrayList<>();
        transactionPayload.add(transaction.toHashMap());

        try {
            return connection.api().transactions.create(transactionPayload);
        } catch (IOException e) {
            LOGGER.error(e.toString());
            e.printStackTrace();
        }

        return null;
    }

    public void shutdownConnection() {
        if (!isConnectionClosed) {
            connection.client().getClient().dispatcher().executorService().shutdown();
            connection.client().getClient().connectionPool().evictAll();;
            isConnectionClosed = true;
        }
    }
}
