/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of VDT plug-in.
 * VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VDT plug-in is distributed in the hope that it will be useful,
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.types.ParamTypeBool;
import com.elphel.vdt.core.tools.BooleanUtils;

public class BoolComponent extends GeneralComponent {

    private Button boolField;

    protected DefaultSelectionListeners selectionListener;
    
    public BoolComponent(Parameter param) {
        super(param);
        selectionListener = createSelectionListener();
    }
    
    public void createControl(Composite parent) {
        super.createControl(parent);

        boolField = new Button(parent, SWT.CHECK);
        boolField.setText("");
        boolField.setLayoutData(new GridData());
        
        boolField.setMenu(createPopupMenu(boolField.getShell()));        
        endCreateControl();
    }
    
    public void setSelection(String paramValue) {
        boolField.setSelection(((ParamTypeBool)param.getType()).toBoolean(paramValue));
    }

    protected String getSelection() {
        String value = "";
        if (boolField.getSelection())
            value = new String(BooleanUtils.VALUE_TRUE);
        else
            value = new String(BooleanUtils.VALUE_FALSE);
        return value;
    }
    
    protected boolean isDisposed() {
        return (boolField == null)
            ||  boolField.isDisposed();
    }
    
    public void setEnabled (boolean enabled) {
        super.setEnabled(enabled);
        boolField.setEnabled(enabled);
    }
    
    public void setVisible (boolean visible) {
        super.setVisible(visible);
        boolField.setVisible(visible);
    }

    public void setFocus() {
        boolField.setFocus();
    }
    
    public Button getCheckboxField() {
        return boolField;
    }
    
    protected void addListeners() {
        boolField.addSelectionListener(selectionListener);
    }

    protected void removeListeners() {
        boolField.removeSelectionListener(selectionListener);
    }
    
    protected void switchState(boolean defaulted) {
    }

    protected DefaultSelectionListeners createSelectionListener() {
        return new DefaultSelectionListeners();
    }
    
    //-------------------------------------------------------------------------
    protected class DefaultSelectionListeners extends SelectionAdapter {
        public void widgetSelected(SelectionEvent event) {
            selectionChanged();
        }
    } // class DefaultModifyListeners
    
} // class BoolComponent
