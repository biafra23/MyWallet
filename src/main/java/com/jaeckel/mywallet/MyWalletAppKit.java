package com.jaeckel.mywallet;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.kits.WalletAppKit;

import java.io.File;
import java.util.List;

public class MyWalletAppKit extends WalletAppKit {


    private List<ECKey> keys;

    public MyWalletAppKit(NetworkParameters params, File directory, String filePrefix, List<ECKey> keys) {
        super(params, directory, filePrefix);
        this.keys = keys;
    }


    @Override
    protected void onSetupCompleted() {
        super.onSetupCompleted();
        for (ECKey key : vWallet.getKeys()) {
            vWallet.removeKey(key);
        }
        for (ECKey key : keys) {
            vWallet.addKey(key);
        }
    }
}

