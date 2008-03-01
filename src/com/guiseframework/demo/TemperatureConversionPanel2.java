package com.guiseframework.demo;

import java.beans.PropertyVetoException;

import com.globalmentor.beans.AbstractGenericPropertyChangeListener;
import com.globalmentor.beans.GenericPropertyChangeEvent;
import com.globalmentor.beans.GenericPropertyChangeListener;
import com.guiseframework.component.*;
import com.guiseframework.event.*;
import com.guiseframework.model.*;

/**Temperature Conversion Guise demonstration panel using URF PLOOP.
Copyright © 2005-2006 GlobalMentor, Inc.
Demonstrates layout panels, group panels, double value input controls,
	double value input validation, radio button controls, dynamic updates
	(e.g. AJAX on the web platform), required value validation,
	and read-only controls.
@author Garret Wilson
*/
public class TemperatureConversionPanel2 extends LayoutPanel
{
	private TextControl<Double> temperatureInput;
	private TextControl<Double> temperatureOutput;
	private CheckControl celsiusCheckControl;
	private CheckControl fahrenheitCheckControl;

	/**Initializes the component after construction.*/
	public void initialize()
	{
		temperatureInput=(TextControl<Double>)AbstractComponent.getComponentByName(this, "temperatureInput");	//get a reference to the temperature input
		temperatureInput.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Double>()	//listen for temperature changes
				{
					public void propertyChange(final GenericPropertyChangeEvent<Double> propertyChangeEvent)	//if the input temperature changes
					{
						convertTemperature();	//convert the temperature						
					}
				});		
		temperatureOutput=(TextControl<Double>)AbstractComponent.getComponentByName(this, "temperatureOutput");	//get a reference to the temperature output for displaying results
		celsiusCheckControl=(CheckControl)AbstractComponent.getComponentByName(this, "celsiusCheckControl");	//get the check control for the Celsius scale
		try
		{
			celsiusCheckControl.setValue(Boolean.TRUE);	//default to converting from Celsius to Fahrenheit
		}
		catch(final PropertyVetoException propertyVetoException)	//if the change was vetoed, ignore the exception
		{
		}
		fahrenheitCheckControl=(CheckControl)AbstractComponent.getComponentByName(this, "fahrenheitCheckControl");	//get the check control for the Fahrenheit scale
			//create a mutual exclusion policy group and add the Celsius and Fahrenheit check box boolean value models to get radio button functionality
		final ModelGroup<ValueModel<Boolean>> radioButtonModelGroup=new MutualExclusionPolicyModelGroup(celsiusCheckControl, fahrenheitCheckControl);

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
		final Button convertButton=(Button)AbstractComponent.getComponentByName(this, "conversionButton");	//get the button for initiating the conversion
		convertButton.addActionListener(new ActionListener()	//when the convert button is pressed
				{
					public void actionPerformed(ActionEvent actionEvent)	//convert the temperature in the input field and place the result in the output field
					{
						convertTemperature();	//convert the temperature
					}
				});		
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
