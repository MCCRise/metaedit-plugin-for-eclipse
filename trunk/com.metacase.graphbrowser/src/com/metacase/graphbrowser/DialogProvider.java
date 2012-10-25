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
/**
 * @author metacase
 *
 */
/**
 * @author metacase
 *
 */
/**
 * @author metacase
 *
 */
/**
 * @author metacase
 *
 */
/**
 * @author metacase
 *
 */
public class DialogProvider {

	/**
	 * Simple information message dialog with OK button.
	 * @param message the message in the window.
	 * @param title the title for the dialog window.
	 */
	public static void showMessageDialog(String message, String title){
		setLookAndFeel();
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Message dialog to for asking questions from user. 
	 * @param message the message in the window.
	 * @param title the title for the dialog window.
	 * @return true if <b>OK</b> clicked, false if <b>Cancel</b> cliked.
	 */
	public static boolean showYesNoMessageDialog(String message, String title){
		setLookAndFeel();
		return (JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
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
	 * Ensure correct look and feel on Windows
	 */
	public static void setLookAndFeel(){
		if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			} catch (Exception e) { }
		}
	}

}
