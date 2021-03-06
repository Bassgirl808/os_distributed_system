package osdistributedsystem;

//Date and timestamping
import java.util.Date;
import java.text.SimpleDateFormat;

//Standard file io
import java.io.File;
import java.io.RandomAccessFile;

//Fancy nio concurrent safe libraries
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

//Exception Handling
import java.io.FileNotFoundException;
import java.io.IOException;

//Semaphore
import java.util.concurrent.Semaphore;

//File IO class is used to perform file operations for the simulation
public class FileIO {
	//Semaphore used to lock access when writing process happening on file
	private static volatile Semaphore writeLock = new Semaphore(1);

	//Copy makes a local copy in your folder of the target file for reading
	public static void copy(int id) throws IOException {
		//Target is location of target file determined to be at PC[0]
    	File target = new File(Constants.DIRECTORY_PC[0] + Constants.FILE_TARGET);
		//Destination is where the copy will be saved
		File destination = new File(Constants.DIRECTORY_PC[id] + Constants.FILE_TARGET_COPY);
		//Copy target to location based on relative URI and replace inlocation if already existing
		Files.copy(target.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	//Delete local copy in your folder of the target file for reading
	public static void deleteCopy(int id) throws IOException {
		//Local copy to delete
		File file = new File(Constants.DIRECTORY_PC[id] + Constants.FILE_TARGET_COPY);
		//File be gone!
		file.delete();
	}

	//Download the target file to local machine for writing
    public static void download(int id) throws IOException, InterruptedException {
		//FileIO.writeLock.acquire();
		//Target is location of target file determined to be at PC[0]
    	File target = new File(Constants.DIRECTORY_PC[0] + Constants.FILE_TARGET);
		//Same name as target in local directory to differentiate from readonly local copy
		File destination = new File(Constants.DIRECTORY_PC[id] + Constants.FILE_TARGET_WRITE);
		//Copy target file to local machine and replace if existing 
		Files.copy(target.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	
	//Upload the target file to the source machine
	public static void upload(int id) throws IOException, InterruptedException {
		//Writing process begins, lock file access
		FileIO.writeLock.acquire();
		//Target is location of target file determined to be at PC[0]
    	File target = new File(Constants.DIRECTORY_PC[0] + Constants.FILE_TARGET);
		//Source is local write file to upload
		File source = new File(Constants.DIRECTORY_PC[id] + Constants.FILE_TARGET_WRITE);
		//Replace original target with local write file
		Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		//Local write file be gone!
		source.delete();
		//Writing process ends Unlock file access
		FileIO.writeLock.release();
	}
	
	//Write is used to modify the local write file
	public static void write(VectorClock clock) throws IOException, InterruptedException {
		//RandomAccessFile is used to write to the local write file
		RandomAccessFile file = new RandomAccessFile(Constants.DIRECTORY_PC[clock.getId()] + Constants.FILE_TARGET_WRITE, "rwd");
		//FileChannel is used for concurrent writing in conjunction with FileLocks
		FileChannel channel = file.getChannel();
		//FileLock is used to ensure concurrent write safety
		FileLock lock = null;
		
		//Attempt to acquire a lock on the channel to ensure exclusivity
		lock = channel.tryLock();
		
		//Go to end of file
		file.seek(file.length());
		//Tag file with identifier and timestamp to show proof of change (at end of file)
		file.writeBytes("[" + (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss::SSS").format(new Date())) + "] - " + clock.toString() + " - wrote to file\n");
		//Release and reset
		lock.release();
		channel.close();
		file.close();
	}
}