package com.metacase.graphbrowser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MEVersion {

	public String major;
	public String minor;
	public boolean isEvaluation;
	public boolean isClient;
	
	public MEVersion()
	{
		this.major = "0";
		this.minor = "0";
		this.isEvaluation = false;
		this.isClient = false;
	}
	
	public MEVersion getVersion() {
		return new MEVersion();
	}
	
	public String getMajor() {
		return this.major;
	}

	public String getMinor() {
		return this.minor;
	}

	public boolean isEvaluation() {
		return this.isEvaluation;
	}
	
	public boolean isClient() {
		return this.isClient;
	}

	public void setValuesFromPath(String path) {
		String versionString = path.substring((path.indexOf("MetaEdit+") + 10));
        String[] tokens = versionString.split("[\\\\.\\s+]+");
		this.major = tokens[0];
		this.minor = tokens[1];
        this.isEvaluation = path.contains("Evaluation");
        this.isClient = path.contains("Client");
	}
	
	public void setValuesFromLinuxPath(String path) {
		String versionString = path.substring((path.indexOf("mep") + 3));
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(versionString);
		m.find();
		String versionNumber = m.group();
		this.major = versionNumber.substring(0, 1);
		this.minor = versionNumber.substring(1);
        this.isEvaluation = path.contains("eval");
        this.isClient = path.contains("client");		
	}	

	public String versionString()
    {
		String versionString = this.major + "." + this.minor;
		if(this.isEvaluation) {
			versionString = versionString + " Evaluation"; 
		}
		if(this.isClient) {
			versionString = versionString + " Client";
		}
        return versionString;
    }
	
    public String versionNumberString()
    {
        return this.major + "." + this.minor;
    }

    public String shortVersionString()
    {
        return this.major + this.minor;
    }

    public String winProgramName()
    {
    	String name = "mep" + this.shortVersionString();
    	if(this.isEvaluation) {
    		name = name + "eval";
    	}
    	if(this.isClient) {
    		name = name + "m";
    	}
    	return name + ".exe";
    }
    
    public String osxProgramName()
    {
    	return "MetaEdit+ " + this.versionString() + ".app";
    }    

    public String linuxProgramName()
    {
    	String name = "mep" + this.shortVersionString();    	
    	if(this.isEvaluation) {
    		name = name + "eval";
    	}
    	if(this.isClient) {
    		name = name + "m";
    	}
    	return name + "/metaedit";
    }
    
    public boolean isSuperiorTo(MEVersion version)
    {
        if(Integer.parseInt(this.major) > Integer.parseInt(version.getMajor())) return true;
        if(Integer.parseInt(this.minor) > Integer.parseInt(version.getMinor())) return true;
    	if(!this.isEvaluation && (version.isEvaluation() || version.isClient())) return true;
    	if(this.isEvaluation && version.isClient()) return true;
    	return false;
    }
    
    public boolean isAtLeast(String versionString) {
    	String[] tokens = versionString.split("\\.");
        if(Integer.parseInt(this.major) < Integer.parseInt(tokens[0])) return false;
        if(Integer.parseInt(this.minor) < Integer.parseInt(tokens[1])) return false;
        return true;        
    }
}
