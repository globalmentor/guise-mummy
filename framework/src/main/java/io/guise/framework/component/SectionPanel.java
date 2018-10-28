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

package io.guise.framework.component;

import io.guise.framework.component.layout.*;

/**
 * A panel that demarcates a semantically significant area of the a parent component with arranged child components.
 * <p>
 * This components defaults to a {@link SectionType#SECTION} section type.
 * </p>
 * @author Garret Wilson
 */
public class SectionPanel extends ArrangePanel implements SectionComponent {

	private SectionType sectionType = SectionType.SECTION;

	@Override
	public SectionType getSectionType() {
		return sectionType;
	}

	@Override
	public void setSectionType(final SectionType newSectionType) {
		if(sectionType != newSectionType) { //if the value is really changing
			final SectionType oldType = sectionType; //get the old value
			sectionType = newSectionType; //actually change the value
			firePropertyChange(SECTION_TYPE_PROPERTY, oldType, newSectionType); //indicate that the value changed
		}
	}

	/** Default constructor with a default vertical flow layout and a default {@link SectionType#SECTION} section type. */
	public SectionPanel() {
		this(SectionType.SECTION);
	}

	/**
	 * Layout constructor with a default {@link SectionType#SECTION} section type.
	 * @param layout The layout definition for the container.
	 * @throws NullPointerException if the given layout is <code>null</code>.
	 */
	public SectionPanel(final Layout<?> layout) {
		this(layout, SectionType.SECTION);
	}

	/**
	 * Section type constructor with a default vertical flow layout.
	 * @param newSectionType The type of section, or <code>null</code> if there is no specific type specified.
	 */
	public SectionPanel(final SectionType newSectionType) {
		this(new FlowLayout(Flow.PAGE), newSectionType); //default to flowing vertically
	}

	/**
	 * Layout and section type constructor.
	 * @param layout The layout definition for the container.
	 * @param newSectionType The type of section, or <code>null</code> if there is no specific type specified.
	 * @throws NullPointerException if the given layout is <code>null</code>.
	 */
	public SectionPanel(final Layout<?> layout, final SectionType newSectionType) {
		super(layout); //construct the parent class
		this.sectionType = newSectionType;
	}

}
