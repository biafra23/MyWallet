package com.jaeckel.mywallet.listener;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.WalletEventListener;
import com.google.bitcoin.script.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

public class WalletListener implements WalletEventListener {

    public static final Logger slf4jLogger = LoggerFactory.getLogger(com.jaeckel.mywallet.Main.class);


    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
        slf4jLogger.info("-----> received: prevBalance: " + prevBalance);
        slf4jLogger.info("-----> received: newBalance: " + newBalance);
        slf4jLogger.info("-----> received: " + (newBalance.subtract(prevBalance)));
    }

    @Override
    public void onCoinsSent(Wallet wallet, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
        //slf4jLogger.info("-----> sent coins.");
    }

    @Override
    public void onReorganize(Wallet wallet) {
        //slf4jLogger.info("-----> onReorganize()");
    }

    @Override
    public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
        //slf4jLogger.info("-----> onTransactionConfidenceChanged()");
    }

    @Override
    public void onWalletChanged(Wallet wallet) {
    }

    @Override
    public void onKeysAdded(Wallet wallet, List<ECKey> keys) {
        //slf4jLogger.info("-----> onKeysAdded()");
    }

    @Override
    public void onScriptsAdded(Wallet wallet, List<Script> scripts) {
        //slf4jLogger.info("-----> onScriptsAdded()");
    }
}
