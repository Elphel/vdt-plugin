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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import com.elphel.vdt.core.Utils;
import com.elphel.vdt.core.launching.LaunchCore;
import com.elphel.vdt.core.options.OptionsCore;
import com.elphel.vdt.core.options.ValueBasedOption;
import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.menu.DesignMenu;
import com.elphel.vdt.core.tools.params.Tool;

import com.elphel.vdt.Txt;
import com.elphel.vdt.VDT;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;

import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.VDTPluginImages;

import com.elphel.vdt.ui.variables.SelectedResourceManager;
import com.elphel.vdt.ui.views.DesignMenuModel;
import com.elphel.vdt.ui.dialogs.DesignMenuSelectionDialog;
import com.elphel.vdt.ui.options.ContextOptionsDialog;
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
    private static final String TAG_SELECTED_RESOURCE = "SelectedProject";

    private TreeViewer viewer;
    private DrillDownAdapter drillDownAdapter;
    private Action showLaunchConfigAction;
    private Action launchAction;

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
    
    private IResource selectedResource;
    
    private DesignMenuModel.Item selectedItem;
    
    private ValueBasedOption desigMenuName;
    
    private String defaultPartName;

    private IMemento memento;
    
    /**
     * The constructor.
     */
    public DesignFlowView() {
        desigMenuName = new ValueBasedOption(VDT.OPTION_PROJECT_MENU);
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
        viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        drillDownAdapter = new DrillDownAdapter(viewer);

        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
// more complex sorter is needed        
//        viewer.setSorter(new NameSorter());
//        viewer.setInput(getViewSite());
//        viewer.setInput(ToolsCore.getDesignMenu());
        viewer.addSelectionChangedListener(new ToolSelectionChangedListener());

        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();

        if (memento != null)
            restoreState(memento);
        memento = null;
    } // createPartControl()

    private void doLoadDesignMenu() {
        String menuName = selectedResource == null
                        ? null
                        : OptionsCore.doLoadOption(desigMenuName, selectedResource.getProject()); 
        doLoadDesignMenu(menuName);
    }

    private void doLoadDesignMenu(String menuName) {
        if (menuName != null) {
            DesignMenu designMenu = ToolsCore.getDesignMenuManager().findDesignMenu(menuName);
            viewer.setInput(designMenu);
            List<Context> packages = ToolsCore.getDesignMenuManager().getPackageContexts(designMenu);
            showPackagePropertiesToolbarAction.setContexts(packages);
            clearPackagePropertiesAction.setContexts(packages);
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

        
    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                DesignFlowView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        manager.add(clearInstallationPropertiesAction);
        manager.add(clearPackagePropertiesAction);
        manager.add(clearProjectPropertiesAction);
        manager.add(clearToolPropertiesAction);
        manager.add(new Separator());
        manager.add(selectDesignMenuAction);
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(launchAction);
//      manager.add(new Separator());
//      drillDownAdapter.addNavigationActions(manager);
      // Other plug-ins can contribute there actions here
        manager.add(new Separator());
        manager.add(showInstallationPropertiesAction);
        manager.add(showPackagePropertiesAction);
        manager.add(showProjectAction);
        manager.add(showPropertiesAction);
//        manager.add(showLaunchConfigAction);
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }
        
    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(launchAction);
//      manager.add(new Separator());
//      drillDownAdapter.addNavigationActions(manager);
        manager.add(new Separator());
        manager.add(showInstallationPropertiesAction);
        manager.add(showPackagePropertiesToolbarAction);
        manager.add(showProjectPropertiesToolbarAction);
        manager.add(showPropertiesAction);
//        manager.add(showLaunchConfigAction);
    }

    private void makeActions() {
        showInstallationPropertiesAction = new Action() {
            public void run() {
                openInstallationPropertiesDialog(); 
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
        
        
        showPackagePropertiesToolbarAction = new GlobalContextsAction("Package Parameters");
        showPackagePropertiesToolbarAction.setText("Package Parameters");
        showPackagePropertiesToolbarAction.setToolTipText("Set package parameters for this tool");
        showPackagePropertiesToolbarAction.setImageDescriptor(VDTPluginImages.DESC_PACKAGE_PROPERTIES);
        
        showPackagePropertiesAction = new Action() {
            public void run() {
                GlobalContextsAction.openDialog( "Package Parameters"
                                               , selectedItem.getPackageContext() );
            }
        };
        showPackagePropertiesAction.setText("Package Parameters");
        showPackagePropertiesAction.setToolTipText("Set package parameters");
        showPackagePropertiesAction.setImageDescriptor(VDTPluginImages.DESC_PACKAGE_PROPERTIES);

        clearPackagePropertiesAction = new ClearContextAction("Do you wish to delete values of packages parameters?");
        clearPackagePropertiesAction.setText("Clear Package Parameters");
        clearPackagePropertiesAction.setImageDescriptor(VDTPluginImages.DESC_PACKAGE_PROPERTIES);

        
        showProjectPropertiesToolbarAction = new LocalContextsAction("Project Parameters");
        showProjectPropertiesToolbarAction.setText("Project Parameters");
        showProjectPropertiesToolbarAction.setToolTipText("Set project parameters (toolbar)");
        showProjectPropertiesToolbarAction.setImageDescriptor(VDTPluginImages.DESC_PROJECT_PROPERTIES);

        showProjectAction = new Action() {
            public void run() {
                LocalContextsAction.openDialog( "Project Parameters"
                                              , selectedItem.getProjectContext() 
                                              , selectedResource.getProject() );
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
                openToolPropertiesDialog(selectedItem); 
//              ConsoleView.getDefault().println("Action 1 executed", ConsoleView.MSG_INFORMATION);                
            }
        };
        showPropertiesAction.setText("Tool Parameters");
        showPropertiesAction.setToolTipText("Set tool parameters");
        showPropertiesAction.setImageDescriptor(VDTPluginImages.DESC_TOOL_PROPERTIES);

        clearToolPropertiesAction = new ClearAction("Do you wish to delete values of tool parameters?") {
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
            
        showLaunchConfigAction = new Action() {
            public void run() {
                try {
                    openToolLaunchDialog(selectedItem); 
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

        launchAction = new Action() {
            public void run() {
                try {
                    launchTool(selectedItem);
                } catch (Exception e) {
                    MessageUI.error( Txt.s("Action.ToolLounch.Error", 
                                           new String[] {selectedItem.getLabel(), e.getMessage()})
                                    , e);
                }    
            }
        };
        launchAction.setText(Txt.s("Action.ToolLounch.Caption.Default"));
        launchAction.setToolTipText(Txt.s("Action.ToolLounch.ToolTip.Default"));
        launchAction.setImageDescriptor(VDTPluginImages.DESC_RUN_TOOL);
        launchAction.setEnabled(false);
//        launchAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
//                                        getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    } // makeActions()

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                    launchAction.run();
            }
        });
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
        IResource oldSelection = selectedResource; 
        selectedResource = SelectedResourceManager.getDefault().getSelectedResource(part, selection);
        IProject newProject = selectedResource == null 
                            ? null
                            : selectedResource.getProject();
        
        if ( (oldSelection == null) || 
             (newProject != oldSelection.getProject())
           ) {
            doLoadDesignMenu();
        }
        updateLaunchAction();
    } // selectionChanged()
    
    private void updateLaunchAction() {
        IProject project = selectedResource == null 
                         ? null
                         : selectedResource.getProject();
        showProjectPropertiesToolbarAction.setProject(project);
        clearProjectPropertiesAction.setProject(project);

        boolean enabled = (selectedItem != null) // At startup null (twice went through this); Right Click - "Icarus Ver..."
                       && (selectedResource != null) // at startup x353_1.tf;  Right Click - "L/x353/x353_1.tf
                       && (selectedItem.isEnabled(selectedResource));

        launchAction.setEnabled(enabled);

        
        //Just trying        
        
        
        if (enabled){
            launchAction.setText(Txt.s("Action.ToolLounch.Caption", new String[]{selectedResource.getName()}));
            launchAction.setToolTipText(Txt.s("Action.ToolLounch.ToolTip", new String[]{selectedItem.getLabel(), selectedResource.getName()}));
            Tool tool = selectedItem.getTool();
            if (tool!=null){
            	System.out.println("Tool:"+tool.getName()); // Not yet parsed
            }
            
        } else {
            launchAction.setText(Txt.s("Action.ToolLounch.Caption.Default"));
            launchAction.setToolTipText(Txt.s("Action.ToolLounch.ToolTip.Default"));
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
    } // updateLaunchAction()
    

    private void launchTool(DesignMenuModel.Item item) throws CoreException {
        Tool tool = selectedItem.getTool(); 
        if (tool != null) {
            LaunchCore.launch( tool
                             , selectedResource.getProject()
                             , selectedResource.getFullPath().toString() ); 
        } else if (selectedItem.hasChildren()) {
            if (viewer.getExpandedState(selectedItem))
                viewer.collapseToLevel(selectedItem, AbstractTreeViewer.ALL_LEVELS);
            else    
                viewer.expandToLevel(selectedItem, 1);
        }
    } // launchTool()
    
    private void openInstallationPropertiesDialog() {
        Shell shell = VerilogPlugin.getActiveWorkbenchShell();
        SetupOptionsDialog dialog = new SetupOptionsDialog(shell);
//        ContextOptionsDialog dialog = new ContextOptionsDialog(shell, ToolsCore.getContextManager().getInstallationContext());
        dialog.setTitle("Instalation Parameters");
        dialog.create();
        dialog.open();
    } // openInstallationPropertiesDialog()

    
    private void openToolPropertiesDialog(DesignMenuModel.Item item) {
        Shell shell = VerilogPlugin.getActiveWorkbenchShell();
        Context context = item.getTool();
        ContextOptionsDialog dialog = new ContextOptionsDialog( shell
                                                              , context 
                                                              , selectedResource.getProject() );
        dialog.setTitle("Tool Parameters");
        dialog.create();
        dialog.open();
    } // openToolPropertiesDialog()

    private void openDesignMenuSelectionDialog(IProject project) {
        Shell shell = VerilogPlugin.getActiveWorkbenchShell();
        DesignMenuSelectionDialog dialog = new DesignMenuSelectionDialog( shell
                                                                        , desigMenuName.getValue() ); 
        dialog.create();
        if (dialog.open() == Window.OK) {
            DesignMenu newDesignMenu = dialog.getSelectedDesignMenu();
            String newDesignMenuName = newDesignMenu == null ? null
                                                             : newDesignMenu.getName();
            if (newDesignMenuName != null) {
/*                try {
                    Utils.addNature(VDT.VERILOG_NATURE_ID, project, null);
                    desigMenuName.setValue(newDesignMenuName);
                } catch (CoreException e) {
                    MessageUI.error( "Cannot set " + VDT.VERILOG_NATURE_ID + "nature for " + project.getName() 
                                   , e);
                }
*/                
            } else {
/*                try {
                    Utils.removeNature(VDT.VERILOG_NATURE_ID, project, null);
                    desigMenuName.doClear();
                } catch (CoreException e) {
                    MessageUI.error( "Cannot remove " + VDT.VERILOG_NATURE_ID + "nature for " + project.getName() 
                                   , e);
                }
*/            }
            OptionsCore.doStoreOption(desigMenuName, project);
            doLoadDesignMenu(newDesignMenuName);
        }
    }

    
    private void openToolLaunchDialog(DesignMenuModel.Item item) throws CoreException {
        Shell shell = VerilogPlugin.getActiveWorkbenchShell();
        ILaunchConfiguration launchConfig = LaunchCore.createLaunchConfiguration( item.getTool()
                                                                                , selectedResource.getProject()
                                                                                , null );
        IStructuredSelection selection = new StructuredSelection(launchConfig);
        DebugUITools.openLaunchConfigurationDialogOnGroup( shell
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
                selectedResource = SelectedResourceManager.getDefault().getViewSelectedResource(IPageLayout.ID_RES_NAV);
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
        String location = memento.getString(TAG_SELECTED_RESOURCE);
        if (location == null)
            return;

        selectedResource = ResourcesPlugin.getWorkspace().getRoot().findMember(Path.fromPortableString(location));
        doLoadDesignMenu();
        updateLaunchAction();
    }
    

    /** 
     * @see ViewPart#saveState
     */
    public void saveState(IMemento memento) {
        if (viewer == null) {
            if (this.memento != null) //Keep the old state;
                memento.putMemento(this.memento);
            return;
        }
        if (selectedResource != null) {
            String location = selectedResource.getFullPath().toPortableString();
            memento.putString(TAG_SELECTED_RESOURCE, location);
        }
    }

} // class DesignFlowView