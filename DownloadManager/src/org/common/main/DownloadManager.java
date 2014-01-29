package org.common.main;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableModel;

import org.common.download.Download;
import org.common.download.IDownload;
import org.common.tablemodel.DownloadsTableModel;
import org.common.tablemodel.IDownloadsTableModel;
import org.common.tablemodel.ProgressRenderer;

/**
 * Graphic interface for application 
 * @author nbabic
 */
public class DownloadManager extends JFrame implements Observer {
	
	//display components
	//windows and layout managers
	JFrame viewFrame;
	// Set up file menu.
    JMenuBar menuBar;
    JMenu fileMenu;
    // Set up add panel.
    JPanel addPanel;
    // Set up downloads panel.
    JPanel downloadsPanel;
    // Set up buttons panel.
    JPanel buttonsPanel;
    //buttons for managing the selected download.
    JButton addButton;
    JButton pauseButton;
    JButton resumeButton;
    JButton clearButton;
    JButton cancelButton;
    //menu bar components
    JMenuItem fileExitMenuItem;
    
    //Download table's data model.
    private IDownloadsTableModel tableModel;
	
	// Add download text field.
	private JTextField addTextField;
	
	// Table showing downloads.
	private JTable table;
	
	// Currently selected download.
	private IDownload selectedDownload;
	
	// Flag for whether or not table selection is being cleared.
	private boolean clearing;

