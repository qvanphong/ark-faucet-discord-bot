package tech.qvanphong.discordfaucet;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import tech.qvanphong.discordfaucet.config.FaucetConfig;
import tech.qvanphong.discordfaucet.config.TokenConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class BlockchainInfoPostProcessor implements BeanPostProcessor {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Override
    public final Object postProcessAfterInitialization(final Object bean, final String beanName) {
        if (bean instanceof FaucetConfig) {
            String fileExtension = ".token";
            FaucetConfig config = (FaucetConfig) bean;
            Gson gson = new Gson();

            File tokenSettingDir = new File(config.getTokenSettingLocation());
            File[] tokenSettingFiles = tokenSettingDir.listFiles((dir, name) -> name.endsWith(fileExtension));
            for (File tokenSettingFile : tokenSettingFiles) {
                try {
                    String tokenSettingContent = Files.readString(tokenSettingFile.toPath(), StandardCharsets.UTF_8);
                    TokenConfig tokenConfig = gson.fromJson(tokenSettingContent, TokenConfig.class);
                    config.getTokens().put(tokenSettingFile.getName().replace(fileExtension, ""), tokenConfig);

                } catch (IOException e) {
                    e.printStackTrace();
                    LOGGER.error(e.getMessage());
                }
            }
        }
        return bean;
    }
}
