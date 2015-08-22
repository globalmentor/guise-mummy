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
import java.net.URI;
import java.util.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.model.AbstractModel;

import static com.globalmentor.text.xml.xhtml.XHTML.*;
import static com.globalmentor.w3c.spec.CSS.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**
 * Strategy for rendering a pictures as a series of XHTML elements along with label and description.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebPictureDepictor<C extends Picture> extends AbstractWebComponentDepictor<C> {

	/** Default constructor using the XHTML <code>&lt;div&gt;</code> element. */
	public WebPictureDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV); //represent <xhtml:div>
	}

	/**
	 * Retrieves the styles for the body element of the component. This version sets the opacity if necessary, and adds layout fixes for images within tables.
	 * @return The styles for the body element of the component, mapped to CSS property names.
	 */
	protected Map<String, Object> getBodyStyles() {
		final Map<String, Object> styles = super.getBodyStyles(); //get the default body styles
		final double opacity = getDepictedObject().getImageOpacity(); //get the image opacity
		if(opacity < 1.0) { //if the opacity isn't 100% TODO combine with component opacity
			styles.put(CSS_PROP_OPACITY, Double.valueOf(opacity)); //indicate the opacity
		}
		return styles; //return the updated body styles
	}

	/**
	 * Begins the rendering process. This version wraps the component in a decorator element.
	 * @throws IOException if there is an error rendering the component.
	 */
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		final GuiseSession session = getSession(); //get the session
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		writeIDClassAttributes(null, null); //write the ID and class attributes with no prefixes or suffixes
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IMG, true); //<xhtml:img>
		writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX); //write the ID and class for the main element
		writeStyleAttribute(getBodyStyles()); //write the component's body styles
		final URI image = component.getImageURI(); //get the component image
		if(image != null) { //if there is an image
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, depictContext.getDepictionURI(image).toString()); //src="image"
		}
		final String label = component.getLabel(); //get the component label, if there is one
		String resolvedLabel = label != null ? session.dereferenceString(label) : null; //resolve the label, if there is one
		if(resolvedLabel == null) {
			resolvedLabel = ""; //TODO fix
		}
		if(resolvedLabel != null) { //if there is a label
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_ALT, AbstractModel.getPlainText(resolvedLabel, component.getLabelContentType())); //alt="label"
		}
	}

	/**
	 * Ends the rendering process. This version renders the caption, if any, and closes the decorator elements.
	 * @throws IOException if there is an error rendering the component.
	 */
	protected void depictEnd() throws IOException {
		final GuiseSession session = getSession(); //get the session
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IMG); //</xhtml:img>
		final boolean hasLabel = hasLabelContent(); //see if there is label content
		final String description = component.getDescription(); //get the description
		final boolean hasDescription = component.isDescriptionDisplayed() && description != null; //see if there is a description to display
		if(hasLabel || hasDescription) { //if there is a label and/or a description
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div> (component-caption)
			writeIDClassAttributes(null, IMAGE_CAPTION_CLASS_SUFFIX); //write the ID and class attributes for a caption
			writeDirectionAttribute(); //write the component direction, if this component specifies a direction
			if(hasLabel) { //if there is a label (do the extra check here, because we only want to write the spacer if there is a label)
				writeLabel(decorateID(getPlatform().getDepictIDString(component.getDepictID()), null, COMPONENT_BODY_CLASS_SUFFIX)); //write the image label, if there is one
				depictContext.write(' '); //separate the label and the description
			}
			if(hasDescription) { //if there is a description
				depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN); //<xhtml:span> (component-description)
				writeIDClassAttributes(null, COMPONENT_DESCRIPTION_CLASS_SUFFIX); //write the ID and class attributes for the description
				writeText(session.dereferenceString(description), component.getDescriptionContentType()); //write the resolved description appropriately for its content type
				depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN); //</xhtml:span> (component-description)
			}
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div> (component-caption)
		}
		super.depictEnd(); //do the default ending rendering
	}

}
