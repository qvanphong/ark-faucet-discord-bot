package tech.qvanphong.discordfaucet.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import tech.qvanphong.discordfaucet.entity.TokenConfigPK;

import javax.persistence.*;

@Getter
@Setter
@Data
@Entity
@Table(name = "token_configs")
@IdClass(TokenConfigPK.class)
public class TokenConfig {

    @Id
    @SerializedName("name")
    @Column(nullable = false)
    private String name;

    @Id
    @SerializedName("guild_id")
    @Column(nullable = false)
    private long guildId;

    /*
     * Blockchain's explorer url
     * */
    @SerializedName("api_url")
    @Column(nullable = false)
    private String apiUrl;

    /*
     * Blockchain's explorer url
     * */
    @SerializedName("explorer_url")
    @Column(nullable = false)
    private String explorerUrl;

    /*
    * Token symbol
    * */
    @SerializedName("symbol")
    @Column(nullable = false)
    private String tokenSymbol;

    /*
    * Wallet address in this chain
    * */
    @SerializedName("sender_address")
    @Column(nullable = false)
    private String senderAddress;

    /*
    * Network version
    * */
    @SerializedName("network")
    @Column(nullable = false)
    private int network;

    /*
    * Fee to use in transaction
    * */
    @SerializedName("fee")
    @Column(nullable = false)
    private long fee;

    /*
    * Amount reward to user.
    * */
    @SerializedName("reward_amount")
    @Column(nullable = false)
    private long rewardAmount;

    /*
    * Wallet passphrase to sign transactions.
    * */
    @SerializedName("passphrase")
    @Column(nullable = false)
    private String passphrase;

    /*
     * Wallet's 2nd passphrase to sign transactions.
     * */
    @SerializedName("second_passphrase")
    @Column(nullable = true)
    private String secondPassphrase;

    /*
    * Is network allow vendor field
    * */
    @SerializedName("allow_vendor_field")
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean allowVendorField = true;

    /*
    * vendor field content that will in transaction
    * */
    @SerializedName("vendor_field")
    @Column(nullable = false, columnDefinition = "varchar(255) default 'From ARK Faucet Discord bot with love'")
    private String vendorField = "From ARK Faucet Discord bot with love";

    /*
    * Is token a ASLP token
    * */
    @SerializedName("is_aslp")
    @Column(columnDefinition = "boolean default false")
    private boolean isAslp = false;

    /*
    * Amount ASLP token to send
    * */
    @SerializedName("aslp_reward")
    @Column(nullable = true)
    private long AslpReward;

    /*
    * Token id
    * */
    @SerializedName("aslp_token_id")
    @Column(nullable = true)
    private String aslpTokenId;

    /*
    * ASLP Restful API
    * */
    @SerializedName("aslp_api_url")
    @Column(nullable = true, columnDefinition = "varchar(255) default 'https://aslp.qredit.dev/api/'")
    private String aslpApiUrl = "https://aslp.qredit.dev/api/";

    @Column(columnDefinition = "boolean default false")
    private boolean isDisabled;

    @Override
    public String toString() {
        return "TokenConfig{" +
                ", name='" + name + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                ", explorerUrl='" + explorerUrl + '\'' +
                ", tokenSymbol='" + tokenSymbol + '\'' +
                ", senderAddress='" + senderAddress + '\'' +
                ", network=" + network +
                ", fee=" + fee +
                ", rewardAmount=" + rewardAmount +
                ", allowVendorField=" + allowVendorField +
                ", vendorField='" + vendorField + '\'' +
                ", isAslp=" + isAslp +
                ", AslpReward=" + AslpReward +
                ", aslpTokenId='" + aslpTokenId + '\'' +
                ", aslpApiUrl='" + aslpApiUrl + '\'' +
                ", guildId=" + guildId +
                '}';
    }
}
