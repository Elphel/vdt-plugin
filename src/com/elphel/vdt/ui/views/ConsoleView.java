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
package com.elphel.vdt.ui.views;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Create an instance of this class in any of your plugin classes.
 * 
 * Use it as follows ...
 * 
 * ConsoleView.getDefault().println("Some error msg", ConsoleDisplayMgr.MSG_INFORMATION);
 * ...
 * ...
 * ConsoleView.getDefault().clear();
 * ...
 * 
 *   This code was taken from an article available at
 *      http://www.eclipsezone.com/eclipse/forums/t52910.html
 */
public class ConsoleView {
	
    private static ConsoleView fDefault = new ConsoleView("My console view");
    private String fTitle = null;
    private MessageConsole fMessageConsole = null;
    
    public static final int MSG_INFORMATION = 1;
    public static final int MSG_ERROR = 2;
    public static final int MSG_WARNING = 3;
    
    public ConsoleView(String messageTitle) {     
        fDefault = this;
        fTitle = messageTitle;
    }
    
    public static ConsoleView getDefault() {
        return fDefault;
    }  
    
    public void println(String msg, int msgKind)
    {     
        if(msg == null)
            return;
        
        // if console-view in Java-perspective is not active, then show it and
        // then display the message in the console attached to it      
        if(!displayConsoleView()) {
            // If an exception occurs while displaying in the console, then just diplay atleast the same in a message-box
            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error", msg);
            return;
        }
        
        // display message on console
        getNewMessageConsoleStream(msgKind).println(msg);           
    }
    
    public void clear()
    {     
        IDocument document = getMessageConsole().getDocument();
        if (document != null) {
            document.set("");
        }        
    }  
    
    private boolean displayConsoleView()
    {
        try
        {
            IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if(activeWorkbenchWindow != null) {
                IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
                
                if (activePage != null)
                    activePage.showView(IConsoleConstants.ID_CONSOLE_VIEW, null, IWorkbenchPage.VIEW_VISIBLE);
            }
            
        } catch(PartInitException partEx) {         
            return false;
        }
        
        return true;
    }
    
    private MessageConsoleStream getNewMessageConsoleStream(int msgKind)
    {     
        int swtColorId = SWT.COLOR_DARK_GREEN;
        
        switch (msgKind) {
            case MSG_INFORMATION:
                swtColorId = SWT.COLOR_BLACK;
                break;
                
            case MSG_ERROR:
                swtColorId = SWT.COLOR_RED;
                break;
                
            case MSG_WARNING:
                swtColorId = SWT.COLOR_DARK_BLUE;
                break;
                
            default:          
                swtColorId = SWT.COLOR_DARK_MAGENTA;
        }  
        
        MessageConsoleStream msgConsoleStream = getMessageConsole().newMessageStream();     
        msgConsoleStream.setColor(Display.getCurrent().getSystemColor(swtColorId));
        return msgConsoleStream;
    }
    
    private MessageConsole getMessageConsole()
    {
        if( fMessageConsole == null )
            createMessageConsoleStream(fTitle); 
        
        return fMessageConsole;
    }
    
    private void createMessageConsoleStream(String title)
    {
        fMessageConsole = new MessageConsole(title, null); 
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ fMessageConsole });
    }  
}
