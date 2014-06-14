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
package com.elphel.vdt.ui.wizards.project;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;


/**
 * Wizard page with ProjectData updating.
 * 
 * Created: 18.02.2006
 * @author  Lvov Konstantin
 */

abstract class AbstractProjectPage extends WizardPage {

    protected ProjectData data;

    AbstractProjectPage(String pageName, ProjectData data) {
        super(pageName);
        this.data = data;
    }
    
    public final ProjectData getData() {
        return data;
    }

    public abstract void updateData();
    
    protected abstract void validatePage();
    
    //* @see org.eclipse.jface.wizard.WizardPage#getNextPage()
    public IWizardPage getNextPage() {
        updateData();
        return super.getNextPage();
    }

    protected void updateStatus(IStatus status) {
        setPageComplete(status.isOK());

        if (status.isOK())
            updateData();
        
        updateStatusMessage(status);
    } // updateStatus()
    
    private void updateStatusMessage(IStatus status) {
        String errorMessage = null;
        String warningMessage = null;
        String statusMessage = status.getMessage();
        if (statusMessage.length() > 0) {
            if (status.matches(IStatus.ERROR))
                errorMessage = statusMessage;
            else if (!status.isOK())
                warningMessage = statusMessage;
        }    
        setErrorMessage(errorMessage);
        setMessage(warningMessage);
    } // updateStatusMessage()
    
    static IStatus createStatus(int severity, String message) {
        return new Status(severity, VerilogPlugin.getVdtId(), severity, message, null);
    }
    
    protected ModifyListener fieldsListener = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
            validatePage();
        }
    };
    
    protected static Label createLabel(Composite parent, String text ) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        return label;
    } // createLabel()
    
    protected static Text createText(Composite parent, ModifyListener listener) {
        Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.addModifyListener(listener);
        return text;
    }

    protected static Combo createCombo(Composite parent) {
        Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        return combo;
    } // createCombo()  

    protected static void createStab(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING
                                         | GridData.GRAB_HORIZONTAL )
                           );
    } // createStab()
    
} // class AbstractProjectPage
