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

import com.guiseframework.component.Message;

import static com.globalmentor.w3c.spec.HTML.*;

/**
 * Strategy for rendering a message component as an XHTML <code>&lt;div&gt;</code> element.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebMessageDepictor<C extends Message> extends AbstractDecoratedWebComponentDepictor<C> { //TODO del class if not needed

	/** Default constructor using the XHTML <code>&lt;div&gt;</code> element. */
	public WebMessageDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV); //represent <xhtml:div>
	}

	@Override
	protected void depictBody() throws IOException {
		super.depictBody(); //render the default main part of the component
		final C component = getDepictedObject(); //get the component
		final String message = component.getMessage(); //get the component message, if any
		if(message != null) { //if the component has a message
			writeText(getSession().dereferenceString(message), component.getMessageContentType()); //write the resolved message appropriately for its content type
		}
	}
}
