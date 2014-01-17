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
package com.elphel.vdt.ui.wizards.module;



import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.elphel.vdt.Txt;
//import com.elphel.vdt.core.verilog.VerilogUtils;


class NewModuleTemplatePage extends AbstractModulePage {

    private Text textModuleName;

    private ModulePortListViewer portListViewer;

    private Button addButton;
    private Button removeButton;
    private Button upButton;
    private Button downButton;
    
    private SelectionListener buttonListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            ModulePort port = (ModulePort) portListViewer.getSelection().getFirstElement();
            Object source= e.getSource();
            if (source == addButton) { 
                // Add a port to the PortList and refresh the view
                portListViewer.getPortList().addPort();
            } else if (source == removeButton) {
                // Remove the selection and refresh the view
                if (port != null) {
                    portListViewer.getPortList().removePort(port);
                }                               
            } else if (source == upButton) {
                if (port != null) {
                    portListViewer.getPortList().moveUpPort(port);
                }                               
            } else if (source == downButton) {
                if (port != null) {
                    portListViewer.getPortList().moveDownPort(port);
                }                               
            }
        } // widgetSelected()
    };
        
    private final String getModuleName() { return textModuleName.getText().trim(); }

    public NewModuleTemplatePage(ModuleData data) {
        super("newModuleTemplatePage", data);
        setTitle(Txt.s("WizardPage.NewModuleTemplate.caption"));
        setDescription(Txt.s("WizardPage.NewModuleTemplate.description"));
    }
    
    private void createModuleName(Composite parent) {
        createLabel(parent, Txt.s("WizardPage.NewModuleTemplate.Module.name"));
        textModuleName = createText(parent, fieldsListener);
    }

    private Button createButton(Composite parent, String caption) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(caption);
        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_FILL);
        button.setLayoutData(gridData);
        button.addSelectionListener(buttonListener);
        return button;
    }
        
    private void createModuleInterface(Composite parent) {
        Composite panel = new Composite(parent, SWT.NULL);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        panel.setLayoutData(gridData);
        GridLayout layout = new GridLayout(2, false);
        panel.setLayout(layout);

        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;

        Label label = new Label(panel, SWT.NULL);
        label.setText(Txt.s("WizardPage.NewModuleTemplate.ModuleInterface.prompt"));
        label.setLayoutData(gridData);
                
                // Create the table of ports 
        gridData = new GridData( GridData.FILL_BOTH 
                               | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        gridData.verticalSpan = 4;
        portListViewer = new ModulePortListViewer(panel);
        portListViewer.setLayoutData(gridData);
        
        addButton    = createButton(panel, Txt.s("WizardPage.NewModuleTemplate.AddPort"));
        removeButton = createButton(panel, Txt.s("WizardPage.NewModuleTemplate.RemovePort"));
        upButton     = createButton(panel, Txt.s("WizardPage.NewModuleTemplate.MoveUpPort"));
        downButton   = createButton(panel, Txt.s("WizardPage.NewModuleTemplate.MoveDownPort"));
    } // createModuleInterface()
        
    public void createControl(Composite parent) {
        Composite panel = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        panel.setLayout(layout);
        layout.numColumns = 2;
        layout.verticalSpacing = 9;

        createModuleName(panel);
        createModuleInterface(panel);
        setControl(panel);
    } // createControl()
        


    private boolean validateModuleName() {
        String moduleName = getModuleName();
        if ((moduleName == null) || (moduleName.length() == 0)) {
            updateStatus(Txt.s("WizardPage.NewModuleTemplate.Error.NoModuleName"));         
            return false;               
        }
        try {
            IFile file = data.getFile(); 
//            if (file.exists() && VerilogUtils.existsModule(file, moduleName)) {
                if (file.exists()) {
                updateStatus("Specified module already exists");
                return false;
            }
        } catch (CoreException e) {
        }
        return true;
    }
        
    protected void validatePage() {
        if (validateModuleName())
            updateStatus(null);
    } // validatePage()

    public void updateData() {
        data.setModuleName(getModuleName());
        List<ModulePort> ports = portListViewer.getPortList().getPorts();
        data.setPorts((ModulePort[])ports.toArray(new ModulePort[ports.size()]));
    } // updateData()
    
     // @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
    public void setVisible(boolean visible) {
        if (visible) {          
            textModuleName.setText(data.getPureFileName());
        }
        super.setVisible(visible);
    }       

} // class NewModuleTemplatePage
