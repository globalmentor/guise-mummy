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

import static com.globalmentor.rdf.RDFResources.*;
import static com.globalmentor.rdf.rdfs.RDFS.*;

import com.globalmentor.rdf.*;
import com.guiseframework.component.TreeControl;
import com.guiseframework.model.*;

/**
 * Abstract functionality for a tree node representation strategy representing an RDF resource.
 * @author Garret Wilson
 */
public abstract class AbstractRDFResourceTreeNodeRepresentationStrategy<V extends RDFResource> extends AbstractRDFObjectTreeNodeRepresentationStrategy<V> {

	/** Whether the resource reference URI should be included. */
	private boolean resourceReferenceURIIncluded = true;

	/** @return Whether the resource reference URI should be included. */
	protected boolean isResourceReferenceURIIncluded() {
		return resourceReferenceURIIncluded;
	}

	/**
	 * Sets whether the resource reference URI should be included.
	 * @param referenceURIIncluded Whether the resource reference URI should be included.
	 */
	protected void setResourceReferenceURIIncluded(final boolean referenceURIIncluded) {
		this.resourceReferenceURIIncluded = referenceURIIncluded;
	}

	/** Whether the resource type should be included. */
	private boolean resourceTypeIncluded = true;

	/** @return Whether the resource type should be included. */
	protected boolean isResourceTypeIncluded() {
		return resourceTypeIncluded;
	}

	/**
	 * Sets whether the resource type should be included.
	 * @param resourceTypeIncluded Whether the resource type should be included.
	 */
	protected void setResourceTypeIncluded(final boolean resourceTypeIncluded) {
		this.resourceTypeIncluded = resourceTypeIncluded;
	}

	/** Default constructor with a default RDF XMLifier. */
	public AbstractRDFResourceTreeNodeRepresentationStrategy() {
		this(new RDFXMLGenerator()); //create the class with a default RDF XMLifier
	}

	/**
	 * RDF XMLifier constructor.
	 * @param rdfXMLifier The RDF XMLifier to use for creating labels.
	 * @throws NullPointerException if the given RDF XMLifier is <code>null</code>.
	 */
	public AbstractRDFResourceTreeNodeRepresentationStrategy(final RDFXMLGenerator rdfXMLifier) {
		super(rdfXMLifier); //construct the parent
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version appends information about the resource.
	 * </p>
	 */
	@Override
	//TODO fix	protected <N extends V> StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final N value)
	protected StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model,
			final TreeNodeModel<? extends V> treeNode, final V value) { //TODO later put this method hierarchy in a custom label model
		super.buildLabelText(stringBuilder, treeControl, model, treeNode, value); //do the default label text building
		final RDFResource type = getType(value); //get the type of the resource
		final RDFLiteral label = getLabel(value); //get the label of the resource
		final boolean hasProperty = stringBuilder.length() > 0; //see if we have property information
		boolean hasPredicateToken = false; //we'll note whether we ever have something to represent the predicate of the statement
		if(isResourceTypeIncluded() && type != null) { //if we should indicate the resource type and we have a type
			if(hasProperty && !hasPredicateToken) //if we had a property but no predicate representation
				stringBuilder.append(':'); //append a colon to separate the property from the rest
			if(hasPredicateToken) //if we had something to represent the predicate
				stringBuilder.append(' '); //append a space to separate the rest
			//TODO important: check for type URI null here and elsewhere
			stringBuilder.append('(').append(getXMLGenerator().getLabel(type.getURI())).append(')'); //append "(type)"
			hasPredicateToken = true; //show that we have something to represent the predicate
		}
		if(label != null) { //if there is a label
			if(hasProperty && !hasPredicateToken) //if we had a property but no predicate representation
				stringBuilder.append(':'); //append a colon to separate the property from the rest
			if(hasPredicateToken) //if we had something to represent the predicate
				stringBuilder.append(' '); //append a space to separate the rest
			stringBuilder.append(label); //append the text of the label
			hasPredicateToken = true; //show that we have something to represent the predicate
		}
		if(isResourceReferenceURIIncluded() && value.getURI() != null) { //if we should indicate the reference URI this is not a blank node resource
			if(hasProperty && !hasPredicateToken) //if we had a property but no predicate representation
				stringBuilder.append(':'); //append a colon to separate the property from the rest
			if(hasPredicateToken) //if we had something to represent the predicate
				stringBuilder.append(' '); //append a space to separate the rest
			stringBuilder.append('[').append(getXMLGenerator().getLabel(value.getURI())).append(']'); //append "[referenceURI]" label
			hasPredicateToken = true; //show that we have something to represent the predicate
		}
		final RDFObject literalValue = getValue(value); //get the rdf:value property value, if there is one
		if(literalValue instanceof RDFLiteral) { //if this resource has a literal value
			if(hasProperty && !hasPredicateToken) //if we had a property but no predicate representation
				stringBuilder.append(':'); //append a colon to separate the property from the rest
			if(hasPredicateToken) //if we had something to represent the predicate
				stringBuilder.append(' '); //append a space to separate the rest
			stringBuilder.append('{').append(((RDFLiteral)literalValue).getLexicalForm()).append('}'); //append "{lexicalForm}" label
			hasPredicateToken = true; //show that we have something to represent the predicate				
		}
		return stringBuilder; //return the string builder
	}
}
