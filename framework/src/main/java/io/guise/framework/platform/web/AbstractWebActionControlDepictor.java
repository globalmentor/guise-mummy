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

import java.net.URI;
import java.util.*;

import io.guise.framework.component.*;
import io.guise.framework.platform.*;

import static com.globalmentor.css.spec.CSS.*;
import static io.guise.framework.platform.web.GuiseCSSStyleConstants.*;

/**
 * Abstract depictor for rendering simple action controls in XHTML. By default this implementation ignores changes in the
 * {@link ActionControl#ROLLOVER_PROPERTY} property when determining whether to update the control. Depictors that wish to update the control upon rollover
 * change must (besides implementing changing of the rollover property) remove the rollover property from the list of ignored properties,
 * {@link #getIgnoredProperties()}.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public abstract class AbstractWebActionControlDepictor<C extends ActionControl> extends AbstractSimpleWebComponentDepictor<C> {

	/** Default constructor with no element representation. */
	public AbstractWebActionControlDepictor() {
		this(null, null); //construct the strategy with no element representation
	}

	/**
	 * Element namespace and local name constructor that doesn't create an empty element, even if there is no content.
	 * @param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	 */
	public AbstractWebActionControlDepictor(final URI namespaceURI, final String localName) {
		this(namespaceURI, localName, false); //don't allow an empty element
	}

	/**
	 * Element namespace and local name constructor.
	 * @param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	 * @param isEmptyElementAllowed Whether an empty element can be created if there is no content.
	 */
	public AbstractWebActionControlDepictor(final URI namespaceURI, final String localName, final boolean isEmptyElementAllowed) {
		super(namespaceURI, localName, isEmptyElementAllowed); //construct the parent class
		getIgnoredProperties().add(ActionControl.ROLLOVER_PROPERTY); //ignore rollover property changes by default (even though rollover isn't even implemented by default) because rollovers are expensive and action controls won't by default update the control when rollover occurs
	}

	@Override
	public void processEvent(final PlatformEvent event) {
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
	 * This version adds an attribute for selected status if the control is a select action control with a selected model. This version also adds an attribute if
	 * the component is disabled.
	 * </p>
	 */
	@Override
	protected Set<String> getBaseStyleIDs(final String prefix, final String suffix) {
		final Set<String> baseStyleIDs = super.getBaseStyleIDs(prefix, suffix); //get the default base style IDs
		if(!getDepictedObject().isEnabled()) { //if this component is disabled	//TODO maybe add an Enableable interface
			baseStyleIDs.add(DISABLED_CLASS); //add the disabled class ID
		}
		return baseStyleIDs; //return the new style IDs
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version lowers the opacity if the control is disabled.
	 * </p>
	 */
	@Override
	protected Map<String, Object> getBodyStyles() {
		final Map<String, Object> styles = super.getBodyStyles(); //get the default body styles
		final C component = getDepictedObject(); //get the component
		if(!component.isEnabled()) { //if this component is disabled
			final double opacity = component.getOpacity(); //get the component's opacity
			if(opacity == 1.0) { //if there is no custom opacity (i.e. don't override any custom opacity)
				styles.put(CSS_PROP_OPACITY, Double.valueOf(0.5)); //lower the opacity
			}
		}
		return styles; //return the styles
	}

	/**
	 * Writes any action parameters as comments. This methods writes a comment containing the confirmation message, if any.
	 * @param context Guise context information.
	 * @param component The component being rendered.
	 * @throws IOException if there is an error rendering the component.
	 */
	/*TODO del; transfer to another technique if needed
		protected void writeParameters(final GC context, final C component) throws IOException {	//TODO replace with better parameters; probably remove confirmation altogether
			final MessageModel confirmationMessage=component.getModel().getConfirmationMessage();	//get the action confirmation message, if there is one
			if(confirmationMessage!=null) {	//if there is a confirmation message
				final String message=confirmationMessage.getMessage();	//get the actual message
				if(message!=null) {	//if a message is given
					context.writeComment("confirm:"+AbstractModel.getPlainText(message, confirmationMessage.getMessageContentType()));	//confirm:confirmMessage TODO use constants; perhaps the confirm property, and a common routine for adding parameters
				}
			}
		}
	*/

}
