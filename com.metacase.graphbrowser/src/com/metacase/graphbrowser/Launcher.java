/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.graphbrowser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisProperties;

import com.metacase.API.*;

/**
 * Launcher class that runs MetaEdit. Contains methods for
 * creating the run command with particular parameters and
 * for checking the API connection. 
 */
public class Launcher {
	
	private static MetaEditAPI service = new MetaEditAPILocator();
	private static MetaEditAPIPortType port = apiPort();
	private static boolean needStopAPI = false;
	private static boolean isInitialized = false;

	/**
	 * Port initializer
	 * @return created MetaEditAPIPortType instance
	 */
	private static MetaEditAPIPortType apiPort() {
	    java.net.URL address = null;
	    try {
			try {
				address = new URL("http://"+ getSettings().getHostname() +
					":"+ getSettings().getPort() +"/MetaEditAPI");
			} catch (MalformedURLException e) {
			    e.printStackTrace();
			}
			return service.getMetaEditAPIPort(address);
	    } catch (ServiceException e) { 
	    	e.printStackTrace();
	    }
	    return null;
	}
	
	/**
	 * Setter for port variable.
	 * @param port MetaEditAPIPortType instance
	 */
	public static void setPort(MetaEditAPIPortType port) {
	    Launcher.port = port;
	}

	/**
	 * Getter for port variable.
	 * @return MetaEditAPIPortType instance.
	 */
	public static MetaEditAPIPortType getPort() {
	    return port;
	}

	/**
	 * Launcher method for doing initialization launch.
	 */
	public static boolean doInitialLaunch() {
		AxisProperties.setProperty("http.nonProxyHost", "localhost");
	    if (getSettings().checkIfMerExists() || isApiOK()) {
	    	initializeAPI();
	    }
	    else {
	    	DialogProvider.showSettingsDialog(false);
	    }
	    return isApiOK();
	}
	
	/**
	 * Initializes API connection by checking if it's available launching MetaEdit+ if 
	 * no connection found.
	 * @return true if MetaEdit+ was launched successfully else false.
	 */
	public static boolean initializeAPI() {
	    if (!isApiOK()) {
		int maxWaitMs = 500;
		if (launchMetaEdit()) {
		    maxWaitMs = 2500;
		    poll(maxWaitMs);
		}
	    }
	    return isInitialized;
	}
	
	/**
	 * Polls MetaEdit+ until connection is OK or the time is up.
	 * @param maxWaitMs maximum wait time in milliseconds.
	 */
	public static void poll(int maxWaitMs) {
	    int totalWaitMs = 0;
	    int waitMs = 500;
	    while (!isApiOK() && ((totalWaitMs += waitMs) <= maxWaitMs )) {
		try {
		    Thread.sleep(waitMs);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}
	
	/**
	 * Method for checking if API is running. 
	 * Simple calls MetaEdit+ and check if it answers.
	 * @return true if API is ok, otherwise false.
	 */
	public static boolean isApiOK() {
	    String name;
	    METype metype = new METype();
	    metype.setName("Graph");
	    try {
		name = getPort().typeName(metype);
	    } catch (RemoteException e) {
		name = ""; 
	    }
	    isInitialized = name.equals("Graph");
	    return isInitialized;
	}

	/**
	 * Launches MetaEdit+.
	 * @return true or false.
	 */
	public static boolean launchMetaEdit(){
	    Runtime rt = Runtime.getRuntime();
	    try {
		needStopAPI = true;
		rt.exec(createLaunchParameters());
		return true;
	    } catch (IOException e) { 
		DialogProvider.showMessageDialog("Could not start MetaEdit+: " + e.getMessage(), "Launch error");
	    }
	    return false;
	}
	
	/**
	 * Reads the launch parameters and forms the entire launch command that runs MetaEdit+,
	 * opens one or more projects and starts API server.
	 * @return command that can be executed.
	 */
	private static String[] createLaunchParameters(){
	    ArrayList<String> cmdLine = new ArrayList<String>();
	    String executable = getSettings().getProgramPath();
	    if (System.getProperty("os.name").contains("OS X")) {
	    	executable += File.separator + "Contents" + File.separator + "Resources" + File.separator + "script";
	    }
	    cmdLine.add(executable);
	    
	    cmdLine.add("currentDir:");
	    cmdLine.add(getSettings().getWorkingDirectory());
	    
	    // Any existing fileInPatches, e.g. in Linux & Mac OS X scripts, will be done in the wrong dir, so repeat
	    cmdLine.add("fileInPatches");
	    
	    cmdLine.add("loginDB:user:password:");
	    cmdLine.add(getSettings().getDatabase());
	    cmdLine.add(getSettings().getUsername());
	    cmdLine.add(getSettings().getPassword());
		
	    String [] projects = getSettings().getProjects();
	    for (String s : projects) {
	    	if (!s.equals("")) {
	    		cmdLine.add("setProject:");
	    		cmdLine.add(s);
	    	}
	    }
				
	    cmdLine.add("startAPIHostname:port:logEvents:");
	    cmdLine.add(getSettings().getHostname());
	    cmdLine.add(String.valueOf(getSettings().getPort()));
	    cmdLine.add(String.valueOf(getSettings().isLogging()));
	    
	    return cmdLine.toArray(new String[0]);
	}

	/**
	 * Stops api.
	 */
	public static void stopApi() {
	    if (needStopAPI) {
		MENull menull = new MENull();
		try {
		    getPort().stopAPI(menull);
		    needStopAPI = false;
		} catch (RemoteException e) { }
	    }
	}
	
	/**
	 * Getter for instance of Settings class
	 * @return instance of Settings class
	 */
	public static Settings getSettings() {
	    return Settings.getSettings();
	}
}
