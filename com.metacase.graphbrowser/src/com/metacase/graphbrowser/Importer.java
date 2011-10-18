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

/**
 * Class that imports existing Eclipse project to workspace. The
 * MetaEdit+ generator ini file writer is implemented in this file
 * because its only used when generating and importing project.
 */
public class Importer {
	
	/**
	 * Imports eclipse project to workspace and opens it. If project already exists, refreshes the project.
	 * Finally builds and runs the imported project.
	 * If no .project file found, does nothing.
	 * @param applicationName name of the project that should be imported. Used for compiling and running the project.
	 */
	public static void importAndExecuteProject(String applicationName) {
	    IProjectDescription description = null;
	    IProject project = null;
	    IProgressMonitor monitor =  new NullProgressMonitor();
	    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	    try {
	    	description = ResourcesPlugin.getWorkspace().loadProjectDescription(new Path(root.getLocation().toString() + "/" + applicationName + "/.project"));
	    	project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
	    	if (project.exists()) {
	    		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	    	} else {
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
	    	VMRunnerConfiguration config = new VMRunnerConfiguration("_" + applicationName, cp);
	    	ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
	    	vmr.run(config, launch, monitor);
	    	}
	    catch (CoreException e) { 	
		// MetaEdit+ doesn't generate Eclipse project every time.
	    }	    
	}

	/**
	 * Writes the plugin.ini file that is written for MetaEdit+ generator. 
	 * The file contains information for the generator.
	 */
	public static String writePluginIniFile(String path, String generatorName) {
	    	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot(); 
        	// set the ini file
        	path = path + "\\plugin.ini";
        	File _temp = new File(path);
        	if (_temp.exists()) _temp.deleteOnExit();
        	IniHandler writer = new IniHandler(path);
        	writer.flushValues();
        	writer.AddSetting("IDE", "eclipse");
        	// workspace path
        	writer.AddSetting("workspace", new File(root.getLocation().toString()).toString());
        	// If we are running Autobuild generator mark to the file that the generated source
        	// code should be compiled and run in Eclipse.
        	String _runValue = generatorName.equalsIgnoreCase("autobuild") ? "true" : "false";
        	writer.AddSetting("runGenerated", _runValue);
        	writer.SaveSettings();
        	// Return the path of written ini file so that it can be read later.  
        	return path;
	}
	
	/**
	 * Removes the written ini file.
	 */
	public static void removeIniFile(File path) {
	    path.delete();
	}
}