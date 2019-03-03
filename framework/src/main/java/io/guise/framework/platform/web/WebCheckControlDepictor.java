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

import java.beans.*;
import java.io.IOException;
import java.util.*;

import io.guise.framework.component.*;
import io.guise.framework.model.*;
import io.guise.framework.platform.DepictEvent;
import io.guise.framework.platform.PlatformEvent;

import static java.util.Collections.*;
import static com.globalmentor.html.spec.HTML.*;
import static com.globalmentor.java.Booleans.*;
import static com.globalmentor.java.Objects.*;
import static io.guise.framework.platform.web.GuiseCSSStyleConstants.*;

/**
 * Strategy for rendering a check control as an XHTML <code>&lt;input&gt;</code> element with type <code>checkbox</code> or <code>radio</code>.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebCheckControlDepictor<C extends CheckControl> extends AbstractWebComponentDepictor<C> {

	/** The weak map of IDs for mutual exclusion groups. The map is weak so as not to tie up memory for groups that are no longer used. */
	private static Map<MutualExclusionPolicyModelGroup, Long> mutualExclusionGroupIDMap = synchronizedMap(
			new WeakHashMap<MutualExclusionPolicyModelGroup, Long>());

	/**
	 * Determines the ID for the mutual exclusion group. If no ID has been assigned to the given group, one will be generated and associated with the group.
	 * @param mutualExclusionModelGroup The model policy group for mutual exclusion for which its ID should be given.
	 * @return The component identifier for the mutual exclusion group.
	 */
	protected long getMutualExclusionGroupID(final MutualExclusionPolicyModelGroup mutualExclusionModelGroup) {
		Long idLong = mutualExclusionGroupIDMap.get(mutualExclusionModelGroup); //see if there is already an ID for this group
		if(idLong == null) {
			idLong = Long.valueOf(getPlatform().generateDepictID()); //generate an ID for this group TODO important fix; this can create duplicate IDs across sessions
			mutualExclusionGroupIDMap.put(mutualExclusionModelGroup, idLong); //associate the ID with this group
		}
		return idLong.longValue(); //return the ID for the group
	}

	/**
	 * Determines whether the check control is a mutual exclusion control and should be considered a radio button rather than a checkbox.
	 * @param checkControl The component which should be determined to be mutual exclusion or not.
	 * @return <code>true</code> if the given component has a {@link MutualExclusionPolicyModelGroup} value listener.
	 * @see MutualExclusionPolicyModelGroup
	 */
	protected static boolean isMutualExclusion(final CheckControl checkControl) {
		//TODO important: change to a method of mutual exclusion determination that works for nested value models, such as are used with value prototypes
		for(final PropertyChangeListener valueChangeListener : checkControl.getPropertyChangeListeners(ValueModel.VALUE_PROPERTY)) { //examine all objects listening for value changes
			if(valueChangeListener instanceof MutualExclusionPolicyModelGroup) { //if these models are all part of a mutual exclusion group i.e. radio button-style functionality)
				return true; //this is a mutual exclusion check control
			}
		}
		return false; //there is no mutual exclusion policy group creating mutual exclusion
	}

	/**
	 * Determines the identifier to place in the name attribute of the component's XHTML element. For XHTML radio buttons, a unique identifier for its mutual
	 * exclusion policy model group is returned; the component ID is returned if this is not a mutual exclusion component.
	 * @param checkControl The component for which a name should be retrieved.
	 * @return An identifier appropriate for the name attribute of the component's XHTML element, or <code>null</code> if the component's element should not have
	 *         a name.
	 * @see MutualExclusionPolicyModelGroup
	 */
	protected String getCheckControlName(final CheckControl checkControl) {
		final WebPlatform platform = getPlatform(); //get the platform
		//TODO important: change to a method of mutual exclusion determination that works for nested value models, such as are used with value prototypes
		for(final PropertyChangeListener valueChangeListener : checkControl.getPropertyChangeListeners(ValueModel.VALUE_PROPERTY)) { //examine all objects listening for value changes
			if(valueChangeListener instanceof MutualExclusionPolicyModelGroup) { //if these models are all part of a mutual exclusion group i.e. radio button-style functionality)
				final MutualExclusionPolicyModelGroup mutualExclusionModelGroup = (MutualExclusionPolicyModelGroup)valueChangeListener; //cast the listener to a mutual exclusion group
				return platform.getDepictIDString(getMutualExclusionGroupID(mutualExclusionModelGroup)); //return the string form of the ID of this group
			}
		}
		return platform.getDepictIDString(checkControl.getDepictID()); //return the component ID if there is no special group name needed
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version combines the body styles with the outer styles.
	 * </p>
	 */
	@Override
	protected Map<String, Object> getOuterStyles() {
		final Map<String, Object> outerStyles = super.getOuterStyles(); //get the default outer styles
		outerStyles.putAll(getBodyStyles()); //add the styles for the body
		return outerStyles; //return the combined styles		
	}

	/** Default constructor using the XHTML <code>&lt;span&gt;</code> element. */
	public WebCheckControlDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_SPAN); //represent <xhtml:span>
	}

	/**
	 * Determines the identifier to place in the name attribute of the component's XHTML element. For XHTML radio buttons, a unique identifier for its mutual
	 * exclusion policy model group is returned; the component ID is returned if this is not a mutual exclusion component.
	 * @return An identifier appropriate for the name attribute of the component's XHTML element, or <code>null</code> if the component's element should not have
	 *         a name.
	 * @see #getCheckControlName(CheckControl)
	 */
	public String getDepictName() { //TODO why is this is a separate method? now that controller and viewer are merged, probably merge the getCheckControlName() code back into this method
		return getCheckControlName(getDepictedObject()); //return the name of the check control, taking into account mutual exclusion groups
	}

	@Override
	public void processEvent(final PlatformEvent event) {
		if(event instanceof WebChangeDepictEvent) { //if a property changed
			final WebChangeDepictEvent webChangeEvent = (WebChangeDepictEvent)event; //get the web change event
			final C component = getDepictedObject(); //get the depicted object
			if(webChangeEvent.getDepictedObject() != component) { //if the event was meant for another depicted object
				throw new IllegalArgumentException("Depict event " + event + " meant for depicted object " + webChangeEvent.getDepictedObject());
			}
			final Map<String, Object> properties = webChangeEvent.getProperties(); //get the new properties
			asInstance(properties.get("value"), Boolean.class).ifPresent(value -> { //get the new value TODO use a constant
				try {
					component.setNotification(null); //clear the component errors; this method may generate new errors
					component.setValue(value); //store the value in the model; this will throw an exception if the value is invalid
				} catch(final PropertyVetoException propertyVetoException) { //if there is a veto
					final Throwable cause = propertyVetoException.getCause(); //get the cause of the veto, if any
					component.setNotification(new Notification(cause != null ? cause : propertyVetoException)); //add notification of the error to the component
				}
			});
		}
		/*TODO fix for non-AJAX posts
				if(event instanceof FormControlEvent) {	//if this is a form submission
					final FormControlEvent formEvent=(FormControlEvent)event;	//get the form submit event
					final String componentName=getComponentName(component);	//get the component's name
					if(componentName!=null) {	//if there is a component name
						final String parameterValue=asInstance(formEvent.getParameterListMap().getItem(componentName), String.class);	//get the form value for this control
						if(parameterValue!=null || formEvent.isExhaustive()) {	//if there was a parameter value for this component, or this was an event for all the form's controls (checkboxes and radio buttons send back nothing for exhaustive form events if the value is false)
							final Boolean value=Boolean.valueOf(getPlatform().getDepictIDString(component.getID()).equals(parameterValue));	//the value of the control is true if the encoded value is the component's absolute unique ID (the paramter value could be null)
							try
							{
								component.setNotification(null);	//clear the component errors; this method may generate new errors
								component.setValue(value);	//store the value in the model; this will throw an exception if the value is invalid
							}
							catch(final PropertyVetoException propertyVetoException) {	//if there is a veto
								final Throwable cause=propertyVetoException.getCause();	//get the cause of the veto, if any
								component.setNotification(new Notification(cause!=null ? cause : propertyVetoException));	//add notification of the error to the component
							}
						}
					}
				}
		*/
		super.processEvent(event); //do the default event processing
	}

	/**
	 * Determines the XHTML input type to use for getting input from the user.
	 * @return One of the XHTML input types.
	 */
	protected String getInputType() {
		final C component = getDepictedObject(); //get the component
		final CheckControl.CheckType checkType = component.getCheckType(); //see which type of check the component wants to use
		if(checkType != null) { //if a check type is specified
			switch(checkType) { //see which type of check to use
				case RECTANGLE:
					return INPUT_TYPE_CHECKBOX; //use a checkbox input type				
				case ELLIPSE:
					return INPUT_TYPE_RADIO; //use a radio input type				
				default: //if we don't recognize the check type, this class isn't up-to-date with the CheckControl.CheckType class
					throw new AssertionError("Unrecognized " + checkType.getClass() + " value: " + checkType);
			}
		} else { //if no check type is specified
			return isMutualExclusion(component) ? INPUT_TYPE_RADIO : INPUT_TYPE_CHECKBOX; //appear as a radio button if this is a mutual exclusion control
		}
	}

	@Override
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering		
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		writeIDClassAttributes(null, null); //write the ID and class attributes with no prefixes or suffixes
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_INPUT, true); //<xhtml:input>
		writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX); //write the ID and class for the control element
		//TODO del if not needed		writeStyleAttribute(context, getBodyStyles(context, component));	//write the component's body styles
		depictContext.writeAttribute(null, ATTRIBUTE_NAME, getDepictName()); //write the appropriate component name in the XHTML name attribute
		depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_TYPE, getInputType()); //type="inputType"
		if(!component.isEnabled()) { //if the component's model is not enabled
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_DISABLED, INPUT_DISABLED_DISABLED); //disabled="disabled"			
		}
		if(!component.isEditable()) { //if the component's model is not editable
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_READONLY, INPUT_READONLY_READONLY); //readonly="readonly"			
		}
		depictContext.writeAttribute(null, ATTRIBUTE_VALUE, getPlatform().getDepictIDString(component.getDepictID())); //value="id" (always write the ID as the value, whether the control is checked or not)
		if(booleanValue(component.getValue())) { //if the model has a value of true
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_CHECKED, INPUT_CHECKED_CHECKED); //checked="checked"				
		}
	}

	@Override
	protected void depictEnd() throws IOException {
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_INPUT); //</xhtml:input>
		writeLabel(decorateID(getPlatform().getDepictIDString(getDepictedObject().getDepictID()), null, COMPONENT_BODY_CLASS_SUFFIX)); //write the check label, if there is one
		writeErrorMessage(); //write the error message, if any
		super.depictEnd(); //do the default ending rendering
	}

}
