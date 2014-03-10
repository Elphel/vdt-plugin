/*******************************************************************************
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of Eclipse/VDT plug-in.
 * Eclipse/VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * Eclipse/VDT plug-in is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Eclipse VDT plug-in; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *******************************************************************************/
package com.elphel.vdt.ui.views;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
//import org.eclipse.swt.graphics.ImageData;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

import java.io.File;

import com.elphel.vdt.core.launching.LaunchCore;
import com.elphel.vdt.core.launching.ToolLogFile;
import com.elphel.vdt.core.options.OptionsCore;
import com.elphel.vdt.core.options.ValueBasedOption;
import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.menu.DesignMenu;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.core.tools.params.Tool.TOOL_MODE;
import com.elphel.vdt.core.tools.params.Tool.TOOL_STATE;
import com.elphel.vdt.core.tools.params.ToolSequence;
import com.elphel.vdt.core.tools.params.types.RunFor;
import com.elphel.vdt.Txt;
import com.elphel.vdt.VDT;
import com.elphel.vdt.VerilogUtils;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.VDTPluginImages;
import com.elphel.vdt.ui.variables.SelectedResourceManager;
import com.elphel.vdt.ui.views.DesignMenuModel;
import com.elphel.vdt.ui.dialogs.DesignMenuSelectionDialog;
import com.elphel.vdt.ui.options.ContextOptionsDialog;
import com.elphel.vdt.ui.options.FilteredFileSelector;
import com.elphel.vdt.ui.options.SetupOptionsDialog;
import com.elphel.vdt.ui.options.SetupOptionsManager;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;

/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
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

public class DesignFlowView extends ViewPart implements ISelectionListener {

    // Persistance tags.
    private static final String TAG_SELECTED_RESOURCE =   "SelectedProject";
    private static final String TAG_SELECTED_HDL_FILE =   "SelectedHdlFile";
    private static final String TAG_SELECTED_HDL_FILTER = "SelectedHdlFilter";
    private static final String TAG_LINKED_TOOLS =        "LinkedTools";

    private TreeViewer viewer;
//    private DrillDownAdapter drillDownAdapter;
    private Action showLaunchConfigAction;
//    private Action launchAction;
    
    private Action toggleLinkedTools;
    private Action toggleSaveTools;
    private Action toggleStopTools;

    private Action showInstallationPropertiesAction;
    private ClearAction clearInstallationPropertiesAction;
    
    private Action showPackagePropertiesAction;
    private GlobalContextsAction showPackagePropertiesToolbarAction;
    private ClearContextAction clearPackagePropertiesAction;
    
    private Action showProjectAction;
    private LocalContextsAction showProjectPropertiesToolbarAction;
    private ClearLocalContextAction clearProjectPropertiesAction;
    
    private Action showPropertiesAction;
    private ClearAction clearToolPropertiesAction;

    private Action selectDesignMenuAction;
    private ClearToolStates clearToolStatesAction;
    private ClearStateFiles clearStateFilesAction;
    private ClearLogFiles clearLogFilesAction;

    
    
    private IResource selectedResource;
    
    private DesignMenuModel.Item selectedItem;
    
    private ValueBasedOption desigMenuName;
    
    private String defaultPartName;

    private IMemento memento;
    
    private Action pinAction;
    private Action restoreAction;
    private Action restoreSelectAction;

    private Action playbackLogLatestAction;
    private Action playbackLogSelectAction;
    
    
    IDoubleClickListener doubleClickListener=null;
    private Action [] launchActions;
    private ToolSequence toolSequence=null;
//    private Composite compositeParent;
    /**
     * The constructor.
     */
    public DesignFlowView() {
        desigMenuName = new ValueBasedOption(VDT.OPTION_PROJECT_MENU);
        toolSequence = new ToolSequence(this);
    }
    public ToolSequence getToolSequence(){
    	return toolSequence;
    }
    public void setToggleSaveTools(boolean checked){
    	toggleSaveTools.setChecked(checked);
    }
    public void setToggleStopTools(boolean checked){
    	toggleStopTools.setChecked(checked);
    }

    /* 
     * Method declared on IViewPart.
     */
     public void init(IViewSite site, IMemento memento) throws PartInitException {
         defaultPartName = getPartName();
         super.init(site, memento);
         this.memento = memento;
         // add itself as a global selection listener
         getSite().getPage().addSelectionListener(this);
     }
    
    /* 
     * Method declared on IWorkbenchPart.
     */
    public void dispose() {
        // remove ourself as a global selection listener
        getSite().getPage().removeSelectionListener(this);
        // run super.
        super.dispose();
    }

    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
			System.out.println("+++++ createPartControl()");
    	
//    	compositeParent=parent; // will it help to re-draw
        viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
//        drillDownAdapter = new DrillDownAdapter(viewer);

        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
// more complex sorter is needed        
//        viewer.setSorter(new NameSorter());
//        viewer.setInput(getViewSite());
//        viewer.setInput(ToolsCore.getDesignMenu());
        viewer.addSelectionChangedListener(new ToolSelectionChangedListener());
        // Draw tool state (running, success, failure, ...) icon after the label in the tree
        final Tree tree=  viewer.getTree();
        tree.addListener(SWT.MeasureItem, new Listener() {
        	public void handleEvent(Event event) {
    			((DesignMenuModel.Item) ((TreeItem)event.item).getData()).measureItem (event);
        	}
        });
        
        tree.addListener(SWT.PaintItem, new Listener() {
        	   public void handleEvent(Event event) {
         	      TreeItem item = (TreeItem)event.item;
         	     ((DesignMenuModel.Item) item.getData()).showStateIcon(event,tree,item);
        	   }
        	});
        makeActions();
        hookContextMenu();
/**+       hookDoubleClickAction(); */
        contributeToActionBars();

        if (memento != null)
            restoreState(memento);
        memento = null;
        
