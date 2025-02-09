/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.graphbrowser;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Display;

import com.metacase.API.*;
import com.metacase.graphbrowser.views.GraphView;
import com.metacase.objects.Graph;

/**
 * Class that runs MetaEdit+ dialogs by calling them via MetaEdit+ API.
 * Since dialogs block application process, this class extends java.lang.Thread class.
 */
public class MEDialog extends Thread {
	
    // Dialog types
	public static final int CREATE_NEW_GRAPH = 1;
	//public static final int CREATE_NEW_GRAPH_OF_SAME_TYPE = 2;
	public static final int EDIT_GRAPH_PROPERTIES = 3;
	private int dialogType;
	private Graph selectedGraph;
	
	/**
	 * Constructor.
	 * @param dialogType integer showing the dialog type. Use CREATE_NEW_GRAPH or EDIT_GRAPH_PROPERTIES
	 * @param selectedGraph graph that is selected in the treeview or null.
	 */
	public MEDialog(int dialogType, Graph selectedGraph) {
	    this.dialogType = dialogType;
	    this.selectedGraph = selectedGraph;
	}
	
	/**
	 * Runs MetaEdit+ dialog.
	 */
	public void run() {
	    MetaEditAPIPortType port = Launcher.getPort();
	    
	   	final Object o;
	    
	    switch (this.dialogType) {
	    	case CREATE_NEW_GRAPH:
	    	    // Opens "Create Graph" dialog in MetaEdit+
	    	    METype m  = null;
	    	    try {
		    		if (selectedGraph == null) {
		    		    m = new METype();
		    		    m.setName("Graph");
		    		}
		    		else {
		    		    m = selectedGraph.getMEType(); 
		    		}
		    		// If new graph is created, update the graph list
		    		o = port.createGraphDialog(m);
		    		if (o instanceof MEAny) {
	    	    		Display.getDefault().syncExec( new Runnable() {  
	    	    			public void run() {
	    	    				GraphView.updateView();
	    	    			} 
	    	    		});
		    		}
	    	    } catch (RemoteException e) {
	    	    	e.printStackTrace();
	    	    }
	    	    break;
	    	case EDIT_GRAPH_PROPERTIES:
	    	    // Opens "Properties" dialog for the selected graph in MetaEdit+
	    	    try {
	    	    	if ( port.propertyDialog(this.selectedGraph.toMEOop()) ) {
	    	    		Display.getDefault().syncExec( new Runnable() {  
	    	    			public void run() {
	    	    				GraphView.updateView();
	    	    			} 
	    	    		});
	    	    	}
	    	    } catch (RemoteException e) { 
	    	    	e.printStackTrace();
	    	    }
	    	    break;
	    }
	    
	}
}
