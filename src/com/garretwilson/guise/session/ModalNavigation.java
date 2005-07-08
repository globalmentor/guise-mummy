package com.garretwilson.guise.session;

import java.net.URI;

import com.garretwilson.guise.component.ModalFrame;
import com.garretwilson.guise.event.ActionListener;
import static com.garretwilson.lang.ObjectUtilities.*;

/**The encapsulation of a point of modal navigation.
@author Garret Wilson
*/
public class ModalNavigation extends Navigation
{

	/**The listener to respond to the end of modal interaction.*/
	private final ActionListener<ModalFrame<?, ?>> endModalListener;

		/**@return The listener to respond to the end of modal interaction.*/
		protected final ActionListener<ModalFrame<?, ?>> getEndModalListener() {return endModalListener;}

	/**Creates an object encapsulating a point of modal navigation.
	@param navigationURI The point of modal navigation.
	@param endModalListener The listener to respond to the end of modal interaction.
	@exception NullPointerException if the given navigation URI and/or listener is <code>null</code>.
	*/
	public ModalNavigation(final URI navigationURI, final ActionListener<ModalFrame<?, ?>> endModalListener)
	{
		super(navigationURI);	//construct the parent class
		this.endModalListener=checkNull(endModalListener, "End modal listener cannot be null.");
	}
}
