/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.objects;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import com.metacase.API.*;
import com.metacase.graphbrowser.DialogProvider;
import com.metacase.graphbrowser.Importer;
import com.metacase.graphbrowser.Launcher;
import com.metacase.graphbrowser.Settings;

/**
 * Graph class represents a MetaEdit+ graph. Attributes name, type, area and object ID,
 * boolean value of being child to another graph and array of children graphs
 */
public class Graph {

	protected String name;
	protected String type;
	protected int areaID;
	protected int objectID;
	protected boolean isChild  = false;
	protected Graph[] children = new Graph[0];
	protected static Hashtable<Integer, Hashtable<Integer, Graph>> projectTable = new Hashtable<Integer, Hashtable<Integer, Graph>>();
	
	/**
	 * Constructor.
	 * @param name Graph name
	 * @param type Graph type name
	 * @param areaID Area id of MEOop
	 * @param objectID Object id of MEOop
	 */
	private Graph(String name, String type, int areaID, int objectID) {
		this.setName(name);
		this.setType(type);
		this.setAreaID(areaID);
		this.setObjectID(objectID);
		
		Hashtable<Integer, Graph> graphTable =  (Hashtable<Integer, Graph>) projectTable.get(areaID);
		if ( graphTable == null ) {
			graphTable  = new Hashtable<Integer, Graph>();
			projectTable.put(areaID, graphTable);
		}
		graphTable.put(objectID, this);
	}
	
	/**
	 * Creates Graph object from MEOop and saves it to Hashtable.
	 * Checks if the graph is already in the hashtable. Does not
	 * duplicate graphs. If graph already exists checks if its name
	 * has changed and edits it if needed.
	 * @param m MEOop object
	 * @return created Graph
	 */
	public static Graph MEOopToGraph(MEOop m) {
		Hashtable<Integer, Graph> graphTable = (Hashtable<Integer, Graph>) projectTable.get(m.getAreaID());
		Graph graph = null;
		if ( graphTable != null ) {
			graph = (Graph) graphTable.get(m.getObjectID());	
		}
		MetaEditAPIPortType port = Launcher.getPort();
		if (graph == null) {
			try {
				graph = new Graph(port.userPrintString(m), port.typeName(port.type(m)), m.getAreaID(), m.getObjectID());
			} catch (RemoteException e) { }
		}
		// in the case graph exists check if its name has changed.
		else if (graph != null) {
			String _name = "";
			try {
				 _name = port.userPrintString(m);
			} catch (RemoteException e) { }
			if (!_name.equals(graph.getName())) {
				graph.setName(_name);
			}
		}
		return graph;
	}
	
	/**
	 * Runs generator for caller Graph. After calling ME+ to run generator, tries
	 * to import project with same name as the graph to workspace. Used for MetaEdit+ 5.0 API
	 * @param generator name of the generator to be run.
	 */
	public void runGenerator(String generator) {
		MetaEditAPIPortType port = Launcher.getPort();
		Settings s = Settings.getSettings();
		try {
			port.forGraphRun(this.toMEOop(), generator);
		} catch (RemoteException e) { 
			DialogProvider.showMessageDialog("API error: " + e.toString(), "API error");
		}
		this.importProject(s);
	}
	
	/**
	 * Runs Autobuild generator for selected graph. This method is used for MetaEdit+ 4.5 API.
	 */
	public void runAutoBuildFor45() {
		MetaEditAPIPortType port = Launcher.getPort();
		MENull meNull = new MENull();
		try {
			port.forName(meNull, this.getName(), this.getType(), "Autobuild");
		} catch (RemoteException e) { 
			DialogProvider.showMessageDialog("API error: " + e.toString(), "API error");
		}
		this.importProject(Settings.getSettings());
	}
		
	/**
	 * Imports generated project.
	 * @param s Settings instance
	 */
	private void importProject(Settings s) {
		String workDir = s.getWorkingDirectory();
		if (workDir.equals("")) {
			DialogProvider.showMessageDialog("Error when importing generated project to workspace. Can't read working directory path from .mer file.",
					"MER file doesn't exist");
			return;
		}
		Importer i = new Importer(workDir + "\\reports\\" + this.getName());
		i.importProject();
	}
	
	/**
	 * Get Graph's children
	 * @return children in array.
	 */
	public Graph[] getChildren() {
		return this.children;
	}
	
	/**
	 * Creates a MEOop object from Graph object.
	 * @return created MEOop object.
	 */
	public MEOop toMEOop() {
		return new MEOop(this.getAreaID(), this.getObjectID());
	}
	
	/**
	 * Method that return METype of Graph object.
	 * @return METype corresponding to Graphs attribute (String) type.
	 */
	public METype getMEType() {
		METype type = new METype();
		type.setName(this.getType());
		return type;
	}
	
	/**
	 * Setter for Graphs name
	 * @param _name Graphs name
	 */
	public void setName(String _name) {
		this.name = _name;
	}
	
	/**
	 * Getter for Graphs name.
	 * @return graph name.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Setter for Graphs type name.
	 * @param _type name of type.
	 */
	public void setType(String _type) {
		this.type = _type;
	}
	
	/**
	 * Getter for Graph type name
	 * @return name of graph type.
	 */
	public String getType() {
		return this.type;
	}
	
	/**
	 * Graphs area id setter.
	 * @param _areaID area id integer.
	 */
	public void setAreaID(int _areaID) {
		this.areaID = _areaID;
	}
	
	/**
	 * Getter for graphs area id.
	 * @return - id of graphs area. 
	 */
	public int getAreaID(){
		return this.areaID;
	}
	
	/**
	 * Setter for graphs object id.
	 * @param _objectID - graphs object id.
	 */
	public void setObjectID(int _objectID){
		this.objectID = _objectID;
	}
	
	/**
	 * Getter of graph object id.
	 * @return object id integer.
	 */
	public int getObjectID() {
		return this.objectID;
	}

	/**
	 * Graphs toString methdod
	 * @return Graphs name.
	 */
	public String toString() {
		return this.name;
	}
	
	/**
	 * Sets the graph isChild property showing if the graph is subgraph of
	 * another graph or not.
	 * @param _isChild
	 */
	public void setIsChild(boolean _isChild){
		this.isChild = _isChild;
	}
	
	/**
	 * Getter for graphs isChild property.  
	 * @return isChild boolean property.
	 */
	public boolean getIsChild(){
		return this.isChild;
	}
	
	/**
	 * Setter for children array.
	 * @param children - array of children graphs.
	 */
	public void setChildren(Graph[] children){
		this.children = children;
	}
	
	/**
	 * Init graphs children recursively calling method for every children. Except
	 * those that are already initialized.
	 * @param port - Port for API calls.
	 * @param done - Array of graphs that are initialized.
	 * @throws Exception
	 */
	public void initChildren(MetaEditAPIPortType port, ArrayList<Graph> done) throws  Exception {
		if (done.contains(this)) return;
		done.add(this);
		MEOop[] subgraphOops = null;
		subgraphOops = port.subgraphs(this.toMEOop());
		// Set the subgraph items to be children of this graph.
		if (subgraphOops.length > 0 && subgraphOops != null) {
			Graph [] graphs = new Graph[subgraphOops.length];
			for (int i=0; i < subgraphOops.length; i++){
				Graph g = graphs[i] = MEOopToGraph(subgraphOops[i]);
				g.setIsChild(true);
				g.initChildren(port, done);
			}
			this.setChildren(graphs);
		 }
	}
}
