package com.metacase.graphbrowser;

import java.io.*;
import java.util.*;

public class IniHandler {
	
    private HashMap<String, String> values = new HashMap<String, String>();
    private String iniFilePath;

    /**
     * Constructor. Reads data from ini file.
     * @param _iniPath path to ini file.
     */
    public IniHandler(String _iniPath) {
        FileInputStream fis = null;
        String strLine = null;
        String[] keyPair = null;
        this.iniFilePath = _iniPath;
        File iniFile = new File(this.iniFilePath);
        
        if (iniFile.exists()) {
            try {
	            fis = new FileInputStream(iniFile);
	        	DataInputStream in = new DataInputStream(fis);
	        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
	                while ((strLine = br.readLine()) != null) {
	                    strLine = strLine.trim();
	                    if (!strLine.isEmpty() && !strLine.startsWith("#")) {
	                    	if (strLine.endsWith("=")) {
	                    		// rows with key have only null values.
	                    		keyPair = new String [] {strLine.substring(0, strLine.length()-1), ""};
	                    	} else {
	                    		keyPair = strLine.split("=");
	                    	}
	                        values.put(keyPair[0], keyPair[1]);
	                    }
	                }
            } catch (Exception e) {    
            	e.printStackTrace();
            }
            finally {
                if (fis != null) {
                	try {
                		fis.close();
                	} catch (IOException e) {
                		e.printStackTrace();
                	}
                }
            }
        }
    }
    
    /**
     * Removes old values from the inifile.
     */
    public void flushValues() {
    	this.values = new HashMap<String, String>();
    }


    /**
     * Returns the value for the given section, key pair.
     * @param settingName key for the value
     * @return value
     */
    public String getSetting(String settingName) {
        return (String)values.get(settingName);
    }

    /**
     * Adds or replaces a setting to the table to be saved.
     * @param settingName key for the value
     * @param settingValue value
     */
    public void addSetting(String settingName, String settingValue) {
        if (values.containsKey(settingName))
            values.remove(settingName);

        values.put(settingName, settingValue);
    }

    /**
     * Adds or replaces a setting to the table to be saved with a null value.
     * @param settingName key for the value
     */ 
    public void addSetting(String settingName) {
        addSetting(settingName, null);
    }

    /**
     * Removes setting from the file
     * @param settingName key for the value that will be removed.
     */
    public void deleteSetting(String settingName) {
        if (values.containsKey(settingName))
            values.remove(settingName);
    }

    /**
     * Save settings to new file.
     * @param newFilePath path to the new file
     */
    public void saveSettings(String newFilePath) {
        String tmpValue = "";
        String strToSave = "";
        Set<String> e = values.keySet();
        for (String key : e) {
            tmpValue = (String) values.get(key);
            if (tmpValue != null)
                tmpValue = "=" + tmpValue;
            strToSave += (key + tmpValue + "\r\n");
        }
        strToSave += "\r\n";
        
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(newFilePath));
            out.write(strToSave);
	    	out.close();
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
    }

    /**
     * Save settings back to ini file.
     */
    public void saveSettings() {
        saveSettings(iniFilePath);
    }
}