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
import java.util.*;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StackLayout;
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

public class GraphView extends ViewPart implements Observer {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.metacase.graphbrowser.views.GraphView";

	private TreeViewer treeViewer;
	private Composite errorView;
	private Composite container;
	private StackLayout layout;
	private static Action actionOpenInMetaEdit;
	private static Action actionRunAutobuild;
	private static Action actionCallGenerator;
	private static Action actionUpdateGraphList;
	private static Action actionStartMetaEdit;
	private static Action actionOpenSettings;
	private static Action actionOpenCreateGraphDialog;
	private static Action actionOpenEditPropertiesDialog;
	private static Action actionToggleGraphTypeText;
	private static Action doubleClickAction;
	private ViewContentProvider viewContentProvider;
	private static boolean isGraphTypeText;
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
			String s = isGraphTypeText ? getName() + ": " + getGraph().getTypeName() : getName();
		    return s;
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

	class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
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
		    if (parent instanceof TreeObject) return ((TreeObject)parent).hasChildren();
		    return false;
		}
		
		/**
		 * Initializes the tree by first calling the graphs from MetaEdit+ and
		 * creating a tree from the graph set. Shows busy cursor while the work is being done.
		 */
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
	    	Settings.getSettings().addObserver(this);
	    	
	    	layout = new StackLayout();
	    	container = new Composite(parent, SWT.NONE);
	    	container.setLayout(layout);
	    	
	    	createErrorView(container);
	    	createTreeView(container); 
	    	
	    	makeActions();
	    	hookContextMenu();
	    	hookDoubleClickAction();
	    	contributeToActionBars();
	    	setToolBarButtonsEnabled();
	    	
	    	setView();
	}

	/**
	 * Sets a view on top of StackLayout.
	 * Shows the treeview if API connection found. If not
	 * shows the errorview.
	 */
	private void setView() {
	    layout.topControl = this.isAPI() ? treeViewer.getTree() : errorView; 
	    container.layout();
	    setToolBarButtonsEnabled();
	}
	
	/**
	 * Creates the treeview that is shown if API connection is available.
	 * @param parent The parent composite for the view.
	 */
	private void createTreeView(Composite parent) {
	    	treeViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
	    	viewContentProvider = new ViewContentProvider();
	    	treeViewer.setContentProvider(viewContentProvider);
	    	treeViewer.setLabelProvider(new ViewLabelProvider());
	    	treeViewer.setSorter(new NameSorter());
	    	treeViewer.setInput(getViewSite());
	    	treeViewer.expandToLevel(2);    	
	    	treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
        		    
	    	    @Override
	    	    public void selectionChanged(SelectionChangedEvent event) {
	    	    	setToolBarButtonsEnabled();
	    	    	setView();
	    	    }
	    	});
	}
	
	/**
	 * Creates an error view that is shown when no API connection is available.
	 * @param parent The parent composite for the error view.
	 */
	private void createErrorView(Composite parent) {
		GridLayout gridLayout = new GridLayout();
		GridData gridData;
		
	    errorView = new Composite(parent, SWT.NONE);
	    errorView.setLayout(gridLayout);
	    
	    // Add an empty space above the label.
	    new Label(errorView, SWT.NONE).setText(""); 
	    
	    Label errorLabel = new Label(errorView, SWT.NONE);
	    errorLabel.setAlignment(SWT.CENTER);
	    errorLabel.setText("No API connection found.");
	    gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		errorLabel.setLayoutData(gridData);
	    
	    Listener listener = new Listener() {
	    	public void handleEvent(Event event) {
	    		actionStartMetaEdit.run();
	    	}
	    };
	    
	    Button errorButton = new Button(errorView, SWT.PUSH);
	    errorButton.addListener(SWT.Selection, listener);
	    errorButton.setText("Start MetaEdit+");
	    gridData = new GridData();
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.grabExcessHorizontalSpace = true;
	    errorButton.setLayoutData(gridData);
	    	
	    errorView.layout();
	}
	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
		    public void menuAboutToShow(IMenuManager manager) {
			GraphView.this.fillContextMenu(manager);
		    }
		});
		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillContextMenu(IMenuManager manager) {
		if (!treeViewer.getSelection().isEmpty()) {
		    manager.add(actionRunAutobuild);
		    if (this.is50()) manager.add(actionCallGenerator);	
		    manager.add(new Separator());
		    manager.add(actionOpenInMetaEdit);
		    if (this.is50()) manager.add(actionOpenEditPropertiesDialog);
		    if (this.is50()) manager.add(actionOpenCreateGraphDialog);
		} else {
		    if (this.is50()) manager.add(actionOpenCreateGraphDialog);
		}
		manager.add(new Separator());
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
	    manager.add(actionRunAutobuild);
	    manager.add(actionCallGenerator);
		manager.add(new Separator());
		manager.add(actionOpenInMetaEdit);
		manager.add(actionOpenCreateGraphDialog);
		manager.add(new Separator());
		manager.add(actionUpdateGraphList);
		manager.add(actionOpenSettings);
		manager.add(new Separator());
		manager.add(actionToggleGraphTypeText);
	}
	
	/**
	 * Check if set MetaEdit+ program version is 4.5 or 5.0
	 * @return true if version is 5.0 false if not.
	 */
	private boolean is50() {
	    Settings s = Settings.getSettings();
	    return s.getIs50();
	}
	
	/**
	 * Check if API is ok
	 * @return true if connection available.
	 */
	private boolean isAPI() {
	    return Launcher.isApiOK();
	}
	
	/**
	 * Sets the toolbar buttons enabled or disabled.
	 */
	private void setToolBarButtonsEnabled() {
	    boolean _is50 = this.is50();
	    boolean _isAPI = this.isAPI();
	    boolean _isSelection = !treeViewer.getSelection().isEmpty();
	    
	    actionRunAutobuild.setEnabled(_isAPI && _isSelection);
	    actionCallGenerator.setEnabled( _is50 && _isAPI && _isSelection);
	    actionOpenInMetaEdit.setEnabled(_isAPI && _isSelection);
	    actionOpenCreateGraphDialog.setEnabled(_is50 && _isAPI);
	    actionUpdateGraphList.setEnabled(true);
	    actionOpenSettings.setEnabled(true);
	    actionToggleGraphTypeText.setEnabled(isAPI());
	    
	    IActionBars bars = getViewSite().getActionBars();
	    IToolBarManager manager = bars.getToolBarManager();
	    manager.update(true);
	}
	
	/**
	 * Gets the selected graph from treeview.
	 * @return selected Graph or null
	 */
	private Graph getSelectedGraph(){
		ISelection selection = treeViewer.getSelection();
		TreeObject to = (TreeObject) ((IStructuredSelection)selection).getFirstElement();
		if (to == null) return null;
		return to.getGraph();
	}

	/**
	 * Creates the action methods for toolbar and context menu items.
	 */
	private void makeActions() {
	    	// This action opens selected graph in MetaEdit+. 
	    	// SHows user a dialog in case of issues.
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
		
		// RunsAutobuild for the selected graph.
		actionRunAutobuild = new Action() {
		    public void run() {
			Graph _graph = getSelectedGraph();
			if (_graph == null) return;
			_graph.executeGenerator("Autobuild");
		    }
		};
		
		this.setActionDetails(actionRunAutobuild,
			"Run Autobuild",
			"icons/run_generator_icon.png");
		
		// Runs seleceted generator for graph. Shows all available generators to user in 
		// a list where user can choose one to be run.
		actionCallGenerator = new Action() {
		    	public void run() {
		    		final Graph _graph = getSelectedGraph();
		    		if (_graph == null) return;
		    		// Creates dialog that shows available generators and lets user to select one.
					String okString = "<HTML><p>Select the generator to run.</p></HTML>";
					String notOkString = "<HTML><p>No generators found for the the graph</p></HTML>";
					MetaEditAPIPortType port = Launcher.getPort();
					
					JFrame frame = new JFrame("");
					
					String [] generators = null;
					try {
					    String line = port.generatorNames(_graph.getMEType());
					    generators = line.split("\r");
					} catch (RemoteException e) {
					    e.printStackTrace();
					}
					
					ArrayList<String> generatorList = new ArrayList<String>();
					
					for (int i=generators.length-1; i >= 0; i--) {
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
					    		_graph.executeGenerator(p.getItemsAsArray()[0]);
							}
					    }
					    public void windowActivated(WindowEvent e) { }
					});
					
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setResizable(true);
					frame.setVisible(true);
					frame.setSize(new Dimension(250, 300));
					frame.setIconImage(SettingsDialog.getImage("icons/metaedit_logo.png"));
					frame.setLocation(300, 300);
				}
		};
		this.setActionDetails(actionCallGenerator,
				"Select Generator to Run",
				"icons/select_generator_to_run_icon.png");
		
		// Open settings dialog.
		actionOpenSettings = new Action() {
			public void run() {
				DialogProvider.showSettingsDialog(false);
				
			}
		};
		this.setActionDetails(actionOpenSettings,
				"MetaEdit+ Launch Parameters",
				"icons/settings_icon.png");
		
		// Double click opens Graph in MetaEdit+
		doubleClickAction = new Action() {
			public void run() {
			    actionOpenInMetaEdit.run();
			}
		};
		
		actionUpdateGraphList = new Action() {
			public void run() {
			    Object oldInput = treeViewer.getInput();
			    viewContentProvider.initialize();
			    viewContentProvider.inputChanged(treeViewer, oldInput, treeViewer.getInput());
			    treeViewer.expandToLevel(2);
			    setView();
			}
		};
		this.setActionDetails(actionUpdateGraphList,
				"Update Graph List",
				"icons/update_graph_list_icon.png");
		
		// This action looks for existing API connection and start new MetaEdit+ instance if 
		// no API connection is found.
		actionStartMetaEdit = new Action() {
		  public void run() {
		      boolean runUpdate;
		      if (isAPI()) {
			  DialogProvider.showMessageDialog("Found an existing API connection.", "API connection found.");			  
			  runUpdate = true;
		      } else {
			  runUpdate = Launcher.doInitialLaunch();
		      }
		      if (runUpdate) actionUpdateGraphList.run(); 
		  }
		};
		
		// Open graph creation dialog in MetaEdit+
		actionOpenCreateGraphDialog = new Action() {
		    public void run() {
		    	MEDialog md = new MEDialog(MEDialog.CREATE_NEW_GRAPH, getSelectedGraph());
		    	md.start();
		    }
		};
		this.setActionDetails(actionOpenCreateGraphDialog,
				"Create a New Graph",
				"icons/create_graph_icon.png");
		
		// Opens properties dialog for the selected graph in MetaEdit+.
		actionOpenEditPropertiesDialog = new Action() {
			public void run() {
			    MEDialog md = new MEDialog(MEDialog.EDIT_GRAPH_PROPERTIES, getSelectedGraph());
			    md.start();
			}
		};
		this.setActionDetails(actionOpenEditPropertiesDialog,
				"Edit Graph Properties",
				"icons/edit_properties_icon.png");
		
		actionToggleGraphTypeText = new Action("", Action.AS_CHECK_BOX) {
			public void run() {
				isGraphTypeText = actionToggleGraphTypeText.isChecked() ? true : false; 
				viewContentProvider.inputChanged(treeViewer, null, null);
			}
		};
		
		this.setActionDetails(actionToggleGraphTypeText,
				"Show/Hide Graph Type", 
				"icons/folder_explore.png");
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
	    treeViewer.addDoubleClickListener(new IDoubleClickListener() {
		public void doubleClick(DoubleClickEvent event) {
			doubleClickAction.run();
		}
	    });
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		layout.topControl.setFocus();
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
	    } catch (MalformedURLException e) {
		e.printStackTrace();
	    }
	    image = ImageDescriptor.createFromURL(url);
	    return image;
	}

	/**
	 * Implementation of java.util.Observer interface method. Checks if
	 * the observable class is Settings and calls the method for checking
	 * the toolbar buttons enabling/disabling. 
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
	    if (arg0.getClass() == Settings.class) {
		Display.getDefault().syncExec(
			new Runnable() {
			    public void run(){
				setToolBarButtonsEnabled();
			    }
			});
	    }
	}
	
	/**
	 * Updated the view by calling the update action. For use outside the UI thread.
	 */
	public static synchronized void updateView() {
		actionUpdateGraphList.run();
	}
}