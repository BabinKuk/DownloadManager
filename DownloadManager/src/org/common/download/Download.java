package org.common.download;

import java.io.*;
import java.io.ObjectInputStream.GetField;
import java.net.*;
import java.util.*;

import javax.swing.JFileChooser;

/**
 * This class downloads a file from a URL.
 * Every time when new download is added to the list, 
 * new Download object is created to manage the download process.
 * @author nbabic
 */
public class Download extends Observable implements IDownload, Runnable {
	
	// Max size of download buffer.
	private static final int MAX_BUFFER_SIZE = 1024;
	
	// Status names
	public static final String STATUSES[] = { "Downloading", "Paused", "Complete", "Cancelled", "Error" };
	
	// Status codes
	public static final int DOWNLOADING = 0;
	public static final int PAUSED = 1;
	public static final int COMPLETE = 2;
	public static final int CANCELLED = 3;
	public static final int ERROR = 4;

	// download URL
	private URL url;
	// size of download in bytes
	private int size;
	// number of bytes downloaded
	private int downloaded;
	// current status of download
	private int status;
	
	String fileName;
	JFileChooser fileSave = new JFileChooser();
	
	/**
	 * Pass url into Constructor 
	 * @param url
	 */
	public Download(URL url) {
		//System.out.println("Download.Download()");
		this.url = url;
		size = -1; // initial value, not defined 
		downloaded = 0;
		status = DOWNLOADING;
		
		//get download filename
		getDownloadFileName();
		
		//start downloading
		download();
	}

	private void getDownloadFileName() {
		//System.out.println("Download.getDownloadFileName() url - " + url);
		
		fileSave.setSelectedFile(new File(getFileName(url)));
		fileSave.showSaveDialog(fileSave);
		
		fileName = fileSave.getSelectedFile().toString();
		//System.out.println("Download.getDownloadFileName() fileName - " + fileName);
		
	}

	/**
	 * Start or resume downloading.
	 */
	public void download() {
		//System.out.println("Download.download()");
		//separate each download in its own thread
		Thread thread = new Thread(this);
		//run
		thread.start();
	}

	/**
	 * Download file
	 */
	@Override
	public void run() {
		//System.out.println("Download.run()");
		
		//remote file to read from
		RandomAccessFile file = null;
		//local file to write into
		InputStream stream = null;
		
		try {
			// Open connection to URL.
			//cast into HttpURLConnection because only http protocol is supported
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			// Specify what portion of file to download.
			connection.setRequestProperty("Range", "bytes-" + downloaded + "-");
			
			// Connect to server.
			connection.connect();
			
			// Make sure that everything's OK: response code is in the 200 range.
			// HTTP protocol has a list of codes
			// response code range of 200 identifies success 
			//System.out.println("Download.run() connection.getResponseCode() " + connection.getResponseCode());
			if (connection.getResponseCode() / 100 != 2) {
				error();
			}
			
			// Check for valid content length.
			int contentLength = connection.getContentLength();
			//int contentLength = connection.getContentLength() + downloaded;
			if (contentLength < 1) {
				error();
			}
			
			// Set the size for this download if it hasn't been already set.
			if (size == -1) {
				size = contentLength;
				System.out.println("Download.run() size " + size);
				stateChanged();
			}
			
			// Open file and seek to the end of it.
			//file = new RandomAccessFile(getFileName(url), "rw");
			file = new RandomAccessFile(fileName, "rw");

			// start/resume download after last downloaded byte
			// this way we ensure that if the download is stopped, next time it won't start from the beginning   
			file.seek(downloaded);
			
			stream = connection.getInputStream();
			stream.skip(downloaded);
			
			// loop is repeating until the status is set to DOWNLOADING
			System.out.println("Download.run() status " + status + ", downloaded - " + downloaded);
			while (status == DOWNLOADING) {
				// Size buffer according to how much of the file is left to download.
				byte buffer[];
				//if it is bigger than max size, increase to max
				//else set it to size left to download
				if (size - downloaded > MAX_BUFFER_SIZE) {
					buffer = new byte[MAX_BUFFER_SIZE];
				} else {
					buffer = new byte[size - downloaded];
				}
				//System.out.println("Download.run() buffer.length " + buffer.length);
				
				// Read bytes from server into buffer.
				int count = stream.read(buffer);
				System.out.println("Download.run() count " + count);
				
				//check if download is finished and exit loop
				if (count == -1) {
					break;
				}
				
				// Write buffer to file.
				file.write(buffer, 0, count);
				downloaded += count;
				stateChanged();
			}
			System.out.println("Download.run() u breaking " + status);
			// Change status to complete if this point was reached because downloading has finished.
			if (status == DOWNLOADING) {
				System.out.println("Download.run() u DOWNLOADING " + status);
				status = COMPLETE;
				stateChanged();
			}
			
		} catch (Exception e) {
			System.out.println("Download.run() Exception");
			error();
		} finally {
			// Close file.
			if (file != null) {
				//System.out.println("Download.run() finally");
				try {
					file.close();
					//System.out.println("Download.run() file.close()");
				} catch (Exception e) {}
			}
		}
		
		// Close connection to server.
		if (stream != null) {
			try {
				stream.close();
			} catch (Exception e) {}
		}
		
	}

	/**
	 * Get file name portion of URL.
	 * @param url
	 * @return
	 */
	public String getFileName(URL url) {
		//System.out.println("Download.getFileName() " + url);
		String fileName = url.getFile();
		//filename is after last char '/'
		fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
		return fileName;
	}
	
	/**
	 * Get this download's URL.
	 * @return
	 */
	public String getUrl() {
		//System.out.println("Download.getUrl()");
		return url.toString();
	}

	/**
	 * Get this download's size.
	 * @return
	 */
	public int getSize() {
		//System.out.println("Download.getSize()");
		return size;
	}
	
	/**
	 * Get this download's progress.
	 * @return
	 */
	public float getProgress() {
		//System.out.println("Download.getProgress()");
		return ((float) downloaded / size) * 100;
	}
	
	/**
	 * Get this download's status.
	 * @return
	 */
	public int getStatus() {
		//System.out.println("Download.getStatus()");
		return status;
	}
	
	/**
	 * Pause this download.
	 */
	public void pause() {
		System.out.println("Download.pause()");
		status = PAUSED;
		stateChanged();
	}

	/**
	 * Resume this download.
	 */
	public void resume() {
		//System.out.println("Download.resume()");
		status = DOWNLOADING;
		stateChanged();
		download();
	}

	/**
	 * Cancel this download.
	 */
	public void cancel() {
		//System.out.println("Download.cancel()");
		status = CANCELLED;
		stateChanged();
	}

	/**
	 * Mark this download as having an error.
	 */
	public void error() {
		//System.out.println("Download.error()");
		status = ERROR;
		stateChanged();
	}

	/**
	 * Notify observers that this download's status has changed.
	 */
	public void stateChanged() {
		//System.out.println("Download.stateChanged()");
		//notify about changes
		setChanged();
		//notify Observer classes (DownloadsTableModel and Download Manager implement Observer)
	    notifyObservers();
	}
	
}