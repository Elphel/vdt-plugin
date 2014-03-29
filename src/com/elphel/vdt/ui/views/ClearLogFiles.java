/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
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
package com.elphel.vdt.ui.views;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.elphel.vdt.core.tools.params.ToolSequence;
import com.elphel.vdt.veditor.VerilogPlugin;

public class ClearLogFiles extends ClearAction {
	private ToolSequence toolSequence;

    public ClearLogFiles(String message, ToolSequence toolSequence) {
        super(message);
    	this. toolSequence= toolSequence;
    }

//    public void clear() {
//    	toolSequence.clearLogFiles();
//  }
    public void clear() {
    	String msg="The following files will be deleted:\n\n";
		Set<IFile> toRemove=toolSequence.getOldFiles(toolSequence.getLogDirs());
		int index=0;
		for (IFile file:toRemove){
			index++;
//			msg+=file.getLocation().toOSString()+"\n";
			msg+=file.getName()+"\n";
			if (index>25){
				msg+="\n... and more";
				break;
			}
		}
		if (toRemove.size()==0){
			msg="There are no files to be deleted";
		}
        Shell shell = VerilogPlugin.getActiveWorkbenchShell();
        MessageDialog messageBox = new MessageDialog( shell, "Warning", null
                                                , msg
                                                , MessageDialog.QUESTION
                                                , buttonText, 1);
        messageBox.open();
        if (messageBox.getReturnCode() == 0) {
    		for (IFile file:toRemove){
    			try {
//					file.delete(IResource.ALWAYS_DELETE_PROJECT_CONTENT ,null);
					file.delete(true ,null); // force
				} catch (CoreException e) {
					System.out.println("Could not delete "+file.getLocation().toOSString()+", exception:"+e);
				}
    		}
        }
    }
    
} // class ContextsAction
