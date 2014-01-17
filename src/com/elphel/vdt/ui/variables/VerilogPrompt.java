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
package com.elphel.vdt.ui.variables;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

import com.elphel.vdt.Txt;


/**
 * Prompts the user to choose a verilog file and expands the selection
 *
 * Created: 02.02.2006
 * @author  Lvov Konstantin
 */
public class VerilogPrompt extends PromptingResolver {

    /**
     * Prompts the user to choose a file @see PromptExpanderBase#prompt()
     */
    public void prompt() {
        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider());
        dialog.setTitle(Txt.s("Variable.Verilog.Promp.Dialog.Title")); 
        dialog.setMessage(Txt.s("Variable.Verilog.Promp.Dialog.Message")); 
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot()); 
        dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));
        if (dialog.open() == Window.OK) {
            IResource resource = (IResource) dialog.getFirstResult();
//            dialogResultString = resource.getLocation().toOSString();
            dialogResultString = resource.getFullPath().toOSString();
        }    
    } // prompt()
    
} // class VerilogPrompt
