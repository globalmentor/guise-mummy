/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.platform.web;

import java.io.IOException;

import io.guise.framework.component.LayoutComponent;

import static com.globalmentor.html.spec.HTML.*;
import static io.guise.framework.platform.web.GuiseCSSStyleConstants.*;

/**
 * Strategy for rendering a layout component as an XHTML <code>&lt;fieldset&gt;</code> element.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 * @see WebLabelPanelDepictor
 */
public class WebFieldsetDepictor<C extends LayoutComponent> extends AbstractWebLayoutComponentDepictor<C> {

	/** Default constructor using the XHTML <code>&lt;fieldset&gt;</code> element. */
	public WebFieldsetDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_FIELDSET); //represent <xhtml:fieldset>
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation writes the fieldset <code>&lt;fieldset&gt;</code> element if the component has a label.
	 * </p>
	 */
	@Override
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		writeBodyIDClassAttributes(null, null); //write the ID and class attributes
		writeStyleAttribute(getBodyStyles()); //write the component's body styles
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction
		if(hasLabelContent()) { //if there is label content		
			final WebDepictContext depictContext = getDepictContext(); //get the depict context
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_LEGEND); //<xhtml:legend>
			writeIDClassAttributes(null, COMPONENT_LABEL_CLASS_SUFFIX); //write the ID and class for the label element
			writeDirectionAttribute(); //write the component direction, if this component specifies a direction
			writeLabelContent(); //write the content of the label
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_LEGEND); //</xhtml:legend>
		}
	}
}
