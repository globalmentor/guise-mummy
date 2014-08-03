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
import java.util.*;

import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.geometry.*;

import static com.globalmentor.text.xml.stylesheets.css.XMLCSS.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**
 * Strategy for rendering a menu as a drop menu. The following illustrates a simplified drop menu structure. <blockquote><code><pre>
&lt;div class="dropMenu" style="position:relative;"&gt;
	&lt;label&gt;Menu&lt;/label&gt;
	&lt;div class="dropMenu-container" style="position:absolute;left:0%;top:100%;width:1000%"&gt;
		&lt;div class="dropMenu-body" style="position:absolute;left:0%;top:0%;"&gt;
			&lt;table class="layout-flow-y"&gt;
				&lt;tbody&gt;
					&lt;tr class="layout-flow-y"&gt;
						&lt;td class="layout-flow-y-child"&gt;&lt;div&gt;
							&lt;a href=""&gt;&lt;span&gt;Submenu&lt;/span&gt;&lt;/a&gt;&lt;/div&gt;
						&lt;/td&gt;
					&lt;/tr&gt;
				&lt;/tbody&gt;
			&lt;/table&gt;
		&lt;/div&gt;
	&lt;/div&gt;
&lt;/div&gt;
</pre></code><blockquote>
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebDropMenuDepictor<C extends Menu> extends AbstractWebMenuDepictor<C> {

	/** Default constructor using the XHTML <code>&lt;ol&gt;</code> element. */
	public WebDropMenuDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV); //represent <xhtml:div>
	}

	/**
	 * Retrieves the styles for the outer element of the component. This version adds relative positioning to make the main part of the menu the containing block
	 * for absolute child positioning. //TODO del This version removes the visibility specification, allowing this to be controlled by client JavaScript.
	 * @return The styles for the outer element of the component, mapped to CSS property names.
	 * @see AbstractWebComponentDepictor#getBodyStyles()
	 */
	protected Map<String, Object> getOuterStyles() { //TODO eventually switch to pure AJAX menus
		final Map<String, Object> outerStyles = super.getOuterStyles(); //get the default outer styles
		final CompositeComponent parent = getDepictedObject().getParent(); //get the component parent
		if(parent instanceof Menu) { //if this menu isn't a root menu (warning: giving the root menu relative positioning will cause submenu items to disappear on IE6/7 when the modal layer is turned on)
			outerStyles.put(CSS_PROP_POSITION, CSS_POSITION_RELATIVE); //use position:relative to make the main part of the menu the containing block for absolute child positioning
			outerStyles.put(CSS_PROP_Z_INDEX, Integer.toString(1)); //z-index:1 (warning: putting this on the absolutely positioned element will make other non-absolutely positioned elements, such as toolbar child image elements, appear in front of this element) TODO create a menu layer, and use that constant
		}
		return outerStyles; //return the styles
	}

	/**
	 * Retrieves the styles for the body element of the component. This version correctly positions the menu body for non-root menus using absolute positioning.
	 * @return The styles for the body element of the component, mapped to CSS property names.
	 */
	protected Map<String, Object> getBodyStyles() {
		final Map<String, Object> styles = super.getBodyStyles(); //get the default body styles
		final C component = getDepictedObject(); //get the component
		final CompositeComponent parent = component.getParent(); //get the component parent
		if(parent instanceof Menu) { //if this menu isn't a root menu
			styles.put(CSS_PROP_POSITION, CSS_POSITION_ABSOLUTE); //position:absolute
			final Flow flow = component.getLayout().getFlow(); //get the flow
			final Axis flowAxis = component.getComponentOrientation().getAxis(flow); //get the axis of the flow
			switch(flowAxis) { //see on which axis the menu flows
				case X:
					throw new UnsupportedOperationException("Support not yet added for horizontal menus.");
				case Y:
					//TODO check the flow of the parent menu to determine positioning
					styles.put(CSS_PROP_LEFT, new Extent(0, Unit.RELATIVE)); //left:0%
					styles.put(CSS_PROP_TOP, new Extent(1, Unit.RELATIVE)); //top:100%
					break;
				default:
					throw new AssertionError("Unrecognized axis: " + flowAxis);
			}
		}
		return styles; //return the styles
	}

	/**
	 * Begins the rendering process. This version wraps the component in a decorator element.
	 * @throws IOException if there is an error rendering the component.
	 * @throws IllegalArgumentException if the given value control represents a value type this controller doesn't support.
	 */
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		writeIDClassAttributes(null, null, MOUSE_LISTENER_CLASS); //write the ID and class attributes with no prefixes or suffixes, indicating that the menu should be a mouse listener TODO maybe switch to overridding isMouseListener(); see tool button depictor
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction
		writeLabel(decorateID(getPlatform().getDepictIDString(component.getDepictID()), null, COMPONENT_BODY_CLASS_SUFFIX)); //write the label for the menu body, if there is a label
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div> (component-container)
		writeIDClassAttributes(null, COMPONENT_CONTAINER_CLASS_SUFFIX); //write the ID and class attributes for the container
		final Map<String, Object> containerStyles = new HashMap<String, Object>(); //create a new map of styles
		final boolean showOpen = component.isOpen() || (component.isRolloverOpenEnabled() && component.isRollover()); //show the menu as open if it is open or is in rollover state with rollover open enabled
		containerStyles.put(CSS_PROP_DISPLAY, showOpen ? CSS_DISPLAY_BLOCK : CSS_DISPLAY_NONE); //show or hide the container based upon open state
		final CompositeComponent parent = component.getParent(); //get the component parent
		if(parent instanceof Menu) { //if this menu isn't a root menu
			final Menu parentMenu = (Menu)parent; //get the menu as a parent
			final Flow parentFlow = parentMenu.getLayout().getFlow(); //get the parent flow
			final Axis parentFlowAxis = parentMenu.getComponentOrientation().getAxis(parentFlow); //get the axis of the parent flow
			containerStyles.put(CSS_PROP_POSITION, CSS_POSITION_ABSOLUTE); //position:absolute
			final Flow flow = component.getLayout().getFlow(); //get the flow
			final Axis flowAxis = component.getComponentOrientation().getAxis(flow); //get the axis of the flow
			switch(flowAxis) { //see on which axis the menu flows
				case X:
					throw new UnsupportedOperationException("Support not yet added for horizontal menus.");
				case Y:
					switch(parentFlowAxis) { //see what axis the parent is flowing on
						case X:
							containerStyles.put(CSS_PROP_LEFT, new Extent(0, Unit.RELATIVE)); //left:0%
							containerStyles.put(CSS_PROP_TOP, new Extent(1, Unit.RELATIVE)); //top:100%
							break;
						case Y:
							containerStyles.put(CSS_PROP_LEFT, new Extent(1, Unit.RELATIVE)); //left:100%
							containerStyles.put(CSS_PROP_TOP, new Extent(0, Unit.RELATIVE)); //top:0%
							break;
						default:
							throw new AssertionError("Unrecognized axis: " + parentFlowAxis);
					}
					containerStyles.put(CSS_PROP_WIDTH, new Extent(10, Unit.RELATIVE)); //width:1000%
					break;
				default:
					throw new AssertionError("Unrecognized axis: " + flowAxis);
			}
		}
		writeStyleAttribute(containerStyles); //write styles to position the container
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div> (component-body)
		writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX); //write the ID and class attributes for the body
		writeStyleAttribute(getBodyStyles()); //write the component's body styles
	}

	/**
	 * Ends the rendering process. This version closes the decorator elements.
	 * @throws IOException if there is an error rendering the component.
	 */
	protected void depictEnd() throws IOException {
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div> (component-body)
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div> (component-container)
		//TODO fix		writeErrorMessage();	//write the error message, if any
		super.depictEnd(); //do the default ending rendering
	}
}
