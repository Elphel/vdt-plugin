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
package com.elphel.vdt.ui.variables;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

import com.elphel.vdt.VDT;
import com.elphel.vdt.VerilogUtils;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.core.tools.params.ToolSequence;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Maintains the context used to launch tools. The context is based on
 * the selected resource.
 * 
 * Created: 26.01.2006
 * @author  Lvov Konstantin
 */

public class SelectedResourceManager implements IWindowListener, ISelectionListener {

    // singleton
    private static SelectedResourceManager fgDefault = new SelectedResourceManager();
    private IResource fSelectedResource    = null;
    private IResource fSelectedVerilogFile = null;
    private ITextSelection fSelectedText = null;
    private Stack<IWorkbenchWindow> fWindowStack = new Stack<IWorkbenchWindow>();

    //Andrey
    private String fChosenTarget=null; // full path of the chosen (for action) resource or any string. Used to calculate CurrentFile, verilog file, ...
    private String fChosenShort=null; // last segment of the chosen resource name
    private IResource fChosenVerilogFile = null; // to keep fSelectedVerilogFile
    private int fChosenAction=0; // Chosen variant of running the tool
//    private long timestamp=0;
    private String timestamp;
    private String ignoreFilter=null;
    private boolean toolsLinked=true;
    private ToolSequence toolSequence=null; // to be able to reach toolSequence instance from VEditor
    
//    private Tool selectedTool=null; // last selected tool
 //   
    private SelectedResourceManager() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench != null) { //may be running headless
            workbench.addWindowListener(this);
            IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
            if (activeWindow != null) {
                windowActivated(activeWindow);
            }
        } 
    } // SelectedResourceManager()
    
    /**
     * Returns the singleton resource selection manager
     * 
     * @return VariableContextManager
     */
    public static SelectedResourceManager getDefault() {
    	if (fgDefault.fSelectedResource == null)
    		fgDefault.tryDefaultSelection();
        return fgDefault;
    }

    private void tryDefaultSelection() {
        IWorkbenchPage page = fWindowStack.peek().getActivePage();
        if (page != null) {
            IViewPart view = page.findView(IPageLayout.ID_RES_NAV);
            if (view != null)
            	selectionChanged(view, view.getViewSite().getSelectionProvider().getSelection());
        }
    }
    
    public void setToolsLinked(boolean linked){
    	this.toolsLinked=linked;
    }
    public boolean isToolsLinked(){
    	return toolsLinked;
    }
    
    public void setToolSequence (ToolSequence toolSequence){
    	this.toolSequence=toolSequence;
    }

    public ToolSequence getToolSequence(){
    	return toolSequence;
    }
    
    /**
     * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
     */
    public void windowActivated(IWorkbenchWindow window) {
        fWindowStack.remove(window);
        fWindowStack.push(window);
        ISelectionService service = window.getSelectionService(); 
        service.addSelectionListener(this);
        IWorkbenchPage page = window.getActivePage();
        if (page != null) {
            IWorkbenchPart part = page.getActivePart();
            if (part != null) {             
                ISelection selection = service.getSelection();
                if (selection != null) {
                    selectionChanged(part, selection);
                }
            }
        }
    } // windowActivated()

    /**
     * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
     */
    public void windowClosed(IWorkbenchWindow window) {
        ISelectionService selectionService = window.getSelectionService();
        selectionService.removeSelectionListener(this);
        fWindowStack.remove(window);
    }

    /**
     * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
     */
    public void windowDeactivated(IWorkbenchWindow window) {
    }

    /**
     * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
     */
    public void windowOpened(IWorkbenchWindow window) {
        windowActivated(window);
    }

    /**
     * Returns the currently selected resource in the active workbench window,
     * or <code>null</code> if none. If an editor is active, the resource adapter
     * associated with the editor is returned.
     * 
     * @return selected resource or <code>null</code>
     */
    public IResource getSelectedResource() {
        return fSelectedResource;
    }
    
    public IProject getSelectedProject() {
        if (fSelectedResource != null)
            return fSelectedResource.getProject();
        else
            return null;
    }

    public IResource getSelectedVerilogFile() {
        return fSelectedVerilogFile;
    }

     /**
     * Returns resource by selection in the active workbench window,
     * or <code>null</code> if none. If an editor is active, the resource adapater
     * associated with the editor is returned.
     * 
     * @return selected resource or <code>null</code>
     */
    public IResource getSelectedResource(IWorkbenchPart part, ISelection selection) {
        IResource selectedResource = null;
        if (selection instanceof IStructuredSelection) {
            Object result = ((IStructuredSelection)selection).getFirstElement();
            if (result instanceof IResource) {
                selectedResource = (IResource) result;
            } else if (result instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) result;
                selectedResource = (IResource)adaptable.getAdapter(IResource.class);
            }
        }
        
        if (selectedResource == null) {
            // If the active part is an editor, get the file resource used as input.
            if (part instanceof IEditorPart) {
                IEditorPart editorPart = (IEditorPart) part;
                IEditorInput input = editorPart.getEditorInput();
                selectedResource = (IResource) input.getAdapter(IResource.class);
            } 
        }

        if (selectedResource != null)
            return selectedResource;
        else
            return fSelectedResource;
    } // getSelectedResource()
    

    public IResource getViewSelectedResource(String viewId) {
        IWorkbenchPage page = fWindowStack.peek().getActivePage();
        if (page != null) {
            IViewPart view = page.findView(viewId);
            if (view != null) {
                return getSelectedResource(view, view.getViewSite().getSelectionProvider().getSelection());
            }    
        }
        return null;
    }
    
    /**
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
			System.out.print("$$$ Selection changed, fSelectedResource="+fSelectedResource);
//    	System.out.println("SelectedResourceManager.selectionChanged()");
        IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
        if (fWindowStack.isEmpty() || !fWindowStack.peek().equals(window)) {
            // selection is not in the active window
        	System.out.println(" - stray selection outside acrtive window");
            return;
        }
        IResource selectedResource = getSelectedResource(part, selection);
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
			System.out.println(" New selection: "+selectedResource);
        if (selectedResource != null) {
            fSelectedResource = selectedResource;
            if ((selectedResource.getType()==IResource.FILE) && (VerilogUtils.isHhdlFile((IFile)fSelectedResource))){
                fSelectedVerilogFile = selectedResource; /* Maybe same will work for vhdl too? */
        		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        			System.out.println("Updated fSelectedVerilogFile: "+fSelectedVerilogFile);
            } else {
        		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        			System.out.println(selectedResource+" is not a file or not an HDL file");
            }
        }
        
        if (selection instanceof ITextSelection) {
            fSelectedText = (ITextSelection)selection;
        }
    } // selectionChanged()
