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
import java.util.*;

import static com.globalmentor.java.Objects.*;
import com.globalmentor.log.Log;

import static com.globalmentor.w3c.spec.HTML.*;

import com.guiseframework.component.*;
import com.guiseframework.model.*;
import com.guiseframework.platform.*;
import com.guiseframework.validator.*;

/**
 * Strategy for rendering a select control as an XHTML <code>&lt;select&gt;</code> element.
 * @param <V> The type of values to select.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebSelectDepictor<V, C extends ListSelectControl<V>> extends AbstractDecoratedWebComponentDepictor<C> {

	/** The value for indicating a dummy <code>null</code> value when no value is selected. */
	protected static final String GUISE_DUMMY_NULL_VALUE = "$guiseDummyNull"; //TODO del if not needed

	/** Default constructor using the XHTML <code>&lt;select&gt;</code> element. */
	public WebSelectDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_SELECT); //represent <xhtml:select>
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
			final List<?> selectedIDObjects = asInstance(properties.get("selectedIDs"), List.class); //get the new selected IDs TODO use a constant
			if(selectedIDObjects != null) { //if we have selected IDs
				processSelectedIDs(component, selectedIDObjects.toArray(new String[selectedIDObjects.size()])); //process the selected IDs as strings				
			}
		} else if(event instanceof WebFormEvent) { //if this is a form submission
			final WebFormEvent formEvent = (WebFormEvent)event; //get the form submit event
			final String componentName = getDepictName(); //get the component's name
			if(componentName != null) { //if this component has a name
				final List<?> selectedIDs = formEvent.getParameterListMap().get(componentName); //get the value IDs reported for this component
				if(selectedIDs != null) { //if there are values
					processSelectedIDs(getDepictedObject(), selectedIDs.toArray(new String[selectedIDs.size()])); //process the selected IDs				
				}
			}
		}
		super.processEvent(event); //do the default event processing
	}

	/**
	 * Updates the selection of a list select control based upon the selected IDs sent from the web platform.
	 * @param <V> The type of value contained in the control.
	 * @param selectControl The select control.
	 * @param selectedIDs The array of new selected IDs, each identifying a representation component of the value.
	 * @throws NullPointerException if the given control and/or selected IDs is <code>null</code>.
	 */
	public static <V> void processSelectedIDs(final ListSelectControl<V> selectControl, final String[] selectedIDs) {
		final WebPlatform platform = (WebPlatform)checkInstance(selectControl, "Component cannot be null.").getSession().getPlatform(); //get the platform
		checkInstance(selectedIDs, "SelectedIDs cannot be null.");
		selectControl.setNotification(null); //clear the component errors; this method may generate new errors
		final List<Integer> selectedIndexList = new ArrayList<Integer>(selectedIDs.length); //create a list to hold selected indices
		final ListSelectControl.ValueRepresentationStrategy<V> representationStrategy = selectControl.getValueRepresentationStrategy(); //get the component's value representation strategy
		synchronized(selectControl) { //don't allow the model to be modified while we access it (we'll have to iterate over every value in the model) TODO change to using a lock
			int valueIndex = -1; //keep track of the index of each model value
			for(final V value : selectControl) { //for each value in the model
				final String valueID = platform.getDepictIDString(selectControl.getComponent(value).getDepictID()); //get the ID of this value's representation component
				++valueIndex; //show that we're looking at the next index in the model
				int selectedIDIndex = -1; //keep track of the index of the selected ID
				for(final String selectedID : selectedIDs) { //look at each selected ID
					++selectedIDIndex; //indicate that we're looking at the next selected ID index
					if(valueID.equals(selectedID)) { //if we found the value the ID matches
						if(selectControl.isIndexEnabled(valueIndex)) { //if this index is enabled
							selectedIndexList.add(new Integer(valueIndex)); //store the index of the value it represents
						}
					}
				}
			}
		}
		final int[] selectedIndices = new int[selectedIndexList.size()]; //create an array to hold the selected indices we found
		if(selectedIndices.length > 0) { //if there are really selected indices
			int i = -1; //we'll keep track of the array index
			for(final Integer selectedIndex : selectedIndexList) { //for each selected index
				++i; //go to the next array index
				selectedIndices[i] = selectedIndex.intValue(); //store this selected index in the array
			}
		}
		try {
			Log.trace("ready to set selected indexes to", Arrays.toString(selectedIndices));
			selectControl.setSelectedIndexes(selectedIndices); //store the decoded value in the model
		} catch(final PropertyVetoException propertyVetoException) { //if there is a veto
			final Throwable cause = propertyVetoException.getCause(); //get the cause of the veto, if any
			selectControl.setNotification(new Notification(cause != null ? cause : propertyVetoException)); //add notification of the error to the component
		}
	}

	@Override
	protected void depictBegin() throws IOException {
		//TODO del Log.trace("updating select view");
		super.depictBegin(); //do the default beginning rendering
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		depictContext.writeAttribute(null, ATTRIBUTE_NAME, getPlatform().getDepictIDString(component.getDepictID())); //write the component ID in the XHTML name attribute
		if(!component.isEnabled()) { //if the component's model is not enabled
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_DISABLED, INPUT_DISABLED_DISABLED); //disabled="disabled"			
		}
		final ListSelectionPolicy<V> selectionStrategy = component.getSelectionPolicy(); //see what sort of selection this model allows
		final boolean isSingleSelection = selectionStrategy instanceof SingleListSelectionPolicy; //see if a single selection is being used
		if(!isSingleSelection) { //if anything besides a single-selection strategy is installed, assume the model will implement its own selection policy, so allow multiple selections
			depictContext.writeAttribute(null, ELEMENT_SELECT_ATTRIBUTE_MULTIPLE, SELECT_MULTIPLE_MULTIPLE); //multiple="multiple"
		}
		final int rowCount; //see what row count is specified, it any
		if(ListControl.class.isInstance(component)) { //if the component is a list control, add the select-specific attributes
			final ListControl<?> listControl = ListControl.class.cast(component); //get the component as a list control
			rowCount = listControl.getRowCount(); //get the row count
			if(rowCount >= 0) { //if a valid row count is given
				depictContext.writeAttribute(null, ELEMENT_SELECT_ATTRIBUTE_SIZE, Integer.toString(rowCount)); //size="rowCount"							
			}
		} else { //if this is not a list control
			rowCount = -1; //no row count was specified
		}
		final V[] selectedValues = component.getSelectedValues(); //get the selected values
		//TODO del Log.trace("selected values count:", selectedValues.length);
		final Set<String> selectedIDs = new HashSet<String>(selectedValues.length); //create a set to contain all selected IDs
		for(final V selectedValue : selectedValues) { //for each selected value
			selectedIDs.add(getPlatform().getDepictIDString(component.getComponent(selectedValue).getDepictID())); //get the ID of this value's representation component
		}
		//TODO del Log.trace("selected IDs count:", selectedIDs.size());
		final Validator<V> validator = component.getValidator(); //get the model's validator
		final boolean valueRequired = validator != null && validator instanceof AbstractValidator && ((AbstractValidator<V>)validator).isValueRequired(); //see if a value is required
		//TODO del Log.trace("size:", component.size(), "isEmpty", component.isEmpty());
		int optionsWritten = 0; //keep track of how many options we've written
		if(!component.isEmpty()) { //if there are options, determine whether to add a dummy null option (if there are no options, there's no point in having a "no options" option)
		/*TODO del when works
					if((isSingleSelection && !valueRequired)	//for single selection controls, if there is no value required we need to have a way for the user to unselect everything (presumably a multiple selection would inherently allow this)
							|| (selectedIDs.size()==0 && rowCount<=1))	//if no IDs are selected and not more than one row is showing (HTML defaults to a single row if there is no row count specified)
		*/
			//TODO decide if we want to keep the new logic; apparently even single-row controls allow value removal using the Ctrl key
			if(isSingleSelection && (selectedIDs.size() == 0 || !valueRequired)) { //for single selection controls, if there is no value required we need to have a way for the user to unselect everything (presumably a multiple selection would inherently allow this)
			//TODO del Log.trace("writing dummy null!");
				depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_OPTION); //<xhtml:option>
				depictContext.writeAttribute(null, ATTRIBUTE_VALUE, GUISE_DUMMY_NULL_VALUE); //value="guiseDummyNull"
				if(selectedIDs.size() == 0) { //if no items are selected, select the dummy null option
					depictContext.writeAttribute(null, ELEMENT_OPTION_ATTRIBUTE_SELECTED, OPTION_SELECTED_SELECTED); //selected="selected"
				}
				final Component representationComponent = component.getComponent(null); //get a component to represent null TODO check the index
				final String valueLabel = representationComponent.getLabel(); //get the component label, if there is one
				if(valueLabel != null) { //if there is a label for this value
					depictContext.write(representationComponent.getSession().dereferenceString(valueLabel)); //write the label for this item
					//TODO del if causing problems					representationComponent.getView().setUpdated(true);	//because the child component view isn't updating itself, tell that the view is updated so that the view won't be sent back during page initialization
				}
				depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_OPTION); //</xhtml:option>
				++optionsWritten; //indicate that we wrote another option
			}
		}
		synchronized(component) { //don't allow the model to be modified while we access it
			int index = -1; //keep track of each index in the model
			for(final V value : component) { //for each value in the component
				++index; //increment the index
				final Component representationComponent = component.getComponent(value); //create a component to represent the value
				final String valueID = getPlatform().getDepictIDString(representationComponent.getDepictID()); //get the ID of this value's representation component
				depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_OPTION); //<xhtml:option>
				depictContext.writeAttribute(null, ATTRIBUTE_VALUE, valueID); //value="componentID"
				final boolean selected = selectedIDs != null && selectedIDs.contains(valueID); //see if this value is selected
				if(selected) { //if we have this component's ID in the list of selected IDs
					depictContext.writeAttribute(null, ELEMENT_OPTION_ATTRIBUTE_SELECTED, OPTION_SELECTED_SELECTED); //selected="selected"			
				}
				final String valueLabel = representationComponent.getLabel(); //get the component label, if there is one
				if(valueLabel != null) { //if there is a label for this value
					depictContext.write(representationComponent.getSession().dereferenceString(valueLabel)); //write the label for this item					
				}
				//TODO do error handling here if the model is not a label model
				depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_OPTION); //</xhtml:option>
				++optionsWritten; //indicate that we wrote another option
			}
		}
		if(optionsWritten == 0) { //if no options were written, we must write a dummy option, as HTML select controls must have at least one option, and IE7 will lose an empty select element from the DOM; see http://www.w3.org/TR/html4/interact/forms.html#edef-OPTION
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_OPTION); //<xhtml:option>
			depictContext.writeAttribute(null, ATTRIBUTE_VALUE, ""); //value=""
			depictContext.writeAttribute(null, ELEMENT_OPTION_ATTRIBUTE_DISABLED, OPTION_DISABLED_DISABLED); //disabled="disabled"			
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_OPTION); //</xhtml:option>
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version does nothing, because if a list select control is a composite component the child controls have already been rendered as values in
	 * {@link #depictBody()}.
	 * </p>
	 */
	@Override
	protected void depictChildren() throws IOException {
	}

}
