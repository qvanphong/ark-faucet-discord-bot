package tech.qvanphong.discordfaucet;

import org.arkecosystem.client.Connection;
import org.arkecosystem.crypto.configuration.Network;
import org.arkecosystem.crypto.identities.Address;
import org.arkecosystem.crypto.identities.WIF;
import org.arkecosystem.crypto.networks.Devnet;
import org.arkecosystem.crypto.transactions.builder.TransferBuilder;
import org.arkecosystem.crypto.transactions.types.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.qvanphong.discordfaucet.config.FaucetConfig;
import tech.qvanphong.discordfaucet.config.TokenConfig;
import tech.qvanphong.discordfaucet.entity.User;
import tech.qvanphong.discordfaucet.network.bind.BINDTestnet;
import tech.qvanphong.discordfaucet.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ApplicationTests {
// testnet address

    private String senderAddress = "dEPmnE78qW4N3jM2FhGmeN3yG62CujkiUL";
    private String recipientAddress = "dLLQ3orMbXbYp6BeDPyet94Rm6b9G5B9ov";
    private String senderPrivateKey = "";
    private String BINDApi = "https://api.nos.dev/api/v2/";

//      private String senderAddress = "D9FnCNZwpsmtSHZJBnXWG5Wg5iMZeBo7dp";
//    private String recipientAddress = "DA8irG3TK7WEoK4YJn9A3gHm5XrNUk6UwQ";
//    private String senderPrivateKey = "";
//    private String BINDApi = "http://167.114.43.53:4003/api/";



    @Test
    void contextLoads() {
    }

    @Test
    void connectToNetworkTest() {
        Map<String, Object> connectionProperties = new HashMap<>();
        connectionProperties.put("host", BINDApi);
        Connection connection = new Connection(connectionProperties);

        try {
            Map<String, Object> transaction = connection.api().transactions.allUnconfirmed();
            System.out.println(transaction);
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void validateAddressTest() {
        Network.set(new Devnet());

        System.out.println(Address.validate(senderAddress));
    }

    @Test
    void getWIFFromPrivateTest() {
        Network.set(new BINDTestnet());
        try {
            String wif = WIF.fromPassphrase(senderPrivateKey);
            System.out.println(wif);

            assert true;

        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void sendingTransactionTest(@Autowired FaucetConfig config) {
        long nonce = 0L;
        Network.set(new Devnet());
        TokenConfig tokenInfo= config.getTokenConfigFromChainName("bark");

        Map<String, Object> connectionProperties = new HashMap<>();
        connectionProperties.put("host", tokenInfo.getApiUrl());
        Connection clientConnection = new Connection(connectionProperties);


        try {
            Map<String, Object> walletInformation = clientConnection.api().wallets.show(tokenInfo.getSenderAddress());
            Map<String, Object> responseData = (Map<String, Object>) walletInformation.get("data");


            if (responseData != null && responseData.containsKey("nonce")) {
                nonce = Long.parseLong((String) ((Map<?, ?>) responseData).get("nonce"));
            } else {
                assert false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
            return;
        }


        TransferBuilder transferBuilder = new TransferBuilder();
        Transaction transaction = transferBuilder
                .network(tokenInfo.getNetwork())
                .recipient(recipientAddress)
                .amount(tokenInfo.getRewardAmount())
                .vendorField("{\"aslp1\":{\"tp\":\"SEND\",\"id\":\"8259ce077b1e767227e5e0fce590d26d\",\"qt\":\"1\",\"no\":\"" + tokenInfo.getVendorField() + "\"}}\n")
                .fee(tokenInfo.getFee())
                .nonce(nonce + 1)
                .sign(tokenInfo.getPassphrase())
                .transaction;

        System.out.println(transaction.verify());

        System.out.println("Created Transaction: " + transaction);
        System.out.println(transaction.verify());

        ArrayList<Map<String, ?>> transactionPayload = new ArrayList<>();
        transactionPayload.add(transaction.toHashMap());

        try {
            Map<String, Object> broadCastedTransaction = clientConnection.api().transactions.create(transactionPayload);
            System.out.println(broadCastedTransaction);

            assert !broadCastedTransaction.containsKey("error");

        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
     void getUserListTest(UserService userService) {
        List<User> users = userService.getUsers();
        assert  !users.isEmpty();

        users.forEach(user -> System.out.println(user.getId()));
    }
}
