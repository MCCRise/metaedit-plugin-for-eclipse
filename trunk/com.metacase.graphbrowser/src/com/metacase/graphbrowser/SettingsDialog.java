/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.graphbrowser;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

import javax.swing.*;

import org.eclipse.core.runtime.Platform;

import com.metacase.verifier.*;

/**
 * Settings dialog for setting the launch parameters. Contains code (in comments) for
 * hostname textfield stuff, its verifier and also for checkbox for selecting
 * if logging should be enabled on API server.
 *
 */
public class SettingsDialog extends JDialog {
	
	private static final long serialVersionUID = 1L;
	private static JDialog dialog;
	private JPanel panel;
	Box programDirBox, workingDirBox, databaseBox, usernameBox, passwordBox, projectsBox, portNumberBox, buttonBox;
	JLabel headerLabel, programDirLabel, workingDirLabel, databaseLabel, usernameLabel, passwordLabel,
	projectsLabel, hostnameLabel, loggingLabel, portLabel;
	JLabel programDirIconLabel, workingDirIconLabel, databaseIconLabel, usernameIconLabel, passwordIconLabel,
	projectsIconLabel, hostnameIconLabel, portIconLabel;
	JTextField programDirField, workingDirField, databaseField, usernameField, passwordField,
	hostnameField, projectsField, portField;
	JCheckBox loggingCheckBox;
	JButton programDirBrowseButton, workingDirBrowseButton, projectsSelectionDialogButton, saveButton, cancelButton;
	Hashtable <JLabel, String []> tooltipTexts = new Hashtable<JLabel, String []>();
	private File managerAbPath = new File("");
	ImageIcon [] icons = { new ImageIcon(getImage("icons/error_icon.png")),
			new ImageIcon(getImage("icons/question_icon.png")), new ImageIcon(getImage("icons/ok_icon.png")) };
	private Settings settings;
	
	public SettingsDialog() {
	}
	
