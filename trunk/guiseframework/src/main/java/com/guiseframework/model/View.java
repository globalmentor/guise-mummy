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

import java.net.URI;

import com.globalmentor.util.StringTemplate;

import static com.guiseframework.Resources.*;

/**
 * Represents a view or aspect of data.
 * @author Garret Wilson
 */
public enum View {
	/** The view in which a list of items is shown. */
	LIST,
	/** The view in which the data is shown in a graph structure. */
	//TODO implement later	GRAPH,
	/** The view in which a sequence of items is shown, perhaps as a card deck. */
	SEQUENCE,
	/** The view in which any source data used to generate the data is shown. */
	//TODO implement later	SOURCE;
	/** The view in which the data is shown as a brief overview. */
	//TODO implement later	SUMMARY,
	/** The view in which the data is shown as a series of small images. */
	THUMBNAILS,
	/** The view in which the data is shown in a tree structure. */
	TREE,
	/** The view in which the data is shown as it would be in its final form. */
	WYSIWYG;

	/** The resource key template for each view label. */
	private final static StringTemplate LABEL_RESOURCE_KEY_TEMPLATE = new StringTemplate("theme.view.", StringTemplate.STRING_PARAMETER, ".label");
	/** The resource key template for each view glyph. */
	private final static StringTemplate GLYPH_RESOURCE_KEY_TEMPLATE = new StringTemplate("theme.view.", StringTemplate.STRING_PARAMETER, ".glyph");

	/** @return A resource reference representing a label for no view. */
	public static String getNoLabel() {
		return createStringResourceReference(LABEL_RESOURCE_KEY_TEMPLATE.apply("")); //get the label representing no value
	}

	/** @return The resource reference for the view label. */
	public String getLabel() {
		return createStringResourceReference(LABEL_RESOURCE_KEY_TEMPLATE.apply(getResourceKeyName(this))); //create a resource reference using the resource key name of this enum value
	}

	/** @return A resource reference representing a glyph for no view. */
	public static URI getNoGlyph() {
		return createURIResourceReference(GLYPH_RESOURCE_KEY_TEMPLATE.apply("")); //get the glyph representing no value
	}

	/** @return The resource reference for the view glyph. */
	public URI getGlyph() {
		return createURIResourceReference(GLYPH_RESOURCE_KEY_TEMPLATE.apply(getResourceKeyName(this))); //create a resource reference using the resource key name of this enum value
	}

	/**
	 * Returns a string representation of the view. This implementation delegates to {@link #getLabel()}.
	 * @return A string representation of the object.
	 */
	public String toString() {
		return getLabel();
	}
}
