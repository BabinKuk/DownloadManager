package org.common.tablemodel;

import java.awt.Component;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

/**
 * This class renders a JProgressBar in a table cell.
 * @author nbabic
 */
public class ProgressRenderer extends JProgressBar implements TableCellRenderer {

	public ProgressRenderer(int min, int max) {
		super(min, max);
		//System.out.println("ProgressRenderer.ProgressRenderer()");
	}

	/**
	 * Returns this JProgressBar as the renderer for the given table cell.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, 
													Object value, 
													boolean isSelected,
													boolean hasFocus, 
													int row, 
													int column) {
		//System.out.println("ProgressRenderer.getTableCellRendererComponent()");
	    
		// Set JProgressBar's percent complete value.
		setValue((int) ((Float) value).floatValue());
		return this;
	}

}
