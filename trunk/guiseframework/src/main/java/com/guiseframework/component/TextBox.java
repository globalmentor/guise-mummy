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

package com.guiseframework.component;

import static com.globalmentor.java.Objects.*;

import com.globalmentor.net.ContentType;
import com.guiseframework.component.layout.*;
import com.guiseframework.model.*;

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
public class TextBox extends AbstractContainer implements TextModel, SectionComponent
{

	/** The text model used by this component. */
	private final TextModel textModel;

	/** @return The text model used by this component. */
	protected TextModel getTextModel()
	{
		return textModel;
	}

	/** @return The layout definition for the text. */
	public ReferenceLayout getLayout()
	{
		return (ReferenceLayout)super.getLayout(); //a text component can only have a reference layout
	}

	/** Default to no section type. */
	private SectionType sectionType = null;

	@Override
	public SectionType getSectionType()
	{
		return sectionType;
	}

	@Override
	public void setSectionType(final SectionType newSectionType)
	{
		if(sectionType != newSectionType) //if the value is really changing
		{
			final SectionType oldType = sectionType; //get the old value
			sectionType = newSectionType; //actually change the value
			firePropertyChange(SECTION_TYPE_PROPERTY, oldType, newSectionType); //indicate that the value changed
		}
	}

	/** Default constructor with a default text model. */
	public TextBox()
	{
		this((String)null); //construct the class with no text
	}

	/**
	 * Text constructor with a default {@link Model#PLAIN_TEXT_CONTENT_TYPE} content type.
	 * @param text The text, which may include a resource reference, or <code>null</code> if there is no text.
	 */
	public TextBox(final String text)
	{
		this(text, PLAIN_TEXT_CONTENT_TYPE); //construct the class with a plain text content type
	}

	/**
	 * Text and content type constructor
	 * @param text The text, which may include a resource reference, or <code>null</code> if there is no text.
	 * @param textContentType The content type of the text.
	 * @throws NullPointerException if the given content type is <code>null</code>.
	 * @throws IllegalArgumentException if the given content type is not a text content type.
	 */
	public TextBox(final String text, final ContentType textContentType)
	{
		this(new DefaultTextModel(text, textContentType)); //construct the class with a default text model using the given values
	}

	/**
	 * Text model constructor.
	 * @param textModel The component text model.
	 * @throws NullPointerException if the given text model is <code>null</code>.
	 */
	public TextBox(final TextModel textModel)
	{
		super(new ReferenceLayout()); //construct the parent class
		this.textModel = checkInstance(textModel, "Text model cannot be null."); //save the text model
		this.textModel.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the text model
		this.textModel.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the text model
	}

	//TextModel delegates 

	/** @return The text, which may include a resource reference, or <code>null</code> if there is no text. */
	public String getText()
	{
		return getTextModel().getText();
	}

	/**
	 * Sets the text. This is a bound property.
	 * @param newText The new text, which may include a resource reference.
	 * @see #TEXT_PROPERTY
	 */
	public void setText(final String newText)
	{
		getTextModel().setText(newText);
	}

	/** @return The content type of the text. */
	public ContentType getTextContentType()
	{
		return getTextModel().getTextContentType();
	}

	/**
	 * Sets the content type of the text. This is a bound property.
	 * @param newTextContentType The new text content type.
	 * @throws NullPointerException if the given content type is <code>null</code>.
	 * @throws IllegalArgumentException if the given content type is not a text content type.
	 * @see #TEXT_CONTENT_TYPE_PROPERTY
	 */
	public void setTextContentType(final ContentType newTextContentType)
	{
		getTextModel().setTextContentType(newTextContentType);
	}

}
