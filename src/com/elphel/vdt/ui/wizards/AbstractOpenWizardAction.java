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
package com.elphel.vdt.ui.wizards;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWizard;

//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.ui.MessageUI;

/**
 * Common engine for open wizard actions.
 * 
 * Created: 20.02.2006
 * @author  Lvov Konstantin
 */

public abstract class AbstractOpenWizardAction extends Action 
                                               implements IWorkbenchWindowActionDelegate 
{ 

    /**
     * Creates the specific wizard.
     * (to be implemented by a subclass)
     */
    abstract protected Wizard createWizard() throws CoreException;

    /**
     * The user has invoked this action.
     */
    public void run() {
        Shell shell= VerilogPlugin.getActiveWorkbenchShell();
        try {
            Wizard wizard= createWizard();
            if (wizard instanceof IWorkbenchWizard) {
                ((IWorkbenchWizard)wizard).init(getWorkbench(), getCurrentSelection());
            }
            
            WizardDialog dialog= new WizardDialog(shell, wizard);
            dialog.create();
            int res= dialog.open();
            
            notifyResult(res == Window.OK);
        } catch (CoreException e) {
            MessageUI.error(e);
        }
    } // run()

    protected IWorkbench getWorkbench() {
        return VerilogPlugin.getDefault().getWorkbench();
    }
    
    protected IStructuredSelection getCurrentSelection() {
        IWorkbenchWindow window= VerilogPlugin.getActiveWorkbenchWindow();
        if (window != null) {
            ISelection selection= window.getSelectionService().getSelection();
            if (selection instanceof IStructuredSelection) {
                return (IStructuredSelection) selection;
            }
        }
        return null;
    } // getCurrentSelection()
    
    
    /*
     * @see IActionDelegate#run(IAction)
     */
    public void run(IAction action) {
        run();
    }

    /*
     * @see IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
    }

    /*
     * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
    }
    
    /*
     * @see IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // selection taken from selectionprovider
    }
    
} // class AbstractOpenWizardAction
