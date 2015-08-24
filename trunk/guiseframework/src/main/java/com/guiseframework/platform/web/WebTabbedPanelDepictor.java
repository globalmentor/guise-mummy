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

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Set;

import com.globalmentor.net.URIQueryParameter;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.model.Notification;
import com.guiseframework.platform.DepictEvent;
import com.guiseframework.platform.PlatformEvent;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.net.URIs.*;
import static com.globalmentor.w3c.spec.HTML.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**
 * Strategy for rendering a tabbed panel as a series of XHTML elements.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebTabbedPanelDepictor<C extends AbstractCardPanel> extends AbstractWebLayoutComponentDepictor<C> {

	/** Default constructor using the XHTML <code>&lt;div&gt;</code> element. */
	public WebTabbedPanelDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV); //represent <xhtml:div>
	}

	/**
	 * Processes an event from the platform.
	 * @param event The event to be processed.
	 * @throws IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	 */
	public void processEvent(final PlatformEvent event) {
		if(event instanceof WebActionDepictEvent) { //if this is an action event
			final WebActionDepictEvent webActionEvent = (WebActionDepictEvent)event; //get the web action event
			final C component = getDepictedObject(); //get the depicted object
			if(webActionEvent.getDepictedObject() != component) { //if the event was meant for another depicted object
				throw new IllegalArgumentException("Depict event " + event + " meant for depicted object " + webActionEvent.getDepictedObject());
			}
			if(component.isEnabled()) { //if the component is enabled
				final WebPlatform platform = getPlatform(); //get the platform
				final String componentID = getPlatform().getDepictIDString(component.getDepictID()); //get the component's ID
				final String targetID = webActionEvent.getTargetID(); //get the target ID, if any
				if(targetID != null && targetID.startsWith(componentID + '-')) { //if the target starts with the component ID TODO use a constant
					final String childComponentIDString = targetID.substring((componentID + '-').length()); //get the child component ID string TODO use a constant
					final long childComponentID = platform.getDepictID(childComponentIDString); //get the child component ID
					for(final Component tabComponent : component) { //look at each child component
						if(tabComponent.getDepictID() == childComponentID) { //if this is the identified child component
							final CardConstraints constraints = component.getLayout().getConstraints(tabComponent); //get the constraints for this tab
							if(constraints.isEnabled()) { //if this tab is enabled
								try {
									component.setNotification(null); //clear the component errors; this method may generate new errors
									component.setValue(tabComponent); //switch to the specified tab component
								} catch(final PropertyVetoException propertyVetoException) { //if there is a veto
									final Throwable cause = propertyVetoException.getCause(); //get the cause of the veto, if any
									component.setNotification(new Notification(cause != null ? cause : propertyVetoException)); //add notification of the error to the component
								}
							}
						}
					}
				}
			}
		} else if(event instanceof WebFormEvent) { //if this is a form submission, there will be a query string indicating the tab
			final WebFormEvent formEvent = (WebFormEvent)event; //get the form submit event
			final WebPlatform platform = getPlatform(); //get the platform
			final C component = getDepictedObject(); //get the component
			final String componentID = platform.getDepictIDString(component.getDepictID()); //get the component's ID
			final String tabID = asInstance(formEvent.getParameterListMap().getItem(componentID), String.class); //get the value reported for this component, if there is one
			if(tabID != null) { //if a tab is indicated TODO maybe indicate "no tab" with an empty string
				final long childComponentID = platform.getDepictID(tabID); //get the requested component ID
				for(final Component tabComponent : component) { //look at each child component
					if(tabComponent.getDepictID() == childComponentID) { //if this is the identified child component
						if(component.isEnabled()) { //if the component is enabled
							final CardConstraints constraints = component.getLayout().getConstraints(tabComponent); //get the constraints for this tab
							assert constraints != null : "No constraints found for tab component " + tabComponent;
							if(constraints.isEnabled()) { //if this tab is enabled
								try {
									component.setNotification(null); //clear the component errors; this method may generate new errors
									component.setValue(tabComponent); //switch to the specified tab component
								} catch(final PropertyVetoException propertyVetoException) { //if there is a veto
									final Throwable cause = propertyVetoException.getCause(); //get the cause of the veto, if any
									component.setNotification(new Notification(cause != null ? cause : propertyVetoException)); //add notification of the error to the component
								}
							}
						}
					}
				}
			}
		}
		super.processEvent(event); //do the default event processing
	}

	/**
	 * Begins the rendering process. This version wraps the component in a decorator element and writes tabs. Each tab link is given an href of
	 * "?<var>tabbedPaneID</var>=<var>tabID</var>".
	 * @throws IOException if there is an error rendering the component.
	 * @throws IllegalArgumentException if the given value control represents a value type this controller doesn't support.
	 */
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		writeIDClassAttributes(null, null); //write the ID and class attributes with no prefixes or suffixes
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_OL); //<xhtml:ol> (component-tabs)
		writeIDClassAttributes(null, TABBED_PANEL_TABS_CLASS_SUFFIX); //write the ID and class for the tabs
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction
		final String componentID = getPlatform().getDepictIDString(component.getDepictID()); //get the ID of the tabbed panel
		final CardLayout layout = component.getLayout(); //get the component layout
		final Component selectedCard = layout.getValue(); //get the selected card, if any
		for(final Component childComponent : component) { //for each child component
			final String childComponentID = getPlatform().getDepictIDString(childComponent.getDepictID()); //get the absolute unique ID of the tab component
			final boolean isSelectedCard = childComponent == selectedCard; //see if this is the selected component
			final CardConstraints constraints = layout.getConstraints(childComponent); //get the constraints for this component
			assert constraints != null : "No constraints found for component " + childComponent;
			if(hasLabelContent(constraints)) { //if there is label content
				final Set<String> tabStyleIDs = getBaseStyleIDs(null, TABBED_PANEL_TAB_CLASS_SUFFIX); //get the set of tab style IDs
				if(isSelectedCard) { //if this tab is selected
					tabStyleIDs.add(SELECTED_CLASS); //add the selected class ID
				}
				if(!constraints.isEnabled()) { //if this tab is disabled
					tabStyleIDs.add(DISABLED_CLASS); //add the disabled class ID
				}
				//TODO fix tab displayed status
				depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_LI); //<xhtml:li> (component-tab)
				writeDirectionAttribute(); //write the component direction, if this component specifies a direction
				writeClassAttribute(tabStyleIDs); //write the base style IDs with a "-tab" (or "-tab-selected") suffix
				depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_A); //<xhtml:a> (component-tab)
				writeIDAttribute(null, "-" + childComponentID); //id="componentID-childComponentID"
				tabStyleIDs.add(ACTION_CLASS); //allow the tab link to be an action
				writeClassAttribute(tabStyleIDs); //write the base style IDs with a "-tab" (or "-tab-selected") suffix
				writeDirectionAttribute(); //write the component direction, if this component specifies a direction
				final String query = constructQuery(new URIQueryParameter(componentID, childComponentID)); //construct a query in the form "tabbedPanelID=tabID"
				//TODO del				final URI panelComponentURI=resolveFragment(null, childComponentID);	//create a fragment URI link to the component, even if it isn't showing
				depictContext.writeAttribute(null, ELEMENT_A_ATTRIBUTE_HREF, query); //write the href attribute to the tab component
				writeLabelContent(constraints); //write the content of the label
				depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_A); //</xhtml:a> (component-tab)
				depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_LI); //</xhtml:li> (component-tab)
				depictContext.write(' '); //TODO testing; separate the list items
			}
		}
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_OL); //</xhtml:ol> (component-tabs)
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div> (component-body)
		writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX); //write the ID and class attributes for the body		
		writeStyleAttribute(getBodyStyles()); //write the component's body styles
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
