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

package com.guiseframework.model.rdf;

import com.globalmentor.rdf.*;
import com.guiseframework.model.DefaultTreeNodeModel;

/**
 * A tree node model that represents an RDF literal.
 * @author Garret Wilson
 */
public class RDFLiteralTreeNodeModel extends DefaultTreeNodeModel<RDFLiteral> implements RDFObjectTreeNodeModel<RDFLiteral> {

	/** The RDF property of which this RDF object is an object, or <code>null</code> if this object should not be considered the object of any property. */
	private final RDFResource property;

	/** @return The RDF property of which this RDF object is an object, or <code>null</code> if this object should not be considered the object of any property. */
	public RDFResource getProperty() {
		return property;
	}

	/** Default constructor with no initial value. */
	public RDFLiteralTreeNodeModel() {
		this(null); //construct the class with no initial value
	}

	/**
	 * Initial value constructor.
	 * @param initialValue The initial value, which will not be validated.
	 */
	public RDFLiteralTreeNodeModel(final RDFLiteral initialValue) {
		this(null, initialValue); //construct the class with a null initial value
	}

	/**
	 * Property and initial value constructor.
	 * @param rdfProperty The property of which this object is a resource, or <code>null</code> if this object should not be considered the object of any
	 *          property.
	 * @param initialValue The initial value, which will not be validated.
	 */
	public RDFLiteralTreeNodeModel(final RDFResource rdfProperty, final RDFLiteral initialValue) {
		super(RDFLiteral.class, initialValue); //construct the parent class
		property = rdfProperty; //save the property of which this resource is the object
	}
}
