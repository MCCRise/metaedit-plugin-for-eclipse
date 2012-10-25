/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.graphbrowser;

import java.awt.Dimension;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class SelectionDialog extends JPanel {
	
	/**
	 * JPanel for selecting MetaEdit+ projects from given database.
	 */
	private static final long serialVersionUID = 1L;
	JList itemsList;
	JLabel topLabel;
	JButton selectAllButton;
	JButton OKbutton, cancelButton;
	String [] selectedItems;
	JFrame parent;
	boolean isOKd;
	int itemCount;
	
	/**
	 * Dialog for showing selectable items. Used for showing projects and generators. Sorts items in
	 * alpabetical order before showing them.
	 * @param _parent parent frame.
	 * @param items selectable items in a array.
	 * @param singleSelection boolean value indicating if multiple selections are allowed.
	 * @param headerOkString String for label if itemslist contains items.
	 * @param headerNotOkString String for label if itemslist doesn't contain anything.
	 */
	public SelectionDialog (final JFrame _parent, List<String> items, boolean singleSelection, String headerOkString, String headerNotOkString) {
		DialogProvider.setLookAndFeel();
		parent = _parent;
    	String labelStr;
		setIsOKd(false);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		Collections.sort(items);
    	if (items.size() == 0) {
    		labelStr = headerNotOkString;
    	} else {
    		labelStr = headerOkString;
    	}
    	itemsList = new JList(items.toArray());
    	if (singleSelection) itemsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	itemCount = items.size();
    	
    	itemsList.addMouseListener(new MouseAdapter() {
    	    public void mouseClicked(MouseEvent evt) {
    	        if (evt.getClickCount() == 2) {
    	        	okButtonClicked();
    	        }
    	    }
    	});

    	JScrollPane scrollPane = new JScrollPane(itemsList);
    	
		topLabel = new JLabel(labelStr);	
		Box topLabelBox= Box.createHorizontalBox();
		Box buttonBox1 = Box.createHorizontalBox();
    	Box buttonBox2 = Box.createHorizontalBox();
    	OKbutton = new JButton(new AbstractAction("OK") {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						okButtonClicked();
					}
	            });
			}			
		});
    	
    	cancelButton = new JButton(new AbstractAction("Cancel") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						exitDialog();
					}
	            });
			}			
		});
    	if (!singleSelection) {
	    	selectAllButton = new JButton(new AbstractAction("Select All") {
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							itemsList.addSelectionInterval(0, itemsList.getModel().getSize()-1);
						}
		            });
				}			
			});
    	}
    	Dimension d = new Dimension(75, 25);
    	Dimension m = new Dimension(100, 25);
    	OKbutton.setPreferredSize(d);
    	OKbutton.setMaximumSize(m);
    	OKbutton.setMinimumSize(m);
    	cancelButton.setPreferredSize(d);
    	cancelButton.setMaximumSize(m);
    	cancelButton.setMinimumSize(m);
    	if (!singleSelection) {
	    	selectAllButton.setPreferredSize(d);
	    	selectAllButton.setMaximumSize(m);
	    	selectAllButton.setMinimumSize(m);
	    	buttonBox1.add(selectAllButton);
    	}
    	topLabelBox.add(topLabel);
    	buttonBox2.add(OKbutton);
    	buttonBox2.add(Box.createRigidArea(new Dimension(15, 10)));
    	buttonBox2.add(cancelButton);
    	
    	setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    	add(topLabelBox);
    	add(Box.createRigidArea(new Dimension(15, 10)));
    	add(scrollPane);
    	add(Box.createRigidArea(new Dimension(15, 10)));
    	add(buttonBox1);
    	add(buttonBox2);
	}
	
	
	private void okButtonClicked() {
		setIsOKd(true);
		setSelectedItems();
		exitDialog();
	}
	
	/**
	 * Sets selected projects to array from list.
	 */
	private void setSelectedItems() {
		ArrayList<String> _items = new ArrayList<String>(); 
		int[] selectedIndex = itemsList.getSelectedIndices();
		for (int i=0; i<selectedIndex.length; i++) {
		    Object selected = itemsList.getModel().getElementAt(selectedIndex[i]);
		    _items.add(selected.toString());
		}
		this.selectedItems = _items.toArray(new String[_items.size()]);
	}

	/**
	 * Getter for selectedItems array.
	 * @return array of selected items as strings
	 */
	public String [] getItemsAsArray() {
		return selectedItems;
	}
	
	/**
	 * Closes the dialog.
	 */
	private void exitDialog(){
		   this.setVisible(false);
		   parent.dispose();
	}
	
	/**
	 * Setter for isOKd property. This is set true when OK is cliked
	 * to show that selected projects should be read to parent window.
	 * @param isOKd boolean value
	 */
	public void setIsOKd(Boolean isOKd) {
		this.isOKd = isOKd;
	}

	/**
	 * Getter for isOKd property.
	 * @return value telling if OK button is clicked.
	 */
	public Boolean getIsOKd() {
		return isOKd;
	}
}
