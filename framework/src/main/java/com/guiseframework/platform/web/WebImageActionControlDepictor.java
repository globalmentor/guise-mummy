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

package com.guiseframework.platform.web;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.NavigateActionListener;
import com.guiseframework.geometry.*;
import com.guiseframework.model.AbstractModel;

import static com.globalmentor.w3c.spec.CSS.*;
import static com.globalmentor.w3c.spec.HTML.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;
import static com.guiseframework.platform.web.WebPlatform.*;

/**
 * Strategy for rendering an image action control as an XHTML <code>&lt;img&gt;</code> inside a <code>&lt;a&gt;</code> element. If a link has a
 * {@link NavigateActionListener} as one of its action listeners, the generated <code>href</code> URI will be that of the listener, and a <code>target</code>
 * attribute will be set of the listener specifies a viewport ID. This depictor supports {@link PendingImageComponent}.
 * <p>
 * This view uses the following attributes which are not in XHTML:
 * </p>
 * <ul>
 * <li><code>guise:originalSrc</code></li>
 * <li><code>guise:rolloverSrc</code></li>
 * </ul>
 * @param <C> The type of component being controlled.
 * @author Garret Wilson
 */
public class WebImageActionControlDepictor<C extends ImageComponent & ActionControl> extends WebLinkDepictor<C> {

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version requests a poll interval if the image is pending.
	 * </p>
	 */
	@Override
	public void installed(final C component) {
		super.installed(component); //perform the default installation
		if(component instanceof PendingImageComponent && ((PendingImageComponent)component).isImagePending()) { //if the image is pending
			getPlatform().requestPollInterval(component, 2000); //indicate that polling should occur for this image TODO use a constant			
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version requests any poll interval.
	 * </p>
	 */
	@Override
	public void uninstalled(final C component) {
		if(component instanceof PendingImageComponent && ((PendingImageComponent)component).isImagePending()) { //if the image is pending
			getPlatform().discontinuePollInterval(component); //indicate that polling should no longer occur for this image; another depictor can request polling if necessary
		}
		super.uninstalled(component); //perform the default uninstallation
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation requests or discontinues a poll interval when the pending state changes.
	 * </p>
	 * @see PendingImageComponent#isImagePending()
	 */
	@Override
	protected void depictedObjectPropertyChange(final PropertyChangeEvent propertyChangeEvent) {
		super.depictedObjectPropertyChange(propertyChangeEvent); //do the default property change functionality
		final C component = getDepictedObject(); //get the depicted object
		if(propertyChangeEvent.getSource() == getDepictedObject() && component instanceof PendingImageComponent
				&& PendingImageComponent.IMAGE_PENDING_PROPERTY.equals(propertyChangeEvent.getPropertyName())) { //if the image pending property is changing
			final WebPlatform webPlatform = getPlatform(); //get the web platform
			if(Boolean.TRUE.equals(propertyChangeEvent.getNewValue())) { //if the image is now pending
				webPlatform.requestPollInterval(component, 2000); //indicate that polling should occur for this image TODO use a constant
			} else { //if the image is no longer pending
				webPlatform.discontinuePollInterval(component); //indicate that polling should no longer occur for this image
			}
		}
	}

	/**
	 * Determines the image URI to use for this component. If the delegate image is a {@link PendingImageComponent} with a pending image, this version return the
	 * {@link PendingImageComponent#getPendingImageURI()} value. Otherwise, this version returns the delegate image's {@link ImageComponent#getImageURI()} value.
	 * @return The image to use for the component, or <code>null</code> if there should not be an image.
	 */
	protected URI getImageURI() {
		final C component = getDepictedObject(); //get the component
		return component instanceof PendingImageComponent && ((PendingImageComponent)component).isImagePending() ? ((PendingImageComponent)component)
				.getPendingImageURI() : component.getImageURI(); //get the image URI to use, using the pending image URI if appropriate
	}

	/**
	 * Determines the rollover image URI to use for this component. This implementation returns <code>null</code>.
	 * @return The rollover image to use for the component, or <code>null</code> if there should be no rollover image.
	 */
	protected URI getRolloverImageURI() {
		return null; //by default don't use a rollover image
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version returns an empty map of styles.
	 * </p>
	 */
	@Override
	protected Map<String, Object> getOuterStyles() {
		return new HashMap<String, Object>(); //don't use any outer styles---all the body styles will be put on the image
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This adds layout fixes for images within tables.
	 * </p>
	 */
	@Override
	protected Map<String, Object> getBodyStyles() {
		final Map<String, Object> styles = super.getBodyStyles(); //get the default body styles
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		final Orientation orientation = component.getComponentOrientation(); //get this component's orientation
		if((orientation.getAxis(Flow.LINE) == Axis.X ? component.getLineExtent() : component.getPageExtent()) == null) { //if there is no preferred width and this image is within a fixed layout, set the maximum width of the image to keep large images from forcing a column width to be very large
			CompositeComponent parent = component.getParent(); //get this component's parent
			if(parent instanceof LayoutComponent) { //if the parent is a layout component
				final Layout<?> layout = ((LayoutComponent)parent).getLayout(); //get the layout
				if(layout instanceof RegionLayout && ((RegionLayout)layout).isFixed()) { //if the container layout is a fixed region layout
					styles.put(CSS_PROP_MAX_WIDTH, "100%"); //indicate a maximum width of 100%				
					final RegionConstraints constraints = ((RegionLayout)layout).getConstraints(component); //get the region constraints
					final Extent pageExtent = constraints.getPageExtent(); //get the page extent of the region
					if(pageExtent != null) { //if a page extent is given, restrict the image to the same height to prevent overflow
						styles.put(CSS_PROP_MAX_HEIGHT, "100%"); //indicate a maximum height of 100% (using a maximum height of the specified height of the region will not resize correctly on Firefox 2 or IE7)
					}
				}
			}
		}
		return styles; //return the updated body styles
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version renders the contained image element.
	 * </p>
	 */
	@Override
	protected void depictBody() throws IOException {
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final GuiseSession session = getSession(); //get the session
		final C component = getDepictedObject(); //get the component
		final String label = component.getLabel(); //get the component label, if there is one
		final String resolvedLabel = label != null ? session.dereferenceString(label) : null; //resolve the label, if there is one
		final URI imageURI = getImageURI(); //get the image URI to use
		if(imageURI != null) { //if there is an image URI
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IMG, true); //<xhtml:img>
			writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX); //write the ID and class for the main element
			writeStyleAttribute(getBodyStyles()); //write the component's body styles
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, depictContext.getDepictionURI(imageURI).toString()); //src="image"
			//TODO fix to use description or something else, and always write an alt, even if there is no information
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_ALT,
					resolvedLabel != null ? AbstractModel.getPlainText(resolvedLabel, component.getLabelContentType()) : ""); //alt="label"
			//TODO determine which rollover image we want to use if the image is selected
			final URI rolloverImage = getRolloverImageURI(); //get the rollover image to use
			if(rolloverImage != null) { //if there is a rollover image
				depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, ELEMENT_IMG_ATTRIBUTE_ORIGINAL_SRC, depictContext.getDepictionURI(imageURI).toString()); //guise:originalSrc="image"
				depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, ELEMENT_IMG_ATTRIBUTE_ROLLOVER_SRC, depictContext.getDepictionURI(rolloverImage).toString()); //guise:rolloverSrc="image"
			}
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IMG); //</html:img>
		}
		super.depictBody(); //update the body normally
	}
}