        tree.addListener(SWT.KeyUp, new Listener(){
            @Override
            public void handleEvent(Event event) {
            	if (event.keyCode == SWT.SHIFT) {
            		toolSequence.setShiftPressed(false);
            	}
            }
        });
        tree.addListener(SWT.KeyDown, new Listener(){
            @Override
            public void handleEvent(Event event) {
            	if (event.keyCode == SWT.SHIFT) {
            		toolSequence.setShiftPressed(true);
            	}
            }
        });

        
    } // createPartControl()

    private void doLoadDesignMenu(IProject newProject) {
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
			System.out.println("##### doLoadDesignMenu("+newProject+"): current project="+selectedResource.getProject().toString());
		}
		if (newProject!=null){
			restoreCurrentState(newProject);
    		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
    			System.out.println("Restored new project: "+newProject.toString());
   	   	    IResource HDLFile=SelectedResourceManager.getDefault().getChosenVerilogFile();
   	   	    if ((HDLFile!=null) && (toolSequence!=null)){
   	   	    	if (VerilogUtils.existsVeditorOutlineDatabase(newProject)){
   	   	    		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
   	   	    			System.out.println("VEditor DB for "+newProject+" already exist");
   	   	    		toolSequence.setUnfinishedBoot(null,false);
   	   	    		finalizeAfterVEditorDB(memento);
   	   	    	} else {
   	   	    		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
   	   	    			System.out.println("Initiating DB rebuild HDLFile="+HDLFile);
   	   	    		toolSequence.setUnfinishedBoot(null,true);
   	   	    		VerilogUtils.getTopModuleNames((IFile) HDLFile); // will initiate DB rebuild, updateDirty and call doLoadDesignMenu();
   	   	    	}
   	   	    	return; 
   	   	    }
		}
		doLoadDesignMenu();
    }

    private void doLoadDesignMenu() {
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
			System.out.println("#### doLoadDesignMenu(): project="+selectedResource.getProject().toString());
		}
        String menuName = selectedResource == null
                        ? null
                        : OptionsCore.doLoadOption(desigMenuName, selectedResource.getProject()); 
        doLoadDesignMenu(menuName);
        
    }

    
    private void doLoadDesignMenu(String menuName) {
        if (menuName != null) { // Horizontal menu bar
            DesignMenu designMenu = ToolsCore.getDesignMenuManager().findDesignMenu(menuName);
            viewer.setInput(designMenu);
            List<Context> packages = ToolsCore.getDesignMenuManager().getPackageContexts(designMenu);
            showPackagePropertiesToolbarAction.setContexts(packages);
            clearPackagePropertiesAction.setContexts(packages); // toolBarSeparator already null here
            List<Context> projects = ToolsCore.getDesignMenuManager().getProjectContexts(designMenu);
            showProjectPropertiesToolbarAction.setContexts(projects);
            clearProjectPropertiesAction.setContexts(projects);
            setPartName(designMenu.getLabel());
            setTitleToolTip(designMenu.getDescription());
        } else {
            viewer.setInput(null);
            showPackagePropertiesToolbarAction.setContexts(null);
            showProjectPropertiesToolbarAction.setContexts(null);
            setPartName(defaultPartName);
            setTitleToolTip(null);
        }
    }
    
    public void changeMenuTitle(String title){
    	setPartName(title);
    }
    public String getMenuTitle(){
    	return getPartName();
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                DesignFlowView.this.fillContextMenu(manager);  // context (right-click) menu
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    
    //
    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());   // rightmost pull-down 
        fillLocalToolBar(bars.getToolBarManager()); // horizontal bar
    }

    private void fillLocalPullDown(IMenuManager manager) { //rightmost pull-down
        manager.add(clearInstallationPropertiesAction);
        manager.add(clearPackagePropertiesAction);
        manager.add(clearProjectPropertiesAction);
        manager.add(clearToolPropertiesAction);
        manager.add(new Separator());
        manager.add(clearToolStatesAction);
        manager.add(clearStateFilesAction);
        manager.add(clearLogFilesAction);
        manager.add(new Separator());
        manager.add(selectDesignMenuAction);
    }
    
    private void fillContextMenu(IMenuManager manager) { // context (right-click) menu
    	// Always come here after setting launchActions, so just add all launchActions here
    	/**+       manager.add(launchAction); */
    	if (launchActions!=null) { 
    		for (Action action:launchActions){
    			manager.add(action); // No Separator??
    		}
    	}
   	
//    	System.out.println("fillContextMenu(), launchActions="+launchActions);
        //      manager.add(new Separator());
//      drillDownAdapter.addNavigationActions(manager);
      // Other plug-ins can contribute their actions here
    	
    	if (pinAction!=null){
    		manager.add(new Separator());
    		manager.add(pinAction);
    	}    	
    	if ((restoreAction!=null) || (restoreSelectAction!=null) ) {
    		manager.add(new Separator()); 
    		if (restoreAction!=null) manager.add(restoreAction);
    		if (restoreSelectAction!=null) manager.add(restoreSelectAction);
    	}    	
    	if ((playbackLogLatestAction!=null) || (playbackLogSelectAction!=null) ) {
    		manager.add(new Separator()); 
    		if (playbackLogLatestAction!=null) manager.add(playbackLogLatestAction);
    		if (playbackLogSelectAction!=null) manager.add(playbackLogSelectAction);
    	}
        manager.add(new Separator()); 
        manager.add(showInstallationPropertiesAction);
        manager.add(showPackagePropertiesAction);
        manager.add(showProjectAction);
        manager.add(showPropertiesAction);
//        manager.add(showLaunchConfigAction);
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }
        
    private void fillLocalToolBar(IToolBarManager manager) { // On Horizontal bar
/**+               manager.add(launchAction); */
     	if (launchActions!=null) { 
    		for (Action action:launchActions){
    			manager.add(action); // No Separator??
    		}
    	}
//    	System.out.println("fillLocalToolBar(), launchActions="+launchActions);
   	 
//        manager.add(launchAction);  // test
//      manager.add(new Separator());
//      drillDownAdapter.addNavigationActions(manager);
        manager.add(new Separator("toolbar-separator"));
        manager.add(toggleLinkedTools);
        manager.add(toggleSaveTools);
        manager.add(toggleStopTools);
        manager.add(new Separator());
        manager.add(showInstallationPropertiesAction);
        manager.add(showPackagePropertiesToolbarAction);
        manager.add(showProjectPropertiesToolbarAction);
        manager.add(showPropertiesAction);
        manager.update(false); // (force) - added new, but removed project, tool and clear/change menu for Icarus (kept the same number of items) Need to update higher menu
        getViewSite().getActionBars().updateActionBars();
    }
    
    private void makeActions() {
    	final DesignFlowView fDesignFlowView=this;
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
			System.out.println("makeActions()");
		}
		
	    toggleLinkedTools= new Action("Toggle tool dependency", Action.AS_CHECK_BOX) {
            public void run() {
//            	System.out.println("isChecked()="+isChecked());
            	SelectedResourceManager.getDefault().setToolsLinked(!isChecked());
            	if (isChecked()){
            		toggleLinkedTools.setImageDescriptor(VDTPluginImages.DESC_TOOLS_UNLINKED);
            	} else {
            		toggleLinkedTools.setImageDescriptor(VDTPluginImages.DESC_TOOLS_LINKED);
            	}
//            	toolSequence.setToolsDirtyFlag(false); //boolean update) - recalculate dirty flags
            	fDesignFlowView.updateLaunchAction(true); // will call toolSequence.setToolsDirtyFlag(false)
            }
        };
        toggleLinkedTools.setToolTipText("Toggle tool dependency");
        toggleLinkedTools.setImageDescriptor(VDTPluginImages.DESC_TOOLS_LINKED);
        toggleLinkedTools.setChecked(!SelectedResourceManager.getDefault().isToolsLinked()); // normally happens before reading memento
        

        toggleSaveTools= new Action("Save tool state", Action.AS_CHECK_BOX) {
            public void run() {
            	toolSequence.setSave(isChecked());
            }
        };
        toggleSaveTools.setToolTipText("Save tool state");
        toggleSaveTools.setImageDescriptor(VDTPluginImages.DESC_TOOLS_SAVE);
        
        //isSaveEnabled()
        

        toggleStopTools= new Action("Stop tools", Action.AS_CHECK_BOX) {
            public void run() {
            	toolSequence.setStop(isChecked());
            }
        };
        toggleStopTools.setToolTipText("Request tool sequence stop (when convenient), with <SHFT> - mark stopped (for debug)");
        toggleStopTools.setImageDescriptor(VDTPluginImages.DESC_TOOLS_STOP);
         
		
        showInstallationPropertiesAction = new Action() {
            public void run() {
                if (openInstallationPropertiesDialog()==Window.OK){
            		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
            			System.out.println("openInstallationPropertiesDialog()-> OK");
            		}
                	fDesignFlowView.updateLaunchAction(true);
                }; 
            }
        };
        showInstallationPropertiesAction.setText("Installation Parameters");
        showInstallationPropertiesAction.setToolTipText("Set installation parameters");
        showInstallationPropertiesAction.setImageDescriptor(VDTPluginImages.DESC_INSTALL_PROPERTIES);
        showInstallationPropertiesAction.setEnabled(ToolsCore.getInstallationContext() != null);

        clearInstallationPropertiesAction = new ClearAction("Do you wish to delete values of installation parameters?") {
            public void clear() {
                SetupOptionsManager.clear();    
            }
        };
        clearInstallationPropertiesAction.setText("Clear Installation Parameters");
        clearInstallationPropertiesAction.setImageDescriptor(VDTPluginImages.DESC_INSTALL_PROPERTIES);
        clearInstallationPropertiesAction.setEnabled(ToolsCore.getInstallationContext() != null);
        
        
        showPackagePropertiesToolbarAction = new GlobalContextsAction("Package Parameters",fDesignFlowView);
        showPackagePropertiesToolbarAction.setText("Package Parameters");
        showPackagePropertiesToolbarAction.setToolTipText("Set package parameters for this tool");
        showPackagePropertiesToolbarAction.setImageDescriptor(VDTPluginImages.DESC_PACKAGE_PROPERTIES);
        
        showPackagePropertiesAction = new Action() {
            public void run() {
            	if (GlobalContextsAction.openDialog( "Package Parameters"
                                               , selectedItem.getPackageContext() )==Window.OK){
            		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
            			System.out.println("GlobalContextsAction.openDialog()-> OK");
            		}
                	fDesignFlowView.updateLaunchAction(); // is it already updated?
                }; 
            }
        };
        showPackagePropertiesAction.setText("Package Parameters");
        showPackagePropertiesAction.setToolTipText("Set package parameters");
        showPackagePropertiesAction.setImageDescriptor(VDTPluginImages.DESC_PACKAGE_PROPERTIES);

        clearPackagePropertiesAction = new ClearContextAction("Do you wish to delete values of packages parameters?");
        clearPackagePropertiesAction.setText("Clear Package Parameters");
        clearPackagePropertiesAction.setImageDescriptor(VDTPluginImages.DESC_PACKAGE_PROPERTIES);

        
        showProjectPropertiesToolbarAction = new LocalContextsAction("Project Parameters",fDesignFlowView);
        showProjectPropertiesToolbarAction.setText("Project Parameters");
        showProjectPropertiesToolbarAction.setToolTipText("Set project parameters (toolbar)");
        showProjectPropertiesToolbarAction.setImageDescriptor(VDTPluginImages.DESC_PROJECT_PROPERTIES);

        showProjectAction = new Action() {
            public void run() {
            	if (LocalContextsAction.openDialog( "Project Parameters"
                                              , selectedItem.getProjectContext() 
                                              , selectedResource.getProject() )==Window.OK){
            		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
            			System.out.println("LocalContextsAction.openDialog()-> OK");
            		}
                	fDesignFlowView.updateLaunchAction(); // is it already updated?
                }; 
            }
        };
        showProjectAction.setText("Project Parameters");
        showProjectAction.setToolTipText("Set project parameters (context menue)");
        showProjectAction.setImageDescriptor(VDTPluginImages.DESC_PROJECT_PROPERTIES);
        
        clearProjectPropertiesAction = new ClearLocalContextAction("Do you wish to delete values of projects parameters?");
        clearProjectPropertiesAction.setText("Clear Project Parameters");
        clearProjectPropertiesAction.setImageDescriptor(VDTPluginImages.DESC_PROJECT_PROPERTIES);

        
        showPropertiesAction = new Action() {
            public void run() {
            	if (openToolPropertiesDialog(selectedItem)==Window.OK){
            		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
            			System.out.println("openToolPropertiesDialog()-> OK");
            		}
                	fDesignFlowView.updateLaunchAction(true);
                };  
//              ConsoleView.getDefault().println("Action 1 executed", ConsoleView.MSG_INFORMATION);                
            }
        };
        showPropertiesAction.setText("Tool Parameters");
        showPropertiesAction.setToolTipText("Set tool parameters");
        showPropertiesAction.setImageDescriptor(VDTPluginImages.DESC_TOOL_PROPERTIES);

        clearToolPropertiesAction = new ClearAction("Do you wish to delete values of the tool parameters?") {
            public void clear() {
                OptionsCore.doClearContextOptions(selectedItem.getTool(), selectedResource.getProject());
            }
        };
        clearToolPropertiesAction.setText("Clear Tool Parameters");
        clearToolPropertiesAction.setImageDescriptor(VDTPluginImages.DESC_TOOL_PROPERTIES);
        selectDesignMenuAction = new Action() {
            public void run() {
                openDesignMenuSelectionDialog(selectedResource.getProject());
            }
        };
        selectDesignMenuAction.setText("Change Design Menu");
        selectDesignMenuAction.setImageDescriptor(VDTPluginImages.DESC_DESIGM_MENU);

        clearToolStatesAction = new ClearToolStates("Do you wish to reset all tool states (as if they never ran)?",toolSequence);
        clearToolStatesAction.setText("Clear tool states");
        clearToolStatesAction.setImageDescriptor(VDTPluginImages.DESC_DESIGM_MENU);

        clearStateFilesAction = new ClearStateFiles("Do you wisth to remove all state files (snapshots), but the current ones?",toolSequence);
        clearStateFilesAction.setText("Clear all but latest snapshot files");
        clearStateFilesAction.setImageDescriptor(VDTPluginImages.DESC_DESIGM_MENU);

        clearLogFilesAction = new ClearLogFiles("Do you wisth to remove all log files, but the most recent?",toolSequence);
        clearLogFilesAction.setText("Clear all but latest log files");
        clearLogFilesAction.setImageDescriptor(VDTPluginImages.DESC_DESIGM_MENU);
        
        showLaunchConfigAction = new Action() {
            public void run() {
                try {
                    int result = openToolLaunchDialog(
                    		selectedItem,
                    		fDesignFlowView);
            		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
            			System.out.println("Ran openToolLaunchDialog() ->"+result);
            		}
                	fDesignFlowView.updateLaunchAction(true);
                } catch (CoreException e) {
                    MessageUI.error(Txt.s("Action.OpenLaunchConfigDialog.Error", 
                                          new String[] {selectedItem.getLabel(), e.getMessage()}),
                                    e);
                }    
            }
        };
        showLaunchConfigAction.setText("Launch configuration");
        showLaunchConfigAction.setToolTipText("Open launch configuration dialog for this tool");
        showLaunchConfigAction.setImageDescriptor(VDTPluginImages.DESC_LAUNCH_CONFIG);
        launchActions=null;
        doubleClickListener=null;
        
    } // makeActions()

    private void removeDoubleClickAction() {
    	if (doubleClickListener!=null){
        	viewer.removeDoubleClickListener(doubleClickListener);
        	doubleClickListener=null;
    	}
    }
    private void hookDoubleClickAction() {
    	if ((launchActions==null) || (launchActions[0]==null)) return;
    	doubleClickListener=new IDoubleClickListener() {
    		public void doubleClick(DoubleClickEvent event) {
    			launchActions[0].run();  // Andrey: will go to launchAction[0].run
    		}
    	};
    	viewer.addDoubleClickListener(doubleClickListener);
    }
    
