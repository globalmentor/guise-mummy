/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.guise.framework.model;

import java.net.URI;

import static java.util.Objects.*;

import com.globalmentor.java.Objects;
import com.globalmentor.net.MediaType;
import com.globalmentor.text.Text;

import static com.globalmentor.text.Text.*;

/**
 * A default implementation of a model for a label and descriptive information.
 * @author Garret Wilson
 */
public class DefaultInfoModel extends DefaultLabelModel implements InfoModel {

	/** The description text, such as might appear in a flyover, or <code>null</code> if there is no description. */
	private String description = null;

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(final String newDescription) {
		if(!Objects.equals(description, newDescription)) { //if the value is really changing
			final String oldDescription = description; //get the old value
			description = newDescription; //actually change the value
			firePropertyChange(DESCRIPTION_PROPERTY, oldDescription, newDescription); //indicate that the value changed
		}
	}

	/** The content type of the description text. */
	private MediaType descriptionContentType = Text.PLAIN_MEDIA_TYPE;

	@Override
	public MediaType getDescriptionContentType() {
		return descriptionContentType;
	}

	@Override
	public void setDescriptionContentType(final MediaType newDescriptionContentType) {
		requireNonNull(newDescriptionContentType, "Content type cannot be null.");
		if(descriptionContentType != newDescriptionContentType) { //if the value is really changing
			final MediaType oldDescriptionContentType = descriptionContentType; //get the old value
			if(!isText(newDescriptionContentType)) { //if the new content type is not a text content type
				throw new IllegalArgumentException("Content type " + newDescriptionContentType + " is not a text content type.");
			}
			descriptionContentType = newDescriptionContentType; //actually change the value
			firePropertyChange(DESCRIPTION_CONTENT_TYPE_PROPERTY, oldDescriptionContentType, newDescriptionContentType); //indicate that the value changed
		}
	}

	/** The advisory information text, such as might appear in a tooltip, or <code>null</code> if there is no advisory information. */
	private String info = null;

	@Override
	public String getInfo() {
		return info;
	}

	@Override
	public void setInfo(final String newInfo) {
		if(!Objects.equals(info, newInfo)) { //if the value is really changing
			final String oldInfo = info; //get the old value
			info = newInfo; //actually change the value
			firePropertyChange(INFO_PROPERTY, oldInfo, newInfo); //indicate that the value changed
		}
	}

	/** The content type of the advisory information text. */
	private MediaType infoContentType = Text.PLAIN_MEDIA_TYPE;

	@Override
	public MediaType getInfoContentType() {
		return infoContentType;
	}

	@Override
	public void setInfoContentType(final MediaType newInfoContentType) {
		requireNonNull(newInfoContentType, "Content type cannot be null.");
		if(infoContentType != newInfoContentType) { //if the value is really changing
			final MediaType oldInfoContentType = infoContentType; //get the old value
			if(!isText(newInfoContentType)) { //if the new content type is not a text content type
				throw new IllegalArgumentException("Content type " + newInfoContentType + " is not a text content type.");
			}
			infoContentType = newInfoContentType; //actually change the value
			firePropertyChange(INFO_CONTENT_TYPE_PROPERTY, oldInfoContentType, newInfoContentType); //indicate that the value changed
		}
	}

	/** Default constructor. */
	public DefaultInfoModel() {
		this(null); //construct the class with no label
	}

	/**
	 * Label constructor.
	 * @param label The text of the label, or <code>null</code> if there should be no label.
	 */
	public DefaultInfoModel(final String label) {
		this(label, null); //construct the label model with no glyph
	}

	/**
	 * Label and glyph URI constructor.
	 * @param label The text of the label, or <code>null</code> if there should be no label.
	 * @param glyphURI The glyph URI, which may be a resource URI, or <code>null</code> if there is no glyph URI.
	 */
	public DefaultInfoModel(final String label, final URI glyphURI) {
		super(label, glyphURI); //construct the parent class
	}

}
