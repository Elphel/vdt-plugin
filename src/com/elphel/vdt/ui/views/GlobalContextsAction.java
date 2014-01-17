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
package com.elphel.vdt.ui.views;

import org.eclipse.swt.widgets.Shell;

//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.ui.options.ContextOptionsDialog;

/**
 * Drop-down action for contexts list.
 * 
 * Created: 12.04.2006
 * @author  Lvov Konstantin
 */
public class GlobalContextsAction extends ContextsAction {

    public GlobalContextsAction(String title) {
        super(title);
    }

    protected ShowContextAction createContextAction(Context context) {
        return new ShowGlobalContextAction(context);
    }
    
    public void run() {
        if (lastSelected != null) {
            openDialog(title, lastSelected);
        }
    }
    
    public static void openDialog(String title, Context context) {
        Shell shell = VerilogPlugin.getActiveWorkbenchShell();
        ContextOptionsDialog dialog = new ContextOptionsDialog(shell, context);
        dialog.setTitle(title);
        dialog.create();
        dialog.open();
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
