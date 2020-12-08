//Operating Systems Distributed System package
package osdistributedsystem;

public interface Constants {
    //Number of clients
    public static final int NUMBER_OF_CLIENTS = 5;

    public static final String IPV4 = "127.0.0.1";
    public static final int PORT = 8020;
    
    //Percentage of actions client should take
    public static final double PERCENT_IDLE = 0.10;
    public static final double PERCENT_READ = 0.70;
    public static final double PERCENT_WRITE = 0.20;
    
    //Log File Names
    public static final String FILE_LOG_GLOBAL = "global.log";
    public static final String FILE_LOG_LOCAL = "local.log";
    public static final String FILE_TARGET = "target.txt";
    public static final String FILE_TARGET_COPY = "target_copy.txt";
    public static final String FILE_TARGET_WRITE = "target_write.txt";
    public static final String FILE_LOG_BACKGROUND = "background.log";

    //Directories
    public static final String DIRECTORY_LOG = "data/";
    public static final String DIRECTORY_CONTROLLER = Constants.DIRECTORY_LOG + "controller/";
    public static final String[] DIRECTORY_PC = new String[] {
        Constants.DIRECTORY_LOG + "PC1/",
        Constants.DIRECTORY_LOG + "PC2/",
        Constants.DIRECTORY_LOG + "PC3/",
        Constants.DIRECTORY_LOG + "PC4/",
        Constants.DIRECTORY_LOG + "PC5/"
    };

    //Thread names
    public static final String THREAD_NAME_SERVER = "Server";
    public static final String THREAD_NAME_SERVERTHREAD = "ServerThread";
    public static final String THREAD_NAME_CLIENT = "Client";
    public static final String THREAD_NAME_INSTRUCTOR = "Instructor";
    public static final String THREAD_NAME_OPERATOR = "Operator";

    public static final long DELAY_ACTION = 250;
    public static final long DELAY_NETWORK = 400;
    public static final long DELAY_CLIENT_READ = 4000;
    public static final long DELAY_CLIENT_WRITE = 1500;
}