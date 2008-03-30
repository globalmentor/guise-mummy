package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;

import com.guiseframework.component.*;

import static com.globalmentor.text.xml.xhtml.XHTML.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**An abstract XHTML component depictor that writes component decorations such as label and error.
The component is wrapped in an ourter XHTML <code>&lt;div&gt;</code> element.
The body of the component will be preceded by a label and succeeded by an error.
The provided element, if any, will be used as the body of the component with the {@link #COMPONENT_BODY_CLASS_SUFFIX} ID and style suffix. 
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public abstract class AbstractDecoratedWebComponentDepictor<C extends Component> extends AbstractWebComponentDepictor<C>
{

	/**The URI of the XML namespace of the body element, or <code>null</code> if there is no namespace.*/
	private final URI bodyNamespaceURI;

		/**Determines the namespace URI of the body XML element.
		@return The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
		*/
		public URI getBodyNamespaceURI() {return bodyNamespaceURI;}

	/**The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.*/
	private final String bodyLocalName;

		/**Determines the local name of the body XML element.
		@return The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
		*/
		public String getBodyLocalName() {return bodyLocalName;}

	/**Whether an empty body element can be created if there is no content.*/
	private final boolean emptyBodyElementAllowed;

		/**Determines whether an empty body element can be created if there is no content.
		@return Whether an empty body element can be created if there is no content.
		@see #getBodyLocalName()
		*/
		public boolean isEmptyBodyElementAllowed() {return emptyBodyElementAllowed;}

	/**The state of this controller's body XML element, if there is one.*/ 
	private WebDepictContext.ElementState bodyElementState=null;
		
	/**Default constructor with no element representation for the body.*/
	public AbstractDecoratedWebComponentDepictor()
	{
		this(null, null);	//construct the strategy with no element representation
	}

	/**Body element namespace and local name constructor that doesn't create an empty element, even if there is no content.
	@param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	@param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	*/
	public AbstractDecoratedWebComponentDepictor(final URI namespaceURI, final String localName)
	{
		this(namespaceURI, localName, false);	//don't allow an empty element
	}

	/**Body element namespace and local name constructor.
	@param bodyNamespaceURI The URI of the XML namespace of the body element, or <code>null</code> if there is no namespace.
	@param bodyLocalName The local name of the body element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	@param isBodyEmptyElementAllowed Whether an empty body element can be created if there is no content.
	*/
	public AbstractDecoratedWebComponentDepictor(final URI bodyNamespaceURI, final String bodyLocalName, final boolean isBodyEmptyElementAllowed)
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//represent <xhtml:div>
		this.bodyNamespaceURI=bodyNamespaceURI;
		this.bodyLocalName=bodyLocalName;
		this.emptyBodyElementAllowed=isBodyEmptyElementAllowed;
	}

	/**Begins the rendering process.
	This version calls {@link #writeDecoratorBegin()}.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictBegin() throws IOException
	{
		super.depictBegin();	//do the default beginning rendering
		writeDecoratorBegin();	//write the decorator attributes
		final String bodyLocalName=getBodyLocalName();	//get the body element local name, if there is one
		if(bodyLocalName!=null)	//if there is a body element name
		{
			bodyElementState=getDepictContext().writeElementBegin(getBodyNamespaceURI(), bodyLocalName, isEmptyBodyElementAllowed());	//start the element
			writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX);	//write the ID and class for the body element
			writeStyleAttribute(getBodyStyles());	//write the component's body styles
		}
		else	//if there is no body element name
		{
			bodyElementState=null;	//show that we have no body element state
		}
	}

	/**Writes the beginning part of the outer decorator element.
	This version writes the beginning decorator items, such as the label, if any.
	@exception IOException if there is an error rendering the component.
	*/
	protected void writeDecoratorBegin() throws IOException
	{
		writeIDClassAttributes(null, null);	//write the ID and class attributes with no prefixes or suffixes
		writeDirectionAttribute();	//write the component direction, if this component specifies a direction
		writeLabel(decorateID(getPlatform().getDepictIDString(getDepictedObject().getDepictID()), null, COMPONENT_BODY_CLASS_SUFFIX));	//write the label for the body, if there is a label		
	}

	/**Ends the rendering process.
	This version writes the end of the body element, if there is one, and the ending decorator items, such as the error, if any.
	This version calls {@link #writeDecoratorEnd()}.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictEnd() throws IOException
	{
		if(bodyElementState!=null && bodyElementState.isOpen())	//if the body element is open
		{
			final String bodyNamespaceURIString=bodyElementState.getNamespaceURI();	//get the namespace of the open element
			getDepictContext().writeElementEnd(bodyNamespaceURIString!=null ? URI.create(bodyNamespaceURIString) : null, bodyElementState.getLocalName());	//end the element
		}
		bodyElementState=null;	//release the element state
		writeDecoratorEnd();	//write the decorator ending elements
		super.depictEnd();	//do the default ending rendering
	}

	/**Writes the ending part of the outer decorator element.
	This version writes the ending decorator items, such as the error message, if any.
	@exception IOException if there is an error rendering the component.
	@see #writeErrorMessage()
	*/
	protected void writeDecoratorEnd() throws IOException
	{
		writeErrorMessage();	//write the error message, if any
	}
}
