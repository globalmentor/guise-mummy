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
import java.util.Map;

import com.guiseframework.component.*;

import static com.globalmentor.text.xml.xhtml.XHTML.*;

/**
 * Strategy for rendering a {@link LayoutComponent} as an XHTML <code>&lt;div&gt;</code> element.
 * <p>
 * Changes to {@link LayoutComponent#NOTIFICATION_PROPERTY} are ignored.
 * </p>
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebLayoutComponentDepictor<C extends LayoutComponent> extends AbstractWebLayoutComponentDepictor<C> {

	/** Default constructor using the XHTML <code>&lt;div&gt;</code> element. */
	public WebLayoutComponentDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV); //represent <xhtml:div>
		getIgnoredProperties().add(LayoutComponent.NOTIFICATION_PROPERTY); //ignore Panel.notification, because we don't want to mark the component invalid when it registers a notification as this is used to pass a notification up to an enclosing class
	}

	/**
	 * {@inheritDoc} This version combines the body styles with the outer styles.
	 * @see AbstractWebComponentDepictor#getBodyStyles(XMLGuiseContext, Component)
	 */
	@Override
	protected Map<String, Object> getOuterStyles() { //TODO decide if this technique is the best for the container views
		final Map<String, Object> outerStyles = super.getOuterStyles(); //get the default outer styles
		outerStyles.putAll(getBodyStyles()); //add the styles for the body
		return outerStyles; //return the combined styles		
	}

	/** {@inheritDoc} This version writes ID, class, and direction attributes. */
	@Override
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		writeBodyIDClassAttributes(null, null); //write the ID and class attributes
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction
	}
}
