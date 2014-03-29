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
package com.elphel.vdt.ui.variables;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

import com.elphel.vdt.Txt;
import com.elphel.vdt.VDT;


/**
 * Common function of verilog file resolvers.
 *
 * Created: 29.01.2006
 * @author  Lvov Konstantin
 */

public class VerilogResolver implements IDynamicVariableResolver {

    public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
        IResource resource = null;
        if (argument == null) {
            resource = getSelectedResource(variable);
        } else {
            resource = getWorkspaceRoot().findMember(new Path(argument));
        }
        if (resource != null && resource.exists()) {
            resource = translateSelectedResource(resource);
            if (resource != null && resource.exists()) {
                return translateToValue(resource, variable);
            }
        }
        abort(Txt.s("Error.Variable.Verilog.NotExist", new String[]{getReferenceExpression(variable, argument)}), null);
        return null;
    }

    /**
     * Translates the given resource into a value for this variable resolver.
     * 
     * @param resource the resource applicable to this resolver's variable
     * @param variable the variable being resolved
     * @return variable value
     * @throws CoreException if the variable name is not recognized
     */
    protected String translateToValue(IResource resource, IDynamicVariable variable) throws CoreException {
        String name = variable.getName();
        if (name.endsWith("_loc")) { //$NON-NLS-1$
            return resource.getLocation().toOSString();
        } else if (name.endsWith("_path")) { //$NON-NLS-1$
            return resource.getFullPath().toOSString();
        } else if (name.endsWith("_name")) { //$NON-NLS-1$
            return resource.getName();
        }
        abort(Txt.s("Error.Variable.Verilog.Unknown", new String[]{getReferenceExpression(variable, null)}), null); //$NON-NLS-1$
        return null;
    }
    

    /**
     * Returns the selected resource.
     * 
     * @param variable variable referencing a resource
     * @return selected resource
     * @throws CoreException if there is no selection
     */
    protected IResource getSelectedResource(IDynamicVariable variable) throws CoreException {
//        IResource resource = SelectedResourceManager.getDefault().getSelectedVerilogFile();
        IResource resource = SelectedResourceManager.getDefault().getChosenVerilogFile();
        if (resource == null) {
            abort(Txt.s("Error.Variable.Verilog.NoSelection", new String[]{getReferenceExpression(variable, null)}), null);
        }
        return resource;    
    }

    
    /**
     * Returns the resource applicable to this resolver, relative to the selected
     * resource. This method is called when no argument is present in a variable
     * expression. For, example, this method might return the project for the
     * selected resource.
     * 
     * @param resource selected resource
     * @return resource applicable to this variable resolver
     */
    protected IResource translateSelectedResource(IResource resource) {
        return resource;
    }
    
    /**
     * Returns the workspace root
     * 
     * @return workspace root
     */
    protected IWorkspaceRoot getWorkspaceRoot() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }
    
    /**
     * Returns an expression used to reference the given variable and optional argument.
     * For example, <code>${var_name:arg}</code>.
     * 
     * @param variable referenced variable
     * @param argument referenced argument or <code>null</code>
     * @return vraiable reference expression
     */
    protected String getReferenceExpression(IDynamicVariable variable, String argument) {
        StringBuffer reference = new StringBuffer();
        reference.append("${");
        reference.append(variable.getName());
        if (argument != null) {
            reference.append(":");
            reference.append(argument);
        }
        reference.append("}");
        return reference.toString();
    }

    /**
     * Throws an exception with the given message and underlying exception.
     *  
     * @param message exception message
     * @param exception underlying exception or <code>null</code> 
     * @throws CoreException
     */
    protected void abort(String message, Throwable exception) throws CoreException {
        throw new CoreException(new Status(IStatus.ERROR, VDT.ID_VDT, 0, message, exception));
    }
    
} // class VerilogResolver
