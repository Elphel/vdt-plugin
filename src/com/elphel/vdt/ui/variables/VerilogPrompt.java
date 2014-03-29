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