//    private void showMessage(String message) {
//        MessageDialog.openInformation( viewer.getControl().getShell()
//                                     , "Design Flow"
//                                     , message );
//    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

//    private IWorkbenchPart getActivePart() {
//        IWorkbenchWindow window= getSite().getWorkbenchWindow();
//        IPartService service= window.getPartService();
//        return service.getActivePart();
//    }
    
    // ------------------------------------------------------------------------
    //                    Design Flow logic 
    // ------------------------------------------------------------------------

    /* Method declared on ISelectionListener */
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
			System.out.println(">>>>> DesignFlowView.selectionChanged(, new selection)="+selection+" selectedResource="+selectedResource);
        IResource oldSelection = selectedResource; 
     // Save old project if it changed, before doin anything else
        IProject oldProject= (selectedResource ==null)? null : selectedResource.getProject();
        IResource newSelectedResource=SelectedResourceManager.getDefault().getSelectedResource(part, selection);
        IProject newProject= (newSelectedResource ==null)? null : newSelectedResource.getProject();
//        	if (HDLFile.getProject().getFullPath().toPortableString().equals(project.getFullPath().toPortableString())){
        
        if ((oldProject != null) && 
        		((newProject == null) ||
        		!oldProject.getFullPath().toPortableString().equals(newProject.getFullPath().toPortableString()))){
		    saveState(oldProject);
        }
        selectedResource = SelectedResourceManager.getDefault().getSelectedResource(part, selection);
