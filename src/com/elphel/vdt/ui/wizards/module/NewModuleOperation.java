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
package com.elphel.vdt.ui.wizards.module;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * New Verilog module creation.
 * 
 * Created: 16.12.2005
 * @author  Lvov Konstantin
 */

public class NewModuleOperation implements IRunnableWithProgress {

    private ModuleData data; 
    private Wizard  wizard;
    
    NewModuleOperation(ModuleData data, Wizard wizard) {
        this.data   = data;
        this.wizard = wizard;
    }

    public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try {
            createModule(monitor);
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        } finally {
            monitor.done();
        }
    } // run()
        
    /**
     * The worker method. It will find the container, create the
     * file if missing or just replace its contents, and open
     * the editor on the newly created file.
     */
    private void createModule( IProgressMonitor monitor
                             ) throws CoreException     {
        // create a sample file
        monitor.beginTask("Creating " + data.getFileName(), 2);
        final IFile file = data.getFile();
        try {
            InputStream stream = openContentStream(file.exists());
            if (file.exists()) {
//                file.setContents(stream, true, true, monitor);
                file.appendContents(stream, true, true, monitor);
            } else {
                file.create(stream, true, monitor);
            }
            stream.close();
        } catch (IOException e) {
        }
        monitor.worked(1);
        monitor.setTaskName("Opening file for editing...");
        wizard.getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                IWorkbenchPage page =
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                try {
                    IDE.openEditor(page, file, true);
                } catch (PartInitException e) {
                }
            }
        });
        monitor.worked(1);
    } // createModule()
        
        
    /** We will initialize file contents with a sample text. */
    private InputStream openContentStream(boolean fileExists) {
        StringWriter swriter = new StringWriter();
        PrintWriter writer = new PrintWriter(swriter);

        if (fileExists)
            generateModuleHeader(writer);
        else
            generateFileHeader(writer);
        generateModuleInterface(writer);                
        generateModuleBody(writer);             

        writer.flush();
        try {
            swriter.close();
        } catch (IOException e) {}

        return new ByteArrayInputStream(swriter.toString().getBytes());
    } // openContentStream()

    private void generateFileHeader(PrintWriter writer) {
        writer.println("//-----------------------------------------------------------------------------");
        writer.println("//");
        writer.println("//");
        writer.println("// Created: " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(System.currentTimeMillis())));
        writer.println("// Autor:   ");
        writer.println("//-----------------------------------------------------------------------------");
    } 

    private void generateModuleHeader(PrintWriter writer) {
        writer.println();
        writer.println();
        writer.println("//-----------------------------------------------------------------------------");
    }
    
    private void generateModuleInterface(PrintWriter writer){
        // generate module declaration
        String str = "module " + data.getModuleName() + "(";
        ModulePort[] ports = data.getPorts();
        if ((ports != null) && (ports.length > 0)) {
            str += ports[0].getName();
            for (int i=1; i < ports.length; i++)
                str += ", " + ports[i].getName(); 
        }           
        str += ");";
            writer.println(str);

        // generate port declaration
        for (int i=0; i < ports.length; i++)
            generatePortDeclaration(writer, ports[i]);
    } // generateModuleInterface()
    
    private void generatePortDeclaration(PrintWriter writer, ModulePort port) {
        String str;
        ModulePort.Direction direction = port.getDirection();
        if (direction == ModulePort.Direction.input)
            str = "input "; 
        else if (direction == ModulePort.Direction.output)
            str = "output ";
        else
            str = "inout ";

        int msb = port.getMSB();
        int lsb = port.getLSB();
        if (msb != lsb)
            str += "[" + msb + ":" + lsb +"] ";
        
        str += port.getName() + ";";
        writer.println(str);
    } // generatePortDeclaration
    
    private void generateModuleBody(PrintWriter writer){
        writer.println();
        writer.println("endmodule // " + data.getModuleName());
    } // generateModuleBody()
        
} // class NewModuleOperation
