package tech.qvanphong.discordfaucet.config;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("app")
@Getter
@Setter
public @Data
class ApplicationConfig {

    Map<String, TokenConfig> token = new HashMap<>();

    String passphraseLocation;

    public TokenConfig getTokenConfigFromChainName(String chainName) {
        return this.token.get(chainName);
    }
}
