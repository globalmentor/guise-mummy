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

import static io.guise.framework.theme.Theme.*;

import java.net.URI;
import java.util.Objects;

import com.globalmentor.beans.*;

import io.guise.framework.model.*;

/**
 * A simple image component with an indication when the image is pending. If a {@link PendingImageModel} is used, the image reflects the value of the model's
 * {@link PendingImageModel#isImagePending()} value. Otherwise, the pending status will reflect whether {@link #getImageURI()} is non-<code>null</code>.
 * @author Garret Wilson
 */
public class PendingImage extends Image implements PendingImageComponent {

	/** The pending image URI, which may be a resource URI, or <code>null</code> if there is no pending image URI. */
	private URI pendingImageURI = GLYPH_BUSY;

	@Override
	public URI getPendingImageURI() {
		return pendingImageURI;
	}

	@Override
	public void setPendingImageURI(final URI newPendingImageURI) {
		if(!Objects.equals(pendingImageURI, newPendingImageURI)) { //if the value is really changing
			final URI oldPendingImageURI = pendingImageURI; //get the old value
			pendingImageURI = newPendingImageURI; //actually change the value
			firePropertyChange(PENDING_IMAGE_URI_PROPERTY, oldPendingImageURI, newPendingImageURI); //indicate that the value changed
		}
	}

	@Override
	public boolean isImagePending() {
		final ImageModel imageModel = getImageModel(); //get the image model
		return imageModel instanceof PendingImageModel ? ((PendingImageModel)imageModel).isImagePending() : getImageURI() == null;
	}

	/** Default constructor. */
	public PendingImage() {
		this(new DefaultInfoModel(), new DefaultImageModel()); //construct the parent class with default models
	}

	/**
	 * Image model constructor.
	 * @param imageModel The component image model.
	 * @throws NullPointerException if the given image model is <code>null</code>.
	 */
	public PendingImage(final ImageModel imageModel) {
		this(new DefaultInfoModel(), imageModel); //construct the parent class with a default info model
	}

	/**
	 * Info model and image model constructor.
	 * @param infoModel The component info model.
	 * @param imageModel The component image model.
	 * @throws NullPointerException if the given info model and/or image model is <code>null</code>.
	 */
	public PendingImage(final InfoModel infoModel, final ImageModel imageModel) {
		super(infoModel, imageModel); //construct the parent class
		if(!(imageModel instanceof PendingImageModel)) { //if the image model is not a pending image model, the presence/absence of the image model will change the image pending status
			imageModel.addPropertyChangeListener(IMAGE_URI_PROPERTY, new AbstractGenericPropertyChangeListener<URI>() { //listen for the image URI changing

				@Override
				public void propertyChange(final GenericPropertyChangeEvent<URI> propertyChangeEvent) { //when the image URI changes
					PendingImage.this.firePropertyChange(IMAGE_PENDING_PROPERTY, propertyChangeEvent.getOldValue() == null, propertyChangeEvent.getNewValue() == null); //the image is pending if the image URI is null
				}

			});
		}
	}

}
