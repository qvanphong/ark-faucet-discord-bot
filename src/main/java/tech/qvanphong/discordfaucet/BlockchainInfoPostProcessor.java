package tech.qvanphong.discordfaucet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import tech.qvanphong.discordfaucet.config.FaucetConfig;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class BlockchainInfoPostProcessor implements BeanPostProcessor {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Override
    public final Object postProcessAfterInitialization(final Object bean, final String beanName) {
        if (bean instanceof FaucetConfig) {
            FaucetConfig config = (FaucetConfig) bean;
            config.getTokens().forEach((chainName, tokenInfo) -> {
                try {
                    String fileLocation = config.getPassphraseLocation() + chainName + ".ps";
                    BufferedReader br = new BufferedReader(new FileReader(fileLocation));

                    String passphrase = br.readLine();
                    tokenInfo.setPassphrase(passphrase);

                    br.close();
                } catch (FileNotFoundException e) {
                    LOGGER.error("Passphrase của " + chainName + " chưa được cung cấp");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        return bean;
    }
}
