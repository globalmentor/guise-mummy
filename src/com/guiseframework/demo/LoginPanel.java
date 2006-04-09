package com.guiseframework.demo;

import java.util.Arrays;

import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.model.Notification;
import com.guiseframework.validator.*;

/**Login Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates on-the-fly validation, on-the-fly error reporting,
	resetting control values, and setting Guise session user.
@author Garret Wilson
*/
public class LoginPanel extends DefaultNavigationPanel
{

	/**Default constructor.*/
	public LoginPanel()
	{
		super(new RegionLayout());	//construct the parent class, using a region layout
		setLabel("Guise\u2122 Demonstration: Login");	//set the panel title
		
		final LayoutPanel loginPanel=new LayoutPanel(new FlowLayout(Flow.PAGE));	//create the authorization panel flowing vertically
		
			//heading
		final Heading heading=new Heading(0);	//create a top-level heading
		heading.setLabel("Login");	//set the text of the heading, using its model
		loginPanel.add(heading);	//add the heading to the panel

		final LayoutPanel userPanel=new LayoutPanel(new FlowLayout(Flow.LINE));	//create the user panel flowing horizontally

			//ID
		final TextControl<String> idControl=new TextControl<String>(String.class);	//create the ID input control
		idControl.setLabel("User ID *");	//set the ID control label
		idControl.setValidator(new RegularExpressionStringValidator(".+", true));	//require at least a single character
		userPanel.add(idControl);	//add the ID control to the panel

			//password
		final TextControl<char[]> passwordControl=new TextControl<char[]>(char[].class);	//create the password input control
		passwordControl.setLabel("Password *");	//set the password control label
		passwordControl.setMasked(true);	//mask the password input
		passwordControl.setValidator(new RegularExpressionCharArrayValidator(".+", true));	//require at least a single character
		userPanel.add(passwordControl);	//add the password control to the panel

		loginPanel.add(userPanel);	//add the user panel to the login panel

			//login button
		final Button loginButton=new Button();	//create a button for logging in
		loginButton.setLabel("Log in");	//set the button label
		loginButton.addActionListener(new ActionListener()	//when the login button is pressed
				{
					public void actionPerformed(ActionEvent actionEvent)	//get the user, verify the password, and set the new session user
					{
						if(isValid())	//if the form information is valid
						{
							final char[] password=passwordControl.getValue();	//get the password entered by the user
							passwordControl.resetValue();	//reset the password value so that it won't be available on subsequent accesses
							final DemoApplication demoApplication=(DemoApplication)getSession().getApplication();	//get a reference to the demo application
							final DemoUser user=demoApplication.getPrincipal(idControl.getValue());	//get the user by ID
							if(user!=null)	//if a valid user was entered
							{
								if(Arrays.equals(user.getPassword(), password))	//if the entered password matches that of the user
								{
									getSession().setPrincipal(user);	//log in the user
									getSession().navigate(DemoApplication.RESTRICTED_PANEL_NAVIGATION_PATH);	//navigate to the restricted panel
								}
								else	//if the password doesn't match
								{
									final ValidationException validationException=new ValidationException(passwordControl, "Invalid password");	//create an invalid password exception for the password control
									idControl.setNotification(new Notification(validationException));	//indicate that the ID control has a validation exception
								}
							}
							else	//if the user ID is not valid
							{
								final ValidationException validationException=new ValidationException(idControl, "Invalid user ID");	//create an invalid user ID exception for the ID control
								idControl.setNotification(new Notification(validationException));	//indicate that the ID control has a validation exception
							}
						}
					}
				});
		loginPanel.add(loginButton);	//add the button to the panel
	
		add(loginPanel);	//add the panel to the panel in the default center
	}

}
