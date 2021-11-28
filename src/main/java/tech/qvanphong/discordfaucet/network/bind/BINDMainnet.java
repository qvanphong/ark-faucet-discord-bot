package tech.qvanphong.discordfaucet.network.bind;

import org.arkecosystem.crypto.networks.INetwork;

public class BINDMainnet implements INetwork {
    @Override
    public int version() {
        return 88;
    }

    @Override
    public int wif() {
        return 171;
    }

    @Override
    public String epoch() {
        return null;
    }
}
