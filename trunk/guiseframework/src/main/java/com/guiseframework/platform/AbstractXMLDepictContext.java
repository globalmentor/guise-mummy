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

package com.guiseframework.platform;

import java.io.IOException;
import java.net.URI;
import java.security.*;
import java.util.*;

import com.globalmentor.java.Objects;
import com.globalmentor.java.Strings;
import com.globalmentor.net.ContentType;
import com.globalmentor.text.xml.QualifiedName;
import com.globalmentor.text.xml.XMLNamespacePrefixManager;
import com.guiseframework.Destination;
import com.guiseframework.GuiseSession;
import com.guiseframework.platform.web.WebPlatform;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.java.Strings.*;
import static com.globalmentor.security.MessageDigests.*;
import static com.globalmentor.text.xml.XML.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;
import static com.globalmentor.util.Base64.*;

/**Abstract encapsulation of text/xml information related to the current depiction.
@author Garret Wilson
*/
public abstract class AbstractXMLDepictContext extends AbstractTextDepictContext implements XMLDepictContext
{

	/**The message digest for creating hashes on the platform.*/
	private final MessageDigest messageDigest;

		/**@return The message digest for creating hashes on the platform.*/
		protected MessageDigest getMessageDigest() {return messageDigest;}

	/**The stack of elements states.*/
	private final LinkedList<ElementState> elementStateStack=new LinkedList<ElementState>();

	/**The manager of prefixes paired with XML namespaces.*/
	private final XMLNamespacePrefixManager xmlNamespacePrefixManager;

		/**@return The manager of prefixes paired with XML namespaces.*/
		protected XMLNamespacePrefixManager getXMLNamespacePrefixManager() {return xmlNamespacePrefixManager;}

	/**Whether attributes should be generated representing the hash of XML attributes and content.*/
	private boolean hashAttributesGenerated=false;
	
		/**@return Whether attributes should be generated representing the hash of XML attributes and content.*/
		protected boolean isHashAttributesGenerated() {return hashAttributesGenerated;}

		/**Sets whether attributes should be generated representing the hash of XML attributes and content.
		@param generateHashAttribute Whether hash attributes should be generated.
		*/
		protected void setHashAttributesGenerated(final boolean generateHashAttributes) {this.hashAttributesGenerated=generateHashAttributes;}

		/**@return The qualified name to use for the attribute hash attribute.*/
		protected abstract String getAttributeHashAttributeQualifiedName();
	
		/**@return The qualified name to use for the content hash attribute.*/
		protected abstract String getContentHashAttributeQualifiedName();

	/**Guise session constructor.
	@param session The Guise user session of which this context is a part.
	@param destination The destination with which this context is associated.
	@exception NullPointerException if the given session and/or destination is null.
	@exception IOException If there was an I/O error loading a needed resource.
	*/
	public AbstractXMLDepictContext(final GuiseSession session, final Destination destination) throws IOException
	{
		super(session, destination);	//construct the parent class
		xmlNamespacePrefixManager=new XMLNamespacePrefixManager();	//create a new XML namespace prefix manager
		try
		{
			messageDigest=MessageDigest.getInstance(SHA_ALGORITHM);	//get a message digest instance using SHA for creating hashes of element attributes and content
		}
		catch(final NoSuchAlgorithmException noSuchAlgorithmException)	//we should always support SHA message digests
		{
			throw new AssertionError(noSuchAlgorithmException);
		}
	}

	/**Clears all data collected for depiction.
	This version also clears the stack of element states.
	*/
	public void clearDepictText()
	{
		elementStateStack.clear();	//clear the element states
		super.clearDepictText();	//do the default clearing, which will clear the root string buffer
	}

	/**The string builder that holds the current content being collected, though not necessarily all the content collected.
	The string builder returned is appropriate for adding content, but may not be a complete representation of all the text collected.
	This version returns the string builder of the current element state, if there is an element state available.
	@return The string builder that holds the current content being collected for depiction.
	*/
	public StringBuilder getDepictStringBuilder()
	{
		return hasElementState() ? getElementState().getDepictStringBuilder() : super.getDepictStringBuilder();	//if there is an element state, return its string builder; otherwise, return the default string builder
	}	
	