	/**
	 * Constructor.
	 * 
	 * Contains in comments code for the hostname and logging options.
	 * 
	 * @param modal
	 */
	private SettingsDialog(boolean modal){
		this.setModal(modal);
		
		panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		programDirBox = Box.createHorizontalBox();
		workingDirBox = Box.createHorizontalBox();
		databaseBox = Box.createHorizontalBox();
		usernameBox = Box.createHorizontalBox();
		passwordBox = Box.createHorizontalBox();
		projectsBox = Box.createHorizontalBox();
		// Box hostnameBox = Box.createHorizontalBox();
		portNumberBox = Box.createHorizontalBox();
		// Box loggingBox = Box.createHorizontalBox();
		buttonBox = Box.createHorizontalBox();		
		
		//Labels
		programDirLabel = createLabel("MetaEdit+ program path: ");
		workingDirLabel = createLabel("MetaEdit+ working directory: ");
		databaseLabel = createLabel("Database name: ");
		usernameLabel = createLabel("Username: ");
		passwordLabel = createLabel("Password: ");
		projectsLabel = createLabel("Projects: ");
		// hostnameLabel = createLabel("Hostaname: ");
		portLabel = createLabel("Port: ");
		// loggingLabel = createLabel("Logging: ");
		
		// Icon labels and tooltiptexts 
		programDirIconLabel = createIconLabel();
		workingDirIconLabel = createIconLabel();
		databaseIconLabel = createIconLabel();
		usernameIconLabel = createIconLabel();
		passwordIconLabel = createIconLabel();
		projectsIconLabel = createIconLabel();
		// hostnameIconLabel = createIconLabel();
		portIconLabel = createIconLabel();
		
		// Tooltiptexts for textfields. First the error message then neutral message and third the succesfull message (left empty mostly).
		tooltipTexts.put(programDirIconLabel, new String [] {"MetaEdit+ program file not properly set.", "", ""});
		tooltipTexts.put(workingDirIconLabel, new String [] {"Given path not recognized as a MetaEdit+ working directory.", "", ""});
		tooltipTexts.put(databaseIconLabel, new String [] {"Given path not recognized as a MetaEdit+ database",
			"Can't do the verification for given database. Make sure that the given working directory path is correct.", ""});
		tooltipTexts.put(usernameIconLabel, new String [] {"Username not found from the given database",
			"Can't check the given username.", ""});
		tooltipTexts.put(passwordIconLabel, new String [] {"Wrong password!", "Can't check the given password.", ""});
		tooltipTexts.put(projectsIconLabel, new String [] {"One or more projects not found from the database!",
			"Can't check the project name. Make sure the working directory" +
				" and database fields are correct.", ""});
		// tooltipTexts.put(hostnameIconLabel, new String [] {"", "", ""});
		tooltipTexts.put(portIconLabel, new String [] {"Port number should be an number between 1024 and 65535." +
				" By default it's 6390.", "Ports 0-1023 are normally reserved by the system.", ""});
		
		
		// TextFields, passwordfield and logging checkbox;
		programDirField = createTextField();
		workingDirField = createTextField();
		databaseField = createTextField();
		usernameField = createTextField();
		passwordField = new JPasswordField();
		passwordField.selectAll();
		passwordField.setEditable(true);
		projectsField = createTextField();
		// hostnameField = createTextField();
		portField = createTextField();
		// loggingCheckBox = new JCheckBox();
		
		programDirField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) { }
			public void keyReleased(KeyEvent e) {
				verifyField(new ProgramDirectoryVerifier(), programDirField, programDirIconLabel);
			}
			public void keyPressed(KeyEvent e) { }
		});
		
		workingDirField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) { }
			public void keyReleased(KeyEvent e) {
				verifyField(new UsernameVerifier(getManagerAbPath()), usernameField,  usernameIconLabel);
				verifyField(new PasswordVerifier(getManagerAbPath(), usernameField.getText()), passwordField,  passwordIconLabel);
				verifyField(new ProjectsVerifier(getManagerAbPath()), projectsField, projectsIconLabel);
			}
			public void keyPressed(KeyEvent e) { }
		});

		databaseField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {	}
			public void keyReleased(KeyEvent e) { 
				verifyField(new UsernameVerifier(getManagerAbPath()), usernameField,  usernameIconLabel);
				verifyField(new PasswordVerifier(getManagerAbPath(), usernameField.getText()), passwordField,  passwordIconLabel);
				verifyField(new ProjectsVerifier(getManagerAbPath()), projectsField, projectsIconLabel);
			}
			public void keyPressed(KeyEvent e)  { }
		});

		usernameField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {	}
			public void keyReleased(KeyEvent e) {
				verifyField(new UsernameVerifier(getManagerAbPath()), usernameField,  usernameIconLabel);
				verifyField(new PasswordVerifier(getManagerAbPath(), usernameField.getText()), passwordField,  passwordIconLabel);
			}
			public void keyPressed(KeyEvent e) { }
		});

		passwordField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) { }
			public void keyReleased(KeyEvent e) {
				verifyField(new PasswordVerifier(getManagerAbPath(), usernameField.getText()), passwordField,  passwordIconLabel);
			}
			public void keyPressed(KeyEvent e) { }
		});

		projectsField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) { }
			public void keyReleased(KeyEvent e) {
				verifyField(new ProjectsVerifier(getManagerAbPath()), projectsField, projectsIconLabel);
			}
			public void keyPressed(KeyEvent e) { }
		});
		
		/*
	   		hostnameField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) { }
			public void keyReleased(KeyEvent e) {	
				verifyField(new HostnameVerifier(), hostnameField, hostnameIconLabel);
			}
			public void keyPressed(KeyEvent e) { }
		});
		*/
		
		portField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) { }
			public void keyReleased(KeyEvent e) {	
				verifyField(new PortVerifier(), portField, portIconLabel);
			}
			public void keyPressed(KeyEvent e) { }
		});
		
		// Buttons
		programDirBrowseButton = new JButton(new AbstractAction("...") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						 final JFileChooser fc = new JFileChooser();
						 File currentDir = new File(programDirField.getText());
						 if (!currentDir.exists()) currentDir = new java.io.File("C:\\"); 
						 fc.setCurrentDirectory(currentDir);
						 int returnVal = fc.showOpenDialog(dialog); 
						 if (returnVal == JFileChooser.APPROVE_OPTION) {
							 File file = fc.getSelectedFile();
							 programDirField.setText(file.toString());
						 }
						 verifyField(new ProgramDirectoryVerifier(), programDirField, programDirIconLabel);
					}
				});
			}
		});

		workingDirBrowseButton = new JButton(new AbstractAction("...") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
			@Override
					public void run() {
						final JFileChooser fc = new JFileChooser();
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						File currentDir = new File(workingDirField.getText());
						if (!currentDir.exists()) currentDir = new java.io.File("C:\\"); 
						int returnVal = fc.showOpenDialog(dialog); 
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							workingDirField.setText(file.toString());
						}
						verifyField(new WorkingDirectoryVerifier(), workingDirField, workingDirIconLabel);
					}
				});
			}
		});

		projectsSelectionDialogButton = new JButton(new AbstractAction("...") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame frame = new JFrame("");
						frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						final SelectionDialog p = new SelectionDialog(frame,
								Arrays.asList(readFromManagerAb(managerAbPath, "areas")), false,
								"<HTML>Choose the project(s) to open.<br>Multiple selections are allowed.<br></HTML>",
								"<HTML><p>No projects found.</p></HTML>");
						frame.setIconImage(getImage("icons/metaedit_logo.png"));
						JComponent newContentPane = p;
						frame.setContentPane(newContentPane);
						frame.addWindowListener(new WindowListener() {
							public void windowOpened(WindowEvent e) { }
							public void windowIconified(WindowEvent e) { }
							public void windowDeiconified(WindowEvent e) { }
							public void windowDeactivated(WindowEvent e) { }
							public void windowClosing(WindowEvent e) {	}
							public void windowClosed(WindowEvent e) { 
								if (p.getIsOKd()) {
									setProjectsToTextField(p.getItemsAsArray());
									verifyField(new ProjectsVerifier(getManagerAbPath()), projectsField, projectsIconLabel);
								}
							}
							public void windowActivated(WindowEvent e) { }
						});
						//Display the window.
						//frame.pack();
						frame.setResizable(true);
						frame.setVisible(true);
						frame.setLocationRelativeTo(dialog);
						frame.setSize(new Dimension(250, 500)); 
					}
				});
			}
		});
		
		// Create "Save" button ------------>
			saveButton = new JButton(new AbstractAction("Save") {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (!verifyAllFields()) {
							if ( DialogProvider.showYesNoMessageDialog("Are you sure you want to save the settings?",
									"Confirm Save Settings") ) {
								saveSettings();
								exitDialog();
							}
						} else {
							saveSettings();
							exitDialog();
						}
					}
				});

			}
		});
		
		// Create "Cancel" button ---------->
		cancelButton = new JButton(new AbstractAction("Cancel") {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						exitDialog();
					}
				});
			}
		});
			// Add components to boxes.
			programDirBox.add(programDirLabel);
			programDirBox.add(programDirIconLabel);
			programDirBox.add(programDirField);
			programDirBox.add(programDirBrowseButton);
			workingDirBox.add(workingDirLabel);
			workingDirBox.add(workingDirIconLabel);
			workingDirBox.add(workingDirField);
			workingDirBox.add(workingDirBrowseButton);
			databaseBox.add(databaseLabel);
			databaseBox.add(databaseIconLabel);
			databaseBox.add(databaseField);
			usernameBox.add(usernameLabel);
			usernameBox.add(usernameIconLabel);
			usernameBox.add(usernameField);
			passwordBox.add(passwordLabel);
			passwordBox.add(passwordIconLabel);
			passwordBox.add(passwordField);
			projectsBox.add(projectsLabel);
			projectsBox.add(projectsIconLabel);
			projectsBox.add(projectsField);
			projectsBox.add(projectsSelectionDialogButton);
			// hostnameBox.add(hostnameLabel);
			// hostnameBox.add(hostnameIconLabel);
			// hostnameBox.add(hostnameField);
			portNumberBox.add(portLabel);
			portNumberBox.add(portIconLabel);
			portNumberBox.add(portField);
			// loggingBox.add(loggingLabel);
			// loggingBox.add(loggingCheckBox);
			buttonBox.add(Box.createGlue());
			buttonBox.add(saveButton);
			buttonBox.add(cancelButton);
			buttonBox.add(Box.createGlue());
			buttonBox.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
			
			// Add boxes to JFrame
			panel.add(programDirBox);
			panel.add(workingDirBox);
			panel.add(databaseBox);
			panel.add(usernameBox);
			panel.add(passwordBox);
			panel.add(projectsBox);
			// panel.add(hostnameBox);
			panel.add(portNumberBox);
			//panel.add(loggingBox);
			panel.add(buttonBox);
			
			panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
			
			getContentPane().add(panel);
			
			SwingUtilities.invokeLater( new Runnable() 
			{ 
				public void run() { 
					programDirField.requestFocusInWindow(); 
				} 
			});
			// Populate textfields 
			initSettings();
	}
	
	/**
	 * Creates labels.
	 * @param title Label text.
	 * @return created JLabel
	 */
	private JLabel createLabel(String title) {
		JLabel lbl = new JLabel(title);
		lbl.setPreferredSize(new Dimension(180, 25));
		return lbl;
	}
	
	/**
	 * Creates icon labels.
	 * @param title Label text.
	 * @return created JLabel
	 */
	private JLabel createIconLabel() {
		JLabel lbl = new JLabel("");
		lbl.setPreferredSize(new Dimension(20, 25));
		return lbl;
	}
	
	/**
	 * Creates textfield.
	 * @return textfield.
	 */
	private JTextField createTextField(){
		JTextField tf = new JTextField();
		tf.selectAll();
		tf.setEditable(true);
		return tf;
	}
	
	/**
	 * Runs the given verifier and inserts or removes error icons to/from  and tooltiptexts in given iconlabels.
	 * @param inputverifier the verifier to run
	 * @param inputField field that contains (usually) the text for verification.
	 * @param iconLabels label for setting the error icon on/off.
	 * @return true or false, the verification result.
	 */
	private boolean verifyField(SettingsVerifier verifier, JTextField inputField, JLabel iconLabel) {
   	 	int verified = verifier.verify(inputField);
 		iconLabel.setIcon(icons[verified+1]);
 		iconLabel.setToolTipText(tooltipTexts.get(iconLabel)[verified+1]);
   	 	return verified > 0;
	}
	
	/**
	 * Checks all the fields. And raises boolean value if one or more fields are not OK.
	 */
	public boolean verifyAllFields() {
		boolean allOk = true;
		if ( !verifyField(new ProgramDirectoryVerifier(), programDirField, programDirIconLabel) ) allOk = false;
		if ( !verifyField(new WorkingDirectoryVerifier(), workingDirField, workingDirIconLabel) ) allOk = false;
		if ( !verifyField(new DatabaseVerifier(workingDirField.getText()), databaseField, databaseIconLabel) ) allOk = false;
		if ( !verifyField(new UsernameVerifier(getManagerAbPath()), usernameField,  usernameIconLabel) ) allOk = false;
		if ( !verifyField(new PasswordVerifier(getManagerAbPath(), usernameField.getText()), passwordField,  passwordIconLabel) ) allOk = false;
		if ( !verifyField(new ProjectsVerifier(getManagerAbPath()), projectsField, projectsIconLabel) ) allOk = false;
		if ( !verifyField(new PortVerifier(), portField, portIconLabel) ) allOk = false;
		// if ( !verifyField(new HostnameVerifier(), hostnameField, hostnameIconLabel) ) allOk = false;
		return allOk;
	}
	
	/**
	 * Initializes settings if its null.
	 */
	private void getSettings(){
		if ( this.settings == null) this.settings = Settings.getSettings();
	}
	
	/**
	 * Initializes settings to UI textfields.
	 */
	private void initSettings(){
		this.getSettings();
			
		if ( !this.settings.isInitialized() ) return;
		
		this.programDirField.setText(this.settings.getProgramPath());
		this.workingDirField.setText(this.settings.getWorkingDirectory());
		this.databaseField.setText(this.settings.getDatabase());
		this.usernameField.setText(this.settings.getUsername());
		this.passwordField.setText(this.settings.getPassword());
		this.projectsField.setText(projectsFromArrayToLine(this.settings.getProjects()));
		// this.hostnameField.setText(this.settings.getHostname());
		this.portField.setText(String.valueOf(this.settings.getPort()));
		// this.loggingCheckBox.setSelected(this.settings.isLogging());
		verifyAllFields();
	}
   
	/**
	 * Closes the dialog.
	 */
	public void exitDialog(){
	   this.setVisible(false);
	   dialog = null;
	   this.dispose();
	}
	
	/**
	 * Separates project names from one line.
	 * @return Array of project names.
	 */
	public String [] projectsFromLineToArray(){
		return this.projectsField.getText().split(";");
	}
	
	/**
	 * 
	 * @param projectsInArray project names in array
	 * @return String containing project names separated with ";"
	 */
	public String projectsFromArrayToLine(String [] projectsInArray){
		String projects = "";
		String separator = "";
		for (String s : projectsInArray) {
			projects += separator + s;
			separator = ";";
		}
		return projects;
	}
	   
	/**
	 * Saves the settings.
	 */
	public void saveSettings(){
	   settings.setProgramPath(this.programDirField.getText());
	   settings.setWorkingDirectory(this.workingDirField.getText());
	   settings.setDatabase(this.databaseField.getText());
	   settings.setUsername(this.usernameField.getText());
	   settings.setPassword(this.passwordField.getText());
	   settings.setProjects(this.projectsField.getText().split(";"));
	   // settings.setHostname(this.hostnameField.getText());
	   settings.setPort(Integer.parseInt(this.portField.getText()));
	   // settings.setLogging(this.loggingCheckBox.isSelected());
	   settings.save();
	}
   	
   	private void setProjectsToTextField(String[] projects) {
   		this.projectsField.setText(projectsFromArrayToLine(projects));
	}
	
   	/**
   	 * Sets path for database manager.ab file by joining working directory
   	 * path and database name and adding "manager.ab" to it.
   	 */
   	public void setManagerAbPath() {
	   	File f = new File(this.workingDirField.getText() + "\\" + this.databaseField.getText() + "\\" + "manager.ab");
	   	if (f.exists()) {
		   	setManagerAbPath(f);
	   	} else {
		   	setManagerAbPath(new File(""));
	   	}
   	}
   
	   /**
		* Setter for managerAb File attribute.
		* @param managerAbPath File.
		*/
   	private void setManagerAbPath(File managerAbPath) {
	   	this.managerAbPath = managerAbPath;
   	}
   	
	   /**
		* Getter for mangager.ab file path.
		*/
   	public File getManagerAbPath() {
	   	setManagerAbPath();
	   	return managerAbPath;
   	}
   	
	/**
	 * Reads usernames, passwords and project names from manager.ab file depending on the section parameter.
	 * @param path to manager.ab file.
	 * @param section if "areas" reads the project names. If "users" reads usernames and passwords and returns
	 * them as single String separated with ';'. (eg. "root;root")
	 * @return Array containing Strings.
	 */
	private String [] readFromManagerAb(File path, String section) {
		ArrayList <String> list = new ArrayList<String>();
		Scanner scanner = null;
		String line;
		if (!path.exists()) return list.toArray(new String[list.size()]);
		try {
			scanner = new Scanner(path);
		} catch (FileNotFoundException e) { 
			e.printStackTrace();
		}
		try {
			while (scanner.hasNextLine()){
				line = scanner.nextLine();
				if (line.contains("[" + section + "]")) {
					line = scanner.nextLine();
					while (!line.startsWith("[")) {
						if (line.length() > 1) {
							if (section == "areas") list.add(parseProjectFromLine(line));
							if (section == "users") list.add(parseNameAndPasswordFromLine(line));
						}
						line = scanner.nextLine();
					}  
				}
			}
		}
		finally {
		  scanner.close();
		}
		list.removeAll(Collections.singleton(null));
		return list.toArray(new String[list.size()]);
	}
	
	/**
	 * Parses project name from manager.ab file line.
	 * @param line read from manager.ab
	 * @return project name.
	 */
	private String parseProjectFromLine(String line) {	
		String [] inValidProjects = {"Administration-Common", "Administration-System" };
		String project = line.split(";")[1];
		for (int i=0; i<inValidProjects.length; i++) {
			if (project.equalsIgnoreCase(inValidProjects[i])) return null;
		}
		return project;
	}
	
	/**
	 * Parses name and password from manager.ab file line
	 * @param line read from manager.ab [users] section.
	 * @return name and password (name;password)
	 */
	private String parseNameAndPasswordFromLine(String line) {
		String [] splitted = line.split(";");
		return splitted[1] + ";" + splitted[2];
	}

	/**
	 * Method that creates and shows the dialog.
	 * @param initalLaunch if true this dialog is shown at plugin start and
	 * therefore the "Save" button is replaced with "Open MetaEdit+" button.
	 * Prevents multiple windows to be opened at the same time.
	 */
	public static void createAndShowGUI(boolean modal) {
		if ( dialog == null) {
				String title = "MetaEdit+ Launch Parameters";
				try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				} catch (Exception e) { 
					e.printStackTrace();
				}
				dialog = new SettingsDialog(modal);
				dialog.setIconImage(getImage("icons/metaedit_logo.png"));
				dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				dialog.setTitle(title);
				dialog.pack();
				dialog.setLocationByPlatform(true);
				dialog.setResizable(false);
				dialog.setVisible(true);
				dialog.setModal(modal);
				dialog.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent we){
					dialog.setVisible(false);
					dialog = null;
					}
		});
		} else {
				java.awt.EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						dialog.toFront();
						dialog.repaint();
					}
				});

		}
	}
	
	/**
	 * Loads images from icons folder.
	 * @return loaded Image.
	 */
	public static Image getImage(String path) {
		URL url = null;
		try {
		url = new URL(Platform.getBundle(Activator.PLUGIN_ID).getEntry("/"), path);
		} catch (MalformedURLException e) {
		e.printStackTrace();
		}
		return Toolkit.getDefaultToolkit().getImage(url);
	}

	/**
	 * Checks that program directory path is correct. The verifying is made by looking
	 * the given filename and checking that it exists in the filesystem.
	 */
	public class ProgramDirectoryVerifier implements SettingsVerifier {
	
		public int verify(final JComponent input) {
			JTextField tf = (JTextField) input;
			File file = new File(tf.getText());
			if (file.exists() &&  file.getName().contains("mep") && file.getName().contains(".exe")) return 1;
			return -1;
	  	}
	}
	
	/**
	 * Verifier class for verifyin the MetaEdit+ working directory. Looks
	 * for artbase.roo file from given path.
	 */
	public class WorkingDirectoryVerifier implements SettingsVerifier {
		
		public int verify(final JComponent input) {
			JTextField tf = (JTextField) input;
			File file = new File(tf.getText());
			if (file != null && file.isDirectory()) {
				File [] files = file.listFiles();
				for (File f : files) {
					if (f.isFile() && f.getName().equals("artbase.roo")) return 1;
				}
			}
			return -1;
		}
	}
	
	/**
	 * Checks username. Reads all the usernames from the databases manager.ab file
	 * and check if the given username is in there.
	 */
	public class UsernameVerifier implements SettingsVerifier {
		
		File managerAb;
		
		public UsernameVerifier(File managerAb) {
			this.managerAb = managerAb;
		}
		
		/**
		 * Reads the user names from manager.ab file and checks if
		 * given username is in there.
		 */
		public int verify(final JComponent input) {
			if ( !verifyField(new DatabaseVerifier(workingDirField.getText()), databaseField, databaseIconLabel) ) {
				return 0;
			}
			JTextField tf = (JTextField) input;
			String [] users = readFromManagerAb(managerAb, "users");
			for (String user : users) {
				user = user.split(";")[0];
				if (tf.getText().equals(user)) return 1;
			}
			return -1;
		}
	}

	/**
	 * Check that the given password is correct (if the verifying is possible).
	 */
	public class PasswordVerifier implements SettingsVerifier {
		
		String username;
		String password;
		File managerAb;
		
		/**
		 * Checks if usernames "user" and "sysadmin" matches to their passwords.
		 * If user creates new username and password no checking can be made.
		 * @param username username for the password
		 * @param managerAb file that contains the usernames and hashes of their passwords.
		 */
		public PasswordVerifier(File managerAb, String username) {
			this.username = username;
			this.managerAb = managerAb;
		}
		
		public int verify(final JComponent input) {
			JTextField tf = (JTextField) input;
			password = tf.getText();
			String sysadmin = "sysadmin;109859928";
			String user = "user;128988713";
			String [] users = readFromManagerAb(managerAb, "users");
			if (username.equals("user") && password.equals("user")) {
				for (String s : users) {
					if ( s.equals(user)) return 1;
				}
			}
			else if (username.equals("sysadmin") && password.equals("sysadmin")) {
				for (String s : users) {
					if ( s.equals(sysadmin)) return 1;
				}
			}
			if ( username.equals("user") && !password.equals("user") ) return -1;
			if ( username.equals("sysadmin") && !password.equals("sysadmin") ) return -1;
			return 0;
		}
	}
	
	/**
	 * Verifies the database name by checking that the file manager.ab exists in path
	 * that is formed by adding together given working directory path and given database name.
	 */
	public class DatabaseVerifier implements SettingsVerifier {
		
		String workingDirPath;
		
		public DatabaseVerifier(String path) {
			this.workingDirPath = path;
		}

		public int verify(final JComponent input) {
			if (!verifyField(new WorkingDirectoryVerifier(), workingDirField, workingDirIconLabel) ) {
				return 0;
			}
			JTextField tf = (JTextField) input;
			File f = new File(workingDirPath + "\\" + tf.getText());
			if (f.isDirectory()) {
				File [] files = f.listFiles();
				for (File _f : files) {
					if (_f.getName().equals("manager.ab")) return 1;
				}
			}
			return -1;
		}
	}
	
	/**
	 * Verifies projectsfield. Looks if the given project names can be found from
	 * manager.ab file which is located in databases folder.
	 */
	public class ProjectsVerifier implements SettingsVerifier {

		File managerAb;
		
		public ProjectsVerifier(File managerAb) {
			this.managerAb = managerAb;
		}
		
		/**
		 * Reads all projects from manager.ab file and checks if given
		 * project names are in there. If working directory is not verified, returns
		 * 0 (neutral). Else 1 or -1.
		 */
		public int verify(JComponent input) {
			if ( !verifyField(new DatabaseVerifier(workingDirField.getText()), databaseField, databaseIconLabel) ) {
				return 0;
			}
			JTextField tf = (JTextField) input;
			if (tf.getText().equals("")) return 0;
			String [] projects = tf.getText().split(";");
			String [] allProjects = readFromManagerAb(managerAb, "areas");
			List<String> projectsList = new ArrayList<String>(Arrays.asList(projects));
			ArrayList<String> allProjectsList = new ArrayList<String>(Arrays.asList(allProjects));
			if (projects.length > 0) {
				for (int i = projectsList.size()-1; i >= 0; i--){
				 	if (allProjectsList.contains(projectsList.get(i))) projectsList.remove(projectsList.get(i));
				}
			}
			if (projectsList.size() > 0) return -1;
			return 1;
		}

	}
	
	/**
	 * Checks the given port number. Port number should be an integer between 0 and 65535.
	 * If possible numbers less than 1023 should not be used.
	 */
	public class PortVerifier implements SettingsVerifier {

		@Override
		public int verify(JComponent input) {
			JTextField tf = (JTextField) input; 
			int port = -1;
			try {
				 port = Integer.parseInt(tf.getText());
			} catch (NumberFormatException e) {
				return -1;
			} 
			if (0 <= port && port <= 1023) {
				return 0;
			}
			else if (1024 <= port && port <= 65535) {
				return 1;
			}
			else {
				return -1;
			}
		}

	}
	
	/**
	 * Hostname verifier. Not used.
	 */
	public class HostnameVerifier implements SettingsVerifier {

		public int verify(JComponent input) {
			//JTextField tf = (JTextField) input;
			return 1;
		}
	}
}
