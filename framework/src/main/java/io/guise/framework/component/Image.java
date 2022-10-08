/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

import java.net.URI;

import io.guise.framework.model.*;

/**
 * A simple image component with no descriptive text.
 * @author Garret Wilson
 */
public class Image extends AbstractImageComponent {

	/** Default constructor. */
	public Image() {
		this(new DefaultInfoModel(), new DefaultImageModel()); //construct the parent class with default models
	}

	/**
	 * Image model constructor.
	 * @param imageModel The component image model.
	 * @throws NullPointerException if the given image model is <code>null</code>.
	 */
	public Image(final ImageModel imageModel) {
		this(new DefaultInfoModel(), imageModel); //construct the parent class with a default info model
	}

	/**
	 * Info model and image model constructor.
	 * @param infoModel The component info model.
	 * @param imageModel The component image model.
	 * @throws NullPointerException if the given info model and/or image model is <code>null</code>.
	 */
	public Image(final InfoModel infoModel, final ImageModel imageModel) {
		super(infoModel, imageModel); //construct the parent class
	}

	/**
	 * Image URI constructor.
	 * @param imageURI The image URI, which may be a resource URI, or <code>null</code> if there is no image URI.
	 */
	public Image(final URI imageURI) {
		this(imageURI, null);
	}

	/**
	 * Image URI and label constructor.
	 * @param imageURI The image URI, which may be a resource URI, or <code>null</code> if there is no image URI.
	 * @param label The text of the label, or <code>null</code> if there should be no label.
	 */
	public Image(final URI imageURI, final String label) {
		this(new DefaultInfoModel(label), new DefaultImageModel(imageURI));
	}

}
