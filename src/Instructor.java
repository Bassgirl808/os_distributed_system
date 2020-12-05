package osdistributedsystem;

import java.io.IOException;

import java.net.Socket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.concurrent.Semaphore;

import osdistributedsystem.VectorClock;

public class Instructor implements Runnable {
    //
    Socket socket = null;
    ObjectInputStream input = null;
    ObjectOutputStream output = null;
    VectorClock clock = null;

    public Instructor(VectorClock clock, Socket socket, ObjectInputStream input, ObjectOutputStream output, Semaphore lock) {
        
    }

    public void run() {}
}