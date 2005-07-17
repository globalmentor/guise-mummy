package com.javaguise.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.net.URI;

import com.javaguise.model.LabelModel;

/**A root-level component such as a window or an HTML page.
The title is specified by the frame model's label.
@author Garret Wilson
*/
public interface Frame<C extends Frame<C>> extends ModelComponent<LabelModel, C>, Box<C>
{
	/**The URI of the referrer bound property.*/
	public final static String REFERRER_URI_PROPERTY=getPropertyName(Frame.class, "referrerURI");

	/**@return The URI of the referring frame or other entity, or <code>null</code> if no referring URI is known.*/
	public URI getReferrerURI();

	/**Sets the URI of the referrer.
	This is a bound property
	@param newReferrerURI The URI of the referring frame or other entity, or <code>null</code> if no referring URI is known.
	@see #REFERRER_URI_PROPERTY
	*/
	public void setReferrerURI(final URI newReferrerURI);

}
