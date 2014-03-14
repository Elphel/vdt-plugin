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
 *******************************************************************************/
package com.elphel.vdt.ui.options.component;

import java.lang.NullPointerException;
import java.util.List;
import java.util.ArrayList;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.elphel.vdt.ui.dialogs.ListPromptDialog;


public class ListPromptField {

    private Button browseButton;
    private Text visibleText;

    private List<String> list = new ArrayList<String>();

    private SelectionListener selectionListener; 
//    private ModifyListener modifyListener;
    private PromptModifyListener modifyListener;
    private MouseListener mouseListener;
    
    public  IPromptAction promptAction;
    
    // this dialog field is intended to store the created dialog object,
    // to prevent it from losing the last directory position 
    // it should be created once by the 'browse' selection listener 
    private ListPromptDialog promptDialog;

    ListPromptField(IPromptAction promptAction){
        this.promptAction = promptAction;
        selectionListener = new PromptSelectionListener();
        modifyListener = new PromptModifyListener();
        mouseListener = new PromptMouseListener(); 
    }
    
    public void createControl(Composite parent) {
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;

        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_FILL 
                                        | GridData.GRAB_HORIZONTAL );
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(layout);
        composite.setLayoutData(gridData);
                                      
        visibleText = TextComponent.createTextControl(composite);
        
//        setVisibleText();

        GridData browseGridData = new GridData();
        browseGridData.heightHint = visibleText.getLineHeight()+6;
        browseGridData.horizontalAlignment = GridData.END;
        
        browseButton = new Button(composite, SWT.NONE);
        browseButton.setLayoutData(browseGridData);
        browseButton.setText("...");
    } // BrowseableField()
    
    public List<String> getList() {
        return list;
    }
    
    protected boolean isDisposed() {
        return (visibleText == null) 
            ||  visibleText.isDisposed();
    }
    
    public Text getVisibleNameField() {
        return visibleText;
    }
    
    public ListPromptDialog getPromptDialog() {
        return promptDialog;
    }
    
    public void setPromptDialog(ListPromptDialog browseDialog) {
        this.promptDialog = browseDialog;
    }
    
   
    public void addListeners() {
//        System.out.println("-- ListPromptField.addListeners: " + this);
//        System.out.println("                 modifyListener: " + modifyListener);
        browseButton.addSelectionListener(selectionListener);
        visibleText.addModifyListener(modifyListener);
        visibleText.addMouseListener(mouseListener);
    }

    public void removeListeners() {
//        System.out.println("-- ListPromptField.removeListeners: " + this);
//        System.out.println("                    modifyListener: " + modifyListener);
        browseButton.removeSelectionListener(selectionListener);
        visibleText.removeModifyListener(modifyListener);
        visibleText.removeMouseListener(mouseListener);
    }
    

    public void setBackground (Color color) {
        visibleText.setBackground(color);
    }
    
    private void setVisibleText() {
        String text = "";
        
        if (list != null && !list.isEmpty())
            text = list.get(0);
        
        visibleText.setText(text);        
    }
    
    protected void setList(List<String> list) {
        if (list == null)
            throw new NullPointerException("Setting null list");
        
        this.list = list;
        setVisibleText();
    }
    
    private void promptAndSetList(IPromptAction promptAction) {
        removeListeners();
        setList(promptAction.prompt(list));
        addListeners();
        if (promptAction.getReturnCode() == Window.OK)
            promptAction.slectionChanged();
    }
    
    public interface IPromptAction {
        public List<String> prompt(List<String> current);
        public void slectionChanged();
        public int getReturnCode();
    }       

    public void setEnabled (boolean enabled) {
        browseButton.setEnabled(enabled);
        visibleText.setEnabled(enabled);
    }
    
    public void setVisible (boolean visible) {
        browseButton.setVisible(visible);
        visibleText.setVisible(visible);
    }
    
    public void setFocus() {
        visibleText.setFocus();
    }
    
    public Shell getShell () {
    	return visibleText.getShell();
    }
    
    public void setMenu (Menu menu) {
    	visibleText.setMenu(menu);	
        browseButton.setMenu(menu);
    }
    
    //-------------------------------------------------------------------------
    private class PromptSelectionListener extends SelectionAdapter {
        public void widgetSelected(SelectionEvent event) {
//            System.out.println("-- PromptSelectionListener: " + this);
            promptAndSetList(promptAction);
        }
    };

    private class PromptModifyListener implements ModifyListener {
        public void modifyText(ModifyEvent e) {
//            System.out.println("-- PromptModifyListener: " + this);
            promptAndSetList(promptAction);
        }
    };
    
    private class PromptMouseListener extends MouseAdapter {
        public void mouseDoubleClick(MouseEvent e) {
//            System.out.println("-- PromptMouseListener: " + this);
            promptAndSetList(promptAction);
        }
    }; 
    
} // class ListPromptField
