package osdistributedsystem;

import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Random;

import java.util.List;

public class Instructor implements Runnable {
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private volatile VectorClock clock;
    private Operator operator;
    private Distribution<Instruction> distribution;

    public Instructor(VectorClock clock, ObjectInputStream input, ObjectOutputStream output, Operator operator) {
        this.clock = clock;
        this.input = input;
        this.output = output;
        this.operator = operator;
        this.distribution = new Distribution<Instruction>(
            List.of(
                new Pair<Instruction, Double>(Instruction.IDLE, Constants.PERCENT_IDLE),
                new Pair<Instruction, Double>(Instruction.READ, Constants.PERCENT_READ),
                new Pair<Instruction, Double>(Instruction.WRITE, Constants.PERCENT_WRITE)
            )
        );
    }

    public void run() {
        FileLogger.writeClient(this.clock.getId(), "[INFO]:[Instructor#run]::Instruction thread started successfully");
        try {
            while (!Thread.currentThread().interrupted()) {
                while (this.operator.isBusy());
                Thread.sleep(Constants.DELAY_ACTION);
                Instruction instruction = getInstruction();

                switch (instruction) {
                    case IDLE:
                        this.operator.setStatus(Status.IDLE);
                        break;
                    case READ:
                        this.clock.increment();
                        this.operator.setStatus(Status.READING);

                        this.output.writeInt(Command.REQUEST_READ.ordinal());
                        this.output.writeObject(this.clock);

                        this.output.flush();
                        this.output.reset();
                        break;
                    case WRITE:
                        this.clock.increment();
                        this.operator.setStatus(Status.WRITING);
                        
                        this.output.writeInt(Command.REQUEST_WRITE.ordinal());
                        this.output.writeObject(this.clock);

                        this.output.flush();
                        this.output.reset();
                        break;
                }
            }
        } catch (InterruptedException iex){
            FileLogger.writeClient(this.clock.getId(), "[ERROR]:[Instructor#run]::Interrupted Exception: " + iex.getMessage());
            System.err.println(iex.getMessage());
        }catch (IOException ioex){
            FileLogger.writeClient(this.clock.getId(), "[ERROR]:[Instructor#run]::IO Exception: " + ioex.getMessage());
            System.err.println(ioex.getMessage());
        }
    }

    private Instruction getInstruction() {
        return this.distribution.sample();
    }
}
