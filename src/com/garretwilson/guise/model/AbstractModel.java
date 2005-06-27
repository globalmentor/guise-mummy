package com.garretwilson.guise.model;

import com.garretwilson.beans.BoundPropertyObject;
import com.garretwilson.event.EventListenerManager;
import com.garretwilson.guise.session.GuiseSession;
import com.garretwilson.util.Debug;

import static com.garretwilson.lang.ObjectUtilities.*;

/**A base abstract class implementing helpful functionality for models.
@author Garret Wilson
*/
public class AbstractModel<M extends Model<M>> extends BoundPropertyObject implements Model<M>
{
	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}

	/**The Guise session that owns this model.*/
	private final GuiseSession<?> session;

		/**@return The Guise session that owns this model.*/
		public GuiseSession<?> getSession() {return session;}

	/**The group to which this model belongs, or <code>null</code> if this model does not belong to a group.*/
	private ModelGroup<M> group=null;

		/**@return The group to which this model belongs, or <code>null</code> if this model does not belong to a group.*/
		public ModelGroup<M> getGroup() {return group;}

		/**Sets the group to which this model belongs.
		This method is managed by model groups, and should usually never be called my other classes.
		In order to guard against inadvertent incorrect use, the group must only be set after the model is added to the group, and only be unset after the model is removed from the group.
		If a model is given the same group it already has, no action occurs.
		This is a bound property.
		@param newGroup The group to which this model belongs.
		@exception IllegalStateException if a group is provided and this model already has a parent.
		@exception IllegalStateException if no group is provided and this model's old group still recognizes this model as its member.
		@exception IllegalArgumentException if a group is provided and the given model does not already recognize this model as its member.
		@see Model#GROUP_PROPERTY
		*/
		public void setGroup(final ModelGroup<M> newGroup)
		{
			if(group!=newGroup)	//if the value is really changing
			{
				final ModelGroup<M> oldGroup=group;	//get the old value
				if(newGroup!=null)	//if a group is provided
				{
					if(oldGroup!=null)	//if we already have a group
					{
						throw new IllegalStateException("Model "+this+" already has group: "+oldGroup);
					}
					if(!newGroup.contains((M)this))	//if the group does not really contain this model TODO why do we need this?
					{
						throw new IllegalArgumentException("Provided group "+newGroup+" does not really contain model "+this);
					}
				}
				else	//if no parent is provided
				{
					if(oldGroup!=null && oldGroup.contains((M)this))	//if we had a group before, and that group still thinks it contains this model TODO why do we need this?
					{
						throw new IllegalStateException("Old group "+oldGroup+" still thinks it contains model "+this); 
					}
				}
				group=newGroup;	//actually change the value
				firePropertyChange(GROUP_PROPERTY, oldGroup, newGroup);	//indicate that the value changed
			}			
		}

	/**Session constructor.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractModel(final GuiseSession<?> session)
	{
		this.session=checkNull(session, "Session cannot be null");	//save the session
	}
}
