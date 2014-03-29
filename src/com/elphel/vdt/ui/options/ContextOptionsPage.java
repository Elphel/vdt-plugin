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

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.elphel.vdt.Txt;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;
import com.elphel.vdt.core.options.OptionsCore;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.params.ToolException;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.preferences.ScrolledPageContent;

public class ContextOptionsPage extends PreferencePage 
                                implements IWorkbenchPreferencePage
{

    private Context context;
    private OptionsBlock optionsBlock;

    public ContextOptionsPage() {
        super();
        noDefaultAndApplyButton();
    }
    
    public ContextOptionsPage(String title, Context context) {
        super(title);
        this.context = context;
        noDefaultAndApplyButton();
    }

    public void setContext(Context context) {
        this.context = context;
        setTitle(context.getLabel());
    }
    
    protected Control createContents(Composite parent) {
        ScrolledPageContent panel = createScrolledPanel(parent);
        optionsBlock = new OptionsBlock(panel.getBody(), context);
        return panel;
    }

    private ScrolledPageContent createScrolledPanel(Composite parent) {
        ScrolledPageContent panel = new ScrolledPageContent(parent);
        panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Composite content = panel.getBody();
        GridLayout layout = new GridLayout();
        content.setLayout(layout);
        content.setFont(parent.getFont());
        content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return panel;
    }
    
    public boolean performOk() {
        optionsBlock.performApply();
        OptionsCore.doStoreContextOptions(context, getPreferenceStore());
        try {
            context.buildParams();    
        } catch (ToolException e) {
            MessageUI.error(Txt.s( "Action.Context.Save.Error" 
                                 , new String[] {context.getLabel(), e.getMessage()})
                                 , e );
        }
        if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        	System.out.println("ContexOptionsPage.performOK()");
        return super.performOk();
    }
    
    public void init(IWorkbench workbench) {
        setPreferenceStore(VerilogPlugin.getDefault().getPreferenceStore());
        OptionsCore.doLoadContextOptions(context, getPreferenceStore());
    }

} // class ContextOptionsPage
