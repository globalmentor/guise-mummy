package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.globalmentor.text.xml.stylesheets.css.XMLCSS.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;

import com.guiseframework.component.*;
import com.guiseframework.component.layout.Corner;
import com.guiseframework.component.layout.Flow;
import com.guiseframework.component.layout.Orientation;
import com.guiseframework.event.ActionListener;
import com.guiseframework.event.ModalNavigationListener;
import com.guiseframework.event.NavigateActionListener;
import com.guiseframework.geometry.Axis;
import com.guiseframework.geometry.Dimensions;
import com.guiseframework.geometry.Extent;
import com.guiseframework.geometry.Unit;

/**Strategy for rendering an action control as an XHTML <code>&lt;a&gt;</code> element styled as a button.
This view recognizes {@link Button} controls and can render their images.
@param <C> The type of component being depicted.
@author Garret Wilson
@see Button
*/
public class WebCustomButtonDepictor<C extends ActionControl> extends AbstractWebActionControlDepictor<C>
{

	/**Default constructor using the XHTML <code>&lt;a&gt;</code> element.*/
	public WebCustomButtonDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_A);	//represent <xhtml:a>
	}

	/**Begins the rendering process.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictBegin() throws IOException
	{
		super.depictBegin();	//do the default beginning rendering
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final C component=getDepictedObject();	//get the component
		String href="";	//default to a self-referential href so that the browser will show the link as a link
		for(final ActionListener actionListener:component.getActionListeners())	//for each registered action listener of this action control
		{
			if(actionListener instanceof NavigateActionListener || actionListener instanceof ModalNavigationListener)	//if this is a navigate action listener TODO create a common parent type
			{
				final URI navigationURI=((NavigateActionListener)actionListener).getNavigationURI();	//get the navigation URI
				href=depictContext.getDepictionURI(navigationURI).toString();	//use the navigation URI for our href; if JavaScript and AJAX are installed, they will contact the server themselves; if not, the links will continue to work, albeit without communication with Guise
				break;	//we can't go to two places at once, so it can't be helped if there are unexpectedly multiple navigate action listeners
			}
		}
		depictContext.writeAttribute(null, ELEMENT_A_ATTRIBUTE_HREF, href);	//write the href attribute
//TODO del		writeParameters(context, component);	//write any parameters for the JavaScript
	/*TODO fix	
			//top border elements
		context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//<xhtml:span>	//TODO use constants
		context.writeAttribute(null, ATTRIBUTE_CLASS, "cornerTop");
		context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//<xhtml:span>
		context.writeAttribute(null, ATTRIBUTE_CLASS, "corner1");
		context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//</xhtml:span>
		context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//<xhtml:span>
		context.writeAttribute(null, ATTRIBUTE_CLASS, "corner2");
		context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//</xhtml:span>
		context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//<xhtml:span>
		context.writeAttribute(null, ATTRIBUTE_CLASS, "corner3");
		context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//</xhtml:span>
		context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//<xhtml:span>
		context.writeAttribute(null, ATTRIBUTE_CLASS, "corner4");
		context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//</xhtml:span>
		context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//</xhtml:span>
*/
		
		updateCorners(Flow.Direction.DECREASING);	//update the top two corners
		
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//<xhtml:div>
		writeClassAttribute(getBaseStyleIDs(null, "-decorator"));	//TODO testing; use a constant
		
		URI imageURI=null;	//we'll see if there is an image to use for the button
/*TODO fix
		if(component instanceof Button)	//if the action control is a button
		{
			final Button button=(Button)component;	//cast the component to a button
			imageURI=button.getImage();	//get the URI of the button, if there is one
		}
*/
		
		
/*TODO important fix custom button
		if(imageURI!=null)	//if we have an image for this button
		{
			context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IMG, true);	//<xhtml:img>
			context.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, depictContext.getDepictURI(imageURI).toString());	//src="imageURI"
			String label=model.getLabel();	//get the component label, if there is one
			if(label==null)
			{
				label="";	//TODO fix to use something meaningful
			}
			if(label!=null)	//if there is a label
			{
				context.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_ALT, AbstractModel.getPlainText(label, component.getModel().getLabelContentType()));	//alt="label"
			}			
			context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IMG);	//</xhtml:img>
		}
		else	//if this button has no image, write the label
		{
			if(model.hasLabel())	//if the component has a label, output it inside a <xhtml:span> element
			{
				context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//<xhtml:span>	//use a span element inside the link instead of a label, so that IE will use a link mouse icon
				writeClassAttribute(context, getBaseStyleIDs(component, null, COMPONENT_LABEL_CLASS_SUFFIX));	//write the base style IDs with a "-label" suffix
				writeLabelContent(context, component, model);	//write the content of the label
				context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//</xhtml:span>			
			}
		}
*/
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//</xhtml:div>	//TODO put this in updateEnd()		

		updateCorners(Flow.Direction.INCREASING);	//update the bottom two corners
