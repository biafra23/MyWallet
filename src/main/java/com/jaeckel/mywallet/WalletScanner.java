package com.jaeckel.mywallet;

import com.google.bitcoin.core.*;
import com.google.bitcoin.net.BlockingClient;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.net.discovery.PeerDiscovery;
import com.google.bitcoin.net.discovery.PeerDiscoveryException;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.SPVBlockStore;
import com.google.bitcoin.store.UnreadableWalletException;
import com.jaeckel.mywallet.listener.WalletListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import javax.net.SocketFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class WalletScanner {

    public static final Logger Log = LoggerFactory.getLogger(com.jaeckel.mywallet.WalletScanner.class);
    //    private Wallet wallet;
    private List<Wallet> wallets = new ArrayList<Wallet>();

    private PeerGroup peerGroup;
    public static void main(String[] args) throws InterruptedException, UnreadableWalletException, AddressFormatException {

        new WalletScanner().run();

    }

    public void run() throws InterruptedException, UnreadableWalletException, AddressFormatException {


//        ECKey ev3Key = showWallet("/Users/biafra/ev3_spv_wallet_file");

        MainNetParams netParams = new MainNetParams();
        Log.info("Got netParams: " + netParams);
        Wallet wallet = new Wallet(netParams);

        wallet.addWatchedAddress(new Address(netParams, "1Dh2Pzbqe15QPsrHXrurLCHpY28K6ZGQMx"));
        wallets.add(wallet);

//        for (int i = 0; i < 10; i++) {
//            Wallet wallet = new Wallet(netParams);
//            ECKey key = new ECKey(new BigInteger("" + i));
//
//            Log.debug(" eckey.secKey: " + showBytes(key.getPrivKeyBytes()));
//            Log.debug(" eckey.toAddress: " + key.toAddress(netParams));
//
//            wallet.addKey(key);
//            wallets.add(wallet);
//        }


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


        usePeerGroup(netParams);
//        useSinglePeer(netParams);

    }

    private void spend(PeerGroup peerGroup) {
        // Get the address 1RbxbA1yP2Lebauuef3cBiBho853f7jxs in object form.
        Address targetAddress = null;
        try {
            targetAddress = new Address(new MainNetParams(), "1Dh2Pzbqe15QPsrHXrurLCHpY28K6ZGQMx");


            // Do the send of the coins in the background. This could throw InsufficientMoneyException.
            Wallet.SendResult result = wallets.get(0).sendCoins(peerGroup, targetAddress, Utils.toNanoCoins(0, 13));
            // Save the wallet to disk, optional if using auto saving (see below).
            //wallet.saveToFile(....);
            // Wait for the transaction to propagate across the P2P network, indicating acceptance.
            result.broadcastComplete.get();


        } catch (AddressFormatException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
        }

    }

//    private void useSinglePeer(MainNetParams netParams) throws InterruptedException {
//        File blockStoreFile = new File("ws_spv_block_store");
//        SPVBlockStore blockStore;
//        Peer peer;
//        try {
//            blockStore = new SPVBlockStore(netParams, blockStoreFile);
//
//            BlockChain blockChain = new BlockChain(netParams, wallets.get(0), blockStore);
//
//             peer = new Peer(netParams, blockChain, new PeerAddress(InetAddress.getLocalHost(), 8333), "test app", "0.1") {
//                @Override
//                public void connectionOpened() {
//                    super.connectionOpened();
//                    Log.info("TCP connect done");
//                }
//            };
//
//            peer.addEventListener(new AbstractPeerEventListener() {
//                @Override
//                public void onPeerConnected(Peer peer, int peerCount) {
//                    Log.info("Version handshake done");
//
//
//
//                }
//
//                @Override
//                public void onBlocksDownloaded(Peer peer, Block block, int blocksLeft) {
//                    Log.info("onBlocksDownloaded() block: " + block);
//                }
//
//                @Override
//                public void onChainDownloadStarted(Peer peer, int blocksLeft) {
//                    Log.info("onChainDownloadStarted() blocksLeft: " + blocksLeft);
//
//
//                }
//            });
//
//
//            SocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), 8333);
//            BlockingClient client = new BlockingClient(address, peer, 10000, SocketFactory.getDefault(), null);
//
//            for (Wallet wallet : wallets) {
//
//                blockChain.addWallet(wallet);
//                peer.addWallet(wallet);
//
//
//                Log.info("Installing WalletListener!");
//                WalletEventListener walletEventListener = new WalletListener();
//
//                wallet.addEventListener(walletEventListener);
//            }
//
//
//        } catch (BlockStoreException e) {
//            e.printStackTrace();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        Log.info("Starting blockchain download ...");
//        peer.startBlockChainDownload();
//
//
//        Thread.sleep(100 * 1000);
//        logNonEmptyWallets();
//    }

    private void usePeerGroup(MainNetParams netParams) throws InterruptedException {
        File blockStoreFile = new File("ws_spv_block_store");
        try {
            SPVBlockStore blockStore = new SPVBlockStore(netParams, blockStoreFile);
            Log.info("SPVBlockStore instantiated");

            InputStream stream = getClass().getClassLoader().getResourceAsStream("checkpoints");
            CheckpointManager.checkpoint(netParams, stream, blockStore, netParams.getGenesisBlock().getTimeSeconds() * 1000);
            BlockChain blockChain = new BlockChain(netParams, wallets.get(0), blockStore);

            peerGroup = new PeerGroup(netParams, blockChain);

            for (Wallet wallet : wallets) {

                blockChain.addWallet(wallet);
                peerGroup.addWallet(wallet);

//            peerGroup.setFastCatchupTimeSecs(netParams.getGenesisBlock().getTimeSeconds());
                peerGroup.addPeerDiscovery(new PeerDiscovery() {
                    @Override
                    public InetSocketAddress[] getPeers(long timeoutValue, TimeUnit timeoutUnit) throws PeerDiscoveryException {
                        InetSocketAddress[] result = new InetSocketAddress[1];

                        result[0] = new InetSocketAddress("localhost", 8333);
//                        result[1] = new InetSocketAddress("localhost", 8333);

                        return result;
                    }

                    @Override
                    public void shutdown() {
                        Log.info("shutDown");
                    }
                });

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
//            Log.info("Spending money");
//            spend(peerGroup);

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
//                spend(peerGroup);

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
