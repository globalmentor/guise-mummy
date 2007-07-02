package com.guiseframework.platform;

import java.io.IOException;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.transfer.Transferable;

/**An object that can be depicted on some platform.
@author Garret Wilson
*/
public interface DepictedObject
{

	/**@return The Guise session that owns this object.*/
	public GuiseSession getSession();

	/**@return The object depiction identifier.*/
	public long getDepictID();

	/**@return The depictor for this object.*/
	public Depictor<? extends DepictedObject> getDepictor();

	/**Exports data from the depicted object.
	Each export strategy, from last to first added, will be asked to export data, until one is successful.
	@return The object to be transferred, or <code>null</code> if no data can be transferred.
	*/
	public Transferable<?> exportTransfer();

	/**Processes an event from the platform.
	This method delegates to the currently installed depictor.
	@param event The event to be processed.
	@exception IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	@see #getDepictor()
	@see Depictor#processEvent(PlatformEvent)
	*/
	public void processEvent(final PlatformEvent event);

	/**Updates the depiction of the object.
	The depiction will be marked as updated.
	This method delegates to the currently installed depictor.
	@exception IOException if there is an error updating the depiction.
	@see #getDepictor()
	@see Depictor#depict()
	*/
	public void depict() throws IOException;

}
