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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.elphel.vdt.core.tools.config.xml.XMLConfig;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.params.ParamGroup;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.types.ParamType;
import com.elphel.vdt.core.tools.params.types.ParamTypeBool;
import com.elphel.vdt.core.tools.params.types.ParamTypeEnum;
import com.elphel.vdt.core.tools.params.types.ParamTypeNumber;
import com.elphel.vdt.core.tools.params.types.ParamTypeString;
import com.elphel.vdt.ui.options.component.BoolComponent;
import com.elphel.vdt.ui.options.component.ComboComponent;
import com.elphel.vdt.ui.options.component.Component;
import com.elphel.vdt.ui.options.component.DirComponent;
import com.elphel.vdt.ui.options.component.DirListComponent;
import com.elphel.vdt.ui.options.component.FileComponent;
import com.elphel.vdt.ui.options.component.FileListComponent;
import com.elphel.vdt.ui.options.component.LabelComponent;
import com.elphel.vdt.ui.options.component.NumberComponent;
import com.elphel.vdt.ui.options.component.StringListComponent;
import com.elphel.vdt.ui.options.component.TextComponent;

/**
 * Fills in a panel with controls for editing option of the context.
 * 
 * Created: 17.03.2006
 * @author  Lvov Konstantin
 */

public class OptionsBlock {

    protected final Context context;

    private Composite parent;
	
	private Composite tabComposites[];
    private ScrolledComposite scrolledComposite[];
    
    private TabItem tabItems[];
    private TabFolder tabFolder;

    private HashMap<Parameter, Component> components = new HashMap<Parameter, Component>();
    private List<Component> activeComponents = new ArrayList<Component>();
    
    private ParameterValueChangeListener changeListener;
    
    public OptionsBlock(Composite parent, Context context) {
        this.parent  = parent;
		this.context = context;
		createControl(parent);
        for (Parameter param :  context.getParams())
            param.checkDependentParametersPresents(context.getParams());
	}

	public void performApply() {
        Iterator<Entry<Parameter, Component>> i = components.entrySet().iterator(); 
        while (i.hasNext()) {
            Component component = i.next().getValue();
            component.performApply();
        }
	}

	private void createControl(Composite parent) {
        ParamGroup paramGroups[] = getParamGroups();
        tabComposites = new Composite[paramGroups.length];
        scrolledComposite = new ScrolledComposite[paramGroups.length];
        if (paramGroups.length==0)
        	return;
        if (paramGroups.length > 1)
            createTabFolder(parent, paramGroups);
        else
            createGroupPanel(parent, paramGroups[0]);
            
        // add property controls
        changeListener = new ParameterValueChangeListener();
        addProperties(paramGroups);
        
        for (int i = 0; i < paramGroups.length; i++) {
            scrolledComposite[i].setContent(tabComposites[i]);
            scrolledComposite[i].setMinSize(tabComposites[i].computeSize(SWT.DEFAULT, SWT.DEFAULT));
        }
	} // createControl()

    private void createGroupPanel(Composite parent, ParamGroup paramGroup) {
        ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);

        Group panel = new Group(sc, SWT.NONE);
        
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 3;
        layout.marginWidth = 3;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 5;
        panel.setLayout(layout);

        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        panel.setLayoutData(data);
        
        panel.setText(paramGroup.getLabel());
        
