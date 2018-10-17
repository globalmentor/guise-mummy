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

import java.net.URI;
import java.util.*;

import com.guiseframework.component.*;
import com.guiseframework.platform.DepictEvent;
import com.guiseframework.platform.PlatformEvent;

import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**
 * Abstract strategy for rendering a menu.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public abstract class AbstractWebMenuDepictor<C extends Menu> extends AbstractWebLayoutComponentDepictor<C> {

	/** Default constructor with no element representation. */
	public AbstractWebMenuDepictor() {
		this(null, null); //construct the strategy with no element representation
	}

	/**
	 * Element namespace and local name constructor that doesn't create an empty element, even if there is no content.
	 * @param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	 */
	public AbstractWebMenuDepictor(final URI namespaceURI, final String localName) {
		this(namespaceURI, localName, false); //don't allow an empty element
	}

	/**
	 * Element namespace and local name constructor.
	 * @param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	 * @param isEmptyElementAllowed Whether an empty element can be created if there is no content.
	 */
	public AbstractWebMenuDepictor(final URI namespaceURI, final String localName, final boolean isEmptyElementAllowed) {
		super(namespaceURI, localName, isEmptyElementAllowed); //construct the parent class
		getIgnoredProperties().remove(ActionControl.ROLLOVER_PROPERTY); //re-enable updates when rollover changes
	}

	/**
	 * Processes an event from the platform.
	 * @param event The event to be processed.
	 * @throws IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	 */
	public void processEvent(final PlatformEvent event) { //TODO combine with AbstractWebActionDepictor
		if(event instanceof WebActionDepictEvent) { //if this is an action event
			final WebActionDepictEvent webActionEvent = (WebActionDepictEvent)event; //get the web action event
			final C component = getDepictedObject(); //get the depicted object
			if(webActionEvent.getDepictedObject() != component) { //if the event was meant for another depicted object
				throw new IllegalArgumentException("Depict event " + event + " meant for depicted object " + webActionEvent.getDepictedObject());
			}
			if(component.isEnabled()) { //if the component is enabled
				component.performAction(); //tell the component to perform its action TODO implement option and perhaps force
			}
		}
		/*TODO fix for non-AJAX form posts
				if(event instanceof FormControlEvent) {	//if this is a form submission
					final FormControlEvent formEvent=(FormControlEvent)event;	//get the form submit event
					final String frameActionInputID=XHTMLApplicationFrameView.getActionInputID(component.getSession().getApplicationFrame());	//get the ID of the hidden action input field
					final String actionInputValue=asInstance(formEvent.getParameterListMap().getItem(frameActionInputID), String.class);	//get the action input value
					if(component.getID().equals(actionInputValue))	//if this action put its ID in the hidden input field
					final String componentID=getPlatform().getDepictIDString(component.getID());	//get this component's ID
					final String value=asInstance(formEvent.getParameterListMap().getItem(componentID), String.class);	//get the form value for this control
					if(componentID.equals(value)) {	//if this action put its ID as the value
						if(component.isEnabled()) {	//if the component is enabled
							component.performAction();	//tell the component to perform its action
						}
					}
				}
		*/
		super.processEvent(event); //do the default event processing
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version adds attributes based upon the menu axis and orientation, as well as whether the menu is open and/or in a rollover state.
	 * </p>
	 */
	@Override
	protected Set<String> getBaseStyleIDs(final String prefix, final String suffix) {
		final Set<String> baseStyleIDs = super.getBaseStyleIDs(prefix, suffix); //get the default base style IDs
		final C component = getDepictedObject(); //get the component
		addFlowStyleIDs(baseStyleIDs, component.getLayout().getFlow()); //add style IDs related to flow
		if(component.isOpen()) { //if this menu is open
			baseStyleIDs.add(OPEN_CLASS); //add the "open" class to the menu
		}
		if(component.isRollover()) { //if this menu is in rollover state
			baseStyleIDs.add(ROLLOVER_CLASS); //add the "rollover" class to the menu
		}
		return baseStyleIDs; //return the new style IDs
	}

	/**
	 * Retrieves the styles for the body element of the component. This version sets the z-index of menus to ensure that they cover normal content.
	 * @param context Guise context information.
	 * @param component The component for which styles should be retrieved.
	 * @return The styles for the body element of the component, mapped to CSS property names.
	 */
	/*TODO del; this must be set on the relatively positioned element, not the absolutely positioned element, to ensure correct behavior on IE)
		protected Map<String, String> getBodyStyles(final GC context, final C component)
		{
			final Map<String, String> styles=super.getBodyStyles(context, component);	//get the default body styles
	//TODO del if not needed		styles.put(CSS_PROP_Z_INDEX, Integer.toString(1));	//z-index:1 TODO create a menu layer, and use that constant
			return styles;	//return the styles
		}
	*/
}
