/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.graphbrowser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import com.metacase.API.*;

/**
 * Launcher class that runs MetaEdit. Contains methods for
 * creating the run command with particular parameters and
 * for checking the API connection. 
 *
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
		} catch (ServiceException e) { }
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
	public static void doInitialLaunch() {
		if (getSettings().checkIfMerExists() || isApiOK()) initializeAPI();
		else {
			DialogProvider.showSettingsDialog(true);
		}
	}
	
	/**
	 * Initializes API connection by checking if it's available and asking user
	 * if MetaEdit+ should be launched. 
	 * @return true if ME launched successfully else false.
	 */
	public static boolean initializeAPI(){
		if (!isApiOK()) {
			int maxWaitMs = 500;
			String message = "MetaEdit+ API server not found." +
					"\n\nClick Yes to start new MetaEdit+ instance or No if you will start API server manually.\n\n";
			String title = "API not found";
			if ( DialogProvider.showYesNoMessageDialog(message, title)) {
				if (launchMetaEdit()) {
					maxWaitMs = 2500;
				}
			}
			else DialogProvider.showMessageDialog("Start MetaEdit+ API and click OK to proceed.", "Start MetaEdit+");
			poll(maxWaitMs);
			if (!isInitialized) DialogProvider.showMessageDialog("MetaEdit+ API server not found.", "API not found");
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
			} catch (InterruptedException e) { }
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
			name = ""; }
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
			DialogProvider.showMessageDialog("Could not start MetaEdit+: " + e.getMessage() + "\nPlease start MetaEdit+ API and click OK to proceed", "Launch error");
		}
		return false;
	}
	
	/**
	 * Reads the launch parameters and forms the entire launch command that runs MetaEdit+,
	 * opens one or more projects and starts API server.
	 * @return command that can be executed.
	 */
	private static String createLaunchParameters(){
		String metaEditDir = "\"" + getSettings().getProgramPath() + "\"";
		String workingDir = "\"" + getSettings().getWorkingDirectory() + "\"";
		String db = "\"" + getSettings().getDatabase() + "\"";
		String user = "\"" + getSettings().getUsername() + "\"";
		String password = "\"" + getSettings().getPassword() + "\"";
		int port = getSettings().getPort();
		String hostname = getSettings().getHostname();
		boolean logging = getSettings().isLogging();
		
		String line = metaEditDir + " currentDir: " + workingDir + " " + "loginDB:user:password: " + db + " " + 
		user + " " + password; 
		
		String [] projects = getSettings().getProjects();
			for (String s : projects) {
				if (!s.equals("")) {
					line += " setProject: " + "\"" + s + "\"";
				}
			}
				
		line += " startAPIHostname:port:logEvents: " + hostname + " " + port + " " + logging;
		return line;
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

	public static Settings getSettings() {
		return Settings.getSettings();
	}
}
