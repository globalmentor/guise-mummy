/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
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
import java.util.*;

import com.globalmentor.net.ContentType;
import com.globalmentor.text.xml.QualifiedName;

/**
 * Encapsulation of text/xml information related to the current depiction.
 * @author Garret Wilson
 */
public interface XMLDepictContext extends TextDepictContext
{

	/**
	 * Retrieves the qualified name of the given namespace and local name. If the namespace URI is not recognized, a new prefix will be generated for that
	 * namespace This method therefore works for attributes in the <code>null</code> namespace, but cannot work for elements in the <code>null</code> namespace
	 * because this would be ambiguous with elements in the the XHTML namespace.
	 * @param namespaceURI The URI of the XML namespace, or <code>null</code> if there is no namespace and there should be no prefix.
	 * @param localName The local name of the element or attribute with no prefix.
	 * @return The XML qualified name.
	 */
	public String getQualifiedName(final URI namespaceURI, final String localName);

	/**
	 * Writes a doctype along with an optional XML declaration to the string builder and sets the output content type. No system ID or public ID will be written.
	 * @param writeXMLDeclaration Whether an XML declaration should be included before the doctype.
	 * @param namespaceURI The URI of the XML namespace of document element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the document element with no prefix.
	 */
	public void writeDocType(final boolean writeXMLDeclaration, final URI namespaceURI, final String localName) throws IOException;

	/**
	 * Writes a doctype along with an optional XML declaration to the string builder and sets the output content type. No system ID or public ID will be written.
	 * @param writeXMLDeclaration Whether an XML declaration should be included before the doctype.
	 * @param namespaceURI The URI of the XML namespace of document element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the document element with no prefix.
	 * @param contentType The specific XML content type.
	 * @throws NullPointerException if the given content type is <code>null</code>.
	 */
	public void writeDocType(final boolean writeXMLDeclaration, final URI namespaceURI, final String localName, final ContentType contentType) throws IOException;

	/**
	 * Writes a doctype along with an optional XML declaration to the string builder and sets the output content type. The system ID and content type will be
	 * determined from the given public ID.
	 * @param writeXMLDeclaration Whether an XML declaration should be included before the doctype.
	 * @param namespaceURI The URI of the XML namespace of document element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the document element with no prefix.
	 * @param publicID The XML declaration public ID.
	 * @throws NullPointerException if the given public ID is <code>null</code>.
	 * @throws IllegalArgumentException if a system ID could not be determined from the given public ID.
	 */
	public void writeDocType(final boolean writeXMLDeclaration, final URI namespaceURI, final String localName, final String publicID) throws IOException;

	/**
	 * Writes a doctype along with an optional XML declaration to the string builder and sets the output content type.
	 * @param writeXMLDeclaration Whether an XML declaration should be included before the doctype.
	 * @param namespaceURI The URI of the XML namespace of document element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the document element with no prefix.
	 * @param publicID The XML declaration public ID, or <code>null</code> if none is used.
	 * @param systemID The XML declaration system ID, or <code>null</code> if one can be determined from the given public ID.
	 * @param contentType The specific XML content type, or <code>null</code> if a content type should be determined from the public ID; otherwise will default to
	 *          "text/xml".
	 * @throws IllegalArgumentException if a system ID was not provided or one could not be determined from the given public ID.
	 */
	public void writeDocType(final boolean writeXMLDeclaration, final URI namespaceURI, final String localName, String publicID, String systemID,
			ContentType contentType) throws IOException;

	/**
	 * Begins an XML element that will not be an empty element, even if it has no content.
	 * @param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the element with no prefix.
	 * @return The state of the element being written.
	 * @throws IOException if there is an error writing the information.
	 */
	public ElementState writeElementBegin(final URI namespaceURI, final String localName) throws IOException;

	/**
	 * Begins an XML element, specifying whether an empty element is allowed.
	 * @param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the element with no prefix.
	 * @param isEmptyElementAllowed Whether an empty element can be created if there is no content.
	 * @return The state of the element being written.
	 * @throws IOException if there is an error writing the information.
	 */
	public ElementState writeElementBegin(final URI namespaceURI, final String localName, final boolean isEmptyElementAllowed) throws IOException;

	/**
	 * Ends an XML element.
	 * @param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the element with no prefix.
	 * @return The state of the element being written.
	 * @throws NoSuchElementException if the element state stack is empty.
	 * @throws IllegalStateException if the given namespace URI and/or local name does not match that of the currently open element.
	 * @throws IOException if there is an error writing the information.
	 */
	public ElementState writeElementEnd(final URI namespaceURI, final String localName) throws IOException;

	/**
	 * Writes an attribute of an XML element. The attribute value will be properly encoded for XML.
	 * @param namespaceURI The URI of the XML namespace of the attribute, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the attribute with no prefix.
	 * @param value The unencoded value of the attribute.
	 * @throws NoSuchElementException if the element state stack is empty.
	 * @throws IOException if there is an error writing the information.
	 */
	public void writeAttribute(final URI namespaceURI, final String localName, final String value) throws IOException;

	/**
	 * Writes the beginning part of an XML comment.
	 * @throws IllegalStateException if the comment has already been opened but not closed.
	 * @throws IOException if there is an error writing the information.
	 * @see #writeCommentClose()
	 */
	public void writeCommentOpen() throws IOException;

	/**
	 * Writes the ending part of an XML comment.
	 * @throws IllegalStateException if the comment has not been opened or has already been closed.
	 * @throws IOException if there is an error writing the information.
	 * @see #writeCommentOpen()
	 */
	public void writeCommentClose() throws IOException;

	/**
	 * Writes an XML comment. This method ensures that any open beginning tag has been closed.
	 * @param comment The comment to write.
	 * @throws IOException if there is an error writing the information.
	 * @see #writeCommentOpen()
	 * @see #writeCommentClose()
	 */
	public void writeComment(final String comment) throws IOException;

	/**
	 * The state of rendering for a particular element.
	 * @author Garret Wilson
	 */
	public static class ElementState extends QualifiedName
	{
		/** The map of attribute values keyed to attribute qualified names. */
		private final Map<QualifiedName, String> attributeMap = new HashMap<QualifiedName, String>();

		/** @return The map of attribute values keyed to attribute qualified names. */
		public Map<QualifiedName, String> getAttributeMap()
		{
			return attributeMap;
		}

		/** The string builder that holds the element content being collected for depiction. */
		private final StringBuilder depictStringBuilder = new StringBuilder();

		/** @return The string builder that holds the element content being collected for depiction. */
		public StringBuilder getDepictStringBuilder()
		{
			return depictStringBuilder;
		}

		/** Whether an empty element can be created if there is no content. */
		private final boolean emptyElementAllowed;

		/** @return Whether an empty element can be created if there is no content. */
		public boolean isEmptyElementAllowed()
		{
			return emptyElementAllowed;
		}

		/** Whether the element has been opened but not closed. */
		protected boolean open = true;

		/** Whether the element has been opened but not closed. */
		public boolean isOpen()
		{
			return open;
		}

		/**
		 * Constructor.
		 * @param namespaceURI The namespace URI, or <code>null</code> if there is no namespace URI..
		 * @param qname The combined <var>prefix</var>:<var>localName</var> qualified name.
		 * @param isEmptyElementAllowed Whether an empty element can be created if there is no content.
		 */
		public ElementState(final URI namespaceURI, final String qname, final boolean isEmptyElementAllowed)
		{
			super(namespaceURI, qname); //construct the parent class
			this.emptyElementAllowed = isEmptyElementAllowed;
		}
	}
}
