package com.metacase.graphbrowser;

public class MEVersion {

	public String major;
	public String minor;
	
	public MEVersion(String versionString)
	{
		this.setVersion(versionString);
	}
	
	public MEVersion getVersion() {
		return new MEVersion("0.0");
	}
	
	public void setVersion(String versionString) {
		String[] tokens = versionString.split("\\.");
		this.major = tokens[0];
		this.minor = tokens[1];
	}
	
	public void setVersion(float versionNumber) {
		this.setVersion(Float.toString(versionNumber));
	}
	
	public String getMajor() {
		return this.major;
	}

	public String getMinor() {
		return this.minor;
	}

    public String versionString()
    {
        return this.major + "." + this.minor;
    }

    public String shortVersionString()
    {
        return this.major + this.minor;
    }

    public boolean isEqualWith(MEVersion version)
    {
        return (this.major.equals(version.getMajor()) && this.minor.equals(version.getMinor()));
    }

    public boolean isGreaterThan(MEVersion version)
    {
        if(Integer.parseInt(this.major) > Integer.parseInt(version.getMajor())) return true;
        if(Integer.parseInt(this.minor) > Integer.parseInt(version.getMinor())) return true;
        return false;
    }

    public boolean isEqualOrGreaterThan(MEVersion version)
    {
        return (this.isEqualWith(version) || this.isGreaterThan(version));
    }

    public boolean isEqualOrGreaterThan(String versionString)
    {
        MEVersion tempVersion = new MEVersion(versionString);
        return this.isEqualOrGreaterThan(tempVersion);
    }	
}
