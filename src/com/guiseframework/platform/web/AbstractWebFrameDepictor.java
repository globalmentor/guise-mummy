package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;

/**Abstract strategy for rendering a frame as a series of XHTML elements.
If the session changes a property, such as locale, orientation, or principal, it is assumed that the entire frame needs updating.
This depictor ignores changes to the {@link InputFocusGroupComponent#INPUT_FOCUS_STRATEGY_PROPERTY} and {@link InputFocusGroupComponent#INPUT_FOCUSED_COMPONENT_PROPERTY} properties.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class AbstractWebFrameDepictor<C extends Frame> extends AbstractWebComponentDepictor<C>
{

	/**Default constructor with no element representation.*/
	public AbstractWebFrameDepictor()
	{
		this(null, null);	//construct the strategy with no element representation
	}

	/**Element namespace and local name constructor that doesn't create an empty element, even if there is no content.
	@param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	@param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	*/
	public AbstractWebFrameDepictor(final URI namespaceURI, final String localName)
	{
		this(namespaceURI, localName, false);	//don't allow an empty element
	}

	/**Element namespace and local name constructor.
	@param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	@param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	@param isEmptyElementAllowed Whether an empty element can be created if there is no content.
	*/
	public AbstractWebFrameDepictor(final URI namespaceURI, final String localName, final boolean isEmptyElementAllowed)
	{
		super(namespaceURI, localName, isEmptyElementAllowed);	//construct the parent class
		getIgnoredProperties().add(Frame.INPUT_FOCUS_STRATEGY_PROPERTY);	//ignore changes in the focus strategy, because that shouldn't change the way information is updated 
		getIgnoredProperties().add(Frame.INPUT_FOCUSED_COMPONENT_PROPERTY);	//ignore the focused component changing, because this doesn't make the frame as a whole dirty---this is more of a book-keeping property
		//TODO eventually optimize frame label changing as we do for application frames
		getIgnoredProperties().add(GuiseSession.INPUT_STRATEGY_PROPERTY);	//ignore changes to GuiseSession.inputStrategy, because changes to the input strategy does not affect the frame's view
	}

	/**Called when the depictor is installed in a component.
	This implementation listens for changes in the session and in response marks the view as needing updated.
	@param component The component into which this view is being installed.
	@exception NullPointerException if the given component is <code>null</code>.
	@exception IllegalStateException if this view is already installed in a component.
	@see #getDepictedPropertyChangeListener()
	*/
	public void installed(final C component)
	{
		super.installed(component);	//install ourselves normally
		component.getSession().addPropertyChangeListener(getDepictedPropertyChangeListener());	//listen for session changes
	}

	/**Called when the depictor is uninstalled from a component.
	This implementation stops listening for session changes.
	@param component The component from which this view is being uninstalled.
	@exception NullPointerException if the given component is <code>null</code>.
	@exception IllegalStateException if this view is not installed in a component.
	@see #getDepictedPropertyChangeListener()
	*/
	public void uninstalled(final C component)
	{
		super.uninstalled(component);	//uninstall ourselves normally
		component.getSession().removePropertyChangeListener(getDepictedPropertyChangeListener());	//stop listening for session changes
	}

	/**Renders the body of the component.
	This version renders the content of the frame.
	@exception IOException if there is an error rendering the component.
	@see Frame#getContent()
	*/
	protected void depictBody() throws IOException
	{
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		depictContext.indent();	//indent the context
		try
		{
			final Component content=getDepictedObject().getContent();	//get the content of the frame
			if(content!=null)	//if there is content
			{
				content.depict();	//update the content
			}
		}
		finally
		{
			depictContext.unindent();	//always unindent the context
		}
	}

}
