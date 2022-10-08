/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.model;

import java.net.URI;

import com.globalmentor.net.MediaType;

import static com.globalmentor.java.Classes.*;

/**
 * A model for an identifier such as text and/or an icon.
 * @author Garret Wilson
 */
public interface LabelModel extends Model {

	/** The icon bound property. */
	public static final String GLYPH_URI_PROPERTY = getPropertyName(LabelModel.class, "glyphURI");
	/** The label bound property. */
	public static final String LABEL_PROPERTY = getPropertyName(LabelModel.class, "label");
	/** The label content type bound property. */
	public static final String LABEL_CONTENT_TYPE_PROPERTY = getPropertyName(LabelModel.class, "labelContentType");

	/** @return The glyph URI, which may be a resource URI, or <code>null</code> if there is no glyph URI. */
	public URI getGlyphURI();

	/**
	 * Sets the URI of the icon. This is a bound property.
	 * @param newIcon The new URI of the icon, which may be a resource URI.
	 * @see #GLYPH_URI_PROPERTY
	 */
	public void setGlyphURI(final URI newIcon);

	/** @return The label text, which may include a resource reference, or <code>null</code> if there is no label text. */
	public String getLabel();

	/**
	 * Sets the text of the label. This is a bound property.
	 * @param newLabelText The new text of the label, which may include a resource reference.
	 * @see #LABEL_PROPERTY
	 */
	public void setLabel(final String newLabelText);

	/** @return The content type of the label text. */
	public MediaType getLabelContentType();

	/**
	 * Sets the content type of the label text. This is a bound property.
	 * @param newLabelTextContentType The new label text content type.
	 * @throws NullPointerException if the given content type is <code>null</code>.
	 * @throws IllegalArgumentException if the given content type is not a text content type.
	 * @see #LABEL_CONTENT_TYPE_PROPERTY
	 */
	public void setLabelContentType(final MediaType newLabelTextContentType);

}
