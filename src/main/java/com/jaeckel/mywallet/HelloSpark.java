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

        get(new Route("/start") {
            @Override
            public Object handle(Request request, Response response) {

                try {

                    Log.info("response: " + response);

                    new CheckBalance().run();


                } catch (Exception e) {
                    e.printStackTrace();
                }


                return "Service started";
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