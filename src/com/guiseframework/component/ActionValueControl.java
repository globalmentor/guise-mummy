package com.guiseframework.component;

import static com.globalmentor.java.Classes.*;

import java.net.URI;

/**An action control that also contains a value in its model.
@author Garret Wilson
@param <V> The type of value the action represents.
*/
public interface ActionValueControl<V> extends ActionControl, ValueControl<V>
{

	/**The bound property for an icon associated with a value.*/
	public final static String VALUE_GLYPH_URI_PROPERTY=getPropertyName(SelectActionControl.class, "valueGlyphURI");

	/**Retrieves the icon associated with a given value.
	@param value The value for which an associated icon should be returned, or <code>null</code> to retrieve the icon associated with the <code>null</code> value.
	@return The value icon URI, which may be a resource URI, or <code>null</code> if the value has no associated icon URI.
	*/
	public URI getValueGlyphURI(final V value);

	/**Sets the URI of the icon associated with a value.
	This method fires a property change event for the changed icon if its value changes.
	@param value The value with which the icon should be associated, or <code>null</code> if the icon should be associated with the <code>null</code> value.
	@param newValueIcon The new URI of the value icon, which may be a resource URI.
	@see #VALUE_GLYPH_URI_PROPERTY
	*/
	public void setValueGlyphURI(final V value, final URI newValueIcon);

}
