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


import org.eclipse.debug.ui.actions.OpenLaunchDialogAction;

import com.elphel.vdt.VDT;


/**
 * Opens the launch config dialog on the verilog tools launch group.
 * 
 * Created: 23.12.2005
 * @author lion
 */

public class OpenVerilogToolsConfigurations extends OpenLaunchDialogAction {

	public OpenVerilogToolsConfigurations() {
		super(VDT.ID_VERILOG_TOOLS_LAUNCH_GROUP);
	}

} // class OpenVerilogToolsConfigurations
