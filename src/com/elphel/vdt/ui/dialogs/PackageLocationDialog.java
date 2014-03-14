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
package com.elphel.vdt.ui.dialogs;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import com.elphel.vdt.core.tools.contexts.PackageContext;

public class PackageLocationDialog {

    private DirectoryDialog dialog;
    
    public PackageLocationDialog(Shell parent, int style, PackageContext context) {
        dialog = new DirectoryDialog(parent, style);
        dialog.setMessage("Specify the location of \""+context.getLabel()+"\""); 
    }
    
    public String open() {
        return dialog.open();
    }
    
} // class PackageLocationDialog
