package com.guiseframework.platform;

import java.io.IOException;

import com.guiseframework.GuiseSession;

/**A strategy for depicting objects on some platform.
@param <O> The type of object being depicted.
@author Garret Wilson
*/
public interface Depictor<O extends DepictedObject>
{
	/**The property indicating general depicted object changes.*/
	public final static String GENERAL_PROPERTY="generalProperty";

	/**@return The Guise session that owns this object.*/
	public GuiseSession getSession();

	/**@return The platform on which this depictor is depicting ojects.*/
	public Platform getPlatform();

	/**Retrieves information and functionality related to the current depiction on the platform.
	This method delegates to {@link Platform#getDepictContext()}.
	@return A context for the current depiction.
	@exception IllegalStateException if no depict context can be returned in the current depiction state.
	*/
	public DepictContext getDepictContext();

	/**@return The object being depicted, or <code>null</code> if this depictor is not installed in a depicted object.*/
	public O getDepictedObject();

	/**@return Whether this depictor's representation of the depicted object is up to date.*/
	public boolean isDepicted();

	/**Changes the depictor's updated status.
	If the new depicted status is <code>true</code>, all modified properties are removed.
	If the new depicted status is <code>false</code>, the {@link Depictor#GENERAL_PROPERTY} property is set as modified.
	@param newDepicted Whether this depictor's representation of the depicted object is up to date.
	*/
	public void setDepicted(final boolean newDepicted);
		
	/**Called when the depictor is installed in a depicted object.
	@param depictedObject The depictedObject into which this depictor is being installed.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	@exception IllegalStateException if this depictor is already installed in a depicted object.
	*/
	public void installed(final O depictedObject);

	/**Called when the depictor is uninstalled from a depicted object.
	@param depictedObject The depicted object from which this depictor is being uninstalled.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	@exception IllegalStateException if this depictor is not installed in a depicted object.
	*/
	public void uninstalled(final O depictedObject);

	/**Processes an event from the platform.
	@param event The event to be processed.
	@exception IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	*/
	public void processEvent(final PlatformEvent event);

	/**Updates the depiction of the object.
	The depiction will be marked as updated.
	@exception IOException if there is an error updating the depiction.
	*/
	public void depict() throws IOException;

}
