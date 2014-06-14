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
package com.elphel.vdt.ui.views;

//import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IResource;
//import org.eclipse.jface.viewers.TreeViewer;
//import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
//import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.elphel.vdt.VDT;
import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.contexts.PackageContext;
import com.elphel.vdt.core.tools.contexts.ProjectContext;
import com.elphel.vdt.core.tools.menu.DesignMenu;
import com.elphel.vdt.core.tools.menu.DesignMenuItem;
import com.elphel.vdt.core.tools.menu.DesignMenuToolItem;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.core.tools.params.Tool.TOOL_STATE;
import com.elphel.vdt.ui.VDTPluginImages;
import com.elphel.vdt.ui.variables.SelectedResourceManager;

/**
 * Adapter of core design menu for user interface level.
 * 
 * Created: 28.03.2006
 * @author  Lvov Konstantin
 */

public class DesignMenuModel {

    private Config config;
    
    private MenuItem root;
    private final int IMAGE_MARGIN=2;
    public DesignMenuModel(DesignMenu menu) {
        config = ToolsCore.getConfig();
        root = new MenuItem(null, menu);
    }
    
    public Item[] getItems() {
        return root.getChildren();  
    }

    // ------------------------------------------------------------------------
    public abstract class Item {
        private static final String ICON_ID_PREFIX = VDT.ID_VDT + ".DesignMenu.Image.";
        private String imageKey = null;
        
        protected String imageKeyState = null;

        private DesignMenuItem source;
        private Item parent;
        private int animN;
        private Rectangle animBounds=null;
        private ImageData[] imageData=null;
//        private boolean animEn=true; // just testing, fill be  false;
        final private AtomicBoolean animBusy=new AtomicBoolean(false);
        private Timer timer=null;
//        private int updateWidth=16;
        private Item(Item parent, DesignMenuItem source) {
            this.parent = parent;
            this.source = source;
            this.timer=null;

            String image = source.getIcon();
            if (image != null) {
//              imageKey = ICON_ID_PREFIX + source.getName();
                imageKey = ICON_ID_PREFIX + (new File(source.getName())).getName();
                if (VDTPluginImages.getImage(imageKey) == null)
                    VDTPluginImages.addImage(image, imageKey, null/*tool.getLaunchType()*/);
            }
        }

        public boolean isEnabled(IResource resource) {
            return false;
        }
        public boolean isEnabled(String path) {
            return false;
        }

        public Object getParent() {
            return parent;
        }

        public Item[] getChildren() {
            return new Item[0];
        }
        public boolean hasChildren() {
            return false;
        }

        public String toString() {
            String label = source.getLabel();
            return label != null ? label : "Unknown";
        }
        
        public String getLabel() {
            return toString();
        }
        public String getImageKey() {
            return imageKey;
        }
        public void measureItem (Event event){
        	if (imageKeyState==null) return;
        	Image image=VDTPluginImages.getImage(imageKeyState);
        	if (image!=null) event.width+=IMAGE_MARGIN+image.getBounds().width;
        }
        
        public void showStateIcon(Event event, final Tree tree, final TreeItem item){
        	if ((imageKeyState==null) || (item.isDisposed())) return;
        	int x = event.x + event.width + IMAGE_MARGIN;
        	int itemHeight = tree.getItemHeight();
        	imageData=VDTPluginImages.getImageData(imageKeyState);
        	Image frameImage;
        	boolean isAnimation=false;
        	if ((imageData!=null) && (imageData.length>1) && animBusy.compareAndSet(false,true)){
        		isAnimation=true;
        		animN=animN % imageData.length;
        		frameImage = new Image(Display.getDefault(),imageData[animN]);
        	} else {
        		frameImage = VDTPluginImages.getImage(imageKeyState); // do not dispose() !
        	}
        	int imageHeight = frameImage.getBounds().height;
        	int y = event.y + (itemHeight - imageHeight) / 2;
    		event.gc.drawImage(frameImage, x, y);
//    		System.out.println("showStateIcon(): "+imageKeyState);
        	if (isAnimation){
            	animBounds=frameImage.getBounds();
            	frameImage.dispose();
            	final int fDelayTime=imageData[animN].delayTime*10; // ms
            	animN++;
    			// item does not include our animation on the right of it, so we need to store image location
    			// relative to the top right corner of the item (height does not change)
    			Rectangle r=item.getBounds();
            	animBounds.x=x-(r.x+r.width);
            	animBounds.y=y-(r.y);
            	
            	TimerTask timerTask=new TimerTask() {          
            		@Override
            		public void run() {
           				refresh(item, tree); // threw on shutdown - 
            		}
            	};
        	
            	if (timer==null) timer=new Timer();
            	try {
            		timer.schedule(timerTask, fDelayTime);
            	} catch (IllegalStateException e){
            		System.out.println("DesignMenuModel(): Timer was somewhere canceled, recovering");
            		timer.cancel();
            		timer=new Timer();
            		timer.schedule(timerTask, fDelayTime);
            	}

            	
            	
/*            	
            	timer.schedule(new TimerTask() {          
            		@Override
            		public void run() {
           				refresh(item, tree); // threw on shutdown - 
            		}
            	}, fDelayTime);
*/            	
        	}
        }
        
