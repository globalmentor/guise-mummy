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

package com.guiseframework;

import java.net.URI;

import org.urframework.*;

import static com.globalmentor.java.Classes.*;

import com.globalmentor.java.Classes;
import com.globalmentor.java.Enums;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.net.URIs.*;
import static com.globalmentor.text.Text.*;
import static org.urframework.URF.*;

/**Guise resources description in URF.
@author Garret Wilson
*/
public class Resources extends URFMapResource<URFResource, URFResource>
{

	/**The recommended prefix to the resources key ontology namespace.*/
	public final static String RESOURCES_NAMESPACE_PREFIX="resources";
	/**The URI to the resource key ontology namespace.*/
	public final static URI RESOURCES_NAMESPACE_URI=URI.create("http://guiseframework.com/namespaces/resources/");

	/**The default base name of the Guise resource bundle.*/
	public final static String DEFAULT_RESOURCE_BUNDLE_BASE_NAME=Resources.class.getPackage().getName()+"/resources";

	/**The prefix character used to introduce string value references.*/
	public final static char STRING_VALUE_REFERENCE_PREFIX_CHAR='=';

		//common property strings
	/**The aspect used to identify labels as part of resource property keys.*/
	public final static String LABEL_PROPERTY_KEY_ASPECT="label";
	/**The aspect used to identify glyphs as part of resource property keys.*/
	public final static String GLYPH_PROPERTY_KEY_ASPECT="glyph";
	/**The aspect used to identify info as part of resource property keys.*/
	public final static String INFO_PROPERTY_KEY_ASPECT="info";

		//standard labels
	public final static String APPLICATION_NAME=createStringResourceReference("application.name");
	public final static String APPLICATION_NAME_SHORT=createStringResourceReference("application.name.short");
	public final static String APPLICATION_LABEL=createStringResourceReference("application.label");
	public final static String APPLICATION_DESCRIPTION=createStringResourceReference("application.description");
	public final static String APPLICATION_COPYRIGHT=createStringResourceReference("application.copyright");
	public final static String APPLICATION_VERSION=createStringResourceReference("application.version");

		//converter resources
	/**The resource reference for a converter message indicating that a value is invalid.*/
	public final static String CONVERTER_INVALID_VALUE_MESSAGE_RESOURCE_REFERENCE=createStringResourceReference("converter.invalid.value.message");
		//validator resources
	/**The resource reference for a validator message indicating that a value is required.*/
	public final static String VALIDATOR_VALUE_REQUIRED_MESSAGE_RESOURCE_REFERENCE=createStringResourceReference("validator.value.required.message");
	/**The resource reference for a validator message indicating that a value is invalid.*/
	public final static String VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE_REFERENCE=createStringResourceReference("validator.invalid.value.message");
	/**The resource bundle key for a general failed validation message.*/
	public final static String VALIDATION_FALSE_MESSAGE_RESOURCE_REFERENCE=createStringResourceReference("validation.false.message");

	/**Default constructor.*/
	public Resources()
	{
		this(null);	//construct the class with no reference URI
	}

	/**Reference URI constructor.
	@param referenceURI The reference URI for the new resource.
	*/
	public Resources(final URI referenceURI)
	{
		super(referenceURI, createResourceURI(RESOURCES_NAMESPACE_URI, getLocalName(Resources.class)));  //construct the parent class
	}

	/**Creates a string containing a reference to the given string resource key.
	The string resource reference is a control string according to ECMA-48, "Control Functions for Coded Character Sets", Section 5.6, "Control strings".
	A control string begins with the Start of String control character (U+0098) and ends with a String Terminator control character (U+009C).
	ECMA-48 publication is also approved as ISO/IEC 6429.
	@param resourceKey The resource key to a string in the resources which could be retrieved using {@link GuiseSession#getStringResource(String)}.
	@return A string containing a reference to the given resource key, an ECMA-48 control string with the given resource key as its content, which can be resolved using {@link GuiseSession#dereferenceString(String)}.
	@exception NullPointerException if the given resource key is <code>null</code>.
	@see <a href="http://www.ecma-international.org/publications/standards/Ecma-048.htm">ECMA-48: Control Functions for Coded Character Sets</a>
	*/
	public final static String createStringResourceReference(final String resourceKey)
	{
		return createControlString(resourceKey);	//return a control string for the given resource key
	}

	/**Creates a string containing a value that can be used as an argument in formatting.
	@param value The value to be used as an argument.
	@return A string containing a reference to the given value, an ECMA-48 control string beginning with {@value #STRING_VALUE_REFERENCE_PREFIX_CHAR} with the given Value as its content.
	@exception NullPointerException if the given value is <code>null</code>.
	@see <a href="http://www.ecma-international.org/publications/standards/Ecma-048.htm">ECMA-48: Control Functions for Coded Character Sets</a>
	*/
	public final static String createStringValueReference(final String value)
	{
		return createControlString(new StringBuilder().append(STRING_VALUE_REFERENCE_PREFIX_CHAR).append(value).toString());	//return a control string for the given value prefixed by the value reference character
	}

	/**Creates a URI containing a reference to the given string resource key.
	The URI resource reference is URI with the scheme <code>resource</code> and the scheme-specific part indicating the resource key.
	@param resourceKey The resource key to a string in the resources which could be retrieved using {@link GuiseSession#getURIResource(String)}.
	@return A URI containing a reference to the given resource key, which can be resolved using {@link GuiseSession#resolveURI(String)}.
	@exception NullPointerException if the given resource key is <code>null</code>.
	*/
	public final static URI createURIResourceReference(final String resourceKey)
	{
		return createURI(RESOURCE_SCHEME, checkInstance(resourceKey, "Resource key cannot be null."));
	}

