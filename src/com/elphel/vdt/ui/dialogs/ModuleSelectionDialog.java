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
package com.elphel.vdt.ui.dialogs;


import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.elphel.vdt.Txt;
//import com.elphel.vdt.core.verilog.parser.Module;

public class ModuleSelectionDialog extends ElementListSelectionDialog {

    public ModuleSelectionDialog(Shell shell) {
        this(shell, Txt.s("Dialog.ModuleSelection.Message"));
    }
    

    public ModuleSelectionDialog(Shell shell, String message) {
        super(shell, new ModuleLabelProvider());
        setTitle(Txt.s("Dialog.ModuleSelection.Caption"));
        if (message != null)
            setMessage(message);
        setMultipleSelection(false);
        setIgnoreCase(true);
    }
    
    private static class ModuleLabelProvider extends LabelProvider {
        public String getText(Object element) {
        	System.out.println("*** Broken ui.dialogs.ModuleSelectionDialog.ModuleLabelProvider() for Verilog parser ***");
        	return "*** broken ***";
//            return element == null ? "": ((Module)element).getName();
        }
    } // class ToolLabelProvider
/*    
    public Module open(Module[] modules) {
        setElements(modules);
        if (super.open() == Window.OK)
            return (Module)getResult()[0];
        else
            return null;
    }
*/    
} // class ModuleSelectionDialog
