package com.guiseframework.validator;

import java.util.*;
import static java.util.Collections.*;

import javax.mail.internet.ContentType;

import static com.garretwilson.io.Files.*;
import static com.garretwilson.util.CollectionUtilities.*;
import static com.globalmentor.java.Classes.*;

import com.garretwilson.util.Debug;
import com.guiseframework.GuiseSession;
import com.guiseframework.model.ResourceImport;

/**A resource import validator that can validate resource imports against accepted content types, file extensions, and/or maximum size.
If accepted content types are specified, the set needs to contain the <code>null</code> value if resource imports with no content type are to be allowed.
Not providing a set of accepted content types allows any content types.
If accepted filename extensions are specified, the set needs to contain the <code>null</code> value if resource imports with no extension are to be allowed.
Not providing a set of accepted filename extensions allows any extensions.
If a maximum content length of -1 is specified, any content length, including an undefined content length, will be accepted.
Specifying a content length greater than zero will never allow a resource with an undefined content length.
@author Garret Wilson
*/
public class ResourceImportValidator extends AbstractValidator<ResourceImport>
{

	/**The read-only set of accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.*/
	private final Set<ContentType> acceptedContentTypes;	//TODO fix content type set, as ContentType does not correctly implement equals()

		/**@return The read-only set of accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.*/
		public Set<ContentType> getAcceptedContentTypes() {return acceptedContentTypes;}

	/**The read-only set of accepted filename extensions, or <code>null</code> if all filename extensions are accepted.*/
	private final Set<String> acceptedExtensions;

		/**@return The read-only set of accepted filename extensions, or <code>null</code> if all filename extensions are accepted.*/
		public Set<String> getAcceptedExtensions() {return acceptedExtensions;}

	/**The maximum content length to accept, or -1 if there is no limit to the content length.*/
	private final long maxContentLength;

		/**@return The maximum content length to accept, or -1 if there is no limit to the content length.*/
		public long getMaxContentLength() {return maxContentLength;}

	/**Default constructor.*/
	public ResourceImportValidator(final GuiseSession session)
	{
		this(-1);	//accept any content type, extension, and content length, and don't require a value
	}

	/**Maximum content length constructor.
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	*/
	public ResourceImportValidator(final long maxContentLength)
	{
		this(maxContentLength, false);	//accept any content type and extension, and don't require a value
	}

	/**Maximum content length and value required constructor.
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public ResourceImportValidator(final long maxContentLength, final boolean valueRequired)
	{
		this(null, null, maxContentLength, valueRequired);	//accept any content type and extension
	}

	/**Accepted content types constructor.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	*/
	public ResourceImportValidator(final Set<ContentType> acceptedContentTypes)
	{
		this(acceptedContentTypes, false);	//don't require a value
	}

	/**Accepted content type constructor.
	@param acceptedContentType The single accepted content types.	
	*/
	public ResourceImportValidator(final ContentType acceptedContentType)
	{
		this(acceptedContentType, false);	//don't require a value
	}

	/**Accepted content type, and value required constructor.
	@param acceptedContentType The single accepted content types.	
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public ResourceImportValidator(final ContentType acceptedContentType, final boolean valueRequired)
	{
		this(createHashSet(acceptedContentType), valueRequired);	//pass a hash set with the single accepted content type
	}

	/**Accepted content types and value required constructor.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public ResourceImportValidator(final Set<ContentType> acceptedContentTypes, final boolean valueRequired)
	{
		this(acceptedContentTypes, -1, valueRequired);	//accept any content length
	}

	/**Accepted content types and maximum content length constructor.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	*/
	public ResourceImportValidator(final Set<ContentType> acceptedContentTypes, final long maxContentLength)
	{
		this(acceptedContentTypes, null, maxContentLength, false);	//accept any content types and don't require a value		
	}

	/**Accepted content type and maximum content length constructor.
	@param acceptedContentType The single accepted content type.
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	*/
	public ResourceImportValidator(final ContentType acceptedContentType, final long maxContentLength)
	{
		this(acceptedContentType, maxContentLength, false);	//don't require a value
	}

	/**Accepted content type, maximum content length, and value required constructor.
	@param acceptedContentType The single accepted content type.
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public ResourceImportValidator(final ContentType acceptedContentType, final long maxContentLength, final boolean valueRequired)
	{		
		this(createHashSet(acceptedContentType), maxContentLength, valueRequired);	//pass a hash set with the single accepted content type
	}

	/**Accepted content types, maximum content length, and value required constructor.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public ResourceImportValidator(final Set<ContentType> acceptedContentTypes, final long maxContentLength, final boolean valueRequired)
	{
		this(acceptedContentTypes, null, maxContentLength, valueRequired);	//accept any content types
	}

	/**Accepted content types, and accepted extensions constructor.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@param acceptedExtensions The accepted filename extensions, or <code>null</code> if all filename extensions are accepted.
	*/
	public ResourceImportValidator(final Set<ContentType> acceptedContentTypes, final Set<String> acceptedExtensions)
	{
		this(acceptedContentTypes, acceptedExtensions, false);	//accept any content length and don't require a value
	}

