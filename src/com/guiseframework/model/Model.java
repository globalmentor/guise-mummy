package com.guiseframework.model;

import javax.mail.internet.ContentType;

import com.garretwilson.beans.*;
import com.garretwilson.text.TextConstants;
import com.garretwilson.text.xml.xhtml.XHTMLConstants;
import com.guiseframework.GuiseSession;

/**Base interface for all component models.
A model should never fire a model-related event directly. It should rather create a postponed event and queue that event with the session.
@author Garret Wilson
@see GuiseSession#queueEvent(com.garretwilson.event.PostponedEvent)
*/
public interface Model extends PropertyBindable
{

	/**A content type of <code>text/plain</code>.*/
	public final static ContentType PLAIN_TEXT_CONTENT_TYPE=TextConstants.TEXT_PLAIN_CONTENT_TYPE;

	/**A content type of <code>application/xhtml+xml</code>.*/
	public final static ContentType XHTML_CONTENT_TYPE=XHTMLConstants.XHTML_CONTENT_TYPE;
	
	/**A content type of <code>application/xhtml+xml-external-parsed-entity</code>.*/
	public final static ContentType XHTML_FRAGMENT_CONTENT_TYPE=XHTMLConstants.XHTML_FRAGMENT_CONTENT_TYPE;

	/**@return The Guise session that owns this model.*/
	public GuiseSession getSession();

}
