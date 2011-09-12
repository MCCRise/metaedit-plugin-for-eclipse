package com.metacase.graphbrowser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;

public class IniHandler
{
    private Hashtable<String, String> values = new Hashtable<String, String>();
    private String key;
    private String iniFilePath;

    public IniHandler(String _iniPath)
    {
        FileInputStream fis = null;
        String strLine = null;
        String[] keyPair = null;
        File iniPath = new File(_iniPath);
        this.iniFilePath = _iniPath;
        
        if (iniPath.exists())
        {
            try
            {
                fis = new FileInputStream(iniPath);
        	DataInputStream in = new DataInputStream(fis);
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
                while ((strLine = br.readLine()) != null)
                {
                    strLine = strLine.trim();
                    if (strLine != "" && !strLine.startsWith("#"))
                    {
                        keyPair = strLine.split("=");
                        values.put(keyPair[0], keyPair[1]);
                        
                    }
                }
            } catch (Exception e) {    }
            finally
            {
                if (fis != null)
		    try {
			fis.close();
		    } catch (IOException e) {	    }
            }
        }
    }
    
    /**
     * Removes old values from the inifile.
     */
    public void flushValues() {
	this.values = new Hashtable<String, String>();
    }


    /**
     * Returns the value for the given section, key pair.
     * @param settingName key for the value
     * @return value
     */
    public String GetSetting(String settingName)
    {
        return (String)values.get(settingName);
    }

    /**
     * Adds or replaces a setting to the table to be saved.
     * @param settingName key for the value
     * @param settingValue value
     */
    public void AddSetting(String settingName, String settingValue)
    {
        if (values.containsKey(settingName))
            values.remove(settingName);

        values.put(settingName, settingValue);
    }

    /**
     * Adds or replaces a setting to the table to be saved with a null value.
     * @param settingName key for the value
     */
    public void AddSetting(String settingName)
    {
        AddSetting(settingName, null);
    }

    /**
     * Removes setting from the file
     * @param settingName key for the value that will be removed.
     */
    public void DeleteSetting(String settingName)
    {
        if (values.containsKey(settingName))
            values.remove(settingName);
    }

    /**
     * Save settings to new file.
     * @param newFilePath path to the new file
     */
    public void SaveSettings(String newFilePath)
    {
        String tmpValue = "";
        String strToSave = "";
        Enumeration<String> e = values.keys();
        while(e.hasMoreElements())
        {
            key = e.nextElement();
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
	} catch (IOException e1) {	}
    }

    /**
     * Save settings back to ini file.
     */
    public void SaveSettings()
    {
        SaveSettings(iniFilePath);
    }
}