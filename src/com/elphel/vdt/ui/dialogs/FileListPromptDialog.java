/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of VDT plug-in.
 * VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VDT plug-in is distributed in the hope that it will be useful,
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
package com.elphel.vdt.ui.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.elphel.vdt.ui.variables.SelectedResourceManager;


/**
 * Dialog to promt list of files. 
 * 
 * Created: 14.02.2006
 * @author  Lvov Konstantin
 */
public class FileListPromptDialog extends ListPromptDialog {
/*
	public FileListPromptDialog( final Shell parentShell, String title) {
		this(parentShell, title, null,null);
	}
	public FileListPromptDialog( final Shell parentShell,
			String title,
			final String[] extensions
			)
	{
		this(parentShell, title, extensions,null);

	}
*/
	public FileListPromptDialog( final Shell parentShell,
			String title,
			final String[] extensions,
			final String projectPath
			)
	{
		super( parentShell
				, title
				, new IAddAction() {
			public String getNewValue() {
				FileDialog dialog = new FileDialog(parentShell);
				dialog.setFilterExtensions(extensions);
				if (projectPath!=null){
					dialog.setFileName(projectPath);
				}
				String selectedFile = dialog.open();
// try to convert to project-relative
		    	if ((selectedFile!=null) && selectedFile.startsWith(projectPath)) {
		        	if (selectedFile.equals(projectPath)){
		        		System.out.println("FileListPromptDialog(): Path equals to project path = \""+selectedFile+"\", returning \".\"");
		        		return ".";
		        	}
		    		return selectedFile.substring(projectPath.length()+1);
		    	}
		    	return selectedFile;
			}
		}
				);
	}
} // class FileListPromptDialog
