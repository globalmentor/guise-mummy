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

import com.globalmentor.model.NameValuePair;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.effect.Effect;
import com.guiseframework.geometry.CompassPoint;
import com.guiseframework.geometry.Extent;
import com.guiseframework.style.Color;

import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.Classes.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;
import static com.globalmentor.w3c.spec.CSS.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**
 * Strategy for rendering a frame as a series of XHTML elements. If the session changes a property, such as locale, orientation, or principal, it is assumed
 * that the entire frame needs updating.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebFrameDepictor<C extends Frame> extends AbstractWebFrameDepictor<C> {

	/** Default constructor using the XHTML <code>&lt;div&gt;</code> element. */
	public WebFrameDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV); //represent <xhtml:div>
	}

	/**
	 * Called when the depictor is installed in a component. This implementation listens for changes in the session and in response marks the view as needing
	 * updated.
	 * @param component The component into which this view is being installed.
	 * @throws NullPointerException if the given component is <code>null</code>.
	 * @throws IllegalStateException if this view is already installed in a component.
	 * @see #getDepictedPropertyChangeListener()
	 */
	public void installed(final C component) {
		super.installed(component); //install ourselves normally
	}

	/**
	 * Called when the depictor is uninstalled from a component. This implementation stops listening for session changes.
	 * @param component The component from which this view is being uninstalled.
	 * @throws NullPointerException if the given component is <code>null</code>.
	 * @throws IllegalStateException if this view is not installed in a component.
	 * @see #getDepictedPropertyChangeListener()
	 */
	public void uninstalled(final C component) {
		super.uninstalled(component); //uninstall ourselves normally
	}

	/**
	 * Retrieves the styles for the outer element of the component. This version combines the body styles with the outer styles.
	 * @return The styles for the outer element of the component, mapped to CSS property names.
	 * @see AbstractWebComponentDepictor#getBodyStyles()
	 */
	protected Map<String, Object> getOuterStyles() { //TODO decide if this technique is the best for the container views
		final Map<String, Object> outerStyles = super.getOuterStyles(); //get the default outer styles
		outerStyles.putAll(getBodyStyles()); //add the styles for the body
		return outerStyles; //return the combined styles		
	}

	/**
	 * Begins the rendering process.
	 * @throws IOException if there is an error rendering the component.
	 */
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		final GuiseSession session = getSession(); //get the session
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		depictContext.writeAttribute(null, ATTRIBUTE_ID, decorateID(getPlatform().getDepictIDString(component.getDepictID()), null, null)); //write the ID
		final Set<String> styleIDs = getBaseStyleIDs(null, null); //get the base style IDs
		if(component.isMovable()) { //if the frame is moveable
			styleIDs.add(DRAG_SOURCE_CLASS); //make the frame a drag source
		}
		if(component.isModal()) { //if the frame is modal
			styleIDs.add(FRAME_MODAL_CLASS); //add the class for modality
		}
		final Effect openEffect = component.getOpenEffect(); //get the frame's open effect, if there is one
		if(openEffect != null) { //if there is an open effect
			final String effectName = getSimpleName(openEffect.getClass()); //get the simple name of the effect
			final int delay = openEffect.getDelay(); //get the effect delay
			styleIDs.add("openEffect-" + effectName + "-delay-" + delay); //TODO testing; use constant
		}
		writeClassAttribute(styleIDs); //write the base style IDs		
		final Component relatedComponent = component.getRelatedComponent(); //see if this frame has a related component
		if(relatedComponent != null) { //if there is a related component
			writeParameterInputs(new NameValuePair<String, String>("relatedComponentID", getPlatform().getDepictIDString(relatedComponent.getDepictID()))); //write the component's related ID TODO use a constant
		}
		if(component instanceof FlyoverFrame) { //TODO fix; testing; create separate flyover frame view
			final FlyoverFrame flyoverFrame = (FlyoverFrame)component;
			final URI tetherImage = flyoverFrame.getTetherImage();
			if(tetherImage != null) {
				//TODO del Log.trace("rendering tether image", tetherImage);
				depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IMG, true); //<xhtml:img> (component-tether)

				final CompassPoint tetherCompassPoint = CompassPoint.getCompassPoint(flyoverFrame.getTetherBearing()); //get the compass point nearest the tether bearing
				//TODO del				final String tetherStyleSuffix="-tether-"+tetherCompassPoint.getAbbreviation();	//construct the tether style suffix TODO use a constant

				writeIDClassAttributes(null, FRAME_TETHER_CLASS_SUFFIX, "bear" + tetherCompassPoint.getAbbreviation()); //write the ID and class for the tether, along with the bearing style TODO use a constant 
				//TODO del when works				writeAttribute(null, ATTRIBUTE_ID, decorateID(component.getID(), null, "-tether"));	//write the absolute unique ID with the correct suffix TODO use constant; use convenience method
				//TODO fix				writeClassAttribute(context, closeStyleIDs);	//write the title style IDs

				/*TODO del when works				
								final Map<String, String> tetherStyles=new HashMap<String, String>();	//create a new map of styles
								tetherStyles.put("position", "absolute");
								tetherStyles.put("right", "100%");
								tetherStyles.put("bottom", "0");
								writeStyleAttribute(context, tetherStyles);	//write the body style
				*/
				//look up the tether URI, using the tether compass point abbreviation as a suffix
				depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, depictContext.getDepictionURI(tetherImage, tetherCompassPoint.getAbbreviation())
						.toString()); //src="tetherImage.gif"
				depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IMG); //</xhtml:img> (component-tether)
			}
		}

		if(component.isTitleVisible()) { //if the frame title is visible
			final Set<String> titleStyleIDs = getBaseStyleIDs(null, FRAME_TITLE_CLASS_SUFFIX); //get the base style IDs with the correct suffix
			if(component.isMovable()) { //if the frame is moveable
				titleStyleIDs.add(DRAG_HANDLE_CLASS); //make the title bar a drag handle
			}
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div> (component-title)
			final String componentIDString = getPlatform().getDepictIDString(component.getDepictID()); //get the string form of the ID
			depictContext.writeAttribute(null, ATTRIBUTE_ID, decorateID(componentIDString, null, FRAME_TITLE_CLASS_SUFFIX)); //write the absolute unique ID with the correct suffix
			writeClassAttribute(titleStyleIDs); //write the title style IDs
			writeDirectionAttribute(); //write the component direction, if this component specifies a direction
			final Map<String, Object> titleStyles = new HashMap<String, Object>(); //create a new map of styles
			titleStyles.put(CSS_PROP_POSITION, CSS_POSITION_RELATIVE); //make the title position relative so that it can be a containing block for the title components
			final Color titleBackgroundColor = component.getTitleBackgroundColor(); //get the title background color to use
			if(titleBackgroundColor != null) { //if there is a title background color
				titleStyles.put(CSS_PROP_BACKGROUND_COLOR, titleBackgroundColor); //indicate the title background color
			}
			writeStyleAttribute(titleStyles); //write the title styles
			if(hasLabelContent()) { //if there is label content TODO how can we check for a blank string here, so that the title won't disappear in those cases?
				writeLabel(decorateID(componentIDString, null, COMPONENT_BODY_CLASS_SUFFIX)); //write the label for the body, if there is a label
			} else { //if there is no label content
				depictContext.write(NO_BREAK_SPACE_CHAR); //write a non-breaking space character for the title so that IE6/7 won't make the title disappear
			}
			//controls
			final Set<String> titleControlsStyleIDs = getBaseStyleIDs(null, FRAME_TITLE_CONTROLS_CLASS_SUFFIX); //get the base style IDs with the correct suffix
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div> (component-titleControls)
			depictContext.writeAttribute(null, ATTRIBUTE_ID, decorateID(componentIDString, null, FRAME_TITLE_CONTROLS_CLASS_SUFFIX)); //write the absolute unique ID with the correct suffix TODO use a constant
			writeClassAttribute(titleControlsStyleIDs); //write the title style IDs
			writeDirectionAttribute(); //write the component direction, if this component specifies a direction
			final Map<String, Object> titleControlStyles = new HashMap<String, Object>(); //create a new map of styles
			titleControlStyles.put(CSS_PROP_DISPLAY, CSS_DISPLAY_INLINE); //make the title control wrapper inline so that a new block won't be created
			titleControlStyles.put(CSS_PROP_POSITION, CSS_POSITION_ABSOLUTE); //make the title position absolute relative to the title
			titleControlStyles.put(CSS_PROP_RIGHT, Extent.ZERO_EXTENT1); //align the right side of the controls with the right side of the title
			writeStyleAttribute(titleControlStyles); //write the title control styles
			final ActionControl closeActionControl = component.getCloseActionControl(); //get the control for closing the frame
			if(closeActionControl != null) { //if there is a control for closing the frame
				closeActionControl.depict(); //depict the close control
			}
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div> (component-titleControls)
			writeFloatClear(); //clear all floats in the title
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div> (component-title)
		}
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div> (component-body)
		writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX); //write the ID and class for the body element
	}

	/**
	 * Ends the rendering process. This version closes the decorator elements.
	 * @throws IOException if there is an error rendering the component.
	 */
	public void depictEnd() throws IOException {
		getDepictContext().writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div> (component-body)
		writeErrorMessage(); //write the error message, if any
		super.depictEnd(); //do the default ending rendering
	}
}
