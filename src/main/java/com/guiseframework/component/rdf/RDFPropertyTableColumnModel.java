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

package com.guiseframework.component.rdf;

import java.net.URI;

import static com.globalmentor.java.Objects.*;

import com.globalmentor.rdf.*;
import com.guiseframework.model.DefaultTableColumnModel;

/**
 * A model for a table column representing an RDF property.
 * @param <V> The type of values contained in the table column representing the property value, which must be an RDF object.
 * @author Garret Wilson
 */
public class RDFPropertyTableColumnModel<V extends RDFObject> extends DefaultTableColumnModel<V> {

	/** The URI of the RDF property this column represents. */
	private final URI propertyURI;

	/** @return The URI of the RDF property this column represents. */
	public URI getPropertyURI() {
		return propertyURI;
	}

	/**
	 * Value class constructor.
	 * @param valueClass The class indicating the type of values held in the model.
	 * @param propertyURI The URI of the RDF property this column represents.
	 * @throws NullPointerException if the given value class and/or property URI is <code>null</code>.
	 */
	public RDFPropertyTableColumnModel(final Class<V> valueClass, final URI propertyURI) {
		this(valueClass, propertyURI, new RDFXMLGenerator().getLabel(propertyURI)); //construct the class with a label appropriate for this property URI TODO use a shared RDF XMLifier
	}

	/**
	 * Value class and label constructor.
	 * @param valueClass The class indicating the type of values held in the model.
	 * @param propertyURI The URI of the RDF property this column represents.
	 * @param labelText The text of the label.
	 * @throws NullPointerException if the given value class and/or property URI is <code>null</code>.
	 */
	public RDFPropertyTableColumnModel(final Class<V> valueClass, final URI propertyURI, final String labelText) {
		super(valueClass, labelText); //construct the parent class
		this.propertyURI = checkInstance(propertyURI, "Property URI cannot be null.");
	}

}
