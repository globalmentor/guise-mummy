package com.garretwilson.guise.demo;

import java.security.Principal;

/**A demo user class for the Guise demo.
@author Garret Wilson
*/
public class User implements Principal
{

	/**The user ID.*/
	private final String id;

		/**@return The user ID.*/
		public String getID() {return id;}

    /**@return the name of this principal.
    This implementation returns the user ID.
    This method is provided to implement the {@link Principal} interface.
    @see #getID()
    */
		public String getName()	{return getID();}

	/**The first name of the user.*/
	private final String firstName;

		/**@return The first name of the user.*/
		public String getFirstName() {return firstName;}

	/**The middle name of the user, or <code>null</code> if there is no middle name.*/
	private final String middleName;

		/**@return The middle name of the user, or <code>null</code> if there is no middle name.*/
		public String getMiddleName() {return middleName;}

	/**The last name of the user.*/
	private final String lastName;

		/**@return The last name of the user.*/
		public String getLastName() {return lastName;}

	/**The email address of the user.*/
	private final String email;

		/**@return The email address of the user.*/
		public final String getEmail() {return email;}

	/**Constructor.
	@param id The user ID.
	@param firstName The first name of the user.
	@param middleName The middle name of the user, or <code>null</code> if there is no middle name.
	@param lastName The last name of the user.
	@param email The email address of the user.
	@exception NullPointerException if the ID, first name, last name, and/or email address is <code>null</code>.
	*/
	public User(final String id, final String firstName, final String middleName, final String lastName, final String email)
	{
		if(id==null || firstName==null || lastName==null || email==null)	//if anything besides the middle name is null
		{
			throw new NullPointerException("Only the user middle name is optional");
		}
		this.id=id;
		this.firstName=firstName;
		this.middleName=middleName;
		this.lastName=lastName;
		this.email=email;
	}

	/**@return <code>true</code> if the given object is another user with the same ID.
	@see #getID()
	*/
	public boolean equals(final Object object)
	{
		return object instanceof User && getID().equals(((User)object).getID());	//see if the other object is a user with the same ID
	}

	/**@return The hash code for this user.
	This implementation returns the hash code of the ID.
	@see #getID()
	*/
	public int hashCode()
	{
		return getID().hashCode();	//return the hash code of the ID, as the ID should be unique
	}

	/**@return A string representation of this user in the form "(<var>id</var>) <var>lastName</var>, <var>firstName</var> <var>middleName</var>"*/
	public String toString()
	{
		final StringBuilder stringBuilder=new StringBuilder();	//create a new string builder
		stringBuilder.append('(').append(getID()).append(')').append(' ');	//(id) 
		stringBuilder.append(getLastName()).append(',').append(' ').append(getFirstName());	//lastName, firstName
		final String middleName=getMiddleName();	//get the user's middle name
		if(middleName!=null)	//if there is a middle name
		{
			stringBuilder.append(' ').append(middleName);	// middle name
		}
		return stringBuilder.toString();	//return the string representation we created of the user
	}
}
