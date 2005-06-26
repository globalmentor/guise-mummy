package com.garretwilson.guise.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.garretwilson.guise.session.GuiseSession;

/**Base interface for all component models.
@author Garret Wilson
*/
public interface Model
{

	/**@return The Guise session that owns this model.*/
	public GuiseSession<?> getSession();

	/**Adds a property change listener to the listener list.
		The listener is registered for all properties.
		<p>If the listener is <code>null</code>, no exception is thrown and no action
		is performed.</p>
	@param listener The <code>PropertyChangeListener</code> to be added.
	@see PropertyChangeEvent
	*/
	public void addPropertyChangeListener(final PropertyChangeListener listener);

	/**Remove a property change listener from the listener list.
		This removes a <code>PropertyChangeListener</code> that was registered for
		all properties.
		<p>If the listener is <code>null</code>, no exception is thrown and no action
		is performed.</p>
	@param listener The <code>PropertyChangeListener</code> to be removed.
	*/
	public void removePropertyChangeListener(final PropertyChangeListener listener);

	/**Add a property change listener for a specific property.
		The listener will be invoked only when a call to
		<code>firePropertyChange()</code> names that specific property.
		<p>If the listener is <code>null</code>, no exception is thrown and no action
		is performed.</p>
	@param propertyName The name of the property to listen on.
	@param listener The <code>PropertyChangeListener</code> to be added.
	*/
	public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener);
	
	/**Remove a property change listener for a specific property.
		<p>If the listener is <code>null</code>, no exception is thrown and no
		action is performed.</p>
	@param propertyName The name of the property that was listened on.
	@param listener The <code>PropertyChangeListener</code> to be removed
	*/
	public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener);
}
