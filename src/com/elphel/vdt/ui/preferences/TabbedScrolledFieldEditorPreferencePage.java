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
package com.elphel.vdt.ui.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;


/**
 * Tabbed field editor page with scrolling services for tab item component.
 *
 * Created: 10.04.2006
 * @author  Lvov Konstantin
 */

public abstract class TabbedScrolledFieldEditorPreferencePage 
                extends FieldEditorPreferencePage
{

    private TabFolder tabFolder;
    private ScrolledPageContent scrolledPanel;
    
    /***
     * Maximum number of columns for field editors.
     */
    private int maxNumOfColumns;

    /***
     * Creates a new field editor preference page with the given style, an empty title, and no image.
     * @param style either <code>GRID</code> or <code>FLAT</code>
     */
    protected TabbedScrolledFieldEditorPreferencePage(int style) {
        super(style);
    }

    /***
     * Creates a new field editor preference page with the given title and style, but no image.
     * @param title the title of this preference page
     * @param style either <code>GRID</code> or <code>FLAT</code>
     */
    protected TabbedScrolledFieldEditorPreferencePage(String title, int style) {
        super(title, style);
    }

    /***
     * Creates a new field editor preference page with the given title, image, and style.
     * @param title the title of this preference page
     * @param image the image for this preference page, or <code>null</code> if none
     * @param style either <code>GRID</code> or <code>FLAT</code>
     */
    protected TabbedScrolledFieldEditorPreferencePage(String title, ImageDescriptor image, int style) {
        super(title, image, style);
    }

    /***
     * Adds the given field editor to this page.
     * @param editor the field editor
     */
    protected void addField(FieldEditor editor) {
        // needed for layout, since there is no way to get fields editor from parent
        this.maxNumOfColumns = Math.max(this.maxNumOfColumns, editor.getNumberOfControls());
        super.addField(editor);
    }

    private void adjustGridLayout(ScrolledPageContent panel) {
        Composite content = panel.getBody();
        GridLayout layout = ((GridLayout) content.getLayout());
        layout.numColumns = this.maxNumOfColumns;
        layout.marginHeight = 5;
        layout.marginWidth = 5;
    }
    
    /***
     * Adjust the layout of the field editors so that they are properly aligned.
     */
    protected void adjustGridLayout() {
        if (tabFolder != null) {
            TabItem[] items = tabFolder.getItems();
            for (int j = 0; j < items.length; j++) {
                adjustGridLayout((ScrolledPageContent)items[j].getControl());
            }
        } 
        if (scrolledPanel != null) {
            adjustGridLayout(scrolledPanel);
        }
        
        // need to call super.adjustGridLayout() since fieldEditor.adjustForNumColumns() is protected
        super.adjustGridLayout();

        // reset the main container to a single column
        if (tabFolder != null) {
            ((GridLayout) super.getFieldEditorParent().getLayout()).numColumns = 1;
        }    
    }

    private ScrolledPageContent createScrolledPanel(Composite parent) {
        ScrolledPageContent panel = new ScrolledPageContent(parent);
        panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Composite content = panel.getBody();
        GridLayout layout = new GridLayout();
        content.setLayout(layout);
        content.setFont(super.getFieldEditorParent().getFont());
        content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return panel;
    }
    
    /***
     * Returns a parent composite for a field editor.
     * <p>
     * This value must not be cached since a new parent may be created each time this method called. Thus this method
     * must be called each time a field editor is constructed.
     * </p>
     * @return a parent
     */
    protected Composite getFieldEditorParent() {
        if (tabFolder == null || tabFolder.getItemCount() == 0) {
            if (scrolledPanel == null) {
                scrolledPanel = createScrolledPanel(super.getFieldEditorParent());
            }
            return scrolledPanel.getBody();
        }
        return ((ScrolledPageContent) tabFolder.getItem(tabFolder.getItemCount() - 1).getControl()).getBody();
    }

    protected Composite getUnscrollableFieldEditorParent() {
        if (tabFolder == null || tabFolder.getItemCount() == 0) {
            return super.getFieldEditorParent();
        } else {
            return ((ScrolledPageContent) tabFolder.getItem(tabFolder.getItemCount() - 1).getControl()).getBody();
        }
    }
    
    
    /***
     * Adds a tab to the page.
     * @param text the tab label
     */
    public void addTab(String text) {
        if (tabFolder == null) {
            // initialize tab tabFolder
            if (scrolledPanel == null)
                tabFolder = new TabFolder(super.getFieldEditorParent(), SWT.NONE);
            else
                tabFolder = new TabFolder(scrolledPanel.getBody(), SWT.NONE);
            tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            super.getFieldEditorParent().addControlListener(new ControlListener() {
                public void controlMoved(ControlEvent e) {
                    System.out.println("-- super: control moved");
                }
                public void controlResized(ControlEvent e) {
                    System.out.println("-- super: control resized");
                    
                }
            });
        }

        TabItem item = new TabItem(tabFolder, SWT.NONE);
        item.setText(text);

        Composite currentTab = createScrolledPanel(tabFolder);
        item.setControl(currentTab);
    }

} // class TabbedScrolledFieldEditorPreferencePage
