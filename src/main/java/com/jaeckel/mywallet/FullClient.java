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

    private void run() {

        createWallets();
        connectNetwork();

    }

    private void createWallets() {


    }

    private void connectNetwork() {

        try {

            blockStore = new PostgresFullPrunedBlockStore(netParams, 0, "localhost", "full_mode_db", "biafra", "");
            FullPrunedBlockChain blockChain = new FullPrunedBlockChain(netParams, blockStore);
            PeerDiscovery peerDiscovery = MyUtils.getLocalHostPeerDiscovery();

            //Supposed to be faster
            blockChain.setRunScripts(false);


            PeerGroup peerGroup = new PeerGroup(netParams, blockChain);
            peerGroup.addPeerDiscovery(peerDiscovery);

            peerGroup.startAndWait();
            Log.info("Startup done. Ready for Queries.");

            String[] addresses = new String[]{
                    "18Nnr1236PeuVXpceXKbDJbAhWK6dYxaS9",
                    "1Dh2Pzbqe15QPsrHXrurLCHpY28K6ZGQMx",
                    "1PBuXpUVcLZEpoTSRC1tLpyV6xtGCvEeJR",
                    "1KBmj2QQs9yiq3wW2jEJuVnuBybWvLv6fV",
                    "1E1xPBdS85g8Eocs28NRHU4JQ1PEdbVgPg",
                    "1LttvufSeNniUFTMRJq46ZRbLhZoQvFVNJ"
            };
            for (String address : addresses) {
                getBalaceForAdress(address);

            }

        } catch (BlockStoreException e) {

            Log.error("Exception while opening blockstore", e);
        } catch (AddressFormatException e) {
            Log.error("Exception while calculating balance", e);
        }

    }

    private void getBalaceForAdress(String address) throws BlockStoreException, AddressFormatException {
        long start = System.currentTimeMillis();
        BigInteger balance;

        balance = blockStore.calculateBalanceForAddress(new Address(netParams, address));
        Log.info("Balance for address: " + address + " is " + balance + ". Calulated in " + (System.currentTimeMillis() - start) + "ms");
    }


}