	/**Accepted content types, accepted extensions, and value required constructor.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@param acceptedExtensions The accepted filename extensions, or <code>null</code> if all filename extensions are accepted.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public ResourceImportValidator(final Set<ContentType> acceptedContentTypes, final Set<String> acceptedExtensions, final boolean valueRequired)
	{
		this(acceptedContentTypes, acceptedExtensions, -1, valueRequired);	//accept any content length
	}

	/**Accepted content types, accepted extensions, and maximum content length constructor.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@param acceptedExtensions The accepted filename extensions, or <code>null</code> if all filename extensions are accepted.
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	*/
	public ResourceImportValidator(final Set<ContentType> acceptedContentTypes, final Set<String> acceptedExtensions, final long maxContentLength)
	{
		this(acceptedContentTypes, acceptedExtensions, maxContentLength, false);	//don't require a value
	}

	/**Accepted content types, accepted extensions, maximum content length, and value required constructor.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@param acceptedExtensions The accepted filename extensions, or <code>null</code> if all filename extensions are accepted.
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	*/
	public ResourceImportValidator(final Set<ContentType> acceptedContentTypes, final Set<String> acceptedExtensions, final long maxContentLength, final boolean valueRequired)
	{
		super(valueRequired);	//construct the parent class
		this.acceptedContentTypes=acceptedContentTypes!=null ? unmodifiableSet(new HashSet<ContentType>(acceptedContentTypes)) : null;	//create a read-only copy of the content types passed
		this.acceptedExtensions=acceptedExtensions!=null ? unmodifiableSet(new HashSet<String>(acceptedExtensions)) : null;	//create a read-only copy of the extensions passed
		this.maxContentLength=maxContentLength;
	}

	/**Determines whether a given resource import meets the provided criteria.
	This version delgates to the super class version to determine whether <code>null</code> values are allowed.
	@param resourceImport The resource import to validate, which may be <code>null</code>.
	@exception ValidationException if the provided value is not valid.
	@see #getAcceptedContentTypes()
	@see #getAcceptedExtensions()
	@see #getMaxContentLength()
	*/
	public void validate(final ResourceImport resourceImport) throws ValidationException
	{
Debug.trace("ready to validate resource import", resourceImport);
		super.validate(resourceImport);	//do the default validation
		final Set<ContentType> acceptedContentTypes=getAcceptedContentTypes();	//get the accepted content types
		if(acceptedContentTypes!=null)	//if we need to check the content types
		{
			final ContentType resourceContentType=resourceImport.getContentType();	//get the content type of the resource import
			boolean isContentTypeMatch=false;	//see if we match a content type
			for(final ContentType contentType:acceptedContentTypes)	//look at all the accepted content types
			{
				if((contentType==null && resourceContentType==null)	//if this is a null content type and the resource import content type is also null
						|| (contentType!=null && resourceContentType!=null && contentType.match(resourceContentType)))	//if this content type is not null and the resource content type matches it
				{
					isContentTypeMatch=true;	//show that the resource content type matches one of the accepted content types
					break;	//stop looking for a match
				}
			}
			if(!isContentTypeMatch)	//if there was no content type match
			{
				throwInvalidValueValidationException(resourceImport);	//the resource import didn't pass the content type test TODO add a custom message
			}
		}
		final Set<String> acceptedExtensions=getAcceptedExtensions();	//get the accepted extensions
		if(acceptedExtensions!=null)	//if we need to check the extensions
		{
			final String resourceExtension=getExtension(resourceImport.getName());	//get the extension of the resource import filename
			boolean isExtensionMatch=false;	//see if we match an extension
			for(final String extension:acceptedExtensions)	//look at all the accepted extensions
			{
				if((extension==null && resourceExtension==null)	//if this is a null extension and the resource import extension is also null
						|| (extension!=null && resourceExtension!=null && extension.equals(resourceExtension)))	//if this extension is not null and the resource extension matches it
				{
					isExtensionMatch=true;	//show that the resource extension matches one of the accepted extensions
					break;	//stop looking for a match
				}
			}
			if(!isExtensionMatch)	//if there was no extension match
			{
				throwInvalidValueValidationException(resourceImport);	//the resource import didn't pass the extension test TODO add a custom message
			}
		}
		final long maxContentLength=getMaxContentLength();	//get the maximum content length
		if(maxContentLength>=0)	//if there is a content length restriction
		{
			final long resourceContentLength=resourceImport.getContentLength();	//get the content length of the resource
			if(resourceContentLength<0 && resourceContentLength>maxContentLength)	//if the resource content length is not within the allowed range
			{
				throwInvalidValueValidationException(resourceImport);	//the resource import didn't have an allowed size TODO add a custom message
			}
		}
	}

	/**Retrieves a string representation of the given value appropriate for error messages.
	This version returns the resource import name, if present; otherwise the simple name of the class instance.
	@param resourceImport The value for which a string representation should be returned.
	@return A string representation of the given value.
	*/
	protected String toString(final ResourceImport resourceImport)
	{
		final String name=resourceImport.getName();	//get the name of the resource to be imported
		return name!=null ? name : getSimpleName(resourceImport.getClass());	//return the name of the resource or the simple name of the class if the resource has no name
	}

}
