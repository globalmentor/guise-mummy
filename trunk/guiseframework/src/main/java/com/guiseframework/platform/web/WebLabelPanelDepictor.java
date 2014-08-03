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

import com.guiseframework.component.LayoutComponent;

import static com.globalmentor.text.xml.xhtml.XHTML.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**
 * Strategy for rendering a labeled panel as a series of XHTML elements. This controller can be substituted for {@link WebFieldsetDepictor} for a different
 * rendering of group panels.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 * @see WebFieldsetDepictor
 */
public class WebLabelPanelDepictor<C extends LayoutComponent> extends AbstractWebLayoutComponentDepictor<C> {

	/** Default constructor using the XHTML <code>&lt;div&gt;</code> element. */
	public WebLabelPanelDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV); //represent <xhtml:div>
	}

	/**
	 * Begins the rendering process. This version wraps the component in a decorator element.
	 * @throws IOException if there is an error rendering the component.
	 * @throws IllegalArgumentException if the given value control represents a value type this controller doesn't support.
	 */
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		writeIDClassAttributes(null, null); //write the ID and class attributes with no prefixes or suffixes
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction
		writeLabel(decorateID(getPlatform().getDepictIDString(getDepictedObject().getDepictID()), null, COMPONENT_BODY_CLASS_SUFFIX)); //write the label for the body, if there is a label				
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div> (component-body)
		writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX); //write the ID and class for the body element
	}

	/**
	 * Ends the rendering process. This version closes the decorator elements.
	 * @throws IOException if there is an error rendering the component.
	 */
	protected void depictEnd() throws IOException {
		getDepictContext().writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div> (component-body)
		writeErrorMessage(); //write the error message, if any
		super.depictEnd(); //do the default ending rendering
	}

}
