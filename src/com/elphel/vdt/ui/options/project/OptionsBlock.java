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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.elphel.vdt.VDT;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;

/**
 * Abstract options configuration block providing a general implementation
 * for reading and saving options value.
 * 
 * Created: 17.03.2006
 * @author  Lvov Konstantin
 */

public class OptionsBlock {

    private IResource resource;
    private ScopedPreferenceStore settingsStore = null;
    
    private List<IOption> optionsFields;
    private List<Option> options;
    
    public OptionsBlock(IResource resource, int capacity) {
        optionsFields = new ArrayList<IOption>(capacity);
        init(resource);
    }

    public OptionsBlock(IResource resource, List<Option> options) {
        this(resource, options.size());
        for (Iterator i = options.iterator(); i.hasNext(); ) {
            IOption option = (IOption)i.next();
            addOption(option);
        }
    }
    
    private void init(IResource resource) {
        this.resource = resource;
        if (resource instanceof IProject) {
            settingsStore = new ScopedPreferenceStore(new ProjectScope((IProject)resource), VDT.ID_VDT);
        }
    }
    
    public List<Option> getOptions() {
        if (options == null) {
            options = new ArrayList<Option>(optionsFields.size());
            for (Iterator i = optionsFields.iterator(); i.hasNext(); ) {
                IOption field = (IOption)i.next();
                options.add(field.getOption());
            }
        }
        return options;
    } // getOptions()

    protected void addOption(IOption option) {
        optionsFields.add(option);
        if (settingsStore != null)
            option.setPreferenceStore(settingsStore);
        else 
            option.setResourceStore(resource); 
    }
    
    
    protected void performDefaults() {
        for (Iterator i = optionsFields.iterator(); i.hasNext(); ) {
            ((IOption)i.next()).reset();
        }
    } // performDefaults()

    protected void initializeFields() {
        for (Iterator i = optionsFields.iterator(); i.hasNext(); ) {
            ((IOption)i.next()).read();
        }
    } // initializeFields()
        
    public boolean performOk() {
        boolean ok = true;
        for (Iterator i = optionsFields.iterator(); i.hasNext(); ) {
            ok = ok && ((IOption)i.next()).save();
        }
        if (settingsStore != null) {
            try {
                settingsStore.save();
            } catch (IOException e) {
                  // Nothing do do, we don't need to bother the user
            }
        }
        if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        	System.out.println("OptionsBlock.performOK()");
        return ok;
    } // performOk()
    
    protected static void createLabel(Composite parent, String text ) {
        GridData labelData = new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING );
        
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        label.setLayoutData(labelData);
    } // createLabel()

    protected static Combo createCombo(Composite parent) {
        GridData  gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL); 
     
        Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(gridData);
        
        return combo;
    } // createCombo()  

    protected static void createStab(Composite parent) {
        GridData labelData = new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING
                                         | GridData.GRAB_HORIZONTAL );
        
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(labelData);
    } // createStab()

       
} // class OptionsBlock
