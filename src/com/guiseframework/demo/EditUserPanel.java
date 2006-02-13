package com.guiseframework.demo;

import java.util.Arrays;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.validator.*;

/**Edit User Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates modal panels, component validity checks, masked text controls, ending modality, and modal cancel functionality.
@author Garret Wilson
*/
public class EditUserPanel extends DefaultModalNavigationPanel<DemoUser>
{

	private final TextControl<String> idControl; 
	private final TextControl<String> firstNameControl; 
	private final TextControl<String> middleNameControl; 
	private final TextControl<String> lastNameControl; 
	private final TextControl<char[]> passwordControl; 
	private final TextControl<char[]> passwordVerificationControl; 
	private final TextControl<String> emailControl; 

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public EditUserPanel(final GuiseSession session)
	{
		super(session, new FlowLayout(session, Flow.PAGE));	//construct the parent class flowing vertically
		getModel().setLabel("Guise\u2122 Demonstration: Edit User");	//set the panel title	

		final LayoutPanel idPanel=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));	//create the ID panel flowing horizontally
			//ID
		idControl=new TextControl<String>(session, String.class);	//create the ID input control
		idControl.getModel().setLabel("ID");	//set the ID control label
		idControl.getModel().setEditable(false);	//don't allow the ID to be edited
		idPanel.add(idControl);	//add the ID control to the ID panel
		
		final LayoutPanel namePanel=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));	//create the name panel flowing horizontally
			//first name
		firstNameControl=new TextControl<String>(session, String.class);	//create the first name input control
		firstNameControl.getModel().setLabel("First Name *");	//set the name control label
		firstNameControl.getModel().setValidator(new RegularExpressionStringValidator(session, "\\S+.*", true));	//require at least a single non-whitespace character followed by any other characters
		namePanel.add(firstNameControl);	//add the name control to the name panel
			//middle name
		middleNameControl=new TextControl<String>(session, String.class);	//create the first name input control, but don't require any validation
		middleNameControl.getModel().setLabel("Middle Name");	//set the name control label
		namePanel.add(middleNameControl);	//add the name control to the name panel
			//last name
		lastNameControl=new TextControl<String>(session, String.class);	//create the last name input control
		lastNameControl.getModel().setLabel("Last Name *");	//set the name control label
		lastNameControl.getModel().setValidator(new RegularExpressionStringValidator(session, "\\S+.*", true));	//require at least a single non-whitespace character followed by any other characters
		namePanel.add(lastNameControl);	//add the name control to the name panel

		final LayoutPanel passwordPanel=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));	//create the password panel flowing horizontally
			//password
		passwordControl=new TextControl<char[]>(session, char[].class);	//create the password input control
		passwordControl.getModel().setLabel("Password *");	//set the password control label
		passwordControl.setMasked(true);	//mask the password input
		passwordControl.getModel().setValidator(new RegularExpressionCharArrayValidator(session, "\\S+", true));	//require at least a single non-whitespace character
		passwordPanel.add(passwordControl);	//add the password control to the password panel
			//password verification
		passwordVerificationControl=new TextControl<char[]>(session, char[].class);	//create the password verification input control
		passwordVerificationControl.getModel().setLabel("Password Verification *");	//set the password control label
		passwordVerificationControl.setMasked(true);	//mask the password input
		passwordVerificationControl.getModel().setValidator(new RegularExpressionCharArrayValidator(session, "\\S+", true));	//require at least a single non-whitespace character
		passwordPanel.add(passwordVerificationControl);	//add the password verification control to the password panel

		final LayoutPanel emailPanel=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));	//create the email panel flowing horizontally
			//email
		emailControl=new TextControl<String>(session, String.class);	//create the email input control
		emailControl.getModel().setLabel("Email *");	//set the email control label
		emailControl.getModel().setValidator(new RegularExpressionStringValidator(session, ".+@.+\\.[a-z]+", true));	//require an email in the correct format
		emailPanel.add(emailControl);	//add the email control to the email panel

		final LayoutPanel buttonPanel=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));	//create the button panel flowing horizontally
		final Button okButton=new Button(session);	//create the OK button
		okButton.getModel().setLabel("OK");	//set the text of the OK button
		okButton.getModel().addActionListener(new ActionListener()	//if the OK button was pressed
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						try
						{
							validate();	//validate the form
							endModal(getUser());	//end the panel modality with the edited user only if the form validates
						}
						catch(final ComponentExceptions componentExceptions)	//if there is an error, don't accept the input
						{
						}
					}
				});
		buttonPanel.add(okButton);	//add the button to the button panel
		final Button cancelButton=new Button(session);	//create the cancel button
		cancelButton.getModel().setLabel("Cancel");	//set the text of the cancel button
		cancelButton.getModel().addActionListener(new ActionListener()	//if the cancel button was pressed
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						endModal(null);	//end the panel modality with no user
					}
				});
		buttonPanel.add(cancelButton);	//add the button to the button panel

		add(idPanel);	//add the ID panel to the panel
		add(namePanel);	//add the name panel to the panel
		add(passwordPanel);	//add the password panel to the panel
		add(emailPanel);	//add the email panel to the panel
		add(buttonPanel);	//add the buttonpanel to the panel
	}

	/**@return <code>true</code> if both entered passwords are identical.*/ 
	protected boolean isPasswordMatch()
	{
		return Arrays.equals(passwordControl.getModel().getValue(), passwordVerificationControl.getModel().getValue());	//see if the passwords match
	}

	/**Determines whether the models of this component and all of its child components are valid.
	This version ensures the entered passwords match.
	@return Whether the models of this component and all of its child components are valid.
	@see #isPasswordMatch()
	*/
	public boolean isValid()
	{
		return super.isValid() && isPasswordMatch();	//add a check for password validity
	}

	/**Validates the model of this component and all child components.
	The component will be updated with error information.
	This version adds errors for non-matching passwords.
	@exception ComponentExceptions if there was one or more validation error.
	*/
	public void validate() throws ComponentExceptions
	{
		ComponentExceptions componentExceptions=null;	//we'll store any component exceptions here and keep going
		try
		{
			super.validate();	//validate the component normally
		}
		catch(final ComponentExceptions superComponentExceptions)	//if the super version returns an error
		{
			if(componentExceptions==null)	//if this is our first component exception
			{
				componentExceptions=superComponentExceptions;	//store the exception and continue processing events with other child components
			}
			else	//if we already have component exceptions
			{
				componentExceptions.addAll(superComponentExceptions);	//add all the exceptions to the exception we already have
			}
		}
		if(!isPasswordMatch())	//if the password isn't valid
		{
			final ValidationException passwordValidationException=new ValidationException(EditUserPanel.this, "Passwords do not match");
			passwordControl.addError(passwordValidationException);	//add the error to each password control
			passwordVerificationControl.addError(passwordValidationException);
			if(componentExceptions==null)	//if there are no component exceptions yet
			{
				componentExceptions=new ComponentExceptions(passwordValidationException);	//create a new component exception list with the validation exception
			}
			else	//if there are already component exceptions
			{
				componentExceptions.add(passwordValidationException);	//add the validation exception
			}
		}
		if(componentExceptions!=null)	//if we encountered one or more component exceptions
		{
			throw componentExceptions;	//throw the exception, which may contain multiple exceptions
		}
	}

	/**Initializes the panel with information for a new user.
	@param id The ID of the new user to be edited.
	@exception NullPointerException if the provided user is <code>null</code>.
	@exception IllegalArgumentException if any user information is invalid.
	*/
	public void setNewUser(final String id)
	{
		try
		{
			idControl.getModel().setValue(id);	//set the user ID
		}
		catch(final ValidationException validationException)	//if the user information is invalid
		{
			throw new IllegalArgumentException(validationException);
		}		
	}

	/**Initializes the panel with user information.
	@param user The user with which to initialize the panel.
	@exception NullPointerException if the provided user is <code>null</code>.
	@exception IllegalArgumentException if any user information is invalid.
	*/
	public void setUser(final DemoUser user)
	{
		try
		{
			idControl.getModel().setValue(user.getID());	//update the values
			firstNameControl.getModel().setValue(user.getFirstName());
			middleNameControl.getModel().setValue(user.getMiddleName());
			lastNameControl.getModel().setValue(user.getLastName());
			passwordControl.getModel().setValue(user.getPassword());
			passwordVerificationControl.getModel().setValue(user.getPassword());
			emailControl.getModel().setValue(user.getEmail());
		}
		catch(final ValidationException validationException)	//if the user information is invalid
		{
			throw new IllegalArgumentException(validationException);
		}
	}

	/**@return A user representing the information entered in the panel.*/
	public DemoUser getUser()
	{
		return new DemoUser(	//create and return a new user
				idControl.getModel().getValue(),
				firstNameControl.getModel().getValue(),
				middleNameControl.getModel().getValue(),
				lastNameControl.getModel().getValue(),
				passwordControl.getModel().getValue(),
				emailControl.getModel().getValue());
	}
}
