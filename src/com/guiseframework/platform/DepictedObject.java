package com.guiseframework.platform;

import com.guiseframework.GuiseSession;

/**An object that can be depicted on some platform.
@author Garret Wilson
*/
public interface DepictedObject
{

	/**@return The Guise session that owns this object.*/
	public GuiseSession getSession();

	/**@return The object identifier.*/
	public long getID();

	/**@return The depictor for this object.*/
	public Depictor<? extends DepictedObject> getDepictor();

}
