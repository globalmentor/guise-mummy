package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.guiseframework.component.Component;

/**A component depictor that uses its top-level XHTML element as its main or body component.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public abstract class AbstractSimpleWebComponentDepictor<C extends Component> extends AbstractWebComponentDepictor<C>
{

	/**Default constructor with no element representation.*/
	public AbstractSimpleWebComponentDepictor()
	{
		this(null, null);	//construct the strategy with no element representation
	}

	/**Element namespace and local name constructor that doesn't create an empty element, even if there is no content.
	@param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	@param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	*/
	public AbstractSimpleWebComponentDepictor(final URI namespaceURI, final String localName)
	{
		this(namespaceURI, localName, false);	//don't allow an empty element
	}

	/**Element namespace and local name constructor.
	@param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	@param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	@param isEmptyElementAllowed Whether an empty element can be created if there is no content.
	*/
	public AbstractSimpleWebComponentDepictor(final URI namespaceURI, final String localName, final boolean isEmptyElementAllowed)
	{
		super(namespaceURI, localName, isEmptyElementAllowed);	//construct the parent class
	}

	/**Retrieves the styles for the outer element of the component.
	This version combines the body styles with the outer styles.
	@return The styles for the outer element of the component, mapped to CSS property names.
	@see AbstractWebComponentDepictor#getBodyStyles(XMLGuiseContext, Component)
	*/
	protected Map<String, Object> getOuterStyles()
	{
		final Map<String, Object> outerStyles=super.getOuterStyles();	//get the default outer styles
		outerStyles.putAll(getBodyStyles());	//add the styles for the body
		return outerStyles;	//return the combined styles		
	}

	/**Begins the rendering process.
	This version writes the body ID and class attributes, along with the direction attribute.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictBegin() throws IOException
	{
		super.depictBegin();	//do the default beginning rendering
		final String localName=getLocalName();	//get the element local name, if there is one
		if(localName!=null)	//if there is an element name
		{
			writeBodyIDClassAttributes(null, null);	//write the ID and class
			writeDirectionAttribute();	//write the component direction, if this component specifies a direction
		}
	}

}
