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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.core.tools.contexts.PackageContext;
import com.elphel.vdt.ui.options.fieldeditor.LabelFieldEditor;
import com.elphel.vdt.ui.options.fieldeditor.PackageLocationFieldEditor;
import com.elphel.vdt.ui.options.fieldeditor.SpacerFieldEditor;
import com.elphel.vdt.ui.preferences.TabbedScrolledFieldEditorPreferencePage;

public class PackageInstallPage extends TabbedScrolledFieldEditorPreferencePage
                                implements IWorkbenchPreferencePage
{
    private PackageContext context;
    
    public PackageInstallPage() {
        super(GRID);
        noDefaultAndApplyButton();
    }

    public void setContext(PackageContext context) {
        this.context = context;
        setTitle(context.getLabel());
    }
    
    protected void createFieldEditors() {
        addField(new LabelFieldEditor( "Please specify an absolute path to " + context.getLabel()
                                     , getFieldEditorParent()) );
        addField(new SpacerFieldEditor(getFieldEditorParent()));
        addField(new PackageLocationFieldEditor( context
                                               , getFieldEditorParent())
                );    
    }

//    public boolean isValid() {
//        checkState();
//        return super.isValid();
//    }

    public void init(IWorkbench workbench) {
        setPreferenceStore(VerilogPlugin.getDefault().getPreferenceStore());
    }

    public static void doClear(PackageContext context) {
        IPreferenceStore store = VerilogPlugin.getDefault().getPreferenceStore();
        PackageLocationFieldEditor.doClear(context, store);
    }
    
} // class PackageInstallPage
