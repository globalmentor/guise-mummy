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

package io.guise.framework.demo;

import java.net.URI;
import java.security.Principal;
import java.util.*;

import com.globalmentor.net.URIPath;

import io.guise.framework.AbstractGuiseApplication;

/**
 * Demonstration Guise application. Copyright © 2005 GlobalMentor, Inc. Demonstrates custom Guise applications, setting application resource bundles, and custom
 * authorization.
 * @author Garret Wilson
 */
public class DemoApplication extends AbstractGuiseApplication {

	/** The application-relative navigation path to the edit user modal panel. */
	public static final URIPath EDIT_USER_PANEL_NAVIGATION_PATH = new URIPath("edituser");
	/** The application-relative navigation path to the restricted panel. */
	public static final URIPath RESTRICTED_PANEL_NAVIGATION_PATH = new URIPath("restricted");

	/** The synchronized list of application users. */
	final private List<DemoUser> users = Collections.synchronizedList(new ArrayList<DemoUser>());

	/** @return The synchronized list of application users. */
	public List<DemoUser> getUsers() {
		return users;
	}

	/** The counter for creating unique user IDs. */
	private int userCounter = 0;

	/** @return A new unique user ID. */
	public synchronized String generateUserID() {
		return "user" + (++userCounter); //return a user ID in the form userX
	}

	/** Default constructor with no identifier. */
	public DemoApplication() {
		this(null);
	}

	/**
	 * URI constructor. The URI identifier may or may not be the URI at which the application can be accessed.
	 * @param uri The URI for the application, or <code>null</code> if there is no identifier.
	 */
	public DemoApplication(final URI uri) {
		super(uri); //construct the parent class
		setResourceBundleBaseName(getClass().getPackage().getName() + "/resources"); //set the resource bundle
		users.add(new DemoUser(generateUserID(), "Jane", null, "Smith", "password".toCharArray(), "janesmith@example.com")); //add example users
		users.add(new DemoUser(generateUserID(), "John", null, "Smith", "password".toCharArray(), "johnsmith@example.com"));
		users.add(new DemoUser(generateUserID(), "Jill", null, "Jones", "password".toCharArray(), "jilljones@example.com"));
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version returns the user with the given ID.
	 * </p>
	 */
	@Override
	protected DemoUser getPrincipal(final String id) {
		synchronized(users) { //don't allow others to modify the users while we access them
			for(final DemoUser user : users) { //look at each user (this is an inefficient user lookup routine, but sufficient for demonstration purposes)
				if(user.getID().equals(id)) { //if this user has the correct ID
					return user; //return the user
				}
			}
		}
		return null; //indicate that we couldn't find a user with the given ID
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version returns the password of the given user.
	 * </p>
	 */
	@Override
	protected char[] getPassword(final Principal principal) {
		if(principal instanceof DemoUser) { //if this principal is one of the users we know about
			return ((DemoUser)principal).getPassword(); //return this user's password
		} else { //if we don't know the user
			return null; //indicate that we don't know the principal's password 
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * For the application path "restricted", this version authorized only users marked as authorized.
	 * </p>
	 */
	@Override
	protected boolean isAuthorized(final URIPath applicationPath, final Principal principal, final String realm) {
		if("restricted".equals(applicationPath)) { //if this is the "restricted" application path
			return principal instanceof DemoUser && ((DemoUser)principal).isAuthorized(); //only authorize authorized principals
		} else { //for all other application paths
			return super.isAuthorized(applicationPath, principal, realm); //allow default access (authorized)
		}
	}

}
