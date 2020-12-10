package osdistributedsystem;

import java.lang.InterruptedException;
import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;

import java.util.concurrent.Semaphore;

import java.util.HashMap;

public class ServerThread implements Runnable {
    private volatile HashMap<Integer, ObjectOutputStream> serverThreadOutputStreams;
    private Socket socket = null;

    private ObjectInputStream input = null;
    private ObjectOutputStream output = null;

    private VectorClock clock;
    
    public ServerThread(int id, HashMap<Integer, ObjectOutputStream> serverThreadOutputStreams, Socket socket, ObjectInputStream input , ObjectOutputStream output) {
        FileLogger.writeServerThread(id, "[INFO]:[ServerThread#ServerThread]::Creating ServerThread");
        this.serverThreadOutputStreams = serverThreadOutputStreams;
        this.socket = socket;
        this.input = input;
        this.output= output;
        this.clock = new VectorClock(id);
        FileLogger.writeServerThread(id, "[INFO]:[ServerThread#ServerThread]::Created ServerThread");
    }
    
    public void run() {
        FileLogger.writeServerThread(this.getId(), "[INFO]:[ServerThread#run]::Starting ServerThread");
        try {
            FileLogger.writeServerThread(this.getId(), "[INFO]:[ServerThread#run]::Sending clock to client");
            this.output.writeObject(this.clock);
            this.output.flush();
            FileLogger.writeServerThread(this.getId(), "[INFO]:[ServerThread#run]::Sent clock: " + this.getClock() + " ID: " + this.getId());
            
            //Wait until all clients are connected to the server
            while (this.serverThreadOutputStreams.size() < Constants.NUMBER_OF_CLIENTS);

            this.output.writeInt(Command.START.ordinal());
            this.output.flush();

            Command command = null;
            ObjectOutputStream output = null;

            while ((command = Command.values()[input.readInt()]) != null && !Thread.currentThread().interrupted()) {
                this.clock = (VectorClock)input.readObject();
                switch (command) {
                    case REQUEST_READ:
                        output = this.serverThreadOutputStreams.get(1);
                        output.writeInt(Command.REQUEST_READ.ordinal());
                        output.writeObject(this.getClock());
                        output.flush();
                        //output.reset();
                        break;
                    case REPLY_READ:
                        output = this.serverThreadOutputStreams.get(input.readInt());
                        output.writeInt(Command.REPLY_READ.ordinal());
                        output.writeObject(this.getClock());
                        output.flush();
                        //output.reset();
                        break;
                    case REQUEST_WRITE:
                        output = this.serverThreadOutputStreams.get(this.getId());
                        output.writeInt(Command.REQUEST_WRITE.ordinal());
                        output.writeObject(this.getClock());
                        output.flush();
                        //output.reset();
                        break;
                    case REPLY_WRITE:
                        output = this.serverThreadOutputStreams.get(input.readInt());
                        output.writeInt(Command.REPLY_WRITE.ordinal());
                        output.writeObject(this.getClock());
                        output.flush();
                        //output.reset();
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
