package tech.qvanphong.discordfaucet.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public @Data
class TokenConfig {
    /*
     * Blockchain's explorer url
     * */
    @SerializedName("explorer_url")
    private String explorerUrl;

    /*
    * Token symbol
    * */
    @SerializedName("symbol")
    private String tokenSymbol;

    /*
    * Wallet address in this chain
    * */
    @SerializedName("sender_address")
    private String senderAddress;

    /*
    * Network version
    * */
    @SerializedName("network")
    private int network;

    /*
    * Fee to use in transaction
    * */
    @SerializedName("fee")
    private long fee;

    /*
    * Amount reward to user.
    * */
    @SerializedName("reward_amount")
    private long rewardAmount;

    /*
    * Wallet passphrase to sign transactions.
    * */
    @SerializedName("passphrase")
    private String passphrase;

    /*
    * Is network allow vendor field
    * */
    @SerializedName("allow_vendor_field")
    private boolean allowVendorField = true;

    /*
    * vendor field content that will in transaction
    * */
    @SerializedName("vendor_field")
    private String vendorField = "From ARK Faucet Discord bot with love";

    /*
    * Is token a ASLP token
    * */
    @SerializedName("is_aslp")
    private boolean isAslp = false;

    /*
    * Amount ASLP token to send
    * */
    @SerializedName("aslp_reward")
    private long AslpReward;

    /*
    * Token id
    * */
    @SerializedName("aslp_token_id")
    private String aslpTokenId;

    /*
    * ASLP Restful API
    * */
    @SerializedName("aslp_api_url")
    private String aslpApiUrl = "https://aslp.qredit.dev/api/";
}
