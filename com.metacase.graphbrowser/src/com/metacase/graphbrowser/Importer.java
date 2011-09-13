/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.graphbrowser;

import java.io.File;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.launching.*;
import org.eclipse.debug.core.Launch;
import org.eclipse.osgi.service.datalocation.Location;

/**
 * Class that imports existing Eclipse project to workspace. The
 * MetaEdit+ generator ini file writer is implemented in this file
 * because its only used when generating and importing project.
 */
public class Importer {
	
    	IWorkspaceRoot root;
	File path;
	String appName;
	
	/**
	 *  Constuctor.
	 * @param path for the Project folder.
	 */
	public Importer(File path, String name) {
	    this.path = path;
	    this.appName = name;
	}
	
	/**
	 * Imports eclipse project to workspace and opens it. If project already exists, refreshes the project.
	 * Finally builds and runs the imported project.
	 * If no .project file found, does nothing.
	 */
	public void importProject() {
	    IProjectDescription description = null;
	    IProject project = null;
	    IProgressMonitor monitor =  new NullProgressMonitor();
	    root = ResourcesPlugin.getWorkspace().getRoot();
	    try {
	    	//description = ResourcesPlugin.getWorkspace().loadProjectDescription(new Path(this.path + "\\.project"));
		description = ResourcesPlugin.getWorkspace().loadProjectDescription(new Path(root.getLocation().toString() + "/" + appName + "/.project"));
	    	project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
	    	if (project.exists()) {
	    		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	    	}
	    	else {
	    		project.create(description, monitor);
	    	}
	    	project.open(IResource.PROJECT, monitor);
	    	// Build the project.
	    	project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
	    	// Cast project to IJavaProject and run it.
	    	IJavaProject iproject = JavaCore.create(project);
	    	IVMInstall vm = JavaRuntime.getVMInstall(iproject);
	    	if (vm == null) vm = JavaRuntime.getDefaultVMInstall();
	    	IVMRunner vmr = vm.getVMRunner(ILaunchManager.RUN_MODE);
	    	String[] cp = JavaRuntime.computeDefaultRuntimeClassPath(iproject);
	    	VMRunnerConfiguration config = new VMRunnerConfiguration("_" + appName, cp);
	    	ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
	    	vmr.run(config, launch, monitor);
	    	}
	    catch (CoreException e) { 	
		// 	No need to react because MetaEdit+ won't generate Eclipse project every time.
	    }	    
	}

	/**
	 * Writes the plugin.ini file that is written for MetaEdit+ generator. 
	 * The file contains information for the generator.
	 */
	public String writePluginIniFile() {
        	Location installLoc = Platform.getInstallLocation();
        	String path = installLoc.getURL().getFile();
        	path = path.substring(1, path.length()) + "eclipse.exe";
        	root = ResourcesPlugin.getWorkspace().getRoot(); 
        	
        	// set the ini file
        	String _path = this.path + "\\plugin.ini";
        	IniHandler writer = new IniHandler(_path);
        	writer.flushValues();
        	// the eclipse flag
        	writer.AddSetting("IDE", "eclipse");
        	// workspace path
        	writer.AddSetting("workspace", new File(root.getLocation().toString()).toString());
        	writer.SaveSettings();
        	return _path;
	}
	
	/**
	 * Removes the written ini file.
	 */
	public void removeIniFile() {
	    this.path.delete();
	}
}