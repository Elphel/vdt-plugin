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
package com.elphel.vdt.ui.tools;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.types.ParamType;
import com.elphel.vdt.core.tools.params.types.ParamTypeBool;
import com.elphel.vdt.core.tools.params.types.ParamTypeEnum;
import com.elphel.vdt.core.tools.params.types.ParamTypeNumber;
import com.elphel.vdt.core.tools.params.types.ParamTypeString;
import com.elphel.vdt.ui.tools.params.AbstractTabComponent;
import com.elphel.vdt.ui.tools.params.BoolTabComponent;
import com.elphel.vdt.ui.tools.params.DirTabComponent;
import com.elphel.vdt.ui.tools.params.FileTabComponent;
import com.elphel.vdt.ui.tools.params.ListTabComponent;
import com.elphel.vdt.ui.tools.params.NumberTabComponent;
import com.elphel.vdt.ui.tools.params.TextTabComponent;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.Txt;


/**
 * This tab appears for Verilog tool launch configurations and allows 
 * the user to edit tool parameters.
 * 
 * Created: 27.12.2005
 * @author  Lvov Konstantin
 */

public class LaunchConfigurationTab extends AbstractLaunchConfigurationTab {

    private String tabCaption;
    private ToolUI toolUI;
    private String paramGroup;
    private String toolLocation;    
    private List<AbstractTabComponent> components;
    
    private boolean isInitializing = false;
    private boolean userEdited     = false;
    
    public final static String FIRST_EDIT = "editedByVDTLaunchConfiguration";

    public ToolUI getToolUI() { return toolUI; }
    
    public LaunchConfigurationTab(String caption, ToolUI toolUI, String paramGroup) {
        super();
        
        this.tabCaption = caption;
        this.toolUI = toolUI;
        this.paramGroup = paramGroup;
        this.toolLocation = toolUI.getToolLocation();
        
        createComponents();
    } // LaunchConfigurationTab()
        
    private void createComponents() {
        components = new ArrayList<AbstractTabComponent>();
//        for (Iterator i = toolUI.getTool().getParams().iterator(); i.hasNext(); ) {
        for (Iterator<Parameter> i = toolUI.getTool().getParams().iterator(); i.hasNext(); ) {
            Parameter param = (Parameter)i.next();
            
            if(!param.isVisible())
                continue;
            
            String group = null;//param.getGroupName();
            
            if(group == paramGroup || paramGroup.equals(group)) {
                ParamType paramType = param.getType();
                
                if (paramType instanceof ParamTypeNumber) {
                    components.add(new NumberTabComponent(this, param));         
                } else if (paramType instanceof ParamTypeBool) {
                    components.add(new BoolTabComponent(this, param));         
                } else if (paramType instanceof ParamTypeString) {
                    if(((ParamTypeString)paramType).getKind() == ParamTypeString.KIND.FILE)
                        components.add(new FileTabComponent(this, param));
                    else if(((ParamTypeString)paramType).getKind() == ParamTypeString.KIND.DIR)
                        components.add(new DirTabComponent(this, param));
                    else
                        components.add(new TextTabComponent(this, param));
                } else if (paramType instanceof ParamTypeEnum) {
                    components.add(new ListTabComponent(this, param));         
                } else {
                    MessageUI.error(Txt.s("LaunchTab.Error.UnsupportedType", param.getType().getName()));
                }
            }
        }
    } // createComponets()
        
    public void createControl(Composite parent) {
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 5;
        layout.marginWidth  = 5;
        layout.verticalSpacing   = 3;
        layout.horizontalSpacing = 5;

        Composite panel = new Composite(parent, SWT.NONE);

        for (Iterator<AbstractTabComponent> i = components.iterator(); i.hasNext();)
            ((AbstractTabComponent)i.next()).createControl(panel);

        // do the common stuff
        setControl(panel);
        panel.setFont(parent.getFont());
        
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);

        panel.setLayout(layout);
        panel.setLayoutData(gridData);

        createVerticalSpacer(panel, 1);

        Dialog.applyDialogFont(parent);
    } // createControl()

    /**
     * Initializes the given launch configuration with default values for this 
     * tab.
     */ 
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(FIRST_EDIT, true);
        for (Iterator<AbstractTabComponent> i = components.iterator(); i.hasNext(); ) {
            ((AbstractTabComponent)i.next()).setDefaults(configuration);
        }
    } // setDefaults()

    /**
     * Updates the tab's widgets to match the state of the given launch 
     * configuration.
     */
    public void initializeFrom(ILaunchConfiguration configuration) {
        isInitializing = true;
        for (Iterator<AbstractTabComponent> i = components.iterator(); i.hasNext(); ) {
            ((AbstractTabComponent)i.next()).initializeFrom(configuration);
        }
        isInitializing = false;
        setDirty(false);
    } // initializeFrom()

    /**
     * Copies values from this tab into the given launch configuration.
     */
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        for (Iterator<AbstractTabComponent> i = components.iterator(); i.hasNext(); ) {
            AbstractTabComponent paramUI = ((AbstractTabComponent)i.next()); 
            paramUI.performApply(configuration);
        }
        if (userEdited) {
            configuration.setAttribute(FIRST_EDIT, false);
        }
    } // performApply()

    /**
     * Validates the content of the tab components.
     */
    public boolean isValid(ILaunchConfiguration launchConfig) {
        setErrorMessage(null);
        setMessage(null);
        boolean newConfig = false;
//        try {
//            newConfig = launchConfig.getAttribute(FIRST_EDIT, false);
//        } catch (CoreException e) {
//                //assume false is correct
//        }

        if (!validateToolLocation()) {
        	setErrorMessage(Txt.s("LaunchTab.Error.ToolLocation"
        			, new Object[] {toolUI.getTool().getLabel(), toolLocation}
        			));
            return false;
        }
                
        for (Iterator<AbstractTabComponent> i = components.iterator(); i.hasNext(); ) {
        if (!((AbstractTabComponent)i.next()).isValid(newConfig))
            return false;
        }
        return true;
    } // isValid()
        
    /**
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
     */
    public String getName() {
        return tabCaption;
    } // getName()

    /**
     * Returns the shell this tab is contained in, or <code>null</code>.
     */
    public Shell getShell() {
        return super.getShell();
    }
    
    public void textModifyedNotification() { 
        if (!isInitializing) {
            setDirty(true);
            userEdited = true;
            updateLaunchConfigurationDialog();
        }
    } // textModifyedNotification()
    
    public void dirtyNotification() {
        setDirty(true);
    }
    
    /**
     * Sets this page's error message, possibly <code>null</code>.
     * 
     * @param errorMessage the error message or <code>null</code>
     */
    public void setErrorMessage(String errorMessage) {
        super.setErrorMessage(errorMessage);
    }

    /**
     * Sets this page's message, possibly <code>null</code>.
     * 
     * @param message the message or <code>null</code>
     */
    public void setMessage(String message) {
        super.setMessage(message);
    }
        
    private boolean validateToolLocation() {
        File file = new File(toolLocation);
        return (file != null)
            &&  file.exists()
            &&  file.isFile()
             ;           
    }
        
} // LaunchConfigurationTab
