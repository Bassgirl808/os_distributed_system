//Operating Systems Distributed System package
package osdistributedsystem;

//File io utilities
import java.io.File;
import java.io.RandomAccessFile;

//Exceptions
import java.io.IOException;

//Directory Manager class to setup directory and file structure of application on startup if non-existant
public class DirectoryManager {
    //Create all necessary files and directories for the application
    public void create() throws IOException {
        FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Creating directories");

        //Initialize file object for use in creating directories and files
        File file = null;

        //Catch Exceptions from IO tasks
        try {
            //Create Controller Files and Directory
            FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Creating directory for Controller");
            file = new File(Constants.DIRECTORY_CONTROLLER);
            file.mkdirs();

            FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Make sure files are created or exists");
            //Create and/or wipe Controller Global Log File
            FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Creating controller global log");
            file = new File(Constants.DIRECTORY_CONTROLLER + Constants.FILE_LOG_GLOBAL);
            file.createNewFile();
            FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Controller global log created or exists");
            FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Wiping controller global log");
            DirectoryManager.wipeFile(file);
            FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Controller global log wiped");

            //Create and/or wipe Controller Local Log File
            FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Creating controller local log");
            file = new File(Constants.DIRECTORY_CONTROLLER + Constants.FILE_LOG_LOCAL);
            file.createNewFile();
            FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Controller local log created or existed");
            FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Wiping controller local log");
            DirectoryManager.wipeFile(file);
            FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Controller local log wiped");

            //Dynamically generate PCs up to 
            for (int i = 0; i < Constants.NUMBER_OF_CLIENTS; i++) {
                //Create PC[i] Directory
                FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Creating directory for PC" + (i+1));
                file = new File(Constants.DIRECTORY_PC[i]);
                file.mkdirs();
                FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Directories created or exists");

                //Create and/or wipe PC[i] Global log
                FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Creating PC" + (i+1) + " global log");
                file = new File(Constants.DIRECTORY_PC[i] + Constants.FILE_LOG_GLOBAL);
                file.createNewFile();
                FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::PC" + (i+1) + " global log created or existed");
                FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Wiping PC" + (i+1) + " global log");
                DirectoryManager.wipeFile(file);
                FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::PC" + (i+1) + " global log wiped");
                
                //Create and/or wipe PC[i] Local log
                FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Creating PC" + (i+1) + " local log");
                file = new File(Constants.DIRECTORY_PC[i] + Constants.FILE_LOG_LOCAL);
                file.createNewFile();
                FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::PC" + (i+1) + " local log created or existed");
                FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::Wiping PC" + (i+1) + " local log");
                DirectoryManager.wipeFile(file);
                FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::PC" + (i+1) + " local log wiped");
                
                //If PC[i] is PC1, create and/or wipe target file
                if (i == 0) {
                    //Target File
                    FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::PC" + (i+1) + " Creating target file");
                    file = new File(Constants.DIRECTORY_PC[i] + "/" + Constants.FILE_TARGET);
                    file.createNewFile();
                    FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::PC" + (i+1) + " Target file created or existed");
                    //Create File Empty
                    FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::PC" + (i+1) + " Wiping target file");
                    DirectoryManager.wipeFile(file);
                    FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::PC" + (i+1) + " Target file wiped");
                }
            }
        } catch (IOException iex) {
            System.out.println("[ERROR]:[DirectoryManager#create]::Failure at DirectoryManager");
            System.exit(2);
        }

        FileLogger.writeBackground("[INFO]:[DirectoryManager#create]::DirectoryManager is good");
    }

    public static void createBackgroundLog() {
        try {
            //Background Log
            //Make sure directoryManager exists
            File file = new File(Constants.DIRECTORY_LOG);
            file.mkdirs();
            //Make sure file exists
            file = new File(Constants.DIRECTORY_LOG + Constants.FILE_LOG_BACKGROUND);
            file.createNewFile();
            //Make sure file is wiped
            DirectoryManager.wipeFile(file);
        } catch (IOException iex) {
            System.err.println("[ERROR]:[DirectoryManager#createBackgroundLog]::Failed to create background log!");
            System.exit(2);
        }
    }

    public static void wipeFile(String absoluteUri) {
        try {
            //RandomAccessFile to wipe file at absolute uri location
            RandomAccessFile randomAccessFile = new RandomAccessFile(absoluteUri, "rw");
            
            //Set length to 0 to wipe
            randomAccessFile.setLength(0);
            //Close to prevent memory leaks
            randomAccessFile.close();
        } catch (IOException iex) {
            System.exit(2)
        }
    }
    
    public static void wipeFile(File file) {
        try {
            //RandomAccessFile to wipe file
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            
            //Set length to 0 to wipe
            randomAccessFile.setLength(0);
            //Close to prevent memory leaks
            randomAccessFile.close();
        } catch (IOException iex) {
            System.exit(2);
        }
    }
}