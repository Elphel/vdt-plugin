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
package com.elphel.vdt.ui.options.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Color;

public class BrowseableField {

    private int caretPosition;

    private Button browseButton;
    private Text textField;
    
    // this dialog field is intended to store the created dialog object,
    // to prevent it from losing the last directory position 
    // it should be created once by the 'browse' selection listener 
    private Dialog browseDialog;

    BrowseableField() {
        caretPosition = -1;
    }
    
    public void createControl(Composite parent) {
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;

        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_FILL 
                                        | GridData.GRAB_HORIZONTAL );
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(layout);
        composite.setLayoutData(gridData);
                                      
        textField = TextComponent.createTextControl(composite);
        
        GridData browseGridData = new GridData();
        browseGridData.heightHint = textField.getLineHeight()+6;
        browseGridData.horizontalAlignment = GridData.END;
        
        browseButton = new Button(composite, SWT.NONE);
        browseButton.setLayoutData(browseGridData);
        browseButton.setText("...");
        browseButton.setEnabled(true);
    } // BrowseableField()
    
    public void setSelection(String value) {
        textField.setText(value);
        if (caretPosition < 0) {
            textField.setSelection(value.length());
        } else {
            textField.setSelection(caretPosition);
            caretPosition = -1;
        }
    }
    
    protected boolean isDisposed() {
        return (textField == null)
            ||  textField.isDisposed();
    }
    
    public Text getBrowsedNameField() {
        return textField;
    }
    
    public Dialog getBrowseDialog() {
        return browseDialog;
    }
    
    public void setBrowseDialog(Dialog browseDialog) {
        this.browseDialog = browseDialog;
    }
    
    public void addSelectionListener(SelectionAdapter selectionAdapter) {
        browseButton.addSelectionListener(selectionAdapter);
    }
    
    public void addModifyListener(ModifyListener listener) {
    	textField.addModifyListener(listener);
    }

    public void removeModifyListener(ModifyListener listener) {
        textField.removeModifyListener(listener);
    }
    
    public void setBackground (Color color) {
        textField.setBackground(color);
    }
    
    public void setEnabled (boolean enabled) {
        browseButton.setEnabled(enabled);
        textField.setEnabled(enabled);
    }
    
    public void setVisible (boolean visible) {
        browseButton.setVisible(visible);
        textField.setVisible(visible);
    }
    
    protected void saveControlState() { 
        caretPosition = textField.getCaretPosition();
    }
    
    public void setFocus() {
        textField.setFocus();
    }
    
    public Shell getShell () {
    	return textField.getShell();
    }
    
    public void setMenu (Menu menu) {
        textField.setMenu(menu);	
        browseButton.setMenu(menu);
    }

} // class BrowseableField
