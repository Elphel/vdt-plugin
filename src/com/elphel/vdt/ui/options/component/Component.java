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
package com.elphel.vdt.ui.options.component;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.graphics.Color;

//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.core.tools.params.Parameter;

public abstract class Component {

    private static int TEXTFIELD_WIDTH = 250;

    private Label labelField;
    
    protected Composite parent;
    protected Parameter param;

    protected static Color colorBackground;
    protected static Color colorBackgroundDefault;

    private static Color colorForeground;
    private static Color colorForegroundDefault;

    private ChangeNotifier changeNotifier;
    
    protected boolean isDefault;
    protected boolean loaded;
    protected boolean focused;
    protected boolean activated;

    protected ChangeListener changeListener;
    
    public Component(Parameter param) {
        this(param, null);
    }
    
    public Component(Parameter param, ChangeListener changeListener) {
//        System.out.println("-- Component: id= "+param.getID()+"; label= "+param.getLabel());
        this.param = param;
        setChangeListener(changeListener);
        loaded    = false;
        focused   = false;
        activated = false;
    }

    public void createControl(Composite parent) {
//        System.out.println("-- Component.createControl: id= "+param.getID()+"; label= "+param.getLabel());
        this.parent = parent;
        activated = false;
        if (param!=null) {// Andrey - to add labels
        	labelField = createLabel(parent, param.getLabel());
        	labelField.setMenu(createPopupMenu(labelField.getShell()));
        	if (param.getToolTip()!=null){
        		labelField.setToolTipText(param.getToolTip());
        	}
        }
    }

    protected void endCreateControl() {
//        System.out.println("-- Component.finalizeCreateControl: id= "+param.getID()+"; label= "+param.getLabel());
        setEnabled(isEnable());
        if (! loaded) {
            setDefault(isDefault);
            loaded = true;
        }
    }

    public void resumeControl() {
        if (isDisposed()) 
            return;
        
        if (! activated){
            setDefault(isDefault);
            activated = true;
        }
    }

    public void suspendControl() {
        if (isDisposed()) 
            return;

        if (activated) {
            removeListeners();
            activated = false;
            if (changeNotifier != null)
                changeNotifier.stop();
        }
    }
    
    
    public void setChangeListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
    }
    
    public abstract String performApply();

    public abstract void setPreferenceStore(IPreferenceStore store);

    protected abstract void saveControlState();
    
    public abstract void setFocus();

    protected abstract void addListeners();
    protected abstract void removeListeners();
    
    protected abstract boolean isDisposed();
    
    public Parameter getParam() {
        return param;
    }

    public boolean isEnable() {
    	if (param==null) return true; // label
        return ! param.isReadOnly();
    }

    public void setVisible (boolean visible) {
    	if (labelField!=null) 
    		labelField.setVisible(visible);
    }
    
    public void setEnabled (boolean enabled) {
    	if (labelField!=null) 
    		labelField.setEnabled(enabled);
    }
    
    protected void setDefault(boolean defaulted) {
//        System.out.println("-- Component.setDefault: id= "+param.getID()+"; label= "+param.getLabel());
        isDefault = defaulted;
    	if (labelField!=null) 
    		labelField.setForeground(defaulted ? colorForegroundDefault
    				: colorForeground );     
    }
    
    protected void selectionChanged() {
//        System.out.println("-- Component.selectionChanged: id= "+param.getID()+"; label= "+param.getLabel());
        if ((param == null) || (! param.hasDependentParameters()))
            return;
            
        if (changeNotifier == null)
            changeNotifier = new ChangeNotifier();
        changeNotifier.valueChanged();
    }
    
    protected Menu createPopupMenu(Decorations parent) {
        Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
        MenuItem actionItem = new MenuItem(menu, SWT.PUSH);
        actionItem.setText("Set to default");
        actionItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if (! isDefault) {
                    setDefault(true);
                    notifyChangeListener();
                }
            }
        });
        return menu;
    }

    protected static Label createLabel(Composite parent, String text) {
        GridData labelData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        label.setLayoutData(labelData);
        return label;
    }

    public static Text createTextControl(Composite parent) {
        Text textControl = new Text(parent, SWT.BORDER | SWT.SINGLE);
        GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_FILL | 
                                          GridData.GRAB_HORIZONTAL );
        gridData.widthHint = TEXTFIELD_WIDTH;
        textControl.setLayoutData(gridData);
        return textControl;
    }

    //-------------------------------------------------------------------------
    public interface ChangeListener {
        public void valueChanged(Component componenet);
    }

    private synchronized void notifyChangeListener() {
//        System.out.println("-- Component.notifyChangeListener: id= "+param.getID()+"; label= "+param.getLabel());
        if (changeListener != null) {
            removeListeners();
            saveControlState();
            changeListener.valueChanged(this);
            if (!isDisposed())
                setFocus();            
        }
    }
    
    private class ChangeNotifier {
        private static final int SLEEP = 15;

        private static final int STOP      = 0;
        private static final int CONTIONUE = 1;
        private static final int NOTIFY    = 2;
        
        private boolean active = false;
        private int count = 0;
        
        private Display display = VerilogPlugin.getStandardDisplay();
        private Runnable [] timer = new Runnable [] {
                new Runnable() {
                    public void run () {
                        switch (getStatus()) {
                        case CONTIONUE:
                            display.timerExec(SLEEP, timer [0]);
                            break;
                        case NOTIFY:    
                            notifyChangeListener();
                            stop();
                            break;
                        }
                    }
                }    
        };

        public synchronized void valueChanged() {
            count++;
            if (! active) {
                active = true;
                display.timerExec(SLEEP, timer [0]);
            }
        }

        public synchronized void stop() {
            active = false;
        }
        
        private synchronized int getStatus() {
            int status;
            if (active) {
                if (count > 1) {
                    status = CONTIONUE;
                } else {
                    status = NOTIFY;
                }    
            } else {
                status = STOP;
            }
            count = 0;
            return status;
        }

    } // class ChangeHandler
    
    //-------------------------------------------------------------------------
    static {
        colorBackground = VerilogPlugin.getStandardDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
//        colorBackgroundDefault = new Color(VDTPlugin.getStandardDisplay(), 234, 249, 255);
        colorBackgroundDefault = new Color(VerilogPlugin.getStandardDisplay(), 232, 242, 254);

        colorForeground = VerilogPlugin.getStandardDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
        colorForegroundDefault = VerilogPlugin.getStandardDisplay().getSystemColor(SWT.COLOR_BLUE);
    }
    
} // class Component
