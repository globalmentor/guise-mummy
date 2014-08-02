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

import com.globalmentor.net.*;

import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;

/**A panel displaying a navigation path as a series of breadcrumb links for the path segments.
The navigation path is translated to depiction terms before being displayed,
so that the path segments are shown in depiction terms.
This version automatically updates the breadcrumb navigation path when navigation occurs. 
@author Garret Wilson
*/
public class AutoNavigationPathBreadcrumbLinkPanel extends NavigationPathBreadcrumbLinkPanel implements NavigationListener
{

	/**Default constructor with a default horizontal flow layout.*/
	public AutoNavigationPathBreadcrumbLinkPanel()
	{
		this((URIPath)null);	//default to no navigation path
	}

	/**Navigation path constructor with a default horizontal flow layout.
	@param navigationPath The navigation path, or <code>null</code> if there should be no navigation path.
	*/
	public AutoNavigationPathBreadcrumbLinkPanel(final URIPath navigationPath)
	{
		this(new FlowLayout(Flow.LINE), navigationPath);	//default to flowing horizontally
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@throws NullPointerException if the given layout is <code>null</code>.
	*/
	public AutoNavigationPathBreadcrumbLinkPanel(final Layout<?> layout)
	{
		this(layout, null);	//construct the class with no default label
	}

	/**Layout and URI constructor.
	@param layout The layout definition for the container.
	@param navigationPath The navigation path, or <code>null</code> if there should be no navigation path.
	@throws NullPointerException if the given layout is <code>null</code>.
	*/
	public AutoNavigationPathBreadcrumbLinkPanel(final Layout<?> layout, final URIPath navigationPath)
	{
		super(layout, navigationPath);	//construct the parent class
	}

	/**Called when navigation occurs.
	This implementation changes the breadcrumb navigation path to the new navigation path.
	@param navigationEvent The event indicating navigation details.
	*/
	public void navigated(final NavigationEvent navigationEvent)
	{
		setNavigationPath(navigationEvent.getNavigationPath());	//change to the new navigation path
	}

}
