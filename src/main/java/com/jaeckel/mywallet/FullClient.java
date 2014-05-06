package com.jaeckel.mywallet;


import com.google.bitcoin.core.*;
import com.google.bitcoin.net.discovery.PeerDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.*;
import com.sun.tools.internal.xjc.reader.dtd.bindinfo.BIInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class FullClient {

    public static final Logger Log = LoggerFactory.getLogger(FullClient.class);
    NetworkParameters netParams = new MainNetParams();

    public static void main(String[] args ) {


        new FullClient().run();


    }

    private void run() {

        createWallets();
        connectNetwork();

    }

    private void createWallets() {


    }

    private void connectNetwork() {

        try {

            PostgresFullPrunedBlockStore blockStore = new PostgresFullPrunedBlockStore(netParams, 0, "localhost", "full_mode_db", "biafra", "");
            FullPrunedBlockChain blockChain = new FullPrunedBlockChain(netParams, blockStore);
            PeerDiscovery peerDiscovery = MyUtils.getLocalHostPeerDiscovery();

            PeerGroup peerGroup = new PeerGroup(netParams, blockChain);
            peerGroup.addPeerDiscovery(peerDiscovery);

            peerGroup.startAndWait();

            BigInteger balance = blockStore.calculateBalanceForAddress(new Address(netParams, "1Dh2Pzbqe15QPsrHXrurLCHpY28K6ZGQMx"));

            Log.info("Balance: " + balance);

        } catch (BlockStoreException e) {

            Log.error("Exception while opening blockstore", e);
        } catch (AddressFormatException e) {
            Log.error("Exception while calculating balance", e);
        }

    }



}
