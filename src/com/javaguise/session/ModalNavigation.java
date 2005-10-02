package com.javaguise.session;

import java.net.URI;

import com.javaguise.event.ModalNavigationListener;

import static com.garretwilson.lang.ObjectUtilities.*;

/**The encapsulation of a point of modal navigation.
@param <R> The type of modal result the modal frame produces.
@author Garret Wilson
*/
public class ModalNavigation<R> extends Navigation
{

	/**The listener to respond to the end of modal interaction.*/
	private final ModalNavigationListener<R> modalListener;

		/**@return The listener to respond to the end of modal interaction.*/
		public ModalNavigationListener<R> getModalListener() {return modalListener;}

	/**Creates an object encapsulating a point of modal navigation.
	@param oldNavigationURI The old point of navigation, with an absolute path.
	@param newNavigationURI The new point of navigation, with an absolute path.
	@param modalListener The listener to respond to the end of modal interaction.
	@exception NullPointerException if one of the navigation URIs is <code>null</code>, or does not contain a path.
	@exception IllegalArgumentException if one of the given navigation URIs contains a relative path.
	*/
	public ModalNavigation(final URI oldNavigationURI, final URI newNavigationURI, final ModalNavigationListener<R> modalListener)
	{
		super(oldNavigationURI, newNavigationURI);	//construct the parent class
		this.modalListener=checkNull(modalListener, "Modal listener cannot be null.");
	}
}
