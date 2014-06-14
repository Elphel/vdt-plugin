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
package com.elphel.vdt.ui;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.Txt;
import com.elphel.vdt.ui.dialogs.VDTErrorDialog;

/**
 * Output of user interface message/error  
 *
 * Created: 14.12.2005
 * @author  Lvov Konstantin
 */

public class MessageUI {

    static final String ERROR_CAPTION       = Txt.s("Message.Error.Caption");
    static final String FATAL_ERROR_CAPTION = Txt.s("Message.FatalError.Caption");
        
    static public void error(final String message, final Throwable exception) {
        showErrorMessage(message, exception, ERROR_CAPTION);
    }
        
    static public void error(final String message) {
        showErrorMessage(message, ERROR_CAPTION);
    }
        
    static public void error(final Throwable exception) {
        showErrorMessage(exception, ERROR_CAPTION);
    }
    
    static public void fatalError(String message) {
        showErrorMessage(message, FATAL_ERROR_CAPTION);
    }
        
    static public void showErrorMessage(final String message, 
                                        final Throwable exception, 
                                        final String caption) 
    {
        log(caption+":"+message);
        log(exception);

        openErrorMessageDialog(caption, message, exception); 
    } // showErrorMessage()
    
    static public void showErrorMessage(final String message, final String caption) {
        log(caption+":"+message);

        openErrorMessageDialog(caption, message, null); 
    } // showErrorMessage()
   
    static public void showErrorMessage(final Throwable exception, final String caption) {
        log(exception);
        
        openErrorMessageDialog(caption, null, exception); 
    } // showErrorMessage()
       
    
    private static void openErrorMessageDialog( final String caption
                                              , final String message
                                              , final Throwable exception)
    {
        String actualMessage = message;
        
        if(actualMessage == null) {
            if(exception.getMessage() != null)
                actualMessage = exception.getClass().getName() + ": " + exception.getMessage();
            else
                actualMessage = "Unhandled exception: " + exception.getClass().getName();
        }
        
        final IStatus status = 
            new Status(IStatus.ERROR, 
            		VerilogPlugin.getVdtId(), 
                       IStatus.ERROR, 
                       actualMessage, 
                       exception);

        VerilogPlugin.getStandardDisplay().syncExec(new Runnable() {
            public void run() {
                VDTErrorDialog.openError(VerilogPlugin.getActiveWorkbenchShell(), 
                                         caption, 
                                         status);
            }
        });
    }
    
    //-------------------------------------------------------------------------
    //                        Message Logging
    //-------------------------------------------------------------------------
    private static boolean loggingOn = true;
        
        
    public static void log(IStatus status) {
        if (loggingOn) {
                System.out.println(status.getMessage());
                
                if(status.getException() != null)
                    status.getException().printStackTrace();
                
                VerilogPlugin.getDefault().getLog().log(status);
        }
    }

    public static void log(String message) {
        log(((IStatus) (new Status( IStatus.ERROR
                                  , VerilogPlugin.getVdtId()
                                  , IStatus.ERROR
                                  , message, null
                                  ) )) );
    }

    public static void log(Throwable e) {
        log(((IStatus) (new Status( IStatus.ERROR
                                  , VerilogPlugin.getVdtId()
                                  , IStatus.ERROR
                                  , "Internal Error"
                                  , e
                                  ) )) );
    }
        
} // class MessageUI