	/**The characters that should be encoded in XML.
	@see #XML_REPLACEMENT_STRINGS
	*/
	private final static char[] XML_REPLACE_CHARACTERS=new char[]{'&', '<', '>', '"'};

	/**The strings that replace the XML replace characters, in order.
	@see #XML_REPLACE_CHARACTERS
	*/
	private final static String[] XML_REPLACEMENT_STRINGS=new String[]{"&amp;", "&lt;", "&gt;", "&quot;"};
	
	/**Encodes text information for writing.
	This version encodes XML characters.
	@param string The text information to encode.
	@return The encoded text.
	*/
	protected String encode(final String string)	//TODO check somewhere for invalid XML characters
	{
		return super.encode(replace(string, XML_REPLACE_CHARACTERS, XML_REPLACEMENT_STRINGS));	//encode the special XML characters, if there are any, and then do the default encoding
	}
	
	/**Starts a new element by pushing the given element state onto the stack.
	@param elementState The state of the element to begin.
	*/
	private void pushElementState(final ElementState elementState)
	{
		elementStateStack.addLast(elementState);	//push the element state onto the top of the stack		
	}

	/**Ends an element by popping the element state from the stack.
	The serialization of the element is added to the next string builder.
	@return The state of the element that has ended.
	@exception NoSuchElementException if the element state stack is empty.
	*/
	private ElementState popElementState()
	{
		final ElementState elementState=elementStateStack.removeLast();	//remove the top element state from the stack, as we finished the element
		elementState.open=false;	//show that this element is no longer open TODO maybe remove this entire facility, because no views seem to close the elements early in order to write other content
		final boolean generateHashAttributes=isHashAttributesGenerated();	//see if we should generate hash attributes
		final String qname=elementState.getQName();	//get the element qname in prefix:localName form
		final String elementContent=elementState.getDepictStringBuilder().toString();	//get the content of the element
		final StringBuilder stringBuilder=getDepictStringBuilder();	//get the remaining string builder, which is either the string builder from the next open element or the root string builder
		stringBuilder.append(TAG_START);	//<
		stringBuilder.append(qname);	//prefix:localName
		final String guiseAttributeHashQualifiedName=getAttributeHashAttributeQualifiedName();	//determine the qualifed name to be used for the attribute hash attribute
		final String guiseContentHashQualifiedName=getContentHashAttributeQualifiedName();	//determine the qualifed name to be used for the content hash attribute
		final MessageDigest messageDigest=getMessageDigest();	//get the message digest
		final Map<QualifiedName, String> attributeMap=elementState.getAttributeMap();	//get the element attributes
		if(!attributeMap.isEmpty())	//if there are attributes, write them and generate a hash for them
		{
			if(generateHashAttributes)	//if we're generating hashes
			{
				messageDigest.reset();	//reset the message digest so that we can use it for hashing the attributes
			}
			for(final Map.Entry<QualifiedName, String> attribute:attributeMap.entrySet())	//for each attribute
			{
					//TODO use constants for all of this
				final QualifiedName attributeQualifiedName=attribute.getKey();	//get the attribute qualified name
				final String attributeName=attributeQualifiedName.getQName();	//prefix:localName
				stringBuilder.append(' ');	//separate attributes
				stringBuilder.append(attribute.getKey().getQName());	//prefix:localName
				stringBuilder.append('=');	//=
				stringBuilder.append('"');	//"
				final String attributeValue=encode(getAttributeValue(elementState, attributeQualifiedName, attribute.getValue()));	//get the attribute value, modifying it if needed, and encode it
				if(generateHashAttributes)	//if we're generating hashes
				{
					update(messageDigest, attributeName, attributeValue);	//update our message digest with the attribute name and value
				}
				stringBuilder.append(attributeValue);	//append the encoded value
				stringBuilder.append('"');	//"
			}
			if(generateHashAttributes)	//if we're generating hashes
			{			
				final byte[] digestBytes=messageDigest.digest();	//get the final digest of the attributes
				stringBuilder.append(' ').append(guiseAttributeHashQualifiedName).append('=').append('"').append(encodeBytes(digestBytes)).append('"');	//guise:contentHash="base64Hex"
			}
		}
		if(generateHashAttributes && elementContent.length()>0)	//if there is content and we're generating hashes, add a guise:contentHash attribute
		{
			messageDigest.reset();	//reset the message digest so that we can use it for hashing the content
			final byte[] digestBytes=digest(messageDigest, elementContent);	//create a digest of the element content
			stringBuilder.append(' ').append(guiseContentHashQualifiedName).append('=').append('"').append(encodeBytes(digestBytes)).append('"');	//guise:contentHash="base64Hex"
		}		
		if(elementContent.length()==0 && elementState.isEmptyElementAllowed())	//if there is no content and we're allowed to make an empty element
		{
			stringBuilder.append(END_TAG_IDENTIFIER_CHAR);	//write the ending tag identifier
		}
		else	//if the element is not empty, or we're not allowed to create an empty element serialization
		{
			stringBuilder.append(TAG_END);	//>
			stringBuilder.append(elementContent);	//append the content collected for this element, if any
			stringBuilder.append(TAG_START);	//<
			stringBuilder.append(END_TAG_IDENTIFIER_CHAR);	//write the ending tag identifier
			stringBuilder.append(qname);	//prefix:localName		
		}
		stringBuilder.append(TAG_END);	//>
		return elementState;	//return the element state
	}

