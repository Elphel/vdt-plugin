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
package com.elphel.vdt.ui.wizards.module;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.elphel.vdt.util.SWTutils;

/**
 * Viewer of list of Verilog module ports. 
 * 
 * Created: 09.01.2006
 * @author  Lvov Konstantin
 */

public class ModulePortListViewer {

    private Table portTable;
    private TableViewer portTableViewer;
    private ModulePortList portList;

    // Set the table column property names
    private final String NAME_COLUMN        = "port name";
    private final String DIRECTION_COLUMN   = "direction";
    private final String MSB_COLUMN         = "msb";
    private final String LSB_COLUMN         = "lsb";

    // Set column names of ports table
    private String[] portsTableColumnNames = new String[] { 
       NAME_COLUMN 
    ,  DIRECTION_COLUMN
    ,  MSB_COLUMN
    ,  LSB_COLUMN
    };
    
    private List getColumnNames() {
        return Arrays.asList(portsTableColumnNames);
    }

    // Direction combo box choices
    private static final String[] DIRECTION_LABELS = { "input", "output", "inout" };
    
    private static final ModulePort.Direction[] DIRECTION_VALUES = {
        ModulePort.Direction.input
    ,   ModulePort.Direction.output
    ,   ModulePort.Direction.inout
    };

    public ModulePortList getPortList() { return portList; }

    public IStructuredSelection getSelection() {
        return ((IStructuredSelection)portTableViewer.getSelection());
    }
    
    public ModulePortListViewer(Composite parent) {
        // Create the Table 
        portTable = createPortsTable(parent);

        // Create and setup the TableViewer of ports
        portTableViewer = createPortsTableViewer(portTable); 
        portTableViewer.setContentProvider(new PortListContentProvider());
        portTableViewer.setLabelProvider(new PortListLabelProvider());

        // The input for the table viewer is the instance of ExampleTaskList
        portList = new ModulePortList();
        portTableViewer.setInput(portList);
    }

    private TableViewer createPortsTableViewer(Table table) {
        TableViewer viewer = new TableViewer(table);
        viewer.setUseHashlookup(true);

        viewer.setColumnProperties(portsTableColumnNames);
        CellEditor[] editors = new CellEditor[portsTableColumnNames.length];

        // Column 1 : Name (Free text)
        TextCellEditor textEditor = new TextCellEditor(table);
        editors[0] = textEditor;
        
        // Column 2 : Direction (Combo Box) 
        editors[1] = new ComboBoxCellEditor(table, DIRECTION_LABELS, SWT.READ_ONLY);
        
        // Column 3 : MSB (Text with digits only)
        textEditor = new TextCellEditor(table);
        ((Text) textEditor.getControl()).addVerifyListener(
            new VerifyListener() {
                public void verifyText(VerifyEvent e) {
                    // Here, we could use a RegExp such as the following 
                    // if using JRE1.4 such as  e.doit = e.text.matches("[\\-0-9]*");
                    e.doit = "0123456789".indexOf(e.text) >= 0;
                }
            });
        editors[2] = textEditor;
        
        // Column 4 : LSB (Text with digits only)
        textEditor = new TextCellEditor(table);
        ((Text) textEditor.getControl()).addVerifyListener(
            new VerifyListener() {
                public void verifyText(VerifyEvent e) {
                    // Here, we could use a RegExp such as the following 
                    // if using JRE1.4 such as  e.doit = e.text.matches("[\\-0-9]*");
                    e.doit = "0123456789".indexOf(e.text) >= 0;
                }
            });
        editors[3] = textEditor;
        
        // Assign the cell editors to the viewer 
        viewer.setCellEditors(editors);
        // Set the cell modifier for the viewer
        viewer.setCellModifier(new PortCellModifier());

        return viewer;
    } // createPortsTableViewer()
    
    private Table createPortsTable(Composite parent) {
        int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | 
        SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

        Table table = new Table(parent, style);
        GridData data= new GridData(GridData.FILL_BOTH);
        data.widthHint= 470;
        table.setLayoutData(data);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        // 1st column with port name
        TableColumn column = new TableColumn(table, SWT.LEFT, 0);               
        column.setText("Port name");
        column.setWidth(300);
        
        // 2nd column with port direction
        column = new TableColumn(table, SWT.CENTER, 1);
        column.setText("Direction");
        column.setWidth(70);
        
        // 3rd column with MSB
        column = new TableColumn(table, SWT.CENTER, 2);
        column.setText("MSB");
        column.setWidth(50);
        
        // 4th column with LSB
        column = new TableColumn(table, SWT.CENTER, 3);
        column.setText("LSB");
        column.setWidth(50);

        SWTutils.configureTableResizing(table, new SWTutils.OneColumnResize(table, 0));
        
        return table;   
    } // createPortsTable()
    
