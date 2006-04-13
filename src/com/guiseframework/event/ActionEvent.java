package com.guiseframework.event;

import static com.garretwilson.lang.ObjectUtilities.*;
import com.garretwilson.beans.TargetedEvent;

/**An event indicating an action should take place.
@author Garret Wilson
*/
public class ActionEvent extends AbstractGuiseEvent implements TargetedEvent
{

	/**The target of the event, or <code>null</code> if the event target is not known.*/
	private final Object target;
	
		/**Returns the object to which the event applies.
		This may be a different than <dfn>source</dfn>, which is the object that generated this event instance.
		@return The target of the event.
		*/
		public Object getTarget() {return target;}

	/**Source constructor.
	The target will be set to be the same as the given source.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public ActionEvent(final Object source)
	{
		this(source, source);	//construct the class with the same target as the object
	}

	/**Source and target constructor.
	@param source The object on which the event initially occurred.
	@param target The target of the event.
	@exception NullPointerException if the given source and/or target is <code>null</code>.
	*/
	public ActionEvent(final Object source, final Object target)
	{
		super(source);	//construct the parent class
		this.target=checkInstance(target, "Event target object cannot be null.");	//save the target
	}

	/**Copy constructor that specifies a different source.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public ActionEvent(final Object source, final ActionEvent actionEvent)
	{
		this(source, actionEvent.getTarget());	//construct the class with the same target		
	}
}
