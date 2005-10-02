package com.javaguise.event;

import java.net.URI;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;

/**A abstract object that listens for action events and keeps information for modally navigating in response.
@param <S> The type of the event source.
@param <R> The type of modal result the modal frame produces.
@author Garret Wilson
*/
public class AbstractNavigateModalActionListener<S, R> extends AbstractNavigateActionListener<S>
{

	/**The listener to respond to the end of modal interaction.*/
	private final ModalNavigationListener<R> modalListener;

		/**@return The listener to respond to the end of modal interaction.*/
		public final ModalNavigationListener<R> getModelListener() {return getModelListener();}

	/**Constructs a listener to navigate modally to the provided path.
	@param navigationPath A path that is either relative to the application context path or is absolute.
	@param modalListener The listener to respond to the end of modal interaction.
	@exception NullPointerException if the given path and/or modal listener is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #AbstractNavigateActionListener(URI)}</code> should be used instead).
	*/
	public AbstractNavigateModalActionListener(final String navigationPath, final ModalNavigationListener<R> modalListener)
	{
		this(createPathURI(navigationPath), modalListener);	//create a URI from the path and construct the class
	}

	/**Constructs a listener to navigate modally to the provided URI.
	@param navigationURI The URI for navigation when the action occurs.
	@param modalListener The listener to respond to the end of modal interaction.
	@exception NullPointerException if the given navigation URI and/or modal listener is null.
	*/
	public AbstractNavigateModalActionListener(final URI navigationURI, final ModalNavigationListener<R> modalListener)
	{
		super(navigationURI);	//construct the parent class
		this.modalListener=checkNull(modalListener, "Modal listeners cannot be null");
	}

}
