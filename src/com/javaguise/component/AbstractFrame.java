package com.javaguise.component;

import java.net.URI;

import com.javaguise.component.layout.Layout;
import com.javaguise.model.LabelModel;
import com.javaguise.session.GuiseSession;
import com.garretwilson.lang.ObjectUtilities;

/**Abstract implementation of a frame.
@author Garret Wilson
*/
public abstract class AbstractFrame<C extends Frame<C>> extends AbstractModelBox<LabelModel, C> implements Frame<C>
{

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
			if(!ObjectUtilities.equals(referrerURI, newReferrerURI))	//if the value is really changing
			{
				final URI oldReferrerURI=referrerURI;	//get the old value
				referrerURI=newReferrerURI;	//actually change the value
				firePropertyChange(REFERRER_URI_PROPERTY, oldReferrerURI, newReferrerURI);	//indicate that the value changed
			}
		}

	/**Session, ID, layout, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@param model The component data model.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractFrame(final GuiseSession session, final String id, final Layout layout, final LabelModel model)
	{
		super(session, id, layout, model);	//construct the parent class
	}
}
