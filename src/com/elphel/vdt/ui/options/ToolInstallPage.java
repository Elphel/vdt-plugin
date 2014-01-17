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
package com.elphel.vdt.ui.options;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.ui.options.fieldeditor.LabelFieldEditor;
import com.elphel.vdt.ui.options.fieldeditor.SpacerFieldEditor;
import com.elphel.vdt.ui.options.fieldeditor.ToolLocationFieldEditor;
import com.elphel.vdt.ui.preferences.TabbedScrolledFieldEditorPreferencePage;

public class ToolInstallPage extends TabbedScrolledFieldEditorPreferencePage
                             implements IWorkbenchPreferencePage
{
    private Tool tool;
    
    public ToolInstallPage() {
        super(GRID);
        noDefaultAndApplyButton();
    }

    public void setContext(Tool tool) {
        this.tool = tool;
        setTitle(tool.getLabel());
    }
    
    protected void createFieldEditors() {
        File file = new File(tool.getExeName());
        String toolExeName = file.getName();
        boolean isShell = tool.getIsShell();
        if (isShell) {
        	addField(new LabelFieldEditor( "Please specify the location of shell program to run \""+tool.getLabel()+"\" ("+toolExeName+")"
        			, getFieldEditorParent()) );
        } else {
        	addField(new LabelFieldEditor( "Please specify the location of \""+tool.getLabel()+"\" ("+toolExeName+")"
        			, getFieldEditorParent()) );
        }
        
        addField(new SpacerFieldEditor(getFieldEditorParent()));
        addField(new ToolLocationFieldEditor(tool, getFieldEditorParent()));
    }

//    public boolean isValid() {
//        checkState();
//        return super.isValid();
//    }

    public void init(IWorkbench workbench) {
        setPreferenceStore(VerilogPlugin.getDefault().getPreferenceStore());
    }

    public static void doClear(Tool tool) {
        IPreferenceStore store = VerilogPlugin.getDefault().getPreferenceStore();
        ToolLocationFieldEditor.doClear(tool, store);
    }
    
} // class ToolInstallPage
