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

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

import com.elphel.vdt.core.options.ParamBasedListOption;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.ToolException;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.dialogs.ListPromptDialog;

public abstract class ListComponent extends Component {

    protected ParamBasedListOption option;

    protected ListPromptField promptField;
    
    public ListComponent(Parameter param) {
        super(param);
        option = new ParamBasedListOption(param);
        isDefault = option.isStoredDefault();
        promptField = new ListPromptField(new PromptAction());   
    }
    
    public void createControl(Composite parent) {
        super.createControl(parent);

        promptField.createControl(parent);   
        promptField.setMenu(createPopupMenu(promptField.getShell()));
        endCreateControl();
    }
    
    protected void endCreateControl() {
//        System.out.println("    -- ListComponent.finalizeCreateControl: id= "+param.getID()+"; label= "+param.getLabel());
        if (loaded)
            promptField.setList(option.getValueList());
        else    
            promptField.setList(option.doLoadList());
        super.endCreateControl();
    }
    
    public void setEnabled (boolean enabled) {
        super.setEnabled(enabled);
        promptField.setEnabled(enabled);
    }

    public void setVisible (boolean visible) {
        super.setVisible(visible);
        promptField.setVisible(visible);
    }
    
    protected void saveControlState() { }
    
    public void setFocus() {
        promptField.setFocus();
    }
    
    protected List<String> getSelection() {
        return promptField.getList();
    }
    
    protected boolean isDisposed() {
        return promptField.isDisposed();
    }
    
    public String performApply() {
        if (isDisposed()) {
        } else if (isDefault) {
            option.doClear();
        } else {    
            List<String> items = getSelection();
            option.doStore(items);
        }  
        return null;
    }

    public void setPreferenceStore(IPreferenceStore store) {
        option.setPreferenceStore(store);
    }
    
    protected abstract ListPromptDialog createDialog();

    protected void setDefault(boolean defaulted) {
//        System.out.println("-- ListComponent.setDefault: " + this);
//        System.out.println("                promptField: " + promptField);
//        System.out.println("    -- ListComponent.setDefault: id= "+param.getID()+"; label= "+param.getLabel());
        removeListeners();
        super.setDefault(defaulted);
        if (defaulted) {
            param.setToDefault();
            promptField.setList(param.getDefaultValue(null)); // null for topFormatProcessor
        }
//        switchState(defaulted);
        addListeners();
    }

    protected void switchState(boolean defaulted) {
        promptField.setBackground(defaulted ? colorBackgroundDefault
                                            : colorBackground );     
    }
    
    protected void selectionChanged() {
//        System.out.println("    -- ListComponent.selectionChanged: id= "+param.getID()+"; label= "+param.getLabel());
        if (isDefault) {
            setDefault(false);
        }    
        try {
            List<String> value = getSelection();
            param.setCurrentValue(value);
        } catch(ToolException e) {
            MessageUI.error(e);
            return;
        }
        super.selectionChanged();
    }
    
    protected void addListeners() {
        promptField.addListeners();
    }

    protected void removeListeners() {
        promptField.removeListeners();
    }
    
    //-------------------------------------------------------------------------
    protected class PromptAction implements ListPromptField.IPromptAction {
        private int returnCode;
        public List<String> prompt(List<String> current) {
//            System.out.println("-- id= "+param.getID()+"; label= "+param.getLabel());
//            new Exception().printStackTrace();
            if (promptField.getPromptDialog() == null)
                promptField.setPromptDialog(createDialog());
            
            ListPromptDialog dialog = promptField.getPromptDialog(); // clicked on edit list button
            List<String> list = dialog.open(current); // opened list dialog, running
            returnCode = dialog.getReturnCode();   // canceled list
            return list;
        }
        public void slectionChanged() {
            selectionChanged();
        }
        public int getReturnCode() {
            return returnCode;
        }
    } // class PromptAction
    
} // class ListComponent
