/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework.platform.web;

import static com.globalmentor.text.xml.xhtml.XHTML.*;

import com.guiseframework.component.*;

/**
 * Strategy for rendering a label component as an XHTML <code>h1</code>, <code>h2</code>, etc. element. If a heading level corresponds to one of the XHTML
 * heading element names, that name will be used for the element; otherwise, the span element will be used. If no style ID is provided, the default style ID
 * will be used with the heading level, if given, appended.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebHeadingDepictor<C extends LabelComponent> extends WebLabelDepictor<C> {

	/** The array of XHTML heading element local names. */
	protected final static String[] HEADING_LOCAL_NAMES = new String[] { ELEMENT_H1, ELEMENT_H2, ELEMENT_H3, ELEMENT_H4, ELEMENT_H5, ELEMENT_H6 };

	/**
	 * {@inheritDoc} This version returns one of the XHTML heading element local names if the component is a {@link HeadingComponent} and a valid level is
	 * specified, otherwise the default local name is returned.
	 * @see Heading#getLevel()
	 * @see #getHeadingLocalName(int)
	 */
	@Override
	public String getLocalName() {
		final C component = getDepictedObject(); //get the component		
		if(component instanceof HeadingComponent) { //if this is a heading
			final String localName = getHeadingLocalName(((HeadingComponent)component).getLevel()); //get the heading local name for this heading level
			if(localName != null) { //if there is a local name
				return localName; //return the local name
			}
		}
		return super.getLocalName(); //return the default local name
	}

	/**
	 * Determines the local name to use for a heading based upon a heading level. This method returns one of the XHTML heading element local names if a valid
	 * level is specified.
	 * @param headingLevel The zero-based level of the heading, or {@link #NO_HEADING_LEVEL} if no level is specified.
	 * @return The appropriate XHTML heading element name if a level is specified, or <code>null</code> if there is no heading local name for the given heading
	 *         level.
	 */
	public static String getHeadingLocalName(final int headingLevel) {
		return headingLevel >= 0 && headingLevel < HEADING_LOCAL_NAMES.length ? HEADING_LOCAL_NAMES[headingLevel] : null; //if this is a valid level, retrieve the local name from the array
	}

}
