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