        public void refresh(final TreeItem item, final Tree tree) {
        	Display.getDefault().syncExec(new Runnable() {
        		public void run() {
        			if (item.isDisposed()) {
            			animBusy.compareAndSet(true, false);
        				return;
        			}
        			Rectangle r=item.getBounds();
//        			System.out.println("animBounds="+animBounds.toString()+" item.getBounds()="+item.getBounds().toString());
        			tree.redraw(
        					animBounds.x+r.x+r.width,
        					animBounds.y+r.y,
        					animBounds.width,
        					animBounds.height,true);
        			animBusy.compareAndSet(true, false);
        		}
        	});
        }
        
        public Tool getTool() {
            return null;   
        }

        public Config getConfig() { 
            return null; 
        }

        public PackageContext getPackageContext() { 
            return  null; 
        }

        public ProjectContext getProjectContext() { 
            return  null; 
        }
    } // class Item
    
    // ------------------------------------------------------------------------
    public class ToolItem extends Item {
        private Tool tool;
        
        private ToolItem(Item parent, DesignMenuToolItem source) {
            super(parent, source);
            tool = ToolsCore.getTool(source.getToolName());
        }

        public boolean isEnabled(IResource resource) {
            if (resource == null)
                return false;

            String extensions[] = tool.getExtensions();
            if (extensions == null)
                return true;
            String resourceExt = resource.getFileExtension();
            if (resourceExt == null)
                return false;
            
            for (int i=0; i < extensions.length; i++) {
                if (resourceExt.equalsIgnoreCase(extensions[i]))
                    return true;    
            }
            return false;
        } // isEnabled(IResource)

        public boolean isEnabled(String path) {
            if (path == null)
                return false;

            String extensions[] = tool.getExtensions();
            if (extensions == null)
                return true;
            int index=path.indexOf(".");
            if (index<0) return false;
            String resourceExt = path.substring(index+1);
            for (int i=0; i < extensions.length; i++) {
                if (resourceExt.equalsIgnoreCase(extensions[i]))
                    return true;    
            }
            return false;
        } // isEnabled(String)

        public Tool getTool() {
            return tool;   
        }

        public Config getConfig() { 
            return config; 
        }

        public PackageContext getPackageContext() {
            PackageContext context = tool.getParentPackage();
            if ((context != null) && context.isVisible())
                return context;
            else
                return null;
        }

        public ProjectContext getProjectContext() { 
            ProjectContext context = tool.getParentProject();
            if ((context != null) && context.isVisible())
                return context;
            else
                return null;
        }
        
