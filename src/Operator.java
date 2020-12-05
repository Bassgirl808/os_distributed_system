package osdistributedsystem;

import java.lang.ClassNotFoundException;

import java.net.Socket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.concurrent.Semaphore;

public class Operator implements Runnable {
    //CLASS STUFF HEAAAAAAAAAAAAAAAAAAAAAAA
    Socket socket = null;
    ObjectInputStream input = null;
    ObjectOutputStream output = null;
    //Semaphore ABCD...
    VectorClock clock = null;
    
    public Operator(VectorClock clock, Socket socket, ObjectInputStream input, ObjectOutputStream output, Semaphore lock) {
        
    }

    public void run() {}
}

