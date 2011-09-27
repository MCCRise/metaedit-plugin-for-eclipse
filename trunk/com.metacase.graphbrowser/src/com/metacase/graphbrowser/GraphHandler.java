/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.graphbrowser;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import com.metacase.API.*;
import com.metacase.objects.Graph;

/**
 * Graph handler class that gets graphs from MetaEdit+ as MEOop objects and creates
 * Graphs from the that can have subgraphs as their children.  
 */
public class GraphHandler {

	/**
	 * Initializes the graph view by getting the graphs from MetaEdit.
	 * @return Array of graphs.
	 * @throws RemoteException 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Graph [] init() {
		MetaEditAPIPortType port = Launcher.getPort();
		METype graphType = new METype();
		graphType.setName("Graph");
		ArrayList <Graph> graphs = new ArrayList<Graph>();
		ArrayList <Graph> topLevelGraphs = new ArrayList<Graph>();
		MEOop [] meOops = new MEOop[0];
		if (!Launcher.isApiOK()) return topLevelGraphs.toArray(new Graph[topLevelGraphs.size()]);
		try {
			meOops = port.allSimilarInstances(graphType);
		} catch (RemoteException e) { 
			DialogProvider.showMessageDialog("API error: " + e.toString(), "API error");
			e.printStackTrace();
			}
		for (MEOop m : meOops) {
			Graph g = Graph.MEOopToGraph(m);
			graphs.add(g);
		}
		ArrayList<Graph> done = new ArrayList<Graph>();
		for (Graph g : graphs) {
			try {
				g.initChildren(port, done);
			} catch (Exception e) { 
			    e.printStackTrace();
			}
		}
		for (Graph g : graphs) {
			if (!g.getIsChild()) topLevelGraphs.add(g);
		}
		ArrayList<Graph> reachableGraphs = reachableGraphs(topLevelGraphs);
		
		Collections.sort(graphs, new Comparator(){
			public int compare(Object o1, Object o2) {
			    Graph g1 = (Graph) o1;
			    Graph g2 = (Graph) o2;
			    return g2.getChildren().length -(g1.getChildren().length);
			}
		});
		
		for ( Graph g : graphs ) {
			if (!reachableGraphs.contains(g)) {
				topLevelGraphs.add(g);
				buildReachableGraphs(g, reachableGraphs);
			}
		}
		return topLevelGraphs.toArray(new Graph[topLevelGraphs.size()]);
	}
	
	private static ArrayList<Graph> reachableGraphs(ArrayList<Graph> topLevelGraphs) {
		ArrayList<Graph> done = new ArrayList<Graph>();
		for (Graph g : topLevelGraphs) {
			buildReachableGraphs(g, done);
		}
		return done;
	}
	
	private static void buildReachableGraphs(Graph g, ArrayList<Graph> done) {
		if(done.contains(g)) return;
		done.add(g);
		for(Graph child : g.getChildren()) {
			buildReachableGraphs(child, done);
		}
	}

	/**
	 * Reads usernames and passwords or projects from manager.ab file depending on the section parameter.
	 * @param path to manager.ab file.
	 * @param section if "areas" reads the project names. If "users" reads usernames and passwords and returns
	 * them as single String separated with ';'. (eg. "root;root")
	 * @return Array containing Strings.
	 */
	public static String [] readFromManagerAb(File path, String section) {
		ArrayList <String> list = new ArrayList<String>();
		Scanner scanner = null;
	    String line;
	    if (!path.exists()) return list.toArray(new String[list.size()]);
	    try {
			scanner = new Scanner(path);
		} catch (FileNotFoundException e) { 
		    e.printStackTrace();
		}
	    try {
	    	while (scanner.hasNextLine()){
	    		line = scanner.nextLine();
	    		if (line.contains("[" + section + "]")) {
	    			line = scanner.nextLine();
	    			while (!line.startsWith("[")) {
	    				if (line.length() > 1) {
	    					if (section == "areas") list.add(parseProjectFromLine(line));
	    					if (section == "users") list.add(parseNameAndPasswordFromLine(line));
	    				}
	    				line = scanner.nextLine();
	    			}  
	    		}
	    	}
	    }
	    finally {
	      scanner.close();
	    }
	    list.removeAll(Collections.singleton(null));
		return list.toArray(new String[list.size()]);
	}
	
	/**
	 * Parses project name from manager.ab file line.
	 * @param line read from manager.ab
	 * @return project name.
	 */
	private static String parseProjectFromLine(String line) {	
		String [] inValidProjects = {"Administration-Common", "Administration-System" };
		String project = line.split(";")[1];
		for (int i=0; i<inValidProjects.length; i++) {
			if (project.equalsIgnoreCase(inValidProjects[i])) return null;
		}
		return project;
	}
	
	/**
	 * Parses name and password from manager.ab file line
	 * @param line read from manager.ab [users] section.
	 * @return name and password (name;password)
	 */
	private static String parseNameAndPasswordFromLine(String line) {
		String [] splitted = line.split(";");
		return splitted[1] + ";" + splitted[2];
	}
}
