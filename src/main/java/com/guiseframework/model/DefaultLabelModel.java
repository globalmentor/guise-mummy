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

package com.guiseframework.model;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.text.Text.*;

import java.net.URI;

import com.globalmentor.java.Objects;
import com.globalmentor.net.ContentType;
import com.globalmentor.text.Text;

/**
 * A default implementation of a model for an identifier such as text and/or an icon.
 * @author Garret Wilson
 */
public class DefaultLabelModel extends AbstractModel implements LabelModel {

	/** The glyph URI, which may be a resource URI, or <code>null</code> if there is no glyph URI. */
	private URI glyphURI;

	@Override
	public URI getGlyphURI() {
		return glyphURI;
	}

	@Override
	public void setGlyphURI(final URI newGlyphURI) {
		if(!Objects.equals(glyphURI, newGlyphURI)) { //if the value is really changing
			final URI oldGlyphURI = glyphURI; //get the old value
			glyphURI = newGlyphURI; //actually change the value
			firePropertyChange(GLYPH_URI_PROPERTY, oldGlyphURI, newGlyphURI); //indicate that the value changed
		}
	}

	/** The label text, which may include a resource reference, or <code>null</code> if there is no label text. */
	private String label;

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(final String newLabelText) {
		if(!Objects.equals(label, newLabelText)) { //if the value is really changing
			final String oldLabel = label; //get the old value
			label = newLabelText; //actually change the value
			firePropertyChange(LABEL_PROPERTY, oldLabel, newLabelText); //indicate that the value changed
		}
	}

	/** The content type of the label text. */
	private ContentType labelContentType = Text.PLAIN_CONTENT_TYPE;

	@Override
	public ContentType getLabelContentType() {
		return labelContentType;
	}

	@Override
	public void setLabelContentType(final ContentType newLabelTextContentType) {
		checkInstance(newLabelTextContentType, "Content type cannot be null.");
		if(labelContentType != newLabelTextContentType) { //if the value is really changing
			final ContentType oldLabelTextContentType = labelContentType; //get the old value
			if(!isText(newLabelTextContentType)) { //if the new content type is not a text content type
				throw new IllegalArgumentException("Content type " + newLabelTextContentType + " is not a text content type.");
			}
			labelContentType = newLabelTextContentType; //actually change the value
			firePropertyChange(LABEL_CONTENT_TYPE_PROPERTY, oldLabelTextContentType, newLabelTextContentType); //indicate that the value changed
		}
	}

	/** Default constructor. */
	public DefaultLabelModel() {
		this(null); //construct the class with no label
	}

	/**
	 * Label constructor.
	 * @param label The text of the label, or <code>null</code> if there should be no label.
	 */
	public DefaultLabelModel(final String label) {
		this(label, null); //construct the label model with no glyph
	}

	/**
	 * Label and glyph URI constructor.
	 * @param label The text of the label, or <code>null</code> if there should be no label.
	 * @param glyphURI The glyph URI, which may be a resource URI, or <code>null</code> if there is no glyph URI.
	 */
	public DefaultLabelModel(final String label, final URI glyphURI) {
		this.label = label; //save the label
		this.glyphURI = glyphURI; //save the glyph URI
	}

	@Override
	public String toString() {
		final String label = getLabel(); //get the label, if any
		return label != null ? getClass().getName() + ": " + label : super.toString(); //return the class and label, or the default string if there is no label
	}
}
