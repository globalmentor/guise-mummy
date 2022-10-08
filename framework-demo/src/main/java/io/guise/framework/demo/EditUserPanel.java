/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.demo;

import java.beans.PropertyVetoException;
import java.util.Arrays;

import io.guise.framework.component.*;
import io.guise.framework.component.layout.*;
import io.guise.framework.event.*;
import io.guise.framework.model.Notification;
import io.guise.framework.validator.*;

/**
 * Edit User Guise demonstration panel. Copyright © 2005 GlobalMentor, Inc. Demonstrates modal panels, component validity checks, masked text controls, ending
 * modality, and modal cancel functionality.
 * @author Garret Wilson
 */
public class EditUserPanel extends DefaultModalNavigationPanel<DemoUser> {

	private final TextControl<String> idControl;
	private final TextControl<String> firstNameControl;
	private final TextControl<String> middleNameControl;
	private final TextControl<String> lastNameControl;
	private final TextControl<char[]> passwordControl;
	private final TextControl<char[]> passwordVerificationControl;
	private final TextControl<String> emailControl;

	/** Default constructor. */
	public EditUserPanel() {
		super(new FlowLayout(Flow.PAGE)); //construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Edit User"); //set the panel title	

		final LayoutPanel idPanel = new LayoutPanel(new FlowLayout(Flow.LINE)); //create the ID panel flowing horizontally
		//ID
		idControl = new TextControl<String>(String.class); //create the ID input control
		idControl.setLabel("ID"); //set the ID control label
		idControl.setEditable(false); //don't allow the ID to be edited
		idPanel.add(idControl); //add the ID control to the ID panel

		final LayoutPanel namePanel = new LayoutPanel(new FlowLayout(Flow.LINE)); //create the name panel flowing horizontally
		//first name
		firstNameControl = new TextControl<String>(String.class); //create the first name input control
		firstNameControl.setLabel("First Name *"); //set the name control label
		firstNameControl.setValidator(new RegularExpressionStringValidator("\\S+.*", true)); //require at least a single non-whitespace character followed by any other characters
		namePanel.add(firstNameControl); //add the name control to the name panel
		//middle name
		middleNameControl = new TextControl<String>(String.class); //create the first name input control, but don't require any validation
		middleNameControl.setLabel("Middle Name"); //set the name control label
		namePanel.add(middleNameControl); //add the name control to the name panel
		//last name
		lastNameControl = new TextControl<String>(String.class); //create the last name input control
		lastNameControl.setLabel("Last Name *"); //set the name control label
		lastNameControl.setValidator(new RegularExpressionStringValidator("\\S+.*", true)); //require at least a single non-whitespace character followed by any other characters
		namePanel.add(lastNameControl); //add the name control to the name panel

		final LayoutPanel passwordPanel = new LayoutPanel(new FlowLayout(Flow.LINE)); //create the password panel flowing horizontally
		//password
		passwordControl = new TextControl<char[]>(char[].class); //create the password input control
		passwordControl.setLabel("Password *"); //set the password control label
		passwordControl.setMasked(true); //mask the password input
		passwordControl.setValidator(new RegularExpressionCharArrayValidator("\\S+", true)); //require at least a single non-whitespace character
		passwordPanel.add(passwordControl); //add the password control to the password panel
		//password verification
		passwordVerificationControl = new TextControl<char[]>(char[].class); //create the password verification input control
		passwordVerificationControl.setLabel("Password Verification *"); //set the password control label
		passwordVerificationControl.setMasked(true); //mask the password input
		passwordVerificationControl.setValidator(new RegularExpressionCharArrayValidator("\\S+", true)); //require at least a single non-whitespace character
		passwordPanel.add(passwordVerificationControl); //add the password verification control to the password panel

		final LayoutPanel emailPanel = new LayoutPanel(new FlowLayout(Flow.LINE)); //create the email panel flowing horizontally
		//email
		emailControl = new TextControl<String>(String.class); //create the email input control
		emailControl.setLabel("Email *"); //set the email control label
		emailControl.setValidator(new RegularExpressionStringValidator(".+@.+\\.[a-z]+", true)); //require an email in the correct format
		emailPanel.add(emailControl); //add the email control to the email panel

		final LayoutPanel buttonPanel = new LayoutPanel(new FlowLayout(Flow.LINE)); //create the button panel flowing horizontally
		final Button okButton = new Button(); //create the OK button
		okButton.setLabel("OK"); //set the text of the OK button
		okButton.addActionListener(new ActionListener() { //if the OK button was pressed

			@Override
					public void actionPerformed(ActionEvent actionEvent) {
						if(validate()) { //validate the form, showing errors; if the form validates
							endModal(getUser()); //end the panel modality with the edited user only if the form validates
						}
					}
				});
		buttonPanel.add(okButton); //add the button to the button panel
		final Button cancelButton = new Button(); //create the cancel button
		cancelButton.setLabel("Cancel"); //set the text of the cancel button
		cancelButton.addActionListener(new ActionListener() { //if the cancel button was pressed

			@Override
					public void actionPerformed(ActionEvent actionEvent) {
						endModal(null); //end the panel modality with no user
					}
				});
		buttonPanel.add(cancelButton); //add the button to the button panel

		add(idPanel); //add the ID panel to the panel
		add(namePanel); //add the name panel to the panel
		add(passwordPanel); //add the password panel to the panel
		add(emailPanel); //add the email panel to the panel
		add(buttonPanel); //add the buttonpanel to the panel
	}

