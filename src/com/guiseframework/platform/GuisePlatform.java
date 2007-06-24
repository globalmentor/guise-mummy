package com.guiseframework.platform;

import java.util.Queue;

/**The platform on which Guise objects are being depicted.
@author Garret Wilson
*/
public interface GuisePlatform
{

	/**Generates a new depict ID unique to this session platform.
	@return A new depict ID unique to this session platform.
	*/
	public long generateDepictID();

	/**Determines the depictor appropriate for the given depicted object.
	A depictor class is located by individually looking up the depicted object class hiearchy for registered depictor classes.
	@param <O> The type of depicted object.
	@param depictedObject The depicted object for which a depictor should be returned.
	@return A depictor to depict the given component, or <code>null</code> if no depictor is registered.
	@exception IllegalStateException if the registered depictor could not be instantiated for some reason.
	*/
	public <O extends DepictedObject> Depictor<? super O> getDepictor(final O depictedObject);

	/**Registers a depicted object so that it can interact with the platform.
	@param depictedObject The depicted object to register.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	*/
	public void registerDepictedObject(final DepictedObject depictedObject);

	/**Unregisters a depicted object so that no longer interacts with the platform.
	@param depictedObject The depicted object to unregister.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	*/
	public void unregisterDepictedObject(final DepictedObject depictedObject);

	/**Retrieves a depicted object that has been registered with the platform by the ID of the depicted object.
	@param depictedObjectID The ID of the depicted object to retrieve.
	@return The registered depicted object with the given ID, or <code>null</code> if there is no depicted object registered with this platform with the given ID.
	*/
	public DepictedObject getDepictedObject(final long depictedObjectID);

	/**@return The thread-safe queue of events to be delivered to the platform.*/
	public Queue<PlatformEvent> getSendEventQueue();

}