//        IProject newProject = selectedResource == null 
//                            ? null
//                            : selectedResource.getProject();
        
        if ( (oldSelection == null) || 
             (newProject != oldSelection.getProject())
           ) {
            doLoadDesignMenu(newProject);
        }
        updateLaunchAction();
    } // selectionChanged()
    // Made it public to call from ContexOptionsDialog.okPressed() as launch actions might change
//    private void updateLaunchAction() {
    public void updateLaunchAction() {
    	updateLaunchAction(false);
    }

    public void updateLaunchAction(boolean updateDirty) {
    	if (updateDirty){
    		toolSequence.setToolsDirtyFlag(false); // if true - recalculates parameters
    	}

//    	System.out.println("DesignFlowView.updateLaunchAction()");

        IProject project = selectedResource == null 
                         ? null
                         : selectedResource.getProject();
        showProjectPropertiesToolbarAction.setProject(project);
        clearProjectPropertiesAction.setProject(project);

     // Selected item should be not null, but resource - may be        
     // RunFor[] getMenuActions()
        RunFor [] runFor=null;
        Tool tool=null;
        String ignoreFilter=null;
        if (selectedItem != null){
        	tool= selectedItem.getTool();
        	if (tool!=null){
        		runFor=tool.getMenuActions(project);
        		ignoreFilter=tool.getIgnoreFilter(); // should be after getMenuActions(project) that recalculates parameters
        		tool.initIcons(false); // if not done before - add icons list for actions
        		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
        			System.out.println("Got Runfor["+((runFor!=null)?runFor.length:"null")+"]");
        			if (runFor!=null){
        				for (int i=0;i<runFor.length;i++){
        					System.out.println(
        							"    label='"+runFor[i].getLabel()+
        							"', resource='"+runFor[i].getResource()+
        							"', checkExtension='"+runFor[i].getCheckExtension()+
        							"', checkExistence='"+runFor[i].getCheckExistence()+
        							"'");
        				}
        			}
        		}
        	}
        }
        final Tool fTool=tool;
        boolean enabled = (selectedItem != null) // At startup null (twice went through this); Right Click - "Icarus Ver..."
                       && (selectedResource != null) // at startup x353_1.tf;  Right Click - "L/x353/x353_1.tf
                       && (selectedItem.isEnabled(selectedResource));
        
 // Deal with new menus
