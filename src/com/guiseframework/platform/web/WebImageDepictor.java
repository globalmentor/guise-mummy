package com.guiseframework.platform.web;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static com.globalmentor.text.xml.stylesheets.css.XMLCSSConstants.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.geometry.Axis;
import com.guiseframework.model.AbstractModel;

/**Strategy for rendering an image component an XHTML <code>&lt;img&gt;</code> element.
This depictor supports {@link PendingImageComponent}.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebImageDepictor<C extends ImageComponent> extends AbstractSimpleWebComponentDepictor<C>
{

	/**Called when the depictor is installed in a depicted object.
	This version requests a poll interval if the image is pending.
	@param component The component into which this depictor is being installed.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	@exception IllegalStateException if this depictor is already installed in a depicted object.
	*/
	public void installed(final C component)
	{
		super.installed(component);	//perform the default installation
		if(component instanceof PendingImageComponent && ((PendingImageComponent)component).isImagePending())	//if the image is pending
		{
			getPlatform().requestPollInterval(component, 2000);	//indicate that polling should occur for this image TODO use a constant			
		}
	}

	/**Called when the depictor is uninstalled from a depicted object.
	This version requests any poll interval.
	@param component The component from which this depictor is being uninstalled.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	@exception IllegalStateException if this depictor is not installed in a depicted object.
	*/
	public void uninstalled(final C component)
	{
		if(component instanceof PendingImageComponent && ((PendingImageComponent)component).isImagePending())	//if the image is pending
		{
			getPlatform().discontinuePollInterval(component);	//indicate that polling should no longer occur for this image; another depictor can request polling if necessary
		}
		super.uninstalled(component);	//perform the default uninstallation
	}

	/**Called when a depicted object bound property is changed.
	This method may also be called for objects related to the depicted object, so if specific properties are checked the event source should be verified to be the depicted object.
	This implementation requests or discontinues a poll interval when the pending state changes. 
	@param propertyChangeEvent An event object describing the event source and the property that has changed.
	@see PendingImageComponent#isImagePending()
	*/
	protected void depictedObjectPropertyChange(final PropertyChangeEvent propertyChangeEvent)
	{
		super.depictedObjectPropertyChange(propertyChangeEvent);	//do the default property change functionality
		final C component=getDepictedObject();	//get the depicted object
		if(propertyChangeEvent.getSource()==getDepictedObject() && component instanceof PendingImageComponent && PendingImageComponent.IMAGE_PENDING_PROPERTY.equals(propertyChangeEvent.getPropertyName()))	//if the image pending property is changing
		{
			final WebPlatform webPlatform=getPlatform();	//get the web platform
			if(Boolean.TRUE.equals(propertyChangeEvent.getNewValue()))	//if the image is now pending
			{
				webPlatform.requestPollInterval(component, 2000);	//indicate that polling should occur for this image TODO use a constant
			}
			else	//if the image is no longer pending
			{
				webPlatform.discontinuePollInterval(component);	//indicate that polling should no longer occur for this image
			}
		}
	}

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

	/**Determines the image URI to use for this component.
	If the delegate image is a {@link PendingImageComponent} with a pending image, this version return the {@link PendingImageComponent#getPendingImageURI()} value.
	Otherwise, this version returns the delegate image's {@link ImageComponent#getImageURI()} value.
	@return The image to use for the component, or <code>null</code> if there should not be an image.
	*/
	protected URI getImageURI()
	{
		final C component=getDepictedObject();	//get the component
		return component instanceof PendingImageComponent && ((PendingImageComponent)component).isImagePending() ? ((PendingImageComponent)component).getPendingImageURI() : component.getImageURI();	//get the image URI to use, using the pending image URI if appropriate
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
		final URI imageURI=getImageURI();	//get the component image URI
		if(imageURI!=null)	//if there is an image URI
		{
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, depictContext.getDepictionURI(imageURI).toString());	//src="imageURI"
		}
		final String label=component.getLabel();	//get the component label, if there is one
		String resolvedLabel=label!=null ? session.dereferenceString(label) : null;	//resolve the label, if there is one
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
