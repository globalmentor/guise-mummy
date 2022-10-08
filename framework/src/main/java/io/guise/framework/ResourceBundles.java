/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework;

import java.io.*;
import java.net.*;
import java.util.*;

import static java.util.Objects.*;

import com.globalmentor.io.IO;
import com.globalmentor.model.Locales;
import com.globalmentor.util.HashMapResourceBundle;
import com.globalmentor.util.PropertiesFiles;

import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.java.Java.*;
import static com.globalmentor.model.Locales.*;
import static com.globalmentor.net.URIs.*;

/**
 * Utilities for working with resource bundles.
 * @author Garret Wilson
 * @deprecated Switch to using Rincl.
 */
@Deprecated
public class ResourceBundles //TODO moved out of globalmentor-core to allow org.urframework project extraction; refactor to allow flexibility and storage format pluggability
{

	/** The map of resource bundles softly keyed to resource bundle base paths. This serves as a soft cache to resource bundles. */
	//TODO fix caching; the problem is currently how to verify the same loader and resource path while keeping a weak reference to the loader private static final Map<String, ResourceBundle> resourceBundleMap=synchronizedMap(new SoftValueHashMap<String, ResourceBundle>());

	/**
	 * The format in which a resource bundle is serialized, in order of preference.
	 * @author Garret Wilson
	 */
	private enum ResourceBundleFormat { //TODO make these pluggable via Rincl
		/** The resource bundle is serialized in a TURF file. */
		TURF("turf"), //TODO bring back reference to definition constant

		/** The resource bundle is serialized in an XML file. */
		XML(com.globalmentor.xml.spec.XML.FILENAME_EXTENSION),

		/** The resource bundle is serialized in a properties file. */
		PROPERTIES(PropertiesFiles.FILENAME_EXTENSION);

		/** The extension for this resource bundle file type. */
		private final String extension;

		/** @return The extension for this resource bundle file type. */
		public String getExtension() {
			return extension;
		}

		/**
		 * Extension constructor.
		 * @param extension The extension for this resource bundle file type.
		 * @throws NullPointerException if the given extension is <code>null</code>.
		 */
		private ResourceBundleFormat(final String extension) {
			this.extension = requireNonNull(extension, "Extension cannot be null.");
		}
	}

	/**
	 * Loads a resource bundle for a given base name and locale. This implementation recognizes properties stored in <code>.turf</code>, <code>.xml</code>, and
	 * <code>.properties</code> files, searching in that order. TURF property files are only used if a given TURF resource I/O instance is provided for reading
	 * from the file.
	 * @param baseName The base name of the resource bundle, which is a fully qualified class name, such as "myProperties".
	 * @param locale The locale for which a resource bundle is desired.
	 * @param loader The class loader from which to load the resource bundle.
	 * @param parent The parent resource bundle, or <code>null</code> if there should be no parent for resolving resources.
	 * @param turfResourceIO The I/O support for loading resources from a TURF representation, or <code>null</code> if TURF resource bundles are not supported.
	 * @return A resource bundle for the given base name and locale.
	 * @throws MissingResourceException if no resource bundle for the specified base name can be found, or if there is an error loading the resource bundle.
	 */
	public static ResourceBundle getResourceBundle(final String baseName, final Locale locale, final ClassLoader loader, final ResourceBundle parent,
			final IO<Map<Object, Object>> turfResourceIO) throws MissingResourceException {
		final String basePath = baseName.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR); //create a base path from base name
		final ResourceBundleFormat[] resourceBundleFormats = ResourceBundleFormat.values(); //get the available resource bundle formats
		final int resourceBundleFormatCount = resourceBundleFormats.length; //see how many resource bundle formats there are
		final String[] paths = new String[resourceBundleFormatCount]; //create an array of paths to try
		for(int resourceBundleFormatIndex = 0; resourceBundleFormatIndex < resourceBundleFormatCount; ++resourceBundleFormatIndex) { //for each resource bundle format
			paths[resourceBundleFormatIndex] = addExtension(basePath, resourceBundleFormats[resourceBundleFormatIndex].getExtension()); //create a path to test for this resource bundle format
		}
		for(int depth = 3; depth >= 0; --depth) { //try different locales, starting with the most specific, until we find an input stream
			for(int resourceBundleFormatIndex = 0; resourceBundleFormatIndex < resourceBundleFormatCount; ++resourceBundleFormatIndex) { //for each resource bundle format
				final ResourceBundleFormat resourceBundleFormat = resourceBundleFormats[resourceBundleFormatIndex]; //get this resource bundle format
				if(resourceBundleFormat == ResourceBundleFormat.TURF && turfResourceIO == null) { //if this is a TURF file, only use it if we have I/O for the file
					continue; //don't check for TURF, because we don't have the means to read it
				}
				final String resourcePath = getLocaleCandidatePath(paths[resourceBundleFormatIndex], locale, depth); //get a candidate path for the resource bundle at this locale depth, using the path for this resource bundle type
				if(resourcePath != null) { //if we can generate a candidate path for the locale at this depth
					/*TODO fix
									final ResourceBundle cachedResourceBundle=resourceBundleMap.get(resourcePath);	//see if we have a cached XML resource bundle
									if(cachedResourceBundle!=null) {	//if there is a cached XML resource bundle
										return cachedResourceBundle;	//return the cached bundle
									}
									else	//if there is no cached bundle, try to load one
					*/
					{
						final URL resourceURL = loader.getResource(resourcePath); //see if this resource bundle exists
						if(resourceURL != null) { //if we found an existing resource bundle
							try {
								try (final InputStream inputStream = resourceURL.openConnection().getInputStream()) { //open an input stream to the resource URL
									switch(resourceBundleFormat) { //see which type of resource bundle we're loading
										case TURF: {
											final Map<Object, Object> resourceMap = turfResourceIO.read(inputStream, resourceURL.toURI()); //try to read the resource
											return new HashMapResourceBundle(resourceMap, parent); //create a new hash map resource bundle with resources and the given parent and return it
										}
										case XML: {
											final Properties properties = new Properties(); //we'll load a properties file
											properties.loadFromXML(inputStream); //load the properties file from the XML
											return new HashMapResourceBundle(properties, parent); //create and return a resource bundle with the given parent
										}
										case PROPERTIES: {
											final Properties properties = new Properties(); //we'll load a properties file
											properties.load(inputStream); //load the traditional properties file
											return new HashMapResourceBundle(properties, parent); //create and return a resource bundle with the given parent
										}
										default:
											throw new AssertionError("Unrecognized resource bundle format: " + resourceBundleFormat);
									}
								}
							} catch(final URISyntaxException uriSyntaxException) { //if the resource URL wasn't strictly in compliance with URI syntax								
								throw (MissingResourceException)new MissingResourceException(uriSyntaxException.getMessage(), baseName + Locales.LOCALE_SEPARATOR + locale, "")
										.initCause(uriSyntaxException);
							} catch(final IOException ioException) { //if there is an error loading the resource
								throw (MissingResourceException)new MissingResourceException("I/O error in " + resourceURL + ": " + ioException.getMessage(),
										baseName + Locales.LOCALE_SEPARATOR + locale, "").initCause(ioException);
							}
						}
					}
				}
			}
		}
		throw new MissingResourceException("Can't find resource bundle for base name " + baseName + ", locale " + locale,
				baseName + Locales.LOCALE_SEPARATOR + locale, "");
	}

}
