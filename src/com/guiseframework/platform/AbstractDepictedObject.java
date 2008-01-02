package com.guiseframework.platform;

import java.io.IOException;


import com.globalmentor.java.LongUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.transfer.Transferable;
import com.guiseframework.event.EventListenerManager;
import com.guiseframework.event.GuiseBoundPropertyObject;

/**Abstract implementation of an object that can be depicted on some platform.
@author Garret Wilson
*/
public abstract class AbstractDepictedObject extends GuiseBoundPropertyObject implements DepictedObject
{

	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}

	/**The object depiction identifier*/
	private final long depictID;

		/**@return The object depiction identifier.*/
		public long getDepictID() {return depictID;}

	/**The depictor for this object.*/
	private final Depictor<? extends DepictedObject> depictor;

		/**@return The depictor for this object.*/
		public Depictor<? extends DepictedObject> getDepictor() {return depictor;}

		/**Processes an event from the platform.
		This method delegates to the currently installed depictor.
		@param event The event to be processed.
		@exception IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
		@see #getDepictor()
		@see Depictor#processEvent(PlatformEvent)
		*/
		public void processEvent(final PlatformEvent event)
		{
			getDepictor().processEvent(event);	//ask the depictor to process the event
		}

		/**Updates the depiction of the object.
		The depiction will be marked as updated.
		This method delegates to the currently installed depictor.
		@exception IOException if there is an error updating the depiction.
		@see #getDepictor()
		@see Depictor#depict()
		*/
		public void depict() throws IOException
		{
			getDepictor().depict();	//ask the depictor to depict the object
		}

	/**Default constructor.
	@exception IllegalStateException if no depictor is registered for this object type.
	*/
	public AbstractDepictedObject()
	{
		final GuiseSession session=getSession();	//get the Guise session
		final Platform platform=session.getPlatform();	//get the Guise platform
		this.depictID=platform.generateDepictID();	//ask the platform to generate a new depict ID
		this.depictor=platform.getDepictor(this);	//ask the platform for a depictor for the object
		if(this.depictor==null)	//if no depictor is registered for this object
		{
			throw new IllegalStateException("No depictor registered for class "+getClass());
		}
		notifyDepictorInstalled(depictor);	//tell the the depictor it has been installed
		platform.registerDepictedObject(this);	//register this depicted object with the platform
	}

	/**Notifies a depictor that it has been installed in this object.
	@param <O> The type of depicted object expected by the depictor.
	@param depictor The depictor that has been installed.
	*/
	@SuppressWarnings("unchecked")	//at this point we have to assume that the correct type of depictor has been registered for this object
	private <O extends DepictedObject> void notifyDepictorInstalled(final Depictor<O> depictor)
	{
		depictor.installed((O)this);	//tell the depictor it has been installed		
	}

	/**Exports data from the depicted object.
	This version returns <code>null</code>.
	Each export strategy, from last to first added, will be asked to export data, until one is successful.
	@return The object to be transferred, or <code>null</code> if no data can be transferred.
	*/
	public Transferable<?> exportTransfer()
	{
		return null;	//indicate that no data could be exported
	}

	/**@return A hash code value for the object.*/
	public int hashCode()
	{
		return LongUtilities.hashCode(getDepictID());	//return the hash code of the ID
	}

	/**Indicates whether some other object is "equal to" this one.
	This implementation returns whether the object is a depicted object with the same ID.
	@param object The reference object with which to compare.
	@return <code>true</code> if this object is equivalent to the given object.
	*/
	public boolean equals(final Object object)
	{
		return object instanceof DepictedObject && getDepictID()==((DepictedObject)object).getDepictID();	//see if the other object is a depicted object with the same ID
	}

	/**@return A string representation of this depicted object.*/
	public String toString()
	{
		final StringBuilder stringBuilder=new StringBuilder(super.toString());	//create a string builder for constructing the string
		stringBuilder.append(' ').append('[').append(getDepictID()).append(']');	//append the ID
		return stringBuilder.toString();	//return the string builder
	}
}
