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
package com.elphel.vdt.ui.preferences;


import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import com.elphel.vdt.Txt;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.ui.tools.ToolUI;
import com.elphel.vdt.ui.tools.ToolUIManager;

/**
 * The page for setting the VDT preferences.
 *
 * Created: 09.12.2005
 * @author  Lvov Konstantin
 */

public class PreferencePage extends TabbedScrolledFieldEditorPreferencePage
                            implements IWorkbenchPreferencePage {

    public PreferencePage() {
        super(GRID);
    }
    
    public PreferencePage(String title) {
        super(title, GRID);
    }

    /**
     * Creates the field editors. Field editors are abstractions of
     * the common GUI blocks needed to manipulate various types
     * of preferences. Each field editor knows how to save and
     * restore itself.
     */
    public void createFieldEditors() {
//        addField(new LabelFieldEditor(getUnscrollableFieldEditorParent(), "Prosto tak"));
//        addTab("Main");
        addField(new LabelFieldEditor(getFieldEditorParent(), Txt.s("PropertyPage.Verilog.label")));
        ToolUI[] tools = ToolUIManager.getToolUI();
        for (int i=0; i < tools.length; i++) {
            addField(new ToolLocationFieldEditor( getFieldEditorParent()
                                                , tools[i] ) 
                    );
        }
    } // createFieldEditors()

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        setPreferenceStore(VerilogPlugin.getDefault().getPreferenceStore());
    }
    
    public static String getLocation(Tool tool) {
        IPreferenceStore store = VerilogPlugin.getDefault().getPreferenceStore();
        return ToolLocationFieldEditor.getLocation(store, tool);    
    }

    public static void setLocation(Tool tool, String location) {
        IPreferenceStore store = VerilogPlugin.getDefault().getPreferenceStore();
        ToolLocationFieldEditor.setLocation(store, tool, location); 
    }

/*    
    public static String getShell(Tool tool) {
        IPreferenceStore store = VerilogPlugin.getDefault().getPreferenceStore();
        return ToolShellFieldEditor.getShell(store, tool);    
    }

    public static void setShell(Tool tool, String shell) {
        IPreferenceStore store = VerilogPlugin.getDefault().getPreferenceStore();
        ToolShellFieldEditor.setShell(store, tool, shell); 
    }
*/

} // class PreferencePage