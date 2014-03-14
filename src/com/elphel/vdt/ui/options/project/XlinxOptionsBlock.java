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
package com.elphel.vdt.ui.options.project;


import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.elphel.vdt.Txt;
import com.elphel.vdt.VDT;


/**
 * SWT component of Xilinx project option.
 * 
 * Created: 16.02.2006
 * @author  Lvov Konstantin
 */
public class XlinxOptionsBlock extends OptionsBlock {
        
    private ListOption deviceFamaly;
    private ListOption device;
    private ListOption devicePackage;
    private ListOption deviceSpeedGrade;
    
    private static final String[] DEVICE_FAMALY = new String[] {
                                                  "Automative Spartan2E"  // aspartan2e  
                                                , "Automative Spartan3"   // aspartan3         
                                                , "Spartan2"              // spartan2
                                                , "Spartan2E"             // spartan2e
                                                , "Spartan3"              // spartan3
                                                , "Spartan3E"             // spartan2e
                                                , "Virtex2"               // virtex2           
                                                , "Virtex2P"              // virtex2p 
                                                , "Virtex4"               // virtex4
                                                , "VirtexE"               // virtexe
                                                };
    private static final int DEFAULT_DEVICE_FAMALY = 4;

    private static final String[] DEVICE = new String[] {
                                           "xc3s50"        
                                         , "xc3s200"    
                                         , "xc3s400"    
                                         , "xc3s1000"    
                                         , "xc3s1000I"    
                                         , "xc3s1500"       
                                         , "xc3s1500I"       
                                         };
    private static final int DEFAULT_DEVICE = 3;
    
    private static final String[] DEVICE_PACKAGE = new String[] {
                                                   "fg320"        
                                                 , "fg456"    
                                                 , "fg676"       
                                                 , "ft256"       
                                                 };
    private static final int DEFAULT_DEVICE_PACKAGE = 3;

    private static final String[] DEVICE_SPEED_GRADE = new String[] {
                                                       "5"        
                                                     , "4"    
                                                     };
    private static final int DEFAULT_DEVICE_SPEED_GRADE = 1;
    
    public XlinxOptionsBlock(Composite parent, IResource resource) {
        super(resource, 4);
                
        Group panel = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.numColumns = 3;
        panel.setLayout(layout);

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        panel.setLayoutData(data);
                
        panel.setText(Txt.s("Option.XlinxDevice.GroupCaption"));

        addDeviceFamaly(panel);
        addDevice(panel);
        addDevicePackage(panel);
        addDeviceSpeedGrade(panel);
        
        initializeFields();
    } // XlinxOptionsBlock()

    private void addDeviceFamaly(Composite parent) {
        createLabel(parent, Txt.s("Option.XlinxDevice.DeviceFamaly"));
        deviceFamaly = new ListOption( createCombo(parent)
                                     , VDT.OPTION_XLINX_DEVICE_FAMALY            
                                     , DEVICE_FAMALY
                                     , DEFAULT_DEVICE_FAMALY
                                     );
        addOption(deviceFamaly);
        createStab(parent);
    } // addDeviceFamaly()

    private void addDevice(Composite parent) {
        createLabel(parent, Txt.s("Option.XlinxDevice.Device"));
        device = new ListOption( createCombo(parent)
                               , VDT.OPTION_XLINX_DEVICE            
                               , DEVICE
                               , DEFAULT_DEVICE
                               );
        addOption(device);
        createStab(parent);
    } // addDevice()
    
    private void addDevicePackage(Composite parent) {
        createLabel(parent, Txt.s("Option.XlinxDevice.DevicePackage"));
        devicePackage = new ListOption( createCombo(parent)
                                      , VDT.OPTION_XLINX_DEVICE_PACKAGE            
                                      , DEVICE_PACKAGE
                                      , DEFAULT_DEVICE_PACKAGE
                                      );
        addOption(devicePackage);
        createStab(parent);
    } // addDevicePackage()

    private void addDeviceSpeedGrade(Composite parent) {
        createLabel(parent, Txt.s("Option.XlinxDevice.DeviceSpeedGrade"));
        deviceSpeedGrade = new ListOption( createCombo(parent)
                                         , VDT.OPTION_XLINX_DEVICE_SPEED_GRADE            
                                         , DEVICE_SPEED_GRADE
                                         , DEFAULT_DEVICE_SPEED_GRADE
                                         );
        addOption(deviceSpeedGrade);
        createStab(parent);
    } // addDeviceSpeedGrade()
    
    
} // class XlinxOptionsBlock
