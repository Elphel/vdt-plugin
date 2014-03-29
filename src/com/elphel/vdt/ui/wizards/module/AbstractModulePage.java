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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page with ModulaData updating.
 * 
 * Created: 16.12.2005
 * @author  Lvov Konstantin
 */

abstract class AbstractModulePage extends WizardPage {
	
	protected ModuleData data;

	AbstractModulePage(String pageName, ModuleData data) {
		super(pageName);
		this.data = data;
	}
	
	public final ModuleData getData() {
		return data;
	}

	public abstract void updateData();
	
	protected abstract void validatePage();
	
    //* @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	public IWizardPage getNextPage() {
		updateData();
		return super.getNextPage();
	}

	protected void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	} // updateStatus()

	
	protected ModifyListener fieldsListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			validatePage();
		}
	};
	
    protected static void createLabel(Composite parent, String text ) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
    } // createLabel()
    
	protected Text createText(Composite parent, ModifyListener listener) {
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.addModifyListener(listener);
		return text;
	}
	
} // class AbstractModulePage
