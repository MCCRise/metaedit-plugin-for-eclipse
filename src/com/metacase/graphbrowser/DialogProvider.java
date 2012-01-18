/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.graphbrowser;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Dialog provider class offers some basic dialogs. Used in the plugin.
 */
public class DialogProvider {

	/**
	 * Simple information message dialog with OK button.
	 * @param message the message in the window.
	 * @param title the title for the dialog window.
	 */
	public static void showMessageDialog(String message, String title){
	    try {
		UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
	    } catch (Exception e) {
		e.printStackTrace();
	    }
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
	    } catch (Exception e) { 
		e.printStackTrace();
	    }
	    if (JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) return true;
	    else return false;
	}
	
	/**
	 * Opens settings dialog.
	 * @param modal
	 */
	public static void showSettingsDialog(final boolean modal){
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    SettingsDialog.createAndShowGUI(modal);
		}
	    }); 
	}
	
	/**
	 * Shows dialog with to options and returns the answer value.
	 * @param message Message to show
	 * @param title Window title
	 * @param firstButtonText
	 * @param secondButtonText
	 * @return result of dialog (ID of pressed button).
	 */
	public static int showTwoButtonsDialog(String message, String title, String firstButtonText, String secondButtonText) {
	    try {
		UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
	    } catch (Exception e) { 
		e.printStackTrace();
	    }
	    Object[] buttonOptions = new Object[] {firstButtonText, secondButtonText};
	    return JOptionPane.showOptionDialog(null,
		    message,
		    title,
	            JOptionPane.DEFAULT_OPTION,
	            JOptionPane.QUESTION_MESSAGE,
	            null,
	            buttonOptions,
	            buttonOptions[0]);
	}
}
