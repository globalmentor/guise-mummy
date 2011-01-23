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

import com.globalmentor.java.Objects;
import com.globalmentor.net.ContentType;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.text.Text.*;

/**A default implementation of a model for a label and descriptive information.
@author Garret Wilson
*/
public class DefaultInfoModel extends DefaultLabelModel implements InfoModel
{

	/**The description text, such as might appear in a flyover, or <code>null</code> if there is no description.*/
	private String description=null;

		/**@return The description text, such as might appear in a flyover, or <code>null</code> if there is no description.*/
		public String getDescription() {return description;}

		/**Sets the description text, such as might appear in a flyover.
		This is a bound property.
		@param newDescription The new text of the description, such as might appear in a flyover.
		@see #DESCRIPTION_PROPERTY
		*/
		public void setDescription(final String newDescription)
		{
			if(!Objects.equals(description, newDescription))	//if the value is really changing
			{
				final String oldDescription=description;	//get the old value
				description=newDescription;	//actually change the value
				firePropertyChange(DESCRIPTION_PROPERTY, oldDescription, newDescription);	//indicate that the value changed
			}			
		}

	/**The content type of the description text.*/
	private ContentType descriptionContentType=PLAIN_TEXT_CONTENT_TYPE;

		/**@return The content type of the description text.*/
		public ContentType getDescriptionContentType() {return descriptionContentType;}

		/**Sets the content type of the description text.
		This is a bound property.
		@param newDescriptionContentType The new description text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see #DESCRIPTION_CONTENT_TYPE_PROPERTY
		*/
		public void setDescriptionContentType(final ContentType newDescriptionContentType)
		{
			checkInstance(newDescriptionContentType, "Content type cannot be null.");
			if(descriptionContentType!=newDescriptionContentType)	//if the value is really changing
			{
				final ContentType oldDescriptionContentType=descriptionContentType;	//get the old value
				if(!isText(newDescriptionContentType))	//if the new content type is not a text content type
				{
					throw new IllegalArgumentException("Content type "+newDescriptionContentType+" is not a text content type.");
				}
				descriptionContentType=newDescriptionContentType;	//actually change the value
				firePropertyChange(DESCRIPTION_CONTENT_TYPE_PROPERTY, oldDescriptionContentType, newDescriptionContentType);	//indicate that the value changed
			}			
		}

	/**The advisory information text, such as might appear in a tooltip, or <code>null</code> if there is no advisory information.*/
	private String info=null;

		/**@return The advisory information text, such as might appear in a tooltip, or <code>null</code> if there is no advisory information.*/
		public String getInfo() {return info;}

		/**Sets the advisory information text, such as might appear in a tooltip.
		This is a bound property.
		@param newInfo The new text of the advisory information, such as might appear in a tooltip.
		@see #INFO_PROPERTY
		*/
		public void setInfo(final String newInfo)
		{
			if(!Objects.equals(info, newInfo))	//if the value is really changing
			{
				final String oldInfo=info;	//get the old value
				info=newInfo;	//actually change the value
				firePropertyChange(INFO_PROPERTY, oldInfo, newInfo);	//indicate that the value changed
			}			
		}

	/**The content type of the advisory information text.*/
	private ContentType infoContentType=PLAIN_TEXT_CONTENT_TYPE;

		/**@return The content type of the advisory information text.*/
		public ContentType getInfoContentType() {return infoContentType;}

		/**Sets the content type of the advisory information text.
		This is a bound property.
		@param newInfoContentType The new advisory information text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see #INFO_CONTENT_TYPE_PROPERTY
		*/
		public void setInfoContentType(final ContentType newInfoContentType)
		{
			checkInstance(newInfoContentType, "Content type cannot be null.");
			if(infoContentType!=newInfoContentType)	//if the value is really changing
			{
				final ContentType oldInfoContentType=infoContentType;	//get the old value
				if(!isText(newInfoContentType))	//if the new content type is not a text content type
				{
					throw new IllegalArgumentException("Content type "+newInfoContentType+" is not a text content type.");
				}
				infoContentType=newInfoContentType;	//actually change the value
				firePropertyChange(INFO_CONTENT_TYPE_PROPERTY, oldInfoContentType, newInfoContentType);	//indicate that the value changed
			}			
		}

	/**Default constructor.*/
	public DefaultInfoModel()
	{
		this(null);	//construct the class with no label
	}

	/**Label constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	*/
	public DefaultInfoModel(final String label)
	{
		this(label, null);	//construct the label model with no glyph
	}

	/**Label and glyph URI constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param glyphURI The glyph URI, which may be a resource URI, or <code>null</code> if there is no glyph URI.
	*/
	public DefaultInfoModel(final String label, final URI glyphURI)
	{
		super(label, glyphURI);	//construct the parent class
	}

}
