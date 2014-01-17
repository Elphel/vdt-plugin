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


import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.ui.options.OptionsBlock;

/**
 * The "New Project Options" wizard page allows setting the Verilog 
 * project options.
 * 
 * Created: 12.12.2005
 * @author  Lvov Konstantin
 */

public class NewProjectOptionsPage extends AbstractProjectPage {

    private OptionsBlock options;
    private Composite panel;
    
	public NewProjectOptionsPage(ProjectData data) {
		super("newProjectMainPage", data);
		setTitle(Txt.s("WizardPage.NewProjectOptions.caption"));
		setDescription(Txt.s("WizardPage.NewProjectOptions.description"));
	}
	
	public void createControl(Composite parent) {
        panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        panel.setLayout(layout);
        GridData gridData = new GridData(GridData.FILL);
        gridData.grabExcessHorizontalSpace = true;
        panel.setLayoutData(gridData);

//        GridLayout layout = new GridLayout(1, false);
//        layout.marginHeight = 0;
//        layout.marginWidth  = 5;
//        layout.verticalSpacing   = 0;
//        layout.horizontalSpacing = 0;
//        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
//
//        panel = new Composite(parent, SWT.NONE);
//        panel.setLayout(layout);
//        panel.setLayoutData(gridData);
        
//        options = new OptionsBlock(panel, data.getProjectContext());
        options = new OptionsBlock(panel, ToolsCore.getContextManager().getProjectContexts().get(0));
        
//        updateData();
		setControl(panel);
	} // createControl(

	protected void validatePage() {
		IStatus status = createStatus(IStatus.OK, null);
        updateStatus(status);
	}

	public void updateData() {
//		if (options == null) {
//	        options = new OptionsBlock(panel, data.getProjectContext());
//			setControl(panel);
//			setVisible(true);
//		}
		options.performApply();
	}
	
} // class NewProjectOptionsPage
