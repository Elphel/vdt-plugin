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
package com.elphel.vdt.ui.variables;

import java.util.Stack;

import com.elphel.vdt.VerilogUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Maintains the context used to launch tools. The context is based on
 * the selected resource.
 * 
 * Created: 26.01.2006
 * @author  Lvov Konstantin
 */

public class SelectedResourceManager implements IWindowListener, ISelectionListener {
    // singleton
    private static SelectedResourceManager fgDefault = new SelectedResourceManager();
    
    private IResource fSelectedResource    = null;
    private IResource fSelectedVerilogFile = null;
    private ITextSelection fSelectedText = null;
    private Stack<IWorkbenchWindow> fWindowStack = new Stack<IWorkbenchWindow>();

    
    private SelectedResourceManager() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench != null) { //may be running headless
            workbench.addWindowListener(this);
            IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
            if (activeWindow != null) {
                windowActivated(activeWindow);
            }
        } 
    } // SelectedResourceManager()
    
    /**
     * Returns the singleton resource selection manager
     * 
     * @return VariableContextManager
     */
    public static SelectedResourceManager getDefault() {
    	if (fgDefault.fSelectedResource == null)
    		fgDefault.tryDefaultSelection();
        return fgDefault;
    }

    private void tryDefaultSelection() {
        IWorkbenchPage page = fWindowStack.peek().getActivePage();
        if (page != null) {
            IViewPart view = page.findView(IPageLayout.ID_RES_NAV);
            if (view != null)
            	selectionChanged(view, view.getViewSite().getSelectionProvider().getSelection());
        }
    }
    
    /**
     * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
     */
    public void windowActivated(IWorkbenchWindow window) {
        fWindowStack.remove(window);
        fWindowStack.push(window);
        ISelectionService service = window.getSelectionService(); 
        service.addSelectionListener(this);
        IWorkbenchPage page = window.getActivePage();
        if (page != null) {
            IWorkbenchPart part = page.getActivePart();
            if (part != null) {             
                ISelection selection = service.getSelection();
                if (selection != null) {
                    selectionChanged(part, selection);
                }
            }
        }
    } // windowActivated()

    /**
     * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
     */
    public void windowClosed(IWorkbenchWindow window) {
        ISelectionService selectionService = window.getSelectionService();
        selectionService.removeSelectionListener(this);
        fWindowStack.remove(window);
    }

    /**
     * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
     */
    public void windowDeactivated(IWorkbenchWindow window) {
    }

    /**
     * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
     */
    public void windowOpened(IWorkbenchWindow window) {
        windowActivated(window);
    }

    /**
     * Returns the currently selected resource in the active workbench window,
     * or <code>null</code> if none. If an editor is active, the resource adapater
     * associated with the editor is returned.
     * 
     * @return selected resource or <code>null</code>
     */
    public IResource getSelectedResource() {
        return fSelectedResource;
    }
    
    public IProject getSelectedProject() {
        if (fSelectedResource != null)
            return fSelectedResource.getProject();
        else
            return null;
    }

    public IResource getSelectedVerilogFile() {
        return fSelectedVerilogFile;
    }

    /**
     * Returns resource by selection in the active workbench window,
     * or <code>null</code> if none. If an editor is active, the resource adapater
     * associated with the editor is returned.
     * 
     * @return selected resource or <code>null</code>
     */
    public IResource getSelectedResource(IWorkbenchPart part, ISelection selection) {
        IResource selectedResource = null;
        if (selection instanceof IStructuredSelection) {
            Object result = ((IStructuredSelection)selection).getFirstElement();
            if (result instanceof IResource) {
                selectedResource = (IResource) result;
            } else if (result instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) result;
                selectedResource = (IResource)adaptable.getAdapter(IResource.class);
            }
        }
        
        if (selectedResource == null) {
            // If the active part is an editor, get the file resource used as input.
            if (part instanceof IEditorPart) {
                IEditorPart editorPart = (IEditorPart) part;
                IEditorInput input = editorPart.getEditorInput();
                selectedResource = (IResource) input.getAdapter(IResource.class);
            } 
        }

        if (selectedResource != null)
            return selectedResource;
        else
            return fSelectedResource;
    } // getSelectedResource()
    

    public IResource getViewSelectedResource(String viewId) {
        IWorkbenchPage page = fWindowStack.peek().getActivePage();
        if (page != null) {
            IViewPart view = page.findView(viewId);
            if (view != null) {
                return getSelectedResource(view, view.getViewSite().getSelectionProvider().getSelection());
            }    
        }
        return null;
    }
    
    /**
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
        if (fWindowStack.isEmpty() || !fWindowStack.peek().equals(window)) {
            // selection is not in the active window
            return;
        }
        IResource selectedResource = getSelectedResource(part, selection);
        if (selectedResource != null) {
            fSelectedResource = selectedResource;
            if ((selectedResource.getType()==IResource.FILE) && (VerilogUtils.isHhdlFile((IFile)fSelectedResource)))
//            if (selectedResource.getName().endsWith(".v"))
                fSelectedVerilogFile = selectedResource; /* Maybe same will work for vhdl too? */
        }
        
        if (selection instanceof ITextSelection) {
            fSelectedText = (ITextSelection)selection;
        }
    } // selectionChanged()
    
    
    /**
     * Returns the current text selection as a <code>String</code>, or <code>null</code> if
     * none.
     * 
     * @return the current text selection as a <code>String</code> or <code>null</code>
     */
    public String getSelectedText() {
        return fSelectedText.getText();
    }
    
} // class SelectedResourceManager()
