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
package com.elphel.vdt.ui.dialogs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class VDTErrorDialog extends IconAndMessageDialog {
    private static final String NESTING_INDENT = "  ";
    private static final int MAX_DETAILS_LINES = 15;
    
    private Button detailsButton;
    private String title;
    private Text text;
    private boolean textCreated = false;
    private int displayMask = 0xFFFF;
    private IStatus status;

    /**
     * Creates an error dialog. Note that the dialog will have no visual
     * representation (no widgets) until it is told to open.
     * <p>
     * Normally one should use <code>openError</code> to create and open one
     * of these. This constructor is useful only if the error object being
     * displayed contains child items <it>and </it> you need to specify a mask
     * which will be used to filter the displaying of these children.
     * </p>
     * 
     * @param parentShell
     *            the shell under which to create this dialog
     * @param dialogTitle
     *            the title to use for this dialog, or <code>null</code> to
     *            indicate that the default title should be used
     * @param message
     *            the message to show in this dialog, or <code>null</code> to
     *            indicate that the error's message should be shown as the
     *            primary message
     * @param status
     *            the error to show to the user
     * @param displayMask
     *            the mask to use to filter the displaying of child items, as
     *            per <code>IStatus.matches</code>
     * @see org.eclipse.core.runtime.IStatus#matches(int)
     */
    public VDTErrorDialog(Shell parentShell, 
                          String dialogTitle, 
                          IStatus status, 
                          int displayMask) 
    {
        super(parentShell);
        
        if(dialogTitle != null)
            this.title = dialogTitle;
        else
            this.title = JFaceResources.getString("Problem_Occurred");
                        
        this.message = status.getMessage();
        this.status = status;
        this.displayMask = displayMask;
        
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    protected void buttonPressed(int id) {
        if (id == IDialogConstants.DETAILS_ID) {
            // was the details button pressed?
            toggleDetailsArea();
        } else {
            super.buttonPressed(id);
        }
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }

    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, 
                     IDialogConstants.OK_ID, 
                     IDialogConstants.OK_LABEL,
                     true);
        
        if (shouldShowDetailsButton())
            detailsButton = createButton(parent, 
                                         IDialogConstants.DETAILS_ID,
                                         IDialogConstants.SHOW_DETAILS_LABEL, 
                                         false);
    }

    protected Control createDialogArea(Composite parent) {
        createMessageArea(parent);

        // create a composite with standard margins and spacing
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.numColumns = 2;
        
        composite.setLayout(layout);

        GridData childData = new GridData(GridData.FILL_BOTH);
        childData.horizontalSpan = 2;
        
        composite.setLayoutData(childData);
        composite.setFont(parent.getFont());
        
        return composite;
    }

    /*
     * @see IconAndMessageDialog#createDialogAndButtonArea(Composite)
     */
    protected void createDialogAndButtonArea(Composite parent) {
        super.createDialogAndButtonArea(parent);
        if (this.dialogArea instanceof Composite) {
            //Create a label if there are no children to force a smaller layout
            Composite dialogComposite = (Composite) dialogArea;
            if (dialogComposite.getChildren().length == 0)
                new Label(dialogComposite, SWT.NULL);
        }
    }

    protected Image getImage() {
        if (status != null) {
            if (status.getSeverity() == IStatus.WARNING)
                return getWarningImage();
            if (status.getSeverity() == IStatus.INFO)
                return getInfoImage();
        }
        //If it was not a warning or an error then return the error image
        return getErrorImage();
    }

    private Text createDropDownText(Composite parent) {
        // create the text
        text = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        text.setFont(parent.getFont());

        // fill the text
        populateText(text);
        
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        
        data.heightHint = Math.min(MAX_DETAILS_LINES, text.getLineCount()) * text.getLineHeight();
        data.horizontalSpan = 2;
        text.setLayoutData(data);
        text.setFont(parent.getFont());
        
        textCreated = true;
        
        return text;
    }

    /**
     * Extends <code>Window.open()</code>. Opens an error dialog to display
     * the error. If you specified a mask to filter the displaying of these
     * children, the error dialog will only be displayed if there is at least
     * one child status matching the mask.
     */
    public int open() {
        if (shouldDisplay(status, displayMask))
            return super.open();
        
        setReturnCode(OK);
        return OK;
    }

    /**
     * Opens an error dialog to display the given error. Use this method if the
     * error object being displayed does not contain child items, or if you wish
     * to display all such items without filtering.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param dialogTitle
     *            the title to use for this dialog, or <code>null</code> to
     *            indicate that the default title should be used
     * @param message
     *            the message to show in this dialog, or <code>null</code> to
     *            indicate that the error's message should be shown as the
     *            primary message
     * @param status
     *            the error to show to the user
     * @return the code of the button that was pressed that resulted in this
     *         dialog closing. This will be <code>Dialog.OK</code> if the OK
     *         button was pressed, or <code>Dialog.CANCEL</code> if this
     *         dialog's close window decoration or the ESC key was used.
     */
    public static int openError(Shell parent, 
                                String dialogTitle,
                                IStatus status) 
    {
        return openError(parent, 
                         dialogTitle, 
                         status, 
                         IStatus.OK | 
                         IStatus.INFO | 
                         IStatus.WARNING | 
                         IStatus.ERROR);
    }

    /**
     * Opens an error dialog to display the given error. Use this method if the
     * error object being displayed contains child items <it>and </it> you wish
     * to specify a mask which will be used to filter the displaying of these
     * children. The error dialog will only be displayed if there is at least
     * one child status matching the mask.
     * 
     * @param parentShell
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the title to use for this dialog, or <code>null</code> to
     *            indicate that the default title should be used
     * @param message
     *            the message to show in this dialog, or <code>null</code> to
     *            indicate that the error's message should be shown as the
     *            primary message
     * @param status
     *            the error to show to the user
     * @param displayMask
     *            the mask to use to filter the displaying of child items, as
     *            per <code>IStatus.matches</code>
     * @return the code of the button that was pressed that resulted in this
     *         dialog closing. This will be <code>Dialog.OK</code> if the OK
     *         button was pressed, or <code>Dialog.CANCEL</code> if this
     *         dialog's close window decoration or the ESC key was used.
     * @see org.eclipse.core.runtime.IStatus#matches(int)
     */
    public static int openError(Shell parentShell, 
                                String title,
                                IStatus status, 
                                int displayMask) 
    {
        VDTErrorDialog dialog = new VDTErrorDialog(parentShell, 
                                                   title, 
                                                   status, 
                                                   displayMask);
        
        return dialog.open();
    }

    private void populateText(Text textToPopulate) {
        populateText(textToPopulate, status, 0, false);
    }

    /**
     * Populate the text with the messages from the given status. Traverse the
     * children of the status deeply and also traverse CoreExceptions that appear
     * in the status.
     * @param textToPopulate the text to populate
     * @param buildingStatus the status being displayed
     * @param nesting the nesting level (increases one level for each level of children)
     * @param includeStatus whether to include the buildingStatus in the display or
     * just its children
     */
    private void populateText(Text textToPopulate, 
                              IStatus buildingStatus,
                              int nesting, 
                              boolean includeStatus) 
    {        
        if (!buildingStatus.matches(displayMask)) {
            return;
        }

        Throwable t = buildingStatus.getException();
        boolean isCoreException= t instanceof CoreException;
        boolean incrementNesting= false;
        
        String string = "";
        
        if (includeStatus) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < nesting; i++) {
                sb.append(NESTING_INDENT);
            }
            String message = buildingStatus.getMessage();
            sb.append(message);
            string += sb.toString();
            incrementNesting = true;
        }
            
        if (!isCoreException && t != null) {
            // print the stacktrace in the text field
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                t.printStackTrace(ps);
                ps.flush();
                baos.flush();
                string += baos.toString();
            } catch (IOException e) {                
            }
            
            incrementNesting = true;
        }
        
        String oldString = textToPopulate.getText();
        
        textToPopulate.setText(oldString + string);
        
        if (incrementNesting) 
            nesting++;
        
        // Look for a nested core exception
        if (isCoreException) {
            CoreException ce = (CoreException)t;
            IStatus eStatus = ce.getStatus();
            // Only print the exception message if it is not contained in the parent message
            if (message == null || message.indexOf(eStatus.getMessage()) == -1) {
                populateText(textToPopulate, eStatus, nesting, true);
            }
        }

        
        // Look for child status
        IStatus[] children = buildingStatus.getChildren();
        for (int i = 0; i < children.length; i++) {
            populateText(textToPopulate, children[i], nesting, true);
        }
    }

    /**
     * Returns whether the given status object should be displayed.
     * 
     * @param status
     *            a status object
     * @param mask
     *            a mask as per <code>IStatus.matches</code>
     * @return <code>true</code> if the given status should be displayed, and
     *         <code>false</code> otherwise
     * @see org.eclipse.core.runtime.IStatus#matches(int)
     */
    private static boolean shouldDisplay(IStatus status, int mask) {
        IStatus[] children = status.getChildren();
        if (children == null || children.length == 0) {
            return status.matches(mask);
        }
        for (int i = 0; i < children.length; i++) {
            if (children[i].matches(mask))
                return true;
        }
        return false;
    }

    /**
     * Toggles the unfolding of the details area. This is triggered by the user
     * pressing the details button.
     */
    private void toggleDetailsArea() {
        Point windowSize = getShell().getSize();
        Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        
        if (textCreated) {
            text.dispose();
            textCreated = false;
            detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
        } else {
            text = createDropDownText((Composite) getContents());
            detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
        }
        
        Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        getShell()
                .setSize(
                        new Point(windowSize.x, windowSize.y
                                + (newSize.y - oldSize.y)));
    }

    private boolean shouldShowDetailsButton() {
        return status.isMultiStatus() || status.getException() != null;
    }
}