	/**Returns a form of the enum name appropriate for a resource key.
	The name is converted to lowercaes and all underscore characters ('_') are replaced by periods ('.').
	For example, <code>FILE_NOT_FOUND</code> would produce <code>file.not.found</code>.
	@param e The enum instance to convert to a resource key.
	@return A string representing the enum instance in a style appropriate for use as a resource key.
	@see Enum#name()
	*/
	public static String getResourceKeyName(final Enum<?> e)
	{
		return e.name().toLowerCase().replace('_', '.');	//convert the name to lowercase and replace underscores with periods
	}

	/**Returns a resource reference for the label aspect of a resource key based upon the given enum class.
	The resource reference will a resource key in the form <code><var>com.example.EnumClass</var>.label</code>.
	@param enumClass The enum class for which to return a resource reference.
	@return A string resource reference to the label aspect of a resource key based upon the given enum class.
	@exception NullPointerException if the given enum is <code>null</code>.
	@see Classes#getPropertyName(Class, String)
	@see #LABEL_PROPERTY_KEY_ASPECT
	@see #createStringResourceReference(String)
	*/
	public static <E extends Enum<E>> String getLabelResourceReference(final Class<E> enumClass)
	{
		return createStringResourceReference(Classes.getPropertyName(enumClass, LABEL_PROPERTY_KEY_ASPECT));
	}

	/**Returns a resource reference for the label aspect of a resource key based upon the given enum.
	The resource reference will a resource key in the form <code><var>com.example.EnumClass</var>.<var>NAME</var>.label</code>.
	@param e The enum instance for which to return a resource reference.
	@return A string resource reference to the label aspect of a resource key based upon the given enum.
	@exception NullPointerException if the given enum is <code>null</code>.
	@see Enums#getPropertyName(Enum, String)
	@see #LABEL_PROPERTY_KEY_ASPECT
	@see #createStringResourceReference(String)
	*/
	public static <E extends Enum<E>> String getLabelResourceReference(final E e)
	{
		return createStringResourceReference(Enums.getPropertyName(e, LABEL_PROPERTY_KEY_ASPECT));
	}

	/**Returns a resource reference for the glyph aspect of a resource key based upon the given enum class.
	The resource reference will a resource key in the form <code><var>com.example.EnumClass</var>.glyph</code>.
	@param enumClass The enum class for which to return a resource reference.
	@return A URI resource reference to the glyph aspect of a resource key based upon the given enum class.
	@exception NullPointerException if the given enum is <code>null</code>.
	@see Classes#getPropertyName(Class, String)
	@see #GLYPH_PROPERTY_KEY_ASPECT
	@see #createURIResourceReference(String)
	*/
	public static <E extends Enum<E>> URI getGlyphResourceReference(final Class<E> enumClass)
	{
		return createURIResourceReference(Classes.getPropertyName(enumClass, GLYPH_PROPERTY_KEY_ASPECT));
	}

	/**Returns a resource reference for the glyph aspect of a resource key based upon the given enum.
	The resource reference will a resource key in the form <code><var>com.example.EnumClass</var>.<var>NAME</var>.glyph</code>.
	@param e The enum instance for which to return a resource reference.
	@return A URI resource reference to the glyph aspect of a resource key based upon the given enum.
	@exception NullPointerException if the given enum is <code>null</code>.
	@see Enums#getPropertyName(Enum, String)
	@see #GLYPH_PROPERTY_KEY_ASPECT
	@see #createURIResourceReference(String)
	*/
	public static <E extends Enum<E>> URI getGlyphResourceReference(final E e)
	{
		return createURIResourceReference(Enums.getPropertyName(e, GLYPH_PROPERTY_KEY_ASPECT));
	}

	/**Returns a resource reference for the info aspect of a resource key based upon the given enum class.
	The resource reference will a resource key in the form <code><var>com.example.EnumClass</var>.info</code>.
	@param enumClass The enum class for which to return a resource reference.
	@return A string resource reference to the info aspect of a resource key based upon the given enum class.
	@exception NullPointerException if the given enum is <code>null</code>.
	@see Classes#getPropertyName(Class, String)
	@see #INFO_PROPERTY_KEY_ASPECT
	@see #createStringResourceReference(String)
	*/
	public static <E extends Enum<E>> String getInfoResourceReference(final Class<E> enumClass)
	{
		return createStringResourceReference(Classes.getPropertyName(enumClass, INFO_PROPERTY_KEY_ASPECT));
	}

	/**Returns a resource reference for the info aspect of a resource key based upon the given enum.
	The resource reference will a resource key in the form <code><var>com.example.EnumClass</var>.<var>NAME</var>.info</code>.
	@param e The enum instance for which to return a resource reference.
	@return A string resource reference to the info aspect of a resource key based upon the given enum.
	@exception NullPointerException if the given enum is <code>null</code>.
	@see Enums#getPropertyName(Enum, String)
	@see #INFO_PROPERTY_KEY_ASPECT
	@see #createStringResourceReference(String)
	*/
	public static <E extends Enum<E>> String getInfoResourceReference(final E e)
	{
		return createStringResourceReference(Enums.getPropertyName(e, INFO_PROPERTY_KEY_ASPECT));
	}
}
