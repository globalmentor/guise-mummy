package com.javaguise.demo;

import com.garretwilson.beans.*;
import com.garretwilson.util.Debug;
import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.event.ActionEvent;
import com.javaguise.event.ActionListener;

import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.ValidationException;
import com.javaguise.validator.ValueRequiredValidator;

/**Temperature Conversion Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates layout panels, group panels, float input controls, float input validation,
	radio button controls, dynamic updates (e.g. AJAX on the web platform),
	required value validation, and read-only controls.
@author Garret Wilson
*/
public class TemperatureConversionPanel extends DefaultNavigationPanel
{

	private final TextControl<Float> temperatureInput;
	private final TextControl<Float> temperatureOutput;
	private final CheckControl celsiusCheckControl;
	private final CheckControl fahrenheitCheckControl;

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public TemperatureConversionPanel(final GuiseSession session)
	{
		super(session, new FlowLayout(session, Flow.LINE));	//construct the parent class flowing horizontally
		getModel().setLabel("Guise\u2122 Demonstration: Temperature Conversion");	//set the panel title	

			//input panel
		final LayoutPanel inputPanel=new LayoutPanel(session, new FlowLayout(session, Flow.PAGE));	//create the input panel flowing vertically
		temperatureInput=new TextControl<Float>(session, Float.class);	//create a text input control to receive a float
		temperatureInput.getModel().setLabel("Input Temperature");	//add a label to the text input control
		temperatureInput.getModel().setValidator(new ValueRequiredValidator<Float>(session));	//install a validator requiring a value
		temperatureInput.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractPropertyValueChangeListener<Float>()	//listen for temperature changes
				{
					public void propertyValueChange(final PropertyValueChangeEvent<Float> propertyValueChangeEvent)	//if the input temperature changes
					{
						convertTemperature();	//convert the temperature						
					}
				});		
		inputPanel.add(temperatureInput);	//add the input control to the input panel
		temperatureOutput=new TextControl<Float>(session, Float.class);	//create a text input control to display the result
		temperatureOutput.getModel().setLabel("Output Temperature");	//add a label to the text output control
		temperatureOutput.getModel().setEditable(false);	//set the text output control to read-only so that the user cannot modify it
		inputPanel.add(temperatureOutput);	//add the output control to the input panel

		add(inputPanel);	//add the input panel to the temperature panel

		final LayoutPanel conversionPanel=new LayoutPanel(session, new FlowLayout(session, Flow.PAGE));	//create the right-hand panel flowing vertically
			
			//scale panel
		final GroupPanel scalePanel=new GroupPanel(session, new FlowLayout(session, Flow.PAGE));	//create the scale panel flowing vertically
		scalePanel.getModel().setLabel("Input Scale");	//set the group panel label
		celsiusCheckControl=new CheckControl(session, CheckControl.CheckType.ELLIPSE);	//create a check control for the Celsius scale, using an ellipse check are
		celsiusCheckControl.getModel().setLabel("Celsius");	//set the label of the check to indicate the scale
		try
		{
			celsiusCheckControl.getModel().setValue(Boolean.TRUE);	//default to converting from Celsius to Fahrenheit
		}
		catch(final ValidationException validationException)	//we have no validator installed in the check control model, so we don't expect changing its value ever to cause any problems
		{
			throw new AssertionError(validationException);
		}
		scalePanel.add(celsiusCheckControl);	//add the Celsius check control to the panel	
		fahrenheitCheckControl=new CheckControl(session, CheckControl.CheckType.ELLIPSE);	//create a check control for the Fahrenheit scale, using an ellipse check are
		fahrenheitCheckControl.getModel().setLabel("Fahrenheit");	//set the label of the check to indicate the scale
		scalePanel.add(fahrenheitCheckControl);	//add the Fahrenheit check control to the panel	
			//create a mutual exclusion policy group and add the Celsius and Fahrenheit check box boolean value models to get radio button functionality
		final ModelGroup<ValueModel<Boolean>> radioButtonModelGroup=new MutualExclusionPolicyModelGroup(celsiusCheckControl.getModel(), fahrenheitCheckControl.getModel());
		conversionPanel.add(scalePanel);	//add the scale panel to the conversion panel

			//create a listener to listen for check control changes and update the temperature immediately (e.g. with AJAX on the web platform)
		final PropertyValueChangeListener<Boolean> checkControlListener=new AbstractPropertyValueChangeListener<Boolean>()
				{
					public void propertyValueChange(final PropertyValueChangeEvent<Boolean> propertyValueChangeEvent)
					{
						if(propertyValueChangeEvent.getNewValue())	//if this check control was selected
						{
							convertTemperature();	//convert the temperature							
						}
					}
				};

		celsiusCheckControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, checkControlListener);	//listen for the Celsius control changing
		fahrenheitCheckControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, checkControlListener);	//listen for the Fahrenheit control changing
		
			//conversion button
		final DefaultButton convertButton=new DefaultButton(session);	//create a button for initiating the conversion
		convertButton.getModel().setLabel("Convert");	//set the button label
		convertButton.getModel().addActionListener(new ActionListener<ActionModel>()	//when the convert button is pressed
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)	//convert the temperature in the input field and place the result in the output field
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
Debug.trace("ready to convert temperature");
/*TODO fix
try
{
	validate();
} catch (ComponentExceptions e)
{
	Debug.error(e);
	return;
}
*/
		if(isValid())	//if this panel and all of its components have valid model values
		{
Debug.trace("the value is valid; now getting input value");
			final float inputValue=temperatureInput.getModel().getValue().floatValue();	//get the input value from the control
Debug.trace("valid panel with input value:", inputValue);
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
