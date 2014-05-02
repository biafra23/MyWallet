package com.jaeckel.mywallet;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.kits.WalletAppKit;

import java.io.File;

public class MyWalletAppKit extends WalletAppKit {


    private ECKey key;
    private Address address;
    private long timestamp;

    public MyWalletAppKit(NetworkParameters params, File directory, String filePrefix, ECKey ecKey) {
        super(params, directory, filePrefix);
        this.key = ecKey;

    }

    public MyWalletAppKit(NetworkParameters params, File directory, String filePrefix, Address address, long timestamp) {
        super(params, directory, filePrefix);
        this.address = address;
        this.timestamp = timestamp;
    }
    @Override
    protected void onSetupCompleted() {
        super.onSetupCompleted();

        for (ECKey key : vWallet.getKeys()) {
            vWallet.removeKey(key);
        }

        if (key != null) {
            vWallet.addKey(key);
        }

        if (address != null) {
            vWallet.addWatchedAddress(address, timestamp);
        }
    }
}