	/** @return <code>true</code> if both entered passwords are identical. */
	protected boolean isPasswordMatch() {
		return Arrays.equals(passwordControl.getValue(), passwordVerificationControl.getValue()); //see if the passwords match
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version ensures the entered passwords match.
	 * </p>
	 */
	@Override
	protected boolean determineValid() {
		return super.determineValid() && isPasswordMatch(); //add a check for password validity
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version adds errors for non-matching passwords.
	 * </p>
	 */
	@Override
	public boolean validate() {
		super.validate(); //validate the component normally
		if(!isPasswordMatch()) { //if the password isn't valid
			final Notification notification = new Notification("Passwords do not match.", Notification.Severity.ERROR); //create an error notification
			passwordControl.setNotification(notification); //add the error notification to each password control
			passwordVerificationControl.setNotification(notification);
		}
		return isValid(); //return the current valid state
	}

	/**
	 * Initializes the panel with information for a new user.
	 * @param id The ID of the new user to be edited.
	 * @throws NullPointerException if the provided user is <code>null</code>.
	 * @throws IllegalArgumentException if any user information is invalid.
	 */
	public void setNewUser(final String id) {
		try {
			idControl.setValue(id); //set the user ID
		} catch(final PropertyVetoException propertyVetoException) { //if the user information is invalid
			throw new IllegalArgumentException(propertyVetoException);
		}
	}

	/**
	 * Initializes the panel with user information.
	 * @param user The user with which to initialize the panel.
	 * @throws NullPointerException if the provided user is <code>null</code>.
	 * @throws IllegalArgumentException if any user information is invalid.
	 */
	public void setUser(final DemoUser user) {
		try {
			idControl.setValue(user.getID()); //update the values
			firstNameControl.setValue(user.getFirstName());
			middleNameControl.setValue(user.getMiddleName());
			lastNameControl.setValue(user.getLastName());
			passwordControl.setValue(user.getPassword());
			passwordVerificationControl.setValue(user.getPassword());
			emailControl.setValue(user.getEmail());
		} catch(final PropertyVetoException propertyVetoException) { //if the user information is invalid
			throw new IllegalArgumentException(propertyVetoException);
		}
	}

	/** @return A user representing the information entered in the panel. */
	public DemoUser getUser() {
		return new DemoUser( //create and return a new user
				idControl.getValue(), firstNameControl.getValue(), middleNameControl.getValue(), lastNameControl.getValue(), passwordControl.getValue(),
				emailControl.getValue());
	}
}
