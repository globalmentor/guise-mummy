package com.garretwilson.guise.demo;

import com.garretwilson.guise.component.*;
import com.garretwilson.guise.component.layout.*;
import com.garretwilson.guise.session.GuiseSession;

/**Hello World Guise demonstration frame.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates frames and headings.
@author Garret Wilson
*/
public class HelloWorldFrame extends DefaultFrame
{

	/**Guise session constructor.
	@param session The Guise session that owns this frame.
	*/
	public HelloWorldFrame(final GuiseSession<?> session)
	{
		super(session);	//construct the parent class
		getModel().setLabel("Hello World Guise\u2122 Demonstration");	//set the frame title
		
		final Panel panel=new Panel(session, new FlowLayout(Axis.Y));	//create a panel flowing vertically
		final Heading helloWorldHeading=new Heading(session, 0);	//create a top-level heading
		helloWorldHeading.getModel().setLabel("Hello World!");	//set the text of the heading, using its model
		panel.add(helloWorldHeading);	//add the heading to the panel
		setContent(panel);	//set the panel as the frame's content
	}

}