//        removeLaunchActions(); 
        removeDoubleClickAction();
        launchActions=null;
        playbackLogLatestAction=null;
        playbackLogSelectAction=null;
        pinAction=null;
        restoreAction=null;
        restoreSelectAction=null;

        if ((runFor!=null) && (project!=null)){
        	launchActions=new Action [runFor.length];
        	for (int i=0;i<runFor.length;i++){
//                String name=runFor[i].getResource();
                String name=SelectedResourceManager.getDefault().tryRelativePath(runFor[i].getResource());
//                String shortName=name;
                String shortName=runFor[i].getResource(); // as entered
                String fullPath=name;
                enabled=(selectedItem != null);
                if (enabled && runFor[i].getCheckExistence()){
                	IPath path = new Path(name);
                	IFile file = null;
                	if (path!=null) {
                		try {
                			file = project.getFile(path);
                		} catch (IllegalArgumentException e) {

                		}
                	}
                	
                	if (file==null){
//                		System.out.println(name+" does not exist");
                		enabled=false;
                	} else {
                		shortName=file.getName();
//                		fullPath=file.getFullPath().toString(); // What is different?
                		fullPath=file.getLocation().toOSString(); // that matches generators
                		
                	}
                }
                if (enabled && runFor[i].getCheckExtension()){
                	enabled= selectedItem.isEnabled(name);
                	if (enabled && !runFor[i].getCheckExistence()) { // try to get resource and full path name, but no error if it fails
                    	IPath path = new Path(name);
                    	IFile file = (path==null)?null:project.getFile(path);
                    	if (file!=null){
                    		shortName=file.getName();
//                    		fullPath=file.getFullPath().toString(); // What is different?
                    		fullPath=file.getLocation().toOSString(); // that matches generators
                    	}
                	}
                }
        		final int finalI=i;
        		final String fFullPath=fullPath;
                final String fIgnoreFilter=ignoreFilter;
//                final DesignFlowView fDesignFlowView=this;

                launchActions[i] = new Action() {
                    public void run() {
                        try {
                            launchTool(
                            		fTool, // tool, will get 
                            		TOOL_MODE.RUN,
                            		finalI,
                            		fFullPath,
                            		fIgnoreFilter);
                        } catch (Exception e) {
                            MessageUI.error( Txt.s("Action.ToolLaunch.Error", 
                                                   new String[] {selectedItem.getLabel(), e.getMessage()})
                                            , e);
                        }    
                    }
                };
                launchActions[i].setToolTipText(i+": "+runFor[i].getLabel()+" "+shortName);
                if (shortName.indexOf("@")>=0){
                    launchActions[i].setText(runFor[i].getLabel()+" "+shortName+"@");
                } else {
                    launchActions[i].setText(runFor[i].getLabel()+" "+shortName);
                }
                launchActions[i].setEnabled(enabled);
        		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
        			System.out.println("shortName="+shortName);
        			System.out.println("launchActions[i].getText()="+launchActions[i].getText());
        		}
                String actionIconKey=tool.getImageKey(i);
                if ((actionIconKey!=null) && (VDTPluginImages.getImageDescriptor(actionIconKey)!=null)) {
                	launchActions[i].setImageDescriptor(VDTPluginImages.getImageDescriptor(actionIconKey));
//                	System.out.println(i+":"+actionIconKey+" - "+ VDTPluginImages.getImageDescriptor(actionIconKey));
                }else
                	launchActions[i].setImageDescriptor(VDTPluginImages.DESC_RUN_TOOL);
                
                if (i==0) { // set log play-back (always for default tool only)
// Add pinned action;                	
                	pinAction=new Action("Toggle tool dependency", Action.AS_CHECK_BOX){
                		public void run(){
                			fTool.setPinned(isChecked());
                		}
                	};
                	pinAction.setText("Pin "+tool.getName());
                	pinAction.setToolTipText("Do not automatically re-run "+tool.getName()+" when its dependency changes");
                	pinAction.setEnabled((tool.getState()==TOOL_STATE.SUCCESS) || (tool.isPinned()));
                	pinAction.setChecked(tool.isPinned());
                	pinAction.setImageDescriptor(VDTPluginImages.DESC_TOOLS_PIN);
                	if (tool.getRestore()!=null){
                		restoreAction=new Action(){
                    		public void run(){
                    			String stateFileName=toolSequence.getSelectedStateFile(fTool, false);
                    			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) { 
                    				System.out.println("*** Will restore latest state of "+fTool.getName());
                    				System.out.println("***Selected restore file: "+stateFileName);
                    			}
                    			Tool restoreTool=fTool.getRestore();
                    			if ((stateFileName!=null) && (restoreTool!=null)){
                    				restoreTool.setResultFile(stateFileName);
                                    try {
                                        launchTool(
                                        		restoreTool,
                                        		TOOL_MODE.RESTORE,
                                        		0,
                                        		fFullPath,
                                        		fIgnoreFilter);
                                    } catch (Exception e) {
                                    	MessageUI.error( Txt.s("Action.ToolLaunch.Error", 
                                    			new String[] {fTool.getName()+" -> "+
                                    					restoreTool.getName() , e.getMessage()}), e);
                                    }    
                    				
                    			}
                    			
                    		}
                    	};
                    	restoreAction.setText("Restote latest "+tool.getName());
                    	restoreAction.setToolTipText("Restore state of the latest successful run of "+tool.getName());
                    	restoreAction.setEnabled(tool.getRestore()!=null); // just to decide should it be removed or disabled
                    	restoreAction.setImageDescriptor(VDTPluginImages.DESC_TOOLS_RESTORE);

                    	restoreSelectAction=new Action(){
                    		public void run(){
                    			System.out.println("*** Will restore selected state of "+fTool.getName());
                    			String stateFileName=toolSequence.getSelectedStateFile(fTool, true);
                    			System.out.println("***Selected restore file: "+stateFileName);
                    			Tool restoreTool=fTool.getRestore();
                    			if ((stateFileName!=null) && (restoreTool!=null)){
                    				restoreTool.setResultFile(stateFileName);
                                    try {
                                        launchTool(
                                        		restoreTool,
                                        		TOOL_MODE.RESTORE,
                                        		0,
                                        		fFullPath,
                                        		fIgnoreFilter);
                                    } catch (Exception e) {
                                    	MessageUI.error( Txt.s("Action.ToolLaunch.Error", 
                                    			new String[] {fTool.getName()+" -> "+
                                    					restoreTool.getName() , e.getMessage()}), e);
                                    }    
                    			}
                    		}
                    	};
                    	restoreSelectAction.setText("Restote selected "+tool.getName());
                    	restoreSelectAction.setToolTipText("Restore state of the selected successful run of "+tool.getName());
                    	restoreSelectAction.setEnabled(tool.getRestore()!=null); // just to decide should it be removed or disabled
                    	restoreSelectAction.setImageDescriptor(VDTPluginImages.DESC_TOOLS_RESTORE_SELECT);
                	}
                	boolean logEnabled=(tool.getLogDir()!=null);
// see if tool has log-dir                	
                	playbackLogLatestAction=new Action() {
                        public void run() {
                            try {
                            	playLogs(
                                		false,
                                		fFullPath,
                                		fIgnoreFilter);
                            } catch (Exception e) {
                                MessageUI.error( Txt.s("Action.ToolLaunch.Error", 
                                                       new String[] {selectedItem.getLabel(), e.getMessage()})
                                                , e);
                            }    
                        }
                    };
                    playbackLogSelectAction=new Action() {
                        public void run() {
                            try {
                            	playLogs(
                                		true,
                                		fFullPath,
                                		fIgnoreFilter);
                            } catch (Exception e) {
                                MessageUI.error( Txt.s("Action.ToolLaunch.Error", 
                                                       new String[] {selectedItem.getLabel(), e.getMessage()})
                                                , e);
                            }    
                        }
                    };
                    playbackLogLatestAction.setText("Playback latest log for "+tool.getName());
                    playbackLogSelectAction.setText("Select/playback log for "+tool.getName());
                    playbackLogLatestAction.setToolTipText("Playback latest log for "+runFor[i].getLabel()+" "+shortName);
                    playbackLogSelectAction.setToolTipText("Select and playback log for "+runFor[i].getLabel()+" "+shortName);
                    playbackLogLatestAction.setEnabled(logEnabled);
                    playbackLogSelectAction.setEnabled(logEnabled);
                    playbackLogLatestAction.setImageDescriptor(VDTPluginImages.DESC_PLAY_BACK);
                    playbackLogSelectAction.setImageDescriptor(VDTPluginImages.DESC_PLAY_BACK_SELECT);
                }
        	}
            IToolBarManager toolbarManager= getViewSite().getActionBars().getToolBarManager();
            toolbarManager.removeAll();
            fillLocalToolBar(toolbarManager);
        	hookDoubleClickAction();
        }

        enabled = (selectedItem != null)
               && (selectedResource != null)
               && (selectedItem.getPackageContext() != null);
        showPackagePropertiesAction.setEnabled(enabled);
        clearPackagePropertiesAction.setEnabled(enabled);
        if (enabled){
            String projectName = project.getName(); 
            showPackagePropertiesAction.setText(Txt.s("Action.PackageProperties.Caption", new String[]{selectedItem.getLabel()}));
            showPackagePropertiesAction.setToolTipText(Txt.s("Action.PackageProperties.ToolTip", new String[]{selectedItem.getLabel(), projectName}));
        } else {
            showPackagePropertiesAction.setText(Txt.s("Action.PackageProperties.Caption.Default"));
            showPackagePropertiesAction.setToolTipText(Txt.s("Action.PackageProperties.ToolTip.Default"));
        }
        
        enabled = (selectedItem != null)
               && (selectedResource != null)
               && (selectedItem.getProjectContext() != null);
        showProjectAction.setEnabled(enabled);
        clearProjectPropertiesAction.setEnabled(enabled);
        if (enabled){
            String projectName = project.getName(); 
            showProjectAction.setText(Txt.s("Action.ProjectProperties.Caption", new String[]{selectedItem.getLabel()}));
            showProjectAction.setToolTipText(Txt.s("Action.ProjectProperties.ToolTip", new String[]{selectedItem.getLabel(), projectName}));
        } else {
            showProjectAction.setText(Txt.s("Action.ProjectProperties.Caption.Default"));
            showProjectAction.setToolTipText(Txt.s("Action.ProjectProperties.ToolTip.Default"));
        }
        
        enabled = (selectedItem != null)
               && (selectedItem.getTool() != null);
        showPropertiesAction.setEnabled(enabled);
        clearToolPropertiesAction.setEnabled(enabled);
        
        toggleSaveTools.setEnabled(toolSequence.isSaveEnabled());
        
        ((ViewLabelProvider) viewer.getLabelProvider()).fireChanged();
    } // updateLaunchAction()

    private void launchTool(
    		Tool tool,
    		TOOL_MODE mode,
    		int choice,
    		String fullPath,
    		String ignoreFilter) throws CoreException {
    	if (tool != null) {
    		toolSequence.launchToolSequence(tool,mode, choice, fullPath, ignoreFilter);
    	} else if (selectedItem.hasChildren()) {
    		if (viewer.getExpandedState(selectedItem))
    			viewer.collapseToLevel(selectedItem, AbstractTreeViewer.ALL_LEVELS);
    		else    
    			viewer.expandToLevel(selectedItem, 1);
    	}
    } // launchTool()
    
    private void playLogs(
    		boolean select, // select log file
    		String fullPath,
    		String ignoreFilter) throws CoreException {
        Tool tool = selectedItem.getTool();
        if (tool != null) {
        	String logBuildStamp=select?selectBuildStamp(tool):""; // "" - latest (link)
        	if (logBuildStamp==null) return; // cancelled selection
    		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
    			System.out.println("logBuildStamp="+logBuildStamp);
    		}
        	toolSequence.playLogs(
        			tool,
            		fullPath,
            		logBuildStamp);
        } else if (selectedItem.hasChildren()) {
            if (viewer.getExpandedState(selectedItem))
                viewer.collapseToLevel(selectedItem, AbstractTreeViewer.ALL_LEVELS);
            else    
                viewer.expandToLevel(selectedItem, 1);
        }
    } // launchTool()
    
    private String selectBuildStamp(Tool tool){
//    	System.out.println("tool.getLogDir()="+tool.getLogDir());
//    	System.out.println("ToolLogFile.getDir(tool.getLogDir()).getLocation()="+ToolLogFile.getDir(tool.getLogDir()).getLocation());
    	FilteredFileSelector selector= new FilteredFileSelector(
    			ToolLogFile.getDir(tool.getLogDir()).getLocation().toFile() , //File dir,
    			"Select log file", //String title,
    			 null, // Component parent, or convert from SHell VerilogPlugin.getActiveWorkbenchShell()
    			"Select", //String approveText,
    			"Select timestamp by selecting available log file", //String approveToolTip,
    			ToolLogFile.getBaseRegex(tool.getName()), // String filterRegex,
    			"Matchig log files for "+tool.getName(), //String filterDescription,
    			false //boolean allowDirs
    			);
    	File result=selector.openDialog();
    	if (result == null) {
    		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
    			System.out.println("Selection canceled");
    		return null;
    	}
//    	System.out.println("Got file "+result.toString());
//    	System.out.println("Timestamp="+ToolLogFile.getTimeStamp(
//    			tool.getName(),
//    			result.getName()));
    	return ToolLogFile.getTimeStamp(
    			tool.getName(),
    			result.getName());
    }
    
    
    private int openInstallationPropertiesDialog() {
        Shell shell = VerilogPlugin.getActiveWorkbenchShell();
        SetupOptionsDialog dialog = new SetupOptionsDialog(shell);
//        ContextOptionsDialog dialog = new ContextOptionsDialog(shell, ToolsCore.getContextManager().getInstallationContext());
        dialog.setTitle("Instalation Parameters");
        dialog.create();
        return dialog.open();
        
    } // openInstallationPropertiesDialog()

    
    private int openToolPropertiesDialog(DesignMenuModel.Item item) {
        Shell shell = VerilogPlugin.getActiveWorkbenchShell();
        Context context = item.getTool();
        ContextOptionsDialog dialog = new ContextOptionsDialog( shell
                                                              , context 
                                                              , selectedResource.getProject() );
        dialog.setTitle("Tool Parameters");
        dialog.create();
        return dialog.open();
    } // openToolPropertiesDialog()

    private int openDesignMenuSelectionDialog(IProject project) {
        Shell shell = VerilogPlugin.getActiveWorkbenchShell();
        DesignMenuSelectionDialog dialog = new DesignMenuSelectionDialog( shell
                                                                        , desigMenuName.getValue() ); 
        dialog.create();
        int result=dialog.open();
        if (result == Window.OK) {
 //       	System.out.println("openDesignMenuSelectionDialog()-> OK");
            DesignMenu newDesignMenu = dialog.getSelectedDesignMenu();
            String newDesignMenuName = newDesignMenu == null ? null
                                                             : newDesignMenu.getName();
            desigMenuName.setValue(newDesignMenuName); // ??? Andrey
            OptionsCore.doStoreOption(desigMenuName, project);
            doLoadDesignMenu(newDesignMenuName);
        }
        return result;
    }


    private int openToolLaunchDialog(
    		DesignMenuModel.Item item,
    		DesignFlowView designFlowView
    		) throws CoreException {
    	System.out.println("openToolLaunchDialog()");

        Shell shell = VerilogPlugin.getActiveWorkbenchShell();
        ILaunchConfiguration launchConfig = LaunchCore.createLaunchConfiguration(
        		item.getTool(),
        		selectedResource.getProject(),
        		null,
        		null);
        IStructuredSelection selection = new StructuredSelection(launchConfig);
        return DebugUITools.openLaunchConfigurationDialogOnGroup( shell
                                                         , selection
                                                         , VDT.ID_VERILOG_TOOLS_LAUNCH_GROUP );                        
    } // openToolLaunchDialog()
    
    
    // ------------------------------------------------------------------------
    //                      Tree of the tools 
    // ------------------------------------------------------------------------

    class ToolSelectionChangedListener implements ISelectionChangedListener {
        
        public void selectionChanged(SelectionChangedEvent event) {
            ISelection selection = event.getSelection();
            Object obj = ((IStructuredSelection)selection).getFirstElement();
            selectedItem = (DesignMenuModel.Item) obj;
            if (selectedResource == null)
//                selectedResource = SelectedResourceManager.getDefault().getViewSelectedResource(IPageLayout.ID_RES_NAV);
            	selectedResource = SelectedResourceManager.getDefault().getViewSelectedResource(IPageLayout.ID_PROJECT_EXPLORER);
            //
            updateLaunchAction();
        }    
    } // class ToolSelectionChangedListener
    
    
    // ------------------------------------------------------------------------
    /*
     * The content provider class is responsible for providing objects to 
     * the view. It can wrap existing objects in adapters or simply return
     * objects as-is. These objects may be sensitive to the current input 
     * of the view, or ignore it and always show the same content 
     * (like Task List, for example).
     */
    class ViewContentProvider implements IStructuredContentProvider 
                                       , ITreeContentProvider {
        private DesignMenuModel invisibleRoot;
        private DesignMenu designMenu;

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
            invisibleRoot = null;
            if ((newInput != null) && (newInput instanceof DesignMenu))
                designMenu = (DesignMenu) newInput;
            else 
                designMenu = null;
            v.refresh();
        }
        
        public void dispose() { }
  
        public Object[] getElements(Object parent) {
//            if (parent.equals(getViewSite())) {
            if (parent == designMenu) {
                if (invisibleRoot == null) 
                    initialize();
                return getChildren(invisibleRoot);
            }
            return getChildren(parent);
        }
        public Object getParent(Object child) {
            if (child instanceof DesignMenuModel.Item) {
                return ((DesignMenuModel.Item)child).getParent();
            }
            return null;
        }
        public Object [] getChildren(Object parent) {
            if (parent == null) {
                return new Object[0];
            } else if (parent == invisibleRoot) {
                return invisibleRoot.getItems();
            } else if (parent instanceof DesignMenuModel.Item) {
                return ((DesignMenuModel.Item)parent).getChildren();
            }
            return new Object[0];
        }
        public boolean hasChildren(Object parent) {
            if (parent == null) {
                return false;
            } else if (parent == invisibleRoot) {
                return invisibleRoot.getItems().length > 0;
            } else if (parent instanceof DesignMenuModel.Item) {
                return ((DesignMenuModel.Item)parent).hasChildren();
            }    
            return false;
        }

        private void initialize() {
            if (designMenu != null)
                invisibleRoot = new DesignMenuModel(designMenu);
        }
    } // class ViewContentProvider
    
    //-------------------------------------------------------------------------
    class ViewLabelProvider extends LabelProvider {
    	
    	public void fireChanged(){
    		fireLabelProviderChanged(new LabelProviderChangedEvent(this));
    	}

        public String getText(Object obj) {
            return obj.toString();
        }
        

        public Image getImage(Object obj) {
            String imageKey = ((DesignMenuModel.Item)obj).getImageKey();
            if (imageKey != null) {
                return VDTPluginImages.getImage(imageKey);
            } else {
                imageKey = ISharedImages.IMG_OBJ_ELEMENT;
                if (((DesignMenuModel.Item)obj).hasChildren())
                   imageKey = ISharedImages.IMG_OBJ_FOLDER;
                return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
            }    
        }
    } // class ViewLabelProvider

    class NameSorter extends ViewerSorter {
    }

    /**
     * Restores the state of the receiver to the state described in the specified memento.
     *
     * @param memento the memento
     * @since 2.0
     */
    protected void restoreState(IMemento memento) {
        SelectedResourceManager.getDefault().setToolSequence(toolSequence); // to enable access through static method
    	Boolean linkedTools= memento.getBoolean(TAG_LINKED_TOOLS);
    	ToolsCore.restoreToolsState(memento);
        String location = memento.getString(TAG_SELECTED_RESOURCE);
        if (location == null) {
        	System.out.println("No project selected");
            return;
        }
        selectedResource = ResourcesPlugin.getWorkspace().getRoot().findMember(Path.fromPortableString(location));
        String HDLLocation=memento.getString(TAG_SELECTED_HDL_FILE);
        if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        	System.out.println("restoreState(memento): selectedResource="+selectedResource+ " HDLLocation="+HDLLocation);
        if (HDLLocation!=null) {
        	IResource HDLFile=ResourcesPlugin.getWorkspace().getRoot().findMember(Path.fromPortableString(HDLLocation));
        	if (HDLFile!=null){
        		SelectedResourceManager.getDefault().setChosenVerilogFile(HDLFile);
        		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        			System.out.println("Setting HDL file to "+HDLFile.toString());
        	}
        }
        String HDLFilter=memento.getString(TAG_SELECTED_HDL_FILTER); //SelectedResourceManager.getDefault().getFilter();
        if (HDLFilter!=null){
        	SelectedResourceManager.getDefault().setFilter(HDLFilter);
        	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        		System.out.println("Setting HDL filter to "+HDLFilter);
        }
    	if (linkedTools==null) linkedTools=true;
    	SelectedResourceManager.getDefault().setToolsLinked(linkedTools);
        toggleLinkedTools.setChecked(!SelectedResourceManager.getDefault().isToolsLinked());
        // Initialize VEditor database build
   	    IResource HDLFile=SelectedResourceManager.getDefault().getChosenVerilogFile();
   	    	// restore properties from the project, overwrite global ones
   	    restoreCurrentState(selectedResource.getProject()); // null OK
   	    
   	    if ((HDLFile!=null) && HDLFile.exists()){
   	    	toolSequence.setUnfinishedBoot(memento,true);
        	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        		System.out.println("Initiating DB rebuild HDLFile="+HDLFile);
   	    	VerilogUtils.getTopModuleNames((IFile) HDLFile);
   	    } else {
        	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        		System.out.println("Skipping DB rebuild HDLFile=NULL");
   	    	toolSequence.setUnfinishedBoot(null,false);
   	    	finalizeAfterVEditorDB(memento);
   	    }
    }
    public void finalizeAfterVEditorDB(IMemento memento){
    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
    		System.out.println("finalizeAfterVEditorDB(), memento is "+((memento==null)?"NULL":"not NULL"));
    	if (memento!=null) {
    		toolSequence.restoreCurrentStates(memento); // restore states and recalc "dirty" flags - should be after tools themselves
    	}
        if (selectedResource!=null) toolSequence.restoreCurrentStates(selectedResource.getProject()); // restore states and recalc "dirty" flags - should be after tools themselves
        IResource HDLFile=SelectedResourceManager.getDefault().getChosenVerilogFile();
        if ((HDLFile!=null) && HDLFile.exists()){
        	toolSequence.setToolsDirtyFlag(true); // recalculate each successful tool's parameters - does it trigger Database rebuild?
        }
        doLoadDesignMenu();
        updateLaunchAction(true); // true?
    }
    
    private void restoreCurrentState(IProject project){
    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
    		System.out.println("^^^^ restoreCurrentState("+project+")");
    	if ((project==null) || !project.exists()){
    		System.out.println("Can not restore persistent properties from non-existent project "+project);
    		return;
    	}
    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)){
    		System.out.println("::::: Restoring persistent properties from the project "+project);
    	}

    	ToolsCore.restoreToolsState(project);
    	Map<QualifiedName,String> pp;
    	try {
    		pp=project.getPersistentProperties();
    	} catch (CoreException e){
    		System.out.println(project+": Failed getPersistentProperties(), e="+e);
    		return;
    	}
    	String location=	pp.get(new QualifiedName(VDT.ID_VDT, TAG_SELECTED_RESOURCE));
    	if (location!=null) {
    		IResource resource=ResourcesPlugin.getWorkspace().getRoot().findMember(Path.fromPortableString(location));
        	if (resource.getProject().getFullPath().toPortableString().equals(project.getFullPath().toPortableString())){
        		selectedResource = resource;
        	} else {
        		System.out.println("**** Wrong selected resource "+location+" for project "+project.getFullPath().toPortableString());
        	}
    	}
    	String HDLLocation=	pp.get(new QualifiedName(VDT.ID_VDT, TAG_SELECTED_HDL_FILE));
    	if (HDLLocation!=null) {
            if (HDLLocation!=null) {
            	IResource HDLFile=ResourcesPlugin.getWorkspace().getRoot().findMember(Path.fromPortableString(HDLLocation));
            	if (HDLFile!=null){
            		if (HDLFile.getProject().getFullPath().toPortableString().equals(project.getFullPath().toPortableString())){
            			SelectedResourceManager.getDefault().setChosenVerilogFile(HDLFile);
            			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
            				System.out.println("Setting HDL file to "+HDLFile.toString());
            		} else {
                		System.out.println("*** Wrong HDLFile "+HDLLocation+" for project "+project.getFullPath().toPortableString());
            		}
            	}
            }
    	}
        String HDLFilter= pp.get(new QualifiedName(VDT.ID_VDT, TAG_SELECTED_HDL_FILTER));
        if (HDLFilter!=null){
        	SelectedResourceManager.getDefault().setFilter(HDLFilter);
        	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        		System.out.println("Setting HDL filter to "+HDLFilter);
        }
    	
    	String LinkedToolsStr=pp.get(new QualifiedName(VDT.ID_VDT, TAG_LINKED_TOOLS));
    	if (LinkedToolsStr!=null)	try {
    		SelectedResourceManager.getDefault().setToolsLinked(Boolean.parseBoolean(LinkedToolsStr));
            toggleLinkedTools.setChecked(!SelectedResourceManager.getDefault().isToolsLinked());
    	} catch (Exception e){
    		System.out.println(project+"Failed .setToolsLinked("+LinkedToolsStr+"), e="+e);
    	}
    }
    
    
    
    

    /** 
     * @see ViewPart#saveState
     */
    public void saveState(IMemento memento) {
    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
    		System.out.println("^^^^saveState()");
        if (viewer == null) {
            if (this.memento != null) //Keep the old state;
                memento.putMemento(this.memento);
//            return;
        }
        IProject project = selectedResource == null 
                ? null
                : selectedResource.getProject();
        if (project==null) {
        	System.out.println("No project selected, nothing to save");
        	return;
        }

        if (selectedResource != null) {
            String location = selectedResource.getFullPath().toPortableString();
            memento.putString(TAG_SELECTED_RESOURCE, location);
        	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        		System.out.println("saveState(memento): selectedResource="+selectedResource);
        }
        IResource HDLFile=SelectedResourceManager.getDefault().getChosenVerilogFile();
        if (HDLFile!=null){
        	memento.putString(TAG_SELECTED_HDL_FILE,HDLFile.getFullPath().toPortableString());
        	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        		System.out.println("memento.putString("+TAG_SELECTED_HDL_FILE+","+HDLFile.getFullPath().toPortableString()+")");
        }
        String HDLFilter=SelectedResourceManager.getDefault().getFilter();
        if (HDLFilter!=null){
        	memento.putString(TAG_SELECTED_HDL_FILTER,HDLFilter);
        	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        		System.out.println("memento.putString("+TAG_SELECTED_HDL_FILTER+","+HDLFilter+")");
        }
        memento.putBoolean(TAG_LINKED_TOOLS, new Boolean(SelectedResourceManager.getDefault().isToolsLinked()));
        ToolsCore.saveToolsState(memento);
        if (toolSequence!=null) {
        	toolSequence.saveCurrentStates(memento);
        }
// set project properties  
        
        if ((project!=null) && project.exists())
        	saveState(project); 
    }
