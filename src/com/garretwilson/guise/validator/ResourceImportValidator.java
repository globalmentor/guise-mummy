package com.garretwilson.guise.validator;

import java.util.*;
import static java.util.Collections.*;

import javax.mail.internet.ContentType;

import com.garretwilson.guise.model.ResourceImport;
import com.garretwilson.guise.session.GuiseSession;
import static com.garretwilson.io.FileUtilities.*;
import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.util.CollectionUtilities.*;
import com.garretwilson.util.Debug;

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

	/**Session constructor.
	@param session The Guise session that owns this validator.
	*/
	public ResourceImportValidator(final GuiseSession<?> session)
	{
		this(session, -1);	//accept any content type, extension, and content length, and don't require a value
	}

	/**Session and maximum content length constructor.
	@param session The Guise session that owns this validator.
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ResourceImportValidator(final GuiseSession<?> session, final long maxContentLength)
	{
		this(session, maxContentLength, false);	//accept any content type and extension, and don't require a value
	}

	/**Session, maximum content length, and value required constructor.
	@param session The Guise session that owns this validator.
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ResourceImportValidator(final GuiseSession<?> session, final long maxContentLength, final boolean valueRequired)
	{
		this(session, null, null, maxContentLength, valueRequired);	//accept any content type and extension
	}

	/**Session, and accepted content types constructor.
	@param session The Guise session that owns this validator.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ResourceImportValidator(final GuiseSession<?> session, final Set<ContentType> acceptedContentTypes)
	{
		this(session, acceptedContentTypes, false);	//don't require a value
	}

	/**Session, and accepted content type constructor.
	@param session The Guise session that owns this validator.
	@param acceptedContentType The single accepted content types.	
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ResourceImportValidator(final GuiseSession<?> session, final ContentType acceptedContentType)
	{
		this(session, acceptedContentType, false);	//don't require a value
	}

	/**Session, accepted content type, and value required constructor.
	@param session The Guise session that owns this validator.
	@param acceptedContentType The single accepted content types.	
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ResourceImportValidator(final GuiseSession<?> session, final ContentType acceptedContentType, final boolean valueRequired)
	{
		this(session, createHashSet(acceptedContentType), valueRequired);	//pass a hash set with the single accepted content type
	}

	/**Session, accepted content types, and value required constructor.
	@param session The Guise session that owns this validator.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ResourceImportValidator(final GuiseSession<?> session, final Set<ContentType> acceptedContentTypes, final boolean valueRequired)
	{
		this(session, acceptedContentTypes, -1, valueRequired);	//accept any content length
	}

	/**Session, accepted content types, and maximum content length constructor.
	@param session The Guise session that owns this validator.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ResourceImportValidator(final GuiseSession<?> session, final Set<ContentType> acceptedContentTypes, final long maxContentLength)
	{
		this(session, acceptedContentTypes, null, maxContentLength, false);	//accept any content types and don't require a value		
	}

	/**Session, accepted content type, and maximum content length constructor.
	@param session The Guise session that owns this validator.
	@param acceptedContentType The single accepted content types.	
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	@exception NullPointerException if the given session and/or accepted content type is <code>null</code>.
	*/
	public ResourceImportValidator(final GuiseSession<?> session, final ContentType acceptedContentType, final long maxContentLength)
	{
		this(session, acceptedContentType, maxContentLength, false);	//don't require a value
	}

	/**Session, accepted content type, maximum content length, and value required constructor.
	@param session The Guise session that owns this validator.
	@param acceptedContentType The single accepted content types.	
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session and/or content type is <code>null</code>.
	*/
	public ResourceImportValidator(final GuiseSession<?> session, final ContentType acceptedContentType, final long maxContentLength, final boolean valueRequired)
	{		
		this(session, createHashSet(acceptedContentType), maxContentLength, valueRequired);	//pass a hash set with the single accepted content type
	}

	/**Session, accepted content types, maximum content length, and value required constructor.
	@param session The Guise session that owns this validator.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ResourceImportValidator(final GuiseSession<?> session, final Set<ContentType> acceptedContentTypes, final long maxContentLength, final boolean valueRequired)
	{
		this(session, acceptedContentTypes, null, maxContentLength, valueRequired);	//accept any content types
	}

	/**Session, accepted content types, and accepted extensions constructor.
	@param session The Guise session that owns this validator.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@param acceptedExtensions The accepted filename extensions, or <code>null</code> if all filename extensions are accepted.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ResourceImportValidator(final GuiseSession<?> session, final Set<ContentType> acceptedContentTypes, final Set<String> acceptedExtensions)
	{
		this(session, acceptedContentTypes, acceptedExtensions, false);	//accept any content length and don't require a value
	}

	/**Session, accepted content types, accepted extensions, and value required constructor.
	@param session The Guise session that owns this validator.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@param acceptedExtensions The accepted filename extensions, or <code>null</code> if all filename extensions are accepted.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ResourceImportValidator(final GuiseSession<?> session, final Set<ContentType> acceptedContentTypes, final Set<String> acceptedExtensions, final boolean valueRequired)
	{
		this(session, acceptedContentTypes, acceptedExtensions, -1, valueRequired);	//accept any content length
	}

	/**Session, accepted content types, accepted extensions, and maximum content length constructor.
	@param session The Guise session that owns this validator.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@param acceptedExtensions The accepted filename extensions, or <code>null</code> if all filename extensions are accepted.
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ResourceImportValidator(final GuiseSession<?> session, final Set<ContentType> acceptedContentTypes, final Set<String> acceptedExtensions, final long maxContentLength)
	{
		this(session, acceptedContentTypes, acceptedExtensions, maxContentLength, false);	//don't require a value
	}

	/**Session, accepted content types, accepted extensions, maximum content length, and value required constructor.
	@param session The Guise session that owns this validator.
	@param acceptedContentTypes The accepted content types, each of which can have the special wildcard ("*") subtype, or <code>null</code> if all content types are accepted.	
	@param acceptedExtensions The accepted filename extensions, or <code>null</code> if all filename extensions are accepted.
	@param maxContentLength The maximum content length to accept, or -1 if there is no limit to the content length.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ResourceImportValidator(final GuiseSession<?> session, final Set<ContentType> acceptedContentTypes, final Set<String> acceptedExtensions, final long maxContentLength, final boolean valueRequired)
	{
		super(session, valueRequired);	//construct the parent class
		this.acceptedContentTypes=acceptedContentTypes!=null ? unmodifiableSet(new HashSet<ContentType>(acceptedContentTypes)) : null;	//create a read-only copy of the content types passed
		this.acceptedExtensions=acceptedExtensions!=null ? unmodifiableSet(new HashSet<String>(acceptedExtensions)) : null;	//create a read-only copy of the extensions passed
		this.maxContentLength=maxContentLength;
	}

	/**Determines whether a given resource import meets the provided criteria.
	This version delgates to the super class version to determine whether <code>null</code> values are allowed.
	@param resourceImport The resource import to validate.
	@return <code>true</code> if the resource import meets the criteria of this validator, else <code>false</code>.
	@see #getAcceptedContentTypes()
	@see #getAcceptedExtensions()
	@see #getMaxContentLength()
	*/
	public boolean isValid(final ResourceImport resourceImport)
	{
Debug.trace("ready to validate resource import", resourceImport);
		if(!super.isValid(resourceImport))	//if resource import doesn't pass the default checks (e.g. the value isn't present but is required)
		{
			return false;	//the resource import didn't pass the basic checks
		}
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
				return false;	//the resource import didn't pass the content type test
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
				return false;	//the resource import didn't pass the extension test
			}
		}
		final long maxContentLength=getMaxContentLength();	//get the maximum content length
		if(maxContentLength>=0)	//if there is a content length restriction
		{
			final long resourceContentLength=resourceImport.getContentLength();	//get the content length of the resource
			if(resourceContentLength<0 && resourceContentLength>maxContentLength)	//if the resource content length is not within the allowed range
			{
				return false;	//the resource import didn't have an allowed size
			}
		}
		return true;	//indicate that the resource import passed all the validation tests
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
