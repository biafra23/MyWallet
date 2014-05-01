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
import java.util.Collection;
import java.util.Collections;
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
        final Address forwardingAddress = new Address(params, "1Dh2Pzbqe15QPsrHXrurLCHpY28K6ZGQMx"); //MultiBit ?
//        final Address forwardingAddress = new Address(params, "1KBmj2QQs9yiq3wW2jEJuVnuBybWvLv6fV"); // xapo
        // Start up a basic app using a class that automates some boilerplate.

        ECKey key = new ECKey(null, Hex.decode("02907f5606f848b94e7b1655601f695d1a58347ef117e1c907a8b30eafa36fab79"));
//        key.setCreationTimeSeconds(new Date().getTime() / 1000 - (60 * 60 * 24 * 1));
        final MyWalletAppKit kit = new MyWalletAppKit(params, new File("."), filePrefix, Collections.singletonList(key));

        kit.connectToLocalHost();
        kit.setAutoSave(false);

        Service.State fooState = kit.startAndWait();


        Log.info("State: " + fooState);


//        kit.wallet().addKey(key);


//        ECKey key = new ECKey(null, Hex.decode("0451b3f1e2bdbaef43169dbf814fbecfcf6e0ee0ae77f207a44e3de43ce623b4e65d3db1701a72fcd673377405e37f7acbec77b9662e3992a6f67a102e26b2716f"));
//        key.setCreationTimeSeconds(new Date().getTime());
//        kit.wallet().addKey(key);

//        kit.wallet().addWatchedAddress(forwardingAddress);

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


//        kit.wallet().addKey(new ECKey(null, Hex.decode("034c643969d8e3f821c9cc8f714824a7ff62645df9b8af12ac18fdfb44581a195e")));
        System.out.println("Wallet: " + kit.wallet());

//        kit.wallet().addKey(new ECKey(null, Hex.decode("57622f345f73e1acadf0de4ce367dff391afa3a7")));
//        kit.wallet().addKey(new ECKey(null, Hex.decode("c2f3689ab1d0d6cd8a0cc7ab4d947e8f0e755133")));
//        kit.wallet().addKey(new ECKey());

//        Wallet wallet = kit.wallet();
//        for (ECKey ecKey : wallet.getKeys()) {
//            System.out.println("Address: " + ecKey.toAddress(new MainNetParams()));
//        }


        System.out.println("You have : " + kit.wallet().getBalance(Wallet.BalanceType.AVAILABLE) + " Satoshi");
        System.exit(0);
    }
}
