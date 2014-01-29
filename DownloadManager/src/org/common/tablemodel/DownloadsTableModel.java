package org.common.tablemodel;

import java.util.*;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

import org.common.download.Download;

/**
 * This class manages the download table's data as JTable object in graphic interface.
 * @author nbabic
 */
public class DownloadsTableModel extends AbstractTableModel implements IDownloadsTableModel, Observer {

	// Names for the table's columns
	private static final String[] columnNames = {"URL", "Size", "Progress", "Status"};
	
	// Classes for each column's values
	private static final Class[] columnClasses = {String.class, String.class, JProgressBar.class, String.class};
	
	// The table's list of downloads.
	private ArrayList<Download> downloadList = new ArrayList<Download>();
	
	/**
	 * Add a new download to the table.
	 * @param download
	 */
	public void addDownload(Download download) {
		//System.out.println("DownloadsTableModel.addDownload() " + download);
		// Register to be notified when the download changes.
		download.addObserver(this);
		
		// add to the list of downloads
		downloadList.add(download);
		
		// notify table to insert new row
		fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
	}
	
	/**
	 * Get a download for the specified row.
	 * @param row
	 * @return
	 */
	public Download getDownload(int row) {
		//System.out.println("DownloadsTableModel.getDownload() row " + row);
		return downloadList.get(row);
	}
	
	/**
	 * Remove a download from the list.
	 * @param row
	 */
	public void clearDownload(int row) {
		//System.out.println("DownloadsTableModel.clearDownload() row " + row);
		downloadList.remove(row);
		
		// Fire table row deletion notification to table.
		fireTableRowsDeleted(row, row);
	}

	/**
	 * Get table's column count.
	 */
	@Override
	public int getColumnCount() {
		//System.out.println("DownloadsTableModel.getColumnCount() " + columnNames.length);
		return columnNames.length;
	}
	
	/**
	 * Get a column's name.
	 */
	public String getColumnName(int col) {
		//System.out.println("DownloadsTableModel.getColumnName() " + columnNames[col]);
		return columnNames[col];
	}
	
	/**
	 * Get a column's class.
	 */
	public Class getColumnClass(int col) {
		//System.out.println("DownloadsTableModel.getColumnClass() col " + col);
		return columnClasses[col];
	}
	
	/**
	 * Get table's row count.
	 */
	@Override
	public int getRowCount() {
		//System.out.println("DownloadsTableModel.getRowCount() " + downloadList.size());
		return downloadList.size();
	}
	
	/**
	 * Get value for a specific row and column combination.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		//System.out.println("DownloadsTableModel.getValueAt()");
		
		//get download object for selected row
		Download download = downloadList.get(rowIndex);
		//get values for each column
		switch (columnIndex) {
			case 0: // URL
				//System.out.println("DownloadsTableModel.getValueAt() case0");
				return download.getUrl();
			case 1: // Size
				//System.out.println("DownloadsTableModel.getValueAt() case1");
				int size = download.getSize();
				return (size == -1) ? "" : Integer.toString(size);
			case 2: // Progress
				//System.out.println("DownloadsTableModel.getValueAt() case2");
				return new Float(download.getProgress());
			case 3: // Status
				//System.out.println("DownloadsTableModel.getValueAt() case3");
				return Download.STATUSES[download.getStatus()];
		}
	    return "";
	}

	/**
	 * Update is called when a Download notifies its observers of any changes. 
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		//System.out.println("DownloadsTableModel.update()");
		
		int index = downloadList.indexOf(arg0);
		
		// Fire table row update notification to table.
	    fireTableRowsUpdated(index, index);		
	}

}