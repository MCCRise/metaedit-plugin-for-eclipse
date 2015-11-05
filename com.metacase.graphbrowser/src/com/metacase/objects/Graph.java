/**
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 * 
 * Graph class represents a MetaEdit+ graph. Attributes name, type, area and object ID,
 * boolean value of being child to another graph and array of children graphs
 */

package com.metacase.objects;

import java.io.File;
import java.rmi.RemoteException;
import java.util.*;
import com.metacase.API.*;
import com.metacase.graphbrowser.*;

public class Graph {

	private String name;
	private String type;
	private String typeName;
	private int areaID;
	private int objectID;
	private boolean isChild  = false;
	private String classToLaunch = "";
	private String projectName = "";
	private Graph[] children = new Graph[0];
	private static Hashtable<String, String> typeNameTable = new Hashtable<String, String>();
	private static Hashtable<Integer, Hashtable<Integer, Graph>> projectTable = new Hashtable<Integer, Hashtable<Integer, Graph>>();
	
	/**
	 * Constructor.
	 * 
	 * @param name Graph name
	 * @param type Graph type name
	 * @param areaID Area id of MEOop
	 * @param objectID Object id of MEOop
	 */
	private Graph(String name, String type, String typeName, int areaID, int objectID) {
	    this.setName(name);
	    this.setType(type);
	    this.setTypeName(typeName);
	    this.setAreaID(areaID);
	    this.setObjectID(objectID);
	    Hashtable<Integer, Graph> graphTable = (Hashtable<Integer, Graph>) projectTable.get(areaID);
	    if ( graphTable == null ) {
	    	graphTable = new Hashtable<Integer, Graph>();
	    	projectTable.put(areaID, graphTable);
	    }
	    graphTable.put(objectID, this);
	}
	
	/**
	 * Creates Graph object from MEOop and saves it to Hashtable.
	 * Checks if the graph is already in the hashtable. Does not
	 * duplicate graphs. If graph already exists checks if its name
	 * has changed and edits it if needed.
	 * 
	 * @param m MEOop object
	 * @return graph The created Graph
	 * @throws RemoteException 
	 */
	public static Graph MEOopToGraph(MEOop m) throws RemoteException {
	    Hashtable<Integer, Graph> graphTable = (Hashtable<Integer, Graph>) projectTable.get(m.getAreaID());
	    Graph graph = null;
            if (graphTable == null)
            {
                graphTable = new Hashtable<Integer, Graph>();
                projectTable.put(m.getAreaID(), graphTable);
            }
            graph = (Graph) graphTable.get(m.getObjectID());	
            MetaEditAPIPortType port = Launcher.getPort();
        
            METype _graphType = port.type(m);
            String _typeName;
            if (typeNameTable.containsKey(_graphType.getName()))
            {
                _typeName = (String)typeNameTable.get(_graphType.getName());
            }
            else
            {
                _typeName = port.typeName(_graphType);
                typeNameTable.put(_graphType.getName(), _typeName);
            }
            if (graph == null) {
        	graph = new Graph(port.userPrintString(m), _graphType.getName(), _typeName, m.getAreaID(), m.getObjectID());
                graphTable.put(m.getObjectID(), graph);
        	    }
            else {
                graph.setName(port.userPrintString(m));
                graph.setType(_graphType.getName());
                graph.setTypeName(_typeName);
            }
            return graph;
	}

	/**
     * Resets all cached graph and type information from MetaEdit+
    */
    public static void resetCaches()
    {
    	projectTable = new Hashtable<Integer, Hashtable<Integer, Graph>>();
        typeNameTable = new Hashtable<String, String>();
    }
	
	/**
	 * The generator run process. Calls for plugin.ini file writer, runs the generator,
	 * reads and removes the plugin.ini file and imports and executes (maybe) the imported project. 
	 * 
	 * @param generator Name of the generator.
	 */
	public void executeGenerator(String generator) {
	    String pluginINIpath = this.writePluginIniFile();
	    MetaEditAPIPortType port = Launcher.getPort();
	    // Run generator
	    this.runGenerator(port, generator);
	    // Remove the written INI file and import generated project.
	    this.readAndRemoveIniFile(pluginINIpath);
	    
	    /* null values show that those values were not included in ini file.
	       In that case graph's name is used for both project name and main class name. */
	    Importer.importAndExecute(
		    	this.getProjectName()   == null ? this.getName() 		: this.getProjectName(),
				this.getClassToLaunch() == null ? "_" + this.getName() 	: this.getClassToLaunch()
		    );
	}

	/**
	 * Runs generator for caller Graph. After calling ME+ to run generator, tries
	 * to import project with same name as the graph to workspace. Used for MetaEdit+ 5.0 API
	 * 
	 * @param port Connection to MetaEdit+
	 * @param generator Generator name that is to be run.
	 */
	public void runGenerator(MetaEditAPIPortType port, String generator) {
		Settings s = Settings.getSettings();
	    try {
	    	if (s.getVersion().isAtLeast("5.0")) {
	    		port.forGraphRun(this.toMEOop(), generator);
	    	} else {
	    		MENull meNull = new MENull();
		    	port.forName(meNull, this.getName(), this.getTypeName(), generator);
	    	}
	    } catch (RemoteException e) { 
			DialogProvider.showMessageDialog("API error: " + e.toString(), "API error");
			e.printStackTrace();
	    }
	}
		
