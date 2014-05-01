package com.jaeckel.mywallet;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.io.OutputStream;

import static spark.Spark.get;


public class HelloSpark {
    public static final Logger Log = LoggerFactory.getLogger(WalletScanner.class);

    public static void main(String[] args) throws Exception {


        get(new Route("/hello") {
            @Override
            public Object handle(Request request, Response response) {
                return "Hello World!";
            }
        });

        get(new Route("/balance/:pubkey/:timestamp") {
            @Override
            public Object handle(Request request, Response response) {
                long balance = 0;
                try {

                    Log.info("response: " + request.params("pubkey"));
                    Log.info("response: " + request.params("timestamp"));
                    Log.info("response: " + response);
                    long timestamp = Long.valueOf(request.params("timestamp"));

                    balance = new CheckBalance(request.params("pubkey"), timestamp).run();

                } catch (Exception e) {
                    e.printStackTrace();
                }


                return "Balance: " + balance ;
            }
        });

        get(new Route("/stream") {
            @Override
            public Object handle(Request request, Response response) {


                Log.info("response: " + response);

//                response.type("text/plain");
//                response.header("Content-Disposition", "attachment;filename=downloadname.txt");
                try {
                    final OutputStream os = response.raw().getOutputStream();


                    os.write("test1".getBytes());
                    Thread.sleep(1000);
                    os.write("test1".getBytes());
                    Thread.sleep(1000);
                    os.write("test1".getBytes());
                    Thread.sleep(1000);
                    os.write("test1".getBytes());
                    Thread.sleep(1000);
                    os.write("test1".getBytes());
                    Thread.sleep(1000);
                    os.write("test1".getBytes());


                    os.flush();
                    os.close();


                } catch (IOException ex) {
                    Log.error("IOException: ", ex);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return null;
            }
        });

    }

}