    public void setLayoutData (Object layoutData) {
        portTable.setLayoutData(layoutData);
    }
    
    /**
     * Proxy for the ModulePortList providing content for the portsTable. 
     * It implements the ModulePortList.Listener interface since it must 
     * register in the ModulePortList 
     */
    private class PortListContentProvider implements IStructuredContentProvider
                                          , ModulePortList.Listener {
        // --------------- IStructuredContentProvider -------------------------
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
            if (newInput != null)
                ((ModulePortList) newInput).addListener(this);
            if (oldInput != null)
                ((ModulePortList) oldInput).removeListener(this);
        }

        public void dispose() {
            portList.removeListener(this);
        }
        
        // Return the ports as an array of Objects
        public Object[] getElements(Object parent) {
            return portList.getPorts().toArray();
        }

        // --------------- ModulePortList.Listener ----------------------------
        public void portAdded(ModulePort port) {
            portTableViewer.add(port);
        }
        
        public void portRemoved(ModulePort port) {
            portTableViewer.remove(port);   
        }
        
        public void portChanged(ModulePort port) {
            portTableViewer.update(port, null);     
        }

        public void portInserted(ModulePort port, int position){
            portTableViewer.insert(port, position); 
        }

    } // class PortListContentProvider

    
    private class PortListLabelProvider extends LabelProvider
                                        implements ITableLabelProvider {
            
        public String getColumnText(Object element, int columnIndex) {
            String result = "";
            ModulePort port = (ModulePort) element;
            switch (columnIndex) {
            case 0:  
                    result = port.getName();
                    break;
            case 1 :
                    ModulePort.Direction direction = port.getDirection();
                    if (direction == ModulePort.Direction.input)
                            result = "input"; 
                    else if (direction == ModulePort.Direction.output)
                            result = "output";
                    else
                            result = "inout";
                    break;
            case 2 :
                    result = ""+port.getMSB();
                    break;
            case 3 :
                    result = ""+port.getLSB();;
                    break;
            default :
                    break;  
            }
            return result;
        } // getColumnText()
        
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    } // class PortListLabelProvider

    /**
     * This class implements an ICellModifier
     * An ICellModifier is called when the user modifes a cell in the 
     * tableViewer
     */
    private class PortCellModifier implements ICellModifier {

        public boolean canModify(Object element, String property) {
                return true;
        }
        
        public Object getValue(Object element, String property) {
            // Find the index of the column
            int columnIndex = getColumnNames().indexOf(property);

            Object result = null;
            ModulePort port = (ModulePort) element;

            switch (columnIndex) {
            case 0 :  // NAME_COLUMN
                result = port.getName();
                break;
            case 1 : // DIRECTION_COLUMN 
                ModulePort.Direction direction = port.getDirection();
                int i = DIRECTION_VALUES.length - 1;
                while ((direction != DIRECTION_VALUES[i]) && (i > 0))
                    --i;
                result = new Integer(i);                                    
                break;
            case 2 : // MSB_COLUMN 
                result = "" + port.getMSB();                                    
                break;
            case 3 : // LSB_COLUMN 
                result = "" + port.getLSB();                                    
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
            ModulePort port = (ModulePort) item.getData();
            String valueString;

            switch (columnIndex) {
            case 0 :  // NAME_COLUMN
                valueString = ((String) value).trim();
                port.setName(valueString);
                break;
            case 1 : // DIRECTION_COLUMN
                ModulePort.Direction direction =  DIRECTION_VALUES[((Integer) value).intValue()];                   
                port.setDirection(direction);
                break;
            case 2 : // MSB_COLUMN 
                valueString = ((String) value).trim();
                if (valueString.length() == 0)
                    valueString = "0";
                port.setMSB(Integer.parseInt(valueString));
                break;
            case 3 : // LSB_COLUMN 
                valueString = ((String) value).trim();
                if (valueString.length() == 0)
                    valueString = "0";
                port.setLSB(Integer.parseInt(valueString));
                break;
            default :
            }
            portList.updatePort(port);
        } // modify()
    } // class PortCellModifier

    
} // class ModulePortListViewer
