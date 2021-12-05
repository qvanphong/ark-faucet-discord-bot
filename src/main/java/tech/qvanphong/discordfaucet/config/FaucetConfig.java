package tech.qvanphong.discordfaucet.config;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;

@ConfigurationProperties("faucet")
@Getter
@Setter
@Scope("singleton")
public @Data
class FaucetConfig {

    private String tokenSettingLocation;

    private String aslpApiUrl;

    private long defaultCoolDownMinutes;

}
