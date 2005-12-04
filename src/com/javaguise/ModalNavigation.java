package com.javaguise;

import java.net.URI;

import com.javaguise.component.ModalNavigationPanel;
import com.javaguise.event.ModalNavigationListener;

import static com.garretwilson.lang.ObjectUtilities.*;

/**The encapsulation of a point of modal navigation.
@param <P> The type of navigation panel used for navigation.
@author Garret Wilson
*/
public class ModalNavigation<P extends ModalNavigationPanel<?, ?>> extends Navigation
{

	/**The listener to respond to the end of modal interaction.*/
	private final ModalNavigationListener<P> modalListener;

		/**@return The listener to respond to the end of modal interaction.*/
		public ModalNavigationListener<P> getModalListener() {return modalListener;}

	/**Creates an object encapsulating a point of modal navigation.
	@param oldNavigationURI The old point of navigation, with an absolute path.
	@param newNavigationURI The new point of navigation, with an absolute path.
	@param modalListener The listener to respond to the end of modal interaction.
	@exception NullPointerException if one of the navigation URIs is <code>null</code>, or does not contain a path.
	@exception IllegalArgumentException if one of the given navigation URIs contains a relative path.
	*/
	public ModalNavigation(final URI oldNavigationURI, final URI newNavigationURI, final ModalNavigationListener<P> modalListener)
	{
		super(oldNavigationURI, newNavigationURI);	//construct the parent class
		this.modalListener=checkNull(modalListener, "Modal listener cannot be null.");
	}
}
