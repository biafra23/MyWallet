package com.jaeckel.mywallet;


import com.google.bitcoin.net.discovery.PeerDiscovery;
import com.google.bitcoin.net.discovery.PeerDiscoveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class MyUtils {

    public static final Logger Log = LoggerFactory.getLogger(WalletScanner.class);

    public static PeerDiscovery getLocalHostPeerDiscovery() {
        return new PeerDiscovery() {
            @Override
            public InetSocketAddress[] getPeers(long timeoutValue, TimeUnit timeoutUnit) throws PeerDiscoveryException {
                InetSocketAddress[] result = new InetSocketAddress[1];
                result[0] = new InetSocketAddress("localhost", 8333);
                return result;
            }

            @Override
            public void shutdown() {
                Log.info("shutDown");
            }
        };
    }

}
