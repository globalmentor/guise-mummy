package com.guiseframework.converter;

import java.net.URI;

import static com.globalmentor.java.Objects.*;

import com.garretwilson.rdf.RDFName;
import com.garretwilson.rdf.RDFXMLGenerator;
import com.garretwilson.text.xml.XMLNamespacePrefixManager;

//TODO update to look up labels and definitions in some external OWL definition or something
//TODO update to parse the namespace prefix and look up a namespace URI fromt he RDF XML generator when converting back to URI

/**A converter that converts a URI from and to a string literal.
This converter does not support converting to URIs from all the formats it can produce.
If the reference URI has namespace URI and local name, the XML qualified name will be returned in <var>namespaceURI</var>:<var>localName</var> format;
otherwise, the reference URI itself will be returned.
By default the prefix is included.
@author Garret Wilson
@see Integer
*/
public class RDFURIStringLiteralConverter extends DefaultStringLiteralConverter<URI> 
{

	/**The strategy for generating XML from RDF.*/
	private final RDFXMLGenerator rdfXMLGenerator;

		/**@return The strategy for generating XML from RDF.*/
		protected final RDFXMLGenerator getRDFXMLGenerator() {return rdfXMLGenerator;}

	/**Whether a representation of the namespace should be included.*/
	private boolean namespaceIncluded=true;

		/**@return Whether a representation of the namespace should be included.*/
		public boolean isNamespaceIncluded() {return namespaceIncluded;}

		/**Sets whether the namespace should be represented.
		@param namespaceIncluded Whether a representation of the namespace should be included.
		*/
		public void setNamespaceIncluded(final boolean namespaceIncluded) {this.namespaceIncluded=namespaceIncluded;}
		
	/**Default constructor.*/
	public RDFURIStringLiteralConverter()
	{
		this(new XMLNamespacePrefixManager());	//construct the class with a default XML namespace prefix manager
	}

	/**XML namespace prefix manager constructor.
	@param xmlNamespacePrefixManager The object managing XML namespaces and prefixes.
	@excepion NullPointerException if the given XML namespace prefix manager is <code>null</code>.
	*/
	public RDFURIStringLiteralConverter(final XMLNamespacePrefixManager xmlNamespacePrefixManager)
	{
		this(new RDFXMLGenerator(xmlNamespacePrefixManager));//construct the class with a new RDF XML generator
	}
	
	/**XML RDF generator constructor.
	@param rdfXMLGenerator The RDF XML generator to use in converting URIs to strings.
	@exception NullPointerException if the given RDF XML generator is <code>null</code>.
	*/
	public RDFURIStringLiteralConverter(final RDFXMLGenerator rdfXMLGenerator)
	{
		super(URI.class);	//construct the parent class
		this.rdfXMLGenerator=checkInstance(rdfXMLGenerator, "RDF XML generator cannot be null.");
	}

	/**Converts a value from the value space to a literal value in the lexical space.
	If the reference URI has namespace URI and local name, the XML qualified name will be	returned in <var>namespaceURI</var>:<var>localName</var> format;
	otherwise, the reference URI itself will be returned.
	@param value The value in the value space to convert.
	@return The converted value in the lexical space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the value cannot be converted.
	*/
	public String convertValue(final URI value) throws ConversionException
	{
		if(value!=null)	//if a value was given
		{
			final RDFXMLGenerator rdfXMLGenerator=getRDFXMLGenerator();	//get the RDF XML generator
			if(isNamespaceIncluded())	//if we should include the namespace
			{
				return rdfXMLGenerator.getLabel(value);	//get a label for the value
			}
			else	//if we shouldn't include the namespace
			{
				final RDFName rdfName=rdfXMLGenerator.getRDFName(value);	//get the RDF name for this URI
				return rdfName!=null ? rdfName.getLocalName() : value.toString();	//return just the local name if we can; otherwise, return the entire URI as a string TODO improve to work with rdf:li, as RDFXMLGenerator.getLabel() does
			}
		}
		else	//if no value was given
		{
			return null;	//indicate that there was no value to convert
		}
	}
}
