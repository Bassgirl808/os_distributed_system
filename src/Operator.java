package osdistributedsystem;

import java.io.IOException;
import java.lang.ClassNotFoundException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.concurrent.Semaphore;

import osdistributedsystem.Constants;

import java.util.Queue;
import java.util.LinkedList;

//Operator handles managing incoming requests from "server" and taking appropriate actions
public class Operator implements Runnable {
    private volatile VectorClock clock;
    private volatile ObjectInputStream input;
    private volatile ObjectOutputStream output;
    private volatile Status status;
    private volatile boolean busy;
    private Queue<VectorClock> queue;
    private Semaphore readLock;
    private Semaphore writeLock;
    private byte event;
    
    public Operator(VectorClock clock, ObjectInputStream input, ObjectOutputStream output) {
        this.clock = clock;
        this.input = input;
        this.output = output;
        this.status = Status.IDLE;
        //PC is busy unless otherwise noted
        this.busy = false;
        this.queue = new LinkedList<VectorClock>();
        //Read Semaphore has licenses allowing for parallel execution of reads (partially consistent reading)
        //Can license all clients (partially consistent)
        this.readLock = new Semaphore(Constants.CONCURRENT_IO_READ_MAX);
        //Write Semaphore is used for ensuring serial execution of writes
        this.writeLock = new Semaphore(Constants.CONCURRENT_IO_WRITE_MAX);
        //Event is used to track the number of write replies allowing for write when all clients agree
        this.event = 0;
    }

    public void run() {
        try {
            while (!Thread.currentThread().interrupted()) {
                Command command = Command.values()[input.readInt()];
                VectorClock otherClock = (VectorClock)input.readObject();
                this.clock.increment();
                this.clock.merge(otherClock);

                //Handle requests and replies
                switch (command) {
                    case REQUEST_READ:
                        this.output.writeInt(Command.REPLY_READ.ordinal());
                        this.output.writeObject(this.clock);
                        this.output.writeInt(otherClock.getId());

                        this.output.flush();
                        this.output.reset();
                        break;
                    case REPLY_READ:
                        //Begin critical reading section - parallelism allowed
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Acquiring access to read");
                        this.readLock.acquire();
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Reading access acquired");

                        this.clock.increment();

                        //Simulate copying file over network for partially consistent reading
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Receiving copy file for reading");
                        FileIO.copy(this.clock.getId());
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Copy file for reading received");

                        //Simulate network delay and reading time
                        Thread.sleep(Constants.DELAY_NETWORK);
                        Thread.sleep(Constants.DELAY_CLIENT_READ);
                        //Simulate deleting file
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Deleting copy file after reading");
                        FileIO.deleteCopy(this.clock.getId());
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Copy file deleted");

                        this.clock.increment();

                        this.status = Status.IDLE;

                        FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Return access for reading");
                        this.readLock.release();
                        //End critical reading section - parallelism allowed
                        break;
                    case REQUEST_WRITE: {
                        if (this.status == Status.WRITING) {
                            int comparison = this.clock.compareTo(otherClock);

                            //this->other or (this||other and this smaller id than other) - either our clock is not as new as theirs or arbitrary if parallel
                            if (comparison == -1 || (comparison == 0 && this.clock.getId() < otherClock.getId())) {
                                FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Clock out of date");
                                this.queue.add(otherClock);
                                FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Updating clock");
                                this.busy = true;
                            //other->this or (this||other and this larger id than other) - either our clock is newer than theirs or arbitrary if parallel
                            } else if (comparison == 1 || (comparison == 0 && this.clock.getId() > otherClock.getId())) {
                                FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Clock up to date");

                                this.clock.increment();

                                FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Writing access granted");
                                this.output.writeInt(Command.REPLY_WRITE.ordinal());
                                this.output.writeObject(this.clock);
                                this.output.writeInt(otherClock.getId());
                                
                                this.output.flush();
                                this.output.reset();
                            } 
                        } else {
                            FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::No client writing");
                            this.clock.increment();

                            FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Writing access granted");
                            this.output.writeInt(Command.REPLY_WRITE.ordinal());
                            this.output.writeObject(this.clock);
                            this.output.writeInt(this.clock.getId());
                            
                            this.output.flush();
                            this.output.reset();
                        }
                        break;
                    }
                    case REPLY_WRITE: {
                        //Begin critical writing section - non concurrent - serial
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Acquiring access to write");
                        this.writeLock.acquire();
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Writing access acquired");

                        this.event++;

                        FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Getting permissions from other clients to write");
                        if (!(this.event < Constants.NUMBER_OF_CLIENTS)) {
                            //Can take all licenses, no reading while writing but this is not necessary
                            //FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Acquiring all licenses for reading");
                            //this.readLock.acquire(Constants.NUMBER_OF_CLIENTS);
                            //FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Reading licenses acquired");
                            
                            this.clock.increment();
                            FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Downloading file for writing");
                            FileIO.download(this.clock.getId());
                            FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::File for writing downloaded");

                            //Simulate network delay downloading changes
                            Thread.sleep(Constants.DELAY_NETWORK);
                            
                            this.clock.increment();
                            FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Begin writing to file");
                            FileIO.write(this.clock);

                            //Simulate delay for client writing
                            Thread.sleep(Constants.DELAY_CLIENT_WRITE);

                            this.clock.increment();
                            FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Uploading written file");
                            FileIO.upload(this.clock.getId());
                            FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Written file uploaded");
                            
                            //Simulate network delay uploading changes
                            Thread.sleep(Constants.DELAY_NETWORK);

                            FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Finished with file writing");
                            this.event = 0;
                            this.status = Status.IDLE;
                            //Can take all licenses, no reading while writing but this is not necessary
                            //FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::Return all licenses for reading");
                            //this.readLock.release(Constants.NUMBER_OF_CLIENTS);
                            //FileLogger.writeSimulation(this.clock, "[INFO]:[Operator#run]::All licenses for reading returned");
                        }
                        this.writeLock.release();
                        //End critical writing section - non concurrent -serial
                        break;
                    }
                }

                while (!this.busy && this.status == Status.IDLE && !this.queue.isEmpty()) {
                    VectorClock other = this.queue.remove();

                    this.clock.increment();

                    this.output.writeInt(Command.REPLY_WRITE.ordinal());
                    this.output.writeObject(this.clock);
                    this.output.writeInt(other.getId());

                    this.output.flush();
                    this.output.reset();

                    if (this.queue.isEmpty()) break;
                }
            }
        } catch (InterruptedException iex) {
            System.err.println(iex.getMessage());
        } catch (ClassNotFoundException cnfex) {
            System.err.println(cnfex.getMessage());
        } catch (IOException ioex) {
            System.err.println(ioex.getMessage());
        }
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isBusy() {
        return this.busy;
    }
}