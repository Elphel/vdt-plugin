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

import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog to promt list of files. 
 * 
 * Created: 14.02.2006
 * @author  Lvov Konstantin
 */
public class FileListPromptDialog extends ListPromptDialog {

	public FileListPromptDialog( final Shell parentShell, String title) {
		this(parentShell, title, null);
	}

	
	public FileListPromptDialog( final Shell parentShell
			                   , String title
			                   , final String[] extensions)
	{
		super( parentShell
		     , title
		     , new IAddAction() {
		    	public String getNewValue() {
		            FileDialog dialog = new FileDialog(parentShell);
		            dialog.setFilterExtensions(extensions);
		            String selectedFile = dialog.open();
		            return selectedFile;
		    	}
		    }
		);
	}
	
} // class FileListPromptDialog
