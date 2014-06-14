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
package com.elphel.vdt.ui.options;

import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.ui.MessageUI;

public class ContextPreferenceNode extends PreferenceNode {

    private Context context;
    
    public ContextPreferenceNode(String id, Context context) {
        this(id, null, context);	
    }
    
    public ContextPreferenceNode(String id, ImageDescriptor image, Context context) {
        super(id, context.getLabel(), image, ContextOptionsPage.class.getName());
        this.context = context;
    }
    
    public void createPage() {
        try {
            ContextOptionsPage page = ContextOptionsPage.class.newInstance();
            page.setContext(context);
            page.init(PlatformUI.getWorkbench());
            setPage(page);
        } catch (InstantiationException e) {
            MessageUI.error(e);
        } catch (IllegalAccessException e) {
            MessageUI.error(e);
        }
    }
    
} // class PluginPreferenceNode
