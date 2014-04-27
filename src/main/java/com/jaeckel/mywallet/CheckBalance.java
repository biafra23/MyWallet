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
import java.util.Date;
import java.util.List;


public class CheckBalance {
    public static final Logger Log = LoggerFactory.getLogger(CheckBalance.class);

    private BigInteger oldBalance;

    public static void main(String[] args) throws Exception {
       new CheckBalance().run();
    }

    public void run() throws Exception {

        // This line makes the log output more compact and easily read, especially when using the JDK log adapter.
        BriefLogFormatter.init();
        // Figure out which network we should connect to. Each one gets its own set of files.
        final NetworkParameters params = new MainNetParams();
        final String filePrefix = "forwarding-service-testnet";
        // Parse the address given as the first parameter.
//        final Address forwardingAddress = new Address(params, "1CPfLk1oAJwzFSUa7PrkfFyn5YE1zwUBjV"); //note, I replace bogusHash when I really run
        final Address forwardingAddress = new Address(params, "1Dh2Pzbqe15QPsrHXrurLCHpY28K6ZGQMx"); //note, I replace bogusHash when I really run
        // Start up a basic app using a class that automates some boilerplate.
        final WalletAppKit kit = new WalletAppKit(params, new File("."), filePrefix);
//        kit.connectToLocalHost();
        kit.setAutoSave(true);

        Service.State fooState = kit.startAndWait();

        Log.info("State: " + fooState);

        ECKey key = new ECKey(null, Hex.decode("02907f5606f848b94e7b1655601f695d1a58347ef117e1c907a8b30eafa36fab79"));
        key.setCreationTimeSeconds(new Date().getTime());
        kit.wallet().addKey(key);

        kit.wallet().addWatchedAddress(forwardingAddress);

        kit.wallet().addEventListener(new AbstractWalletEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
                System.out.println("onWalletChanged -> Balance: " + wallet.getBalance(Wallet.BalanceType.AVAILABLE));

            }
        });

//        System.out.println("Wallet: " + kit.wallet());
//        kit.wallet().addKey(new ECKey(null, Hex.decode("02907f5606f848b94e7b1655601f695d1a58347ef117e1c907a8b30eafa36fab79")));
//        kit.wallet().addKey(new ECKey(null, Hex.decode("57622f345f73e1acadf0de4ce367dff391afa3a7"))); // sec key 0
        //1CjgioGLRpLyEiFSsLpYdEKfaFjWg3ZWSS
//        kit.wallet().addKey(new ECKey(null, Hex.decode("0210fdade86b268597e9aa4f2adc314fe459837be831aeb532f04b32c160b4e50a")));
        System.out.println("Wallet: " + kit.wallet());

//        kit.wallet().addKey(new ECKey(null, Hex.decode("57622f345f73e1acadf0de4ce367dff391afa3a7")));
//        kit.wallet().addKey(new ECKey(null, Hex.decode("c2f3689ab1d0d6cd8a0cc7ab4d947e8f0e755133")));
//        kit.wallet().addKey(new ECKey());

//        Wallet wallet = kit.wallet();
//        for (ECKey ecKey : wallet.getKeys()) {
//            System.out.println("Address: " + ecKey.toAddress(new MainNetParams()));
//        }


        System.out.println("You have : " + kit.wallet().getBalance(Wallet.BalanceType.AVAILABLE) + " Satoshi");
//        System.exit(0);
    }
}
