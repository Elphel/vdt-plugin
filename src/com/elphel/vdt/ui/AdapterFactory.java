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
package com.elphel.vdt.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
//import com.elphel.vdt.core.verilog.parser.Module;
//import com.elphel.vdt.ui.views.VerilogModuleElement;

/**
 * Adapter factory for verilog module.  
 *
 * Created: 23.03.2006
 * @author  Lvov Konstantin
 */
public class AdapterFactory implements IAdapterFactory {

    /** The supported types that we can adapt to */
    private static final Class[] types = {
        IWorkbenchAdapter.class
    ,   IResource.class  
    ,   IFile.class  
    };    
    
    public Object getAdapter(Object object, Class adapterType) {
    	System.out.println("*** Broken ui.AdapterFactory.getAdapter() for Verilog parser ***");
/*    	
        if (object instanceof Module) {
            Module module = (Module) object;
            if (adapterType == IWorkbenchAdapter.class) {
                return new VerilogModuleElement(module);
            } else if (adapterType == IResource.class) {
                return module.getSourceFile().getFile();
            } else if (adapterType == IFile.class) {
                return module.getSourceFile().getFile();
            }
        }
        */
        return null;
    }

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList() {
        return types;
    }

} // class AdapterFactory
