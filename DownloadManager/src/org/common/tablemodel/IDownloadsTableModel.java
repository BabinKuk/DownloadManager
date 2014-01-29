package org.common.tablemodel;

import java.util.Observable;

import org.common.download.Download;

public interface IDownloadsTableModel {
	/**
	 * Add a new download to the table.
	 * @param download
	 */
	public void addDownload(Download download);
	
	/**
	 * Get a download for the specified row.
	 * @param row
	 * @return
	 */
	public Download getDownload(int row);
	
	/**
	 * Remove a download from the list.
	 * @param row
	 */
	public void clearDownload(int row);

	/**
	 * Get a column's name.
	 */
	public String getColumnName(int col);
	
	/**
	 * Get a column's class.
	 */
	public Class getColumnClass(int col);
	
}
