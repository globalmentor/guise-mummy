/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.globalmentor.util;

import java.io.*;
import java.net.*;
import java.util.*;

import com.globalmentor.io.IO;
import com.globalmentor.model.Locales;
import com.globalmentor.net.Resource;
import com.globalmentor.rdf.*;
import com.globalmentor.urf.*;

import static com.globalmentor.io.Files.*;
import static com.globalmentor.java.Java.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.model.Locales.*;
import static com.globalmentor.net.URIs.*;
import static com.globalmentor.rdf.RDF.*;
import static com.globalmentor.rdf.RDFResources.*;
import static com.globalmentor.text.xml.XML.*;
import static com.globalmentor.urf.TURF.*;
import static com.globalmentor.util.PropertiesUtilities.*;

/**Utilities for working with resource bundles.
@author Garret Wilson
*/
public class ResourceBundles
{

	/**The map of resource bundles softly keyed to resource bundle base paths. This serves as a soft cache to resource bundles.*/
//TODO fix caching; the problem is currently how to verify the same loader and resource path while keeping a weak reference to the loader private final static Map<String, ResourceBundle> resourceBundleMap=synchronizedMap(new SoftValueHashMap<String, ResourceBundle>());

	/**The format in which a resource bundle is serialized, in order of preference.
	@author Garret Wilson
	*/
	private enum ResourceBundleFormat
	{
		/**The resource bundle is serialized in a TURF file.*/
		TURF(TURF_NAME_EXTENSION),
		
		/**The resource bundle is serialized in an RDF+XML file.*/
		RDFXML(RDF_NAME_EXTENSION),
		
		/**The resource bundle is serialized in an XML file.*/
		XML(XML_NAME_EXTENSION),
		
		/**The resource bundle is serialized in a properties file.*/
		PROPERTIES(PROPERTIES_NAME_EXTENSION);

		/**The extension for this resource bundle file type.*/
		private final String extension;

			/**@return The extension for this resource bundle file type.*/
			public String getExtension() {return extension;}

		/**Extension constructor.
		@param extension The extension for this resource bundle file type.
		@exception NullPointerException if the given extension is <code>null</code>.
		*/
		private ResourceBundleFormat(final String extension)
		{
			this.extension=checkInstance(extension, "Extension cannot be null.");
		}
	}