	// Run Download Manager.
	public void go() {
		System.out.println("DownloadManager.go()");
		// Create all Swing components here
		JFrame.setDefaultLookAndFeelDecorated(false);
		viewFrame = new JFrame("Download Manager");
		viewFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		viewFrame.setSize(640, 480);
		
		// Handle window closing event
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				actionExit();
			}
		});
		
		// Set up menu bar
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
        //menu bar components
		fileExitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		//register action event
		fileExitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	actionExit();
            }
        });
		//add menubar to menu
		menuBar.add(fileMenu);
		fileMenu.add(fileExitMenuItem);
		//set menubar in the frame
		viewFrame.setJMenuBar(menuBar);
		
		// Set up add panel
		addPanel = new JPanel();
		//add panel components
		addTextField = new JTextField(30);
		addButton = new JButton("Add Download");
		//register action event
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionAdd();
			}
		});
		//add to panel
		addPanel.add(addTextField);
		addPanel.add(addButton);
		
		//Set up Downloads table
		tableModel = new DownloadsTableModel();
		table = new JTable((TableModel) tableModel);
		//register action event
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				tableSelectionChanged();
			}
		});
		// Allow only one row at a time to be selected.
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// Set up ProgressBar as renderer for progress column.
		ProgressRenderer renderer = new ProgressRenderer(0, 100);
		// show progress text
		renderer.setStringPainted(true);
		table.setDefaultRenderer(JProgressBar.class, renderer);
		
		// Set table's row height large enough to fit JProgressBar.
		table.setRowHeight((int) renderer.getPreferredSize().getHeight());
		
		// Set up downloads panel.
		downloadsPanel = new JPanel();
		downloadsPanel.setBorder(BorderFactory.createTitledBorder("Downloads"));
		downloadsPanel.setLayout(new BorderLayout());
		downloadsPanel.add(new JScrollPane(table), BorderLayout.CENTER);

		// Set up buttons panel.
		buttonsPanel = new JPanel();
		//button panel components
		pauseButton = new JButton("Pause");
		resumeButton = new JButton("Resume");
		cancelButton = new JButton("Cancel");
		clearButton = new JButton("Clear");
		//register action event
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionPause();
			}
		});
		
		resumeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionResume();
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionCancel();
			}
		});
		
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionClear();
			}
		});
		//disable buttons by default
		pauseButton.setEnabled(false);
		resumeButton.setEnabled(false);
		cancelButton.setEnabled(false);
		clearButton.setEnabled(false);
		//add to panel
		buttonsPanel.add(pauseButton);
		buttonsPanel.add(resumeButton);
		buttonsPanel.add(cancelButton);
		buttonsPanel.add(clearButton);
		
		// Add panels to display.
		viewFrame.getContentPane().setLayout(new BorderLayout());
		viewFrame.getContentPane().add(addPanel, BorderLayout.NORTH);
	    viewFrame.getContentPane().add(downloadsPanel, BorderLayout.CENTER);
	    viewFrame.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		
		//viewFrame.pack();
	    viewFrame.setVisible(true);

	}
	
	/**
	 * Exit application
	 */
	private void actionExit() {
		//System.out.println("DownloadManager.actionExit()");
		//exit application
    	System.exit(0);
	}
	
	/**
	 * Called when table row selection changes.
	 */
	private void tableSelectionChanged() {
		//System.out.println("DownloadManager.tableSelectionChanged()");
		//Unregister from receiving notifications from the last selected download.
		if (selectedDownload != null) {
			((Observable) selectedDownload).deleteObserver(DownloadManager.this);
		}
		
		/* If not in the middle of clearing a download,
		 * set the selected download and register to
		 * receive notifications from it. 
		*/
		if (!clearing && table.getSelectedRow() > -1) {
			selectedDownload = tableModel.getDownload(table.getSelectedRow());
			((Observable) selectedDownload).addObserver(DownloadManager.this);
			updateButtons();
	    }
		
	}

	/**
	 * Update each button's state based off of the
	 * currently selected download's status.
	 */
	private void updateButtons() {
		//System.out.println("DownloadManager.updateButtons()");
		if (selectedDownload != null) {
			int status = selectedDownload.getStatus();
			//System.out.println("DownloadManager.updateButtons() status " + status);
			switch (status) {
				case Download.DOWNLOADING:
					pauseButton.setEnabled(true);
					resumeButton.setEnabled(false);
					cancelButton.setEnabled(true);
					clearButton.setEnabled(false);
					break;
				case Download.PAUSED:
					pauseButton.setEnabled(false);
					resumeButton.setEnabled(true);
					cancelButton.setEnabled(true);
					clearButton.setEnabled(false);
					break;
				case Download.ERROR:
					pauseButton.setEnabled(false);
					resumeButton.setEnabled(true);
					cancelButton.setEnabled(false);
					clearButton.setEnabled(true);
					break;
				default: // COMPLETE or CANCELLED
					pauseButton.setEnabled(false);
					resumeButton.setEnabled(false);
					cancelButton.setEnabled(false);
					clearButton.setEnabled(true);
			}
		} else {
			//System.out.println("No download is selected in table.");
			pauseButton.setEnabled(false);
			resumeButton.setEnabled(false);
			cancelButton.setEnabled(false);
			clearButton.setEnabled(false);
		}
		
	}

	/**
	 * Add a new download.
	 */
	private void actionAdd() {
		System.out.println("DownloadManager.actionAdd()");
		//verify url
		URL verifiedURL = verifyURL(addTextField.getText());
		String downloadedDirName;
		if (verifiedURL != null) {
			System.out.println("DownloadManager.actionAdd() - OK");
			//add download to list of downloads
			tableModel.addDownload(new Download(verifiedURL));
			//reset add text field
			addTextField.setText("");
		} else {
			JOptionPane.showMessageDialog(this, "Invalid Download URL", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Pause selected download
	 */
	private void actionPause() {
		//System.out.println("DownloadManager.actionPause()");
		selectedDownload.pause();
	    updateButtons();
	}
	
	/**
	 * Resume selected download
	 */
	private void actionResume() {
		//System.out.println("DownloadManager.actionResume()");
		selectedDownload.resume();
	    updateButtons();
	}
	
	/**
	 * Cancel selected download
	 */
	private void actionCancel() {
		//System.out.println("DownloadManager.actionCancel()");
		selectedDownload.cancel();
	    updateButtons();
	}

	/**
	 * Clear selected download
	 */
	private void actionClear() {
		//System.out.println("DownloadManager.actionClear()");
		clearing = true;
		tableModel.clearDownload(table.getSelectedRow());
		clearing = false;
		selectedDownload = null;
	    updateButtons();
	}

	/**
	 * Update is called when a Download notifies its
	 * observers of any changes.
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		//System.out.println("DownloadManager.update()");
		// Update buttons if the selected download has changed.
		if (selectedDownload != null && selectedDownload.equals(arg0)) {
			updateButtons();
		}
	}
	
	/**
	 * Verify download URL
	 */
	private URL verifyURL(String url) {
		//System.out.println("DownloadManager.verifyUrl()");
		
		// Only allow HTTP URLs.
		//System.out.println("DownloadManager.verifyUrl() url - " + url);
		if (!url.toLowerCase().startsWith("http://")) {
			return null;
		}
		      
		// Verify format of URL.
	    URL verifiedUrl = null;
	    try {
			verifiedUrl = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			return null;
		}
	    
		// Make sure URL specifies a file.
	    //System.out.println("DownloadManager.verifyUrl() getFile - " + verifiedUrl.getFile());
	    if (verifiedUrl.getFile().length() < 2) {
	    	//System.out.println("DownloadManager.verifyURL() < 2");
	    	return null;
	    }
	    
	    //System.out.println("DownloadManager.verifyURL() return - " + verifiedUrl);
		return verifiedUrl;
	}

}