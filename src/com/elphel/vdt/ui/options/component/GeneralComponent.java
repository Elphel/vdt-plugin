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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Menu;

import com.elphel.vdt.core.options.ParamBasedOption;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.ToolException;
import com.elphel.vdt.ui.MessageUI;

public abstract class GeneralComponent extends Component {

    protected ParamBasedOption option; 
    
    protected DefaultModifyListeners modifyListener;
    protected Menu popupMenu; 
    
    GeneralComponent(Parameter param) {
        super(param);
        option = new ParamBasedOption(param);
        isDefault = option.isStoredDefault();
        modifyListener = createModifyListener();
    }

    protected void endCreateControl() {
//        System.out.println("    -- GeneralComponent.finalizeCreateControl: id= "+param.getID()+"; label= "+param.getLabel());
        if (loaded)
            setSelection(option.getValue());
        else    
            setSelection(option.doLoad());
        super.endCreateControl();
    }
    
    public void setPreferenceStore(IPreferenceStore store) {
        option.setPreferenceStore(store);
    }

    public String performApply() {
        String value;
        if (isDisposed()) {
            value = null;
        } else if (isDefault) {
            option.doClear();
            value = param.getDefaultValue().get(0);
        } else {   
            value = getSelection();
            option.doStore(value);
        }    
        return value;
    }
    
    protected abstract void setSelection(String value);
    protected abstract String getSelection();

    protected abstract void switchState(boolean defaulted);
    
    protected abstract void addListeners();
    protected abstract void removeListeners();

    protected void setDefault(boolean defaulted) {
//        System.out.println("    -- GeneralComponent.setDefault: id= "+param.getID()+"; label= "+param.getLabel());
        removeListeners();
        super.setDefault(defaulted);
        if (defaulted) {
            param.setToDefault();
            setSelection(param.getDefaultValue().get(0));
        }
//        switchState(defaulted);
        addListeners();
    }

    protected void selectionChanged() {
//        System.out.println("    -- GeneralComponent.selectionChanged: id= "+param.getID()+"; label= "+param.getLabel());
        if (isDefault) {
            setDefault(false);
        }    
        try {
            String value = getSelection();
            param.setCurrentValue(value);
        } catch(ToolException e) {
            MessageUI.error(e);
            return;
        }
        super.selectionChanged();
    }
    
    protected void saveControlState() { }
    
    protected DefaultModifyListeners createModifyListener() {
    	return new DefaultModifyListeners();
    }
    
    //-------------------------------------------------------------------------
    protected class DefaultModifyListeners implements ModifyListener {
        public void modifyText(ModifyEvent e) {
            selectionChanged();
        }
    } // class DefaultModifyListeners

} // class GeneralComponent
