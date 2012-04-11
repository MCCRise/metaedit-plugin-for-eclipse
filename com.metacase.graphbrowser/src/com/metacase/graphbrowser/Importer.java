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
 * MetaEdit+ generator INI file writer is implemented in this file
 * because its only used when generating and importing project.
 */
public class Importer {
	
	/**
	 * Imports eclipse project to workspace and opens it. If project already exists, refreshes the project.
	 * Finally builds the project, and runs it if classToLaunch is not empty.
	 * If no .project file found, does nothing.
	 *  
	 * @param projectName Name of the project that should be imported. Used for compiling and running the project.
	 * @param classToLaunch Name of the class that is launched. If the parameter is empty launch nothing. 
	 * 		  In case of null parameter try to launch class named after the project name.
	 */	
	public static void importAndExecute(String projectName, String classToLaunch) {
	    IProjectDescription description = null;
	    IProject project = null;
	    IProgressMonitor monitor = new NullProgressMonitor();
	    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	    try {
	    	description = ResourcesPlugin.getWorkspace().loadProjectDescription(new Path(root.getLocation().toString() + "/" + projectName + "/.project"));
	    	project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
	    	if (project.exists()) {
	    		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	    	} else {
	    		project.create(description, monitor);
	    	}
	    	project.open(IResource.PROJECT, monitor);
	    	// Build the project.
	    	project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
	    	if (!classToLaunch.isEmpty()) {
	    		
		    	// Cast project to IJavaProject and run it.
		    	IJavaProject iproject = JavaCore.create(project);
	
		    	IVMInstall vm = JavaRuntime.getVMInstall(iproject);
		    	if (vm == null) vm = JavaRuntime.getDefaultVMInstall();
		    	IVMRunner vmr = vm.getVMRunner(ILaunchManager.RUN_MODE);
		    	String[] cp = JavaRuntime.computeDefaultRuntimeClassPath(iproject);
		    	VMRunnerConfiguration config = new VMRunnerConfiguration(classToLaunch, cp);
		    	
		    	ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
		    	vmr.run(config, launch, monitor);
	    	}
	    }
	    catch (CoreException e) { 	
	    	// MetaEdit+ doesn't generate Eclipse project every time.
	    }	    
	}

	/**
	 * Writes the plugin.ini file that is written for MetaEdit+ generator. 
	 * The file contains information for the generator.
	 * 
	 * @return path The path to the plugin.ini file.
	 */
	public static String writePluginIniFile(String path) {
	    	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot(); 
        	// set the ini file
        	path = path + "\\plugin.ini";
        	File _temp = new File(path);
        	if (_temp.exists()) _temp.deleteOnExit();
        	IniHandler writer = new IniHandler(path);
        	writer.flushValues();
        	// write the IDE information for MetaEdit+
        	writer.addSetting("IDE", "eclipse");
        	// workspace path
        	writer.addSetting("workspace", new File(root.getLocation().toString()).toString());
        	writer.saveSettings();
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