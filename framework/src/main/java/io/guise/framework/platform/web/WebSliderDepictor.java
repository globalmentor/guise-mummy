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

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.*;
import java.net.URI;
import java.util.*;

import io.guise.framework.GuiseSession;
import io.guise.framework.component.*;
import io.guise.framework.component.layout.*;
import io.guise.framework.geometry.Axis;
import io.guise.framework.model.Notification;
import io.guise.framework.platform.*;
import io.guise.framework.validator.*;

import static java.util.Objects.*;
import static java.util.function.Predicate.*;
import static com.globalmentor.java.Numbers.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.w3c.spec.CSS.*;
import static com.globalmentor.w3c.spec.HTML.*;
import static io.guise.framework.platform.web.GuiseCSSStyleConstants.*;

/**
 * Strategy for rendering a slider component as an XHTML <code>&lt;slider&gt;</code> element.
 * @param <V> The type of values to select.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebSliderDepictor<V extends Number, C extends SliderControl<V>> extends AbstractWebComponentDepictor<C> {

	/** Default constructor using the XHTML <code>&lt;div&gt;</code> element. */
	public WebSliderDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV); //represent <xhtml:div>
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
			asInstance(properties.get("position"), Number.class).ifPresent(position -> processPosition(component, position.doubleValue())); //process the new position TODO use a constant
		} else if(event instanceof WebFormEvent) { //if this is a form submission
			final WebFormEvent formEvent = (WebFormEvent)event; //get the form submit event
			final String componentName = getDepictName(); //get the component's name
			if(componentName != null) { //if there is a component name
				asInstance(formEvent.getParameterListMap().getItem(componentName), String.class).filter(not(String::isEmpty)) //get the form value for this control
						.ifPresent(text -> { //if there was a parameter value for this component
							processPosition(getDepictedObject(), Double.parseDouble(text)); //process the new position
						});
			}
		} else if(event instanceof WebActionDepictEvent) { //if this is an action
			final WebActionDepictEvent webActionEvent = (WebActionDepictEvent)event; //get the action control event
			final C component = getDepictedObject(); //get the component
			if(webActionEvent.getDepictedObject() != component) { //if the event was meant for another depicted object
				throw new IllegalArgumentException("Depict event " + event + " meant for depicted object " + webActionEvent.getDepictedObject());
			}
			final String actionID = webActionEvent.getActionID(); //get the action ID
			if("slideBegin".equals(actionID)) { //if sliding is beginning TODO use a constant
				component.setSliding(true); //turn sliding on
			} else if("slideEnd".equals(actionID)) { //if sliding is ending TODO use a constant
				component.setSliding(false); //turn sliding off
				//TODO del Log.trace("Received slider action: "+actionEvent.getActionID());
			}
		}
		super.processEvent(event); //do the default event processing
	}

	/**
	 * Updates the position of a slider control based upon the relative position sent from the web platform.
	 * @param <V> The type of value contained in the control.
	 * @param sliderControl The slider control.
	 * @param position The relative position of the slider.
	 * @throws NullPointerException if the given control and/or value class is <code>null</code>.
	 */
	public static <V extends Number> void processPosition(final SliderControl<V> sliderControl, final double position) {
		final Validator<V> validator = requireNonNull(sliderControl, "Component cannot be null.").getValidator(); //get the model's validator
		final Class<V> valueClass = sliderControl.getValueClass(); //see what type of class is represented in the model
		final RangeValidator<V> rangeValidator = validator instanceof RangeValidator ? (RangeValidator<V>)validator : null; //get the validator as a range validator, if it is one
		final V rangeMinimum = rangeValidator != null ? rangeValidator.getMinimum() : null; //find the minumum, if there is one
		final V rangeMaximum = rangeValidator != null ? rangeValidator.getMaximum() : null; //find the maximum, if there is one
		final V rangeStep = rangeValidator != null ? rangeValidator.getStep() : null; //find the step, if there is one
		final Number newValue; //we'll calculate the new value
		if(Integer.class.isAssignableFrom(valueClass) || Long.class.isAssignableFrom(valueClass)) { //integer or long
			final long minimum = rangeMinimum != null ? rangeMinimum.longValue() : 0; //get the minimum to use for calculations 
			final long maximum = rangeMaximum != null ? rangeMaximum.longValue() : 100; //get the maximum to use for calculations 
			final long range = maximum - minimum; //calculate the range
			final long step = rangeStep != null ? rangeStep.longValue() : 1; //get the step to use for calculations
			final double pureValue = range * position + ((double)step / 2); //calculate the pure value without accounting for the base, and add half a step to allow rounding to the nearest step
			final long newLongValue = (long)(pureValue - (pureValue % step) + minimum); //find the value that takes into account the minimum value and the step value
			if(Integer.class.isAssignableFrom(valueClass)) { //if the value is an integer
				newValue = Integer.valueOf((int)newLongValue); //use an integer
			} else if(Long.class.isAssignableFrom(valueClass)) { //if the value is a long
				newValue = Long.valueOf(newLongValue); //use a long
			} else { //we should have covered all the subtypes for which we use longs
				throw new AssertionError("Neglected to support integer type: " + valueClass);
			}
		} else if(Float.class.isAssignableFrom(valueClass) || Double.class.isAssignableFrom(valueClass) || BigInteger.class.isAssignableFrom(valueClass)) { //decimal types
			final BigDecimal bigMinimum = rangeMinimum != null ? toBigDecimal(rangeMinimum) : BigDecimal.valueOf(0); //get the minimum to use for calculations 
			final BigDecimal bigMaximum = rangeMaximum != null ? toBigDecimal(rangeMaximum) : BigDecimal.valueOf(100); //get the maximum to use for calculations 
			final BigDecimal bigRange = bigMaximum.subtract(bigMinimum); //calculate the range
			final BigDecimal bigStep = rangeStep != null ? toBigDecimal(rangeStep) : BigDecimal.valueOf(1); //get the step to use for calculations
			//TODO check this next line; it has thrown an Infinite or NaN NumberFormatException
			final BigDecimal pureValue = bigRange.multiply(new BigDecimal(position)).add(bigStep.divide(BigDecimal.valueOf(2))); //calculate the pure value without accounting for the base, and add half a step to allow rounding to the nearest step
			final BigDecimal bigFactor = new BigDecimal(pureValue.divide(bigStep).intValue()); //find out how many times the step goes evenly into the pure value
			final BigDecimal bigResult = bigStep.multiply(bigFactor).add(bigMinimum); //get the result of the step multiplied by a whole step, and then resolve to the base
			if(Float.class.isAssignableFrom(valueClass)) { //if the value is a float TODO create a convenience method
				newValue = Float.valueOf(bigResult.floatValue()); //create a float
			} else if(Double.class.isAssignableFrom(valueClass)) { //if the value is a double
				newValue = Double.valueOf(bigResult.doubleValue()); //create a double
			} else if(BigDecimal.class.isAssignableFrom(valueClass)) { //if the value is a big decimal
				newValue = bigResult; //use the result as-is
			} else { //we should have covered all the subtypes for which we use BigDecimal
				throw new AssertionError("Neglected to support decimal type: " + valueClass);
			}
			/*TODO del when works
								//TODO check these calculations, which were rushed when converted from Integer
						newValue=valueClass.cast(new Float(pureValue-(pureValue%step)+minimum));	//find the value that takes into account the minimum value and the step value
			*/
		} else { //if we don't support this type TODO add support for new types
			throw new AssertionError("XHTML slider controller does not yet support value type " + valueClass);
		}
		sliderControl.setNotification(null); //clear the component errors; this method may generate new errors
		try {
			sliderControl.setValue(valueClass.cast(newValue)); //store the value in the model, throwing an exception if the value is invalid
		} catch(final PropertyVetoException propertyVetoException) { //if there is a veto TODO maybe use the converter when creating the notification
			final Throwable cause = propertyVetoException.getCause(); //get the cause of the veto, if any
			sliderControl.setNotification(new Notification(cause != null ? cause : propertyVetoException)); //add notification of the error to the component
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version adds attributes based upon the slider axis and orientation, as well as whether the slider is sliding.
	 * </p>
	 * @see AbstractWebComponentDepictor#addFlowStyleIDs(Set, Flow)
	 * @see GuiseCSSStyleConstants#SLIDER_SLIDING_CLASS
	 */
	@Override
	protected Set<String> getBaseStyleIDs(final String prefix, final String suffix) {
		final Set<String> baseStyleIDs = super.getBaseStyleIDs(prefix, suffix); //get the default base style IDs
		final C component = getDepictedObject(); //get the component
		addFlowStyleIDs(baseStyleIDs, component.getFlow()); //add style IDs related to flow
		if(component.isSliding()) { //if this slider is sliding
			baseStyleIDs.add(SLIDER_SLIDING_CLASS); //add the "sliding" class to the menu
		}
		return baseStyleIDs; //return the new style IDs
	}

	/*
	 * Retrieves the styles for the outer element of the component.
	 * @param context Guise context information.
	 * @param component The component for which styles should be retrieved.
	 * @return The styles for the outer element of the component, mapped to CSS property names.
	 */
	/*TODO del
		protected Map<String, String> getBodyStyles(final GC context, final C component)
		{
			final Map<String, String> styles=super.getBodyStyles(context, component);	//get the default body styles
			styles.put(CSS_PROP_POSITION, CSS_POSITION_RELATIVE);	//TODO testing
			return styles;	//return the styles
		}
	*/

	@Override
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		writeIDClassAttributes(null, null); //write the ID and class attributes with no prefixes or suffixes
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction
		writeLabel(decorateID(getPlatform().getDepictIDString(getDepictedObject().getDepictID()), null, COMPONENT_BODY_CLASS_SUFFIX)); //write the label for the body, if there is a label				
	}

	@Override
	protected void depictBody() throws IOException {
		final GuiseSession session = getSession(); //get the session
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		final Orientation orientation = component.getComponentOrientation(); //get the component's orientation
		final Flow flow = component.getFlow(); //get the flow of the slider		
		final Axis flowAxis = component.getComponentOrientation().getAxis(flow); //see what axis the slider flows on
		final Flow.Direction flowDirection = orientation.getDirection(flow); //get the direction of the flow
		final String sliderPositionID = decorateID(getPlatform().getDepictIDString(component.getDepictID()), null, "-position"); //determine the slider position input ID TODO use a constant
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_INPUT, true); //<xhtml:input> (slider.position)
		depictContext.writeAttribute(null, ATTRIBUTE_ID, sliderPositionID); //id="xxx.position"
		//TODO del if not needed		context.writeAttribute(null, ATTRIBUTE_NAME, sliderPositionID);	//name="xxx.position"
		depictContext.writeAttribute(null, ATTRIBUTE_NAME, getPlatform().getDepictIDString(component.getDepictID())); //name="componentID"
		depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_TYPE, INPUT_TYPE_HIDDEN); //type="hidden"
		final Validator<V> validator = component.getValidator(); //get the component's validator
		final RangeValidator<V> rangeValidator = validator instanceof RangeValidator ? (RangeValidator<V>)validator : null; //get the validator as a range validator, if it is one
		final V rangeMinimum = rangeValidator != null ? rangeValidator.getMinimum() : null; //find the minumum, if there is one
		final V rangeMaximum = rangeValidator != null ? rangeValidator.getMaximum() : null; //find the maximum, if there is one
		final V rangeStep = rangeValidator != null ? rangeValidator.getStep() : null; //find the step, if there is one
		final V value = component.getValue(); //get the current value
		if(value != null) { //if there is a value TODO decide what to do with a slider with no initial value
			final double position; //we'll determine the position as a number from 0.0 to 1.0
			final Class<V> valueClass = component.getValueClass(); //see what type of class is represented in the model
			if(Integer.class.isAssignableFrom(valueClass) || Long.class.isAssignableFrom(valueClass)) { //integer or long
				final long minimum = rangeMinimum != null ? rangeMinimum.longValue() : 0; //get the minimum to use for calculations 
				final long maximum = rangeMaximum != null ? rangeMaximum.longValue() : 100; //get the maximum to use for calculations 
				final long range = maximum - minimum; //calculate the range
				//TODO check for divide by zero
				//TODO del if not needed				final int step=rangeStep!=null ? rangeStep.intValue() : 1;	//get the step to use for calculations
				position = range != 0 ? ((value.longValue() - minimum) / (double)range) : 0; //calculate the fractional position, defaulting to a zero position if there is no range
			} else if(Float.class.isAssignableFrom(valueClass) || Double.class.isAssignableFrom(valueClass)) { //float or double
				final double minimum = rangeMinimum != null ? rangeMinimum.doubleValue() : 0; //get the minimum to use for calculations 
				final double maximum = rangeMaximum != null ? rangeMaximum.doubleValue() : 100; //get the maximum to use for calculations 
				final double range = maximum - minimum; //calculate the range
				//TODO del if not needed				final int step=rangeStep!=null ? rangeStep.intValue() : 1;	//get the step to use for calculations
				position = range != 0 ? ((value.doubleValue() - minimum) / range) : 0; //calculate the fractional position, defaulting to a zero position if there is no range
			} else { //if we don't support this type TODO add support for new types
				throw new AssertionError("XHTML slider controller does not yet support value type " + valueClass);
			}
			depictContext.writeAttribute(null, ATTRIBUTE_VALUE, Double.toString(position)); //value="position"
		}
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_INPUT); //</xhtml:input>		
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div> (component-body)
		writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX); //write the ID and class for the body element
		writeStyleAttribute(getBodyStyles()); //write the component's body styles
		super.depictBody(); //render the default main part of the component
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div> (component-track)
		writeIDClassAttributes(null, SLIDER_TRACK_CLASS_SUFFIX); //write the ID and class attributes for the track
		/*TODO del
		
				final Map<String, String> trackStyles=new HashMap<String, String>();	//create a new map of styles
				final String trackDisplay=axis==Axis.Y ? CSS_DISPLAY_INLINE : CSS_DISPLAY_BLOCK;	//determine if the labels should go under or beside the slider
				trackStyles.put(CSS_PROP_DISPLAY, trackDisplay);	//set the styles of the track
				writeStyleAttribute(context, trackStyles);	//write the body style
		*/
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction

		//TODO fix flowAxis

		final URI trackImageURI = component.getTrackImage(flowAxis); //get the track image, if any
		if(trackImageURI != null) { //if there is a track image		
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IMG, true); //<xhtml:img>
			//TODO fix			context.writeAttribute(null, ATTRIBUTE_ID, decorateID(component.getID(), null, FRAME_CLOSE_CLASS_SUFFIX));	//write the absolute unique ID with the correct suffix
			//TODO fix			writeClassAttribute(context, closeStyleIDs);	//write the title style IDs
			//use the correct flow axis suffix when resolving URIs
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, depictContext.getDepictionURI(trackImageURI).toString()); //src="trackImageURI"
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IMG); //</xhtml:img>
		}
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div> (component-thumb)
		writeIDClassAttributes(null, SLIDER_THUMB_CLASS_SUFFIX); //write the ID and class attributes for the thumb
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction
		final URI thumbImageURI = component.getThumbImage(flowAxis); //get the thumb image, if any
		if(thumbImageURI != null) { //if there is a thumb image		
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IMG, true); //<xhtml:img>
			//TODO fix			context.writeAttribute(null, ATTRIBUTE_ID, decorateID(component.getID(), null, FRAME_CLOSE_CLASS_SUFFIX));	//write the absolute unique ID with the correct suffix
			//TODO fix			writeClassAttribute(context, closeStyleIDs);	//write the title style IDs
			//use the correct flow axis suffix when resolving URIs
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, depictContext.getDepictionURI(thumbImageURI).toString()); //src="thumbImageURI"
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IMG); //</xhtml:img>
		}
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div> (component-thumb)
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div> (component-track)

		if(false) { //if we should show interval labels TODO fix
			final V interval = component.getInterval(); //get the interval
			if(rangeMinimum != null && rangeMaximum != null) { //if we have a range

				final BigDecimal bigRangeMinimum = toBigDecimal(rangeMinimum); //get a big decimal version of the minimum
				final BigDecimal bigRangeMaximum = toBigDecimal(rangeMaximum); //get a big decimal version of the maximum
				final BigDecimal bigRange = bigRangeMaximum.subtract(bigRangeMinimum); //calculate the range
				final BigDecimal bigInterval = interval != null ? toBigDecimal(interval) : bigRange; //use the existing interval or simply use the range if there is no interval
				//Log.trace("using big interval", bigInterval);
				/*TODO fix
								final List<BigDecimal> intervalValues=new ArrayList<BigDecimal>();	//create a list for the interval values
				
								BigDecimal intervalValue=bigRangeMinimum;	//start out with the minimum value
								while(intervalValue.compareTo(bigRangeMaximum)<=0) {	//while we haven't overshot the maximum
									intervalValues.add(intervalValue);	//add this interval value
									intervalValue=intervalValue.add(bigInterval);	//go to the next interval
								}
				*/

				/*TODO del if not needed
								context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//<xhtml:div> (component-intervals)
								writeIDClassAttributes(context, component, null, "-intervals");	//write the ID and class attributes for the interval labels TODO use a constant
				*/

				//TODO fix			writeDirectionAttribute(context, component);	//write the component direction, if this component specifies a direction

				/*TODO del when works				
								final Map<String, String> labelsStyles=new HashMap<String, String>();	//create a new map of styles
				//TODO fix				labelsStyles.put(CSS_PROP_FLOAT, CSS_FLOAT_RIGHT);	//set the styles of the labels box
								labelsStyles.put(CSS_PROP_POSITION, CSS_POSITION_ABSOLUTE);	//set the styles of the labels box
								labelsStyles.put(CSS_PROP_TOP, String.valueOf(0));	//set the styles of the labels box
								labelsStyles.put(CSS_PROP_RIGHT, String.valueOf(0));	//set the styles of the labels box
								writeStyleAttribute(context, labelsStyles);	//write the body style
				*/

				final String flowCoordinate = flowAxis == Axis.Y ? CSS_PROP_TOP : CSS_PROP_LEFT; //TODO test; fix for direction
				final String tileCoordinate = flowAxis == Axis.Y ? CSS_PROP_RIGHT : CSS_PROP_BOTTOM; //TODO test
				final BigDecimal bigDecimal1 = new BigDecimal(1); //create a big decimal representing 1
				final BigDecimal bigDecimal100 = new BigDecimal(100); //create a big decimal representing 100
				BigDecimal intervalValue = bigRangeMinimum; //start out with the minimum value
				while(intervalValue.compareTo(bigRangeMaximum) <= 0) { //while we haven't overshot the maximum
					depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div> (component-interval-label-wrapper)					
					//TODO write the class and style					
					final BigDecimal bigPosition = intervalValue.divide(bigRange); //get the percentage along the slider
					final BigDecimal bigPercent = flowDirection == Flow.Direction.INCREASING ? bigPosition.multiply(bigDecimal100)
							: bigDecimal1.subtract(bigPosition).multiply(bigDecimal100); //get the position along the slider, compensating for flow direction
					final String percent = bigPercent.toPlainString() + "%"; //create a percentage string

					final Map<String, Object> intervalWrapperStyles = new HashMap<String, Object>(); //create a new map of styles
					//				TODO fix				labelsStyles.put(CSS_PROP_FLOAT, CSS_FLOAT_RIGHT);	//set the styles of the labels box
					intervalWrapperStyles.put(CSS_PROP_POSITION, CSS_POSITION_ABSOLUTE); //set the styles of the labels box
					intervalWrapperStyles.put(flowCoordinate, percent); //set the percentage coordinate
					intervalWrapperStyles.put(tileCoordinate, "0"); //set the tile coordinate
					writeStyleAttribute(intervalWrapperStyles); //write the interval style

					/*TODO del; doesn't work
										context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//<xhtml:div> (component-interval-label)					
										final Map<String, String> intervalStyles=new HashMap<String, String>();	//create a new map of styles
										intervalStyles.put(CSS_PROP_POSITION, CSS_POSITION_RELATIVE);	//set the style of the label to relative for centering
										intervalStyles.put(flowCoordinate, "-50%");	//center the coordinate
										writeStyleAttribute(context, intervalStyles);	//write the interval style
					*/
					depictContext.write(intervalValue.toString()); //write the interval label					
					//TODO del					context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//</xhtml:div> (component-interval-label)
					depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div> (component-interval-label-wrapper)
					intervalValue = intervalValue.add(bigInterval); //go to the next interval
				}
				/*TODO del if not needed
								context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//</xhtml:div> (component-intervals)
				*/
			}
		}
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div> (component-body)
	}

	@Override
	public void depictEnd() throws IOException {
		writeErrorMessage(); //write the error message, if any
		super.depictEnd(); //do the default ending rendering
	}

}
