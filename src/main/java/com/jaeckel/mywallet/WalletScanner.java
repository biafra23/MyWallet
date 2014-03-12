package com.jaeckel.mywallet;

import com.google.bitcoin.core.*;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.SPVBlockStore;
import com.google.bitcoin.store.UnreadableWalletException;
import com.jaeckel.mywallet.listener.WalletListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

public class WalletScanner {

    public static final Logger Log = LoggerFactory.getLogger(com.jaeckel.mywallet.WalletScanner.class);
    //    private Wallet wallet;
    private List<Wallet> wallets = new ArrayList<Wallet>();

    public static void main(String[] args) throws InterruptedException, UnreadableWalletException {

        new WalletScanner().run();

    }

    public void run() throws InterruptedException, UnreadableWalletException {


//        ECKey ev3Key = showWallet("/Users/biafra/ev3_spv_wallet_file");

        MainNetParams netParams = new MainNetParams();
        Log.info("Got netParams: " + netParams);


        for (int i = 0; i < 1000; i++) {
            Wallet wallet = new Wallet(netParams);
            ECKey key = new ECKey(new BigInteger("" + i));
            wallet.addKey(key);
            wallets.add(wallet);
        }

//        ECKey eckey = new ECKey(null, Hex.decode("02907f5606f848b94e7b1655601f695d1a58347ef117e1c907a8b30eafa36fab79"));
//        Log.debug(" eckey.toAddress: " + eckey.toAddress(netParams));
//
//        wallet.addKey(eckey);
//        wallet.addKey(ev3Key);

//        for (ECKey key : wallet.getKeys()) {
//            Log.debug(" toAddress: " + key.toAddress(netParams));
//            Log.debug("    pubKey: " + bytesToHexString(key.getPubKey()));
//            if (key.getPrivKeyBytes() != null) {
//                Log.debug("   privKey: " + bytesToHexString(key.getPrivKeyBytes()));
//            }
//        }

        logNonEmptyWallets();

//        System.exit(0);


        File blockStoreFile = new File("ws_spv_block_store");
        try {
            SPVBlockStore blockStore = new SPVBlockStore(netParams, blockStoreFile);
            Log.info("SPVBlockStore instantiated");

            InputStream stream = getClass().getClassLoader().getResourceAsStream("checkpoints");
            CheckpointManager.checkpoint(netParams, stream, blockStore, netParams.getGenesisBlock().getTimeSeconds() * 1000);
            BlockChain blockChain = new BlockChain(netParams, wallets.get(0), blockStore);

            PeerGroup peerGroup = new PeerGroup(netParams, blockChain);

            for (Wallet wallet : wallets) {

                blockChain.addWallet(wallet);
                peerGroup.addWallet(wallet);

//            peerGroup.setFastCatchupTimeSecs(netParams.getGenesisBlock().getTimeSeconds());
                peerGroup.addPeerDiscovery(new DnsDiscovery(netParams));

                Log.info("Starting peerGroup ...");
                peerGroup.startAndWait();

                Log.info("Installing WalletListener!");
                WalletEventListener walletEventListener = new WalletListener();

                wallet.addEventListener(walletEventListener);
            }

            peerGroup.startBlockChainDownload(new DownloadListener() {
                @Override
                protected void progress(double pct, int blocksSoFar, Date date) {
                    super.progress(pct, blocksSoFar, date);

                    logNonEmptyWallets();

                }
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


        Thread.sleep(100 * 1000);
        logNonEmptyWallets();

    }

    private void logNonEmptyWallets() {
        for (Wallet wallet : wallets) {
            if (wallet.getBalance().compareTo(new BigInteger("0")) > 0) {

                Log.debug("secKey: " + showBytes(wallet.getKeys().get(0).getPrivKeyBytes()) + ": Balance: " + wallet.getBalance());
            }
        }
    }

    private String showBytes(byte[] privKeyBytes) {
        StringBuffer result = new StringBuffer();

        for(byte b :privKeyBytes){
            result.append(b + " ");
        }
        return result.toString();
    }

    private ECKey showWallet(String s) throws UnreadableWalletException {
        Wallet wallet = Wallet.loadFromFile(new File(s));

        for (int i = 0; i < wallet.getKeys().size(); i++) {
            Log.info("Key: " + wallet.getKeys().get(i).toAddress(new MainNetParams()));
            return wallet.getKeys().get(i);
        }

        return null;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return sb.toString();
    }
}
