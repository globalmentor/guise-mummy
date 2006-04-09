package com.guiseframework.demo;

import com.garretwilson.beans.AbstractGenericPropertyChangeListener;
import com.garretwilson.beans.GenericPropertyChangeEvent;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.model.ValueModel;
import com.guiseframework.validator.RegularExpressionStringValidator;

/**Hello User Guise demonstration panel.
Copyright � 2005 GlobalMentor, Inc.
Demonstrates flow layouts, hidden components, text controls, control labels,
	tooltips, text control regular expression validators, buttons,
	and model value change listeners.
@author Garret Wilson
*/
public class HelloUserPanel extends DefaultNavigationPanel
{

	/**Default constructor.*/
	public HelloUserPanel()
	{
		super(new FlowLayout(Flow.PAGE));	//construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Hello User");	//set the panel title	

		final Label helloUserLabel=new Label();	//create a label
		helloUserLabel.setVisible(false);	//don't show the label initially
		add(helloUserLabel);	//add the label to the panel
		
		final TextControl<String> userInput=new TextControl<String>(String.class);	//create a text input control to retrieve a string
		userInput.setLabel("What's your name?");	//add a label to the text input control
		userInput.setInfo("Enter a name that does not start with whitespace.");	//add advisory information that may be shown as a tooltip
		userInput.setValidator(new RegularExpressionStringValidator("\\S+.*", true));	//require at least a single non-whitespace character followed by any other characters
		userInput.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<String>()
				{
					public void propertyChange(final GenericPropertyChangeEvent<String> propertyValueChangeEvent)
					{
						final String user=propertyValueChangeEvent.getNewValue();	//get the name the user entered
						helloUserLabel.setLabel("Hello, "+user+"!");	//update the label
						helloUserLabel.setVisible(true);	//make the label visible
					}
				});
		add(userInput);	//add the user input control to the panel
	}

}
