package com.javaguise.demo;

import java.util.*;
import static java.util.Collections.*;

import com.javaguise.AbstractGuiseApplication;

/**Demonstration Guise application.
Copyright © 2005 GlobalMentor, Inc.
@author Garret Wilson
*/
public class DemoApplication extends AbstractGuiseApplication
{

	/**The synchronized list of application users.*/
	final private List<DemoUser> users=synchronizedList(new ArrayList<DemoUser>());

		/**The synchronized list of application users.*/
		public List<DemoUser> getUsers() {return users;}

	/**Default constructor.
	This implemetation sets the locale to the JVM default.
	*/
	public DemoApplication()
	{
		super();	//construct the parent class
		users.add(new DemoUser("user1", "Jane", null, "Smith", "janesmith@example.com"));	//add example users
		users.add(new DemoUser("user2", "John", null, "Smith", "johnsmith@example.com"));
		users.add(new DemoUser("user3", "Jill", null, "Jones", "jilljones@example.com"));
	}
}
