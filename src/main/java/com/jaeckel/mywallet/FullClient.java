package com.jaeckel.mywallet;


import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.BlockStore;
import com.google.bitcoin.store.FullPrunedBlockStore;
import com.google.bitcoin.store.H2FullPrunedBlockStore;
import com.google.bitcoin.store.PostgresFullPrunedBlockStore;

import java.io.File;

public class FullClient {


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
        File blockStoreFile = new File("ws_full_block_store");

//        BlockStore blockStore = new PostgresFullPrunedBlockStore(new MainNetParams(), "full_mode_db", );

    }



}
