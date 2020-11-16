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

import static java.util.Objects.*;

import com.globalmentor.net.ContentType;
import com.globalmentor.text.Text;

import io.guise.framework.component.layout.*;
import io.guise.framework.model.*;

/**
 * A box containing text.
 * <p>
 * This component may have child components, each bound to a specific ID in the text using {@link ReferenceConstraints}. When the text is rendered, XML elements
 * with IDs referencing child components will be replaced with representations of those child components. Child element ID reference replacement can only occur
 * if the text has an XML-based content type (such as XHTML).
 * </p>
 * <p>
 * This component only supports text content types, including:
 * </p>
 * <ul>
 * <li><code>text/*</code></li>
 * <li><code>application/xml</code></li>
 * <li><code>application/*+xml</code></li>
 * </ul>
 * <p>
 * The component defaults to a content type of <code>text/plain</code>.
 * </p>
 * <p>
 * This component by default indicates no section type, thereby indicating that its text does not indicate a semantically significant area.
 * </p>
 * @author Garret Wilson
 * @see ReferenceLayout
 */
public class TextBox extends AbstractContainer implements TextModel, SectionComponent {

	/** The text model used by this component. */
	private final TextModel textModel;

	/** @return The text model used by this component. */
	protected TextModel getTextModel() {
		return textModel;
	}

	@Override
	public ReferenceLayout getLayout() {
		return (ReferenceLayout)super.getLayout(); //a text component can only have a reference layout
	}

	/** Default to no section type. */
	private SectionType sectionType = null;

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

	/** Default constructor with a default text model. */
	public TextBox() {
		this((String)null); //construct the class with no text
	}

	/**
	 * Text constructor with a default {@link Text#PLAIN_MEDIA_TYPE} content type.
	 * @param text The text, which may include a resource reference, or <code>null</code> if there is no text.
	 */
	public TextBox(final String text) {
		this(text, Text.PLAIN_MEDIA_TYPE); //construct the class with a plain text content type
	}

	/**
	 * Text and content type constructor
	 * @param text The text, which may include a resource reference, or <code>null</code> if there is no text.
	 * @param textContentType The content type of the text.
	 * @throws NullPointerException if the given content type is <code>null</code>.
	 * @throws IllegalArgumentException if the given content type is not a text content type.
	 */
	public TextBox(final String text, final ContentType textContentType) {
		this(new DefaultTextModel(text, textContentType)); //construct the class with a default text model using the given values
	}

	/**
	 * Text model constructor.
	 * @param textModel The component text model.
	 * @throws NullPointerException if the given text model is <code>null</code>.
	 */
	public TextBox(final TextModel textModel) {
		super(new ReferenceLayout()); //construct the parent class
		this.textModel = requireNonNull(textModel, "Text model cannot be null."); //save the text model
		this.textModel.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the text model
		this.textModel.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the text model
	}

	//TextModel delegates 

	@Override
	public String getText() {
		return getTextModel().getText();
	}

	@Override
	public void setText(final String newText) {
		getTextModel().setText(newText);
	}

	@Override
	public ContentType getTextContentType() {
		return getTextModel().getTextContentType();
	}

	@Override
	public void setTextContentType(final ContentType newTextContentType) {
		getTextModel().setTextContentType(newTextContentType);
	}

}
