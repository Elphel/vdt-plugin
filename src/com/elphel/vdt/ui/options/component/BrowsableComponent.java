package com.elphel.vdt.ui.options.component;

import com.elphel.vdt.core.tools.params.Parameter;

public abstract class BrowsableComponent extends GeneralComponent {

    protected BrowseableField locationField;
    
    public BrowsableComponent(Parameter param) {
        super(param);
        locationField = new BrowseableField();                                                                            
    }
    
    public BrowseableField getLocationField() {
        return locationField;
    }
       
    public void setSelection(String value) {
    	locationField.setSelection(value);
    }

    protected String getSelection() {
        String location = locationField.getBrowsedNameField().getText().trim();
        return location;
    }
    
    protected boolean isDisposed() {
        return (locationField == null)
            ||  locationField.isDisposed();
    }
    
    public void setEnabled (boolean enabled) {
        super.setEnabled(enabled);
        locationField.setEnabled(enabled);
    }
    
    public void setVisible (boolean visible) {
        super.setVisible(visible);
        locationField.setVisible(visible);
    }
    
    protected void saveControlState() { 
        locationField.saveControlState();
    }
    
    public void setFocus() {
        locationField.setFocus();
    }

    protected void addListeners() {
        locationField.addModifyListener(modifyListener);
    }

    protected void removeListeners() {
        locationField.removeModifyListener(modifyListener);
    }
    
    protected void switchState(boolean defaulted) {
        locationField.setBackground(defaulted ? colorBackgroundDefault
                                              : colorBackground );     
    }
    
} // class BrowsableComponent
