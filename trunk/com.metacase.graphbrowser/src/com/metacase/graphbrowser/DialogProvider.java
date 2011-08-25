/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.graphbrowser;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Dialog provider class offers some basic dialogs.
 */
public class DialogProvider {

	private DialogProvider(){
	}
	
	/**
	 * Simple information message dialog with OK button.
	 * @param message the message in the window.
	 * @param title the title for the dialog window.
	 */
	public static void showMessageDialog(String message, String title){
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) { }
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Message dialog to for asking questions from user. 
	 * @param message the message in the window.
	 * @param title the title for the dialog window.
	 * @return true if <b>OK</b> clicked, false if <b>Cancel</b> cliked.
	 */
	public static boolean showYesNoMessageDialog(String message, String title){
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) { }
		if (JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION)
				== JOptionPane.YES_OPTION) return true;
		else return false;
	}
	
	/**
	 * Opens settings dialog.
	 * @param initialLaunch true if dialog is opened at plug-in start. Else false.
	 */
	public static void showSettingsDialog(final boolean initialLaunch){
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
	           public void run() {
	               SettingsDialog.createAndShowGUI(initialLaunch);
	           }
	    });
	}
}
