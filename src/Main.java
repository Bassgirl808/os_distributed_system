package osdistributedsystem;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        //Set up logging early startup
        FileLogger.writeBackground("[INFO]:[Main#main]::Create logger");
        DirectoryManager.createBackgroundLog();
        FileLogger.writeBackground("[INFO]:[Main#main]::logger created");

        //Initialize class as object to remove necessity for static references
        Main main = new Main();
        main.run();
    }

    //Create run method to non static
    public void run() throws IOException {
        //Create Directories and Files for the application
        FileLogger.writeBackground("[INFO]:[Main#run]::Create directory manager");
        DirectoryManager directoryManager = new DirectoryManager();
        FileLogger.writeBackground("[INFO]:[Main#run]::Create Directories and files for application");
        directoryManager.create();
        FileLogger.writeBackground("[INFO]:[Main#run]::Free directory manager for garbage collection");
        directoryManager = null;

        //Start GUI - JavaFX or Swing
        FileLogger.writeBackground("[INFO]:[Main#run]::Start GUI");
        //this.startGui();

        //Application Complete
        FileLogger.writeBackground("[INFO]:[Main#run]::Startup Complete");
    }
}