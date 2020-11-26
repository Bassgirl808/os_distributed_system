package osdistributedsystem;

import java.lang.InterruptedException;
import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.HashMap;

public class Server implements Runnable {
    private ServerSocket server;
    private Socket client;

    private HashMap<Integer, ServerThread> serverThreads;

    public Server() {
        FileLogger.writeServer("[INFO]:[Server#Server]::Creating server");
        this.server = null;
        this.client = null;
        this.serverThreads = new HashMap<Integer, ServerThread>();
        FileLogger.writeServer("[INFO]:[Server#Server]::Server created");
    }

    public void run() {
        FileLogger.writeServer("[INFO]:[Server#run]::Launching server");
        try {
            FileLogger.writeServer("[INFO]:[Server#run]::Server IP is: " + Constants.IPV4);
            FileLogger.writeServer("[INFO]:[Server#run]::Server Port is: " + Constants.PORT);
            FileLogger.writeServer("[INFO]:[Server#run]::Starting server");
            this.server = new ServerSocket(Constants.PORT);
            FileLogger.writeServer("[INFO]:[Server#run]::Server started");

            int id = 0;
            //Would be nice if we could id++ here instead of elsewhere
            while (id < Constants.NUMBER_OF_CLIENTS) {
                this.client = null;
                try {
                    FileLogger.writeServer("[INFO]:[Server#run]::Listening for client connection requests");
                    this.client = this.server.accept();
                    //Increment id (starts at 0, first client should be 1)
                    id++;
                    FileLogger.writeServer("[INFO]:[Server#run]::Accepted connection of PC" + id);

                    FileLogger.writeServer("[INFO]:[Server#run]::Creating ServerThread to run client");
                    ServerThread serverThread = new ServerThread(id, this.serverThreads, this.client);
                    this.serverThreads.put(id, serverThread);
                    Thread thread = new Thread(serverThread);
                    FileLogger.writeServer("[INFO]:[Server#run]::ServerThread created");

                    FileLogger.writeServer("[INFO]:[Server#run]::Start ServerThread");
                    thread.start();
                    FileLogger.writeServer("[INFO]:[Server#run]::ServerThread Started");
                    
                    FileLogger.writeServer("[INFO]:[Server#run]::Server resetting for next connection (PC " + id + ")");
                    //Reset values for next connection (not necessary)
                    FileLogger.writeServer("[INFO]:[Server#run]::Server reset for next connection (PC " + id + ")");
                } catch (IOException ioex) {
                    FileLogger.writeServer("[ERROR]:[Server#run]::Server Failure: " + ioex.getMessage());
                    System.err.println(ioex.getMessage());
                }
            }
        } catch (IOException ioex) {
            FileLogger.writeServer("[ERROR]:[Server#run]::Server Failure: " + ioex.getMessage());
            System.err.println(ioex.getMessage());
            System.exit(1);
        }
    }    
}

