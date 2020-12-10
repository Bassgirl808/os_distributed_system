package osdistributedsystem;

import java.lang.InterruptedException;
import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class Server implements Runnable {
    private ServerSocket server;

    private volatile Map<Integer, ObjectOutputStream> serverThreadOutputStreams;

    public Server() {
        FileLogger.writeServer("[INFO]:[Server#Server]::Creating server");
        this.server = null;
        this.serverThreadOutputStreams = Collections.synchronizedMap(new HashMap<Integer, ObjectOutputStream>());
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

            for (int id = 0; id < Constants.NUMBER_OF_CLIENTS; id++) {
                try {
                    FileLogger.writeServer("[INFO]:[Server#run]::Listening for client connection requests");
                    Socket client = this.server.accept();
                    FileLogger.writeServer("[INFO]:[Server#run]::Accepted connection of PC" + id);

                    FileLogger.writeServer("[INFO]:[Server#run]::Initialize object streams for PC" + id);
                    ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
                    output.flush();
                    this.serverThreadOutputStreams.put(id, output);
                    ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                    FileLogger.writeServer("[INFO]:[Server#run]::Initialized object streams for PC" + id);

                    FileLogger.writeServer("[INFO]:[Server#run]::Creating ServerThread to run client");
                    ServerThread serverThread = new ServerThread(id, this.serverThreadOutputStreams, client, input, output);
                    Thread thread = new Thread(serverThread);
                    ShutdownHandler.ServerThreads.add(thread);
                    FileLogger.writeServer("[INFO]:[Server#run]::ServerThread created");

                    FileLogger.writeServer("[INFO]:[Server#run]::Start ServerThread");
                    thread.start();
                    FileLogger.writeServer("[INFO]:[Server#run]::ServerThread Started");

                    FileLogger.writeServer("[INFO]:[Server#run]::Server resetting for next connection");
                } catch (IOException ioex) {
                    FileLogger.writeServer("[ERROR]:[Server#run]::Server Failure: " + ioex.getMessage());
                    System.err.println(ioex.getMessage());
                    System.exit(1);
                }
            }

            //Busy wait until completion of program
            while (!Thread.currentThread().interrupted());
        } catch (IOException ioex) {
            FileLogger.writeServer("[ERROR]:[Server#run]::Server Failure: " + ioex.getMessage());
            System.err.println(ioex.getMessage());
            System.exit(1);
        }
    }
}
