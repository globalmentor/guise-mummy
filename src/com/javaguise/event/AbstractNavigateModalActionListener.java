package com.javaguise.event;

import java.net.URI;

import com.javaguise.component.ModalNavigationPanel;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;

/**A abstract object that listens for action events and keeps information for modally navigating in response.
@param <S> The type of the event source.
@param <P> The type of navigation panel for modal navigation.
@author Garret Wilson
*/
public class AbstractNavigateModalActionListener<S, P extends ModalNavigationPanel<?, ?>> extends AbstractNavigateActionListener<S>
{

	/**The listener to respond to the end of modal interaction.*/
	private final ModalNavigationListener<P> modalListener;

		/**@return The listener to respond to the end of modal interaction.*/
		public final ModalNavigationListener<P> getModelListener() {return getModelListener();}

	/**Constructs a listener to navigate modally to the provided path.
	@param navigationPath A path that is either relative to the application context path or is absolute.
	@param modalListener The listener to respond to the end of modal interaction.
	@exception NullPointerException if the given path and/or modal listener is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #AbstractNavigateModalActionListener(URI, ModalNavigationListener)}</code> should be used instead).
	*/
	public AbstractNavigateModalActionListener(final String navigationPath, final ModalNavigationListener<P> modalListener)
	{
		this(createPathURI(navigationPath), modalListener);	//create a URI from the path and construct the class
	}

	/**Constructs a listener to navigate modally to the provided URI.
	@param navigationURI The URI for navigation when the action occurs.
	@param modalListener The listener to respond to the end of modal interaction.
	@exception NullPointerException if the given navigation URI and/or modal listener is null.
	*/
	public AbstractNavigateModalActionListener(final URI navigationURI, final ModalNavigationListener<P> modalListener)
	{
		super(navigationURI);	//construct the parent class
		this.modalListener=checkNull(modalListener, "Modal listeners cannot be null");
	}

}
