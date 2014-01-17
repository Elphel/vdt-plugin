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
package com.elphel.vdt.ui.views;

import java.util.Iterator;
import java.util.List;

import com.elphel.vdt.core.options.OptionsCore;
import com.elphel.vdt.core.tools.contexts.Context;

public class ClearContextAction extends ClearAction {

    private List<Context> contexts;
    
    public ClearContextAction(String message) {
        super(message);
    }

    public void setContexts(List<Context> contexts) {
        this.contexts = contexts;
        if (contexts != null) {
            for (Iterator<Context> i = contexts.iterator(); i.hasNext(); ) {
                Context context = i.next();
                if (context.isVisible()) {
                    setEnabled(true);
                    return;
                }
            }
        }
        setEnabled(false);
    }

    public void clear() {
        for (Iterator<Context> i = contexts.iterator(); i.hasNext(); ) {
            Context context = (Context)i.next();
            if (context.isVisible()) {
                clear(context);
            }
        }
    }
    
    public void clear(Context context) {
        OptionsCore.doClearContextOptions(context);
    }
    
} // class ContextsAction
