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
package com.elphel.vdt.ui.preferences;


import java.io.File;

import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

import com.elphel.vdt.Txt;


/**
 * A field editor for a Xilinx path type preference.
 * 
 * Created: 12.12.2005
 * @author  Lvov Konstantin
 */

public class XilinxPathFieldEditor extends StringButtonFieldEditor {

    /**
     * Creates a directory field editor.
     * 
     * @param name the name of the preference this field editor works on
     * @param parent the parent of the field editor's control
     */
    public XilinxPathFieldEditor(Composite parent) {
        init(PreferenceConstants.Xilinx_PATH, Txt.s("Property.Xilinx.Path.prompt"));
        setErrorMessage(Txt.s("Property.Xilinx.Path.errorMessage"));
        setChangeButtonText(Txt.s("Property.Xilinx.Path.browse"));
        setValidateStrategy(VALIDATE_ON_FOCUS_LOST);
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
        String fileName = getTextControl().getText();
        fileName = fileName.trim();
        if (fileName.length() == 0 && isEmptyStringAllowed())
            return true;
        File file = new File(fileName);
        return file.isDirectory();
    }

    /**
     * Helper that opens the directory chooser dialog.
     * @param startingDirectory The directory the dialog will open in.
     * @return File File or <code>null</code>.
     * 
     */
    private File getDirectory(File startingDirectory) {

        DirectoryDialog fileDialog = new DirectoryDialog(getShell(), SWT.OPEN);
        fileDialog.setMessage(Txt.s("Property.Xilinx.Path.BrowseDialog.Message"));
        if (startingDirectory != null)
            fileDialog.setFilterPath(startingDirectory.getPath());
        String dir = fileDialog.open();
        if (dir != null) {
            dir = dir.trim();
            if (dir.length() > 0)
                return new File(dir);
        }

        return null;
    }
    
} // class XilinxPathFieldEditor
