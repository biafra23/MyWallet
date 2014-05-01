package com.jaeckel.mywallet;

import com.google.bitcoin.core.*;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.utils.BriefLogFormatter;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.math.BigInteger;
import java.util.*;


public class CheckBalance {
    public static final Logger Log = LoggerFactory.getLogger(CheckBalance.class);

    private long timestamp;
    private byte[] pubKey;
    private String hexPubKey;

    public static void main(String[] args) throws Exception {
        new CheckBalance("034c643969d8e3f821c9cc8f714824a7ff62645df9b8af12ac18fdfb44581a195e", 1398801057L).run();
        System.exit(0);
    }

    public CheckBalance(String hexPubKey, long timestamp) {

        this.pubKey = Hex.decode(hexPubKey);
        this.timestamp = timestamp;
        this.hexPubKey = hexPubKey;
    }

    public long run() throws Exception {

        // This line makes the log output more compact and easily read, especially when using the JDK log adapter.
        BriefLogFormatter.init();
        // Figure out which network we should connect to. Each one gets its own set of files.
        final NetworkParameters params = new MainNetParams();
        ECKey key = new ECKey(null, pubKey);
        key.setCreationTimeSeconds(timestamp);

        List<ECKey> keyList = new ArrayList<ECKey>();
        keyList.add(key);
        final MyWalletAppKit kit = new MyWalletAppKit(params, new File("."), hexPubKey + "-" + timestamp, keyList);

//        kit.connectToLocalHost();
        kit.setAutoSave(true);
        kit.setBlockingStartup(true);

        Service.State fooState = kit.startAndWait();

        kit.wallet().addEventListener(new AbstractWalletEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
                System.out.println("onWalletChanged -> Balance: " + wallet.getBalance(Wallet.BalanceType.AVAILABLE));

            }
        });

        Log.info("State: " + fooState);
        System.out.println("Wallet: " + kit.wallet());
        System.out.println("You have : " + kit.wallet().getBalance(Wallet.BalanceType.AVAILABLE) + " Satoshi (available)");
        System.out.println("You have : " + kit.wallet().getBalance(Wallet.BalanceType.ESTIMATED) + " Satoshi (estimated)");

        long result = kit.wallet().getBalance(Wallet.BalanceType.ESTIMATED).longValue();

        kit.stopAndWait();

        return result;

    }

}
