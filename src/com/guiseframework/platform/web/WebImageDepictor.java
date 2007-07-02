package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static com.garretwilson.text.xml.stylesheets.css.XMLCSSConstants.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.geometry.Axis;
import com.guiseframework.model.AbstractModel;

/**Strategy for rendering an image component an XHTML <code>&lt;img&gt;</code> element.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebImageDepictor<C extends ImageComponent> extends AbstractSimpleWebComponentDepictor<C>
{

	/**Default constructor using the XHTML <code>&lt;label&gt;</code> element.*/
	public WebImageDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_IMG, true);	//represent <xhtml:img>, creating an empty element
	}

	/**Retrieves the styles for the body element of the component.
	This adds layout fixes for images within tables.
	@return The styles for the body element of the component, mapped to CSS property names.
	*/
	protected Map<String, Object> getBodyStyles()
	{
		final Map<String, Object> styles=super.getBodyStyles();	//get the default body styles
		final C component=getDepictedObject();	//get the component
		final Orientation orientation=component.getComponentOrientation();	//get this component's orientation
		if((orientation.getAxis(Flow.LINE)==Axis.X ? component.getLineExtent() : component.getPageExtent())==null)	//if there is no preferred width and this image is within a fixed layout, set the maximum width of the image to keep large images from forcing a column width to be very large
		{
			CompositeComponent parent=component.getParent();	//get this component's parent
			if(parent instanceof Container)	//if the parent is a container
			{
				final Layout<?> layout=((Container)parent).getLayout();	//get the container layout
				if(layout instanceof RegionLayout && ((RegionLayout)layout).isFixed())	//if the container layout is a fixed region layout
				{
					final WebUserAgentProduct userAgent=getPlatform().getClientProduct();	//get the user agent
					final boolean isUserAgentIE6=userAgent.getBrand()==WebUserAgentProduct.Brand.INTERNET_EXPLORER && userAgent.getVersionNumber()<7;	//see if this is IE6
					if(isUserAgentIE6)	//if we're in IE6, simply make the image fill the entire area (it would be better to add max-width, but that's not supported by IE6; on IE7, neither width or max-width takes into consideration the padding of the container)
					{
						styles.put(CSS_PROP_WIDTH, "100%");	//indicate a width of 100%
					}
					else	//if we're not on IE6, assume that the browser can handle max-width
					{
						styles.put(CSS_PROP_MAX_WIDTH, "100%");	//indicate a maximum width of 100%				
					}
				}
			}
		}
		return styles;	//return the updated body styles
	}

	/**Renders the body of the component.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictBody() throws IOException
	{
		super.depictBody();	//render the default main part of the component
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final GuiseSession session=getSession();	//get the session
		final C component=getDepictedObject();	//get the component
		writeLabelContent();	//write the content of the label
		final URI image=component.getImage();	//get the component image
		if(image!=null)	//if there is an image
		{
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, session.resolveURI(image).toString());	//src="image"
		}
		final String label=component.getLabel();	//get the component label, if there is one
		String resolvedLabel=label!=null ? session.resolveString(label) : null;	//resolve the label, if there is one
		if(resolvedLabel==null)
		{
			resolvedLabel="";	//TODO fix
		}
		if(resolvedLabel!=null)	//if there is a label
		{
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_ALT, AbstractModel.getPlainText(resolvedLabel, component.getLabelContentType()));	//alt="label"
		}
	}

}