        tabComposites[0] = panel;
        scrolledComposite[0] = sc;
    } // createGroupPanel()
    
    private void createTabFolder(Composite parent, ParamGroup paramGroups[]) {
        tabFolder = new TabFolder(parent, SWT.NONE);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tabItems  = new TabItem[paramGroups.length];
        
        for(int i = 0; i < paramGroups.length; i++) {
            tabItems[i] = new TabItem(tabFolder, SWT.NONE);
            tabItems[i].setText(paramGroups[i].getLabel());
        }

        for (int i = 0; i < paramGroups.length; i++) {
            ScrolledComposite sc = new ScrolledComposite(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
            sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
            sc.setExpandHorizontal(true);
            sc.setExpandVertical(true);

            GridLayout layout = new GridLayout(2, false);
            layout.marginHeight = 3;
            layout.marginWidth = 3;
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 5;

            GridData tabGridData = new GridData(SWT.FILL, SWT.FILL, true, false);

            Composite panel = new Composite(sc, SWT.NONE);
            
            panel.setLayout(layout);
            panel.setLayoutData(tabGridData);

            tabComposites[i] = panel;
            scrolledComposite[i] = sc;
            tabItems[i].setControl(sc);
        }
    } // createTabFolders()

    protected Component createComponent(Parameter param) {
        Component component = null;
        ParamType paramType = (param==null)?null:param.getType();
        
        if (param == null) {
            component = new LabelComponent(param);            
        } else if (paramType instanceof ParamTypeNumber) {
            component = new NumberComponent(param);            
        } else if (paramType instanceof ParamTypeBool) {
            component = new BoolComponent(param);            
        } else if (paramType instanceof ParamTypeString) {
            if (((ParamTypeString)paramType).getKind() == ParamTypeString.KIND.FILE)
                component = paramType.isList() ? new FileListComponent(param) 
                                               : new FileComponent(param);            
            else if (((ParamTypeString)paramType).getKind() == ParamTypeString.KIND.DIR)
                component = paramType.isList() ? new DirListComponent(param) 
                                               : new DirComponent(param);            
            else
                component = paramType.isList() ? new StringListComponent(param) 
                                               : new TextComponent(param);            
        } else if (paramType instanceof ParamTypeEnum) {
            component = new ComboComponent(param);            
        } else {
            System.out.println("Param type " + param.getType().getName() + " unknown (not implemented?)");
        }
        if (param!=null) { // Andrey
        	components.put(param, component);
        }
        return component;
    } // createComponent()
    
    private ParamGroup[] getParamGroups() {
        if (context == null)
            return new ParamGroup[0];
        
        List<ParamGroup> paramGroups = context.getVisibleParamGroups();
        return (ParamGroup[]) paramGroups.toArray(new ParamGroup[paramGroups.size()]);
    }
    
    protected void addProperties(ParamGroup paramGroups[]) {
        activeComponents.clear();
        for (int i = 0; i < paramGroups.length; i++) {
            ParamGroup paramGroup = paramGroups[i];
            
            for (Iterator<String> pi = paramGroup.getParams().iterator(); pi.hasNext();) {
                String paramID = (String)pi.next();
                Component component;

                if (paramID.equals(XMLConfig.PARAMGROUP_SEPARATOR)){
                	component = createComponent(null); // will create horizontal separator?
                    component.createControl(tabComposites[i]);
                	continue;
                } else {
                	//
                	Parameter param = context.findParam(paramID);
                	if (!param.isVisible())
                		continue;
                	component = components.get(param);
                	if (component == null)
                		component = createComponent(param);
                	if (component == null)
                		continue;
                	
                }
                activeComponents.add(component);
                //              component.setPreferenceStore(store);
                component.createControl(tabComposites[i]);
                component.setChangeListener(changeListener);
            }
        }
    } // addProperties()
    
    
    private void disposeControl() {
        for (Component component : activeComponents) {
            component.suspendControl();
        }
        Control[] controls = parent.getChildren();
        for (int i = 0; i < controls.length; i++) {
            if (! controls[i].isDisposed())
                controls[i].dispose();
        }
    } // disposeControl()
    
    private void activateControl(Component selected) {
        if (selected != null) {
            selected.resumeControl();
            selected.setFocus();
        }
        for (Component component : activeComponents) {
            component.resumeControl();
        }
        if (selected != null) {
            selected.setFocus();
        }
    }
    
    //-------------------------------------------------------------------------
    private class ParameterValueChangeListener implements Component.ChangeListener {

        public void valueChanged(Component componenet) {
//            System.out.println("");
//            System.out.println("");
//            System.out.println("OptionsBlock.valueChanged: start");
            if ((parent != null) && ! parent.isDisposed()) {
                parent.setRedraw(false);
                int selectedTab = 0;
                if (tabFolder != null)
                    selectedTab = tabFolder.getSelectionIndex();
                disposeControl();
                createControl(parent);
                parent.layout();
                if (tabFolder != null) {
                    tabFolder.setSelection(selectedTab);
                }
                activateControl(componenet);
                parent.setRedraw(true);
            }
//            System.out.println("OptionsBlock.valueChanged: end");
        }
        
    } // class ParameterValueChangeListener
    
} // class OptionsBlock
