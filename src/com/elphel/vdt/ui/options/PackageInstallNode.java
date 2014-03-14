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

import com.elphel.vdt.core.tools.contexts.PackageContext;
import com.elphel.vdt.ui.MessageUI;

public class PackageInstallNode extends PreferenceNode {

    private PackageContext context;
    
    public PackageInstallNode(String id, ImageDescriptor image, PackageContext context) {
        super(id, context.getLabel(), image, PackageInstallPage.class.getName());
        this.context = context;
    }
    
    public void createPage() {
        try {
            PackageInstallPage page = PackageInstallPage.class.newInstance();
            page.setContext(context);
            page.init(PlatformUI.getWorkbench());
            setPage(page);
        } catch (InstantiationException e) {
            MessageUI.error(e);
        } catch (IllegalAccessException e) {
            MessageUI.error(e);
        }
    }
    
} // class PackageInstallNode
