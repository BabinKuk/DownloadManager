package org.common.download;

import java.net.URL;

public interface IDownload {
	/**
	 * Start or resume downloading.
	 */
	public void download();
	
	/**
	 * Get file name portion of URL.
	 * @param url
	 * @return
	 */
	public String getFileName(URL url);
	
	/**
	 * Get this download's URL.
	 * @return
	 */
	public String getUrl();

	/**
	 * Get this download's size.
	 * @return
	 */
	public int getSize();
	
	/**
	 * Get this download's progress.
	 * @return
	 */
	public float getProgress();
	
	/**
	 * Get this download's status.
	 * @return
	 */
	public int getStatus();
	
	/**
	 * Pause this download.
	 */
	public void pause();

	/**
	 * Resume this download.
	 */
	public void resume();

	/**
	 * Cancel this download.
	 */
	public void cancel();

	/**
	 * Mark this download as having an error.
	 */
	public void error();
	
	/**
	 * Notify observers that this download's status has changed.
	 */
	public void stateChanged();
	
}
