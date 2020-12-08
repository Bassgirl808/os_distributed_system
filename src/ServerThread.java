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

    private ObjectInputStream input = null;
    private ObjectOutputStream output = null;

    private VectorClock clock;
    
    public ServerThread(int id, HashMap<Integer, ServerThread> serverThreads, Socket socket) {
        FileLogger.writeServerThread(id, "[INFO]:[ServerThread#ServerThread]::Creating ServerThread");
        this.serverThreads = serverThreads;
        this.socket = socket;
        FileLogger.writeServerThread(id, "[INFO]:[ServerThread#ServerThread]::Initialize object streams for " + id);
        try {
            this.input = new ObjectInputStream(this.socket.getInputStream());
            this.output = new ObjectOutputStream(this.socket.getOutputStream());
        } catch (IOException ioex) {
            FileLogger.writeServerThread(id, "[ERROR]:[ServerThread#ServerThread]::ServerThread Failure: Could not initialize object streams: " + ioex.getMessage());
            System.err.println(ioex.getMessage());
            System.exit(1);
        }
        FileLogger.writeServerThread(id, "[INFO]:[ServerThread#ServerThread]::Initialized object streams for " + id);
        this.clock = new VectorClock(id);
        FileLogger.writeServerThread(id, "[INFO]:[ServerThread#ServerThread]::Created ServerThread");
    }
    
    public void run() {
        FileLogger.writeServerThread(this.getId(), "[INFO]:[ServerThread#run]::Starting ServerThread");
        //Wait until all clients are connected to the server
        while (this.serverThreads.size() != Constants.NUMBER_OF_CLIENTS);
        try {
            FileLogger.writeServerThread(this.getId(), "[INFO]:[ServerThread#run]::Sending clock to client");
            this.output.writeObject(this.clock);
            FileLogger.writeServerThread(this.getId(), "[INFO]:[ServerThread#run]::Sent clock: " + this.getClock() + " ID: " + this.getId());
            //if (this.clock.getId() != this.getId()) throw new Exception("Invalid clock, id mismatch");
            
            Command command = null;
            ObjectOutputStream output = null;

            while ((command = Command.values()[input.readInt()]) != null && !Thread.currentThread().interrupted()) {
                this.clock = (VectorClock)input.readObject();
                switch (command) {
                    case REQUEST_READ:
                        output = this.serverThreads.get(1).getObjectOutputStream();
                        output.writeInt(Command.REQUEST_READ.ordinal());
                        output.writeObject(this.getClock());
                        output.flush();
                        output.reset();
                        break;
                    case REPLY_READ:
                        output = this.serverThreads.get(input.readInt()).getObjectOutputStream();
                        output.writeInt(Command.REPLY_READ.ordinal());
                        output.writeObject(this.getClock());
                        output.flush();
                        output.reset();
                        break;
                    case REQUEST_WRITE:
                        output = this.serverThreads.get(this.getId()).getObjectOutputStream();
                        output.writeInt(Command.REQUEST_WRITE.ordinal());
                        output.writeObject(this.getClock());
                        output.flush();
                        output.reset();
                        break;
                    case REPLY_WRITE:
                        output = this.serverThreads.get(input.readInt()).getObjectOutputStream();
                        output.writeInt(Command.REPLY_WRITE.ordinal());
                        output.writeObject(this.getClock());
                        output.flush();
                        output.reset();
                        break;
                }
                output = null;
            }
            this.socket.close();
            this.input.close();
            output.close();
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
        return this.input;
    }
    
    public ObjectOutputStream getObjectOutputStream() {
        return output;
    }
}
