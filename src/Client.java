package osdistributedsystem;

//Exception Handling
import java.io.IOException;
import java.net.UnknownHostException;
import java.lang.ClassNotFoundException;

import java.net.Socket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Client implements Runnable {
    private String command;
    private volatile VectorClock clock;
    private Socket socket;
    private volatile ObjectInputStream input;
    private volatile ObjectOutputStream output;
    
    public Client() {
        FileLogger.writeBackground("[INFO]:[Client#Client]::Setting up client for PC");
        this.command = null;
        try {
            //Connecting client to socket
            FileLogger.writeBackground("[INFO]:[Client#Client]::Setting up socket");
            this.socket = new Socket(Constants.IPV4, Constants.PORT);
            FileLogger.writeBackground("[INFO]:[Client#Client]::Socket has been set up");

            FileLogger.writeBackground("[INFO]:[Client#Client]::Connecting streams");
            this.input = new ObjectInputStream(this.socket.getInputStream());
            this.output = new ObjectOutputStream(this.socket.getOutputStream());
            this.output.flush();
            //this.output.reset();
            FileLogger.writeBackground("[INFO]:[Client#Client]::Streams connected");

            FileLogger.writeBackground("[INFO]:[Client#Client]::Receive Vectorclock");
            this.clock = (VectorClock)this.input.readObject();
            FileLogger.writeBackground("[INFO]:[Client#Client]::VectorClock received: " + this.clock);
        } catch (ClassNotFoundException cnfex) {
            FileLogger.writeBackground("[ERROR]:[Client#Client]::Class Not Found: " + cnfex.getMessage());
            System.err.println(cnfex.getMessage());
        } catch (UnknownHostException uhex) {
            FileLogger.writeBackground("[ERROR]:[Client#Client]::Unknown Host: " + uhex.getMessage());
            System.err.println(uhex.getMessage());
        } catch (IOException ioex) {
            FileLogger.writeBackground("[ERROR]:[Client#Client]::Server Failure: " + ioex.getMessage());
            System.err.println(ioex.getMessage());
        }
        FileLogger.writeBackground("[INFO]:[Client#Client]::Client for PC" + this.getId() + " is set up");
    }

    public int getId() {
        return this.clock.getId();
    }

    public void run() {
        FileLogger.writeClient(this.getId(), "[INFO]:[Client#run]::Starting Client");

        FileLogger.writeClient(this.getId(), "[INFO]:[Client#run]::Waiting for start command");
        try {
            if (Command.values()[this.input.readInt()] != Command.START) System.exit(5);
        } catch (IOException ioex) {
            System.err.println(ioex.getMessage());
            System.exit(4);
        }
        FileLogger.writeClient(this.getId(), "[INFO]:[Client#run]::Received start command");

        FileLogger.writeClient(this.getId(), "[INFO]:[Client#run]::Creating operator to respond to serverthread communications");
        Operator operator = new Operator(this.clock, this.input, this.output);
        Thread operatorThread = new Thread(operator, Constants.THREAD_NAME_OPERATOR);
        ShutdownHandler.Operators.add(operatorThread);
        FileLogger.writeClient(this.getId(), "[INFO]:[Client#run]::Operator created");
        
        FileLogger.writeClient(this.getId(), "[INFO]:[Client#run]::Creating instructor to send instructions to serverthreads for requesting reads and writes");
        Instructor instructor = new Instructor(this.clock, this.input, this.output, operator);
        Thread instructorThread = new Thread(instructor, Constants.THREAD_NAME_INSTRUCTOR);
        ShutdownHandler.Instructors.add(instructorThread);
        FileLogger.writeClient(this.getId(), "[INFO]:[Client#run]::Instructor created");

        FileLogger.writeClient(this.getId(), "[INFO]:[Client#run]::Starting threads");
        operatorThread.start();
        instructorThread.start();
        FileLogger.writeClient(this.getId(), "[INFO]:[Client#run]::Started threads");

        //Busy wait until joined
        while (!Thread.currentThread().interrupted());
    }
}
