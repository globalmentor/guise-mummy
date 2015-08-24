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

import static com.globalmentor.w3c.spec.HTML.*;

import javax.annotation.Nonnull;

import com.globalmentor.text.xml.xhtml.XHTML;
import com.guiseframework.component.*;
import com.guiseframework.component.SectionComponent.SectionType;

/**
 * Strategy for rendering a panel as an XHTML <code>&lt;div&gt;</code> element.
 * <p>
 * This implementation recognizes panels that are also {@link SectionComponent}s, and uses the appropriate XHTML element for the
 * {@link SectionComponent#getSectionType()} indicated. A utility method is also provided for other depictors wishing to determine the appropriate XHTML element
 * for a {@link SectionType}.
 * </p>
 * <p>
 * Changes to {@link Component#LABEL_PROPERTY} are ignored.
 * </p>
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebPanelDepictor<C extends Panel> extends WebLayoutComponentDepictor<C> {

	/** Default constructor. */
	public WebPanelDepictor() {
		getIgnoredProperties().add(Panel.LABEL_PROPERTY); //ignore Panel.label by default, because panels are large objects with many children but most do not show labels
	}

	/** {@inheritDoc} This version returns special section elements if the component is a {@link SectionComponent} with a non-<code>null</code> section type. */
	@Override
	public String getLocalName() {
		final C component = getDepictedObject(); //get the component
		if(component instanceof SectionComponent) { //if this is a section component
			final SectionType sectionType = ((SectionComponent)component).getSectionType();
			if(sectionType != null) { //if a specific type is indicated
				return getLocalName(sectionType); //return the local name for the section type
			}
		}
		return super.getLocalName(); //return the default local name
	}

	/**
	 * Determines the appropriate XHTML element local name (e.g. <code>&lt;section&gt;</code>) for the given section type. If there is no specific XHTML element
	 * appropriate for the given section type, {@value XHTML#ELEMENT_DIV} will be returned.
	 * @param sectionType The type of section indicated.
	 * @return The XHTML element local name for the indicated section type.
	 * @throws NullPointerException if the given section type is <code>null</code>.
	 */
	@Nonnull
	public static String getLocalName(@Nonnull final SectionType sectionType) {
		switch(sectionType) {
			case ARTICLE:
				return ELEMENT_ARTICLE;
			case ASIDE:
				return ELEMENT_ASIDE;
			case FOOTER:
				return ELEMENT_FOOTER;
			case HEADER:
				return ELEMENT_HEADER;
			case NAVIGATION:
				return ELEMENT_NAV;
			case SECTION:
				return ELEMENT_SECTION;
			default:
				return ELEMENT_DIV;
		}
	}

}