	/**Retrieves the value of a given attribute.
	This method is provided so that the platform may manipulate an attribute if needed.
	This version returns the given attribute value unmodified.
	@param elementQualifedName The qualified name of the element.
	@param attributeQualifiedName The qualified name of the attribute.
	@param attributeValue The default value of the attribute.
	@return The value of the attribute.
	*/
	protected String getAttributeValue(final QualifiedName elementQualifiedName, final QualifiedName attributeQualifiedName, final String attributeValue)	//TODO move code from HTTPServletGuiseContext
	{
		return attributeValue;	//return the attribute value with no modifications
	}

	/**Determines whether there is an element state on the stack.
	@return <code>true</code> if the element state stack is not empty.
	*/
	private boolean hasElementState()
	{
		return !elementStateStack.isEmpty();	//return whether the element state stack has content		
	}

	/**Determines the current element state.
	@return The current element state.
	@exception NoSuchElementException if the element state stack is empty.
	*/
	private ElementState getElementState()
	{
		return elementStateStack.getLast();	//return the top element state from the stack		
	}
	
	/**Whether a comment has been opened but not closed.*/
	private boolean isCommentOpen=false;

	/**Retrieves the qualified name of the given namespace and local name.
	If the namespace URI is not recognized, a new prefix will be generated for that namespace
	This method therefore works for attributes in the <code>null</code> namespace,
	but cannot work for elements in the <code>null</code> namespace because this would be ambiguous with elements in the the XHTML namespace.
	@param namespaceURI The URI of the XML namespace, or <code>null</code> if there is no namespace and there should be no prefix.
	@param localName The local name of the element or attribute with no prefix.
	@return The XML qualified name.
	*/
	public String getQualifiedName(final URI namespaceURI, final String localName)
	{
		final String prefix=namespaceURI!=null ? getXMLNamespacePrefixManager().getNamespacePrefix(namespaceURI.toString()) : null;	//if a namespace was given, look up the prefix
		return createQualifiedName(prefix, localName);	//return the qualified name
	}

	/**Writes a doctype along with an optional XML declaration to the string builder and sets the output content type.
	No system ID or public ID will be written.
	@param writeXMLDeclaration Whether an XML declaration should be included before the doctype.
	@param namespaceURI The URI of the XML namespace of document element, or <code>null</code> if there is no namespace.
	@param localName The local name of the document element with no prefix.
	*/
	public void writeDocType(final boolean writeXMLDeclaration, final URI namespaceURI, final String localName) throws IOException
	{
		writeDocType(writeXMLDeclaration, namespaceURI, localName, null, null, null);
	}

