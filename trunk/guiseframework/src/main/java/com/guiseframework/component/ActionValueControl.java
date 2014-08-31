/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.component;

import static com.globalmentor.java.Classes.*;

import java.net.URI;

/**
 * An action control that also contains a value in its model.
 * @author Garret Wilson
 * @param <V> The type of value the action represents.
 */
public interface ActionValueControl<V> extends ActionControl, ValueControl<V> {

	/** The bound property for an icon associated with a value. */
	public static final String VALUE_GLYPH_URI_PROPERTY = getPropertyName(SelectActionControl.class, "valueGlyphURI");

	/**
	 * Retrieves the icon associated with a given value.
	 * @param value The value for which an associated icon should be returned, or <code>null</code> to retrieve the icon associated with the <code>null</code>
	 *          value.
	 * @return The value icon URI, which may be a resource URI, or <code>null</code> if the value has no associated icon URI.
	 */
	public URI getValueGlyphURI(final V value);

	/**
	 * Sets the URI of the icon associated with a value. This method fires a property change event for the changed icon if its value changes.
	 * @param value The value with which the icon should be associated, or <code>null</code> if the icon should be associated with the <code>null</code> value.
	 * @param newValueIcon The new URI of the value icon, which may be a resource URI.
	 * @see #VALUE_GLYPH_URI_PROPERTY
	 */
	public void setValueGlyphURI(final V value, final URI newValueIcon);

}
