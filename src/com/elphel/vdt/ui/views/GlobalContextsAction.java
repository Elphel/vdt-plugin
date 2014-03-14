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
package com.elphel.vdt.ui.views;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;




//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.ui.options.ContextOptionsDialog;

/**
 * Drop-down action for contexts list.
 * 
 * Created: 12.04.2006
 * @author  Lvov Konstantin
 */
public class GlobalContextsAction extends ContextsAction {

    public GlobalContextsAction(String title, DesignFlowView designFlowView) {
        super(title);
        super.setDesignFlowView(designFlowView);
    }


    protected ShowContextAction createContextAction(Context context) {
        return new ShowGlobalContextAction(context);
    }
    
    public void run() {
        if (lastSelected != null) {
//            openDialog(title, lastSelected);
            if (openDialog(title, lastSelected)== Window.OK){
            	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
            		System.out.println("GlobalContextsAction.run()");
            	}
            	super.updateActions(true);
            }
        }
    }
    
    public static int openDialog(String title, Context context) {
        Shell shell = VerilogPlugin.getActiveWorkbenchShell();
        ContextOptionsDialog dialog = new ContextOptionsDialog(shell, context);
        dialog.setTitle(title);
        dialog.create();
        return dialog.open();
    } // openDialog()
    
    // ------------------------------------------------------------------------
    protected class ShowGlobalContextAction extends ShowContextAction {

        ShowGlobalContextAction(Context context) {
            super(context);
        }
        
        public void run() {
            lastSelected = context;
            openDialog(title, lastSelected);
        }
    } // class ShowContextAction
    
} // class GlobalContextsAction
