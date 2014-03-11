package com.jaeckel.mywallet;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.CheckpointManager;
import com.google.bitcoin.core.DownloadListener;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.WalletEventListener;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.SPVBlockStore;
import com.google.bitcoin.store.UnreadableWalletException;
import com.google.bitcoin.uri.BitcoinURI;
import com.google.bitcoin.wallet.WalletFiles;

import com.jaeckel.mywallet.listener.WalletListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Main implements Runnable {

    public static final Logger Log = LoggerFactory.getLogger(com.jaeckel.mywallet.Main.class);

    public static void main(String[] args) {
        Log.info("-> main()");
        new Thread(new com.jaeckel.mywallet.Main()).start();

    }

    public void run() {
        Log.info("-> run()");

        MainNetParams netParams = new MainNetParams();
        Log.info("Got netParams: " + netParams);

        Wallet wallet = getWallet(netParams);
        Log.info("Got wallet.");

        List<ECKey> keys = wallet.getKeys();
        Address address = keys.get(0).toAddress(netParams);


        String uri = BitcoinURI.convertToBitcoinURI(address.toString(),
                new BigInteger("10000"), "MY Wallet", "");

        Log.info("Wallet address: URI: " + uri);
        int widthHeight = 150;

        QrHelper.encodeAndWriteQrCode(uri, widthHeight);

        File blockStoreFile = new File("ev3_spv_block_store");
        long offset = 0; // 86400 * 30;
        try {
            SPVBlockStore blockStore = new SPVBlockStore(netParams, blockStoreFile);
            Log.info("SPVBlockStore instantiated");

            InputStream stream = getClass().getClassLoader().getResourceAsStream("checkpoints");
            CheckpointManager.checkpoint(netParams, stream, blockStore,
                    wallet.getEarliestKeyCreationTime() - offset);

            BlockChain blockChain = new BlockChain(netParams, wallet, blockStore);


            PeerGroup peerGroup = new PeerGroup(netParams, blockChain);
            peerGroup.addWallet(wallet);
            peerGroup.setFastCatchupTimeSecs(wallet.getEarliestKeyCreationTime() - offset);
            peerGroup.addPeerDiscovery(new DnsDiscovery(netParams));

            Log.info("Starting peerGroup ...");
            peerGroup.startAndWait();

            Log.info("Installing WalletListener!");
            WalletEventListener walletEventListener = new WalletListener();
            wallet.addEventListener(walletEventListener);

            peerGroup.startBlockChainDownload(new DownloadListener() {

            });
        } catch (BlockStoreException e) {
            Log.error("Caught BlockStoreException: ", e);
        } catch (UnknownHostException x) {
            Log.error("Caught UnknownHostException: ", x);
        } catch (FileNotFoundException c) {
            Log.error("Caught FileNotFoundException: ", c);
        } catch (IOException ie) {
            Log.error("Caught IOException: ", ie);
        }
    }

    public static Wallet getWallet(MainNetParams netParams) {
        Log.info("getWallet...");

        File walletFile = new File("my_spv_wallet_file");
        Wallet wallet;
        try {
            wallet = Wallet.loadFromFile(walletFile);
        } catch (UnreadableWalletException e) {
            wallet = new Wallet(netParams);
            ECKey key = new ECKey();
            wallet.addKey(key);
            try {
                wallet.saveToFile(walletFile);
            } catch (IOException a) {
                Log.error("Caught IOException: ", a);
            }
        }

        wallet.autosaveToFile(walletFile, 60, TimeUnit.SECONDS, new WalletFiles.Listener() {
            @Override
            public void onBeforeAutoSave(File tempFile) {
                Log.info("onBeforeAutoSave()");
            }

            @Override
            public void onAfterAutoSave(File newlySavedFile) {
                Log.info("onAfterAutoSave(): newlySavedFile: " + newlySavedFile);
            }
        });
        return wallet;
    }

}



