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
package com.elphel.vdt.ui.variables;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.debug.core.DebugException;
import org.eclipse.swt.widgets.Shell;

import com.elphel.vdt.Txt;
import com.elphel.vdt.VDT;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;


/**
 * Base implementation for variable resolvers that prompt the user
 * for their value. 
 *
 * @see org.eclipse.debug.internal.ui.stringsubstitution.PromptingResolver
 *
 * Created: 02.02.2006
 * @author  Lvov Konstantin
 */
public abstract class PromptingResolver  implements IDynamicVariableResolver {
    /**
     * A hint that helps the user choose their input. If a prompt
     * hint is provider the user will be prompted:
     *  Please input a value for <code>promptHint</code>
     */
    protected String promptHint = null;
    /**
     * The prompt displayed to the user.
     */
    protected String dialogMessage = null;
    /**
     * The default value selected when the prompt is displayed
     */
    protected String defaultValue = null;
    /**
     * The last value chosen by the user for this variable 
     */
    protected String lastValue = null;
    /**
     * The result returned from the prompt dialog
     */
    protected String dialogResultString = null;
    
    /**
     * Presents the user with the appropriate prompt for the variable to be expanded
     * and sets the <code>dialogResultString</code> based on the user's selection.
     */
    public abstract void prompt();

    /**
     * Initializes values displayed when the user is prompted. If
     * a prompt hint and default value are supplied in the given
     * variable value, these are extracted for presentation
     * 
     * @param varValue the value of the variable from which the prompt
     * hint and default value will be extracted
     */
    protected void setupDialog(String varValue) {
        promptHint = null;
        defaultValue = null;
        dialogResultString = null;
        if (varValue != null) {
            int idx = varValue.indexOf(':');
            if (idx != -1) {
                promptHint = varValue.substring(0, idx);
                defaultValue = varValue.substring(idx + 1);
            } else {
                promptHint = varValue;
            }
        }

        if (promptHint != null) {
            dialogMessage = Txt.s("Variable.Verilog.Promp.Hint1", new String[] {promptHint}); 
        } else {
            dialogMessage = Txt.s("Variable.Promp.Hint2"); 
        }
    } // setupDialog()

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.core.stringsubstitution.IContextVariableResolver#resolveValue(org.eclipse.debug.internal.core.stringsubstitution.IContextVariable, java.lang.String)
     */
    public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
        String value = null;
        setupDialog(argument);

        VerilogPlugin.getStandardDisplay().syncExec(new Runnable() {
            public void run() {
                prompt();
            }
        });
        if (dialogResultString != null) {
            value = dialogResultString;
            lastValue = dialogResultString;
        } else {
            // dialogResultString == null means prompt was cancelled
            throw new DebugException(new Status(IStatus.CANCEL, VDT.ID_VDT, IStatus.CANCEL, Txt.s("Variable.Promp.Cancel.Message", new String[] { variable.getName() }), null)); 
        }
        return value;
    }
    
    protected Shell getShell() {
        Shell shell = VerilogPlugin.getStandardDisplay().getActiveShell();
        if (shell == null) {
            shell = VerilogPlugin.getActiveWorkbenchShell();
        }
        return shell;
    } // getShell()
    
} // class PromptingResolver
