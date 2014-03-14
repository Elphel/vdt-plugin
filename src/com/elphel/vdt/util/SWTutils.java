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
package com.elphel.vdt.util;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * General purpose SWT utilities
 * 
 * Created: 15.05.2006
 * @author  Lvov Konstantin
 */

public class SWTutils {

    /**
     * Correctly resizes the table so no phantom columns appear
     * 
     * @param table the table
     * @since 3.1
     */
    public static void configureTableResizing( final Table table
                                             , final TableWidth tableWidth ) 
    {
        
        ControlAdapter resizer= new ControlAdapter() {
            private boolean fIsResizing= false;
            private boolean isInitialized = false;
            TableWidth info = tableWidth;
            
            public void controlResized(ControlEvent e) {
                if (fIsResizing)
                    return;
                try {
                    fIsResizing= true;
                    int clientAreaWidth= table.getClientArea().width;
                    
                    if (e.widget == table) {
                        info.updateMinimalWidth();
                        
                        int newWidth;
                        if (isInitialized || (clientAreaWidth - info.tableWidth < 50)) {
                            newWidth = info.tableWidth < clientAreaWidth ? clientAreaWidth : Math.max(clientAreaWidth, info.minimalTableWidth);
                        } else {
                            newWidth = info.tableWidth;
                        }
                        isInitialized = true;
                        final int toDistribute = newWidth - info.tableWidth;
                        info.resize(toDistribute);
                    } else {
                        // column being resized
                        // on GTK, the last column gets auto-adapted - ignore this
                        TableColumn[] columns = table.getColumns();
                        if (e.widget == columns[columns.length-1])
                            return;
                        int tableWidth= 0;
                        for (int i= 0; i < columns.length; i++) {
                            info.columnWidth[i] = columns[i].getWidth();
                            tableWidth += info.columnWidth[i];
                        }
                        info.tableWidth = tableWidth;
                    }
                    
                    // set scroll bar visible
                    table.getHorizontalBar().setVisible(info.tableWidth > clientAreaWidth);
                } finally {
                    fIsResizing= false;
                }
            }
        };
        TableColumn[] columns= table.getColumns();
        table.addControlListener(resizer);
        for (int i= 0; i < columns.length; i++) {
            columns[i].addControlListener(resizer);
        }
    }
    
    //-------------------------------------------------------------------------
    abstract public static class TableWidth {
        protected final Table table;
        
        protected int[] columnWidth;
        protected int[] initialWidth;
        protected int[] minimalWidth;

        protected int tableWidth;
        protected int initialTableWidth;
        protected int minimalTableWidth;
        
        public TableWidth(final Table table) {
            this.table = table;
            TableColumn[] columns= table.getColumns();
            columnWidth = new int[columns.length];
            for (int i= 0; i < columns.length; i++) {
                columnWidth[i] = columns[i].getWidth();
                tableWidth += columnWidth[i];
            }
            initialWidth = columnWidth.clone();
            initialTableWidth = tableWidth;
            minimalWidth = new int[columns.length];
            minimalTableWidth = 0;
        }
        
        protected void updateMinimalWidth() {
            minimalTableWidth = 0;
            TableColumn[] columns = table.getColumns();
            for (int i= 0; i < columns.length; i++) {
                // don't make a column narrower than the minimum, 
                // or than what it is currently if less than the minimum
                minimalWidth[i]= Math.min(columnWidth[i], initialWidth[i]);
                minimalTableWidth += minimalWidth[i];
            }
        }
        
        abstract public void resize(int toDistribute);
        
    } // class TableWidth

    
    //-------------------------------------------------------------------------
    public static class OneColumnResize extends TableWidth {
        private int column;
        
        public OneColumnResize(final Table table, int column) {
            super(table);
            this.column = column;
        }

        public void resize(int toDistribute) {
            int width = Math.max(minimalWidth[column], columnWidth[column] + toDistribute);
            TableColumn[] columns = table.getColumns();
            columns[column].setWidth(width);
            tableWidth = tableWidth - columnWidth[column] + width;
            columnWidth[column] = width;
        }

    } // class OneColumnResize

    
    //-------------------------------------------------------------------------
    public static class AllColumnResize extends TableWidth {

        public AllColumnResize(final Table table) {
            super(table);
        }
        
        public void resize(int toDistribute) {
            TableColumn[] columns = table.getColumns();
            int lastPart = toDistribute;
            int newTableWidth = 0;
            if (toDistribute != 0) {
                for (int i = 0; i < columns.length; i++) {
                    int width;
                    if (tableWidth > 0) {
                        int part;
                        if (i == columns.length - 1)
                            part= lastPart;
                        else
                            // current width is the weight for the distribution of the extra space
                            part = toDistribute * columnWidth[i] / tableWidth;
                        lastPart -= part;
                        width= Math.max(minimalWidth[i], columnWidth[i] + part);
                    } else {
                        width = toDistribute * initialWidth[i] / initialTableWidth;
                    }
                    columns[i].setWidth(width);
                    columnWidth[i] = width;
                    newTableWidth += width;
                }
                tableWidth = newTableWidth;
            }
        }
    } // class AllColumnResize
    
} // class SWTutils
