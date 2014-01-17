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
package com.elphel.vdt.ui.wizards.project;

import com.elphel.vdt.core.tools.menu.DesignMenu;

/**
 * This class provide information captured in the 'New Verilog Project' wizard
 * pages as entered by the user.
 * The information is the provided to other consumers when generating content so
 * that the content can be configured/customized according to the data.
 * 
 * Created: 18.02.2006
 * @author  Lvov Konstantin
 */

public class ProjectData {

    private String projectName;
    DesignMenu designMenu;
    
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public DesignMenu getDesignMenu() {
        return designMenu;
    }

    public void setDesignMenu(DesignMenu menu) {
        designMenu = menu;
    }
    
} // class ProjectData
