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
package com.elphel.vdt.ui.options.component;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.elphel.vdt.core.tools.params.Parameter;

public class NumberComponent extends GeneralComponent {

    private int caretPosition;

    private Text numberField;
    
    
    public NumberComponent(Parameter param) {
        super(param);
        caretPosition = -1;
    }
    
    public void createControl(Composite parent) {
        super.createControl(parent);
       
        numberField = Component.createTextControl(parent);
        numberField.isDisposed();
        
        numberField.setMenu(createPopupMenu(numberField.getShell()));
        endCreateControl();
    }

    public void setSelection(String value) {
        numberField.setText(value);
        if (caretPosition < 0) {
            numberField.setSelection(value.length());
        } else {
            numberField.setSelection(caretPosition);
            caretPosition = -1;
        }
    }
    
    protected String getSelection() {
        String number = numberField.getText().trim();
        return number;
    }
    
    protected boolean isDisposed() {
        return (numberField == null)
            ||  numberField.isDisposed();
    }

    public void setEnabled (boolean enabled) {
        super.setEnabled(enabled);
        numberField.setEnabled(enabled);
    }
    
    public void setVisible (boolean visible) {
        super.setVisible(visible);
        numberField.setVisible(visible);
    }
    
    protected void saveControlState() { 
        caretPosition = numberField.getCaretPosition();
    }
    
    public void setFocus() {
        numberField.setFocus();
    }
    
    public Text getTextField() {
        return numberField;
    }
    
    protected void addListeners() {
        numberField.addModifyListener(modifyListener);
    }

    protected void removeListeners() {
        numberField.removeModifyListener(modifyListener);
    }
    
    protected void switchState(boolean defaulted) {
        numberField.setBackground(defaulted ? colorBackgroundDefault
                                            : colorBackground );     
    }
    
} // class NumberComponent
