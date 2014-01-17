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

import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.types.ParamTypeString;
import com.elphel.vdt.ui.dialogs.FileListPromptDialog;
import com.elphel.vdt.ui.dialogs.ListPromptDialog;

public class FileListComponent extends ListComponent {

    public FileListComponent(Parameter param) {
        super(param);
    }
    
    protected ListPromptDialog createDialog() {
        String filemask = ((ParamTypeString)param.getType()).getFilemask();
        final String[] extensions = (filemask != null? new String[] { filemask } : null);

        return new FileListPromptDialog( promptField.getVisibleNameField().getShell()
                                       , "File list prompt"
                                       , extensions );
    }

}
