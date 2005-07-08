package com.garretwilson.guise.component;

import java.net.URI;
import java.util.*;

import com.garretwilson.guise.model.DefaultLabelModel;
import com.garretwilson.guise.model.LabelModel;
import com.garretwilson.guise.session.GuiseSession;
import com.garretwilson.util.*;

/**Abstract implementation of a frame.
@author Garret Wilson
*/
public abstract class AbstractFrame<C extends Frame<C>> extends AbstractModelComponent<LabelModel, C> implements Frame<C>
{

	/**The component representing the frame's content, or <code>null</code> if there is no content component.*/
	private Component<?> content=null;

		/**@return The component representing the frame's content, or <code>null</code> if there is no content component.*/
		public Component<?> getContent() {return content;}

		/**Sets the content component.
		This is a bound property
		@param newContent The component representing this frame's content, <code>null</code> if this frame has no content.
		@exception IllegalArgumentException if the content component already has a parent.
		@see Frame#CONTENT_PROPERTY
		*/
		public void setContent(final Component<?> newContent)
		{
			if(content!=newContent)	//if the value is really changing
			{
				final Component<?> oldContent=content;	//get the old value
				if(newContent!=null)	//if new content is provided
				{
					if(newContent.getParent()!=null)	//if this component has already been added to component
					{
						throw new IllegalArgumentException("Component "+content+" already has parent, "+content.getParent()+".");
					}
				}
				content=newContent;	//actually change the value
				if(oldContent!=null)	//if there was old content
				{
					assert oldContent.getParent()==this : "Old content component being removed had different parent component than expected.";
					oldContent.setParent(null);	//tell the old content it no longer has a parent
				}
				if(newContent!=null)	//if new content is provided
				{
					newContent.setParent(this);	//tell the new content this frame is its parent
				}
				firePropertyChange(CONTENT_PROPERTY, oldContent, newContent);	//indicate that the value changed
			}
		}

		/**@return Whether this component has children.*/
		public boolean hasChildren()
		{
			return getContent()!=null;	//we have children of we have a content component
		}

		/**Returns an iterator to child components. 
		This implementation returns an iterator to the single content component, if there is one.
		@return An iterator to child components.
		@see #getContent()
		*/
		public Iterator<Component<?>> iterator()
		{
			final Iterator<Component<?>> iterator;	//we'll determine the children and store them here
			final Component<?> content=getContent();	//get the content component
			if(content!=null)	//if we have a content component
			{
				iterator=new ObjectIterator<Component<?>>(content);	//create an iterator to the single content component
			}
			else	//if we have no content component
			{
				iterator=new EmptyIterator<Component<?>>();	//we'll send back an iterator to no components 
			}
			return iterator;	//return the children we found
		}

		/**The URI of the referring frame or other entity, or <code>null</code> if no referring URI is known.*/
		private URI referrerURI;

			/**@return The URI of the referring frame or other entity, or <code>null</code> if no referring URI is known.*/
			public URI getReferrerURI() {return referrerURI;}

			/**Sets the URI of the referrer.
			This is a bound property
			@param newReferrerURI The URI of the referring frame or other entity, or <code>null</code> if no referring URI is known.
			@see Frame#REFERRER_URI_PROPERTY
			*/
			public void setReferrerURI(final URI newReferrerURI)
			{
				if(referrerURI!=newReferrerURI)	//if the value is really changing
				{
					final URI oldReferrerURI=referrerURI;	//get the old value
					referrerURI=newReferrerURI;	//actually change the value
					firePropertyChange(REFERRER_URI_PROPERTY, oldReferrerURI, newReferrerURI);	//indicate that the value changed
				}
			}

	/**Session constructor.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractFrame(final GuiseSession<?> session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractFrame(final GuiseSession<?> session, final LabelModel model)
	{
		this(session, (String)null, model);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractFrame(final GuiseSession<?> session, final String id)
	{
		this(session, id, (Component<?>)null);	//default to no content component
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractFrame(final GuiseSession<?> session, final String id, final LabelModel model)
	{
		this(session, id, null, model);	//default to flowing vertically
	}

	/**Session and content component constructor.
	@param session The Guise session that owns this component.
	@param content The component representing this frame's content, <code>null</code> if this frame has no content.
	@exception NullPointerException if the given session and/or content component is <code>null</code>.
	*/
	public AbstractFrame(final GuiseSession<?> session, final Component<?> content)
	{
		this(session, null, content);	//construct the component with the content, indicating that a default ID should be used
	}

	/**Session, content component, and model constructor.
	@param session The Guise session that owns this component.
	@param content The component representing this frame's content, <code>null</code> if this frame has no content.
	@param model The component data model.
	@exception NullPointerException if the given session, content component, and/or model is <code>null</code>.
	*/
	public AbstractFrame(final GuiseSession<?> session, final Component<?> content, final LabelModel model)
	{
		this(session, null, content, model);	//construct the component with the content, indicating that a default ID should be used
	}

	/**Session, ID, and content component constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param content The component representing this frame's content, <code>null</code> if this frame has no content.
	@exception NullPointerException if the given session and/or content component is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractFrame(final GuiseSession<?> session, final String id, final Component<?> content)
	{
		this(session, id, content, new DefaultLabelModel(session));	//construct the class with a default model
	}

	/**Session, ID, content component, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param content The component representing this frame's content, <code>null</code> if this frame has no content.
	@param model The component data model.
	@exception NullPointerException if the given session, content component, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractFrame(final GuiseSession<?> session, final String id, final Component<?> content, final LabelModel model)
	{
		super(session, id, model);	//construct the parent class
		this.content=content;	//save the content component
	}
}
