package com.javaguise.demo;

import java.util.*;

import com.javaguise.AbstractGuiseApplication;

/**Demonstration Guise application.
Copyright © 2005 GlobalMentor, Inc.
@author Garret Wilson
*/
public class DemoApplication extends AbstractGuiseApplication
{

	/**The synchronized list of application users.*/
	final private List<DemoUser> users=Collections.synchronizedList(new ArrayList<DemoUser>());

		/**The synchronized list of application users.*/
		public List<DemoUser> getUsers() {return users;}

	/**The counter for creating unique user IDs.*/
	private int userCounter=0;

		/**@return A new unique user ID.*/
		public synchronized String generateUserID()
		{
			return "user"+(++userCounter);	//return a user ID in the form userX
		}

	/**Default constructor.
	This implemetation sets the locale to the JVM default.
	*/
	public DemoApplication()
	{
		super();	//construct the parent class
		users.add(new DemoUser(generateUserID(), "Jane", null, "Smith", "janesmith@example.com"));	//add example users
		users.add(new DemoUser(generateUserID(), "John", null, "Smith", "johnsmith@example.com"));
		users.add(new DemoUser(generateUserID(), "Jill", null, "Jones", "jilljones@example.com"));
	}
}
