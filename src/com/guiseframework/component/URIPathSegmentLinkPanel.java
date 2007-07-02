package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.getPropertyName;

import java.net.URI;
import java.util.StringTokenizer;

import com.garretwilson.lang.CharSequenceUtilities;
import com.garretwilson.lang.ObjectUtilities;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import com.guiseframework.component.layout.*;

/**A panel displaying a URI as a series of links for each path segment,
with each link target representing that path segment relative to the application.
Individual path segments are unencoded when displayed in their respective links.
@author Garret Wilson
*/
public class URIPathSegmentLinkPanel extends AbstractPanel
{

	/**The URI bound property.*/
	public final static String URI_PROPERTY=getPropertyName(URIPathSegmentLinkPanel.class, "uri");
	
	/**The URI, or <code>null</code> if there is no URI.*/
	private URI uri=null;

		/**@return The URI, or <code>null</code> if there is no URI.*/
		public URI getURI() {return uri;}

		/**Sets the URI.
		This is a bound property of type <code>URI</code>.
		@param newURI The new URI, or <code>null</code> if there should be no URI.
		@see #URI_PROPERTY
		*/
		public void setURI(final URI newURI)
		{
			if(!ObjectUtilities.equals(uri, newURI))	//if the value is really changing
			{
				final URI oldURI=uri;	//get the old value
				uri=newURI;	//actually change the value
				clear();	//clear all components from the container
				if(uri!=null)	//if there is a URI
				{
					final StringBuilder hrefStringBuilder=new StringBuilder();	//create a string builder to collect the segments of the URI
					final String rawPath=uri.getRawPath();	//get the raw path
					boolean isAbsolutePath=rawPath!=null && isAbsolutePath(rawPath);	//see if there is a path that is absolute
					final boolean isPathURI=isPathURI(uri);	//see if the URI is just a path
					if(!isPathURI)	//if the URI is not just a path
					{
						hrefStringBuilder.append(changeRawPath(uri, null));	//initialize the href with a path-less URI
						add(new Link(decode(hrefStringBuilder.toString()), URI.create(isAbsolutePath ? hrefStringBuilder.toString()+PATH_SEPARATOR : hrefStringBuilder.toString())));	//add a link for the domain, adding a root path to the href if the URI has an absolute path
					}
					final StringTokenizer pathTokenizer=new StringTokenizer(uri.getRawPath(), String.valueOf(PATH_SEPARATOR), true);	//tokenize the raw path
					while(pathTokenizer.hasMoreTokens())	//while there are more tokens
					{
						final String token=pathTokenizer.nextToken();	//get the next token
						hrefStringBuilder.append(token);	//add the token to the href
						if(CharSequenceUtilities.equals(token, PATH_SEPARATOR))	//if this is the path separator
						{
							final Label pathSeparaterLabel=new Label();	//create a new label for the path separator
							pathSeparaterLabel.setLabel(String.valueOf(PATH_SEPARATOR));	//set the label
							add(pathSeparaterLabel);
						}
						else	//if this is not the path separator
						{
							final String uriString=pathTokenizer.hasMoreTokens() ? hrefStringBuilder.toString()+PATH_SEPARATOR : hrefStringBuilder.toString();	//append a path separator to the href if there is a path separator following this path segment (that's all that can follow this segment, as the path separator is the delimiter)
							final Link link=isPathURI ? new Link(decode(token), uriString) : new Link(decode(token), URI.create(uriString));	//create a link for this path segment, creating a URI if needed
							add(link);	//add a link for this path segment
						}
					}
				}
				firePropertyChange(URI_PROPERTY, oldURI, newURI);	//indicate that the value changed
			}
		}

	/**Default constructor with a default horizontal flow layout.*/
	public URIPathSegmentLinkPanel()
	{
		this((URI)null);	//default to no URI
	}

	/**URI constructor with a default horizontal flow layout..
	@param uri The URI, or <code>null</code> if there should be no URI.
	*/
	public URIPathSegmentLinkPanel(final URI uri)
	{
		this(new FlowLayout(Flow.LINE), uri);	//default to flowing horizontally
	}

	/**Layout constructor and URI constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public URIPathSegmentLinkPanel(final Layout<?> layout)
	{
		this(layout, null);	//construct the class with no default label
	}

	/**Layout and URI constructor.
	@param layout The layout definition for the container.
	@param uri The URI, or <code>null</code> if there should be no URI.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public URIPathSegmentLinkPanel(final Layout<?> layout, final URI uri)
	{
		super(layout);	//construct the parent class
		setURI(uri);	//set the URI to that given
	}

}
