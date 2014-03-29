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
package com.elphel.vdt.ui.options.component;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.elphel.vdt.core.tools.params.Parameter;

public class TextComponent extends GeneralComponent {

    private int caretPosition;

    private Text textField;

    public TextComponent(Parameter param) {
        super(param);
        caretPosition = -1;
    }
    
    public void createControl(Composite parent) {
        super.createControl(parent);

        textField = createTextControl(parent);

        textField.setMenu(createPopupMenu(textField.getShell()));        
        endCreateControl();
    }
    
    public void setSelection(String value) {
        textField.setText(value);
        if (caretPosition < 0) {
            textField.setSelection(value.length());
        } else {
            textField.setSelection(caretPosition);
            caretPosition = -1;
        }
    }
    
    protected String getSelection() {
        String value = textField.getText();
        return value;
    }
    
    protected boolean isDisposed() {
        return (textField == null)
            ||  textField.isDisposed();
    }
    
    public void setEnabled (boolean enabled) {
        super.setEnabled(enabled);
        textField.setEnabled(enabled);
    }
    
    public void setVisible (boolean visible) {
        super.setVisible(visible);
        textField.setVisible(visible);
    }
    
    protected void saveControlState() { 
        caretPosition = textField.getCaretPosition();
    }
    
    public void setFocus() {
        textField.setFocus();
    }
    
    public Text getTextField() {
        return textField;
    }
    
    protected void addListeners() {
        textField.addModifyListener(modifyListener);
    }

    protected void removeListeners() {
        textField.removeModifyListener(modifyListener);
    }
    
    protected void switchState(boolean defaulted) {
        textField.setBackground(defaulted ? colorBackgroundDefault
                                          : colorBackground );     
    }
    
} // class TextComponent
