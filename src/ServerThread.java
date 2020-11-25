package osdistributedsystem;

import java.lang.InterruptedException;
import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread implements Runnable {
    private Socket socket = null;

    private ObjectInputStream objectInput = null;
    private ObjectOutputStream objectOutput = null;

    private int id = 0;
    
    public ServerThread(Socket socket, ObjectInputStream objectInput, ObjectOutputStream objectOutput, int id) {
        this.socket = socket;
        this.objectInput = objectInput;
        this.objectOutput = objectOutput;
        this.id = id;
    }
    
    public void run() {
        //Do stuff here, listen for client stuff and do things...

    }
}
