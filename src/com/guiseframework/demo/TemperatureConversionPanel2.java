package com.guiseframework.demo;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.event.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.ValidationException;


/**Temperature Conversion Guise demonstration panel.
Copyright © 2005-2006 GlobalMentor, Inc.
Demonstrates layout panels, group panels, float input controls, float input validation,
	radio button controls, dynamic updates (e.g. AJAX on the web platform),
	required value validation, and read-only controls.
@author Garret Wilson
*/
public class TemperatureConversionPanel2 extends DefaultNavigationPanel
{
	private TextControl<Float> temperatureInput;
	private TextControl<Float> temperatureOutput;
	private CheckControl celsiusCheckControl;
	private CheckControl fahrenheitCheckControl;

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public TemperatureConversionPanel2(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

	/**Initializes the component after construction.*/
	public void initialize()
	{
		temperatureInput=(TextControl<Float>)AbstractComponent.getComponentByName(this, "temperatureInput");	//get a reference to the temperature input
		temperatureInput.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<Float>()	//listen for temperature changes
				{
					public void propertyChange(final GuisePropertyChangeEvent<Float> propertyChangeEvent)	//if the input temperature changes
					{
						convertTemperature();	//convert the temperature						
					}
				});		
		temperatureOutput=(TextControl<Float>)AbstractComponent.getComponentByName(this, "temperatureOutput");	//get a reference to the temperature output for displaying results
		celsiusCheckControl=(CheckControl)AbstractComponent.getComponentByName(this, "celsiusCheckControl");	//get the check control for the Celsius scale
		try
		{
			celsiusCheckControl.getModel().setValue(Boolean.TRUE);	//default to converting from Celsius to Fahrenheit
		}
		catch(final ValidationException validationException)	//we have no validator installed in the check control model, so we don't expect changing its value ever to cause any problems
		{
			throw new AssertionError(validationException);
		}
		fahrenheitCheckControl=(CheckControl)AbstractComponent.getComponentByName(this, "fahrenheitCheckControl");	//get the check control for the Fahrenheit scale
			//create a mutual exclusion policy group and add the Celsius and Fahrenheit check box boolean value models to get radio button functionality
		final ModelGroup<ValueModel<Boolean>> radioButtonModelGroup=new MutualExclusionPolicyModelGroup(celsiusCheckControl.getModel(), fahrenheitCheckControl.getModel());

			//create a listener to listen for check control changes and update the temperature immediately (e.g. with AJAX on the web platform)
		final GuisePropertyChangeListener<Boolean> checkControlListener=new AbstractGuisePropertyChangeListener<Boolean>()
				{
					public void propertyChange(final GuisePropertyChangeEvent<Boolean> propertyChangeEvent)
					{
						if(propertyChangeEvent.getNewValue())	//if this check control was selected
						{
							convertTemperature();	//convert the temperature							
						}
					}
				};
		celsiusCheckControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, checkControlListener);	//listen for the Celsius control changing
		fahrenheitCheckControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, checkControlListener);	//listen for the Fahrenheit control changing
			//conversion button
		final Button convertButton=(Button)AbstractComponent.getComponentByName(this, "conversionButton");	//get the button for initiating the conversion
		convertButton.getModel().addActionListener(new ActionListener()	//when the convert button is pressed
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
			final float inputValue=temperatureInput.getModel().getValue().floatValue();	//get the input value from the control
			final float outputValue;	//we'll convert the value and store it here
			if(celsiusCheckControl.getModel().getValue())	//if the Celsius radio button is selected
			{
				outputValue=(inputValue*9)/5+32;	//convert: (9c/5)+32
			}
			else if(fahrenheitCheckControl.getModel().getValue())	//if the Fahrenheit radio button is selected
			{
				outputValue=((inputValue-32)*5)/9;	//convert: 5(f-32)/9							
			}
			else	//if neither check control is selected (which should never happen, because we set one to begin with and they are both using a mutual exclusion model group)
			{
				throw new AssertionError("Expected one of the scale radio buttons to be selected.");
			}
			try
			{
				temperatureOutput.getModel().setValue(new Float(outputValue));	//store the conversion result in the temperature output control
			}
			catch(final ValidationException validationException)	//we have no validator installed in the temperature output text control, so we don't expect changing its value ever to cause any problems
			{
				throw new AssertionError(validationException);
			}
		}
	}
}
