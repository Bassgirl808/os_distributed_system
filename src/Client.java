package osdistributedsystem;

import java.io.IOException;
import java.net.UnknownHostException;
import java.lang.ClassNotFoundException;

import java.net.Socket;

import java.io.File;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.concurrent.Semaphore;

import java.util.Vector;

public class Client implements Runnable {
    private int id;
    private Status status;
    private Vector<VectorClock> queue;
    private int responseCount;
    private Semaphore lock;
    private String command;
    private VectorClock clock;
    private Socket socket;

    public Client(int id) {
        FileLogger.writeClient(id, "[INFO]:[Client#Client]::Setting up client for PC: " + id);
        this.id = id;
        status = Status.IDLE;
        queue = new Vector<VectorClock>();
        responseCount = 0;
        lock = new Semaphore(1);
        command = null;
        clock = new VectorClock();
        socket = null;
        FileLogger.writeClient(this.id, "[INFO]:[Client#Client]::Client for PC" + this.id + "is set up");
    }

    public int getId() {
        return this.clock.getId();
    }

    public void run() {
        FileLogger.writeClient(this.id, "[INFO]:[Client#run]::Starting Client");
        try {
            FileLogger.writeClient(this.id, "[INFO]:[Client#run]::Setting up socket");
            this.socket = new Socket(Constants.IPV4, Constants.PORT);
            FileLogger.writeClient(this.id, "[INFO]:[Client#run]::Socket has been set up");

            FileLogger.writeClient(this.id, "[INFO]:[Client#run]::Connecting streams");
            ObjectInputStream input = new ObjectInputStream(this.socket.getInputStream());
            ObjectOutputStream output = new ObjectOutputStream(this.socket.getOutputStream());
            FileLogger.writeClient(this.id, "[INFO]:[Client#run]::Streams connected");

            FileLogger.writeClient(this.id, "[INFO]:[Client#run]::Receive Vectorclock");
            this.clock = (VectorClock)input.readObject();
            FileLogger.writeClient(this.id, "[INFO]:[Client#run]::VectorClock received");

            FileLogger.writeClient(this.id, "[INFO]:[Client#run]::Creating operator to respond to serverthread communications");
            Operator operator = new Operator(this.clock, this.socket, input, output, null);
            Thread operatorThread = new Thread(operator, Constants.THREAD_NAME_OPERATOR);
            ShutdownHandler.Operators.add(operatorThread);
            FileLogger.writeClient(this.id, "[INFO]:[Client#run]::Operator created");
            
            FileLogger.writeClient(this.id, "[INFO]:[Client#run]::Creating instructor to send instructions to serverthreads for requesting reads and writes");
            Instructor instructor = new Instructor(this.clock, this.socket, input, output, null);
            Thread instructorThread = new Thread(instructor, Constants.THREAD_NAME_INSTRUCTOR);
            ShutdownHandler.Instructors.add(instructorThread);
            FileLogger.writeClient(this.id, "[INFO]:[Client#run]::Instructor created");

            FileLogger.writeClient(this.id, "[INFO]:[Client#run]::Starting threads");
            operatorThread.start();
            instructorThread.start();
            FileLogger.writeClient(this.id, "[INFO]:[Client#run]::Started threads");


            while (true) {
                /*
                * use ratio from constants to calculate which command should be done, [read, write]
                * 
                *-read or write(always to PC1, PC1 must also request it of themself using semaphores to lock) to/from server
                * aquire locks
                *
                * increment clock
                * read/write to server (including request stuff) use semaphores to freeze things if you are PC1
                * 
                * flush streams
                * reset streams
                *
                * release locks
                *
                * process whole queue
                */
            }
        } catch (ClassNotFoundException cnfex) {
            FileLogger.writeClient(this.id, "[ERROR]:[Client#run]::Class Not Found: " + cnfex.getMessage());
            System.err.println(cnfex.getMessage());
        } catch (UnknownHostException uhex) {
            FileLogger.writeClient(this.id, "[ERROR]:[Client#run]::Unknown Host: " + uhex.getMessage());
            System.err.println(uhex.getMessage());
        } catch (IOException ioex) {
            FileLogger.writeClient(this.id, "[ERROR]:[Client#run]::Server Failure: " + ioex.getMessage());
            System.err.println(ioex.getMessage());
        }
    }
}
