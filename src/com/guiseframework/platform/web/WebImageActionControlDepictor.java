package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.NavigateActionListener;
import com.guiseframework.geometry.*;
import com.guiseframework.model.AbstractModel;

import static com.garretwilson.text.xml.stylesheets.css.XMLCSSConstants.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;
import static com.guiseframework.platform.web.WebPlatform.*;

/**Strategy for rendering an image action control as an XHTML <code>&lt;img&gt;</code> inside a <code>&lt;a&gt;</code> element.
If a link has a {@link NavigateActionListener} as one of its action listeners, the generated <code>href</code> URI will be that of the listener,
	and a <code>target</code> attribute will be set of the listener specifies a viewport ID.
<p>This view uses the following attributes which are not in XHTML:
	<ul>
		<li><code>guise:originalSrc</code></li>
		<li><code>guise:rolloverSrc</code></li>
	</ul>
</p>
@param <C> The type of component being controlled.
@author Garret Wilson
*/
public class WebImageActionControlDepictor<C extends ImageComponent & ActionControl> extends WebLinkDepictor<C>
{

	/**Determines the image to use for this component.
	This implementation returns the component's image.
	@return The image to use for the component, or <code>null</code> if there should not be an image.
	@see ImageComponent#getImageURI()
	*/
	protected URI getImage()
	{
		return getDepictedObject().getImageURI();	//return the component's normal image
	}
	
	/**Determines the rollover image to use for this component.
	This implementation returns <code>null</code>.
	@return The rollover image to use for the component, or <code>null</code> if there should be no rollover image.
	*/
	protected URI getRolloverImage()
	{
		return null;	//by default don't use a rollover image
	}

	/**Retrieves the styles for the outer element of the component.
	This version returns an empty map of styles.
	@return The styles for the outer element of the component, mapped to CSS property names.
	*/
	protected Map<String, Object> getOuterStyles()
	{
		return new HashMap<String, Object>();	//don't use any outer styles---all the body styles will be put on the image
	}

	/**Retrieves the styles for the body element of the component.
	This adds layout fixes for images within tables.
	@return The styles for the body element of the component, mapped to CSS property names.
	*/
	protected Map<String, Object> getBodyStyles()
	{
		final Map<String, Object> styles=super.getBodyStyles();	//get the default body styles
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final C component=getDepictedObject();	//get the component
		final Orientation orientation=component.getComponentOrientation();	//get this component's orientation
		if((orientation.getAxis(Flow.LINE)==Axis.X ? component.getLineExtent() : component.getPageExtent())==null)	//if there is no preferred width and this image is within a fixed layout, set the maximum width of the image to keep large images from forcing a column width to be very large
		{
			CompositeComponent parent=component.getParent();	//get this component's parent
			if(parent instanceof LayoutComponent)	//if the parent is a layout component
			{
				final Layout<?> layout=((LayoutComponent)parent).getLayout();	//get the layout
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
					final RegionConstraints constraints=((RegionLayout)layout).getConstraints(component);	//get the region constraints
					final Extent pageExtent=constraints.getPageExtent();	//get the page extent of the region
					if(pageExtent!=null && !isUserAgentIE6)	//if a page extent is given, restrict the image to the same height to prevent overflow (don't do this on IE6 because, even using expressions to simulate max-height, the images aren't scaled correctly and the browser is slowed down immensely)
					{
						styles.put(CSS_PROP_MAX_HEIGHT, "100%");	//indicate a maximum height of 100% (using a maximum height of the specified height of the region will not resize correctly on Firefox 2 or IE7)
					}
				}
			}
		}
		return styles;	//return the updated body styles
	}

	/**Renders the body of the component.
	This version renders the contained image element.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictBody() throws IOException
	{
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final GuiseSession session=getSession();	//get the session
		final C component=getDepictedObject();	//get the component
		final String label=component.getLabel();	//get the component label, if there is one
		final String resolvedLabel=label!=null ? session.resolveString(label) : null;	//resolve the label, if there is one
		final URI image=getImage();	//get the image to use
		if(image!=null)	//if there is an image
		{
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IMG, true);	//<xhtml:img>
			writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX);	//write the ID and class for the main element
			writeStyleAttribute(getBodyStyles());	//write the component's body styles
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, session.resolveURI(image).toString());	//src="image"
			//TODO fix to use description or something else, and always write an alt, even if there is no information
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_ALT, resolvedLabel!=null ? AbstractModel.getPlainText(resolvedLabel, component.getLabelContentType()) : "");	//alt="label"
				//TODO determine which rollover image we want to use if the image is selected
			final URI rolloverImage=getRolloverImage();	//get the rollover image to use
			if(rolloverImage!=null)	//if there is a rollover image
			{
				depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, ELEMENT_IMG_ATTRIBUTE_ORIGINAL_SRC, session.resolveURI(image).toString());	//guise:originalSrc="image"
				depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, ELEMENT_IMG_ATTRIBUTE_ROLLOVER_SRC, session.getApplication().resolveURI(rolloverImage).toString());	//guise:rolloverSrc="image"
			}
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IMG);	//</html:img>
		}		
		super.depictBody();	//update the body normally
	}
}
