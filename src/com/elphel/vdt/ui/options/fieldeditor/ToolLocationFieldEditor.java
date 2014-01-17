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
package com.elphel.vdt.ui.options.fieldeditor;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.options.OptionsCore;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.ui.options.SetupOptionsManager;

/**
 * A field editor for a tool location type preference.
 * 
 * Created: 25.02.2006
 * @author  Lvov Konstantin
 */

public class ToolLocationFieldEditor extends StringButtonFieldEditor {

    private Tool tool; 
    private String toolExeName;
    
    private Button checkBox = null;
    
    private Boolean hasPackage;
    
    private boolean wasSelected;
    
    private String checkBoxPreferenceName;
    
    public ToolLocationFieldEditor(Tool tool, Composite parent) {
    	String preferenceName = OptionsCore.getLocationPreferenceName(tool);
    	checkBoxPreferenceName = preferenceName + ".checkBox";
        this.tool = tool; 
        File file = new File(tool.getExeName());
        toolExeName = file.getName();
        hasPackage = tool.getParentPackage() != null;

        init(preferenceName, "Tool location");
        setChangeButtonText(Txt.s("Property.ToolLocation.browse"));
        setErrorMessage(Txt.s( "Property.ToolLocation.errorMessage"
                             , new String[]{tool.getLabel(), toolExeName} ) 
                       );
//        setValidateStrategy(VALIDATE_ON_FOCUS_LOST);
        createControl(parent);
    }

    protected void adjustForNumColumns(int numColumns) {
        ((GridData) getCheckBoxControl().getLayoutData()).horizontalSpan = numColumns;
    	super.adjustForNumColumns(numColumns);
    }
    
    protected void doFillIntoGrid(Composite parent, int numColumns) {
    	checkBox = getCheckBoxControl(parent);
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns;
        checkBox.setLayoutData(gd);
        checkBox.setText("Relative to package location");
    	super.doFillIntoGrid(parent, numColumns);
    }
    
    protected Button getCheckBoxControl() {
    	return checkBox;
    }
    
    protected Button getCheckBoxControl(Composite parent) {
        if (checkBox == null) {
            checkBox = new Button(parent, SWT.CHECK | SWT.LEFT);
            checkBox.setFont(parent.getFont());
            checkBox.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    boolean isSelected = checkBox.getSelection();
                    if (isSelected && !checkBoxCanBeSelected()) {
                        checkBox.setSelection(false);
                        isSelected = false;
                    }
                    valueCheckBoxChanged(wasSelected, isSelected);
                    wasSelected = isSelected;
                }
            });
            checkBox.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent event) {
                    checkBox = null;
                }
            });
        } else {
            checkParent(checkBox, parent);
        }
        return checkBox;
    }
    
    private boolean checkBoxCanBeSelected() {
        String packageLocation = getCurrentPackageLocation();
        if ((packageLocation == null) || (packageLocation.length() == 0)) {
            showErrorMessage("Package location is undefined");
            checkBox.setSelection(false);
            return false;
        } else {
            String location = getTextControl().getText().trim();
            File file = new File(location);
            if ( (location.length() != 0) && file.isAbsolute() && !location.startsWith(packageLocation) ) {
                showErrorMessage("Tool location is not subdirectory of package directory");
                checkBox.setSelection(false);
                return false;
            } else {
                clearErrorMessage();
            }
        }
        return true;
    }

    
    /**
     * Informs this field editor's listener, if it has one, about a change
     * to the value (<code>VALUE</code> property) provided that the old and
     * new values are different.
     *
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void valueCheckBoxChanged(boolean oldValue, boolean newValue) {
        if (oldValue == newValue) 
            return;
        
        String location = getTextControl().getText().trim();
        String packageLocation = getCurrentPackageLocation();
        if (newValue) {
            location = OptionsCore.getRelativeLocation(packageLocation, location );
        } else if (OptionsCore.isLocationRelative(location)) {
            location = OptionsCore.getAbsoluteLocation(packageLocation, location );
        }
        getTextControl().setText(location);
    }
    
    
    /* Method declared on StringButtonFieldEditor.
     * Opens the file chooser dialog and returns the selected file.
     */
    protected String changePressed() {
        String location = getTextControl().getText().trim();
        if (checkBox.getSelection()) {
            if (location.length() == 0) {
                location = getCurrentPackageLocation();
            } else {
                location = OptionsCore.getAbsoluteLocation( getCurrentPackageLocation()
                                                          , getTextControl().getText().trim() );  
            }
        }
        File f = new File(location);
        if (!f.exists())
            f = null;
        File d = getDirectory(f);
        if (d == null)
            return null;

        location = d.getAbsolutePath();
        if (checkBox.getSelection()) {
            location = OptionsCore.getRelativeLocation(tool.getParentPackage(), location);  
        }
        return location;
    }
    
    /**
     * Helper to open the file chooser dialog.
     * @param startingDirectory the directory to open the dialog on.
     * @return File The File the user selected or <code>null</code> if they
     * do not.
     */
    private File getDirectory(File startingDirectory) {
        DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
        String message = "Specify the location of \""+tool.getLabel()+"\" ("+toolExeName+")";
        if (checkBox.getSelection()) {
            message += "\nPackage location: " + getCurrentPackageLocation();
        }
        dialog.setMessage(message); 
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
        } else if (checkBox.getSelection() && !OptionsCore.isLocationRelative(path)) {    
            msg = "Tool location is not subdirectory of package directory";
        } else {
            String location = path + File.separator + toolExeName;
            if (checkBox.getSelection())
                location = OptionsCore.getAbsoluteLocation( getCurrentPackageLocation()
                                                          , location );
            File file = new File(location);
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
        getPreferenceStore().setValue(checkBoxPreferenceName, checkBox.getSelection());
        String location = getTextControl().getText().trim() + File.separator + toolExeName;
        if (checkBox.getSelection())
            location = OptionsCore.getAbsoluteLocation(getCurrentPackageLocation(), location);
        tool.setLocation(location);
        super.doStore();
    }
    
    /* (non-Javadoc)
     * Method declared on FieldEditor.
     * Loads the value from the preference store and sets it to
     * the check box.
     */
    protected void doLoad() {
        OptionsCore.doLoadLocation(tool);
        super.doLoad();
        if (checkBox != null) {
            if (hasPackage) {
                boolean value = getPreferenceStore().getBoolean(checkBoxPreferenceName);
                checkBox.setSelection(value);
                wasSelected = value;
            } else {
                checkBox.setSelection(false);
                wasSelected = false;
            }
            checkBox.setEnabled(hasPackage);
        }
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     * Loads the default value from the preference store and sets it to
     * the check box.
     */
    protected void doLoadDefault() {
        super.doLoadDefault();
        if (checkBox != null) {
            boolean value = getPreferenceStore().getDefaultBoolean(checkBoxPreferenceName);
            checkBox.setSelection(value);
            wasSelected = value;
        }
    }
    
    private String getCurrentPackageLocation() {
        return SetupOptionsManager.getCurrentLocation(tool.getParentPackage());
    }
    
    public static void doClear(Tool tool, IPreferenceStore store) {
        String preferenceName = OptionsCore.getLocationPreferenceName(tool);
        store.setToDefault(preferenceName);        

        String checkBoxPreferenceName = preferenceName + ".checkBox";
        store.setToDefault(checkBoxPreferenceName);        
    }
    
} // class ToolLocationFieldEditor
