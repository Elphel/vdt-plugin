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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.core.tools.menu.DesignMenu;

/**
 * The "New Project Main" wizard page allows setting the name and template
 * for Verilog project. The page will only accept unique project name.
 * 
 * Created: 12.12.2005
 * @author  Lvov Konstantin
 */

public class NewProjectMainPage extends AbstractProjectPage {

	private Text  textName;
    private Combo comboDesignMenu;
    private Label labelDesignMenuDesc;
    
    private List<DesignMenu> desigmMenus;
    
    private final String  defaultProjectName = "MyVerilogProject";

    
	public NewProjectMainPage(ProjectData data) {
		super("newProjectMainPage", data);
		setTitle(Txt.s("WizardPage.NewProjectMain.caption"));
		setDescription(Txt.s("WizardPage.NewProjectMain.description"));
        desigmMenus = ToolsCore.getDesignMenuManager().getDesignMenuList();
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		panel.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;

        addProjectName(panel);
        addProjectDesignMenu(panel);

        validatePage();
		setControl(panel);
	} // createControl()
	
    private void addProjectName(Composite parent) {
        createLabel(parent, Txt.s("WizardPage.NewProjectMain.Name.prompt"));
        textName = createText(parent, fieldsListener);
        textName.setText(defaultProjectName);
    }

    private void addProjectDesignMenu(Composite parent) {
        createLabel(parent, Txt.s("WizardPage.NewProjectMain.Menu.prompt"));
        comboDesignMenu = createCombo(parent);
        ArrayList<String> items = new ArrayList<String>(desigmMenus.size()); 
        for (DesignMenu menu : desigmMenus) {
            items.add(menu.getName());
        }
        comboDesignMenu.setItems((String[])items.toArray(new String[items.size()]));
        comboDesignMenu.select(0);
        
        createLabel(parent, "");
        labelDesignMenuDesc = createLabel(parent, "");
        String description = desigmMenus.get(0).getDescription();
        if (description != null)
            labelDesignMenuDesc.setText(description);
        comboDesignMenu.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                String description = getDesignMenu().getDescription();
                if (description == null)
                    description = "";
                labelDesignMenuDesc.setText(description);
            }
        });
    }
    
	@Override
	protected void validatePage() {
		String projectName = getProjectName();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        IStatus status = workspace.validateName(projectName, IResource.PROJECT);

        if (status.isOK() && workspace.getRoot().getProject(projectName).exists())
            status = createStatus(IStatus.ERROR, "Project already exists");

        updateStatus(status);
	} // validatePage()


	private String getProjectName() { 
		return textName.getText().trim(); 
	}

	private DesignMenu getDesignMenu() { 
		return (DesignMenu)desigmMenus.get(comboDesignMenu.getSelectionIndex()); 
	}

	@Override
	public void updateData() {
        if (textName != null)
		    data.setProjectName(getProjectName());
        if (comboDesignMenu != null)
		    data.setDesignMenu(getDesignMenu());
	}

} // class NewProjectMainPage
