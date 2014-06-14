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
package com.elphel.vdt.ui.launching;


import org.eclipse.debug.ui.actions.AbstractLaunchToolbarAction;
import org.eclipse.jface.action.IAction;

import com.elphel.vdt.VDT;


/**
 * This action delegate is responsible for producing the
 * Run > Verilog Tools sub menu contents, which includes
 * an items to run last tool, favorite tools, and show the
 * veroilog tools launch configuration dialog.
 * 
 * Created: 23.12.2005
 * @author lion
 */
public class VDTLaunchMenuDelegate extends AbstractLaunchToolbarAction {

	/**
	 * Creates the action delegate
	 */
	public VDTLaunchMenuDelegate() {
		super(VDT.ID_VERILOG_TOOLS_LAUNCH_GROUP);
	}
	
	/**
	 * @see org.eclipse.debug.ui.actions.AbstractLaunchToolbarAction#getOpenDialogAction()
	 */
	protected IAction getOpenDialogAction() {
		IAction action= new OpenVerilogToolsConfigurations();
		action.setActionDefinitionId(VDT.COMMAND_OPEN_VERILOG_TOOLS_LAUNCH_DIALOG);
		return action;
	} // getOpenDialogAction()

	
	public static void openVDTConfigurationDialog() {
		IAction action = new VDTLaunchMenuDelegate().getOpenDialogAction();
		action.run();
	}
	
} // class VDTLaunchMenuDelegate
