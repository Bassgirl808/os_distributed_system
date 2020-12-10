package osdistributedsystem;

import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Random;

import java.util.List;

import java.util.concurrent.Semaphore;

public class Instructor implements Runnable {
    private volatile ObjectInputStream input;
    private volatile ObjectOutputStream output;
    private volatile VectorClock clock;
    private volatile Operator operator;
    private volatile Semaphore outputLock;
    private Distribution<Instruction> distribution;

    public Instructor(VectorClock clock, ObjectOutputStream output, Operator operator, Semaphore outputLock) {
        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::Building instructor");
        this.clock = clock;
        this.output = output;
        this.operator = operator;
        this.outputLock = outputLock;
        this.distribution = new Distribution<Instruction>(
            List.of(
                new Pair<Instruction, Double>(Instruction.IDLE, Constants.PERCENT_IDLE),
                new Pair<Instruction, Double>(Instruction.READ, Constants.PERCENT_READ),
                new Pair<Instruction, Double>(Instruction.WRITE, Constants.PERCENT_WRITE)
            )
        );
        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::Instructor built");
    }

    public void run() {
        FileLogger.writeClient(this.clock.getId(), "[INFO]:[Instructor#run]::Instruction thread started successfully");
        try {
            while (!Thread.currentThread().interrupted()) {
                while (this.operator.isBusy());
                Thread.sleep(Constants.DELAY_ACTION);
                FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::Generating instructions");
                Instruction instruction = this.getInstruction();
                FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::Instructions generated");

                //Sends requests to read and write
                switch (instruction) {
                    case IDLE:
                        //this.operator.setStatus(Status.IDLE);
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::Instructor IDLING");
                        Thread.sleep(1000);
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::IDLE complete");
                        break;
                    case READ:
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::Requesting to read");
                        this.clock.increment();
                        this.operator.setStatus(Status.READING);

                        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::Acquiring license to read");
                        this.outputLock.acquire();
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::License to read acquired");
                        
                        this.output.writeInt(Command.REQUEST_READ.ordinal());
                        this.output.writeObject(this.clock);

                        this.output.flush();
                        this.output.reset();
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::Returning license to read");
                        this.outputLock.release();
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::License to read returned");
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::Requested to read");
                        break;
                    case WRITE:
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::Requesting to write");
                        this.clock.increment();
                        this.operator.setStatus(Status.WRITING);
                        
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::Acquiring license to write");
                        this.outputLock.acquire();
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::License to write acquired");

                        this.output.writeInt(Command.REQUEST_WRITE.ordinal());
                        this.output.writeObject(this.clock);
                        

                        this.output.flush();
                        this.output.reset();
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::Returning license to write");
                        this.outputLock.release();
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::License to write returned");
                        FileLogger.writeSimulation(this.clock, "[INFO]:[Instructor#run]::Requested to write");
                        break;
                }
            }
        } catch (InterruptedException iex){
            FileLogger.writeSimulation(this.clock, "[ERROR]:[Instructor#run]::Interrupted Exception: " + iex.getMessage());
            System.err.println(iex.getMessage());
            System.exit(1);
        }catch (IOException ioex){
            FileLogger.writeSimulation(this.clock, "[ERROR]:[Instructor#run]::IO Exception: " + ioex.getMessage());
            System.err.println(ioex.getMessage());
            System.exit(1);
        }
    }

    private Instruction getInstruction() {
        return this.distribution.sample();
    }
}
