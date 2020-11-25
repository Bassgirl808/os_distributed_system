package osdistributedsystem;

import java.lang.InterruptedException;
import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ServerSocket;
import java.net.Socket;

import osdistributedsystem.Constants;

public class Server implements Runnable {
    private ServerSocket server = null;
    private Socket client = null;

    private int id = 0;

    public Server() {
        FileLogger.writeServer("[INFO]:[Server#Server]::Creating server");
        this.id = 0; 
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

            while (this.id < Constants.NUMBER_OF_CLIENTS) {
                this.client = null;
                try {
                    FileLogger.writeServer("[INFO]:[Server#run]::Listening for client connection requests");
                    this.client = this.server.accept();
                    FileLogger.writeServer("[INFO]:[Server#run]::Accepted connection of PC" + this.id);
                    
                    FileLogger.writeServer("[INFO]:[Server#run]::Initialize object streams for " + this.id);
                    ObjectInputStream objectInput = new ObjectInputStream(this.client.getInputStream());
                    ObjectOutputStream objectOutput = new ObjectOutputStream(this.client.getOutputStream());
                    FileLogger.writeServer("[INFO]:[Server#run]::Initialized object streams for " + this.id);

                    FileLogger.writeServer("[INFO]:[Server#run]::Creating ServerThread to run client");
                    ServerThread serverThread = new ServerThread(this.client, objectInput, objectOutput, this.id);
                    Thread thread = new Thread(serverThread);
                    FileLogger.writeServer("[INFO]:[Server#run]::ServerThread created");

                    FileLogger.writeServer("[INFO]:[Server#run]::Start ServerThread");
                    thread.start();
                    FileLogger.writeServer("[INFO]:[Server#run]::ServerThread Started");
                    
                    FileLogger.writeServer("[INFO]:[Server#run]::Server resetting for next connection (PC " + this.id + ")");
                    this.id++;
                    FileLogger.writeServer("[INFO]:[Server#run]::Server reset for next connection (PC " + this.id + ")");
                } catch (IOException ioex) {
                    FileLogger.writeServer("[ERROR]:[Server#run]::Server Failure: " + ioex.getMessage());
                    System.err.println(ioex.getMessage());
                }
            }
        } catch (IOException ioex) {
            FileLogger.writeServer("[ERROR]:[Server#run]::Server Failure: " + ioex.getMessage());
            System.err.println(ioex.getMessage());
        }
    }    
}

