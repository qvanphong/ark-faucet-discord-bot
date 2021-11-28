package tech.qvanphong.discordfaucet.network.bind;

import org.arkecosystem.crypto.networks.INetwork;

public class BINDTestnet implements INetwork {
    @Override
    public int version() {
        return 90;
    }

    @Override
    public int wif() {
        return 170;
    }

    @Override
    public String epoch() {
        return null;
    }
}
