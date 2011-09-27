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
	JList projectList;
	JLabel topLabel;
	JButton selectAllButton;
	JButton OKbutton, cancelButton;
	String [] selectedProjects;
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
	public SelectionDialog (final JFrame _parent, List<String> items, boolean singleSelection, String headerOkString, String headerNotOkString){
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
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
    	projectList = new JList(items.toArray());
    	if (singleSelection) projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	itemCount = items.size();
    	
    	projectList.addMouseListener(new MouseAdapter() {
    	    public void mouseClicked(MouseEvent evt) {
    	        if (evt.getClickCount() == 2) {
    	        	okButtonClicked();
    	        }
    	    }
    	});

    	JScrollPane scrollPane = new JScrollPane(projectList);
    	
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
							projectList.addSelectionInterval(0, projectList.getModel().getSize()-1);
						}
		            });
				}			
			});
    	}
    	Dimension d = new Dimension(75, 25);
    	OKbutton.setPreferredSize(d);
    	OKbutton.setMaximumSize(d);
    	OKbutton.setMinimumSize(d);
    	cancelButton.setPreferredSize(new Dimension(75, 25));
    	cancelButton.setMaximumSize(d);
    	cancelButton.setMinimumSize(d);
    	if (!singleSelection) {
	    	selectAllButton.setPreferredSize(new Dimension(75, 25));
	    	selectAllButton.setMaximumSize(d);
	    	selectAllButton.setMinimumSize(d);
	    	buttonBox1.add(selectAllButton);
    	}
    	topLabelBox.add(topLabel);
    	buttonBox2.add(OKbutton);
    	buttonBox2.add(Box.createRigidArea(new Dimension(15, 10)));
    	buttonBox2.add(cancelButton);
    	
    	setBorder(BorderFactory.createTitledBorder(""));
    	add(topLabelBox);
    	add(Box.createRigidArea(new Dimension(15, 10)));
    	add(scrollPane);
    	add(Box.createRigidArea(new Dimension(15, 10)));
    	add(buttonBox1);
    	add(buttonBox2);
	}
	
	
	private void okButtonClicked() {
		setIsOKd(true);
		setSelectedProjects();
		exitDialog();
	}
	
	/**
	 * Sets selected projects to array from list.
	 */
	private void setSelectedProjects() {
		ArrayList<String> _projects = new ArrayList<String>(); 
		int[] selectedIndex = projectList.getSelectedIndices();
		for (int i=0; i<selectedIndex.length; i++) {
		    Object selected = projectList.getModel().getElementAt(selectedIndex[i]);
		    _projects.add(selected.toString());
		}
		this.selectedProjects = _projects.toArray(new String[_projects.size()]);
	}

	/**
	 * Getter for openProjects array.
	 * @return array of openProjects as strings
	 */
	public String [] getOpenProjectsAsArray() {
		return selectedProjects;
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
