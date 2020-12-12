//Operating Systems Distributed System package
package osdistributedsystem;

//Exceptions
import java.io.IOException;

//Date and timestamping
import java.util.Date;
import java.text.SimpleDateFormat;

//File Reading and Writing
import java.io.PrintWriter;
import java.io.RandomAccessFile;

public class FileLogger {
	//Used for writing simulation data
	public static void writeSimulation(VectorClock clock, String message) {
		try {
			//VectorClock should be valid unless there is an error in serialization and transmission of clock
			if (clock == null) {
				//Print error regarding null clock
				System.err.println("[ERROR]:[FileLogger#writeSimulation]::null VectorClock: " + message);
				//Break out of method on invalid clock
				return;
			}

			//Set absolute file location for writing to (logfile including directory relative to ant buildfile working directory set in the jar)
			String absoluteFileUri = "./" + Constants.DIRECTORY_PC[clock.getId()] + "/" + Constants.FILE_LOG_LOCAL;

			//RandomAccessFile used to write to files maintaining synchronicity on device storage in the case of multiple pieces writing to same file
			RandomAccessFile randomAccessFile = new RandomAccessFile(absoluteFileUri, "rw");
			
			//Advance cursor to EOF
			randomAccessFile.seek(randomAccessFile.length());

			//Create message
			String msg = createTimeStamp() + "\t" + clock + "\t" + message + "\n";

			//Write message to end of file and console
			randomAccessFile.writeBytes(msg);
			System.out.println(msg);

			//Prevent Memory Leaks
			randomAccessFile.close();
		} catch (IOException iex) { System.err.println(iex); System.exit(2); }
	}

	//Used for writing client data (non simulation)
	public static void writeClient(int id, String message) {
		try {
			//Set absolute file location for writing to (logfile including directory relative to ant buildfile working directory set in the jar)
			String absoluteFileUri = "./" + Constants.DIRECTORY_PC[id] + "/" + Constants.FILE_LOG_BACKGROUND;

			//RandomAccessFile used to write to files maintaining synchronicity on device storage in the case of multiple pieces writing to same file
			RandomAccessFile randomAccessFile = new RandomAccessFile(absoluteFileUri, "rw");
			
			//Advance cursor to EOF
			randomAccessFile.seek(randomAccessFile.length());

			//Create message
			String msg = createTimeStamp() + "\t[ServerThread-" + id + "]\t" + message + "\n";

			//Write message to end of file and console
			randomAccessFile.writeBytes(msg);
			System.out.println(msg);

			//Prevent Memory Leaks
			randomAccessFile.close();
		} catch (IOException iex) { System.err.println(iex); System.exit(2); }
	}

	//Used for writing server thread data (non simulation)
	public static void writeServerThread(int id, String message) {
		try {
			//Set absolute file location for writing to (logfile including directory relative to ant buildfile working directory set in the jar)
			String absoluteFileUri = "./" + Constants.DIRECTORY_PC[id] + "/" + Constants.FILE_LOG_GLOBAL;

			//RandomAccessFile used to write to files maintaining synchronicity on device storage in the case of multiple pieces writing to same file
			RandomAccessFile randomAccessFile = new RandomAccessFile(absoluteFileUri, "rw");
			
			//Advance cursor to EOF
			randomAccessFile.seek(randomAccessFile.length());

			//Create message
			String msg = createTimeStamp() + "\t[PC" + id + "]\t" + message + "\n";

			//Write message to end of file and console
			randomAccessFile.writeBytes(msg);
			System.out.println(msg);

			//Prevent Memory Leaks
			randomAccessFile.close();
		} catch (IOException iex) { System.err.println(iex); System.exit(2); }
	}

	//Used for writing data from the simulation controller (server/ServerThreads)
	public static void writeServer(String message) {
		try {
			//Set absolute file location for writing to (logfile including directory relative to ant buildfile working directory set in the jar)
			String absoluteFileUri = Constants.DIRECTORY_CONTROLLER + Constants.FILE_LOG_BACKGROUND;

			//RandomAccessFile used to write to files maintaining synchronicity on device storage in the case of multiple pieces writing to same file
			RandomAccessFile randomAccessFile = new RandomAccessFile(absoluteFileUri, "rwd");
			
			//Advance cursor to EOF
			randomAccessFile.seek(randomAccessFile.length());

			//Create message
			String msg = createTimeStamp() + "\t" + message + "\n";

			//Write message to end of file and console
			randomAccessFile.writeBytes(msg);
			System.out.println(msg);

			//Prevent Memory Leaks
			randomAccessFile.close();
		} catch (IOException iex) { System.err.println(iex); System.exit(2); }
	}

	//Used for writing data from the simulation controller
	public static void writeController(VectorClock clock, String message) {
		try {
			//VectorClock should be valid unless there is an error in serialization and transmission of clock
			if (clock == null) {
				//Print error regarding null clock
				System.err.println("[ERROR]:[FileLogger#writeController]::null VectorClock: " + message);
				return;
			}

			//Set absolute file location for writing to (logfile including directory relative to ant buildfile working directory set in the jar)
			String absoluteFileUri = Constants.DIRECTORY_CONTROLLER + Constants.FILE_LOG_GLOBAL;

			//RandomAccessFile used to write to files maintaining synchronicity on device storage in the case of multiple pieces writing to same file
			RandomAccessFile randomAccessFile = new RandomAccessFile(absoluteFileUri, "rwd");
			
			//Advance cursor to EOF
			randomAccessFile.seek(randomAccessFile.length());

			//Create message
			String msg = createTimeStamp() + "\t" + message + "\t" + clock.toString() + "\n";

			//Write message to end of file and console
			randomAccessFile.writeBytes(msg);
			//System.out.println(msg);

			//Prevent Memory Leaks
			randomAccessFile.close();
		} catch (IOException iex) { System.err.println(iex); System.exit(2); }
	}

	//Used for writing background log data not pertaining to the controller or simulation PCs
	public static void writeBackground(String message) {
		try {
			//Set absolute file location for writing to (logfile including directory relative to ant buildfile working directory set in the jar)
			String absoluteFileUri = Constants.DIRECTORY_LOG + Constants.FILE_LOG_BACKGROUND;

			//RandomAccessFile used to write to files maintaining synchronicity on device storage in the case of multiple pieces writing to same file
			RandomAccessFile randomAccessFile = new RandomAccessFile(absoluteFileUri, "rwd");
			
			//Advance cursor to EOF
			randomAccessFile.seek(randomAccessFile.length());
			
			//Create message
			String msg = createTimeStamp() + "\t" + message + "\n";

			//Write message to end of file and console
			randomAccessFile.writeBytes(msg);
			System.out.println(msg);

			//Prevent Memory Leaks
			randomAccessFile.close();
		} catch (IOException iex) { System.err.println(iex); System.exit(2); }
	}

	//Create timestamps for use in logging accurate time scale of actions
	private static String createTimeStamp() {
		//Create new timestamp in the format of [2020/11/17 21:43:39::302842] = [year/month/day hour:minute:second:millisecond] 
		return "[" + (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss::SSS").format(new Date())) + "]";
	}
}