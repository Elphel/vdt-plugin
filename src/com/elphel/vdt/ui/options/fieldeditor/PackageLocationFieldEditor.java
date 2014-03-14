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
package com.elphel.vdt.ui.options.fieldeditor;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

import com.elphel.vdt.core.options.OptionsCore;
import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.core.tools.contexts.PackageContext;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.ui.options.SetupOptionsManager;


public class PackageLocationFieldEditor extends StringButtonFieldEditor {

    private PackageContext context;
    
    public PackageLocationFieldEditor(PackageContext context, Composite parent) {
        this.context = context;
        String preferenceName = OptionsCore.getLocationPreferenceName(context);
        init(preferenceName, "Package location: ");
        setChangeButtonText("Browse");
        setErrorMessage("Invalid package location");
//        setValidateStrategy(VALIDATE_ON_FOCUS_LOST);
        createControl(parent);
    }
    
    /* (non-Javadoc)
     * Method declared on StringButtonFieldEditor.
     * Opens the directory chooser dialog and returns the selected directory.
     */
    protected String changePressed() {
        File f = new File(getTextControl().getText());
        if (!f.exists())
            f = null;
        File d = getDirectory(f);
        if (d == null)
            return null;

        return d.getAbsolutePath();
    }

    /* (non-Javadoc)
     * Method declared on StringFieldEditor.
     * Checks whether the text input field contains a valid directory.
     */
    protected boolean doCheckState() {
        boolean ok;
        String fileName = getTextControl().getText();
        fileName = fileName.trim();
        if (fileName.length() == 0 && isEmptyStringAllowed()) {
            ok = true;
        } else {
            File file = new File(fileName);
            ok = file.isDirectory();
        }
        if (ok) {
            SetupOptionsManager.setCurrentLocation(context, fileName);
        }
        return ok;
    }

    /**
     * Helper that opens the directory chooser dialog.
     * @param startingDirectory The directory the dialog will open in.
     * @return File File or <code>null</code>.
     * 
     */
    private File getDirectory(File startingDirectory) {
        DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
        dialog.setMessage("Specify the location of \""+context.getLabel()+"\""); 
        if (startingDirectory != null)
            dialog.setFilterPath(startingDirectory.getPath());
        String dir = dialog.open();
        if (dir != null) {
            dir = dir.trim();
            if (dir.length() > 0)
                return new File(dir);
        }

        return null;
    }
    
    protected void doStore() {
        super.doStore();
        context.setWorkingDirectory(getTextControl().getText());
        List<Tool> tools = ToolsCore.getTools(context);
        for (Iterator<Tool> i = tools.iterator(); i.hasNext(); ) {
            Tool tool = i.next();
            if (OptionsCore.isLocationRelative(tool)) {
                OptionsCore.doLoadLocation(tool);
                String toolLocation = OptionsCore.getAbsoluteLocation(context, tool.getExeName());
                tool.setLocation(toolLocation);
            }
        }
    }

    protected void doLoad() {
        super.doLoad();
    }
    
    
    public static void doClear(PackageContext context, IPreferenceStore store) {
        String preferenceName = OptionsCore.getLocationPreferenceName(context);
        store.setToDefault(preferenceName);
        context.setWorkingDirectory(null);
    }
    
} // class PackageLocationFieldEditor
