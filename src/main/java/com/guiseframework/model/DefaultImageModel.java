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

/**
 * A default implementation of a model for an image.
 * @author Garret Wilson
 */
public class DefaultImageModel extends AbstractModel implements ImageModel {

	/** The image URI, which may be a resource URI, or <code>null</code> if there is no image URI. */
	private URI imageURI;

	/** @return The image URI, which may be a resource URI, or <code>null</code> if there is no image URI. */
	public URI getImageURI() {
		return imageURI;
	}

	/**
	 * Sets the URI of the image. This is a bound property.
	 * @param newImageURI The new URI of the image, which may be a resource URI.
	 * @see #IMAGE_URI_PROPERTY
	 */
	public void setImageURI(final URI newImageURI) {
		if(!Objects.equals(imageURI, newImageURI)) { //if the value is really changing
			final URI oldImageURI = imageURI; //get the old value
			imageURI = newImageURI; //actually change the value
			firePropertyChange(IMAGE_URI_PROPERTY, oldImageURI, newImageURI); //indicate that the value changed
		}
	}

	/** Default constructor. */
	public DefaultImageModel() {
		this(null); //construct the class with no image
	}

	/**
	 * Image URI constructor.
	 * @param imageURI The image URI, which may be a resource URI, or <code>null</code> if there is no image URI.
	 */
	public DefaultImageModel(final URI imageURI) {
		this.imageURI = imageURI; //save the image URI
	}

}
