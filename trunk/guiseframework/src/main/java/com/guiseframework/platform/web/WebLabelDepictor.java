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

package com.guiseframework.platform.web;

import java.io.IOException;

import com.guiseframework.component.LabelComponent;

import static com.globalmentor.text.xml.xhtml.XHTML.*;

/**
 * Strategy for rendering a label as an XHTML <code>&lt;label&gt;</code> element.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebLabelDepictor<C extends LabelComponent> extends AbstractSimpleWebComponentDepictor<C> {

	/** Default constructor using the XHTML <code>&lt;label&gt;</code> element. */
	public WebLabelDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_LABEL, false); //represent <xhtml:label>; don't allow an empty element to be created, which would confuse IE6 and corrupt the DOM tree
	}

	/**
	 * Renders the body of the component.
	 * @throws IOException if there is an error rendering the component.
	 */
	protected void depictBody() throws IOException {
		super.depictBody(); //render the default main part of the component
		writeLabelContent(); //write the content of the label
	}

}
