package tech.qvanphong.discordfaucet;

import org.springframework.beans.factory.config.BeanPostProcessor;
import tech.qvanphong.discordfaucet.config.FaucetConfig;

public class BlockchainInfoPostProcessor implements BeanPostProcessor {

    @Override
    public final Object postProcessAfterInitialization(final Object bean, final String beanName) {
        if (bean instanceof FaucetConfig) {
//            FaucetUtility faucetUtility = new FaucetUtility((FaucetConfig) bean);
//            faucetUtility.readConfig();
        }
        return bean;
    }
}
