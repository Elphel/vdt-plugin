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
package com.elphel.vdt.ui.wizards.project;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.elphel.vdt.Txt;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.ui.MessageUI;

/**
 * New Verilog project wizard.
 * 
 * Created: 18.02.2006
 * @author  Lvov Konstantin
 */

public class NewProjectWizard  extends Wizard implements INewWizard {

    private IWorkbench workbench;
    private IStructuredSelection selection;

    private ProjectData projectData;    
    
    private NewProjectMainPage     pageMain;
//    private NewProjectOptionsPage  pageOptions;
    
    public NewProjectWizard() {
        super();
        setNeedsProgressMonitor(true);
        setDialogSettings(VerilogPlugin.getDefault().getDialogSettings());
        setWindowTitle(Txt.s("Wizard.NewProject.Caption"));
        projectData = new ProjectData();
    } // NewModuleWizard()
    

    /**
     * Adding the page to the wizard.
     */
    public void addPages() {
        super.addPages();
        pageMain = new NewProjectMainPage(projectData);
        addPage(pageMain);
//        pageOptions = new NewProjectOptionsPage(projectData);
//        addPage(pageOptions);
    }
    
    
    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    public boolean performFinish() {
        pageMain.updateData();
//        pageOptions.updateData();
        
        NewProjectOperation op = new NewProjectOperation(projectData);
        org.eclipse.jface.operation.IRunnableWithProgress runnable = new WorkspaceModifyDelegatingOperation(op);
        try {
            getContainer().run(true, false, runnable);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageUI.error(realException);
            return false;
        }

        BasicNewProjectResourceWizard.updatePerspective(configElement);
        IResource resource = op.getElementToOpen();
        if (resource != null)
            openResource(resource);
        
        return true;
    } // performFinish()

    private void openResource(final IResource resource) {
        if (resource.getType() != IResource.FILE)
            return;
        
        IWorkbenchWindow window = VerilogPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        if (window == null)
            return;
        final IWorkbenchPage activePage = window.getActivePage();
        if (activePage != null) {
            Display display = getShell().getDisplay();
            display.asyncExec(new Runnable() {
                public void run() {
                    try {
                        IDE.openEditor(activePage, (IFile)resource, true);
                    } catch(PartInitException e) {
                        MessageUI.log(e);
                    }
                }
            });
            BasicNewResourceWizard.selectAndReveal(resource, activePage.getWorkbenchWindow());
        }
    } // openResource()
    
    
    /**
     * We will accept the selection in the workbench to see if
     * we can initialize from it.
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
    }
    
    public IWorkbench getWorkbench() {
        return workbench;
    }
    
    public IStructuredSelection getSelection() {
        return selection;
    }
    
    private IConfigurationElement configElement;
    
} // class NewProjectWizard
