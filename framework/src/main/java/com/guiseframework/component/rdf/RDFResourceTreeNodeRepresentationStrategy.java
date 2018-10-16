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

import com.globalmentor.rdf.*;

/**
 * A default tree node representation strategy representing an RDF resource.
 * @author Garret Wilson
 */
public class RDFResourceTreeNodeRepresentationStrategy extends AbstractRDFResourceTreeNodeRepresentationStrategy<RDFResource> {

	/** Default constructor with a default RDF XMLifier. */
	public RDFResourceTreeNodeRepresentationStrategy() {
		this(new RDFXMLGenerator()); //create the class with a default RDF XMLifier
	}

	/**
	 * RDF XMLifier constructor.
	 * @param rdfXMLifier The RDF XMLifier to use for creating labels.
	 * @throws NullPointerException if the given RDF XMLifier is <code>null</code>.
	 */
	public RDFResourceTreeNodeRepresentationStrategy(final RDFXMLGenerator rdfXMLifier) {
		super(rdfXMLifier); //construct the parent
	}

}