	/**Writes a doctype along with an optional XML declaration to the string builder and sets the output content type.
	No system ID or public ID will be written.
	@param writeXMLDeclaration Whether an XML declaration should be included before the doctype.
	@param namespaceURI The URI of the XML namespace of document element, or <code>null</code> if there is no namespace.
	@param localName The local name of the document element with no prefix.
	@param contentType The specific XML content type.
	@throws NullPointerException if the given content type is <code>null</code>.
	*/
	public void writeDocType(final boolean writeXMLDeclaration, final URI namespaceURI, final String localName, final ContentType contentType) throws IOException
	{
		writeDocType(writeXMLDeclaration, namespaceURI, localName, null, null, checkInstance(contentType, "Content type must be provided in this context."));
	}

	/**Writes a doctype along with an optional XML declaration to the string builder and sets the output content type.
	The system ID and content type will be determined from the given public ID.
	@param writeXMLDeclaration Whether an XML declaration should be included before the doctype.
	@param namespaceURI The URI of the XML namespace of document element, or <code>null</code> if there is no namespace.
	@param localName The local name of the document element with no prefix.
	@param publicID The XML declaration public ID.
	@exception NullPointerException if the given public ID is <code>null</code>.
	@exception IllegalArgumentException if a system ID could not be determined from the given public ID.
	*/
	public void writeDocType(final boolean writeXMLDeclaration, final URI namespaceURI, final String localName, final String publicID) throws IOException
	{
		writeDocType(writeXMLDeclaration, namespaceURI, localName, checkInstance(publicID, "Null public ID not allowed in this context."), null, null);	//check the public ID for null and determine defaults for the other values
	}

