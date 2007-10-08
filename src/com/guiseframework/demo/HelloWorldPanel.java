package com.guiseframework.demo;

import com.guiseframework.component.*;
import com.guiseframework.component.layout.RegionLayout;

/**Hello World Guise demonstration panel.
Copyright ™ 2005-2007 GlobalMentor, Inc.
Demonstrates layout panels, region layouts, and headings.
@author Garret Wilson
*/
public class HelloWorldPanel extends LayoutPanel
{

	/**Default constructor.*/
	public HelloWorldPanel()
	{
		super(new RegionLayout());	//construct the parent class, using a region layout
		setLabel("Guise\u2122 Demonstration: Hello World");	//set the panel title
		
		final Heading helloWorldHeading=new Heading(0);	//create a top-level heading
		helloWorldHeading.setLabel("Hello World!");	//set the text of the heading, using its model
		add(helloWorldHeading);	//add the heading to the panel in the default center
	}

}
