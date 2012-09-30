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

package com.guiseframework.component;

import java.util.*;

import com.globalmentor.java.*;
import com.globalmentor.java.Objects;
import static com.globalmentor.java.Classes.*;
import com.globalmentor.net.*;
import static com.globalmentor.net.URIs.*;

import com.guiseframework.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.NavigateActionListener;

/**A panel displaying a navigation path as a series of breadcrumb links for the path segments.
The navigation path is translated to depiction terms before being displayed,
so that the path segments are shown in depiction terms. 
@author Garret Wilson
@see GuiseSession#getBreadcrumbs(URIPath)
*/
public class NavigationPathBreadcrumbLinkPanel extends AbstractPanel
{

	/**The navigation path bound property.*/
	public final static String NAVIGATION_PATH_PROPERTY=getPropertyName(NavigationPathBreadcrumbLinkPanel.class, "navigationPath");
	
	/**The navigation path, or <code>null</code> if there is no navigation path.*/
	private URIPath navigationPath=null;

		/**@return The navigation path, or <code>null</code> if there is no navigation path.*/
		public URIPath getNavigationPath() {return navigationPath;}

		/**Sets the navigation path.
		This is a bound property.
		@param newNavigationPath The new navigation path, or <code>null</code> if there should be no navigation navigation path.
		@see #NAVIGATION_PATH_PROPERTY
		*/
		public void setNavigationPath(final URIPath newNavigationPath)
		{
			if(!Objects.equals(navigationPath, newNavigationPath))	//if the value is really changing
			{
				final URIPath oldNavigationPath=navigationPath;	//get the old value
				navigationPath=newNavigationPath;	//actually change the value
				clear();	//clear all components from the container
				if(navigationPath!=null)	//if there is a navigation path
				{
					final GuiseSession session=getSession();	//get the Guise session
					final List<Breadcrumb> breadcrumbs=session.getBreadcrumbs(navigationPath);	//get the breadcrumbs for this navigation path
					final Iterator<Breadcrumb> breadcrumbIterator=breadcrumbs.iterator();	//get an iterator to examine the breadcrumbs
					boolean hasNextBreadcrumb=breadcrumbIterator.hasNext();	//see if there is at least one breadcrumb
					while(hasNextBreadcrumb)	//while there are more breadcrumbs
					{
						final Breadcrumb breadcrumb=breadcrumbIterator.next();	//get the next breadcrumb
						final Link link=new Link(breadcrumb);	//create a link from the breadcrumb
						link.addActionListener(new NavigateActionListener(breadcrumb.getNavigationPath()));	//add an action listener to navigate to the breadcrumb's navigation path
						add(link);	//add the link for this breadcrumb
						hasNextBreadcrumb=breadcrumbIterator.hasNext();	//see if there is another breadcrumb
						if(hasNextBreadcrumb || navigationPath.isCollection())	//if there is another breadcrumb or this breadcrumb is for a collection (i.e. if there should be a path separator at the end)
						{
							final Label pathSeparaterLabel=new Label();	//create a new label for the path separator
							pathSeparaterLabel.setLabel(String.valueOf(PATH_SEPARATOR));	//set the label
							add(pathSeparaterLabel);
						}
					}
				}
				firePropertyChange(NAVIGATION_PATH_PROPERTY, oldNavigationPath, newNavigationPath);	//indicate that the value changed
			}
		}

	/**Default constructor with a default horizontal flow layout.*/
	public NavigationPathBreadcrumbLinkPanel()
	{
		this((URIPath)null);	//default to no navigation path
	}

	/**Navigation path constructor with a default horizontal flow layout.
	@param navigationPath The navigation path, or <code>null</code> if there should be no navigation path.
	*/
	public NavigationPathBreadcrumbLinkPanel(final URIPath navigationPath)
	{
		this(new FlowLayout(Flow.LINE), navigationPath);	//default to flowing horizontally
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public NavigationPathBreadcrumbLinkPanel(final Layout<?> layout)
	{
		this(layout, null);	//construct the class with no default label
	}

	/**Layout and URI constructor.
	@param layout The layout definition for the container.
	@param navigationPath The navigation path, or <code>null</code> if there should be no navigation path.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public NavigationPathBreadcrumbLinkPanel(final Layout<?> layout, final URIPath navigationPath)
	{
		super(layout);	//construct the parent class
		setNavigationPath(navigationPath);	//set the path to that given
	}

}