/*TODO del when works		
		//top border elements
		context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//<xhtml:span>	//TODO use constants
		context.writeAttribute(null, ATTRIBUTE_CLASS, "cornerBottom");
		context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//<xhtml:span>
		context.writeAttribute(null, ATTRIBUTE_CLASS, "corner4");
		context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//</xhtml:span>
		context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//<xhtml:span>
		context.writeAttribute(null, ATTRIBUTE_CLASS, "corner3");
		context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//</xhtml:span>
		context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//<xhtml:span>
		context.writeAttribute(null, ATTRIBUTE_CLASS, "corner2");
		context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//</xhtml:span>
		context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//<xhtml:span>
		context.writeAttribute(null, ATTRIBUTE_CLASS, "corner1");
		context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//</xhtml:span>
		context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//</xhtml:span>
*/
	}

	private final static Extent[] CORNER_MARGINS=new Extent[]{new Extent(5, Unit.PIXEL), new Extent(3, Unit.PIXEL), new Extent(2, Unit.PIXEL), new Extent(1, Unit.PIXEL), new Extent(1, Unit.PIXEL)};
	
	/**Renders corners for the given component.
	@param direction Which physical end of the vertical flow the corner is on (decreasing for top or increasing for bottom). 
	@exception IOException if there is an error rendering the component.
	*/
	protected void updateCorners(final Flow.Direction direction) throws IOException
	{
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final C component=getDepictedObject();	//get the component
		final Orientation orientation=component.getComponentOrientation();	//get the component's orientation
		final Flow flowX=orientation.getFlow(Axis.X);	//each line of the corner will be on the X axis, so find out which flow that is
		final Flow.Direction flowXDirection=orientation.getDirection(flowX);	//get the direction of the horizontal flow
		final Flow flowY=orientation.getFlow(Axis.Y);	//find out what the vertical flow is
		final Flow.Direction flowYDirection=orientation.getDirection(flowY);	//get the direction of the vertical flow
		final Corner leftCorner, rightCorner;	//determine the left and right corners we're working with
		switch(flowX)	//see which axis we're flowing on horizontally
		{
			case LINE:	//if the line flows horizontally
				leftCorner=Corner.getCorner(flowXDirection.getEnd(Flow.Direction.DECREASING), flowYDirection.getEnd(direction));
				rightCorner=Corner.getCorner(flowXDirection.getEnd(Flow.Direction.INCREASING), flowYDirection.getEnd(direction));
				break;
			case PAGE:	//if the page flows horizontally
				leftCorner=Corner.getCorner(flowYDirection.getEnd(direction), flowXDirection.getEnd(Flow.Direction.DECREASING));
				rightCorner=Corner.getCorner(flowYDirection.getEnd(direction), flowXDirection.getEnd(Flow.Direction.INCREASING));
			default:
				throw new AssertionError("Unrecognized horizontal flow: "+flowX);
		}
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//<xhtml:div>
		writeClassAttribute(getBaseStyleIDs(null, "-corners"));	//write the base style IDs with a "-corners" suffix	TODO use a constant
		final Map<String, Object> cornersStyles=new HashMap<String, Object>();	//create a new map of styles
		cornersStyles.put(CSS_PROP_DISPLAY, CSS_DISPLAY_BLOCK);	//display: block
		cornersStyles.put(CSS_PROP_BACKGROUND, "transparent");	//background: transparent TODO use a constant
		writeStyleAttribute(cornersStyles);	//write the corners style
		final int cornerMarginCount=CORNER_MARGINS.length;	//find out how many corner margins there are
		for(int i=0; i<cornerMarginCount; ++i)	//for each corner margin index
		{
			final Extent cornerMargin=CORNER_MARGINS[direction==Flow.Direction.DECREASING ? i : cornerMarginCount-i-1];	//get the correct corner margin, based upon whether these are the top or bottom margins
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//<xhtml:div>		
			writeClassAttribute(getBaseStyleIDs(null, "-corner"));	//write the base style IDs with a "-corner" suffix	TODO use a constant
			final Map<String, Object> cornerStyles=new HashMap<String, Object>();	//create a new map of styles
			cornerStyles.put(CSS_PROP_DISPLAY, CSS_DISPLAY_BLOCK);	//display: block
			cornerStyles.put(CSS_PROP_HEIGHT, new Extent(1, Unit.PIXEL));	//height: 1px
			cornerStyles.put(CSS_PROP_OVERFLOW, CSS_OVERFLOW_HIDDEN);	//overflow: hidden
/*TODO add the background color once we implement this on the component proper
			final back
			cornerStyles.put(CSS_PROP_BACKGROUND, new RGBColor(0xF5F5F5));	//background: backgroundColor
*/
			cornerStyles.put(CSS_PROP_FONT_SIZE, new Extent(1, Unit.PIXEL));	//font-size: 1px
			if(!Dimensions.ZERO_DIMENSIONS.equals(component.getCornerArcSize(leftCorner)))	//if there is a left corner arc size
			{
				cornerStyles.put(CSS_PROP_MARGIN_LEFT, cornerMargin);	//add the appropriate margin for the left side
			}
			if(!Dimensions.ZERO_DIMENSIONS.equals(component.getCornerArcSize(rightCorner)))	//if there is a right corner arc size
			{
				cornerStyles.put(CSS_PROP_MARGIN_RIGHT, cornerMargin);	//add the appropriate margin for the right side
			}
			writeStyleAttribute(cornerStyles);	//write the corner style
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//</xhtml:div>
		}
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//</xhtml:div>
	}
}