	/**Writes a doctype along with an optional XML declaration to the string builder and sets the output content type.
	If no public ID or system ID is provided, no public ID or system ID will be written.
	@param writeXMLDeclaration Whether an XML declaration should be included before the doctype.
	@param namespaceURI The URI of the XML namespace of document element, or <code>null</code> if there is no namespace.
	@param localName The local name of the document element with no prefix.
	@param publicID The XML declaration public ID, or <code>null</code> if none is used.
	@param systemID The XML declaration system ID, or <code>null</code> if one can be determined from the given public ID.
	@param contentType The specific XML content type, or <code>null</code> if a content type should be determined from the public ID; otherwise will default to "text/xml".
	@exception IllegalArgumentException if a system ID was not provided or one could not be determined from the given public ID.
	*/
	public void writeDocType(final boolean writeXMLDeclaration, final URI namespaceURI, final String localName, String publicID, String systemID, ContentType contentType) throws IOException
	{
		if(contentType==null)	//if no content type was provided
		{
			if(publicID!=null)	//if there is a document type public ID
			{
				contentType=getContentType(publicID);	//get the content type for this doctype public ID
			}
			if(contentType==null)	//if we still couldn't find a content type
			{
				contentType=XML_CONTENT_TYPE;	//use the generic "text/xml" content type
			}
		}		
			//set the content type
		if(contentType!=null)	//if a content type is specified
		{
//TODO del Log.trace("content type specified: ", contentType);
			if(contentType.match(XHTML_CONTENT_TYPE))	//if the preferred content type is "application/xhtml+xml"
			{
					//if the client doesn't accept "application/xhtml+xml" exactly
				if(!getPlatform().getClientProduct().isAcceptedContentType(XHTML_CONTENT_TYPE, false))
				{
					contentType=HTML_CONTENT_TYPE;	//step down to "text/html"
//TODO del Log.trace("stepping down to: ", contentType);
				}
			}
			setOutputContentType(contentType);	//set the content type of the response
		}
		final StringBuilder stringBuilder=getDepictStringBuilder();	//get the string builder
		if(writeXMLDeclaration)	//if we should write the XML declaration
		{
			stringBuilder.append(XML_DECL_START);	//<?xml
			stringBuilder.append(SPACE_CHAR);
			stringBuilder.append(VERSIONINFO_NAME);	//version
			stringBuilder.append(EQUAL_CHAR);	//=
			stringBuilder.append(DOUBLE_QUOTE_CHAR);	//"
			stringBuilder.append(XML_VERSION);	//1.0
			stringBuilder.append(DOUBLE_QUOTE_CHAR);	//"
			stringBuilder.append(SPACE_CHAR);
			stringBuilder.append(ENCODINGDECL_NAME);	//encoding
			stringBuilder.append(EQUAL_CHAR);	//=
			stringBuilder.append(DOUBLE_QUOTE_CHAR);	//"
			stringBuilder.append(getOutputCharacterEncoding().toString());	//encoding
			stringBuilder.append(DOUBLE_QUOTE_CHAR);	//"
				//G***fix standalone writing here
			stringBuilder.append(XML_DECL_END);	//?>
			stringBuilder.append('\n');
		}
			//write the document type declaration
		if(systemID==null)	//if we don't have a system ID, try to get one from the public ID
		{
			if(publicID!=null)	//if we have a public ID
			{
				systemID=getDefaultSystemID(publicID);	//try to determine the system ID from the public ID
			}
		}
		stringBuilder.append(DOCTYPE_DECL_START);	//<!DOCTYPE
		stringBuilder.append(SPACE_CHAR);
		stringBuilder.append(localName);	//root element TODO check the namespace and add a prefix if necessary, or make a note that this is the default namespace so that later writes can know when generating XML qualified names
		if(systemID!=null)	//if a system ID is given
		{
			stringBuilder.append(SPACE_CHAR);
			if(publicID!=null)	//if there is a public ID
			{
				stringBuilder.append(PUBLIC_ID_NAME);	//PUBLIC
				stringBuilder.append(SPACE_CHAR);
				stringBuilder.append(DOUBLE_QUOTE_CHAR);	//"
				stringBuilder.append(publicID);						//public ID
				stringBuilder.append(DOUBLE_QUOTE_CHAR);	//"
				stringBuilder.append(SPACE_CHAR);
			}
			else	//if there is no public ID
			{
				stringBuilder.append(SYSTEM_ID_NAME);	//SYSTEM
				stringBuilder.append(SPACE_CHAR);
			}
			stringBuilder.append(DOUBLE_QUOTE_CHAR);	//"
			stringBuilder.append(systemID);	//always  write the system literal
			stringBuilder.append(DOUBLE_QUOTE_CHAR);	//"
		}
		stringBuilder.append(DOCTYPE_DECL_END);	//>
		stringBuilder.append('\n');
	}
	
	/**Begins an XML element that will not be an empty element, even if it has no content.
	@param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	@param localName The local name of the element with no prefix.
	@return The state of the element being written.
	@exception IOException if there is an error writing the information.
	*/
	public ElementState writeElementBegin(final URI namespaceURI, final String localName) throws IOException
	{
		return writeElementBegin(namespaceURI, localName, false);	//write the beginning of an element that cannot be empty
	}

	/**Begins an XML element, specifying whether an empty element is allowed.
	@param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	@param localName The local name of the element with no prefix.
	@param isEmptyElementAllowed Whether an empty element can be created if there is no content.
	@return The state of the element being written.
	@exception IOException if there is an error writing the information.
	*/
	public ElementState writeElementBegin(final URI namespaceURI, final String localName, final boolean isEmptyElementAllowed) throws IOException
	{
		final String qname=getQualifiedName(namespaceURI, localName);	//get the qualified name for this namespace and local name
		final ElementState elementState=new ElementState(namespaceURI!=null ? namespaceURI.toString() : null, qname, isEmptyElementAllowed);	//create a new element state
		pushElementState(elementState);	//push the element state onto the top of the stack
		return elementState;	//return the state of the element we just started
	}

