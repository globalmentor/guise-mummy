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

import java.io.IOException;

import static com.globalmentor.w3c.spec.HTML.*;

import com.guiseframework.component.*;
import com.guiseframework.event.*;
import com.guiseframework.model.AbstractModel;

/**
 * Strategy for rendering an action model control as an XHTML <code>&lt;a&gt;</code> element. If a link has a {@link NavigateActionListener} as one of its
 * action listeners, the generated <code>href</code> URI will be that of the listener, and a <code>target</code> attribute will be set of the listener specifies
 * a viewport ID.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebLinkDepictor<C extends ActionControl> extends AbstractWebActionControlDepictor<C> {

	/** Default constructor using the XHTML <code>&lt;a&gt;</code> element. */
	public WebLinkDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_A); //represent <xhtml:a>
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version renders any component info as a link title.
	 * </p>
	 */
	@Override
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		final AbstractNavigateActionListener navigateActionListener = getNavigateActionListener(getDepictedObject()); //get any registered navigate action listener
		//write the href attribute, using the navigation URI if available
		//resolve the URI against the application, because navigation, when it occurs, will do the same
		//use at least an empty string so that the link will be recognized as such by all browsers
		depictContext.writeAttribute(null, ELEMENT_A_ATTRIBUTE_HREF,
				navigateActionListener != null ? depictContext.getDepictionURI(navigateActionListener.getNavigationURI()).toString() : "");
		final String target = navigateActionListener != null ? navigateActionListener.getViewportID() : null; //if there is a navigate action listener, get it's viewport ID, if any
		if(target != null) { //if a target is given
			depictContext.writeAttribute(null, ELEMENT_A_ATTRIBUTE_TARGET, target); //write the target attribute
		}
		final String info = component.getInfo(); //get the component info, if any
		final String resolvedInfo = info != null ? getSession().dereferenceString(info) : null; //resolve the info, if there is any
		if(resolvedInfo != null) { //if there is info
			depictContext.writeAttribute(null, ATTRIBUTE_TITLE, AbstractModel.getPlainText(resolvedInfo, component.getInfoContentType())); //title="info"
		}
	}

	/**
	 * Retrieves the first {@link NavigateActionListener} or {@link ModalNavigationListener} registered with the given action control, if any. This action
	 * listener is useful for determining whether predetermined navigation action is called for. If more than one navigate action listener is registered with the
	 * given action control, it is undefined which will be returned.
	 * @param actionControl The action control used to look for the registered listener.
	 * @return An action listener registered with the action control with predefined semantics for navigation, or <code>null</code> if no such action listener is
	 *         registered with the control.
	 */
	public static AbstractNavigateActionListener getNavigateActionListener(final ActionControl actionControl) {
		for(final ActionListener actionListener : actionControl.getActionListeners()) { //for each registered action listener of this action control
			if(actionListener instanceof NavigateActionListener || actionListener instanceof ModalNavigationListener) { //if this is a navigate action listener TODO create a common parent type
				return (AbstractNavigateActionListener)actionListener; //cast the action listener to a navigate action listener and return it
			}
		}
		return null; //indicate that we couldn't find a navigate action listener
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version renders any label information.
	 * </p>
	 */
	@Override
	protected void depictBody() throws IOException {
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		writeSupplementaryIcons(); //write any supplementary icons TODO i18n; check direction and determine whether to do this before or after the main label content
		final boolean isIconDisplayed;
		final boolean isLabelDisplayed;
		if(component instanceof LabelDisplayableComponent) { //if this component specifies whether its label should be displayed
			final LabelDisplayableComponent labelDisplayableComponent = (LabelDisplayableComponent)component; //get the label displayable component
			isIconDisplayed = labelDisplayableComponent.isIconDisplayed(); //find out whether label and/or icon should be displayed
			isLabelDisplayed = labelDisplayableComponent.isLabelDisplayed();
		} else { //if this component doesn't specify whether label information should be displayed
			isIconDisplayed = true; //default to showing the information
			isLabelDisplayed = true;
		}
		if(hasLabelContent(isIconDisplayed, isIconDisplayed)) { //if there is label content
		/*TODO del if no longer needed
					depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//<xhtml:span>	//use a span element inside the link instead of a label, so that IE will use a link mouse icon
					writeClassAttribute(getBaseStyleIDs(null, COMPONENT_LABEL_CLASS_SUFFIX));	//write the base style IDs with a "-label" suffix
		*/
			writeLabelContent(isIconDisplayed, isLabelDisplayed); //write the content of the label
			//TODO del if no longer needed			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//</xhtml:span>			
		}
		super.depictBody(); //update the body normally
	}

	/**
	 * Writes the supplementary icons. If no supplementary icons are present, no action occurs. This version does nothing.
	 * @throws IOException if there is an error writing the icons.
	 */
	protected void writeSupplementaryIcons() throws IOException {
	}
}
