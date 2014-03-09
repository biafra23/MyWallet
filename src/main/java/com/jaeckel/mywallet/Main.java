package com.jaeckel.mywallet;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.Block;
import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.CheckpointManager;
import com.google.bitcoin.core.DownloadListener;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.GetDataMessage;
import com.google.bitcoin.core.Message;
import com.google.bitcoin.core.Peer;
import com.google.bitcoin.core.PeerEventListener;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.TransactionOutput;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.WalletEventListener;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.SPVBlockStore;
import com.google.bitcoin.store.UnreadableWalletException;
import com.google.bitcoin.uri.BitcoinURI;
import com.google.bitcoin.wallet.WalletFiles;

import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import com.jaeckel.mywallet.listener.WalletListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
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

        Wallet wallet = get_wallet(netParams);
        Log.info("Got wallet.");

        List<ECKey> keys = wallet.getKeys();
        Address address = keys.get(0).toAddress(netParams);


        String uri = BitcoinURI.convertToBitcoinURI(address.toString(),
                new BigInteger("10000"), "MY Wallet", "");

        Log.info("Wallet address: URI: " + uri);
        int widthHeight = 150;

        encodeAndWriteQrCode(uri, widthHeight);
        //   System.exit(0);

        File blockStoreFile = new File("ev3_spv_block_store");
        long offset = 0; // 86400 * 30;
        try {
            SPVBlockStore blockStore = new SPVBlockStore(netParams,
                    blockStoreFile);
            Log.info("SPVBlockStore instantiated");

            InputStream stream = getClass().getClassLoader().getResourceAsStream("checkpoints");
            CheckpointManager.checkpoint(netParams, stream, blockStore,
                    wallet.getEarliestKeyCreationTime() - offset);

            BlockChain blockChain = new BlockChain(netParams, wallet,
                    blockStore);


            PeerGroup peerGroup = new PeerGroup(netParams, blockChain);
            peerGroup.addWallet(wallet);
            peerGroup.setFastCatchupTimeSecs(wallet.getEarliestKeyCreationTime() - offset);
//			peerGroup.setBloomFilterFalsePositiveRate(0.5);
            // LocalPeer localPeer = new LocalPeer();
            // peerGroup.addPeerDiscovery(localPeer);
            peerGroup.addPeerDiscovery(new DnsDiscovery(netParams));
            // InetAddress ia = InetAddress.getByName("2.221.132.213");
            // peerGroup.addAddress(new PeerAddress(ia, 8333));
            Log.info("Starting peerGroup ...");
            peerGroup.startAndWait();

//            PeerEventListener listener = new TxListener();
//            peerGroup.addEventListener(listener);

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
            Log.error("Caught BlockStoreException: ", c);
        } catch (IOException ie) {
            Log.error("Caught BlockStoreException: ", ie);
        }
    }

    private void encodeAndWriteQrCode(String uri, int widthHeight) {
        Charset charset = Charset.forName("ISO-8859-1");
        CharsetEncoder encoder = charset.newEncoder();
        byte[] b = null;
        try {
            // Convert a string to ISO-8859-1 bytes in a ByteBuffer
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(uri));
            b = bbuf.array();
        } catch (CharacterCodingException e) {
            System.out.println(e.getMessage());
        }

        String data = null;
        try {
            data = new String(b, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }

        // get a byte matrix for the data
        BitMatrix matrix = null;
        int h = widthHeight;
        int w = widthHeight;
        com.google.zxing.Writer writer = new QRCodeWriter();
        try {
            matrix = writer.encode(data,
                    com.google.zxing.BarcodeFormat.QR_CODE, w, h);
        } catch (com.google.zxing.WriterException e) {
            System.out.println(e.getMessage());
        }

        String filePath = "qr.png";
        File file = new File(filePath);
        try {
            MatrixToImageWriter.writeToFile(matrix, "PNG", file);
            System.out.println("printing to " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static Wallet get_wallet(MainNetParams netParams) {
        Log.info("get_wallet...");

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