	/**
	 * Reads and removes ini file.
	 * 
	 * @param path Path to the file.
	 */
	private void readAndRemoveIniFile(String path) {	    
	    IniHandler h = new IniHandler(path);
	    this.setClassToLaunch(h.getSetting("classToLaunch"));
	    this.setProjectName(h.getSetting("projectName"));

	    Importer.removeIniFile(new File(path));
	} 

	
	/**
	 * Method stub for importer's plugin.ini writer. Writes the plugin.ini file under
	 * the MetaEdit+ working directory.
	 * 
	 * @param generatorName name of the generator in MetaEdit+.
	 * @return path of the written ini file.
	 */
	private String writePluginIniFile() {
	    Settings s = Settings.getSettings();
	    // TODO: what if working directory is null or empty?
	    return Importer.writePluginIniFile(s.getWorkingDirectory());
	}
	
	/**
	 * Creates a MEOop object from Graph object.
	 * 
	 * @return created MEOop object.
	 */
	public MEOop toMEOop() {
	    return new MEOop(this.getAreaID(), this.getObjectID());
	}
	
	/**
	 * Method that return METype of Graph object.
	 * 
	 * @return METype corresponding to Graphs attribute (String) type.
	 */
	public METype getMEType() {
	    METype type = new METype();
	    type.setName(this.getTypeName());
	    return type;
	}
	
	/**
	 * Setter for Graphs name
	 * 
	 * @param _name Graphs name
	 */
	public void setName(String _name) {
	    this.name = _name;
	}
	
	/**
	 * Getter for Graphs name.
	 * 
	 * @return graph name.
	 */
	public String getName() {
	    return this.name;
	}
	
	/**
	 * Setter for Graphs type name.
	 * 
	 * @param _type name of type.
	 */
	public void setType(String _type) {
	    this.type = _type;
	}
	
	/**
	 * Getter for Graph type name
	 * 
	 * @return name of graph type.
	 */
	public String getType() {
	    return this.type;
	}
	
	/**
	 * Getter for the graph type name
	 * 
	 * @return typeName Name of the graph type.
	 */
	public String getTypeName() {
	    return typeName;
	}

	/**
	 * Setter for Graph type name
	 * 
	 * @param typeName Name of the graph type
	 */
	public void setTypeName(String typeName) {
	    this.typeName = typeName;
	}

	/**
	 * Graphs area id setter.
	 * 
	 * @param _areaID area id integer.
	 */
    	public void setAreaID(int _areaID) {
    	    this.areaID = _areaID;
	}
	
	/**
	 * Getter for graphs area id.
	 * 
	 * @return id of graphs area. 
	 */
	public int getAreaID(){
	    return this.areaID;
	}
	
	/**
	 * Setter for graphs object id.
	 * 
	 * @param _objectID - graphs object id.
	 */
	public void setObjectID(int _objectID){
	    this.objectID = _objectID;
	}
	
	/**
	 * Getter of graph object id.
	 * 
	 * @return object id integer.
	 */
	public int getObjectID() {
	    return this.objectID;
	}

	/**
	 * Graphs toString method.
	 * 
	 * @return Graphs name.
	 */
	public String toString() {
	    return this.name;
	}
	
	/**
	 * Sets the graph isChild property showing if the graph is subgraph of
	 * another graph or not.
	 * 
	 * @param _isChild
	 */
	public void setIsChild(boolean _isChild){
	    this.isChild = _isChild;
	}
	
	/**
	 * Getter for graphs isChild property.  
	 * 
	 * @return isChild boolean property.
	 */
	public boolean getIsChild(){
	    return this.isChild;
	}
	
	/**
	 * Get Graph's children
	 * 
	 * @return children in array.
	 */
	public Graph[] getChildren() {
	    return this.children;
	}
	
	/**
	 * Setter for children array.
	 * 
	 * @param children array of children graphs.
	 */
	public void setChildren(Graph[] children){
	    this.children = children;
	}
	
	/**
	 * Getter for the main class name.
	 * 
	 * @return name of the main class.
	 */
	public String getClassToLaunch() {
		return this.classToLaunch;
	}
	
	/**
	 * Setter for the main class name.
	 * 
	 * @param className main class name.
	 */
	public void setClassToLaunch(String className) {
		this.classToLaunch = className; 
	}
	
	/**
	 * Getter for the project name.
	 * 	
	 * @return name of the project.
	 */
	public String getProjectName() {
		return this.projectName;
	}
	
	/**
	 * Setter for the project name.
	 * 
	 * @param _projectName The project name.
	 */
	public void setProjectName(String _projectName) {
		this.projectName = _projectName;
	}

	public static Comparator<Graph> GraphComparator
			= new Comparator<Graph>() {
		public int compare(Graph g1, Graph g2) {
			int result = g1.getName().compareTo(g2.getName());
			if (result == 0) {
				result = g1.getType().compareTo(g2.getType());
			}
			return result; 
		}
	};
	
	/**
	 * Init graphs children recursively calling method for every children. Except
	 * those that are already initialized.
	 * 
	 * @param port Port for API calls.
	 * @param done Array of graphs that are initialized.
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
	    	for (int i=0; i < subgraphOops.length; i++) {
	    		Graph g = graphs[i] = MEOopToGraph(subgraphOops[i]);
	    		g.setIsChild(true);
	    		g.initChildren(port, done);
	    	}
	    this.setChildren(graphs);
	    }
	}
}
