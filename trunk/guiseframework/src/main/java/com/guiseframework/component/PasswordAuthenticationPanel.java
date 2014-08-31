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

package com.guiseframework.component;

import java.beans.PropertyVetoException;
import java.util.Arrays;

import com.guiseframework.component.layout.*;
import com.guiseframework.model.Notification;

import static com.globalmentor.java.Classes.*;
import static com.guiseframework.theme.Theme.*;
import com.guiseframework.validator.*;

/**
 * Panel to gather password authentication information and optionally verify the password. This panel defaults to not verifying the password.
 * @author Garret Wilson
 */
public class PasswordAuthenticationPanel extends ArrangePanel {

	/** The bound property of the password verification status. */
	public static final String PASSWORD_VERIFIED_PROPERTY = getPropertyName(PasswordAuthenticationPanel.class, "passwordVerified");

	/** The username text control. */
	private final TextControl<String> usernameControl;

	/** The password text control. */
	private final TextControl<char[]> passwordControl;

	/** The password verification text control. */
	private final TextControl<char[]> passwordVerificationControl;

	/** Whether password verification is required, resulting in a separate password verification text input. */
	private boolean passwordVerified = false;

	/** @return Whether password verification is required, resulting in a separate password verification text input. */
	public boolean isPasswordVerified() {
		return passwordVerified;
	}

	/**
	 * Sets whether password verification is required This is a bound property of type <code>Boolean</code>. This method unconditionally clears the password
	 * verification control.
	 * @param newPasswordVerified Whether password verification is required, resulting in a separate password verification text input.
	 * @see #PASSWORD_VERIFIED_PROPERTY
	 */
	public void setPasswordVerified(final boolean newPasswordVerified) {
		if(passwordVerified != newPasswordVerified) { //if the value is really changing
			final boolean oldPasswordVerified = passwordVerified; //get the current value
			passwordVerified = newPasswordVerified; //update the value
			passwordVerificationControl.clearValue(); //always clear the password verification control, whatever the new value
			update(); //update the other components that rely on this setting
			firePropertyChange(PASSWORD_VERIFIED_PROPERTY, Boolean.valueOf(oldPasswordVerified), Boolean.valueOf(newPasswordVerified));
		}
	}

	/** Default constructor with a default vertical flow layout. */
	public PasswordAuthenticationPanel() {
		this(new FlowLayout(Flow.PAGE)); //default to flowing vertically
	}

	/**
	 * Layout constructor.
	 * @param layout The layout definition for the container.
	 * @throws NullPointerException if the given layout is <code>null</code>.
	 */
	public PasswordAuthenticationPanel(final Layout<?> layout) {
		super(layout); //construct the parent class

		//username
		usernameControl = new TextControl<String>(String.class); //create the username text control
		usernameControl.setLabel(LABEL_USERNAME); //set the username control label
		usernameControl.setGlyphURI(GLYPH_USER); //set the username control icon
		add(usernameControl); //add the ID control to the panel

		//password
		passwordControl = new TextControl<char[]>(char[].class); //create the password text control
		passwordControl.setLabel(LABEL_PASSWORD); //set the password control label
		passwordControl.setGlyphURI(GLYPH_PASSWORD); //set the password control icon
		passwordControl.setMasked(true); //mask the password input
		add(passwordControl); //add the password control to the panel

		//password verification
		passwordVerificationControl = new TextControl<char[]>(char[].class); //create the password verification text control
		passwordVerificationControl.setLabel(LABEL_PASSWORD); //set the password verification control label
		passwordVerificationControl.setGlyphURI(GLYPH_PASSWORD); //set the password verification control icon
		passwordVerificationControl.setMasked(true); //mask the password verification input
		add(passwordVerificationControl); //add the password verification control to the panel

		update(); //update the state of the password verification control based upon our password verification setting
	}

	/** @return The current username entered, or <code>null</code> if there is no username entered. */
	public String getUsername() {
		return usernameControl.getValue();
	}

	/**
	 * Sets the username shown in the panel.
	 * @param username The username to show.
	 * @throws PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	 */
	public void setUsername(final String username) throws PropertyVetoException {
		usernameControl.setValue(username);
	}

	/** @return The validator for the username, or <code>null</code> if no validator is installed. */
	public Validator<String> getUsernameValidator() {
		return usernameControl.getValidator();
	}

	/**
	 * Sets the username validator.
	 * @param newValidator The validator for the username, or <code>null</code> if no validator should be used.
	 */
	public void setUsernameValidator(final Validator<String> newValidator) {
		usernameControl.setValidator(newValidator);
	}

	/** @return The current password entered, or <code>null</code> if there is no password entered. */
	public char[] getPassword() {
		return passwordControl.getValue();
	}

	/**
	 * Sets the password shown in the panel.
	 * @param password The password to show.
	 * @throws PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	 */
	public void setPassword(final char[] password) throws PropertyVetoException {
		passwordControl.setValue(password);
	}

	/** @return The validator for the password, or <code>null</code> if no validator is installed. */
	public Validator<char[]> getPasswordValidator() {
		return passwordControl.getValidator();
	}

	/**
	 * Sets the password validator.
	 * @param newValidator The validator for the password, or <code>null</code> if no validator should be used.
	 */
	public void setPasswordValidator(final Validator<char[]> newValidator) {
		passwordControl.setValidator(newValidator);
		passwordVerificationControl.setValidator(newValidator); //install the validator into both password controls
	}

	/** Updates the state of the controls based upon current property settings. */
	protected void update() {
		passwordVerificationControl.setDisplayed(isPasswordVerified()); //only show the password verification if we should verify the password
	}

	/** @return <code>true</code> if both entered passwords are identical. */
	protected boolean isPasswordMatch() {
		return Arrays.equals(passwordControl.getValue(), passwordVerificationControl.getValue()); //see if the passwords match
	}

	/**
	 * Checks the state of the component for validity. This version ensures the entered passwords match if password verification is enabled.
	 * @return <code>true</code> if the component and all children passes all validity tests, else <code>false</code>.
	 */
	protected boolean determineValid() {
		return super.determineValid() && (!isPasswordVerified() || isPasswordMatch()); //add a check for password consistency if passwords are being verified 
	}

	/**
	 * Validates the user input of this component and all child components. The component will be updated with error information. This version adds errors for
	 * non-matching passwords if password verification is enabled.
	 * @return The current state of {@link #isValid()} as a convenience.
	 */
	public boolean validate() {
		super.validate(); //validate the component normally
		if(isPasswordVerified() && !isPasswordMatch()) { //if we're verifying the password and it isn't valid
			final Notification notification = new Notification(MESSAGE_PASSWORD_UNVERIFIED, Notification.Severity.ERROR); //create an error notification
			passwordControl.setNotification(notification); //add the error notification to each password control
			passwordVerificationControl.setNotification(notification);
		}
		return isValid(); //return the current valid state
	}

	/** Clears the username and password values. */
	public void clearValues() {
		usernameControl.clearValue(); //clear the username value
		passwordControl.clearValue(); //clear the password value
	}
}
