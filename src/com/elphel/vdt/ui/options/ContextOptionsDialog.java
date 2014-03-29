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
import com.elphel.vdt.veditor.preference.PreferenceStrings;
import com.elphel.vdt.core.options.OptionsCore;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.contexts.PackageContext;
import com.elphel.vdt.core.tools.params.ToolException;
import com.elphel.vdt.core.tools.params.ToolSequence;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.views.DesignFlowView;

public class ContextOptionsDialog extends Dialog {

    private String title; 
    
    private final Context context;
    private IPreferenceStore store; 
    private OptionsBlock optionsBlock;
    private String location;  
//    private DesignFlowView designFlowView; // Andrey
    public ContextOptionsDialog(Shell parent, Context context, IProject project) {
        this( parent
            , context
            , OptionsCore.getPreferenceStore(context, project)
            );
        location = project.getLocation().toOSString(); // project location
//        System.out.println("ContextOptionsDialog: location was "+location);
//        location = project.getProjectRelativePath().toOSString();
//        System.out.println("ContextOptionsDialog: location changed to "+location);
        		
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
  //      this.designFlowView=null; // Andrey
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
        context.recalcHashCodes();
        
        if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        	System.out.println("ContexOptionsDialog.okPressed()");
        // Need to update Design menu as it uses calculated parameters
        super.okPressed();
//        if (this.designFlowView!=null) this.designFlowView.updateLaunchAction();
    }
    
//    public void setDesignFlowView(DesignFlowView designFlowView){
//    	this.designFlowView=designFlowView;
//    };
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
