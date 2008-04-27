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

import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.validator.RegularExpressionStringValidator;

/**Hello User Guise demonstration panel.
Copyright © 2005-2007 GlobalMentor, Inc.
Demonstrates flow layouts, hidden components, text controls, control labels,
	tooltips, text control regular expression validators, buttons,
	form validation, and action listeners.
@author Garret Wilson
*/
public class HelloUserPanel extends LayoutPanel
{

	/**Default constructor.*/
	public HelloUserPanel()
	{
		super(new FlowLayout(Flow.PAGE));	//construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Hello User");	//set the panel title	

		final Label greetingLabel=new Label();	//create a label
		greetingLabel.setVisible(false);	//don't show the label initially
		add(greetingLabel);	//add the label to the panel
		
		final TextControl<String> userInput=new TextControl<String>(String.class);	//create a text input control to retrieve a string
		userInput.setLabel("What's your name?");	//add a label to the text input control
		userInput.setInfo("Enter a name that does not start with whitespace.");	//add advisory information that may be shown as a tooltip
		userInput.setValidator(new RegularExpressionStringValidator("\\S+.*", true));	//require at least a single non-whitespace character followed by any other characters
		add(userInput);	//add the user input control to the panel

		final Button greetButton=new Button();	//create a button for changing the greeting label
		greetButton.setLabel("Greet");	//set the button label
		greetButton.addActionListener(new ActionListener()	//listen for the button being pressed
				{
					public void actionPerformed(final ActionEvent actionEvent)	//if the button is pressed
					{
						if(HelloUserPanel.this.validate())	//if our entire panel (including the user input) validates
						{
							final String user=userInput.getValue();	//get the name the user entered
							greetingLabel.setLabel("Hello, "+user+"!");	//update the label
							greetingLabel.setVisible(true);	//make the label visible							
						}
					}
				});
		add(greetButton);	//add the greet button to the panel
	}

}
