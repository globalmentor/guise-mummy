package com.javaguise.demo;

import com.javaguise.GuiseSession;
import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.event.*;
import com.javaguise.model.ValueModel;
import com.javaguise.validator.RegularExpressionStringValidator;

/**Hello User Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates flow layouts, hidden components, text controls, control labels,
	tooltips, text control regular expression validators, buttons,
	and model value change listeners.
@author Garret Wilson
*/
public class HelloUserPanel extends DefaultNavigationPanel
{

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public HelloUserPanel(final GuiseSession session)
	{
		super(session, new FlowLayout(session, Flow.PAGE));	//construct the parent class flowing vertically
		getModel().setLabel("Guise\u2122 Demonstration: Hello User");	//set the panel title	

		final Label helloUserLabel=new Label(session);	//create a label
		helloUserLabel.setVisible(false);	//don't show the label initially
		add(helloUserLabel);	//add the label to the panel
		
		final TextControl<String> userInput=new TextControl<String>(session, String.class);	//create a text input control to retrieve a string
		userInput.getModel().setLabel("What's your name?");	//add a label to the text input control
		userInput.getModel().setInfo("Enter a name that does not start with whitespace.");	//add advisory information that may be shown as a tooltip
		userInput.getModel().setValidator(new RegularExpressionStringValidator(session, "\\S+.*", true));	//require at least a single non-whitespace character followed by any other characters
		userInput.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<ValueModel<String>, String>()
				{
					public void propertyChange(final GuisePropertyChangeEvent<ValueModel<String>, String> propertyValueChangeEvent)
					{
						final String user=propertyValueChangeEvent.getNewValue();	//get the name the user entered
						helloUserLabel.getModel().setLabel("Hello, "+user+"!");	//update the label
						helloUserLabel.setVisible(true);	//make the label visible
					}
				});
		add(userInput);	//add the user input control to the panel
	}

}
