/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.graphbrowser;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Observable; 
import javax.swing.JFileChooser;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * <p>
 * Settings class that contains the settings properties and methods for reading and
 * writing the to file. If no file is found, the values are "calculated".
 * </p>
 */
public class Settings extends Observable {
 
	private String programPath;
	private String workingDirectory;
	private String database;
	private String username;
	private String password;
	private String [] projects;
	private String hostname;
	private int port;
	private boolean logging;
	private boolean initialized;
	private File merFile;
	private static Settings singleton;

	/**
	 * Constructor. Reads or creates the setting values.
	 */
	private Settings() {
	    setMerFile("default.mer");
	    if (!checkIfMerExists()) {
		this.calculateValues();
	    } else {
		this.readFromConfigFile();
	    }
	    this.setInitialized(true);
	}
	
	/**
	 * Getter for the Settings instance. Returns previously created if exists.
	 * Else, creates a new one.
	 * 
	 * @return the Settings instance
	 */
	public static Settings getSettings() {
	    if (singleton == null) singleton = new Settings();
	    return singleton;
	}
	
	/*
	 * Getters and setters for all properties.
	 */
	public void setProgramPath(String programPath) {
	    this.programPath = programPath;
	    // Notify toolbar buttons (=listeners).
	    setChanged();
	    notifyObservers();
	}
	public String getProgramPath() {
	    return programPath;
	}
	public void setWorkingDirectory(String workingDirectory) {
	    this.workingDirectory = workingDirectory;
	}
	public String getWorkingDirectory() {
	    return workingDirectory;
	}
	public void setDatabase(String database) {
	    this.database = database;
	}
	public String getDatabase() {
	    return database;
	}
	public void setUsername(String username) {
	    this.username = username;
	}
	public String getUsername() {
	    return username;
	}
	public void setPassword(String password) {
	    this.password = password;
	}
	public String getPassword() {
	    return password;
	}
	public void setProjects(String [] projects) {
	    this.projects = projects;
	}
	public String [] getProjects() {
	    return projects;
	}
	public void setHostname(String hostname) {
	    this.hostname = hostname;
	}
	public String getHostname() {
	    return hostname;
	}
	public void setPort(int port) {
	    this.port = port;
	}
	public int getPort() {
	    return port;
	}
	public void setLogging(boolean logging) {
	    this.logging = logging;
	}
	public boolean isLogging() {
	    return logging;
	}
	public void setInitialized(boolean initialized) {
	    this.initialized = initialized;
	}
	public boolean isInitialized() {
	    return initialized;
	}
	public void setMerFile(File f) {
	    merFile = f;
	}
	public boolean getIs50() {
	    return this.programPath.contains("50");
	}
	
