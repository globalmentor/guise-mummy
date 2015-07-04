/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import com.globalmentor.collections.DecoratorReadWriteLockMap;
import com.globalmentor.collections.PurgeOnWriteWeakValueHashMap;
import com.globalmentor.collections.ReadWriteLockMap;
import com.globalmentor.util.*;
import com.guiseframework.component.*;
import com.guiseframework.model.*;
import com.guiseframework.platform.*;

import static com.globalmentor.text.css.CSS.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**
 * Strategy for rendering a tree component as an XHTML <code>&lt;div&gt;</code> element.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebTreeControlDepictor<C extends TreeControl> extends AbstractDecoratedWebComponentDepictor<C> {

	//TODO move these resource keys to the control
	/** The resource bundle key for the tree node expanded image URI. */
	public static final String TREE_NODE_COLLAPSED_IMAGE_RESOURCE_KEY = "theme.tree.node.collapsed.image";
	/** The resource bundle key for the tree node collapsed image URI. */
	public static final String TREE_NODE_EXPANDED_IMAGE_RESOURCE_KEY = "theme.tree.node.expanded.image";
	/** The resource bundle key for the tree node leaf image URI. */
	public static final String TREE_NODE_LEAF_IMAGE_RESOURCE_KEY = "theme.tree.node.leaf.image";

	/** A listener to listen for changes in properties of tree nodes in the model and marks the view as modified accordingly. */
	/*TODO del; now that we use normal property change events, override the default AbstractView routines with node-specific dirtying
		private final TreeNodePropertyChangeListener<Object> treeNodePropertyChangeListener=new TreeNodePropertyChangeListener<Object>()
			{
				public void propertyChange(final TreeNodePropertyChangeEvent<Object> treeNodePropertyChangeEvent) {	//if the control fires a tree node property change event
					setUpdated(false);	//mark the view as dirty	TODO do more specific dirtying here
				}		
			};
	*/

	/** The read/write lock weak value map of tree nodes associated with IDs. */
	private final ReadWriteLockMap<Long, TreeNodeModel<?>> idTreeNodeMap;

	/** The read/write lock weak key map of IDs associated with tree nodes, using {@link #idTreeNodeMap} as the read/write lock. */
	private final ReadWriteLockMap<TreeNodeModel<?>, Long> treeNodeIDMap;

	/**
	 * Determines the ID of the given tree node in this view. If the tree node has no ID assigned, one will be generated.
	 * @param treeNode The tree node for which an ID should be returned.
	 * @return The unique ID of the given tree node.
	 */
	public long getTreeNodeID(final TreeNodeModel<?> treeNode) {
		Long treeNodeID = treeNodeIDMap.get(treeNode); //get the ID for the tree node
		if(treeNodeID == null) { //if there is yet no ID assigned to the tree node
			treeNodeIDMap.writeLock().lock(); //get a write lock to the maps
			try {
				treeNodeID = treeNodeIDMap.get(treeNode); //check again for the ID for the tree node, now that we have a write lock
				if(treeNodeID == null) { //if there still no ID assigned to the tree node
					treeNodeID = Long.valueOf(getPlatform().generateDepictID()); //generate an ID for this tree node
					idTreeNodeMap.put(treeNodeID, treeNode); //associate the tree node with this ID
					treeNodeIDMap.put(treeNode, treeNodeID); //associate the ID with the tree node
				}
			} finally {
				treeNodeIDMap.writeLock().unlock(); //always release the write lock to the maps
			}
		}
		return treeNodeID.longValue(); //return the tree node ID
	}

	/**
	 * Determines the tree node associated with the given ID.
	 * @param treeNodeID An ID assigned to a tree node.
	 * @return The tree node associated with the given ID, or <code>null</code> if there is no tree node associated with the ID.
	 */
	public TreeNodeModel<?> getTreeNode(final long treeNodeID) {
		return idTreeNodeMap.get(Long.valueOf(treeNodeID)); //see if there is a tree node associated with this ID
	}

	/** Default constructor using the XHTML <code>&lt;div&gt;</code> element. */
	public WebTreeControlDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV); //represent <xhtml:div>
		idTreeNodeMap = new DecoratorReadWriteLockMap<Long, TreeNodeModel<?>>(new PurgeOnWriteWeakValueHashMap<Long, TreeNodeModel<?>>()); //create the tree node map
		//create an ID map using the tree node map as the read/write lock
		treeNodeIDMap = new DecoratorReadWriteLockMap<TreeNodeModel<?>, Long>(new WeakHashMap<TreeNodeModel<?>, Long>(), idTreeNodeMap); //TODO switch to a safer implementation of a weak hash map, which has problems
	}

	/**
	 * Called when the view is installed in a component. This implementation listens for {@link TreeNodePropertyChangeEvent}s fired from the control and marks the
	 * view as needing updated.
	 * @param component The component into which this view is being installed.
	 * @throws NullPointerException if the given component is <code>null</code>.
	 * @throws IllegalStateException if this view is already installed in a component.
	 */
	/*TODO del if not needed
		public void installed(final C component)
		{
			super.installed(component);	//install the view normally
			component.addTreeNodePropertyChangeListener(treeNodePropertyChangeListener);	//listen for tree node property changes
		}
	*/

	/**
	 * Called when the view is uninstalled from a component. This implementation stops listening for {@link TreeNodePropertyChangeEvent}s fired from the control.
	 * @param component The component from which this view is being uninstalled.
	 * @throws NullPointerException if the given component is <code>null</code>.
	 * @throws IllegalStateException if this view is not installed in a component.
	 */
	/*TODO del if not needed
		public void uninstalled(final C component)
		{
			super.uninstalled(component);	//uninstall the view normally
			component.removeTreeNodePropertyChangeListener(treeNodePropertyChangeListener);	//stop listening for tree node property changes
		}
	*/

	/**
	 * Processes an event from the platform.
	 * @param event The event to be processed.
	 * @throws IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	 */
	public void processEvent(final PlatformEvent event) {
		if(event instanceof WebActionDepictEvent) { //if this is an action control event
			final WebActionDepictEvent webActionEvent = (WebActionDepictEvent)event; //get the action control event
			final TreeControl treeControl = getDepictedObject(); //get the depicted object
			if(webActionEvent.getDepictedObject() != treeControl) { //if the event was meant for another depicted object
				throw new IllegalArgumentException("Depict event " + event + " meant for depicted object " + webActionEvent.getDepictedObject());
			}
			final WebPlatform platform = getPlatform(); //get the platform
			final String targetID = webActionEvent.getTargetID(); //get the action target
			//			Log.trace("just got action event for tree node target:", targetID);
			if(targetID.endsWith("-child")) { //if the action was for the tree node itself TODO use a constant
				final String treeNodeIDString = targetID.substring(0, targetID.length() - "-child".length()); //get the tree node ID string
				final long treeNodeID = platform.getDepictID(treeNodeIDString); //get the ID for the tree node
				final TreeNodeModel<?> treeNode = getTreeNode(treeNodeID); //get the associated tree node
				if(treeNode != null) { //if we found a tree node
				//			Log.trace("found tree node for action:", treeNode);
					treeNode.performAction(1, webActionEvent.getOption()); //perform the action on the tree node
				}
			} else if(targetID.endsWith("-treeNode-action")) { //if this was a tree-node action TODO use a constant
				final String treeNodeIDString = targetID.substring(0, targetID.length() - "-treeNode-action".length()); //get the tree node ID					
				final long treeNodeID = platform.getDepictID(treeNodeIDString); //get the ID for the tree node
				final TreeNodeModel<?> treeNode = getTreeNode(treeNodeID); //get the associated tree node
				if(treeNode != null) { //if we found a tree node
					//TODO check enabled/disabled status
					//Log.trace("toggling expanded state", treeNode.isExpanded(), "to", !treeNode.isExpanded(), "for tree node", treeNodeID);
					treeNode.setExpanded(!treeNode.isExpanded()); //toggle the tree node expanded state
				}
			}
			/*TODO fix
							if((component.getID()+"-close").equals(actionControlEvent.getTargetID())) {	//if the close button was selected TODO use a constant
								component.close();	//close the frame
							}
			*/
		}
		super.processEvent(event); //do the default event processing
	}

	/**
	 * Retrieves the style IDs for a tree node.
	 * @param <T> The type of value contained in the tree node. This version returns the base tree node style ID {@link #TREE_NODE_CLASS} with the given
	 *          prefix/suffix, if any, along with styles representing the expandeded or collapseed state and whether the node is a leaf.
	 * @param treeModel The component model.
	 * @param treeNode The tree node.
	 * @param prefix The prefix that needs to be added to the base style, or <code>null</code> if there is no prefix to add.
	 * @param suffix The suffix that needs to be added to the base style, or <code>null</code> if there is no suffix to add.
	 * @return The style IDs for the tree node.
	 */
	protected <T> Set<String> getTreeNodeStyleIDs(final TreeModel treeModel, final TreeNodeModel<T> treeNode, final String prefix, final String suffix) {
		final Set<String> styleIDs = getBaseStyleIDs(prefix, suffix); //get the component's base style IDs
		//TODO del when works		final Set<String> styleIDs=new HashSet<String>();	//create a new set of style IDs
		styleIDs.add(decorateID(TREE_NODE_CLASS, prefix, suffix)); //add a style ID for the tree node with correct prefix and/or suffix
		styleIDs.add(treeNode.isExpanded() ? TREE_NODE_EXPANDED_CLASS : TREE_NODE_COLLAPSED_CLASS); //indicate expanded or collapsed
		if(treeNode.isLeaf()) { //if the tree node is a leaf
			styleIDs.add(TREE_NODE_LEAF_CLASS); //add a leaf style ID
		}
		return styleIDs; //return the style IDs
	}

	/**
	 * Updates the views of any children.
	 * @throws IOException if there is an error updating the child views.
	 * @see DepictContext.State#UPDATE_VIEW
	 */
	protected void depictChildren() throws IOException {
		//TODO add "root" class
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		//don't do the default updating of child views, because we control all the writing in the order we want
		final TreeNodeModel<?> rootTreeNode = component.getRootNode(); //get the root node
		if(rootTreeNode instanceof DummyTreeNodeModel || !component.isRootNodeDisplayed()) { //if the root tree node is just a dummy node, or the component doesn't want to show the root node TODO what would happen if we were to show the root node?
			updateTreeNodeChildViews(component, rootTreeNode); //update the views of the root node's child tree nodes, if any
		} else { //if the root node is not just a dummy node and the component wants to show the root node, render the root node along with its children
		//TODO start DIV component-body here		
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_UL); //<xhtml:ul>		
			//TODO fix			context.writeAttribute(null, ATTRIBUTE_CLASS, TREE_NODE_CLASS+getTreeNodeStyleIDSuffix(rootTreeNode));	//write the style class attribute			
			updateTreeNodeView(component, component.getRootNode()); //update the view of thet root node and its children
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_UL); //</xhtml:ul>
			//TODO end DIV component-body here		
		}
	}

	/**
	 * Updates the view of a tree node.
	 * @param <T> The type of value contained in the tree node.
	 * @param treeModel The component model.
	 * @param treeNode The tree node.
	 * @throws IOException if there is an error updating the tree node view.
	 */
	protected <T> void updateTreeNodeView(final TreeModel treeModel, final TreeNodeModel<T> treeNode) throws IOException {
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		final long treeNodeID = getTreeNodeID(treeNode); //get the ID for this tree node
		final String treeNodeIDString = getPlatform().getDepictIDString(treeNodeID); //get the string form of the tree node ID
		//TODO maybe replace all the component-children and and component-child suffixes with some sort of tree node indication, because these aren't really component children but model chidren
		//TODO fix all the ID/class stuff, because that will duplicate the ID of the tree component; use the ID of the generated component instead
		final Set<String> treeNodeStyleIDs = getTreeNodeStyleIDs(treeModel, treeNode, null, COMPONENT_CHILD_CLASS_SUFFIX); //get the base style IDs with the correct suffix
		treeNodeStyleIDs.add(ACTION_CLASS); //allow the tree node to be an action
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_LI); //<xhtml:li>
		depictContext.writeAttribute(null, ATTRIBUTE_ID, decorateID(treeNodeIDString, null, COMPONENT_CHILD_CLASS_SUFFIX)); //write the ID with the correct prefix and suffix		
		writeClassAttribute(treeNodeStyleIDs); //write the style IDs (treeNode-child)

		//TODO del		writeIDClassAttributes(context, component, null, COMPONENT_CHILD_CLASS_SUFFIX);	//write the ID and class attributes for the child
		//TODO fix		context.writeAttribute(null, ATTRIBUTE_CLASS, TREE_NODE_CLASS+getTreeNodeStyleIDSuffix(treeNode));	//write the style class attribute
		/*TODO del when works
					//write the toggle subcomponent
				context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//<xhtml:div> (treeNode-toggle)
				context.writeAttribute(null, ATTRIBUTE_CLASS, TREE_NODE_TOGGLE_CLASS);	//write the style class attribute		
				writeDirectionAttribute(context, component);	//write the component direction, if this component specifies a direction
				final char toggleChar=treeNode.hasChildren() ? (treeNode.isExpanded() ? HYPHEN_MINUS_CHAR : PLUS_SIGN_CHAR) : MIDDLE_DOT_CHAR;	//use +, -, or � depending on the circumstances
				context.write(toggleChar);	//write the toggle character
				context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//</xhtml:div>
		*/

		/*TODO fix if needed; check analogous accordion menu code
				if(!treeNode.isExpanded()) {	//if the tree node is not expanded
					context.writeAttribute(null, "style", "display:none;");	//don't display the wrapper, either (this is necessary for IE, which still leaves a space for the component) TODO use a constant
				}
		*/

		//TODO del; not needed with indents		for(int i=treeNode.getDepth()-1; i>=0; --i)	//TODO testing; check for dummy root node to modify depth
		/*TODO del
				{
					context.write('X');
				}
		*/

		//write the component
		final Component treeNodeComponent = component.getComponent(treeNode); //get the component for this tree node
		//TODO del Log.trace("rendering tree node for", treeNode.getValue(), "is leaf", treeNode.isLeaf());	//TODO del
		//action
		//determine the image resource key to use for the tree node; use a leaf image if the image is a leaf, otherwise show the expanded or collapsed state
		final String treeNodeImageResourceKey = treeNode.isLeaf() ? TREE_NODE_LEAF_IMAGE_RESOURCE_KEY
				: (treeNode.isExpanded() ? TREE_NODE_EXPANDED_IMAGE_RESOURCE_KEY : TREE_NODE_COLLAPSED_IMAGE_RESOURCE_KEY);
		final URI treeNodeImageURI = getSession().getURIResource(treeNodeImageResourceKey); //look up the URI of the image
		final Set<String> actionStyleIDs = getTreeNodeStyleIDs(treeModel, treeNode, null, "-treeNode-action"/*TODO fix FRAME_CLOSE_CLASS_SUFFIX*/); //get the base style IDs with the correct suffix
		actionStyleIDs.add(ACTION_CLASS); //allow the image to be an action
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IMG, true); //<xhtml:img> (component-action)
		depictContext.writeAttribute(null, ATTRIBUTE_ID, decorateID(treeNodeIDString, null, "-treeNode-action"/*TODO fix FRAME_CLOSE_CLASS_SUFFIX*/)); //write the ID with the correct suffix
		writeClassAttribute(actionStyleIDs); //write the title style IDs
		//TODO why aren't we using the suffix version of getting the depict URI?
		depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, depictContext.getDepictionURI(treeNodeImageURI).toString()); //src="treenode-XXX.gif"
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IMG); //</xhtml:img> (component-close)

		depictContext.write(" "); //separate the icon and the component

		treeNodeComponent.depict(); //update the component's view
		updateTreeNodeChildViews(treeModel, treeNode); //update the views of any child tree nodes
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_LI); //</xhtml:li>
	}

	/**
	 * Updates the views of a tree node's children, if any.
	 * @param <T> The type of value contained in the tree node.
	 * @param treeModel The component model.
	 * @param treeNode The tree node the views of the children of which should be updated.
	 * @throws IOException if there is an error updating the child tree node views.
	 */
	protected <T> void updateTreeNodeChildViews(final TreeModel treeModel, final TreeNodeModel<T> treeNode) throws IOException {
		//TODO testing		if(treeNode.hasChildren())	//if this tree node has children
		if(treeNode.hasChildren() && treeNode.isExpanded()) { //if this tree node has children and it is expanded			
			final WebDepictContext depictContext = getDepictContext(); //get the depict context
			final long treeNodeID = getTreeNodeID(treeNode); //get the ID for this tree node
			final String treeNodeIDString = getPlatform().getDepictIDString(treeNodeID); //get the string form of the tree node ID
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div> (component-body)
			depictContext.writeAttribute(null, ATTRIBUTE_ID, decorateID(treeNodeIDString, null, COMPONENT_BODY_CLASS_SUFFIX)); //write the ID with the correct prefix and suffix		
			writeClassAttribute(getTreeNodeStyleIDs(treeModel, treeNode, null, COMPONENT_BODY_CLASS_SUFFIX)); //write the style IDs (treeNode-body)

			//TODO del			writeBodyIDClassAttributes(context, component, null, COMPONENT_BODY_CLASS_SUFFIX);	//write the ID and class attributes for the body
			final Map<String, Object> bodyStyles = new HashMap<String, Object>(); //create a new map of styles
			final String display = treeNode.isExpanded() ? CSS_DISPLAY_BLOCK : CSS_DISPLAY_NONE; //only show the body if the tree node is expanded
			bodyStyles.put(CSS_PROP_DISPLAY, display); //show or hide the body based upon open state
			writeStyleAttribute(bodyStyles); //write the body style

			//don't write children if this is a dynamic tree node that hasn't been loaded and isn't expanded, or we would eventually load render all dynamic tree nodes, negating the purpose of dynamic tree nodes 
			//TODO del; no longer needed			if(treeNode.isLeaf() || treeNode.isExpanded() || !(treeNode instanceof DynamicTreeNodeModel) || ((DynamicTreeNodeModel<?>)treeNode).isChildNodesLoaded())
			{
				depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_OL); //<xhtml:ol> (component-children)
				writeClassAttribute(getTreeNodeStyleIDs(treeModel, treeNode, null, COMPONENT_CHILDREN_CLASS_SUFFIX)); //write the style IDs (treeNode-children)
				//TODO del			writeIDClassAttributes(context, component, null, COMPONENT_CHILDREN_CLASS_SUFFIX);	//write the ID and class attributes for the children
				//TODO fix			context.writeAttribute(null, ATTRIBUTE_CLASS, TREE_NODE_CLASS+getTreeNodeStyleIDSuffix(treeNode));	//write the style class attribute						
				for(final TreeNodeModel<?> childTreeNode : treeNode) { //for each child tree node
					updateTreeNodeView(treeModel, childTreeNode); //update the view of this child node tree
				}
				depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_OL); //</xhtml:ol> (treeNode-children)
			}
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div> (treeNode-body)
		}
	}
}
