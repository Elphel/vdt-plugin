/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of VDT plug-in.
 * VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VDT plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  Additional permission under GNU GPL version 3 section 7:
 * If you modify this Program, or any covered work, by linking or combining it
 * with Eclipse or Eclipse plugins (or a modified version of those libraries),
 * containing parts covered by the terms of EPL/CPL, the licensors of this
 * Program grant you additional permission to convey the resulting work.
 * {Corresponding Source for a non-source form of such a combination shall
 * include the source code for the parts of Eclipse or Eclipse plugins used
 * as well as that of the covered work.}
 *******************************************************************************/
package com.elphel.vdt.ui.wizards.module;


import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.ui.*;

import com.elphel.vdt.Txt;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.ui.MessageUI;

/**
 * New Verilog module wizard.
 * 
 * Created: 12.12.2005
 * @author  Lvov Konstantin
 */

public class NewModuleWizard extends Wizard implements INewWizard {

    private IWorkbench workbench;
    private IStructuredSelection selection;

    private NewModuleMainPage     pageMain;
    private NewModuleTemplatePage pageTemplate;
    
    private ModuleData moduleData;  

    public NewModuleWizard() {
        super();
        setNeedsProgressMonitor(true);
        setDialogSettings(VerilogPlugin.getDefault().getDialogSettings());
        setWindowTitle(Txt.s("Wizard.NewModule.Caption"));
        moduleData = new ModuleData();
    } // NewModuleWizard()
    
    /**
     * Adding the page to the wizard.
     */
    public void addPages() {
        super.addPages();
        pageMain = new NewModuleMainPage(moduleData, selection);
        addPage(pageMain);
        pageTemplate = new NewModuleTemplatePage(moduleData);
        addPage(pageTemplate);
    }

    // @see org.eclipse.jface.wizard.Wizard#canFinish()
    public boolean canFinish() {
        IWizardPage page = getContainer().getCurrentPage();
        return super.canFinish() && page != pageMain;
    }
    
    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    public boolean performFinish() {
        pageMain.updateData();
        pageTemplate.updateData();
        NewModuleOperation op = new NewModuleOperation(moduleData, this);
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageUI.error(realException);
            return false;
        }
        return true;
    }
    
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
    
} // class NewModuleWizard