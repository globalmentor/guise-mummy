package com.garretwilson.guise.model;

import static com.garretwilson.lang.ClassUtilities.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.garretwilson.guise.session.GuiseSession;

/**Base interface for all component models.
Each model can belong to at most one model group for purposes of communication and/or mutual exclusion.
To add or remove a model to a group, use the group's {@link ModelGroup#add()} and {@link ModelGroup#remove()} method, respectively. 
@author Garret Wilson
@see com.garretwilson.guise.model.ModelGroup
*/
public interface Model<M extends Model<M>>
{

	/**The group bound property.*/
	public final static String GROUP_PROPERTY=getPropertyName(Model.class, "group");

	/**@return The Guise session that owns this model.*/
	public GuiseSession<?> getSession();

	/**@return The group to which this model belongs, or <code>null</code> if this model does not belong to a group.*/
	public ModelGroup<M> getGroup();

	/**Sets the group to which this model belongs.
	This method is managed by model groups, and should usually never be called my other classes.
	In order to guard against inadvertent incorrect use, the group must only be set after the model is added to the group, and only be unset after the model is removed from the group.
	If a model is given the same group it already has, no action occurs.
	This is a bound property.
	@param newGroup The group to which this model belongs.
	@exception IllegalStateException if a group is provided and this model already has a parent.
	@exception IllegalStateException if no group is provided and this model's old group still recognizes this model as its member.
	@exception IllegalArgumentException if a group is provided and the given model does not already recognize this model as its member.
	@see #GROUP_PROPERTY
	*/
	public void setGroup(final ModelGroup<M> newGroup);

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
