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

package io.guise.framework.component;

import java.net.URI;

import io.guise.framework.model.PendingImageModel;

import static com.globalmentor.java.Classes.*;

/**
 * A component that displays an image and is able to indicate when an image is pending.
 * @author Garret Wilson
 */
public interface PendingImageComponent extends ImageComponent, PendingImageModel {

	/** The pending image URI bound property. */
	public static final String PENDING_IMAGE_URI_PROPERTY = getPropertyName(PendingImageComponent.class, "pendingImageURI");

	/** @return The pending image URI, which may be a resource URI, or <code>null</code> if there is no image URI. */
	public URI getPendingImageURI();

	/**
	 * Sets the URI of the pending image. This is a bound property.
	 * @param newPendingImageURI The new URI of the pending image, which may be a resource URI.
	 * @see #PENDING_IMAGE_URI_PROPERTY
	 */
	public void setPendingImageURI(final URI newPendingImageURI);

}
