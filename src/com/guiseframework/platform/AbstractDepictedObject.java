package com.guiseframework.platform;

import com.guiseframework.GuiseSession;
import com.guiseframework.event.GuiseBoundPropertyObject;

/**Abstract implementation of an object that can be depicted on some platform.
@author Garret Wilson
*/
public abstract class AbstractDepictedObject extends GuiseBoundPropertyObject implements DepictedObject
{

	/**The object identifier*/
	private final long id;

		/**@return The object identifier.*/
		public long getID() {return id;}

	/**The depictor for this object.*/
	private final Depictor<? extends DepictedObject> depictor;

		/**@return The depictor for this object.*/
		public Depictor<? extends DepictedObject> getDepictor() {return depictor;}

	/**Default constructor.
	@exception IllegalStateException if no construer is registered for this object type.
	*/
	public AbstractDepictedObject()
	{
		final GuiseSession session=getSession();	//get the Guise session
		final GuisePlatform platform=session.getPlatform();	//get the Guise platform
		this.id=platform.generateDepictID();	//ask the platform to generate a new depict ID
		this.depictor=platform.getDepictor(this);	//ask the platform for a depictor for the object
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
}
