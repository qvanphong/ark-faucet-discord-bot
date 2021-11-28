package tech.qvanphong.discordfaucet.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public @Data
class TokenConfig {
    /*
     * Blockchain's restful api url
     * */
    private String apiUrl;

    /*
    * Blockchain's restful api url use for backup
    * */
    private List<String> backupApiUrl;

    /*
     * Blockchain's explorer url
     * */
    private String explorerUrl;

    /*
    * Token symbol
    * */
    private String tokenSymbol;

    /*
    * Wallet address in this chain
    * */
    private String senderAddress;

    /*
    * Network version
    * */
    private int network;

    /*
    * Fee to use in transaction
    * */
    private long fee;

    /*
    * Amount reward to user.
    * */
    private long rewardAmount;

    /*
    * Wallet passphrase to sign transactions.
    * */
    private String passphrase;

    /*
    * Is network allow vendor field
    * */
    private boolean allowVendorField = true;

    /*
    * vendor field content that will in transaction
    * */
    private String vendorField = "From ARK Faucet Discord bot with love";

    /*
    * Is token a ASLP token
    * */
    private boolean isAslp = false;

    /*
    * Amount ASLP token to send
    * */
    private long AslpReward;

    /*
    * Token id
    * */
    private String aslpTokenId;

    /*
    * ASLP Restful API
    * */
    private String aslpApiUrl = "https://aslp.qredit.dev/api/";
}
