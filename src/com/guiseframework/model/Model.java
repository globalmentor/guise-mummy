package com.guiseframework.model;

import javax.mail.internet.ContentType;

import com.globalmentor.beans.*;
import com.globalmentor.text.Text;
import com.globalmentor.text.xml.xhtml.XHTML;

/**Base interface for all component models.
@author Garret Wilson
*/
public interface Model extends PropertyBindable, PropertyConstrainable
{

	/**A content type of <code>text/plain</code>.*/
	public final static ContentType PLAIN_TEXT_CONTENT_TYPE=Text.TEXT_PLAIN_CONTENT_TYPE;

	/**A content type of <code>application/xhtml+xml</code>.*/
	public final static ContentType XHTML_CONTENT_TYPE=XHTML.XHTML_CONTENT_TYPE;
	
	/**A content type of <code>application/xhtml+xml-external-parsed-entity</code>.*/
	public final static ContentType XHTML_FRAGMENT_CONTENT_TYPE=XHTML.XHTML_FRAGMENT_CONTENT_TYPE;

}
