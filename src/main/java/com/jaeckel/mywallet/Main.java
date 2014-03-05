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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

        Wallet wallet = get_wallet(netParams);
		Log.info("Got wallet.");

		List<ECKey> keys = wallet.getKeys();
		Address address = keys.get(0).toAddress(netParams);


		String uri = BitcoinURI.convertToBitcoinURI(address.toString(),
				new BigInteger("10000"), "MY Wallet", "");

		Log.info("Wallet address: URI: " + uri);

		int widthHeight = 150;

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

            PeerEventListener listener = new TxListener();
            peerGroup.addEventListener(listener);

            Log.info("Installing WalletListener!");
            WalletEventListener walletEventListener = new WalletListener();
            wallet.addEventListener(walletEventListener);

            peerGroup.startBlockChainDownload(new DownloadListener() {

            });
        }
		catch (BlockStoreException e) {
			Log.error("Caught BlockStoreException: ", e);
		}
		catch (UnknownHostException x) {
			Log.error("Caught UnknownHostException: ", x);
		}
		catch (FileNotFoundException c) {
			Log.error("Caught BlockStoreException: ", c);
		}
		catch (IOException ie) {
			Log.error("Caught BlockStoreException: ", ie);
		}
	}

	public static Wallet get_wallet(MainNetParams netParams) {
        Log.info("get_wallet...");

        File walletFile = new File("ev3_spv_wallet_file");
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

class WalletListener implements WalletEventListener {

    public static final Logger slf4jLogger = LoggerFactory.getLogger(com.jaeckel.mywallet.Main.class);


    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
        slf4jLogger.info("-----> received: prevBalance: " + prevBalance);
        slf4jLogger.info("-----> received: newBalance: " + newBalance);
        slf4jLogger.info("-----> received: " + (newBalance.subtract(prevBalance)));
    }

    @Override
    public void onCoinsSent(Wallet wallet, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
        slf4jLogger.info("-----> sent coins.");
    }

    @Override
    public void onReorganize(Wallet wallet) {
        slf4jLogger.info("-----> onReorganize()");
    }

    @Override
    public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
        slf4jLogger.info("-----> onTransactionConfidenceChanged()");
    }

    @Override
    public void onWalletChanged(Wallet wallet) {
    }

    @Override
    public void onKeysAdded(Wallet wallet, List<ECKey> keys) {
        slf4jLogger.info("-----> onKeysAdded()");
    }

    @Override
    public void onScriptsAdded(Wallet wallet, List<Script> scripts) {
        slf4jLogger.info("-----> onScriptsAdded()");
    }
}

class TxListener implements PeerEventListener {

	MainNetParams netParams = new MainNetParams();

	public final Logger slf4jLogger = LoggerFactory.getLogger(com.jaeckel.mywallet.Main.class);

	public List<Message> getData (Peer p, GetDataMessage m) {
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
			}
			catch (RuntimeException epicfail) {
				// Log.info("Transaction outpoint verification failed.");
				validTx = false;
			}
		}
		for (TransactionOutput output : txOutputs) {
			Script script = new Script(output.getScriptBytes());
			Address address = script.getToAddress(netParams);
			// T	ODO: add check of TO address here to see if its to ours
			try {
				File walletFile = new File("/home/root/ev3_spv_wallet_file");
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