	/**
	 * <p>
	 * Sets to the merfile attribute a file that has full path.
	 * </p>
	 * @param filename The name of the file.
	 */
	private void setMerFile(String filename) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();  
	    File workSpaceDirectory = workspace.getRoot().getLocation().toFile();
	    merFile = new File(workSpaceDirectory.toString() + "\\" + filename);
	}
	
	public File getMerFile() {
		if (merFile == null) setMerFile("default.mer");
		return merFile;
	}
	
	/**
	 * Method stub
	 * @param propertiesTable
	 */
	private void writeConfigFile() {
		this.writeConfigFile("This is a simple configuration file for MetaEdit+ plugin for Eclipse." +
				" It contains launch parameters that are used when launching MetaEdit+.");
	}
	
	/**
	 * Method for writing properties 
	 * @param comment comment to be written in configurations file.
	 */
	public void writeConfigFile(String comment) {
	    IniHandler writer = new IniHandler(this.getMerFile().toString());
	    writer.addSetting("metaEditDir", this.getProgramPath());
	    writer.addSetting("workingDir", this.getWorkingDirectory());
	    writer.addSetting("database", this.getDatabase());
	    writer.addSetting("username", this.getUsername());
	    writer.addSetting("password", this.getPassword());
	    // Write all projects to StringBuilder with separator.
	    StringBuilder sb1 = new StringBuilder();
	    for (String s : this.getProjects()) {
			sb1.append(s);
			sb1.append(";");
	    }
	    // delete the last separator
	    sb1.deleteCharAt(sb1.length()-1);

	    writer.addSetting("projects", sb1.toString());
	    writer.addSetting("hostname", this.getHostname());
	    writer.addSetting("port", String.valueOf(this.getPort()));
	    writer.addSetting("logging", String.valueOf(this.isLogging()));
	    writer.saveSettings();
	}
	
	/**
	 * <p>
	 * Method for reading the configuration file.
	 * Saves the values to attributes.
	 * </p>
	 */
	private void readFromConfigFile() {
	    IniHandler reader = new IniHandler(this.getMerFile().toString());	
	    this.setProgramPath(new File(reader.getSetting("metaEditDir")).toString());
	    this.setWorkingDirectory(new File(reader.getSetting("workingDir")).toString());
	    this.setDatabase(reader.getSetting("database"));
	    this.setUsername(reader.getSetting("username"));
	    this.setPassword(reader.getSetting("password"));
	    this.setProjects(reader.getSetting("projects").split(";"));
	    this.setHostname(reader.getSetting("hostname"));
	    this.setPort(Integer.valueOf(reader.getSetting("port")));
	    this.setLogging(reader.getSetting("logging").equals("true"));
	}
	
	/**
	 * Creates new empty default.mer file.
	 */
	public void createEmptyMerFile(){
	    try {
	    	merFile.createNewFile();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	/**
	 * Checks if (default).mer file exists in workspace root folder.
	 * @return true if file found. Else false.
	 */
	public boolean checkIfMerExists() {
	    if (merFile == null) return false;
	    return merFile.exists();
	}

	/**
	 * Saves the properties to file.
	 */
	public void save() {
	    this.writeConfigFile();
	}
	
	/**
	 * <p>
	 * Calculates default values for settings dialog if no .mer file is found.
	 * Searches for username and program files (x86) folder from environment variable
	 * to form the MetaEdit+ exe file path and working directory as they were when default
	 * installation is made.
	 * </p> 
	 * <p>
	 * Works only for Windows.
	 * </p>
	 */
	public void calculateValues() {
	    boolean x86 = false;
	    this.database = "demo";
	    this.username = "user";
	    this.password = "user";
	    this.projects = new String [] { "Digital Watch" };
	    this.port = 6390;
	    this.hostname = "localhost";
	    this.logging = false;
		
	    String tempProgramDir = "";
		
	    Map<String, String> variables = System.getenv();  
	    // Search for Program File (x86) folder from env. varible.
	    for (Map.Entry<String, String> entry : variables.entrySet())  
	    {  
	       String name = entry.getKey();  
	       if (name.contains("ProgramFiles(x86)")) x86 = true; 
	    }
	    if (x86) tempProgramDir = variables.get("ProgramFiles(x86)");
	    else tempProgramDir = variables.get("ProgramFiles");
	    
	    File f = new File(tempProgramDir + "\\MetaEdit+ 5.0\\mep50.exe");
	    this.workingDirectory = new JFileChooser().getFileSystemView().getDefaultDirectory() + "\\MetaEdit+ 5.0";
	    
	    if (!f.exists()) {
	    	// Try with MetaEdit+ 5.0 evaluation version.
	    	f = new File(tempProgramDir + "\\MetaEdit+ 5.0\\mep50eval.exe");
	    }
	    
	    if (!f.exists()) {
	    	// Try with MetaEdit+ 4.5
	    	f = new File(tempProgramDir + "\\MetaEdit+ 4.5\\mep45.exe"); 
	    	// No MetaEdit+ 5.0 found, make the working directory for version 4.5
	    	this.workingDirectory = new JFileChooser().getFileSystemView().getDefaultDirectory() + "\\MetaEdit+ 4.5";
	    }
	    
	    // if no mep45.exe found it MUST be the 4.5 evaluation version ;)
	    if (!f.exists()) {
	    	f = new File(tempProgramDir + "\\MetaEdit+ 4.5 Evaluation\\mep45eval.exe");
	    }
	    this.programPath = f.getPath();
	}
}
