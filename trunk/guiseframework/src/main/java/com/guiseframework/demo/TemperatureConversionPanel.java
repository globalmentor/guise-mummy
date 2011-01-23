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

package com.guiseframework.demo;

import java.beans.PropertyVetoException;

import com.globalmentor.beans.AbstractGenericPropertyChangeListener;
import com.globalmentor.beans.GenericPropertyChangeEvent;
import com.globalmentor.beans.GenericPropertyChangeListener;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.ValueRequiredValidator;

/**Temperature Conversion Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates layout panels, group panels, double value input controls,
	double value input validation, radio button controls, dynamic updates
	(e.g. AJAX on the web platform), required value validation,
	and read-only controls.
@author Garret Wilson
*/
public class TemperatureConversionPanel extends LayoutPanel
{

	private final TextControl<Double> temperatureInput;
	private final TextControl<Double> temperatureOutput;
	private final CheckControl celsiusCheckControl;
	private final CheckControl fahrenheitCheckControl;

	/**Default constructor.*/
	public TemperatureConversionPanel()
	{
		super(new FlowLayout(Flow.LINE));	//construct the parent class flowing horizontally
		setLabel("Guise\u2122 Demonstration: Temperature Conversion");	//set the panel title	

			//input panel
		final LayoutPanel inputPanel=new LayoutPanel(new FlowLayout(Flow.PAGE));	//create the input panel flowing vertically
		temperatureInput=new TextControl<Double>(Double.class);	//create a text input control to receive a double
		temperatureInput.setLabel("Input Temperature");	//add a label to the text input control
		temperatureInput.setValidator(new ValueRequiredValidator<Double>());	//install a validator requiring a value
		temperatureInput.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Double>()	//listen for temperature changes
				{
					public void propertyChange(final GenericPropertyChangeEvent<Double> propertyChangeEvent)	//if the input temperature changes
					{
						convertTemperature();	//convert the temperature						
					}
				});		
		inputPanel.add(temperatureInput);	//add the input control to the input panel
		temperatureOutput=new TextControl<Double>(Double.class);	//create a text input control to display the result
		temperatureOutput.setLabel("Output Temperature");	//add a label to the text output control
		temperatureOutput.setEditable(false);	//set the text output control to read-only so that the user cannot modify it
		inputPanel.add(temperatureOutput);	//add the output control to the input panel

		add(inputPanel);	//add the input panel to the temperature panel

		final LayoutPanel conversionPanel=new LayoutPanel(new FlowLayout(Flow.PAGE));	//create the right-hand panel flowing vertically
			
			//scale panel
		final GroupPanel scalePanel=new GroupPanel(new FlowLayout(Flow.PAGE));	//create the scale panel flowing vertically
		scalePanel.setLabel("Input Scale");	//set the group panel label
		celsiusCheckControl=new CheckControl(CheckControl.CheckType.ELLIPSE);	//create a check control for the Celsius scale, using an ellipse check are
		celsiusCheckControl.setLabel("Celsius");	//set the label of the check to indicate the scale
		try
		{
			celsiusCheckControl.setValue(Boolean.TRUE);	//default to converting from Celsius to Fahrenheit
		}
		catch(final PropertyVetoException propertyVetoException)	//if the change was vetoed, ignore the exception
		{
		}
		scalePanel.add(celsiusCheckControl);	//add the Celsius check control to the panel	
		fahrenheitCheckControl=new CheckControl(CheckControl.CheckType.ELLIPSE);	//create a check control for the Fahrenheit scale, using an ellipse check are
		fahrenheitCheckControl.setLabel("Fahrenheit");	//set the label of the check to indicate the scale
		scalePanel.add(fahrenheitCheckControl);	//add the Fahrenheit check control to the panel	
			//create a mutual exclusion policy group and add the Celsius and Fahrenheit check box boolean value models to get radio button functionality
		final ModelGroup<ValueModel<Boolean>> radioButtonModelGroup=new MutualExclusionPolicyModelGroup(celsiusCheckControl, fahrenheitCheckControl);
		conversionPanel.add(scalePanel);	//add the scale panel to the conversion panel

			//create a listener to listen for check control changes and update the temperature immediately (e.g. with AJAX on the web platform)
		final GenericPropertyChangeListener<Boolean> checkControlListener=new AbstractGenericPropertyChangeListener<Boolean>()
				{
					public void propertyChange(final GenericPropertyChangeEvent<Boolean> propertyChangeEvent)
					{
						if(propertyChangeEvent.getNewValue())	//if this check control was selected
						{
							convertTemperature();	//convert the temperature							
						}
					}
				};

		celsiusCheckControl.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, checkControlListener);	//listen for the Celsius control changing
		fahrenheitCheckControl.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, checkControlListener);	//listen for the Fahrenheit control changing
		
			//conversion button
		final Button convertButton=new Button();	//create a button for initiating the conversion
		convertButton.setLabel("Convert");	//set the button label
		convertButton.addActionListener(new ActionListener()	//when the convert button is pressed
				{
					public void actionPerformed(ActionEvent actionEvent)	//convert the temperature in the input field and place the result in the output field
					{
						convertTemperature();	//convert the temperature
					}
				});
		conversionPanel.add(convertButton);	//add the button to the conversion panel
		
		add(conversionPanel);	//add the conversion panel to the panel
	}

	/**Converts the temperature based upon the current UI values.*/
	protected void convertTemperature()
	{
		if(isValid())	//if this panel and all of its components have valid model values
		{
			final double inputValue=temperatureInput.getValue().doubleValue();	//get the input value from the control
			final double outputValue;	//we'll convert the value and store it here
			if(celsiusCheckControl.getValue())	//if the Celsius radio button is selected
			{
				outputValue=(inputValue*9)/5+32;	//convert: (9c/5)+32
			}
			else if(fahrenheitCheckControl.getValue())	//if the Fahrenheit radio button is selected
			{
				outputValue=((inputValue-32)*5)/9;	//convert: 5(f-32)/9							
			}
			else	//if neither check control is selected (which should never happen, because we set one to begin with and they are both using a mutual exclusion model group)
			{
				throw new AssertionError("Expected one of the scale radio buttons to be selected.");
			}
			try
			{
				temperatureOutput.setValue(Double.valueOf(outputValue));	//store the conversion result in the temperature output control
			}
			catch(final PropertyVetoException propertyVetoException)	//if the change was vetoed, ignore the exception
			{
			}
		}
	}

}
