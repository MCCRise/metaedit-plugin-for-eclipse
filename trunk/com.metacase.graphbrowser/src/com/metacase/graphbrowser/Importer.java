/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.graphbrowser;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

public class Importer {
	
	String path;
	/**
	 * Class that imports existing Eclipse project to workspace. 
	 * @param path for the Project folder.
	 */
	public Importer(String path) {
		this.path = path;
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
	    try {
	    	description = ResourcesPlugin.getWorkspace().loadProjectDescription(new Path(this.path + "\\.project"));
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
	    	VMRunnerConfiguration config = new VMRunnerConfiguration("MainFrame", cp);
	    	ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
	    	vmr.run(config, launch, monitor);
	    	} catch (CoreException e) { 	
	    		// 	No need to react because MetaEdit+ won't generate Eclipse project every time.
	    	}

	}
}