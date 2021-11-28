package tech.qvanphong.discordfaucet.utility;

import org.arkecosystem.client.Connection;
import org.arkecosystem.crypto.transactions.builder.TransferBuilder;
import org.arkecosystem.crypto.transactions.types.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.qvanphong.discordfaucet.config.ApplicationConfig;
import tech.qvanphong.discordfaucet.config.TokenConfig;

import java.io.IOException;
import java.util.Map;

public class TransactionUtility {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public Transaction createRewardTransaction(ApplicationConfig config, Connection connection, String tokenName, String recipientAddress) {
        if (config.getToken().containsKey(tokenName)) {
            TokenConfig tokenInfo= config.getTokenConfigFromChainName(tokenName);
            long nonce = 0;

            if ( tokenInfo.getPassphrase() == null || tokenInfo.getPassphrase().isEmpty() ) {
                this.LOGGER.error("Token " + tokenName + " tạm thời ngưng hỗ trợ.");
                return  null;
            }

            try {
                Map<String, Object> addressInfo = connection.api().wallets.show(tokenInfo.getSenderAddress());
                if (addressInfo.containsKey("data") && ((Map<String, String>)addressInfo.get("data")).get("nonce") != null) {
                    nonce = Long.parseLong(((Map<String, String>)addressInfo.get("data")).get("nonce"));
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.LOGGER.error("Có lỗi khi tương tác với network.");
                return  null;
            }


            return new TransferBuilder()
                    .network(tokenInfo.getNetwork())
                    .recipient(recipientAddress)
                    .amount(tokenInfo.getRewardAmount())
                    .fee(tokenInfo.getFee())
                    .nonce(nonce + 1)
                    .sign(tokenInfo.getPassphrase())
                    .transaction;
        }
        return null;
    }
}
