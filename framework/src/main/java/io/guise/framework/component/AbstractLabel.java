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

package io.guise.framework.component;

import com.globalmentor.net.ContentType;

import io.guise.framework.component.transfer.*;
import io.guise.framework.model.*;

import static com.globalmentor.java.Arrays.*;

/**
 * A abstract component the label of which comprises the main content of the component. This component installs a default export strategy supporting export of
 * the following content types:
 * <ul>
 * <li>The label content type.</li>
 * </ul>
 * @author Garret Wilson
 */
public abstract class AbstractLabel extends AbstractComponent implements LabelComponent {

	/** The default export strategy for this component type. */
	protected static final ExportStrategy<LabelComponent> DEFAULT_EXPORT_STRATEGY = new ExportStrategy<LabelComponent>() {

		@Override
		public Transferable<LabelComponent> exportTransfer(final LabelComponent component) {
			return new DefaultTransferable(component); //return a default transferable for this component
		}
	};

	/** Default constructor with a default info model. */
	public AbstractLabel() {
		this(new DefaultInfoModel()); //construct the class with a default info model
	}

	/**
	 * Info model constructor.
	 * @param infoModel The component info model.
	 * @throws NullPointerException if the given label or model is <code>null</code>.
	 */
	public AbstractLabel(final InfoModel infoModel) {
		super(infoModel); //construct the parent class
		addExportStrategy(DEFAULT_EXPORT_STRATEGY); //install a default export strategy 
	}

	/**
	 * The default transferable object for a label.
	 * @author Garret Wilson
	 */
	protected static class DefaultTransferable extends AbstractTransferable<LabelComponent> {

		/**
		 * Source constructor.
		 * @param source The source of the transferable data.
		 * @throws NullPointerException if the provided source is <code>null</code>.
		 */
		public DefaultTransferable(final LabelComponent source) {
			super(source); //construct the parent class
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation returns the content type of the label.
		 * </p>
		 */
		@Override
		public ContentType[] getContentTypes() {
			return createArray(getSource().getLabelContentType());
		}

		@Override
		public Object transfer(final ContentType contentType) {
			final LabelComponent source = getSource(); //get the source of the transfer
			if(contentType.hasBaseType(source.getLabelContentType())) { //if we have the content type requested
				final String label = source.getLabel(); //get the label
				return label != null ? source.getSession().dereferenceString(source.getLabel()) : null; //return the label text, if any
			} else { //if we don't support this content type
				throw new IllegalArgumentException("Content type not supported: " + contentType);
			}
		}
	}
}
