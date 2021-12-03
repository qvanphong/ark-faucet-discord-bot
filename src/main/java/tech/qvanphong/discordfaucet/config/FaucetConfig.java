package tech.qvanphong.discordfaucet.config;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("faucet")
@Getter
@Setter
@Scope("singleton")
public @Data
class FaucetConfig {

    Map<String, TokenConfig> tokens = new HashMap<>();

    String tokenSettingLocation;

    String aslpApiUrl;

    long defaultCoolDownMinutes;

    public TokenConfig getTokenConfigFromChainName(String chainName) {
        return this.tokens.get(chainName);
    }
}
