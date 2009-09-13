/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.platform.web;

import java.io.*;
import java.net.URI;
import java.util.*;

import javax.xml.parsers.*;

import com.globalmentor.collections.DecoratorReadWriteLockMap;
import com.globalmentor.collections.PurgeOnWriteSoftValueHashMap;
import com.globalmentor.net.ContentType;

import static com.globalmentor.io.Charsets.*;
import static com.globalmentor.java.Objects.*;
import com.globalmentor.log.Log;
import static com.globalmentor.text.xml.XML.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;

import com.guiseframework.component.Component;
import com.guiseframework.component.TextBox;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**Strategy for rendering a text component as an XHTML <code>&lt;div&gt;</code> element.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebTextBoxDepictor<C extends TextBox> extends AbstractSimpleWebComponentDepictor<C>
{

	/**The document prefix to wrap around an XHTML fragment.*/
	private final static String XHTML11_FRAGMENT_DOCUMENT_PREFIX=	//TODO fix; this doesn't create valid XHTML; it needs an internal DIV, but we need to then get the contents of the DIV rather than the BODY, which we could do using an ID
		"<?xml version='1.0'?>"+
/*TODO del; we don't validate, so why do we need a doctype?		"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>"+*/
		"<html xmlns='http://www.w3.org/1999/xhtml'>"+
		"<head><title>XHTML Fragment Document</title></head>"+
		"<body>";

	/**The document suffix to wrap around an XHTML fragment.*/
	private final static String XHTML11_FRAGMENT_DOCUMENT_SUFFIX=
		"</body>"+
		"</html>";

	/**A thread-safe cache of softly-referenced XML documents keyed to hashes of the strings with which the documents are associated.*/
	private final static Map<Integer, CachedDocument> cachedDocumentMap=new DecoratorReadWriteLockMap<Integer, CachedDocument>(new PurgeOnWriteSoftValueHashMap<Integer, CachedDocument>());

	/**Default constructor using the XHTML <code>&lt;div&gt;</code> element.*/
	public WebTextBoxDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//represent <xhtml:div>
	}

	/**Updates the views of any children.
	This version does not call the super version, because all child rendering is controlled by this version.
	@exception IOException if there is an error updating the child views.
	*/
	protected void depictChildren() throws IOException
	{
//TODO del Log.trace("ready to load text");
		final C component=getDepictedObject();	//get the component
		final String text=component.getText();	//get the component text, if any
//TODO del Log.trace("text loaded");
		if(text!=null)	//if the component has text
		{
			final String resolvedText=component.getSession().dereferenceString(text);	//resolve the text
			final ContentType contentType=component.getTextContentType();	//get the content type of the text
			final boolean isXML=isXML(contentType);	//see if the content type is for XML
			final boolean isXMLExternalParsedEntity=isXMLExternalParsedEntity(contentType);	//see if the content type is for an XML external parsed entity
			if(isXML || isXMLExternalParsedEntity)	//if the text is XML or an XML external parsed entity
			{
				final String xmlText;	//we'll determine the XML text to process
				if(isXMLExternalParsedEntity)	//if this is an XML external parsed entity
				{
					if(!XHTML_XML_EXTERNAL_PARSED_ENTITY_SUBTYPE.equals(contentType.getSubType()))	//if this is not an XHTML external parsed entity (an XHTML fragment), it's a fragment we don't know what to do with
					{
						throw new AssertionError("Unsupported fragment media type: "+contentType);
					}
						//TODO probably make sure that the external parsed entity is just a fragment---is it legal to have a whole document as an external parsed entity?
					xmlText=XHTML11_FRAGMENT_DOCUMENT_PREFIX+resolvedText+XHTML11_FRAGMENT_DOCUMENT_SUFFIX;	//wrap the fragment in an XHTML 1.1 document
				}
				else	//if this is just standard XML (i.e. the whole XHTML document, if necessary)
				{
					xmlText=resolvedText;	//just use the text as-is
				}
				try
				{
					final Integer xmlTextHash=new Integer(xmlText.hashCode());	//get the hash code of the string
//TODO del					CachedDocument cachedDocument=null;	//we'll see if we can find a cached document for the XML text
						//don't synchronize on the cache, which would cause delays when blocking to load new documents
						//the race condition is benign; at the worse, a document might be loaded several times during initial caching
					CachedDocument cachedDocument=cachedDocumentMap.get(xmlTextHash);	//see if there is an XML document for this string already cached
					if(cachedDocument!=null)	//if we supposedly have a cached document
					{
						if(!xmlText.equals(cachedDocument.getText()))	//if the cached text isn't identical to the text we're using (which is possible because we're keying on hash codes, which aren't guaranteed to be unique)
						{
							cachedDocument=null;	//don't used the cached document
						}
					}
					final Document document;	//we'll use the cached document if we can
					if(cachedDocument!=null)	//if we have a cached document for this text
					{
Log.debug("cache hit for text", xmlTextHash);
						document=cachedDocument.getDocument();	//use the document
					}
					else	//if there is no cached document
					{					
Log.debug("cache miss for text", xmlTextHash);
	//				TODO del Log.trace("ready to parse text");
	//				TODO del Log.trace("creating document builder factory");
					
						final DocumentBuilder documentBuilder=createDocumentBuilder(true);	//create a new namespace-aware document builder
	//				TODO del Log.trace("getting bytes");
						final byte[] bytes=xmlText.getBytes(UTF_8_CHARSET);
	//				TODO del Log.trace("creating input stream");
						final InputStream inputStream=new ByteArrayInputStream(bytes);
	//				TODO del Log.trace("parsing input stream");
						document=documentBuilder.parse(inputStream);	//parse the document; this goes *very* quickly after removing the "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>" from the wrapper
						cachedDocumentMap.put(xmlTextHash, new CachedDocument(xmlText, document));	//cache this parsed XML document along with its text
					}
//				TODO del Log.trace("text parsed");
					final Element rootElement;	//we'll find which element to start with
					if(isHTML(contentType))	//if this is an HTML document
					{
						//TODO fix; this throws a NullPointerException final Element bodyElement=getBodyElement(document);	//see if there is a body element
						Element bodyElement=null;	//TODO 
						final NodeList bodyElements=document.getElementsByTagNameNS(XHTML_NAMESPACE_URI.toString(), "body");
						if(bodyElements.getLength()>0)
						{
							bodyElement=(Element)bodyElements.item(0);
						}
						rootElement=bodyElement!=null ? bodyElement : document.getDocumentElement();	//use the body element if there is one
					}
					else	//if this is any other XML document
					{
						rootElement=document.getDocumentElement();	//use the document element
					}
//				TODO del Log.trace("ready to render text");
					updateElementContent(rootElement);	//update the contents of the root element
//				TODO del Log.trace("text rendered");
				}
				catch(final SAXException saxException)	//we don't expect parsing errors
				{
					throw new AssertionError(saxException);	//TODO maybe change to throwing an IOException
				}
/*TODO del
				catch(final URISyntaxException uriSyntaxException)
				{
					throw new AssertionError(uriSyntaxException);	//TODO fix better
				}
*/
				catch(final IOException ioException)	//if there is an I/O exception
				{
					throw new AssertionError(ioException);	//TODO fix better
				}
			}
			else	//if the text is not XML
			{
				writeText(resolvedText, component.getTextContentType());	//write the text appropriately for its content type				
			}
		}
	}

	/**Renders an XML element.
	If there is a child component with constraints indicating the same ID as this element, that child component will be rendered in place of this element.
	@exception IOException if there is an error rendering the component.
	*/
	protected void updateElement(final Element element) throws IOException
	{
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final C component=getDepictedObject();	//get the component
		final String id=element.getAttributeNS(null, "id");	//see if this element has an ID attribute TODO check for the HTML namespace on the element, maybe; use a constant
		final Component childComponent=id!=null ? component.getLayout().getComponentByID(id) : null;	//if this element has an ID, see if we have a component bound to that ID
		if(childComponent!=null)	//if we have a component bound to the given ID
		{
			childComponent.depict();	//tell the child component to update its view
		}
		else	//if there is no child component to replace this element
		{
			final boolean isEmpty=!element.hasChildNodes();	//find out if this is an empty element TODO see if we need to special-case some HTML elements so as not to confuse IE
			final String elementNamespaceURIString=element.getNamespaceURI();	//get the element namespace
			final URI elementNamespaceURI=elementNamespaceURIString!=null ? URI.create(elementNamespaceURIString) : null;	//get the namespace URI, if there is one
			final String elementLocalName=element.getLocalName();	//get the element local name
			depictContext.writeElementBegin(elementNamespaceURI, elementLocalName, isEmpty);	//begin this element
			final NamedNodeMap attributes=element.getAttributes();	//get the element attributes
			final int attributeCount=attributes.getLength();	//get the number of attributes
			for(int attributeIndex=0; attributeIndex<attributeCount; ++attributeIndex)	//for each attribute
			{
				final Attr attribute=(Attr)attributes.item(attributeIndex);	//get a reference to this attribute
				final String attributeNamespaceURIString=attribute.getNamespaceURI();	//get the attribute namespace
				final URI attributeNamespaceURI=attributeNamespaceURIString!=null ? URI.create(attributeNamespaceURIString) : null;	//get the namespace URI, if there is one
				final String attributeLocalName=attribute.getLocalName();	//get the attribute local name
				if(!XMLNS_NAMESPACE_URI.equals(attributeNamespaceURI) || !ATTRIBUTE_XMLNS.equals(attributeLocalName))	//don't write xmlns:xmlns attributes TODO fix; this is to keep xmlns:xmlns from being redefined, because apparently the Java parse things xmlns="" is in the XMLNS namespace
				{
					final String attributeValue=attribute.getNodeValue();	//get the value of the attribute
					depictContext.writeAttribute(attributeNamespaceURI, attributeLocalName, attributeValue);	//write this attribute
				}
			}
			updateElementContent(element);	//update the element content
			depictContext.writeElementEnd(elementNamespaceURI, elementLocalName);	//end this element
		}
	}
	
	/**Renders the content of an XML element.
	@param element The element the content of which should be rendered.
	@exception IOException if there is an error rendering the component.
	*/
	protected void updateElementContent(final Element element) throws IOException
	{
		final NodeList childNodes=element.getChildNodes();	//get the list of child nodes
		final int childNodeCount=childNodes.getLength();	//find the number of child nodes
		for(int childNodeIndex=0; childNodeIndex<childNodeCount; ++childNodeIndex)	//for each child node
		{
			final Node childNode=childNodes.item(childNodeIndex);	//get a reference to this child node
			final int nodeType=childNode.getNodeType();	//get a reference to this node type
			switch(nodeType)	//see which type of node this is
			{
				case Node.ELEMENT_NODE:
					updateElement((Element)childNode);	//update this child element
					break;
				case Node.TEXT_NODE:
					getPlatform().getDepictContext().write(((org.w3c.dom.Text)childNode).getNodeValue());	//write the text of the text node
					break;
			}
		}
	}

	/**Cached information associating a pre-parsed XML document with a string.
	@author Garret Wilson
	*/
	protected static class CachedDocument
	{

		/**The text for which there is a cached XML document.*/
		private final String text;

			/**@return The text for which there is a cached XML document.*/
			public String getText() {return text;}

		/**The cached XML document.*/
		private final Document document;

			/**@return The cached XML document.*/
			public Document getDocument() {return document;}

		/**Text and XML document constructor.
		@param text The text for which there is a cached XML document.
		@param document The cached XML document.
		*/
		public CachedDocument(final String text, final Document document)
		{
			this.text=checkInstance(text, "Text cannot be null.");
			this.document=checkInstance(document, "Document cannot be null.");
		}
	}

}
