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

package io.guise.framework.platform.web;

import java.io.IOException;
import java.net.URI;

import io.guise.framework.component.Component;
import io.guise.framework.component.CompositeComponent;

import static com.globalmentor.w3c.spec.HTML.*;

/**
 * Strategy for rendering a component as an XHTML <code>&lt;ol&gt;</code> element.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebListDepictor<C extends CompositeComponent> extends AbstractSimpleWebComponentDepictor<C> { //TODO finish, verify, and create corresponding component

	/** Default constructor using the XHTML <code>&lt;ol&gt;</code> element. */
	public WebListDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_OL); //represent <xhtml:ol>
	}

	/**
	 * Element namespace and local name constructor.
	 * @param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	 */
	public WebListDepictor(final URI namespaceURI, final String localName) {
		super(namespaceURI, localName); //construct the parent class
	}

	@Override
	protected void depictChildren() throws IOException {
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		depictContext.write("\n"); //format the output
		//don't do the default updating of child views, because we control generation wrapper elements around each child
		for(final Component childComponent : component.getChildComponents()) { //for each child component
			depictContext.writeIndent(); //write an indentation
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_LI); //<xhtml:li>			
			childComponent.depict(); //update the child view
			depictContext.write("\n"); //format the output
			depictContext.writeIndent(); //write an indentation
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_LI); //</xhtml:li>
			depictContext.write("\n"); //format the output
		}
	}
}
