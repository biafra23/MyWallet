package com.jaeckel.mywallet;


import com.google.bitcoin.core.*;
import com.google.bitcoin.net.discovery.DnsDiscovery;
import com.google.bitcoin.net.discovery.PeerDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.*;
import com.google.bitcoin.utils.BriefLogFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.List;

public class FullClient {

    public static final Logger Log = LoggerFactory.getLogger(FullClient.class);
    NetworkParameters netParams = new MainNetParams();
    PostgresFullPrunedBlockStore blockStore;

    public static void main(String[] args ) {
        new FullClient().run();
    }

    public void run() {
        BriefLogFormatter.init();

        connectBlockChain();
    }

    private void connectBlockChain() {

        try {

            blockStore = new PostgresFullPrunedBlockStore(netParams, 0, "localhost", "full_mode_db2", "biafra", "");
            FullPrunedBlockChain blockChain = new FullPrunedBlockChain(netParams, blockStore);
            PeerDiscovery peerDiscovery = MyUtils.getLocalHostPeerDiscovery();
//            PeerDiscovery peerDiscovery = new DnsDiscovery(netParams);

            //faster
            blockChain.setRunScripts(false);

            PeerGroup peerGroup = new PeerGroup(netParams, blockChain);
            peerGroup.addPeerDiscovery(peerDiscovery);

            peerGroup.start();
            Log.info("Starting up. Soon ready for Queries.");

        } catch (BlockStoreException e) {

            Log.error("Exception while opening block store", e);

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
