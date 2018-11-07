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

import io.guise.framework.component.*;
import io.guise.framework.event.*;

import static com.globalmentor.w3c.spec.HTML.*;

/**
 * Strategy for rendering an action model control as an XHTML <code>&lt;a&gt;</code> element containing a heading. For the component to be rendered at the
 * correct heading level it must be a {@link HeadingComponent}. If a link has a {@link NavigateActionListener} as one of its action listeners, the generated
 * <code>href</code> URI will be that of the listener, and a <code>target</code> attribute will be set of the listener specifies a viewport ID.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 * @see WebHeadingDepictor
 */
public class WebHeadingLinkDepictor<C extends ActionControl> extends WebLinkDepictor<C> {

	/** Default constructor using the XHTML <code>&lt;a&gt;</code> element. */
	public WebHeadingLinkDepictor() {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version starts the inner heading element.
	 * </p>
	 * @see WebHeadingDepictor#getHeadingLocalName(int)
	 */
	@Override
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		final C component = getDepictedObject(); //get the component
		final String localName = component instanceof HeadingComponent ? WebHeadingDepictor.getHeadingLocalName(((HeadingComponent)component).getLevel()) : null; //if this is a heading, try to get a local name for its specified heading level
		getDepictContext().writeElementBegin(XHTML_NAMESPACE_URI, localName != null ? localName : ELEMENT_LABEL); //start the element, using a default element if there isn't an appropriate heading level
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version ends the inner heading element.
	 * </p>
	 * @see WebHeadingDepictor#getHeadingLocalName(int)
	 */
	@Override
	protected void depictEnd() throws IOException {
		final C component = getDepictedObject(); //get the component
		final String localName = component instanceof HeadingComponent ? WebHeadingDepictor.getHeadingLocalName(((HeadingComponent)component).getLevel()) : null; //if this is a heading, try to get a local name for its specified heading level
		getDepictContext().writeElementEnd(XHTML_NAMESPACE_URI, localName != null ? localName : ELEMENT_LABEL); //end the element, using a default element if there isn't an appropriate heading level
		super.depictEnd(); //do the default ending rendering
	}
}
