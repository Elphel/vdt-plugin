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
package com.elphel.vdt.ui.options;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.elphel.vdt.Txt;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.core.options.OptionsCore;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.contexts.PackageContext;
import com.elphel.vdt.core.tools.params.ToolException;
import com.elphel.vdt.ui.MessageUI;

public class ContextOptionsDialog extends Dialog {

    private String title; 
    
    private final Context context;
    private IPreferenceStore store; 
    private OptionsBlock optionsBlock;
    private String location;  
    
    public ContextOptionsDialog(Shell parent, Context context, IProject project) {
        this( parent
            , context
            , OptionsCore.getPreferenceStore(context, project)
            );
        location = project.getLocation().toOSString();
    }

    public ContextOptionsDialog(Shell parent, Context context) {
        this(parent, context, VerilogPlugin.getDefault().getPreferenceStore());
        if (context instanceof PackageContext)
            location = OptionsCore.getPackageLocation((PackageContext)context);
    }

    @SuppressWarnings("ucd")
    public ContextOptionsDialog(Shell parent, Context context, IPreferenceStore store) {
        super(parent);
        this.context = context;
        this.store = store;
        OptionsCore.doLoadContextOptions(context, store);
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }
    
    protected void okPressed() {
    	optionsBlock.performApply();
        OptionsCore.doStoreContextOptions(context, store);
        context.setWorkingDirectory(location);
        try {
            context.buildParams();    
        } catch (ToolException e) {
            MessageUI.error(Txt.s( "Action.Context.Save.Error" 
                                 , new String[] {context.getLabel(), e.getMessage()})
                                 , e );
        }
        super.okPressed();
    }
    
    protected Control createDialogArea(Composite parent) {
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 5;
        layout.marginWidth  = 5;
        layout.verticalSpacing   = 0;
        layout.horizontalSpacing = 0;
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);


        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(layout);
        panel.setLayoutData(gridData);

        optionsBlock = new OptionsBlock(panel, context);
        
        // final actions
        applyDialogFont(panel);
        getShell().setText(getTitle());

        return panel;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    protected String getTitle() {
        if ((context != null) && (context.getInputDialogLabel() != null))
            title = context.getInputDialogLabel();

        if (title == null)
            title = "Parameters";
        
        return title;
    }
    
} // class ContextOptionsDialog
