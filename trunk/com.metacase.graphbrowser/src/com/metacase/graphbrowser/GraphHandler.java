/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.graphbrowser;

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
			// fetch all instances type of "graph"
			meOops = port.allSimilarInstances(graphType);
		} catch (RemoteException e) { 
			DialogProvider.showMessageDialog("API error: " + e.toString(), "API error");
			e.printStackTrace();
			}
		for (MEOop m : meOops) {
			Graph g = null;
			try {
				// cast MeOop objects to Graph objects
			    g = Graph.MEOopToGraph(m);
			} catch (RemoteException e) {
			    e.printStackTrace();
			}
			graphs.add(g);
		}
		ArrayList<Graph> done = new ArrayList<Graph>();
		for (Graph g : graphs) {
			try {
				// init every graph with its children
				g.initChildren(port, done);
			} catch (Exception e) { 
			    e.printStackTrace();
			}
		}
		// Separate "top level"or "parent" graphs (graphs that are not children of any other graph)
		for (Graph g : graphs) {
			if (!g.getIsChild()) topLevelGraphs.add(g);
		}
		
		Collections.sort(graphs, new Comparator() {
			public int compare(Object o1, Object o2) {
			    Graph g1 = (Graph) o1;
			    Graph g2 = (Graph) o2;
			    return g2.getChildren().length -(g1.getChildren().length);
			}
		});
		
		ArrayList<Graph> reachableGraphs = reachableGraphs(topLevelGraphs);
		
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
}
