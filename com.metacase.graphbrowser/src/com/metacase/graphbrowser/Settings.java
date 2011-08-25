/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.graphbrowser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import javax.swing.JFileChooser;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

public class Settings {

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
	private boolean is50;
	
	/**
	 * <p>
	 * Settings class that contains the settings properties and methods for reading and
	 * writing the to file. If no file is found, the values are "calculated".
	 * </p>
	 */
	private Settings() {
		setMerFile("default.mer");
		if (!checkIfMerExists()) {
			this.calculateValues();
			this.setInitialized(true);
		} else {
			this.readFromConfigFile();
			this.setInitialized(true);
		}
	}
	
	public static Settings getSettings() {
		if (singleton == null) singleton = new Settings();
		return singleton;
	}
	
	/*
	Getters and setters for all properties.
	*/
	public void setProgramPath(String programPath) {
		this.programPath = programPath;
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
		return is50;
	}
	public void setIs50(boolean is50) {
		this.is50 = is50;
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
			Properties properties = new Properties();
			if (this.getProgramPath().contains("50")) this.setIs50(true);
			else this.setIs50(false);
			properties.setProperty("metaEditDir", this.getProgramPath());
			properties.setProperty("workingDir", this.getWorkingDirectory());
			properties.setProperty("database", this.getDatabase());
			properties.setProperty("username", this.getUsername());
			properties.setProperty("password", this.getPassword());
			String projects = "";
			String separator = "";
			for (String s : this.getProjects()) {
				projects += separator + s;
				separator = ";";
			}
			properties.setProperty("projects", projects);
			properties.setProperty("hostname", this.getHostname());
			properties.setProperty("port", String.valueOf(this.getPort()));
			properties.setProperty("logging", String.valueOf(this.isLogging()));
			this.writeToFile(properties, comment);
			
	    }
	
	/**
	 * Method for IO actions when writing new properties to file.
	 * @param newProperties new properties as key-value -pairs in HashTable
	 * @param comment comment to be written in configurations file.
	 */
	private void writeToFile(Properties newProperties, String comment){
		try {
			if (!checkIfMerExists()) createEmptyMerFile();
			Properties oldProperties = new Properties();
            // First read all the existing properties
            FileInputStream configFileIs = new FileInputStream(this.getMerFile());
            oldProperties.load(configFileIs);
            configFileIs.close();
            
            FileOutputStream configFileOs = new FileOutputStream(this.getMerFile());
            // Merge existing and new properties
            oldProperties.putAll(newProperties);
            
            // Write properties to configuration file
            newProperties.store(configFileOs, comment);
            configFileOs.close();
        } catch (Exception ex) {
        	DialogProvider.showMessageDialog("Error writing the configurations: " + ex.getMessage(),
			"Error writing the configurations");
        }
	}
	
	/**
	 * <p>
	 * Method for reading the configuration file.
	 * </p>
	 * <p>
	 * Properties with same key are stored in the XML file
	 * as a single String with ';' separator. These are separated and read to list which is then added as
	 * value in the hashtable.
	 * </p>
	 * <p>
	 * Saves the values to attributes.
	 * </p>
	 */
	private void readFromConfigFile(){
		Properties properties = new Properties();
		Hashtable<String, String> propertyTable = new Hashtable<String, String>();
		String key;
		String value;
		try {
			FileInputStream is = new FileInputStream(this.getMerFile());
			properties.load(is);
		} catch (Exception ex) { 
			DialogProvider.showMessageDialog("Error reading the configurations: " + ex.getMessage(),
					"Error reading the configurations");
		}
		Enumeration<?> e = properties.keys();
		while (e.hasMoreElements()) {
			key = (String) e.nextElement();
			value = (String) properties.getProperty(key);
			propertyTable.put(key, value);
			key = value = "";
		}
		if (propertyTable.get("metaEditDir").contains("50")) this.setIs50(true);
		else this.setIs50(false);
		this.setProgramPath(propertyTable.get("metaEditDir"));
		this.setWorkingDirectory(propertyTable.get("workingDir"));
		this.setDatabase(propertyTable.get("database"));
		this.setUsername(propertyTable.get("username"));
		this.setPassword(propertyTable.get("password"));
		this.setProjects(propertyTable.get("projects").split(";"));
		this.setHostname(propertyTable.get("hostname"));
		this.setPort(Integer.valueOf(propertyTable.get("port")));
		this.setLogging(propertyTable.get("logging").equals("true"));
	}
	
	/**
	 * Creates new empty default.mer file.
	 */
	public void createEmptyMerFile(){
		try {
			merFile.createNewFile();
		} catch (IOException e) { }
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
	    
	    else if (!f.exists()) {
	    	// Try with MetaEdit+ 4.5
	    	f = new File(tempProgramDir + "\\MetaEdit+ 4.5\\mep45.exe"); 
	    	// No MetaEdit+ 5.0 found, make the working directory for version 4.5
	    	this.workingDirectory = new JFileChooser().getFileSystemView().getDefaultDirectory() + "\\MetaEdit+ 4.5";
	    }
	    
	    // if no mep45.exe found it MUST be the evaluation version ;)
	    else if (!f.exists()) {
	    	f = new File(tempProgramDir + "\\MetaEdit+ 4.5 Evaluation\\mep45eval.exe");
	    }
	    this.programPath = f.getPath();
	}
}
