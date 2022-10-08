/*
 * Copyright © 2012 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.guise.framework.component;

import static com.globalmentor.java.Classes.*;

import com.globalmentor.lex.Identifier;

/**
 * A component that potentially demarcates a semantically significant area of the a parent component. The section component indicates the type of section it
 * demarcates with {@link #getSectionType()}. If this method returns <code>null</code>, it indicates that the component does not wish to demarcate a
 * semantically significant area.
 * @author Garret Wilson
 */
public interface SectionComponent extends Component {

	/** The section type bound property. */
	public static final String SECTION_TYPE_PROPERTY = getPropertyName(SectionComponent.class, "sectionType");

	/** The type of section. */
	public enum SectionType implements Identifier {
		/** The main section of a self-contained article appropriate for syndication. */
		ARTICLE,
		/** Content tangentially related to the surrounding content. */
		ASIDE,
		/** A footer of a larger section. */
		FOOTER,
		/** A header of a larger section. */
		HEADER,
		/** A separate text area for navigation. */
		NAVIGATION,
		/** A section of a larger piece, such as an article. */
		SECTION
	};

	/** @return The type of section, or <code>null</code> if there is no specific section type specified. */
	public SectionType getSectionType();

	/**
	 * Sets the type of section. This is a bound property.
	 * @param newSectionType The new type of section, or <code>null</code> if there is no specific type specified.
	 * @see #SECTION_TYPE_PROPERTY
	 */
	public void setSectionType(final SectionType newSectionType);

}
