package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;

import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

import com.guiseframework.component.Component;
import com.guiseframework.component.CompositeComponent;

/**Strategy for rendering a component as an XHTML <code>&lt;ol&gt;</code> element.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebListDepictor<C extends CompositeComponent> extends AbstractSimpleWebComponentDepictor<C>	//TODO finish, verify, and create corresponding component
{

	/**Default constructor using the XHTML <code>&lt;ol&gt;</code> element.*/
	public WebListDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_OL);	//represent <xhtml:ol>
	}

	/**Element namespace and local name constructor.
	@param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	@param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	*/
	public WebListDepictor(final URI namespaceURI, final String localName)
	{
		super(namespaceURI, localName);	//construct the parent class
	}

	/**Updates the views of any children.
	@exception IOException if there is an error updating the child views.
	*/
	protected void depictChildren() throws IOException
	{
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final C component=getDepictedObject();	//get the component
		depictContext.write("\n");	//format the output
			//don't do the default updating of child views, because we control generation wrapper elements around each child
		for(final Component childComponent:component.getChildren())	//for each child component
		{
			depictContext.writeIndent();	//write an indentation
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_LI);	//<xhtml:li>			
			childComponent.depict();	//update the child view
			depictContext.write("\n");	//format the output
			depictContext.writeIndent();	//write an indentation
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_LI);	//</xhtml:li>
			depictContext.write("\n");	//format the output
		}
	}
}