	/**Ends an XML element.
	@param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	@param localName The local name of the element with no prefix.
	@return The state of the element being written.
	@exception NoSuchElementException if the element state stack is empty.
	@exception IllegalStateException if the given namespace URI and/or local name does not match that of the currently open element.
	@exception IOException if there is an error writing the information.
	*/
	public ElementState writeElementEnd(final URI namespaceURI, final String localName) throws IOException
	{
		final ElementState elementState=popElementState();	//pop the current element state from the stack
		if(!Objects.equals(elementState.getNamespaceURI(), namespaceURI!=null ? namespaceURI.toString() : null) || !elementState.getLocalName().equals(localName))	//if the namespace and/or local name is not what we expect
		{
			throw new IllegalStateException("Ending namespace "+namespaceURI+" and local name "+localName+" do not match currently open element with namespace "+elementState.getNamespaceURI()+" and local name "+elementState.getLocalName());
		}
		return elementState;	//return the element state
	}

	/**Writes an attribute of an XML element.
	The attribute value will be properly encoded for XML.
	This implemention only recognizes the null namespace and the XML namespace, which is assumed to require a prefix of "xml".
	@param namespaceURI The URI of the XML namespace of the attribute, or <code>null</code> if there is no namespace.
	@param localName The local name of the attribute with no prefix.
	@param value The unencoded value of the attribute.
	@exception NoSuchElementException if the element state stack is empty.
	@exception IOException if there is an error writing the information.
	*/
	public void writeAttribute(final URI namespaceURI, final String localName, final String value) throws IOException
	{
		final String qname=getQualifiedName(namespaceURI, localName);	//get the qualified name for this namespace and local name
		getElementState().getAttributeMap().put(new QualifiedName(namespaceURI!=null ? namespaceURI.toString() : null, qname), value);	//store this attribute, keyed to the qualified name
	}

	/**Writes the beginning part of an XML comment.
	@exception IllegalStateException if the comment has already been opened but not closed.
	@exception IOException if there is an error writing the information.
	@see #writeCommentClose()
	*/
	public void writeCommentOpen() throws IOException
	{
		if(isCommentOpen)	//if a comment is already open
		{
			throw new IllegalStateException("Comment is already open.");
		}
		final StringBuilder stringBuilder=getDepictStringBuilder();	//get the string builder
		stringBuilder.append(COMMENT_START);	//<!--
		isCommentOpen=true;	//show that we opened a comment
	}

	/**Writes the ending part of an XML comment.
	@exception IllegalStateException if the comment has not been opened or has already been closed.
	@exception IOException if there is an error writing the information.
	@see #writeCommentOpen()
	*/
	public void writeCommentClose() throws IOException
	{
		if(!isCommentOpen)	//if a comment is not open
		{
			throw new IllegalStateException("No comment is open.");
		}
		final StringBuilder stringBuilder=getDepictStringBuilder();	//get the string builder
		stringBuilder.append(COMMENT_END);	//-->
		isCommentOpen=false;	//show that we closed a comment
	}

	/**Encoded hyphens to replaces illegal "--" sequences within a comment.*/
	private final static String XML_COMMENT_ENCODED_HYPHENS=createCharacterReference('-')+createCharacterReference('-');
	
	/**Writes an XML comment.
	This method ensures that any open beginning tag has been closed.
	@param comment The comment to write.
	@exception IOException if there is an error writing the information.
	@see #writeCommentOpen()
	@see #writeCommentClose()
	*/
	public void writeComment(final String comment) throws IOException
	{
/*TODO should we check to see if a comment is already open?
		if(isBeginTagOpen())	//if a beginning tag is open
		{
			writeBeginTagClose(false);	//close the beginning tag for a non-empty element
		}
*/
		writeCommentOpen();	//open the comment
		final String encodedComment=Strings.replace(comment, COMMENT_END_PART1, XML_COMMENT_ENCODED_HYPHENS);	//replace any illegal sequence with its encoded counterpart
		getDepictStringBuilder().append(encodedComment);	//write the comment with no other encoding
		writeCommentClose();	//close the comment
	}

}
