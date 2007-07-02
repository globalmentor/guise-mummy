package com.guiseframework.platform.web;

import java.io.IOException;

import com.guiseframework.component.Message;

import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

/**Strategy for rendering a message component as an XHTML <code>&lt;div&gt;</code> element.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebMessageDepictor<C extends Message> extends AbstractDecoratedWebComponentDepictor<C>	//TODO del class if not needed
{

	/**Default constructor using the XHTML <code>&lt;div&gt;</code> element.*/
	public WebMessageDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//represent <xhtml:div>
	}

	/**Renders the body of the component.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictBody() throws IOException
	{
		super.depictBody();	//render the default main part of the component
		final C component=getDepictedObject();	//get the component
		final String message=component.getMessage();	//get the component message, if any
		if(message!=null)	//if the component has a message
		{
			writeText(getSession().resolveString(message), component.getMessageContentType());	//write the resolved message appropriately for its content type
		}
	}
}
