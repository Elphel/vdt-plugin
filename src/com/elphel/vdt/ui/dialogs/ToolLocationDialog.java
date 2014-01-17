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


import java.io.File;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.ui.tools.ToolUI;

public class ToolLocationDialog {

    private DirectoryDialog dialog;
    
    public ToolLocationDialog(Shell parent, int style, ToolUI tool) {
        this(parent, style, tool.getTool());
    }

    public ToolLocationDialog(Shell parent, int style, Tool tool) {
        dialog = new DirectoryDialog(parent, style);
        File file = new File(tool.getExeName());
//        String toolExeName = file.getName();
        String toolExeName= tool.getIsShell()?("Shell path ("+file.getName()+") to launch tools"):file.getName();
        dialog.setMessage( Txt.s("Dialog.ToolLocation.Message"
                                , new String[]{tool.getLabel(), toolExeName}) ); 
    }
    
    public String open() {
        return dialog.open();
    }
    
} // class ToolLocationDialog
