package osdistributedsystem;

import java.lang.InterruptedException;
import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;

import java.util.concurrent.Semaphore;

import java.util.HashMap;

public class ServerThread implements Runnable {
    private HashMap<Integer, ServerThread> serverThreads;
    private Socket socket = null;

    private ObjectInputStream objectInput = null;
    private ObjectOutputStream objectOutput = null;

    private VectorClock clock;
    private Semaphore lock;
    
    public ServerThread(int id, HashMap<Integer, ServerThread> serverThreads, Socket socket) {
        FileLogger.writeServerThread(id, "[INFO]:[ServerThread#ServerThread]::Creating ServerThread");
        this.serverThreads = serverThreads;
        this.socket = socket;
        FileLogger.writeServerThread(id, "[INFO]:[ServerThread#ServerThread]::Initialize object streams for " + id);
        try {
            this.objectInput = new ObjectInputStream(this.socket.getInputStream());
            this.objectOutput = new ObjectOutputStream(this.socket.getOutputStream());
        } catch (IOException ioex) {
            FileLogger.writeServerThread(id, "[ERROR]:[ServerThread#ServerThread]::ServerThread Failure: Could not initialize object streams: " + ioex.getMessage());
            System.err.println(ioex.getMessage());
            System.exit(1);
        }
        FileLogger.writeServerThread(id, "[INFO]:[ServerThread#ServerThread]::Initialized object streams for " + id);
        this.clock = new VectorClock(id);
        this.lock = new Semaphore(1);
        FileLogger.writeServerThread(id, "[INFO]:[ServerThread#ServerThread]::Created ServerThread");
    }
    
    public void run() {
        FileLogger.writeServerThread(this.getId(), "[INFO]:[ServerThread#run]::Starting ServerThread");
        //Wait until all clients are connected to the server
        while (this.serverThreads.size() != Constants.NUMBER_OF_CLIENTS);
        try {
            FileLogger.writeServerThread(this.getId(), "[INFO]:[ServerThread#run]::Sending clock to client");
            objectOutput.writeObject(this.clock);
            FileLogger.writeServerThread(this.getId(), "[INFO]:[ServerThread#run]::Sent clock: " + this.getClock() + " ID: " + this.getId());
            //if (this.clock.getId() != this.getId()) throw new Exception("Invalid clock, id mismatch");
            
            Command command;
            ObjectOutputStream output;

            while ((command = (Command)objectInput.readObject()) != null) {
                this.clock = (VectorClock)objectInput.readObject();
                switch (command) {
                    case REQUEST_READ:
                        output = this.serverThreads.get(1).getObjectOutputStream();
                        output.writeObject(Command.REQUEST_READ);
                        output.writeObject(this.clock);
                        output.flush();
                        break;
                    case REPLY_READ:
                        output = this.serverThreads.get(objectInput.readInt()).getObjectOutputStream();
                        output.writeObject(Command.REPLY_READ);
                        output.writeObject(this.clock);
                        output.flush();
                        break;
                    case REQUEST_WRITE:
                        output = this.serverThreads.get(this.getId()).getObjectOutputStream();
                        output.writeObject(Command.REQUEST_WRITE);
                        output.writeObject(this.getClock());
                        output.flush();
                        break;
                    case REPLY_WRITE:
                        output = this.serverThreads.get(objectInput.readInt()).getObjectOutputStream();
                        output.writeObject(Command.REPLY_WRITE);
                        output.writeObject(this.getClock());
                        output.flush();
                        break;
                }
                output = null;
            }
            this.socket.close();
            this.objectInput.close();
            this.objectOutput.close();
        } catch (ClassNotFoundException cnfex) {
            FileLogger.writeServerThread(this.getId(), "[ERROR]:[ServerThread#run]::Class Not Found: " + cnfex.getMessage());
            System.err.println(cnfex.getMessage());
            System.exit(1);
        } catch (IOException ioex) {
            FileLogger.writeServerThread(this.getId(), "[ERROR]:[ServerThread#run]::Server Failure: " + ioex.getMessage());
            System.err.println(ioex.getMessage());
            System.exit(1);
        }
    }

    public VectorClock getClock() {
        return this.clock;
    }

    public int getId() {
        return this.getClock().getId();
    }

    public ObjectInputStream getObjectInputStream() {
        return this.objectInput;
    }
    
    public ObjectOutputStream getObjectOutputStream() {
        return this.objectOutput;
    }
}
