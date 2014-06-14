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
package com.elphel.vdt.ui.dialogs;


import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.elphel.vdt.Txt;


/**
 * Dialog to promt list of strings. 
 * 
 * Created: 13.02.2006
 * @author  Lvov Konstantin
 */

public class ListPromptDialog extends Dialog {
    
    private String title;
    private ListPromptViewer listPromptViewer;

    private Button addButton;
    private Button removeButton;
    private Button upButton;
    private Button downButton;
    
    private List<String> listElements = null;
    private IAddAction addAction;
    
    public ListPromptDialog(final Shell parentShell, String title) {
        this(parentShell, title, new IAddAction() {
            public String getNewValue() {
                TextPromptDialog d = new TextPromptDialog(parentShell);
                
                d.create();
                
                if(d.open() != Dialog.OK)
                    return null;
                
                return d.getText();
            }
        });
    }

    public ListPromptDialog(Shell parentShell, String title, IAddAction addAction) {
        super(parentShell);
        this.title = title;
        this.addAction = addAction;
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite panel = (Composite)super.createDialogArea(parent);
        GridLayout layout = (GridLayout)panel.getLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        
        // Create the table of entries 
        GridData gridData = new GridData( GridData.FILL_BOTH 
                                        | GridData.GRAB_HORIZONTAL 
                                        | GridData.GRAB_VERTICAL );
        gridData.verticalSpan = 4;
        listPromptViewer = new ListPromptViewer(panel, listElements);
        listPromptViewer.setLayoutData(gridData);
        
        // Create buttons tomodify list of entries 
        addButton    = createButton(panel, Txt.s("Dialog.ListPrompt.Button.Add"));
        removeButton = createButton(panel, Txt.s("Dialog.ListPrompt.Button.Remove"));
        upButton     = createButton(panel, Txt.s("Dialog.ListPrompt.Button.MoveUp"));
        downButton   = createButton(panel, Txt.s("Dialog.ListPrompt.Button.MoveDown"));
        
        getShell().setText(title);
        return panel;
    } // createDialogArea()
    
    private Button createButton(Composite parent, String caption) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(caption);
        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_FILL);
        button.setLayoutData(gridData);
        button.addSelectionListener(buttonListener);
        return button;
    }
    
    private SelectionListener buttonListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            ListPromptModel.Entry entry = (ListPromptModel.Entry) listPromptViewer.getSelection().getFirstElement();
            Object source= e.getSource();
            if (source == addButton) { 
                // Add a entry to the list and refresh the view
                String value = addAction.getNewValue();
                if (value != null)
                    listPromptViewer.getModel().addEntry(value);
            } else if (source == removeButton) {
                // Remove the selection and refresh the view
                if (entry != null) {
                    listPromptViewer.getModel().removeEntry(entry);
                }                               
            } else if (source == upButton) {
                if (entry != null) {
                    listPromptViewer.getModel().moveUpEntry(entry);
                }                               
            } else if (source == downButton) {
                if (entry != null) {
                    listPromptViewer.getModel().moveDownEntry(entry);
                }                               
            }
        } // widgetSelected()
    };
    
    private List<String> getList() {
        return listPromptViewer.getModel().getList();
    }

    public void setList(List<String> list) {
        listElements = list;
    }
    
    public List<String> open(List<String> list) {
        setList(list);
        if (super.open() == Window.OK)
            return getList();
        else
            return listElements;
    }
    
    
    public interface IAddAction {
        public String getNewValue();
    } // interface IAddAction
} // class ListPromtDialog
