/*
 * Copyright (c) 2011 MetaCase Consulting
 * Released under the MIT license. See the file license.txt for details. 
 */

package com.metacase.graphbrowser.views;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import com.metacase.API.*;
import com.metacase.graphbrowser.*;
import com.metacase.objects.*;

/**
 * This class provides the treeview that shows the graphs that
 * are open in MetaEdit+. 
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class GraphView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.metacase.graphbrowser.views.GraphView";

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action actionOpenInMetaEdit;
	private Action actionGenerateGraph;
	private Action actionUpdateGraphList;
	private Action doubleClickAction;
	private Action actionOpenSettings;
	private Action actionOpenCreateGraphDialog;
	private Action actionOpenEditPropertiesDialog;
	private ViewContentProvider viewContentProvider;
	public Graph[] graphs;

	 
	class TreeObject implements IAdaptable {
		private Graph graph;
		private TreeObject parent;
		private ArrayList<TreeObject> children;
		
		public TreeObject(Graph _graph) {
			this.graph = _graph;
			this.children = new ArrayList<TreeObject>();
		}
		public TreeObject() {
			this.graph = null;
			this.children = new ArrayList<TreeObject>();
		}
		public String getName() {
			if (this.getGraph() == null) return "";
			return this.getGraph().toString();
		}
		public void setParent(TreeObject parent) {
			this.parent = parent;
		}
		public TreeObject getParent() {
			return parent;
		}
		public Graph getGraph(){
			return this.graph;
		}
		public String toString() {
			return getName();
		}
		public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
			return null;
		}
		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
		}
		public void removeChild(TreeObject child) {
			children.remove(child);
			child.setParent(null);
		}
		public TreeObject [] getChildren() {
			return children.toArray(new TreeObject[children.size()]);
		}
		public boolean hasChildren() {
			return children.size()>0;
		}
		
		public void populate(Graph[] graphs, ArrayList<Graph> stack) {
			if (!stack.contains(this.getGraph())) {
				stack.add(this.getGraph());
				for (Graph g : graphs) {
					TreeObject to = new TreeObject(g);
					this.addChild(to);
					if (g.getChildren() != null && g.getChildren().length > 0) {
						to.populate(g.getChildren(), stack);
					}
				}
			}
		}
	}

	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {
		private TreeObject invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			v.refresh();
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot==null)
					try {
						initialize();
					} catch (Exception e) { }
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeObject) {
				return ((TreeObject)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeObject)
				return ((TreeObject)parent).hasChildren();
			return false;
		}

		public void initialize() {
			Runnable init = new Runnable() {
				public void run() {
					invisibleRoot = new TreeObject();
					graphs = GraphHandler.init();
					invisibleRoot.populate(graphs, new ArrayList<Graph>());				
					}
			};
	        BusyIndicator.showWhile(getSite().getShell().getDisplay(), init);
		}		
	}
	
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}
		
		public Image getImage(Object obj) {
			String imagePath = "icons/graph_icon.png";
			ImageDescriptor image;
			  URL url = null;
			    try {
			    url = new URL(Platform.getBundle(Activator.PLUGIN_ID).getEntry("/"), imagePath);
			    } catch (MalformedURLException e) { }
			    image = ImageDescriptor.createFromURL(url);
			    return image.createImage();
		}
	}
	
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public GraphView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewContentProvider = new ViewContentProvider();
		viewer.setContentProvider(viewContentProvider);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		viewer.expandToLevel(2);

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.metacase.graphbrowser.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				GraphView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillContextMenu(IMenuManager manager) {
		Settings s = Settings.getSettings();
		if (s.getIs50()) manager.add(actionOpenCreateGraphDialog);
		if (!viewer.getSelection().isEmpty()) {
			manager.add(actionOpenInMetaEdit);
			manager.add(actionGenerateGraph);
			if (s.getIs50()) manager.add(actionOpenEditPropertiesDialog);
			manager.add(new Separator());
			drillDownAdapter.addNavigationActions(manager);
			// Other plug-ins can contribute their actions here
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionOpenInMetaEdit);
		manager.add(actionGenerateGraph);
		manager.add(actionUpdateGraphList);
		manager.add(actionOpenSettings);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}
	
	private Graph getSelectedGraph(){
		ISelection selection = viewer.getSelection();
		TreeObject to = (TreeObject) ((IStructuredSelection)selection).getFirstElement();
		if (to == null) return null;
		return to.getGraph();
	}

	private void makeActions() {
		actionOpenInMetaEdit = new Action() {
			public void run() {
				Graph _graph = getSelectedGraph();
				if (_graph == null) return;
				MetaEditAPIPortType port = Launcher.getPort();
				try {
					port.open(_graph.toMEOop());
				} 
				catch (RemoteException e) { 
					e.printStackTrace();
					DialogProvider.showMessageDialog("API error: " + e.toString(), "API error");
					}
			}
		};
		this.setActionDetails(actionOpenInMetaEdit,
				"Open Graph in MetaEdit+",
				"icons/open_graph_in_metaedit_icon.png");
		
		actionGenerateGraph = new Action() {
			public void run() {
				final Graph _graph = getSelectedGraph();
				if (_graph == null) return;
				Settings s = Settings.getSettings();
				if (s.getIs50()) { 
					String okString = "<HTML><p>Choose the generator you want to run for the graph.</p></HTML>";
					String notOkString = "<HTML><p>No generators found for the the graph</p></HTML>";
					MetaEditAPIPortType port = Launcher.getPort();
					JFrame frame = new JFrame("");
	                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	                String [] generators = null;
	                try {
	                	String line = port.generatorNames(_graph.getMEType());
	                	generators = line.split("\r");
					} catch (RemoteException e1) { }
					
					ArrayList<String> generatorList = new ArrayList<String>();
					for (int i=generators.length-1; i>=0; i--) {
						if (!generators[i].startsWith("_") && !generators[i].startsWith("!")) {
							generatorList.add(generators[i]);
						}
					}
					final SelectionDialog p = new SelectionDialog(frame, generatorList, true, okString ,notOkString);
	                JComponent newContentPane = p;
	                frame.setContentPane(newContentPane);
	                frame.addWindowListener(new WindowListener() {
						public void windowOpened(WindowEvent e) { }
						public void windowIconified(WindowEvent e) { }
						public void windowDeiconified(WindowEvent e) { }
						public void windowDeactivated(WindowEvent e) { }
						public void windowClosing(WindowEvent e) {	}
						public void windowClosed(WindowEvent e) { 
							if (p.getIsOKd()) {
								_graph.runGenerator(p.getOpenProjectsAsArray()[0]);
							}
						}
						public void windowActivated(WindowEvent e) { }
					});
	                frame.setResizable(false);
	                frame.setVisible(true);
	                frame.setSize(new Dimension(200, 300)); 
	                frame.setIconImage(SettingsDialog.getImage("icons/metaedit_logo.png"));
	                frame.setLocation(300, 300);
				} else {
				_graph.runAutoBuildFor45();
				}

			}
		};
		this.setActionDetails(actionGenerateGraph,
				"Run Generator ...",
				"icons/run_generator_icon.png");
		
		actionOpenSettings = new Action() {
			public void run() {
				DialogProvider.showSettingsDialog(false);
			}
		};
		this.setActionDetails(actionOpenSettings,
				"Open Settings",
				"icons/settings_icon.png");
				
		doubleClickAction = new Action() {
			public void run() {
				actionOpenInMetaEdit.run();
			}
		};
		actionUpdateGraphList = new Action() {
			public void run() {
				Object oldInput = viewer.getInput();
				Launcher.doInitialLaunch();
				viewContentProvider.initialize();
				viewContentProvider.inputChanged(viewer, oldInput, viewer.getInput());
				viewer.expandToLevel(2);
			}
		};
		this.setActionDetails(actionUpdateGraphList,
				"Update Graph List",
				"icons/update_graph_list_icon.png");
		
		
		actionOpenCreateGraphDialog = new Action() {
			public void run() {
				MEDialogRunner mdr = new MEDialogRunner(MEDialogRunner.CREATE_NEW_GRAPH, getSelectedGraph());
				mdr.start();
			}
		};
		this.setActionDetails(actionOpenCreateGraphDialog,
				"Create a New Graph",
				"icons/create_graph_icon.png");
		
		actionOpenEditPropertiesDialog = new Action() {
			public void run() {
				MEDialogRunner mdr = new MEDialogRunner(MEDialogRunner.EDIT_GRAPH_PROPERTIES, getSelectedGraph());
				mdr.start();
			}
		};
		this.setActionDetails(actionOpenEditPropertiesDialog,
				"Edit Graph Properties",
				"icons/edit_properties_icon.png");
	}
	
	/**
	 * Sets action label text, tooltiptext and icon.
	 * @param a Action for setting the texts and icon.
	 * @param text Text shows in menus.
	 * @param toolTipText Tooltiptext for menu.
	 * @param iconPath path for icon loader.
	 */
	public void setActionDetails(Action a, String text, String iconPath) {
		a.setText(text);
		a.setToolTipText(text);
		a.setImageDescriptor(getImageDescriptor(iconPath));
	}
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
		
	}
	
	/**
	 * Loads images for action buttons.
	 * @param iconPath
	 * @return loaded image
	 */
	public static ImageDescriptor getImageDescriptor(String iconPath) {
		 ImageDescriptor image;
		  URL url = null;
		    try {
		    url = new URL(Platform.getBundle(Activator.PLUGIN_ID).getEntry("/"), iconPath);
		    } catch (MalformedURLException e) { }
		    image = ImageDescriptor.createFromURL(url);
		    return image;
	}
}