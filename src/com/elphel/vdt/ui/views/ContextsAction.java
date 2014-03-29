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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.ui.VDTPluginImages;

/**
 * Drop-down action for contexts list.
 * 
 * Created: 12.04.2006
 * @author  Lvov Konstantin
 */
abstract public class ContextsAction extends Action
                                     implements IMenuCreator
{
    private Menu menu;

    private List<Context> contexts;

    protected String title;
    protected Context lastSelected;
    protected DesignFlowView designFlowView; // Andrey

    
    public ContextsAction(String title) {
        this.title = title;
        setMenuCreator(this);
        setEnabled(false);
        designFlowView=null;
    }
    
    public void setDesignFlowView(DesignFlowView designFlowView){
    	this.designFlowView=designFlowView;
    }
    
    public void updateActions(boolean updateDirty){
    	if (this.designFlowView!=null) this.designFlowView.updateLaunchAction(updateDirty);
    }
    
    public void setContexts(List<Context> contexts) {
        this.contexts = contexts;
        if (contexts != null) {
            for (Iterator<Context> i = contexts.iterator(); i.hasNext(); ) {
                Context context = i.next();
                if (context.isVisible()) {
                    setEnabled(true);
                    lastSelected = context;
                    return;
                }
            }
        }
        setEnabled(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    public Menu getMenu(Control parent) {
        if (menu != null) {
            menu.dispose();
        }
        
        menu = new Menu(parent);
        for (Iterator<Context> i = contexts.iterator(); i.hasNext(); ) {
            Context context = (Context)i.next();
            if (context.isVisible()) {
                Action action = createContextAction(context);
                ActionContributionItem item= new ActionContributionItem(action);
                item.fill(menu, -1);
            }
        }
        return menu;
    }

    abstract protected ShowContextAction createContextAction(Context context);
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
     */
    public Menu getMenu(Menu parent) {
        return null;
    }

    // ------------------------------------------------------------------------
    protected abstract class ShowContextAction extends Action {
        protected Context context; 
        
        ShowContextAction(Context context) {
            this.context = context;
            
            setText(context.getLabel());
            
            ImageDescriptor image = VDTPluginImages.getImageDescriptor(context);
            if (image == null) {
                image =  ContextsAction.this.getImageDescriptor(); // VDTPluginImages.DESC_PACKAGE_PROPERTIES;
            }
            setImageDescriptor(image);
        }
        
        abstract public void run();
    } // class ShowContextAction
    
} // class ContextsAction
