package osdistributedsystem;

import java.util.List;
import java.util.LinkedList;

import java.util.stream.Stream;
import java.util.stream.Collectors;

import java.util.concurrent.atomic.AtomicBoolean;

//@SuppressWarnings("sunapi")
import sun.misc.Signal;
//@SuppressWarnings("sunapi")
import sun.misc.SignalHandler;

public class ShutdownHandler {
    protected static AtomicBoolean StopMain = new AtomicBoolean(false);
    protected static volatile Thread MainThread = new Thread();
    protected static volatile Thread Server = new Thread();
    protected static volatile List<Thread> Clients = new LinkedList<Thread>();
    protected static volatile List<Thread> ServerThreads = new LinkedList<Thread>();

    protected static void prepareShutdownHandling(Thread mainThread) throws IllegalArgumentException {
        ShutdownHandler.MainThread = mainThread;
        //Shutdown hook
        final Thread shutdown = new Thread() {
            @Override
            public void run() {
                FileLogger.writeBackground("[INFO]:[ShutdownHandler#run]::Shutting down gracefully, press <CTRL-C> again to force quit");
                //Join all threads
                List<Thread> threads = Stream.concat(ShutdownHandler.Clients.stream(), ShutdownHandler.ServerThreads.stream()).collect(Collectors.toList());
                FileLogger.writeBackground("[INFO]:[ShutdownHandler#run]::Stopping all threads - " + (threads.size() + 1) + " total.");
                
                //Shut down all client and serverthread threads
                for (Thread thread : threads) {
                    if (thread.getName().equals(Constants.THREAD_NAME_CLIENT)) {
                        FileLogger.writeBackground("[INFO]:[ShutdownHandler#run]::Stopping client thread ID");
                        try {
                            FileLogger.writeBackground("[INFO]:[ShutdownHandler#run]::Stopping client: " +  thread.getId());
                            thread.join();
                            FileLogger.writeBackground("[INFO]:[ShutdownHandler#run]::Client thread stopped");
                        } catch (InterruptedException iex) {
                            FileLogger.writeBackground("[ERROR]:[ShutdownHandler#run]::Error Stopping client thread: " + thread.getId());
                            System.err.println(iex.getMessage());
                            System.exit(2);
                        }
                    } else if (thread.getName().equals(Constants.THREAD_NAME_SERVERTHREAD)) {
                        FileLogger.writeBackground("[INFO]:[ShutdownHandler#run]::Stopping associated ServerThread thread ID");
                        try {
                            FileLogger.writeBackground("[INFO]:[ShutdownHandler#run]::Stopping ServerThread thread: " +  thread.getId());
                            thread.join();
                            FileLogger.writeBackground("[INFO]:[ShutdownHandler#run]::ServerThread thread stopped");
                        } catch (InterruptedException iex) {
                            FileLogger.writeBackground("[ERROR]:[ShutdownHandler#run]::Error Stopping ServerThread thread: " + thread.getId());
                            System.err.println(iex.getMessage());
                            System.exit(2);
                        }
                    }
                }

                //Shutdown server thread last
                try {
                    FileLogger.writeBackground("[INFO]:[ShutdownHandler#run]::Stopping Server thread");
                    ShutdownHandler.Server.join();
                    FileLogger.writeBackground("[INFO]:[ShutdownHandler#run]::Stopped Server thread");
                } catch (InterruptedException iex) {
                    FileLogger.writeBackground("[ERROR]:[ShutdownHandler#run]::Error stopping Server");
                    System.err.println(iex.getMessage());
                    System.exit(2);
                }

                //Wait for main thread to die
                try {
                    FileLogger.writeBackground("[INFO]:[ShutdownHandler#run]::Stop main thread and wait for it to end");
                    ShutdownHandler.StopMain.set(true);
                    Thread.sleep(2000);
                } catch (InterruptedException iex) {
                    FileLogger.writeBackground("[ERROR]:[ShutdownHandler#run]::Main thread interrupted");
                    System.err.println(iex.getMessage());
                    System.exit(2);
                }
                FileLogger.writeBackground("[INFO]:[ShutdownHandler#run]::Shutdown complete");
                System.exit(0);
            }
        };

        //Handle <CTRL-C> shutdown signal
        Signal.handle(new Signal("INT"), new SignalHandler() {
            private AtomicBoolean shuttingDown = new AtomicBoolean();
            public void handle(Signal sig) {
                if (shuttingDown.compareAndSet(false, true)) {
                    //Graceful shutdown
                    FileLogger.writeBackground("[INFO]:[ShutdownHandler#handle]::Beginning graceful shutdown");
                    shutdown.start();
                } else {
                    FileLogger.writeBackground("[WARN]:[ShutdownHandler#handle]::Force killed application, hard shutdown complete");
                    System.exit(1);
                }
            }
        });
    }
}
