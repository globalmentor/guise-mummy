package com.guiseframework.component;

import static java.util.Collections.*;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.garretwilson.lang.ObjectUtilities.*;

/**Abstract implementation of an application frame.
@author Garret Wilson
@see LayoutPanel
*/
public abstract class AbstractApplicationFrame<C extends ApplicationFrame<C>> extends AbstractFrame<C> implements ApplicationFrame<C>
{

	/**The list of child frames according to z-order.*/
	private final List<Frame<?>> frameList=new CopyOnWriteArrayList<Frame<?>>();

		/**@return An iterable to all child frames.*/
		public Iterable<Frame<?>> getChildFrames() {return unmodifiableList(frameList);}
	
		/**Adds a frame to the list of child frames.
		This method should usually only be called by the frames themselves.
		@param frame The frame to add.
		@exception NullPointerException if the given frame is <code>null</code>.
		@exception IllegalArgumentException if the given frame is this frame.
		*/
		public void addChildFrame(final Frame<?> frame)
		{
			if(checkInstance(frame, "Frame cannot be null.")==this)
			{
				throw new IllegalArgumentException("A frame cannot be its own child frame.");
			}			
			frameList.add(frame);	//add this frame to our list
			addComponent(frame);	//add the frame as a child of this component
			try
			{
				setInputFocusedComponent(frame);	//set the new frame as the focus component
			}
			catch(final PropertyVetoException propertyVetoException)	//if we can't focus on the new frame, ignore the error
			{
			}
		}

		/**Removes a frame from the list of child frames.
		This method should usually only be called by the frames themselves.
		@param frame The frame to remove.
		@exception NullPointerException if the given frame is <code>null</code>.
		@exception IllegalArgumentException if the given frame is the application frame.
		*/
		public void removeChildFrame(final Frame<?> frame)
		{
			if(checkInstance(frame, "Frame cannot be null.")==this)
			{
				throw new IllegalArgumentException("A frame cannot be its own child frame.");
			}
			frameList.remove(frame);	//remove this frame from the list
			removeComponent(frame);	//remove the frame as a child of this component
			final InputFocusableComponent<?> oldFocusedComponent=getInputFocusedComponent();	//get the focused component
			if(frame==oldFocusedComponent)	//if we just removed the focused component
			{
				final InputFocusableComponent<?> newFocusedComponent=frameList.isEmpty() ? null : frameList.get(frameList.size()-1);	//if we have more frames, set the focus to the last one TODO important fix race condition in finding the last frame
				try
				{
					setInputFocusedComponent(newFocusedComponent);	//set the new frame as the focus component
				}
				catch(final PropertyVetoException propertyVetoException)	//if we can't focus on the new frame
				{
					throw new AssertionError("Cannot focus on remaining frame.");
					//TODO fix to focus on an application frame child component, especially if there are no frames left
				}
			}
		}

	/**Component constructor.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	*/
	public AbstractApplicationFrame(final Component<?> component)
	{
		super(component);	//construct the parent class
	}

	/**Determines whether the frame should be allowed to close.
	This implementation returns <code>false</code>.
	This method is called from {@link #close()}.
	@return <code>true</code> if the frame should be allowed to close.
	*/
	public boolean canClose()
	{
		return false;	//don't allow application frames to be closed
	}

	/**Retrieves a list of all child components.
	This version adds all this frame's child frames to the list.
	@return A list of child components.
	@see #getChildFrames()
	*/
	protected List<Component<?>> getChildList()
	{
		final List<Component<?>> childList=super.getChildList();	//get the default list of children
		childList.addAll(frameList);	//add all child frames to the list
		return childList;	//return the list of children, now including child frames
	}
	
	/**Determines whether this component has children.
	This version also checks to see whether there are child frames.
	@return Whether this component has children.
	@see #getChildFrames()
	 */
	public boolean hasChildren()
	{
		return super.hasChildren() || !frameList.isEmpty();	//see if we have normal children and/or frames
	}

}
