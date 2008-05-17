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

package com.guiseframework.component.layout;

import static com.globalmentor.java.Objects.*;

import java.net.URI;

import javax.mail.internet.ContentType;

import com.guiseframework.model.*;

/**Constraints on an individual component in a card layout.
@author Garret Wilson
*/
public class CardConstraints extends ControlConstraints implements InfoModel, Displayable, Enableable
{

	/**The info model used by this component.*/
	private final InfoModel infoModel;

		/**@return The info model used by this component.*/
		protected InfoModel getInfoModel() {return infoModel;}

	/**Default constructor.*/
	public CardConstraints()
	{
		this(true);	//construct the class, defaulting to enabled
	}

	/**Enabled constructor.
	@param enabled Whether the card is enabled.
	*/
	public CardConstraints(final boolean enabled)
	{
		this((String)null, enabled);	//construct the class with no label
	}

	/**Label constructor.
	@param label The text of the label.
	*/
	public CardConstraints(final String label)
	{
		this(label, true);	//construct the class, defaulting to enabled
	}

	/**Label and enabled constructor.
	@param label The text of the label.
	@param enabled Whether the card is enabled.
	*/
	public CardConstraints(final String label, final boolean enabled)
	{
		this(new DefaultInfoModel(label), enabled);	//construct the class with a default info model
	}

	/**Info model constructor.
	@param infoModel The info model representing the card label.
	@exception NullPointerException if the given info model is <code>null</code>.
	*/
	public CardConstraints(final InfoModel infoModel)
	{
		this(infoModel, true);	//construct the class, defaulting to enabled
	}

	/**Info model and enabled constructor.
	@param infoModel The info model representing the card label.
	@param enabled Whether the card is enabled.
	@exception NullPointerException if the given info model is <code>null</code>.
	*/
	public CardConstraints(final InfoModel infoModel, final boolean enabled)
	{
		super(enabled);	//construct the parent class 
		this.infoModel=checkInstance(infoModel, "Info model cannot be null.");	//save the info model
		this.infoModel.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the info model
		this.infoModel.addVetoableChangeListener(getRepeatVetoableChangeListener());	//listen and repeat all vetoable changes of the info model
	}

		//InfoModel delegations

	/**@return The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.*/
	public URI getGlyphURI() {return getInfoModel().getGlyphURI();}

	/**Sets the URI of the icon.
	This is a bound property of type <code>URI</code>.
	@param newLabelIcon The new URI of the icon, which may be a resource URI.
	@see #GLYPH_URI_PROPERTY
	*/
	public void setGlyphURI(final URI newLabelIcon) {getInfoModel().setGlyphURI(newLabelIcon);}

	/**@return The label text, which may include a resource reference, or <code>null</code> if there is no label text.*/
	public String getLabel() {return getInfoModel().getLabel();}

	/**Sets the text of the label.
	This is a bound property.
	@param newLabelText The new text of the label, which may include a resource reference.
	@see #LABEL_PROPERTY
	*/
	public void setLabel(final String newLabelText) {getInfoModel().setLabel(newLabelText);}

	/**@return The content type of the label text.*/
	public ContentType getLabelContentType() {return getInfoModel().getLabelContentType();}

	/**Sets the content type of the label text.
	This is a bound property.
	@param newLabelTextContentType The new label text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #LABEL_CONTENT_TYPE_PROPERTY
	*/
	public void setLabelContentType(final ContentType newLabelTextContentType) {getInfoModel().setLabelContentType(newLabelTextContentType);}

	/**@return The description text, such as might appear in a flyover, or <code>null</code> if there is no description.*/
	public String getDescription() {return getInfoModel().getDescription();}

	/**Sets the description text, such as might appear in a flyover.
	This is a bound property.
	@param newDescription The new text of the description, such as might appear in a flyover.
	@see #DESCRIPTION_PROPERTY
	*/
	public void setDescription(final String newDescription) {getInfoModel().setDescription(newDescription);}

	/**@return The content type of the description text.*/
	public ContentType getDescriptionContentType() {return getInfoModel().getDescriptionContentType();}

	/**Sets the content type of the description text.
	This is a bound property.
	@param newDescriptionContentType The new description text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #DESCRIPTION_CONTENT_TYPE_PROPERTY
	*/
	public void setDescriptionContentType(final ContentType newDescriptionContentType) {getInfoModel().setDescriptionContentType(newDescriptionContentType);}

	/**@return The advisory information text, such as might appear in a tooltip, or <code>null</code> if there is no advisory information.*/
	public String getInfo() {return getInfoModel().getInfo();} 

	/**Sets the advisory information text, such as might appear in a tooltip.
	This is a bound property.
	@param newInfo The new text of the advisory information, such as might appear in a tooltip.
	@see #INFO_PROPERTY
	*/
	public void setInfo(final String newInfo) {getInfoModel().setInfo(newInfo);}

	/**@return The content type of the advisory information text.*/
	public ContentType getInfoContentType() {return getInfoModel().getInfoContentType();}

	/**Sets the content type of the advisory information text.
	This is a bound property.
	@param newInfoContentType The new advisory information text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #INFO_CONTENT_TYPE_PROPERTY
	*/
	public void setInfoContentType(final ContentType newInfoContentType) {getInfoModel().setInfoContentType(newInfoContentType);}

}
