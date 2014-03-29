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


import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import com.elphel.vdt.Txt;
import com.elphel.vdt.Module;
import com.elphel.vdt.ui.variables.SelectedResourceManager;


/**
 * The "New Module Main" wizard page allows setting the folder
 * and file name for Verilog module. The page will only accept file
 * name without the extension OR with the extension that matches 
 * the expected one (v).
 * 
 * Created: 12.12.2005
 * @author  Lvov Konstantin
 */

class NewModuleMainPage extends AbstractModulePage {

	private Text textPath;

	private Text textName;

	private ISelection selection;

	private String getPath() { return textPath.getText().trim(); }
	private String getFileName() { return textName.getText().trim(); }
	
	public NewModuleMainPage(ModuleData data, ISelection selection) {
		super("newModuleMainPage", data);
		setTitle(Txt.s("WizardPage.NewModuleMain.caption"));
		setDescription(Txt.s("WizardPage.NewModuleMain.description"));
        this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		panel.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
        
        addLocation(panel);
        addFileName(panel);
        
		initialize();
		validatePage();
		setControl(panel);
	} // createControl()

    private void addLocation(Composite parent) {
        createLabel(parent, Txt.s("WizardPage.NewModuleMain.Path.prompt"));
        textPath = createText(parent, fieldsListener);
        Button button = new Button(parent, SWT.PUSH);
        button.setText(Txt.s("WizardPage.NewModuleMain.Path.browse"));
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                browsePath();
            }
        });
    }

    private void addFileName(Composite parent) {
        createLabel(parent, Txt.s("WizardPage.NewModuleMain.Name.prompt"));
        textName = createText(parent, fieldsListener);
    }
    
	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */
	private void initialize() {
		if ( (selection != null) && !selection.isEmpty() && (selection instanceof IStructuredSelection) ) {
            IStructuredSelection ssel = (IStructuredSelection) selection;
            if (ssel.size() == 1) {
                Object obj = ssel.getFirstElement();
                if (obj instanceof IResource) {
                    IContainer container;
                    if (obj instanceof IContainer)
                        container = (IContainer) obj;
                    else {
                        container = ((IResource) obj).getParent();
                        textName.setText(((IResource) obj).getName());
                    }
                    textPath.setText(container.getFullPath().toString());
                    textName.setText(((IResource)obj).getName());
                    return;
                } else if (obj instanceof Module) {
                    IFile file = ((Module) obj).getSourceFile().getFile();
                    textPath.setText(file.getParent().getFullPath().toString());
                    textName.setText(file.getName());
                } 
            }
        }

        IProject project = SelectedResourceManager.getDefault().getSelectedResource().getProject();
        textPath.setText(project.getFullPath().toString());
	} // initialize()

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */
	private void browsePath() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				Txt.s("WizardPage.NewModuleMain.Browse.message"));
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				textPath.setText(((Path) result[0]).toString());
			}
		}
	} // browsePath()


	protected void validatePage() {
		if (getPath().length() == 0) {
			updateStatus("File container must be specified");
            textPath.setFocus();
			return;
		}

		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getPath()));
		String fileName = getFileName();

		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("File container must exist");
            textPath.setFocus();
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
            textPath.setFocus();
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
            textName.setFocus();
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
            textName.setFocus();
			return;
		}
		int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			if (ext.equalsIgnoreCase("v") == false) {
				updateStatus("File extension must be \"v\"");
                textName.setFocus();
				return;
			}
		}
		updateStatus(null);
	} // validatePage()


	public void updateData() {
		data.setLocationPath(getPath());
		String name = getFileName();
		if (!name.endsWith(".v"))
			name += ".v";
		data.setFileName(name);
	} // updateData()
	
} // class NewModuleMainPage