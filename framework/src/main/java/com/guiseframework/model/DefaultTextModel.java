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

import static java.util.Objects.*;

import com.globalmentor.java.Objects;
import com.globalmentor.net.ContentType;
import com.globalmentor.text.Text;

import static com.globalmentor.text.Text.*;

/**
 * The default implementation of a model for text and an associated label.
 * @author Garret Wilson
 */
public class DefaultTextModel extends AbstractModel implements TextModel {

	/** The text, which may include a resource reference, or <code>null</code> if there is no text. */
	private String text;

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(final String newText) {
		if(!Objects.equals(text, newText)) { //if the value is really changing
			final String oldText = text; //get the old value
			text = newText; //actually change the value
			firePropertyChange(TEXT_PROPERTY, oldText, newText); //indicate that the value changed
		}
	}

	/** The content type of the text. */
	private ContentType textContentType;

	@Override
	public ContentType getTextContentType() {
		return textContentType;
	}

	@Override
	public void setTextContentType(final ContentType newTextContentType) {
		requireNonNull(newTextContentType, "Content type cannot be null.");
		if(textContentType != newTextContentType) { //if the value is really changing
			final ContentType oldTextContentType = textContentType; //get the old value
			if(!isText(newTextContentType)) { //if the new content type is not a text content type
				throw new IllegalArgumentException("Content type " + newTextContentType + " is not a text content type.");
			}
			textContentType = newTextContentType; //actually change the value
			firePropertyChange(TEXT_CONTENT_TYPE_PROPERTY, oldTextContentType, newTextContentType); //indicate that the value changed
		}
	}

	/** Default constructor. */
	public DefaultTextModel() {
		this(null); //construct the class with no text
	}

	/**
	 * Text constructor with a default {@link Text#PLAIN_CONTENT_TYPE} content type.
	 * @param text The text, which may include a resource reference, or <code>null</code> if there is no text.
	 */
	public DefaultTextModel(final String text) {
		this(text, Text.PLAIN_CONTENT_TYPE); //construct the class with a plain text content type
	}

	/**
	 * Text and content type constructor
	 * @param text The text, which may include a resource reference, or <code>null</code> if there is no text.
	 * @param textContentType The content type of the text.
	 * @throws NullPointerException if the given content type is <code>null</code>.
	 * @throws IllegalArgumentException if the given content type is not a text content type.
	 */
	public DefaultTextModel(final String text, final ContentType textContentType) {
		this.text = text; //save the text
		if(!isText(requireNonNull(textContentType, "Content type cannot be null."))) { //if the content type is not a text content type
			throw new IllegalArgumentException("Content type " + textContentType + " is not a text content type.");
		}
		this.textContentType = textContentType; //save the text content type
	}
}
