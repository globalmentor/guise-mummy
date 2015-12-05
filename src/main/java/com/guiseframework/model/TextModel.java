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

import com.globalmentor.net.ContentType;

import static com.globalmentor.java.Classes.*;

/**
 * A model for text and an associated label. This model only supports text content types, including:
 * <ul>
 * <li><code>text/*</code></li>
 * <li><code>application/xml</code></li>
 * <li><code>application/*+xml</code></li>
 * </ul>
 * <p>
 * The model defaults to a content type of <code>text/plain</code>.
 * </p>
 * @author Garret Wilson
 */
public interface TextModel extends Model {

	/** The text content type bound property. */
	public static final String TEXT_CONTENT_TYPE_PROPERTY = getPropertyName(TextModel.class, "contentType");
	/** The text bound property. */
	public static final String TEXT_PROPERTY = getPropertyName(TextModel.class, "text");

	/** @return The text, which may include a resource reference, or <code>null</code> if there is no text. */
	public String getText();

	/**
	 * Sets the text. This is a bound property.
	 * @param newText The new text, which may include a resource reference.
	 * @see #TEXT_PROPERTY
	 */
	public void setText(final String newText);

	/** @return The content type of the text. */
	public ContentType getTextContentType();

	/**
	 * Sets the content type of the text. This is a bound property.
	 * @param newTextContentType The new text content type.
	 * @throws NullPointerException if the given content type is <code>null</code>.
	 * @throws IllegalArgumentException if the given content type is not a text content type.
	 * @see #TEXT_CONTENT_TYPE_PROPERTY
	 */
	public void setTextContentType(final ContentType newTextContentType);
}
