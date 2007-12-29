package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.net.URI;
import java.util.StringTokenizer;

import com.garretwilson.lang.CharSequenceUtilities;
import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.net.URIPath;
import com.garretwilson.net.URIs;
import com.garretwilson.util.Debug;

import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIs.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;

/**A panel displaying a navigation URI as a series of links for each path segment.
The navigation URI is translated to depiction terms before being displayed,
so that the path segments are shown in depiction terms. 
Individual path segments are unencoded when displayed in their respective links.
@author Garret Wilson
*/
public class NavigationURIPathSegmentLinkPanel extends AbstractPanel
{

	/**The URI bound property.*/
	public final static String NAVIGATION_URI_PROPERTY=getPropertyName(NavigationURIPathSegmentLinkPanel.class, "navigationURI");
	
	/**The navigation URI, or <code>null</code> if there is no URI.*/
	private URI navigationURI=null;

		/**@return The navigation URI, or <code>null</code> if there is no URI.*/
		public URI getNavigationURI() {return navigationURI;}

		/**Sets the navigation URI.
		This is a bound property.
		@param newNavigationURI The new URI, or <code>null</code> if there should be no navigation URI.
		@see #NAVIGATION_URI_PROPERTY
		*/
		public void setNavigationURI(final URI newNavigationURI)
		{
			if(!ObjectUtilities.equals(navigationURI, newNavigationURI))	//if the value is really changing
			{
				final URI oldNavigationURI=navigationURI;	//get the old value
				navigationURI=newNavigationURI;	//actually change the value
				clear();	//clear all components from the container
				if(navigationURI!=null)	//if there is a URI
				{
					final GuiseSession session=getSession();	//get the Guise session
//TODO del					final URI depictionURI=session.getDepictionURI(navigationURI);	//get the depiction URI to show
					
					
					final StringBuilder navigationPathStringBuilder=new StringBuilder();	//create a string builder to collect the segments of the URI
//TODO del					final String depictionRawPath=depictionURI.getRawPath();	//get the raw path
					final boolean isPathURI=true;	//TODO fix
/*TODO fix or just remove absolute URI capability
					boolean isAbsolutePath=depictionRawPath!=null && isAbsolutePath(depictionRawPath);	//see if there is a path that is absolute
					final boolean isPathURI=isPathURI(depictionURI);	//see if the URI is just a path
					if(!isPathURI)	//if the URI is not just a path
					{
							//TODO fix; incomplete and untested
						depictionURIStringBuilder.append(changeRawPath(depictionURI, null));	//initialize the href with a path-less URI
						add(createDepictionLink()
								new Link(decode(depictionURIStringBuilder.toString()), URI.create(isAbsolutePath ? depictionURIStringBuilder.toString()+PATH_SEPARATOR : depictionURIStringBuilder.toString())));	//add a link for the domain, adding a root path to the href if the URI has an absolute path
					}
						//TODO else add a "home" or empty relative path selector
*/
					final StringTokenizer navigationPathTokenizer=new StringTokenizer(navigationURI.getRawPath(), String.valueOf(PATH_SEPARATOR), true);	//tokenize the raw path
					while(navigationPathTokenizer.hasMoreTokens())	//while there are more tokens
					{
						final String token=navigationPathTokenizer.nextToken();	//get the next token
						navigationPathStringBuilder.append(token);	//add the token to the path string builder
						if(CharSequenceUtilities.equals(token, PATH_SEPARATOR))	//if this is the path separator
						{
							final Label pathSeparaterLabel=new Label();	//create a new label for the path separator
							pathSeparaterLabel.setLabel(String.valueOf(PATH_SEPARATOR));	//set the label
							add(pathSeparaterLabel);
						}
						else	//if this is not the path separator
						{
							final URIPath segmentNavigationPath=new URIPath(navigationPathTokenizer.hasMoreTokens() ? navigationPathStringBuilder.toString()+PATH_SEPARATOR : navigationPathStringBuilder.toString());	//append a path separator to the segment string if there is a path separator following this path segment (that's all that can follow this segment, as the path separator is the delimiter)
							add(createDepictionLink(segmentNavigationPath));	//create and add a depiction link for this segment
//TODO del							final Link link=isPathURI ? new Link(decode(token), new URIPath(uriString)) : new Link(decode(token), URI.create(uriString));	//create a link for this path segment, creating a URI if needed
//TODO del							add(link);	//add a link for this path segment
						}
					}
				}
				firePropertyChange(NAVIGATION_URI_PROPERTY, oldNavigationURI, newNavigationURI);	//indicate that the value changed
			}
		}

	protected Link createDepictionLink(final URIPath navigationPath)
	{
		final String labelText;
		final URI depictionURI=getSession().getDepictionURI(navigationPath.toURI());	//get the depiction URI to show
		final String name=URIs.getName(depictionURI);	//get the name---the unencoded last past segment
		if(name!=null)	//if there is a name
		{
			labelText=name;	//use the name for the label text TODO check for "" or "/"
		}
		else	//if there is no name TODO fix for absolute URIs
		{
			labelText=depictionURI.toString();	//TODO maybe decode
		}
		final Link link=new Link(labelText, navigationPath);	//create a link with the text and the navigation path
		return link;	//return the link
	}

	/**Default constructor with a default horizontal flow layout.*/
	public NavigationURIPathSegmentLinkPanel()
	{
		this((URI)null);	//default to no URI
	}

	/**URI constructor with a default horizontal flow layout.
	@param navigationURI The navigation URI, or <code>null</code> if there should be no navigation URI.
	*/
	public NavigationURIPathSegmentLinkPanel(final URI navigationURI)
	{
		this(new FlowLayout(Flow.LINE), navigationURI);	//default to flowing horizontally
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public NavigationURIPathSegmentLinkPanel(final Layout<?> layout)
	{
		this(layout, null);	//construct the class with no default label
	}

	/**Layout and URI constructor.
	@param layout The layout definition for the container.
	@param navigationURI The navigation URI, or <code>null</code> if there should be no navigation URI.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public NavigationURIPathSegmentLinkPanel(final Layout<?> layout, final URI navigationURI)
	{
		super(layout);	//construct the parent class
		setNavigationURI(navigationURI);	//set the URI to that given
	}

}
