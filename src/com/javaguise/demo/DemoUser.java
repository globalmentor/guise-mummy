package com.javaguise.demo;

import java.security.Principal;
import java.text.Collator;

/**A user class for the Guise demo.
@author Garret Wilson
*/
public class DemoUser implements Principal, Comparable<DemoUser>
{

	/**The collator for comparing user names.*/
	protected final static Collator COLLATOR=Collator.getInstance();

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

	/**The password of the user.*/
	private final char[] password;

		/**@return The password of the user.*/
		public char[] getPassword() {return password;}

	/**The email address of the user.*/
	private String email;

		/**@return The email address of the user.*/
		public String getEmail() {return email;}

		/**Sets the email address of the user.
		@param email The new email address of the user.
		@exception NullPointerException if the email is <code>null</code>.
		*/
		public void setEmail(final String email)
		{
			if(email==null)	//if the email is null
			{
				throw new NullPointerException("Only the user middle name is optional");
			}
			this.email=email;	//save the email
		}

	/**Whether the user is authorized.*/
	private boolean authorized=true;

		/**@return Whether the user is authorized.*/
		public boolean isAuthorized() {return authorized;}

		/**Sets whether the user is authorized.
		@param authorized Whether the user is authorized.
		*/
		public void setAuthorized(final boolean authorized) {this.authorized=authorized;}

	/**Constructor.
	@param id The user ID.
	@param firstName The first name of the user.
	@param middleName The middle name of the user, or <code>null</code> if there is no middle name.
	@param lastName The last name of the user.
	@param password The password of the user.
	@param email The email address of the user.
	@exception NullPointerException if the ID, first name, last name, and/or email address is <code>null</code>.
	*/
	public DemoUser(final String id, final String firstName, final String middleName, final String lastName, final char[] password, final String email)
	{
		if(id==null || firstName==null || lastName==null || password==null || email==null)	//if anything besides the middle name is null
		{
			throw new NullPointerException("Only the user middle name is optional");
		}
		this.id=id;
		this.firstName=firstName;
		this.middleName=middleName;
		this.lastName=lastName;
		this.password=password;
		this.email=email;
	}

	/**Compares users based upon lastName+firstName+middleName+ID.
	A more internationalized 
	@param user The user with which to compare.
	@return A value indicating whether the first user is alphabetically less than, equal to, or greater than the second.
	*/
	public int compareTo(final DemoUser user)
	{
		return COLLATOR.compare(getLastName()+getFirstName()+getMiddleName()+getID(), user.getLastName()+user.getFirstName()+user.getMiddleName()+user.getID());	//compare names
	}

	/**@return <code>true</code> if the given object is another user with the same ID.
	@see #getID()
	*/
	public boolean equals(final Object object)
	{
		return object instanceof DemoUser && getID().equals(((DemoUser)object).getID());	//see if the other object is a user with the same ID
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
