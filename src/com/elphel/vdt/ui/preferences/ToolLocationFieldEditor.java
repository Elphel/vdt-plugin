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
package com.elphel.vdt.ui.preferences;


import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.options.OptionsCore;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.ui.tools.ToolUI;

/**
 * A field editor for a tool location type preference.
 *
 * Created: 25.02.2006
 * @author  Lvov Konstantin
 */

public class ToolLocationFieldEditor extends StringButtonFieldEditor {

    private ToolUI tool;
    private String toolExeName;

    public ToolLocationFieldEditor(Composite parent,  ToolUI tool) {
        init(OptionsCore.getLocationPreferenceName(tool.getTool()), tool.getLabel());
        this.tool = tool;
        File file = new File(tool.getToolLocation());
        toolExeName = file.getName();
        setChangeButtonText(Txt.s("Property.ToolLocation.browse")+"ToolLocationFieldEditor");
        setErrorMessage(Txt.s( "Property.ToolLocation.errorMessage"
                             , new String[]{tool.getLabel(), toolExeName} )
                       );
        setValidateStrategy(VALIDATE_ON_FOCUS_LOST);
        createControl(parent);
    }

    /* Method declared on StringButtonFieldEditor.
     * Opens the file chooser dialog and returns the selected file.
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

    /**
     * Helper to open the file chooser dialog.
     * @param startingDirectory the directory to open the dialog on.
     * @return File The File the user selected or <code>null</code> if they
     * do not.
     */
    private File getDirectory(File startingDirectory) {
//        ToolLocationDialog dialog = new ToolLocationDialog(getShell(), SWT.OPEN, tool);
        DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
        dialog.setMessage( Txt.s("Dialog.ToolLocation.Message"
                                , new String[]{tool.getLabel(), toolExeName}) );
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

    /* (non-Javadoc)
     * Method declared on StringFieldEditor.
     * Checks whether the text input field specifies an existing file.
     */
    protected boolean checkState() {
        String msg = null;

        String path = getTextControl().getText();
        if (path != null)
            path = path.trim();
        else
            path = "";//$NON-NLS-1$
        if (path.length() == 0) {
            msg = getErrorMessage();
        } else {
            File file = new File(path + File.separator + toolExeName);
            if ( !(file.isFile() && file.exists()) ) {
                msg = getErrorMessage();
            }
        }

        if (msg != null) { // error
            showErrorMessage(msg);
            return false;
        }

        // OK!
        clearErrorMessage();
        return true;
    } // checkState()

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doStore() {
        super.doStore();
        String location = getTextControl().getText().trim() + File.separator + toolExeName;
        tool.getTool().setLocation(location);
    }


    static String getLocation(IPreferenceStore store, Tool tool) {
        return store.getString(OptionsCore.getLocationPreferenceName(tool));
    }

    static void setLocation(IPreferenceStore store, Tool tool, String location) {
        tool.setLocation(location);
        store.setValue(OptionsCore.getLocationPreferenceName(tool), location);
    }

} // class ToolLocationFieldEditor
