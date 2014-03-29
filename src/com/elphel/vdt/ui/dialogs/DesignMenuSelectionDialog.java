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
package com.elphel.vdt.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.core.tools.menu.DesignMenu;

public class DesignMenuSelectionDialog extends Dialog {

    private static final String NONE_ITEM      = "None";
    private static final String NONE_ITEM_DESC = "No design menu for the project";
    
    private Combo comboDesignMenu;
    private Label labelDesignMenuDesc;

    private List<DesignMenu> desigmMenus;
    
    private DesignMenu selectedDesignMenu;
    private String currentDesignMenuName;
    
    public DesignMenuSelectionDialog(Shell parent) {
        this(parent, null);
    }

   public DesignMenuSelectionDialog( Shell parent
                                   , String currentDesignMenuName ) {
        super(parent);
        desigmMenus = ToolsCore.getDesignMenuManager().getDesignMenuList();
        this.currentDesignMenuName = currentDesignMenuName;
    }

    public DesignMenu getSelectedDesignMenu() {
        return selectedDesignMenu;
    }
    
    protected void okPressed() {
        selectedDesignMenu = getDesignMenu();
        super.okPressed();
    }

    protected Control createDialogArea(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 15;
        layout.marginWidth  = 15;
        layout.numColumns = 2;
        layout.verticalSpacing = 9;

        panel.setLayout(layout);
        addProjectDesignMenu(panel);

        applyDialogFont(panel);
        getShell().setText("Select Design Menu");
 
        return panel;
    }
    
    private void addProjectDesignMenu(Composite parent) {
        createLabel(parent, Txt.s("WizardPage.NewProjectMain.Menu.prompt"));
        comboDesignMenu = createCombo(parent);
        ArrayList<String> items = new ArrayList<String>(desigmMenus.size()+1);
        items.add(NONE_ITEM);
        for (DesignMenu menu : desigmMenus) {
            items.add(menu.getName());
        }
        comboDesignMenu.setItems((String[])items.toArray(new String[items.size()]));
        
        createLabel(parent, "");
        labelDesignMenuDesc = createLabel(parent, getMaximalDescription(NONE_ITEM_DESC));

        setSelection(getIndexByName(currentDesignMenuName));

        comboDesignMenu.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                setDescription(comboDesignMenu.getSelectionIndex());
            }
        });
    }

    private int getIndexByName(final String name) {
        if (name == null)
            return 0;
            
        String items[] = comboDesignMenu.getItems();
        for (int i=0; i < items.length; i++) {
            if (name.equals(items[i]))
                return i;
        }
        return 0;
    }

    private void setSelection(int index) {
        comboDesignMenu.select(index);
        setDescription(index);
    }
    
    private void setDescription(int index) {
        String description;
        switch (index) {
        case 0:
            selectedDesignMenu = null;
            description = NONE_ITEM_DESC;
            break;
        default:
            selectedDesignMenu = desigmMenus.get(index-1);
            description = selectedDesignMenu.getDescription();
            if (description == null)
                description = "";
        }
        labelDesignMenuDesc.setText(description);
    }
    
    private DesignMenu getDesignMenu() { 
        int index = comboDesignMenu.getSelectionIndex();
        switch (index) {
        case 0:
            return null;            
        default:
            return (DesignMenu)desigmMenus.get(index-1); 
        }
    }

    private String getMaximalDescription(String text) {
        String res = text;
        int max_length = res.length();  
        for (DesignMenu menu : desigmMenus) {
            String descrioption = menu.getDescription();
            if ((descrioption != null) && (descrioption.length() > max_length)) {
                res = descrioption;
                max_length = res.length();
            }
        }
        return res;
    }
    
    //-------------------------------------------------------------------------
    protected static Label createLabel(Composite parent, String text ) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.minimumWidth = label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        data.widthHint = label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        label.setLayoutData(data);
        return label;
    } // createLabel()
    
    protected static Combo createCombo(Composite parent) {
        Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        return combo;
    } // createCombo()  
    
} // class DesignMenuSelectionDialog
