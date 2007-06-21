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
		platform.registerDepictedObject(this);	//register this depicted object with the platform
	}
}