// this may need some synchronization as saveState(IMemento memento) is called by the timer (what if selectedResource changes while save is in progress?
    public void saveState(IProject project) {
    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
    		System.out.println("^^^^ SaveState("+project+")");
    	if ((project==null) || !project.exists()){
    		System.out.println("Can not save persistent properties to non-existent project "+project);
    		return;
    	}
    	QualifiedName qn;
        if (selectedResource != null) {
        	if (selectedResource.getProject().getFullPath().toPortableString().equals(project.getFullPath().toPortableString())){
        		String location = selectedResource.getFullPath().toPortableString();
        		qn= new QualifiedName(VDT.ID_VDT, TAG_SELECTED_RESOURCE);
        		try {project.setPersistentProperty(qn, location);}
        		catch (CoreException e)  {System.out.println(project+"Failed setPersistentProperty("+qn+", "+location+", e="+e);}
        	} else {
        		System.out.println("*** Wrong selected resource "+selectedResource+" for project "+project);
        		return;
        	}
        }

        IResource HDLFile=SelectedResourceManager.getDefault().getChosenVerilogFile();
        if (HDLFile!=null){
        	if (HDLFile.getProject().getFullPath().toPortableString().equals(project.getFullPath().toPortableString())){
        		qn= new QualifiedName(VDT.ID_VDT, TAG_SELECTED_HDL_FILE);
        		try {project.setPersistentProperty(qn, HDLFile.getFullPath().toPortableString());}
        		catch (CoreException e)  {System.out.println(project+"Failed setPersistentProperty("+qn+", "+HDLFile.getFullPath().toPortableString()+", e="+e);}
        		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        			System.out.println("project.setPersistentProperty("+qn.toString()+","+HDLFile.getFullPath().toPortableString()+")");
        	} else {
        		System.out.println("*** Wrong HDLFile "+HDLFile+" for project "+project);
        		return;
        	}
        }
        String HDLFilter=SelectedResourceManager.getDefault().getFilter();
        if (HDLFilter!=null){
        	qn= new QualifiedName(VDT.ID_VDT, TAG_SELECTED_HDL_FILTER);
        	try {project.setPersistentProperty(qn, HDLFilter);}
        	catch (CoreException e)  {System.out.println(project+"Failed setPersistentProperty("+qn+", "+HDLFilter+", e="+e);}
        	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        		System.out.println("project.setPersistentProperty("+qn.toString()+","+HDLFilter+")");
        }
        qn= new QualifiedName(VDT.ID_VDT, TAG_LINKED_TOOLS);
        try {project.setPersistentProperty(qn, new Boolean(SelectedResourceManager.getDefault().isToolsLinked()).toString());}
        catch (CoreException e)  {System.out.println(project+"Failed setPersistentProperty("+qn+", "+
        		SelectedResourceManager.getDefault().isToolsLinked()+", e="+e);}
        ToolsCore.saveToolsState(project);
        if (toolSequence!=null) {
        	toolSequence.saveCurrentStates(project);
        }
    }
} // class DesignFlowView