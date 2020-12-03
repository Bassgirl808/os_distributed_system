package osdistributedsystem;

import java.io.File;
import java.io.IOException;

import java.lang.InterruptedException;

import java.util.LinkedList;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        ShutdownHandler.prepareShutdownHandling(Thread.currentThread());
        //Set up logging early startup
        FileLogger.writeBackground("[INFO]:[Main#main]::Create logger");
        DirectoryManager.createBackgroundLog();
        FileLogger.writeBackground("[INFO]:[Main#main]::logger created");

        //Initialize class as object to remove necessity for static references
        Main main = new Main();
        main.run();
    }

    //Create run method to non static
    private void run() throws IOException, InterruptedException {
        //Create Directories and Files for the application
        FileLogger.writeBackground("[INFO]:[Main#run]::Create directory manager");
        DirectoryManager directoryManager = new DirectoryManager();
        FileLogger.writeBackground("[INFO]:[Main#run]::Create Directories and files for application");
        directoryManager.create();
        FileLogger.writeBackground("[INFO]:[Main#run]::Free directory manager for garbage collection");
        directoryManager = null;

        //Start Simulation
        FileLogger.writeBackground("[INFO]:[Main#run]::Building Simulation");
        //Create Server Thread
        FileLogger.writeBackground("[INFO]:[Main#run]::Creating thread to run server");
        Thread server = new Thread(new Server(), Constants.THREAD_NAME_SERVER);
        ShutdownHandler.Server = server;
        FileLogger.writeBackground("[INFO]:[Main#run]::Created thread to run server");
        //Create Client Thread
        FileLogger.writeBackground("[INFO]:[Main#run]::Creating threads to run clients");
        LinkedList<Thread> clients = new LinkedList<Thread>();
        for (int i = 0; i < Constants.NUMBER_OF_CLIENTS;) {
            Thread client = new Thread(new Client(++i), Constants.THREAD_NAME_CLIENT);
            clients.add(client);
            ShutdownHandler.Clients.add(client);
        }
        FileLogger.writeBackground("[INFO]:[Main#run]::Client threads created");
        FileLogger.writeBackground("[INFO]:[Main#run]::Simulation Built");

        FileLogger.writeBackground("[INFO]:[Main#run]::Starting Simulation");
        FileLogger.writeBackground("[INFO]:[Main#run]::Starting Server");
        server.start();
        FileLogger.writeBackground("[INFO]:[Main#run]::Server Started");

        FileLogger.writeBackground("[INFO]:[Main#run]::Starting Clients");
        for (Thread client : clients) {
            //Sleep to give the server time to reset to get it waiting for the next connection
            Thread.sleep(2000);
            FileLogger.writeBackground("[INFO]:[Main#run]::Starting Client: " + client.getId());
            client.start();
            FileLogger.writeBackground("[INFO]:[Main#run]::Client Started: " + client.getId());
        }
        FileLogger.writeBackground("[INFO]:[Main#run]::Clients Started");
        FileLogger.writeBackground("[INFO]:[Main#run]::Simulation Started");

        //Busy waiter
        FileLogger.writeBackground("[INFO]:[Main#run]::Simulation running, <CTRL-C> to stop...");
        //Could do while true but triggers errors so do this to short circuit java logic.
        while (!ShutdownHandler.StopMain.get());

        //Application Complete
        FileLogger.writeBackground("[INFO]:[Main#run]::Simulation Complete");
    }
}