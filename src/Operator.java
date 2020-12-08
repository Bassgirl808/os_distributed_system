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
    private ObjectInputStream input;
    private ObjectOutputStream output;
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
        this.busy = true;
        this.queue = new LinkedList<VectorClock>();
        //Read Semaphore has licenses allowing for parallel execution of reads (partially consistent reading)
        this.readLock = new Semaphore(Constants.NUMBER_OF_CLIENTS);
        //Write Semaphore is used for ensuring serial execution of writes
        this.writeLock = new Semaphore(1);
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
                        //Begin criticical reading section - parallelism allowed
                        this.readLock.acquire();

                        this.clock.increment();

                        //Simulate copying file over network for partially consistent reading
                        FileIO.copy(this.clock.getId() - 1);
                        //Simulate network delay and reading time
                        Thread.sleep(Constants.DELAY_NETWORK);
                        Thread.sleep(Constants.DELAY_CLIENT_READ);
                        //Simulate deleting file
                        FileIO.deleteCopy(this.clock.getId() - 1);

                        this.clock.increment();

                        this.status = Status.IDLE;

                        this.readLock.release();
                        //End critical reading section - parallelism allowed
                        break;
                    case REQUEST_WRITE: {
                        if (this.status == Status.WRITING) {
                            int comparison = this.clock.compareTo(otherClock);

                            //this->other or (this||other and this smaller id than other) - either our clock is not as new as theirs or arbitrary if parallel
                            if (comparison == -1 || (comparison == 0 && this.clock.getId() < otherClock.getId())) {
                                this.queue.add(otherClock);
                                this.busy = true;
                            //other->this or (this||other and this larger id than other) - either our clock is newer than theirs or arbitrary if parallel
                            } else if (comparison == 1 || (comparison == 0 && this.clock.getId() > otherClock.getId())) {
                                this.clock.increment();

                                this.output.writeInt(Command.REPLY_WRITE.ordinal());
                                this.output.writeObject(this.clock);
                                this.output.writeInt(otherClock.getId());
                                
                                this.output.flush();
                                this.output.reset();
                            } 
                        } else {
                            this.clock.increment();

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
                        writeLock.acquire();

                        this.event++;

                        if (this.event >= Constants.NUMBER_OF_CLIENTS) {
                            this.clock.increment();
                            FileIO.download(this.clock.getId() - 1);

                            //Simulate network delay downloading changes
                            Thread.sleep(Constants.DELAY_NETWORK);
                            
                            this.clock.increment();
                            FileIO.write(this.clock);

                            //Simulate delay for client writing
                            Thread.sleep(Constants.DELAY_CLIENT_WRITE);

                            this.clock.increment();
                            FileIO.upload(this.clock.getId() - 1);
                            
                            //Simulate network delay uploading changes
                            Thread.sleep(Constants.DELAY_NETWORK);

                            this.event = 0;
                            this.status = Status.IDLE;
                        }

                        writeLock.release();
                        //End critical writing section - non concurrent -serial
                        break;
                    }
                }

                while (!this.busy && this.status == Status.IDLE) {
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