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

import com.guiseframework.model.DefaultTableColumnModel;

/**
 * A model for a table column representing the reference URI of an RDF resource. This is implemented as a separate class so that other classes such as
 * {@link RDFResourceTableModel} can recognize the column's special semantics.
 * @author Garret Wilson
 */
public class RDFResourceURITableColumnModel extends DefaultTableColumnModel<URI> {

	/** Default constructor. */
	public RDFResourceURITableColumnModel() {
		this(null); //construct the class with no label
	}

	/**
	 * Label constructor.
	 * @param labelText The text of the label.
	 */
	public RDFResourceURITableColumnModel(final String labelText) {
		super(URI.class, labelText); //construct the parent class
	}

}
