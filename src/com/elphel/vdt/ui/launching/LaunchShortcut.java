/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of Eclipse/VDT plug-in.
 * Eclipse/VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Eclipse/VDT plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.elphel.vdt.ui.launching;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.Txt;
import com.elphel.vdt.core.launching.LaunchCore;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.ui.MessageUI;


public class LaunchShortcut implements ILaunchShortcut {

    public void launch(ISelection selection, String mode) {
        // TODO Auto-generated method stub

    }

    public void launch(IEditorPart editor, String mode) {
        // TODO Auto-generated method stub

    }

    public static void launch(Tool tool) {
        IProject project = getActiveProject();
        try {
        	 MessageUI.error("This type of tool launching is not supported");
            LaunchCore.launch(tool, project,  project.getName(),null); 
        } catch (CoreException e) {
            MessageUI.error(Txt.s("Action.ToolLaunch.Error", 
                                  new String[] {tool.getName(), e.getMessage()}),
                            e);
        }    
    } // launch(Tool tool)
    
    
    private static IProject getActiveProject(ISelection selection) {
        if ((selection != null) && (selection instanceof IStructuredSelection)) {
            IStructuredSelection ssel = (IStructuredSelection)selection;
            if (!ssel.isEmpty()) {
                Object object = ssel.getFirstElement();
                if (object instanceof IAdaptable) {
                    IProject project = (IProject)((IAdaptable)object).getAdapter(IProject.class);
                    if (project != null && project.isOpen())
                        return project;
                }
            }
        }
        return null;
    } // getActiveProject(ISelection)
    
    private static IProject getActiveProject(IEditorPart editor) {
        if (editor != null) {
            IEditorInput input = editor.getEditorInput();
            IResource res = (IResource)input.getAdapter(IResource.class);
            return res.getProject();
        }
        return null;
    } // getActiveProject(IEditorPart)

    private static IProject getActiveProject(IViewPart view) {
        if (view != null) {
            ISelection selection = view.getViewSite().getSelectionProvider().getSelection();
            return getActiveProject(selection);
        }
        return null;
    } // getActiveProject(IViewPart)

    private static IProject getActiveProject(IProject[] projects) {
        if ((projects != null) && (projects.length > 0)) {
            ElementListSelectionDialog dialog = createProjectSelectionDialog(projects);
            if (dialog.open() == Window.OK) {
                return (IProject)dialog.getResult()[0];
            }
        }
        return null;
    } // getActiveProject(IProject[])
    
    private static IProject getActiveProject() {
        IProject project = null;

        IWorkbenchWindow wb = VerilogPlugin.getActiveWorkbenchWindow();
        if (wb != null) {
            IWorkbenchPage page = wb.getActivePage();
            if (page != null) {
                project = getActiveProject(page.getSelection());
                if (project == null)
                    project = getActiveProject(page.getActiveEditor());
                if (project == null)
                    project = getActiveProject(page.findView(IPageLayout.ID_RES_NAV));
            }
        }

        if (project == null)
            project = getActiveProject(VerilogPlugin.getWorkspace().getRoot().getProjects());

        return project;
    } // getActiveProject()

    private static ElementListSelectionDialog createProjectSelectionDialog(IProject[] projects) {
        ElementListSelectionDialog dialog = new ElementListSelectionDialog( 
        		VerilogPlugin.getActiveWorkbenchShell()
        ,  WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider() 
        );
        dialog.setTitle(Txt.s("Dialog.ProjectSelection.Caption"));
        dialog.setMessage(Txt.s("Dialog.ProjectSelection.Message"));
        dialog.setMultipleSelection(false);
        dialog.setIgnoreCase(true);
        dialog.setElements(projects);
        return dialog;
    } // createProjectSelectionDialog()
    
} // class LaunchShortcut
