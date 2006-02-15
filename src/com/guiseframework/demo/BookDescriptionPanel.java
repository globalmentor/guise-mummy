package com.guiseframework.demo;

import java.net.URI;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.geometry.Extent;

/**Drop Details Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates flyover frames, preferred widths and heights,
	extents, and units.
<p>Contextual flyover tethers can be specified using the following resource keys,
	each of which indicates an application-relateive URI of an image:</p>
<ul>
	<li><code>flyover.frame.tether.image.NEbN</code></li>
	<li><code>flyover.frame.tether.image.NEbE</code></li>
	<li><code>flyover.frame.tether.image.SEbE</code></li>
	<li><code>flyover.frame.tether.image.SEbS</code></li>
	<li><code>flyover.frame.tether.image.SWbS</code></li>
	<li><code>flyover.frame.tether.image.SWbW</code></li>
	<li><code>flyover.frame.tether.image.NWbW</code></li>
	<li><code>flyover.frame.tether.image.NWbN</code></li>
</ul>
@author Garret Wilson
@see com.guiseframework.component.FlyoverFrame
@see com.guiseframework.geometry.CompassPoint
*/
public class BookDescriptionPanel extends DefaultNavigationPanel
{

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public BookDescriptionPanel(final GuiseSession session)
	{
		super(session, new FlowLayout(session, Flow.PAGE));	//construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Book Descriptions");	//set the panel title

			//book panel
		final GroupPanel bookPanel=new GroupPanel(session, new FlowLayout(session, Flow.LINE));	//create the book panel flowing horizontally
		bookPanel.setLabel("Mouse over a Book");	//set the book panel label

		final Image book1=new Image(session);	//_The Great Philosophers_
		book1.setImage(URI.create("http://www.garretwilson.com/books/reviews/greatphilosophers_small.jpg"));	//set the URI
		book1.setDescriptionContentType(XHTML_CONTENT_TYPE);	//use an XHTML description
		book1.setDescription(	//set the description
			"<?xml version='1.0'?>"+
			"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>"+
			"<html xmlns='http://www.w3.org/1999/xhtml'>"+
			"<head><title>The Great Philosophers</title></head>"+
			"<body>"+
			" <p><cite>The Great Philosophers: An Introduction to Western Philosophy</cite></p>"+
			" <p>by Bryan Magee</p>"+
			" <p>Oxford: Oxford University Press, 1987 (2000), ISBN 0-19-289322-X</p>"+
			"	<p><cite>The Great Philosophers</cite> provides a surprisingly thought-provoking introduction to major figures in western philosophy through an unexpected format: a series of interviews."+
			" In 1987 the BBC created a series of television programs in which Bryan Magee interviewed recognized experts on prominent philosophers. This book was created by editing and in places enlarging the transcripts from those shows."+
			" What results is a lively play that draws the reader into important philsophical issues and at the same time provides an insight into the experts doing the discussing.</p>"+
			"</body>"+
			"</html>");
		book1.setFlyoverEnabled(true);	//enable flyovers
			
			//optional flyover specifications (use after setting flyoverEnabled to true, which installs a flyover strategy) 
		book1.getFlyoverStrategy().setStyleID("bookFlyover");	//set the style ID to use for the flyover frame
		book1.getFlyoverStrategy().setPreferredWidth(new Extent(30, Extent.Unit.EM));	//set the preferred flyover frame width
		book1.getFlyoverStrategy().setPreferredHeight(new Extent(20, Extent.Unit.EM));	//set the preferred flyover frame height

		bookPanel.add(book1);	//add the image to the book panel

		final Image book2=new Image(session);	//_The Supreme Court_
		book2.setImage(URI.create("http://www.garretwilson.com/books/reviews/supremecourt_small.jpg"));	//set the URI
		book2.setDescriptionContentType(XHTML_CONTENT_TYPE);	//use an XHTML description
		book2.setDescription(	//set the description
			"<?xml version='1.0'?>"+
			"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>"+
			"<html xmlns='http://www.w3.org/1999/xhtml'>"+
			"<head><title>The Supreme Court</title></head>"+
			"<body>"+
			" <p><cite>The Supreme Court</cite>, Revised and Updated</p>"+
			" <p>by William H. Rehnquist</p>"+
			" <p>New York: Vintage Books, 2001, ISBN 0-375-70861-8</p>"+
			"	<p>William Rehnquist, past Chief Justice of the Supreme Court, has provided the Court's history to the presend day in his book, <cite>The Supreme Court</cite>."+
			"	In his many years at the Court, a certain spark has been lost. Although he tries to bring forth a flicker in his telling of its history, Rehnquist's Supreme Court never really shines.</p>"+
			"</body>"+
			"</html>");
		book2.setFlyoverEnabled(true);	//enable flyovers		
		bookPanel.add(book2);	//add the image to the book panel

		final Image book3=new Image(session);	//_Uncertain Identities_
		book3.setImage(URI.create("http://www.garretwilson.com/books/reviews/uncertainidentities_small.jpg"));	//set the URI
		book3.setDescriptionContentType(XHTML_CONTENT_TYPE);	//use an XHTML description
		book3.setDescription(	//set the description
			"<?xml version='1.0'?>"+
			"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>"+
			"<html xmlns='http://www.w3.org/1999/xhtml'>"+
			"<head><title>Uncertain Identities</title></head>"+
			"<body>"+
			" <p><cite>Uncertain Identities: Craftwork, Women, and Patriarchy in a Village of Eastern Uttar Pradesh</cite></p>"+
			" <p>by Sayantani Jafa</p>"+
			" <p>Calcutta, India: Progressive Publishers, 2003, ISBN 81-8064-029-9</p>"+
			"	<p>Jafa's work is enormously valuable not just because it provides a quick, effective primer to feminist thought surrounding patriarchy and craftwork, but because it also provides a contextualizing account."+
			" The anecdotal summaries given are important not because they persuade, but because they explain.</p>"+
			"</body>"+
			"</html>");
		book3.setFlyoverEnabled(true);	//enable flyovers		
		bookPanel.add(book3);	//add the image to the book panel

		add(bookPanel);	//add the flag panel to the panel
	}

}
