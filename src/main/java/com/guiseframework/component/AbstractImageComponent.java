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

package com.guiseframework.component;

import java.net.URI;

import static com.globalmentor.java.Arrays.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.net.ContentTypeConstants.*;
import static com.globalmentor.net.URIs.*;

import com.globalmentor.io.*;
import com.globalmentor.net.ContentType;
import com.guiseframework.component.transfer.*;
import com.guiseframework.model.*;

/**
 * An abstract implementation of an image component. This component installs a default export strategy supporting export of the following content types:
 * <ul>
 * <li><code>text/uri-list</code></li>
 * <li>The label content type.</li>
 * </ul>
 * @author Garret Wilson
 */
public abstract class AbstractImageComponent extends AbstractComponent implements ImageComponent {

	/** The default export strategy for this component type. */
	protected static final ExportStrategy<ImageComponent> DEFAULT_EXPORT_STRATEGY = new ExportStrategy<ImageComponent>() {

		@Override
		public Transferable<ImageComponent> exportTransfer(final ImageComponent component) {
			return new DefaultTransferable(component); //return a default transferable for this component
		}
	};

	/** The image model used by this component. */
	private final ImageModel imageModel;

	/** @return The image model used by this component. */
	protected ImageModel getImageModel() {
		return imageModel;
	}

	@Override
	public URI getImageURI() {
		return getImageModel().getImageURI();
	}

	@Override
	public void setImageURI(final URI newImageURI) {
		getImageModel().setImageURI(newImageURI);
	}

	/**
	 * Info model and image model constructor.
	 * @param infoModel The component info model.
	 * @param imageModel The component image model.
	 * @throws NullPointerException if the given info model and/or iamge model is <code>null</code>.
	 */
	public AbstractImageComponent(final InfoModel infoModel, final ImageModel imageModel) {
		super(infoModel); //construct the parent class
		this.imageModel = checkInstance(imageModel, "Image model cannot be null."); //save the image model
		if(imageModel != infoModel) { //if the models are different (we'll already be listening to the info model)
			this.imageModel.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the image model
			this.imageModel.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the image model
		}
	}

	/**
	 * The default transferable object for an image.
	 * @author Garret Wilson
	 */
	protected static class DefaultTransferable extends AbstractTransferable<ImageComponent> {

		/**
		 * Source constructor.
		 * @param source The source of the transferable data.
		 * @throws NullPointerException if the provided source is <code>null</code>.
		 */
		public DefaultTransferable(final ImageComponent source) {
			super(source); //construct the parent class
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation returns a URI-list content type and the content type of the label.
		 * </p>
		 */
		@Override
		public ContentType[] getContentTypes() {
			return createArray(ContentType.create(ContentType.TEXT_PRIMARY_TYPE, URI_LIST_SUBTYPE), getSource().getLabelContentType());
		} //TODO use a static instance

		@Override
		public Object transfer(final ContentType contentType) {
			final ImageComponent image = getSource(); //get the image
			if(contentType.match(ContentType.TEXT_PRIMARY_TYPE, URI_LIST_SUBTYPE)) { //if this is a text/uri-list type
				final URI imageURI = image.getImageURI(); //get the image URI
				return imageURI != null ? createURIList(image.getSession().resolveURI(imageURI)) : null; //return the image URI, if there is one
			} else if(contentType.hasBaseType(image.getLabelContentType())) { //if the label has the content type requested
				final String label = image.getLabel(); //get the image label, if any
				return label != null ? image.getSession().dereferenceString(image.getLabel()) : null; //return the resolved label text, if any
			} else { //if we don't support this content type
				throw new IllegalArgumentException("Content type not supported: " + contentType);
			}
		}
	}

}
