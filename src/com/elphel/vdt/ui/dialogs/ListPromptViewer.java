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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.elphel.vdt.util.SWTutils;


/**
 * Viewer of list of string entries. 
 * 
 * Created: 14.02.2006
 * @author  Lvov Konstantin
 */

public class ListPromptViewer {

    private Table table;
    private TableViewer viewer;
    private ListPromptModel model;

    // Set the table column property names
    private final String VALUE_COLUMN  = "value";

    // Set column names of ports table
    private String[] tableColumnNames = new String[] { 
       VALUE_COLUMN 
    };
    
    private List getColumnNames() {
        return Arrays.asList(tableColumnNames);
    }
    
    
    public ListPromptModel getModel() { return model; }
    
    public IStructuredSelection getSelection() {
        return ((IStructuredSelection)viewer.getSelection());
    }
    
    public ListPromptViewer(Composite parent) {
    	this(parent, null);
    }

    public ListPromptViewer(Composite parent, List<String> initialValues) {
        // Create the Table 
        table = createTable(parent);

        // Create and setup the TableViewer of ports
        viewer = createTableViewer(table); 
        viewer.setContentProvider(new ListContentProvider());
        viewer.setLabelProvider(new ListLabelProvider());

        // The input for the table viewer is the instance of ExampleTaskList
        model = new ListPromptModel(initialValues);
        viewer.setInput(model);
    }
    
    public void setLayoutData (Object layoutData) {
        table.setLayoutData(layoutData);
    }
    
    private Table createTable(Composite parent) {
        int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | 
        SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

        Table table = new Table(parent, style);
        GridData data= new GridData(GridData.FILL_BOTH);
        data.widthHint= 400;
        table.setLayoutData(data);
        table.setLinesVisible(true);
        table.setHeaderVisible(false);

        // 1st column with port name
        TableColumn column = new TableColumn(table, SWT.LEFT, 0);               
//        column.setText("Value");
        column.setWidth(400);
        
        SWTutils.configureTableResizing(table, new SWTutils.AllColumnResize(table));

        return table;   
    } // createTable()
    
    private TableViewer createTableViewer(Table table) {
        TableViewer viewer = new TableViewer(table);
        viewer.setUseHashlookup(true);

        viewer.setColumnProperties(tableColumnNames);
        CellEditor[] editors = new CellEditor[tableColumnNames.length];

        // Column 1 : Name (Free text)
        TextCellEditor textEditor = new TextCellEditor(table);
        editors[0] = textEditor;
        
        // Assign the cell editors to the viewer 
        viewer.setCellEditors(editors);
        // Set the cell modifier for the viewer
        viewer.setCellModifier(new CellModifier());

        return viewer;
    } // createTableViewer()


    /**
     * Proxy for the ModulePortList providing content for the portsTable. 
     * It implements the ModulePortList.Listener interface since it must 
     * register in the ModulePortList 
     */
    private class ListContentProvider implements IStructuredContentProvider
                                               , ListPromptModel.Listener {
        // --------------- IStructuredContentProvider -------------------------
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
            if (newInput != null)
                ((ListPromptModel) newInput).addListener(this);
            if (oldInput != null)
                ((ListPromptModel) oldInput).removeListener(this);
        }

        public void dispose() {
            model.removeListener(this);
        }
        
        // Return the ports as an array of Objects
        public Object[] getElements(Object parent) {
            return model.getEntries().toArray();
        }

        // --------------- ModulePortList.Listener ----------------------------
        public void entryAdded(ListPromptModel.Entry entry) {
            viewer.add(entry);
        }
        
        public void entryRemoved(ListPromptModel.Entry entry) {
            viewer.remove(entry);   
        }
        
        public void entryChanged(ListPromptModel.Entry entry) {
            viewer.update(entry, null);     
        }

        public void entryInserted(ListPromptModel.Entry entry, int position){
            viewer.insert(entry, position); 
        }

    } // class ListContentProvider
    
    private class ListLabelProvider extends LabelProvider
    implements ITableLabelProvider {

		public String getColumnText(Object element, int columnIndex) {
			String result = "";
			ListPromptModel.Entry entry = (ListPromptModel.Entry) element;
			switch (columnIndex) {
			case 0:  
				result = entry.toString();
				break;
			default :
				break;  
			}
			return result;
		} // getColumnText()

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
    } // class ListLabelProvider
    
    /**
     * This class implements an ICellModifier
     * An ICellModifier is called when the user modifes a cell in the 
     * tableViewer
     */
    private class CellModifier implements ICellModifier {

        public boolean canModify(Object element, String property) {
                return true;
        }
        
        public Object getValue(Object element, String property) {
            // Find the index of the column
            int columnIndex = getColumnNames().indexOf(property);

            Object result = null;
            ListPromptModel.Entry entry = (ListPromptModel.Entry) element;

            switch (columnIndex) {
            case 0 :  // VALUE_COLUMN
                result = entry.getValue();
                break;
            default :
                result = "";
            }
            return result;  
        } // getValue()

        public void modify(Object element, String property, Object value) {     
            // Find the index of the column 
            int columnIndex = getColumnNames().indexOf(property);
                    
            TableItem item = (TableItem) element;
            ListPromptModel.Entry entry = (ListPromptModel.Entry) item.getData();
            String valueString;

            switch (columnIndex) {
            case 0 :  // VALUE_COLUMN
                valueString = ((String) value).trim();
                entry.setValue(valueString);
                break;
            default :
            }
            model.updateEntry(entry);
        } // modify()
        
    } // class CellModifier
    
} // class ListPromptViewer