        public void measureItem (Event event){
        	super.measureItem (event);
        	boolean pinned=tool.isPinned() || !SelectedResourceManager.getDefault().isToolsLinked();
        	String iconName,key;
        	if (tool.isRunning()){
        		iconName=VDTPluginImages.ICON_TOOLSTATE_RUNNING;
        		key=     VDTPluginImages.KEY_TOOLSTATE_RUNNING;
        	} else if (tool.isWaiting()){
        		iconName=VDTPluginImages.ICON_TOOLSTATE_WAITING;
        		key=     VDTPluginImages.KEY_TOOLSTATE_WAITING;
        	} else if (tool.isAlmostDone()&& (tool.getState()==TOOL_STATE.SUCCESS )){
        		iconName=VDTPluginImages.ICON_TOOLSTATE_ALMOST_GOOD;
        		key=     VDTPluginImages.KEY_TOOLSTATE_ALMOST_GOOD;
        	} else if (tool.isAlmostDone()&& (tool.getState()==TOOL_STATE.UNKNOWN )){
        		iconName=VDTPluginImages.ICON_TOOLSTATE_ALMOST_WTF;
        		key=     VDTPluginImages.KEY_TOOLSTATE_ALMOST_WTF;
        	} else if (tool.isAlmostDone()){ // restoring state, current state new (erased) or failure
        		iconName=VDTPluginImages.ICON_TOOLSTATE_ALMOST_GOOD;
        		key=     VDTPluginImages.KEY_TOOLSTATE_ALMOST_GOOD;
        	} else {
        		switch (tool.getState()){
        		case NEW:
        			iconName=VDTPluginImages.ICON_TOOLSTATE_NEW;
        			key= VDTPluginImages.KEY_TOOLSTATE_NEW;
        			break;
        		case FAILURE:
        			if (tool.isDirtyOrChanged()){
        				iconName=VDTPluginImages.ICON_TOOLSTATE_BAD_OLD;
        				key= VDTPluginImages.KEY_TOOLSTATE_BAD_OLD;
        			} else {
        				iconName=VDTPluginImages.ICON_TOOLSTATE_BAD;
        				key= VDTPluginImages.KEY_TOOLSTATE_BAD;
        			}
        			break;
        		case SUCCESS:
        			if (pinned){
    					iconName=VDTPluginImages.ICON_TOOLSTATE_PINNED;
    					key= VDTPluginImages.KEY_TOOLSTATE_PINNED;
        			} else {
        				if (tool.isDirtyOrChanged()){
        					iconName=VDTPluginImages.ICON_TOOLSTATE_GOOD_OLD;
        					key= VDTPluginImages.KEY_TOOLSTATE_GOOD_OLD;
        				} else {
        					iconName=VDTPluginImages.ICON_TOOLSTATE_GOOD;
        					key= VDTPluginImages.KEY_TOOLSTATE_GOOD;
        				}
        			}
        			break;
        		case KEPT_OPEN:
        				iconName=VDTPluginImages.ICON_TOOLSTATE_KEPT_OPEN;
        				key= VDTPluginImages.KEY_TOOLSTATE_KEPT_OPEN;
        			break;
        		default:
        			if (tool.isDirtyOrChanged()){
        				iconName=VDTPluginImages.ICON_TOOLSTATE_WTF_OLD;
        				key= VDTPluginImages.KEY_TOOLSTATE_WTF_OLD;
        			} else {
        				iconName=VDTPluginImages.ICON_TOOLSTATE_WTF;
        				key= VDTPluginImages.KEY_TOOLSTATE_WTF;
        			}
        		}
        	}
            key = Item.ICON_ID_PREFIX + key;
            if (VDTPluginImages.getImage(key) == null){
                VDTPluginImages.addImage(iconName, key, null);
        	}
        	super.imageKeyState=key;
//        	System.out.println("iconName="+iconName+", key="+key+" imageKeyState="+super.imageKeyState);
        	/*        	
        */
//TODO: Save tool state in memento        	
        }
    } // class ToolItem

    // ------------------------------------------------------------------------
    public class MenuItem extends Item {
        private Item[] items;
        
        private MenuItem(Item parent, DesignMenu source) {
            super(parent, source);
            
            List<Item> itemList = new ArrayList<Item>();
            
            for(Iterator<DesignMenuItem> i = source.getItems().iterator(); i.hasNext();) {
                DesignMenuItem menuitem = (DesignMenuItem)i.next();

                if(menuitem.isVisible())
                    itemList.add(CreateItem(this, menuitem)); 
            }
        
            items = (Item[])itemList.toArray(new Item[itemList.size()]);
        }    
        
        public Item[] getChildren() {
            return items;
        }
        
        public boolean hasChildren() {
            return items.length > 0;
        }

    } // class MenuItem

    private Item CreateItem(Item parent, DesignMenuItem source) {
        if (source instanceof DesignMenuToolItem)
            return new ToolItem(parent, (DesignMenuToolItem)source);
        else if (source instanceof DesignMenu) 
            return new MenuItem(parent, (DesignMenu)source);
        else
            assert(false);
        return null;
    }
    
} // class DesignMenuModel
