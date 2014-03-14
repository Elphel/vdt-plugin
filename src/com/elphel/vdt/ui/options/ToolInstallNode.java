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
package com.elphel.vdt.ui.options;

import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.ui.MessageUI;

public class ToolInstallNode extends PreferenceNode {

    private Tool tool;
    
    public ToolInstallNode(String id, ImageDescriptor image, Tool tool) {
//        super(id, tool.getLabel(), image, ToolInstallPage.class.getName());
        super(id, tool.getLabel(), image, ToolInstallPage.class.getName());
        this.tool = tool;
    }
    
    public void createPage() {
        try {
        	ToolInstallPage page = ToolInstallPage.class.newInstance();
            page.setContext(tool);
            page.init(PlatformUI.getWorkbench());
            setPage(page);
        } catch (InstantiationException e) {
            MessageUI.error(e);
        } catch (IllegalAccessException e) {
            MessageUI.error(e);
        }
    }
    
} // class PackageInstallNode
