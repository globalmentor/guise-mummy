package com.guiseframework.component;

import java.beans.PropertyVetoException;

import com.guiseframework.component.layout.*;
import com.guiseframework.theme.Theme;
import com.guiseframework.validator.*;

/**Panel to gather password authentication information.
@author Garret Wilson
*/
public class PasswordAuthenticationPanel extends LayoutPanel
{

	/**The username text control.*/
	private final TextControl<String> usernameControl;

	/**The password text control.*/
	private final TextControl<char[]> passwordControl;

	/**Default constructor with a default vertical flow layout.*/
	public PasswordAuthenticationPanel()
	{
		this(new FlowLayout(Flow.PAGE));	//default to flowing vertically
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public PasswordAuthenticationPanel(final Layout<?> layout)
	{
		super(layout);	//construct the parent class

			//username
		usernameControl=new TextControl<String>(String.class);	//create the username text control
		usernameControl.setLabel(Theme.LABEL_USERNAME);	//set the username control label
		usernameControl.setValidator(new RegularExpressionStringValidator(".+", true));	//require at least a single character
		add(usernameControl);	//add the ID control to the panel

			//password
		passwordControl=new TextControl<char[]>(char[].class);	//create the password text control
		passwordControl.setLabel(Theme.LABEL_PASSWORD);	//set the password control label
		passwordControl.setMasked(true);	//mask the password input
		passwordControl.setValidator(new RegularExpressionCharArrayValidator(".+", true));	//require at least a single character
		add(passwordControl);	//add the password control to the panel

	}

	/**@return The current username entered, or <code>null</code> if there is no username entered.*/
	public String getUsername()
	{
		return usernameControl.getValue();
	}

	/**Sets the username shown in the panel.
	@param username The username to show.
	@exception PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	*/
	public void setUsername(final String username) throws PropertyVetoException
	{
		usernameControl.setValue(username);
	}

	/**@return The current password entered, or <code>null</code> if there is no password entered.*/
	public char[] getPassword()
	{
		return passwordControl.getValue();
	}

	/**Sets the password shown in the panel.
	@param password The password to show.
	@exception PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	*/
	public void setPassword(final char[] password) throws PropertyVetoException
	{
		passwordControl.setValue(password);
	}
}