	/**Loads a resource bundle for a given base name and locale.
	This implementation recognizes properties stored in <code>.turf</code>, <code>.rdf</code>, <code>.xml</code>, and <code>.properties</code> files, searching in that order.
	TURF property files are only used if a given TURF resource I/O instance is provided for reading from the file.
	RDF property files are only used if a given RDF resource I/O instance is provided for reading from the file.
	@param baseName The base name of the resource bundle, which is a fully qualified class name, such as "myProperties".
	@param locale The locale for which a resource bundle is desired.
	@param loader The class loader from which to load the resource bundle.
	@param parent The parent resource bundle, or <code>null</code> if there should be no parent for resolving resources.
	@param turfResourceIO The I/O support for loading resources from a TURF representation, or <code>null</code> if TURF resource bundles are not supported.
	@param rdfResourceIO The I/O support for loading resources from an RDF+XML serialization, or <code>null</code> if RDF resource bundles are not supported.
	@param rdfPropertyNamespaceURI The namespace of the properties to gather, using the property local name as the map entry key, or <code>null</code> if RDF resource bundles are not supported.
	@return A resource bundle for the given base name and locale.
	@exception MissingResourceException if no resource bundle for the specified base name can be found, or if there is an error loading the resource bundle.
	*/
	public static ResourceBundle getResourceBundle(final String baseName, final Locale locale, final ClassLoader loader, final ResourceBundle parent, final IO<? extends URFResource> urfResourceIO, final IO<? extends RDFResource> rdfResourceIO, final URI rdfPropertyNamespaceURI) throws MissingResourceException
	{
    final String basePath=baseName.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);	//create a base path from base name
    final ResourceBundleFormat[] resourceBundleFormats=ResourceBundleFormat.values();	//get the available resource bundle formats
    final int resourceBundleFormatCount=resourceBundleFormats.length;	//see how many resource bundle formats there are
    final String[] paths=new String[resourceBundleFormatCount];	//create an array of paths to try
    for(int resourceBundleFormatIndex=0; resourceBundleFormatIndex<resourceBundleFormatCount; ++resourceBundleFormatIndex)	//for each resource bundle format
    {
    	paths[resourceBundleFormatIndex]=addExtension(basePath, resourceBundleFormats[resourceBundleFormatIndex].getExtension());	//create a path to test for this resource bundle format
    }
		for(int depth=3; depth>=0; --depth)	//try different locales, starting with the most specific, until we find an input stream
		{
			for(int resourceBundleFormatIndex=0; resourceBundleFormatIndex<resourceBundleFormatCount; ++resourceBundleFormatIndex)	//for each resource bundle format
			{
				final ResourceBundleFormat resourceBundleFormat=resourceBundleFormats[resourceBundleFormatIndex];	//get this resource bundle format
				if(resourceBundleFormat==ResourceBundleFormat.TURF && urfResourceIO==null)	//if this is a TURF file, only use it if we have I/O for the file
				{
					continue;	//don't check for TURF, because we don't have the means to read it
				}
				if(resourceBundleFormat==ResourceBundleFormat.RDFXML && (rdfResourceIO==null || rdfPropertyNamespaceURI==null))	//if this is an RDF+XML file, only use it if we have I/O for the file
				{
					continue;	//don't check for RDF+XML, because we don't have the means to read it
				}
				final String resourcePath=getLocaleCandidatePath(paths[resourceBundleFormatIndex], locale, depth);	//get a candidate path for the resource bundle at this locale depth, using the path for this resource bundle type
				if(resourcePath!=null)	//if we can generate a candidate path for the locale at this depth
				{
	/*TODO fix
					final ResourceBundle cachedResourceBundle=resourceBundleMap.get(resourcePath);	//see if we have a cached XML resource bundle
					if(cachedResourceBundle!=null)	//if there is a cached XML resource bundle
					{
						return cachedResourceBundle;	//return the cached bundle
					}
					else	//if there is no cached bundle, try to load one
	*/
					{
						final URL resourceURL=loader.getResource(resourcePath);	//see if this resource bundle exists
						if(resourceURL!=null)	//if we found an existing resource bundle
						{
							try
							{
								final InputStream inputStream=resourceURL.openConnection().getInputStream();	//open an input stream to the resource URL
								try
								{
									switch(resourceBundleFormat)	//see which type of resource bundle we're loading
									{
										case TURF:
											{
												final URFResource urfResource=urfResourceIO.read(inputStream, resourceURL.toURI());	//try to read the resource
												final Map<String, Object> resourceMap=getResourceValue(urfResource);	//generate a map from the resources
												return new HashMapResourceBundle(resourceMap, parent);	//create a new hash map resource bundle with resources and the given parent and return it
											}
										case RDFXML:
											{
												final RDFResource rdfResource=rdfResourceIO.read(inputStream, resourceURL.toURI());	//try to read the resource
												return toResourceBundle(rdfResource, rdfPropertyNamespaceURI, parent);	//create and return a resource bundle from the RDF resource
											}
										case XML:
											{
												final Properties properties=new Properties();	//we'll load a properties file
												properties.loadFromXML(inputStream);	//load the properties file from the XML
												return new HashMapResourceBundle(properties, parent);	//create and return a resource bundle with the given parent
											}
										case PROPERTIES:
											{
												final Properties properties=new Properties();	//we'll load a properties file
												properties.load(inputStream);	//load the traditional properties file
												return new HashMapResourceBundle(properties, parent);	//create and return a resource bundle with the given parent
											}
										default:
											throw new AssertionError("Unrecognized resource bundle format: "+resourceBundleFormat);
									}
								}
								finally
								{
									inputStream.close();	//always close the input stream
								}
							}
							catch(final URISyntaxException uriSyntaxException)	//if the resource URL wasn't strictly in compliance with URI syntax
							{								
					      throw (MissingResourceException)new MissingResourceException(uriSyntaxException.getMessage(), baseName+Locales.LOCALE_SEPARATOR+locale, "").initCause(uriSyntaxException);
							}
							catch(final IOException ioException)	//if there is an error loading the resource
							{
					      throw (MissingResourceException)new MissingResourceException("I/O error in "+resourceURL+": "+ioException.getMessage(), baseName+Locales.LOCALE_SEPARATOR+locale, "").initCause(ioException);
							}
						}
					}
				}
			}
		}
     throw new MissingResourceException("Can't find resource bundle for base name "+baseName+", locale "+locale, baseName+Locales.LOCALE_SEPARATOR+locale, "");
	}

	/**Creates a resource bundle with contents reflecting the properties of a given RDF resource.
	@param rdfResource The RDF resource the properties of which should be turned into a map.
	@param rdfPropertyNamespaceURI The namespace of the properties to gather, using the property local name as the map entry key.
	@param parent The parent resource bundle, or <code>null</code> if there should be no parent for resolving resources.
	@return A resource bundle with contents reflecting the property/value pairs, resolving to the given parent.
	*/
	public static ResourceBundle toResourceBundle(final RDFResource rdfResource, final URI rdfPropertyNamespaceURI, final ResourceBundle parent)
	{
		return new HashMapResourceBundle(toMap(rdfResource, rdfPropertyNamespaceURI), parent);	//create a new hash map resource bundle with the given parent and return it		
	}
	
	/**Creates a map with contents reflecting the properties of a given RDF resource.
	Resource values are determined according to the algorithm used by {@link #getResourceValue(RDFObject)}.
	@param rdfResource The RDF resource the properties of which should be turned into a map.
	@param rdfPropertyNamespaceURI The namespace of the properties to gather, using the property local name as the map entry key.
	@return A map with contents reflecting the property/value pairs of the given resource.
	*/
	public static Map<String, Object> toMap(final RDFResource rdfResource, final URI rdfPropertyNamespaceURI)
	{
		final HashMap<String, Object> resourceHashMap=new HashMap<String, Object>(rdfResource.getPropertyCount());	//create a new hash map with enough initial room for all properties
		for(final RDFPropertyValuePair propertyValuePair:rdfResource.getProperties())	//for each resource property/value pair
		{
			final RDFResource property=propertyValuePair.getProperty();	//get the property
			final URI propertyURI=property.getURI();	//get the property URI
			if(propertyURI!=null && rdfPropertyNamespaceURI.equals(getNamespaceURI(propertyURI)))	//if this property is in the resourceKey namespace
			{
				final String resourceKey=getLocalName(propertyURI);	//use the local name as the resource key
				final Object resourceValue=getResourceValue(propertyValuePair.getValue());	//determine the resource value from the RDF property value
				if(resourceValue!=null)	//if we found a resource value
				{
					resourceHashMap.put(resourceKey, resourceValue);	//store the resource key/value pair in the map
				}
			}
		}
		return resourceHashMap;	//return the map		
	}
	
	/**Determines the resource value from an RDF property value.
	Resource values are determined as follows:
	<ol>
		<li>If the property value is a typed literal, the typed literal {@link Object} data is used.</li>
		<li>If the property value is any other literal, the lexical form {@link String} value is used.</li>
		<li>If the property value is a list, a {@link List} is returned filled with values obtained by using this same algorithm; <code>null</code> values are accepted.</li>
		<li>If the value is any other resource with a reference URI, that {@link URI} is used.</li>
		<li>If the value is any other resource with no reference URI but that has a single <code>rdf:value</code>, the value of that value resource is obtained by using this same algorithm.</li>
	</ol>
	@param rdfPropertyValue The RDF property value object to be converted to a resource value.
	@return A {@link String}, {@link URI}, or a {@link List} representing the resource value for the RDF property value, or <code>null</code> if no value could be determined.
	*/
	public static Object getResourceValue(final RDFObject rdfPropertyValue)
	{
		if(rdfPropertyValue instanceof RDFLiteral)	//if the property value is a literal
		{
			return rdfPropertyValue instanceof RDFTypedLiteral ? ((RDFTypedLiteral<?>)rdfPropertyValue).getValue() : ((RDFLiteral)rdfPropertyValue).getLexicalForm();	//get the typed literal object if this is a typed literal
		}
		else if(rdfPropertyValue instanceof RDFListResource)	//if the property value is a list
		{
			final RDFListResource<?> listResource=(RDFListResource<?>)rdfPropertyValue;	//get the property value as a list
			final List<Object> list=new ArrayList<Object>(listResource.size());	//create a new list
			for(final RDFObject elementResource:listResource)	//for each element in the list resource
			{
				list.add(getResourceValue(elementResource));	//convert the element resource to a resource value and add it to the list
			}
			return list;	//return the list
		}
		else if(rdfPropertyValue instanceof RDFResource)	//if the property value is any other resource
		{
			final RDFResource resource=(RDFResource)rdfPropertyValue;	//get the property value as a resource
			final URI referenceURI=resource.getURI();	//get the reference URI
			if(referenceURI!=null)	//if there is a reference URI
			{
				return referenceURI;	//return the reference URI
			}
			else	//if there is no reference URI, see if there is an rdf:value specified
			{
				final RDFObject rdfValue=getValue(resource);	//get the rdf:value specified, if any
				if(rdfValue!=null)	//if there is an rdf:value specified
				{
					return getResourceValue(rdfValue);	//convert the rdf:value to a resource value and return it
				}
			}
		}
		return null;	//indicate that no value could be determined
	}

	/**Creates a resource bundle with contents reflecting the properties of a given URF resource.
	@param urfResource The URF resource the properties of which should be turned into a map.
	@param urfPropertyNamespaceURI The namespace of the properties to gather, using the property local name as the map entry key.
	@param parent The parent resource bundle, or <code>null</code> if there should be no parent for resolving resources.
	@return A resource bundle with contents reflecting the property/value pairs, resolving to the given parent.
	*/
	public static ResourceBundle toResourceBundle(final URFResource urfResource, final URI urfPropertyNamespaceURI, final ResourceBundle parent)
	{
		return new HashMapResourceBundle(toMap(urfResource, urfPropertyNamespaceURI), parent);	//create a new hash map resource bundle with the given parent and return it		
	}
	
	/**Creates a map with contents reflecting the properties of a given URF resource.
	Resource values are determined according to the algorithm used by {@link #getResourceValue(URFResource)}.
	@param urfResource The URF resource the properties of which should be turned into a map.
	@param urfPropertyNamespaceURI The namespace of the properties to gather, using the property local name as the map entry key.
	@return A map with contents reflecting the property/value pairs of the given resource.
	*/
	public static Map<String, Object> toMap(final URFResource urfResource, final URI urfPropertyNamespaceURI)
	{
		urfResource.readLock().lock();	//get a read lock on the resource
		try
		{
			final HashMap<String, Object> resourceHashMap=new HashMap<String, Object>((int)urfResource.getPropertyValueCount());	//create a new hash map with enough initial room for all properties
			for(final URI propertyURI:urfResource.getPropertyURIs())	//look at each resource property URI; we'll only use the first value for each unique property URI
			{
				if(propertyURI!=null && urfPropertyNamespaceURI.equals(URF.getNamespaceURI(propertyURI)))	//if this property is in the requested namespace
				{
					final String resourceKey=URF.getLocalName(propertyURI);	//use the local name as the resource key
					final Object resourceValue=getResourceValue(urfResource.getPropertyValue(propertyURI));	//look up the property value and determine the resource value from the URF property value
					resourceHashMap.put(resourceKey, resourceValue);	//store the resource key/value pair in the map
				}
			}
			return resourceHashMap;	//return the map		
		}
		finally
		{
			urfResource.readLock().unlock();	//always release the read lock
		}
	}
	
	/**Determines the resource value from an URF property value.
	Resource values are determined as follows:
	<ol>
		<li>If the property value is an {@link URFListResource}, a {@link List} is returned filled with values obtained by using this same algorithm.</li>
		<li>If the property value is an {@link URFSetResource}, a {@link Set} is returned filled with values obtained by using this same algorithm.</li>
		<li>If the property value is an {@link URFMapResource}, a {@link Map} is returned filled with values obtained by using this same algorithm.</li>
		<li>For all other resources, the result of {@link URF#asObject(Resource)}, if any, is returned.</li>
	</ol>
	@param urfPropertyValue The URF property value to be converted to a resource value.
	@return An object representing the resource value for the URF property value.
	*/
	@SuppressWarnings("unchecked")
	public static <T> T getResourceValue(final URFResource urfPropertyValue)
	{
		if(urfPropertyValue instanceof URFListResource)	//if the property value is an URF list
		{
			final URFListResource<?> listResource=(URFListResource<?>)urfPropertyValue;	//get the property value as an URF list
			final List<Object> list=new ArrayList<Object>();	//create a new list
			for(final URFResource elementResource:listResource)	//for each element in the list resource
			{
				list.add(getResourceValue(elementResource));	//convert the element resource to a resource value and add it to the list
			}
			return (T)list;	//return the list
		}
		else if(urfPropertyValue instanceof URFSetResource)	//if the property value is an URF set
		{
			final URFSetResource<?> setResource=(URFSetResource<?>)urfPropertyValue;	//get the property value as an URF set
			final Set<Object> set=new HashSet<Object>();	//create a new set
			for(final URFResource elementResource:setResource)	//for each element in the set resource
			{
				set.add(getResourceValue(elementResource));	//convert the element resource to a resource value and add it to the set
			}
			return (T)set;	//return the set
		}
		else if(urfPropertyValue instanceof URFMapResource)	//if the property value is an URF map
		{
			final URFMapResource<?, ?> mapResource=(URFMapResource<?, ?>)urfPropertyValue;	//get the property value as an URF map
			final Map<Object, Object> map=new HashMap<Object, Object>();	//create a new map
			for(final Map.Entry<? extends URFResource, ? extends URFResource> mapEntry:mapResource.entrySet())	//for each entry in the map resource
			{
				final Object keyObject=getResourceValue(mapEntry.getKey());	//convert the key resource to a resource value
				final Object valueObject=getResourceValue(mapEntry.getValue());	//convert the value resource to a resource value
				map.put(keyObject, valueObject);	//store the converted objects in the map 
			}
			return (T)map;	//return the map
		}
		else	//for all other values
		{
			final Object object=URF.asObject(urfPropertyValue);	//convert the property value to an object, if possible
			return object!=null ? (T)object : (T)urfPropertyValue;	//if we found an object, return it; otherwise, return the URF property value resource as it is
		}
	}

}