//TODO: Make them project-relative 
    public String tryRelativePath(String path){
    	if (path==null)
    		return null;
    	if (getSelectedProject()==null) return path;
    	IProject project=getSelectedProject();
    	if (path.startsWith(project.getLocation().toString()))
    		return path.substring(project.getLocation().toString().length()+1);
    	return path;
    }
    public void updateActionChoice(String chosenTarget, int choice, String ignoreFilter){
    	fChosenAction=choice;
    	fChosenTarget=tryRelativePath(chosenTarget);
    	this.ignoreFilter=ignoreFilter;
    	IProject project=getSelectedProject();
    	if (project==null) return;
    	IPath path = new Path(fChosenTarget);
    	IFile file=null;
    	try {
    		file = (path==null)?null:project.getFile(path);
    	} catch (IllegalArgumentException e) {
    		// Path must include project and resource name: /x353
    	}
    	if ((file != null) &&  (VerilogUtils.isHhdlFile(file)))
    		fChosenVerilogFile=file;
    	else if (fChosenVerilogFile==null)
    		fChosenVerilogFile=fSelectedVerilogFile;
    	if (file!=null){
    		fChosenShort=file.getName(); // last segment
    	} else {
    		fChosenShort=fChosenTarget; // whatever
    	}
    }
    
    
    // Build stamp/date methods
    public String setBuildStamp(){
    	timestamp=getBuildStamp(new Date());
    	return getBuildStamp();
    }

    public String getBuildStamp(){
    	return timestamp;
    }

    public static String getBuildStamp(Date date){
    	return new SimpleDateFormat(VDT.TIME_STAMP_FORMAT).format(date);
    }
    
    public static Date parseStamp(String stamp){
    	Date d;
		try {
			d = new SimpleDateFormat(VDT.TIME_STAMP_FORMAT).parse(stamp);
		} catch (ParseException e) {
			d=new Date(0);
			System.out.println("Date format '"+stamp+"' not recognized, using beginning of all of times: "+
					new SimpleDateFormat(VDT.TIME_STAMP_FORMAT).format(d));
			return d; // 1970
		}
    	return d;
    }
    
    public static boolean afterStamp(String after, String before){
    	return  parseStamp(after).after(parseStamp(before));
    }
    
    public String getChosenTarget() {
        return fChosenTarget;
    }
    public String getChosenShort() {
        return fChosenShort;
    }
    
    public IResource getChosenVerilogFile() {
    	IProject project=getSelectedProject();
    	if (project==null) return null;
    	IResource rslt=(fChosenVerilogFile!=null)?fChosenVerilogFile:fSelectedVerilogFile;
    	if (rslt==null) return null;
    	if (project.getFullPath().toPortableString().equals(rslt.getProject().getFullPath().toPortableString())){
    		return (fChosenVerilogFile!=null)?fChosenVerilogFile:fSelectedVerilogFile;
    	} else {
    		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
    			System.out.println("Wrong getChosenVerilogFile="+rslt+" for project "+project);
    		}
    		fChosenVerilogFile = null; // invalidate
    		if (fSelectedVerilogFile==null) return null;
    		if (project.getFullPath().toPortableString().equals(fSelectedVerilogFile.getProject().getFullPath().toPortableString())){
        		System.out.println("Using: "+fSelectedVerilogFile);
    			return  fSelectedVerilogFile;
    		} else {
        		System.out.println("fSelectedVerilogFile is also wrong: "+fSelectedVerilogFile);
    		}
    		return fSelectedResource;
    	}
    }

    // Used when restoring from memento
    public void setChosenVerilogFile(IResource file) {
        if (file != null){
        	if (file.getType() != IResource.FILE) {
        		String msg="Tried to use "+file+" as HDL file";
        		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
        			MessageUI.error(msg);
        		}
        		System.out.println(msg);
        		return;
        	}
        	if (!VerilogUtils.isHhdlFile((IFile)file)){
        		String msg="Tried to use non-HDL "+file+" as HDL file";
        		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
        			MessageUI.error(msg);
        		}
        		System.out.println(msg);
        		return;
        	}

        }
    	fChosenVerilogFile=file;
    	IProject project=getSelectedProject();
    	IProject newProject= (file == null)? null: file.getProject();
    	// if file is different project than selectedResource
    	if ((newProject != null) && ((project == null) || !newProject.getFullPath().toPortableString().equals(project.getFullPath().toPortableString()))){
    		fSelectedResource=file;
    	}
    	if (fSelectedResource==null) fSelectedResource=file;
    }

    
    public int getChosenAction() {
        return fChosenAction;
    }
    
 //   public Tool getSelectedTool(){
 //   	return selectedTool;
 //   }
    
    public String getFilter(){
    	return ignoreFilter;
    }
    public void setFilter(String  filter){
    	ignoreFilter=filter;
    }


    
    
    /**
     * Returns the current text selection as a <code>String</code>, or <code>null</code> if
     * none.
     * 
     * @return the current text selection as a <code>String</code> or <code>null</code>
     */
    public String getSelectedText() {
        return fSelectedText.getText();
    }
    
} // class SelectedResourceManager()
