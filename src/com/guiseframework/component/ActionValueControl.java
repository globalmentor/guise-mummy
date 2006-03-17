package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.net.URI;

/**An action control that also contains a value in its model.
@author Garret Wilson
@param <V> The type of value the action represents.
*/
public interface ActionValueControl<V, C extends ActionValueControl<V, C>> extends ActionControl<C>, ValueControl<V, C>
{

	/**The bound property for an icon associated with a value.*/
	public final static String VALUE_ICON_PROPERTY=getPropertyName(SelectActionControl.class, "valueIcon");
	/**The bound property for an icon resource key associated with a value.*/
	public final static String VALUE_ICON_RESOURCE_KEY_PROPERTY=getPropertyName(SelectActionControl.class, "valueIconResourceKey");

	/**Retrieves the icon associated with a given value.
	@param value The value for which an associated icon should be returned, or <code>null</code> to retrieve the icon associated with the <code>null</code> value.
	@return The value icon URI, or <code>null</code> if the value has no associated icon URI.
	*/
	public URI getValueIcon(final V value);

	/**Sets the URI of the icon associated with a value.
	This method fires a property change event for the changed icon if its value changes.
	@param value The value with which the icon should be associated, or <code>null</code> if the icon should be associated with the <code>null</code> value.
	@param newValueIcon The new URI of the value icon.
	@see #VALUE_ICON_PROPERTY
	*/
	public void setValueIcon(final V value, final URI newValueIcon);

	/**Retrieves the icon resource key associated with a given value.
	@param value The value for which an associated icon resource key should be returned, or <code>null</code> to retrieve the icon resource key associated with the <code>null</code> value.
	@return The value icon resource key, or <code>null</code> if the value has no associated icon resource.
	*/
	public String getValueIconResourceKey(final V value);

	/**Sets the resource key of the icon associated with a value.
	This method fires a property change event for the changed icon resource key if its value changes.
	@param value The value with which the icon resource key should be associated, or <code>null</code> if the icon resource key should be associated with the <code>null</code> value.
	@param newValueIconResourceKey The new value icon resource key.
	@see #VALUE_ICON_RESOURCE_KEY_PROPERTY
	*/
	public void setValueIconResourceKey(final V value, final String newValueIconResourceKey);
}
