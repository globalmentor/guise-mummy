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

package io.guise.framework.demo;

import java.net.URI;

import com.globalmentor.w3c.spec.HTML;

import io.guise.framework.component.*;
import io.guise.framework.component.layout.*;
import io.guise.framework.geometry.Extent;
import io.guise.framework.geometry.Unit;

/**
 * Drop Details Guise demonstration panel. Copyright © 2005 GlobalMentor, Inc. Demonstrates image description visibility, flyover frames, preferred widths and
 * heights, extents, and units.
 * <p>
 * Contextual flyover tethers can be specified using the following resource keys, each of which indicates an application-relative URI of an image:
 * </p>
 * <ul>
 * <li><code>flyover.frame.tether.image.NEbN</code></li>
 * <li><code>flyover.frame.tether.image.NEbE</code></li>
 * <li><code>flyover.frame.tether.image.SEbE</code></li>
 * <li><code>flyover.frame.tether.image.SEbS</code></li>
 * <li><code>flyover.frame.tether.image.SWbS</code></li>
 * <li><code>flyover.frame.tether.image.SWbW</code></li>
 * <li><code>flyover.frame.tether.image.NWbW</code></li>
 * <li><code>flyover.frame.tether.image.NWbN</code></li>
 * </ul>
 * @author Garret Wilson
 * @see io.guise.framework.component.FlyoverFrame
 * @see io.guise.framework.geometry.CompassPoint
 */
public class BookDescriptionPanel extends LayoutPanel {

	/** Default constructor. */
	public BookDescriptionPanel() {
		super(new FlowLayout(Flow.PAGE)); //construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Book Descriptions"); //set the panel title

		//book panel
		final GroupPanel bookPanel = new GroupPanel(new FlowLayout(Flow.LINE)); //create the book panel flowing horizontally
		bookPanel.setLabel("Mouse over a Book"); //set the book panel label

		final Picture book1 = new Picture(); //_The Great Philosophers_
		book1.setImageURI(URI.create("http://www.garretwilson.com/books/reviews/greatphilosophers_small.jpg")); //set the URI
		book1.setDescriptionContentType(HTML.XHTML_CONTENT_TYPE); //use an XHTML description
		book1.setDescription( //set the description
				"<?xml version='1.0'?>"
						+ "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>"
						+ "<html xmlns='http://www.w3.org/1999/xhtml'>"
						+ "<head><title>The Great Philosophers</title></head>"
						+ "<body>"
						+ " <p><cite>The Great Philosophers: An Introduction to Western Philosophy</cite></p>"
						+ " <p>by Bryan Magee</p>"
						+ " <p>Oxford: Oxford University Press, 1987 (2000), ISBN 0-19-289322-X</p>"
						+ "	<p><cite>The Great Philosophers</cite> provides a surprisingly thought-provoking introduction to major figures in western philosophy through an unexpected format: a series of interviews."
						+ " In 1987 the BBC created a series of television programs in which Bryan Magee interviewed recognized experts on prominent philosophers. This book was created by editing and in places enlarging the transcripts from those shows."
						+ " What results is a lively play that draws the reader into important philsophical issues and at the same time provides an insight into the experts doing the discussing.</p>"
						+ "</body>" + "</html>");
		book1.setDescriptionDisplayed(false); //don't show the image description by the image
		book1.setFlyoverEnabled(true); //enable flyovers

		//optional flyover specifications (use after setting flyoverEnabled to true, which installs a flyover strategy) 
		book1.getFlyoverStrategy().setStyleID("bookFlyover"); //set the style ID to use for the flyover frame
		book1.getFlyoverStrategy().setLineExtent(new Extent(30, Unit.EM)); //set the preferred flyover frame width
		book1.getFlyoverStrategy().setPageExtent(new Extent(20, Unit.EM)); //set the preferred flyover frame height

		bookPanel.add(book1); //add the image to the book panel

		final Picture book2 = new Picture(); //_The Supreme Court_
		book2.setImageURI(URI.create("http://www.garretwilson.com/books/reviews/supremecourt_small.jpg")); //set the URI
		book2.setDescriptionContentType(HTML.XHTML_CONTENT_TYPE); //use an XHTML description
		book2.setDescription( //set the description
				"<?xml version='1.0'?>"
						+ "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>"
						+ "<html xmlns='http://www.w3.org/1999/xhtml'>"
						+ "<head><title>The Supreme Court</title></head>"
						+ "<body>"
						+ " <p><cite>The Supreme Court</cite>, Revised and Updated</p>"
						+ " <p>by William H. Rehnquist</p>"
						+ " <p>New York: Vintage Books, 2001, ISBN 0-375-70861-8</p>"
						+ "	<p>William Rehnquist, past Chief Justice of the Supreme Court, has provided the Court's history to the presend day in his book, <cite>The Supreme Court</cite>."
						+ "	In his many years at the Court, a certain spark has been lost. Although he tries to bring forth a flicker in his telling of its history, Rehnquist's Supreme Court never really shines.</p>"
						+ "</body>" + "</html>");
		book2.setDescriptionDisplayed(false); //don't show the image description by the image
		book2.setFlyoverEnabled(true); //enable flyovers		
		bookPanel.add(book2); //add the image to the book panel

		final Picture book3 = new Picture(); //_Uncertain Identities_
		book3.setImageURI(URI.create("http://www.garretwilson.com/books/reviews/uncertainidentities_small.jpg")); //set the URI
		book3.setDescriptionContentType(HTML.XHTML_CONTENT_TYPE); //use an XHTML description
		book3.setDescription( //set the description
				"<?xml version='1.0'?>"
						+ "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>"
						+ "<html xmlns='http://www.w3.org/1999/xhtml'>"
						+ "<head><title>Uncertain Identities</title></head>"
						+ "<body>"
						+ " <p><cite>Uncertain Identities: Craftwork, Women, and Patriarchy in a Village of Eastern Uttar Pradesh</cite></p>"
						+ " <p>by Sayantani Jafa</p>"
						+ " <p>Calcutta, India: Progressive Publishers, 2003, ISBN 81-8064-029-9</p>"
						+ "	<p>Jafa's work is enormously valuable not just because it provides a quick, effective primer to feminist thought surrounding patriarchy and craftwork, but because it also provides a contextualizing account."
						+ " The anecdotal summaries given are important not because they persuade, but because they explain.</p>" + "</body>" + "</html>");
		book3.setDescriptionDisplayed(false); //don't show the image description by the image
		book3.setFlyoverEnabled(true); //enable flyovers		
		bookPanel.add(book3); //add the image to the book panel

		add(bookPanel); //add the flag panel to the panel
	}

}
