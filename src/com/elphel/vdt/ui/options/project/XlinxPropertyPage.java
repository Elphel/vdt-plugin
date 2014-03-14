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
package com.elphel.vdt.ui.options.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * The Xlinx property page class for Verilog projects.
 * 
 * Created: 16.02.2006
 * @author  Lvov Konstantin
 */
public class XlinxPropertyPage extends PropertyPage {

    private XlinxOptionsBlock deviceOptions; 
    
    public XlinxPropertyPage() {
        super();
        noDefaultAndApplyButton();
    }

    /**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL);
        data.grabExcessHorizontalSpace = true;
        composite.setLayoutData(data);

        IResource resource = (IResource) getElement().getAdapter(IResource.class);
        IProject project = resource.getProject();
            
        deviceOptions = new XlinxOptionsBlock(composite, project);
        return composite;
    }

//    private void addSeparator(Composite parent) {
//        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
//        GridData gridData = new GridData();
//        gridData.horizontalAlignment = GridData.FILL;
//        gridData.grabExcessHorizontalSpace = true;
//        separator.setLayoutData(gridData);
//    }
    

    protected void performDefaults() {
        super.performDefaults();
        deviceOptions.performDefaults();
    }
    
    public boolean performOk() {
        return deviceOptions.performOk()
        && super.performOk();    
    }

} // class XlinxPropertyPage