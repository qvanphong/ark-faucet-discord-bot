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
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenConfigReader {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private FaucetConfig faucetConfig;
    private Map<Long, Map<String, TokenConfig>> guildTokenConfigs;

    @Autowired
    public TokenConfigReader(FaucetConfig faucetConfig, Map<Long, Map<String, TokenConfig>> guildTokenConfigs) {
        this.faucetConfig = faucetConfig;
        this.guildTokenConfigs = guildTokenConfigs;
    }


    public void readTokenConfig(long guildId) {
        String fileExtension = ".token";
        Gson gson = new Gson();

        File tokenSettingDir = new File(faucetConfig.getTokenSettingLocation() + guildId);
        if (!tokenSettingDir.exists()) {
            try {
                Files.createDirectories(tokenSettingDir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File[] tokenSettingFiles = tokenSettingDir.listFiles((dir, name) -> name.endsWith(fileExtension));

        guildTokenConfigs.remove(guildId);

        if (tokenSettingFiles != null && tokenSettingFiles.length > 0) {
            for (File tokenSettingFile : tokenSettingFiles) {
                try {
                    Map<String, TokenConfig> guildTokenConfig = guildTokenConfigs.computeIfAbsent(guildId, k -> new HashMap<>());

                    String tokenSettingContent = Files.readString(tokenSettingFile.toPath(), StandardCharsets.UTF_8);
                    TokenConfig tokenConfigContent = gson.fromJson(tokenSettingContent, TokenConfig.class);


                    guildTokenConfig.put(tokenSettingFile.getName().replace(fileExtension, ""), tokenConfigContent);

                } catch (IOException e) {
                    e.printStackTrace();
                    LOGGER.error(e.getMessage());
                }
            }
        }

    }
}
