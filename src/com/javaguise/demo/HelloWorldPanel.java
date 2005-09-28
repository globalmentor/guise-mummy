package com.javaguise.demo;

import com.javaguise.component.*;
import com.javaguise.component.layout.RegionLayout;
import com.javaguise.session.GuiseSession;

/**Hello World Guise demonstration panel.
Copyright � 2005 GlobalMentor, Inc.
Demonstrates panels, region layouts, and headings.
@author Garret Wilson
*/
public class HelloWorldPanel extends DefaultNavigationPanel
{

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public HelloWorldPanel(final GuiseSession session)
	{
		super(session, new RegionLayout(session));	//construct the parent class, using a region layout
		getModel().setLabel("Guise\u2122 Demonstration: Hello World");	//set the panel title
		
		final Heading helloWorldHeading=new Heading(session, 0);	//create a top-level heading
		helloWorldHeading.getModel().setLabel("Hello World!");	//set the text of the heading, using its model
		add(helloWorldHeading);	//add the heading to the panel in the default center
	}

}