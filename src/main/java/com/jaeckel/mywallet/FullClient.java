package com.jaeckel.mywallet;


import com.google.bitcoin.core.*;
import com.google.bitcoin.net.discovery.PeerDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class FullClient {

    public static final Logger Log = LoggerFactory.getLogger(FullClient.class);
    NetworkParameters netParams = new MainNetParams();
    PostgresFullPrunedBlockStore blockStore;

    public static void main(String[] args ) {
        new FullClient().run();
    }

    public void run() {

        connectBlockChain();

//        String[] addresses = new String[]{
//                "18Nnr1236PeuVXpceXKbDJbAhWK6dYxaS9",
//                "1Dh2Pzbqe15QPsrHXrurLCHpY28K6ZGQMx",
//                "1PBuXpUVcLZEpoTSRC1tLpyV6xtGCvEeJR",
//                "1KBmj2QQs9yiq3wW2jEJuVnuBybWvLv6fV",
//                "1E1xPBdS85g8Eocs28NRHU4JQ1PEdbVgPg",
//                "1LttvufSeNniUFTMRJq46ZRbLhZoQvFVNJ"
//        };
//        for (String address : addresses) {
//            try {
//
//                getBalanceForAddress(address);
//
//            } catch (BlockStoreException e) {
//                Log.error("Exception while calculating balance", e);
//            } catch (AddressFormatException e) {
//                Log.error("Exception while calculating balance", e);
//            }
//        }
    }

    private void connectBlockChain() {

        try {

            blockStore = new PostgresFullPrunedBlockStore(netParams, 0, "localhost", "full_mode_db", "biafra", "");
            FullPrunedBlockChain blockChain = new FullPrunedBlockChain(netParams, blockStore);
            PeerDiscovery peerDiscovery = MyUtils.getLocalHostPeerDiscovery();

            //Supposed to be faster
            blockChain.setRunScripts(false);


            PeerGroup peerGroup = new PeerGroup(netParams, blockChain);
            peerGroup.addPeerDiscovery(peerDiscovery);

            peerGroup.start();
            Log.info("Starting up. Soon ready for Queries.");

        } catch (BlockStoreException e) {

            Log.error("Exception while opening blockstore", e);

        }

    }

    public BigInteger getBalanceForAddress(String address) throws BlockStoreException, AddressFormatException {
        long start = System.currentTimeMillis();
        BigInteger balance;

        balance = blockStore.calculateBalanceForAddress(new Address(netParams, address));
        Log.info("Balance for address: " + address + " is " + balance + ". Calulated in " + (System.currentTimeMillis() - start) + "ms");

        return balance;
    }


}
