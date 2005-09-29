package com.javaguise.component;

import java.util.Iterator;

import com.garretwilson.util.EmptyIterator;
import com.garretwilson.util.ObjectIterator;
import com.javaguise.model.LabelModel;
import com.javaguise.session.GuiseSession;

/**Abstract implementation of a frame.
@author Garret Wilson
@see LayoutPanel
*/
public abstract class AbstractFrame<C extends Frame<C>> extends AbstractComponent<C> implements Frame<C>
{

	/**@return The data model used by this component.*/
	public LabelModel getModel() {return (LabelModel)super.getModel();}

	/**The single child component, or <code>null</code> if this frame does not have a child component.*/
	private Component<?> component;

		/**@return The single child component, or <code>null</code> if this frame does not have a child component.*/
		public Component<?> getComponent() {return component;}

		/**Sets the single child component.
		This is a bound property
		@param newComponent The single child component, or <code>null</code> if this frame does not have a child component.
		@see Frame#COMPONENT_PROPERTY
		*/
		public void setComponent(final Component<?> newComponent)
		{
			if(component!=newComponent)	//if the value is really changing
			{
				final Component<?> oldComponent=component;	//get the old value
				component=newComponent;	//actually change the value
				if(oldComponent!=null)	//if there was an old component
				{
					oldComponent.setParent(null);	//tell the old component it no longer has a parent
				}
				if(component!=null)	//if there is a new component
				{
					component.setParent(this);	//tell the new component who its parent is					
				}
				firePropertyChange(COMPONENT_PROPERTY, oldComponent, newComponent);	//indicate that the value changed
			}
		}

	/**@return Whether this component has children.*/
	public boolean hasChildren() {return getComponent()!=null;}

	/**Retrieves the child component with the given ID.
	@param id The ID of the component to return.
	@return The child component with the given ID, or <code>null</code> if there is no child component with the given ID. 
	*/
	public Component<?> getComponent(final String id)
	{
		final Component<?> component=getComponent();	//get the child component, if there is one
		return (component!=null && id.equals(component.getID())) ? component : null;	//return the child component if it has the correct ID
	}

	/**@return An iterator to the single child component, if there is one.*/
	public Iterator<Component<?>> iterator()
	{
		final Component<?> component=getComponent();	//get the child component, if there is one
		return component!=null ? new ObjectIterator<Component<?>>(getComponent()) : new EmptyIterator<Component<?>>();
	}

	/**Whether this frame has been initialized.*/
	private boolean initialized=false;

	/**Sets whether the frame is visible.
	This version registers or unregisters the frame with the session as needed.
	@param newVisible <code>true</code> if the component should be visible, else <code>false</code>.
	@see Component#VISIBLE_PROPERTY
	*/
	public void setVisible(final boolean newVisible)
	{
		if(isVisible()!=newVisible)	//if the value is really changing
		{
			if(initialized)	//if we've initialized the frame
			{
				if(newVisible)	//if the frame is being shown
				{
					getSession().addFrame(this);	//add the frame to the session
				}
				else	//if the frame is being hidden
				{
					getSession().removeFrame(this);	//remove the frame from the session
				}
			}
		}
		super.setVisible(newVisible);	//set the visibility normally
	}

	/**Session, ID, model, and component constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractFrame(final GuiseSession session, final String id, final LabelModel model, final Component<?> component)
	{
		super(session, id, model);	//construct the parent class
		this.component=component;	//set the child component
		setVisible(false);	//default to not being visible
		initialized=true;	//show that we've initialized the frame
	}

	/**Determines whether the frame should be allowed to close.
	This implementation returns <code>true</code>.
	This method is called from {@link #close()}.
	@return <code>true</code> if the frame should be allowed to close.
	*/
	public boolean canClose()
	{
		return true;	//always allow the frame to be closed
	}

	/**Closes the frame.
	This method calls {@link #canClose()} and only performs closing functionality if that method returns <code>true</code>.
	This method delegates actual closing to {@link #closeImpl()}, and that method should be overridden rather than this one. 
	*/
	public final void close()
	{
		if(canClose())	//if the frame can close
		{
			closeImpl();	//actually close the frame
		}
	}
	
	/**Implementation of frame closing.*/
	protected void closeImpl()
	{
		setVisible(false);	//make the frame invisible.
	}

}
