package tech.qvanphong.discordfaucet.utility;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.qvanphong.discordfaucet.config.FaucetConfig;
import tech.qvanphong.discordfaucet.config.TokenConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Component
public class FaucetUtility {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private FaucetConfig faucetConfig;

    @Autowired
    public FaucetUtility(FaucetConfig faucetConfig) {
        this.faucetConfig = faucetConfig;
    }

    public void readConfig() {
        String fileExtension = ".token";
        Gson gson = new Gson();

        File tokenSettingDir = new File(faucetConfig.getTokenSettingLocation());
        File[] tokenSettingFiles = tokenSettingDir.listFiles((dir, name) -> name.endsWith(fileExtension));
        for (File tokenSettingFile : tokenSettingFiles) {
            try {
                String tokenSettingContent = Files.readString(tokenSettingFile.toPath(), StandardCharsets.UTF_8);
                TokenConfig tokenConfig = gson.fromJson(tokenSettingContent, TokenConfig.class);
                faucetConfig.getTokens().put(tokenSettingFile.getName().replace(fileExtension, ""), tokenConfig);

            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            }
        }
    }
}
