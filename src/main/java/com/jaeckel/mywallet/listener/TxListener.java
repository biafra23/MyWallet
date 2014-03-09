package com.jaeckel.mywallet.listener;

import com.google.bitcoin.core.*;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.script.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class TxListener implements PeerEventListener {

    MainNetParams netParams = new MainNetParams();

    public final Logger slf4jLogger = LoggerFactory.getLogger(com.jaeckel.mywallet.Main.class);

    public List<Message> getData(Peer p, GetDataMessage m) {
        slf4jLogger.info("Message received: " + m);
        return null;
    }

    public void onBlocksDownloaded(Peer p, Block b, int i) {
        // Log.info("Block downloaded:: " + b);
    }

    public void onChainDownloadStarted(Peer arg0, int arg1) {
        slf4jLogger.info("blockchain download started.");
    }

    public void onPeerConnected(Peer arg0, int arg1) {
        slf4jLogger.info("Peer Connected.");
    }

    public void onPeerDisconnected(Peer arg0, int arg1) {
        slf4jLogger.info("Peer Disonnected.");
    }

    public Message onPreMessageReceived(Peer arg0, Message m) {
        slf4jLogger.info("PreMessage Received:: " + m);
        return null;
    }

    public void onTransaction(Peer peer, Transaction tx) {
        boolean validTx = true;
        String txHash = tx.getHashAsString();
        List<TransactionOutput> txOutputs = tx.getOutputs();
        for (TransactionOutput output : txOutputs) {
            TransactionInput input = output.getSpentBy();
            try {
                if (output.getValue().compareTo(output.getMinNonDustValue()) != 1) {
                    // Log.info("Output is dust!");
                    validTx = false;
                    break;
                }
                // input.verify();
            } catch (RuntimeException epicfail) {
                // Log.info("Transaction outpoint verification failed.");
                validTx = false;
            }
        }
        for (TransactionOutput output : txOutputs) {
            Script script = new Script(output.getScriptBytes());
            Address address = script.getToAddress(netParams);
            // T	ODO: add check of TO address here to see if its to ours
            try {
                File walletFile = new File("my_spv_wallet_file");
                Wallet wallet;
                wallet = Wallet.loadFromFile(walletFile);
                List<ECKey> keys = wallet.getKeys();
                Address ouraddress = keys.get(0).toAddress(netParams);
                if (ouraddress == address) {
                    slf4jLogger.info("==============EV3 received money!!!!==================");
                    slf4jLogger.info(" Output TO OUR address !!! from: " + address.toString() +
                            " amount " + output.getValue());
                    slf4jLogger.info("================================");

                } else {
                    // Log.info("our address: " + ouraddress + " TO address: " + address);
                }
            } catch (Exception epicfail) {
                slf4jLogger.error("Uhoh address check stuff failed: " + epicfail.getMessage(), epicfail);
            }
        }
    }
}
