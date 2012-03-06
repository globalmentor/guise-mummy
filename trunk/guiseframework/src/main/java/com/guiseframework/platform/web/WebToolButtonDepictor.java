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

import java.util.Map;

import static com.globalmentor.java.Enums.*;
import static com.globalmentor.text.xml.stylesheets.css.XMLCSS.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.geometry.*;
import static com.guiseframework.platform.XHTMLDepictContext.*;
import com.guiseframework.style.Color;
import com.guiseframework.style.RGBColor;

/**Strategy for rendering an action control as an XHTML <code>&lt;button&gt;</code> element.
This depictor allows rollovers and only renders the border upon rollover.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebToolButtonDepictor<C extends ActionControl> extends WebButtonDepictor<C>
{

	/**Default constructor using the XHTML <code>&lt;button&gt;</code> element.*/
	public WebToolButtonDepictor()
	{
		super();	//construct the parent control
		getIgnoredProperties().remove(ActionControl.ROLLOVER_PROPERTY);	//re-enable updates when rollover changes
	}

	/**Retrieves the styles for the body element of the component.
	This version adds special borders for a selected {@link SelectActionControl}.
	@return The styles for the body element of the component, mapped to CSS property names.
	*/
	protected Map<String, Object> getBodyStyles()
	{
		final C component=getDepictedObject();	//get the depicted component
		final GuiseSession session=getSession();	//get the Guise session
		final Map<String, Object> styles=super.getBodyStyles();	//get the default body styles
		if(component.isRollover())	//if the component is in a rollover state
		{
			for(final Side side:CSS_SIDES)	//for each side
			{
				styles.put(CSS_PROPERTY_BORDER_X_STYLE_TEMPLATE.apply(getSerializationName(side)), CSS_VALUE_AUTO);	//set the border width to auto
//TODO del				styles.put(XHTMLDepictContext.CSS_PROPERTY_BORDER_X_WIDTH_TEMPLATE.apply(getSerializationName(side)), CSS_VALUE_AUTO);	//set the border width to auto
			}
		}
		else if(!(component instanceof SelectActionControl) || !((SelectActionControl)component).isSelected())	//if the component isn't in a rollover sttae (don't do anything for selected select action controls, as they already have been rendered differently)
		{
			final CompositeComponent parent=component.getParent();	//get the parent component
			final Color parentBackgroundColor=parent!=null ? parent.getBackgroundColor() : null;	//get the component background color of the parent
			for(final Side side:CSS_SIDES)	//for each side
			{
				styles.put(CSS_PROPERTY_BORDER_X_STYLE_TEMPLATE.apply(getSerializationName(side)), CSS_BORDER_STYLE_SOLID);	//use a solid border to keep the border side from changing
				styles.put(CSS_PROPERTY_BORDER_X_COLOR_TEMPLATE.apply(getSerializationName(side)), parentBackgroundColor!=null ? parentBackgroundColor : RGBColor.TRANSPARENT);	//set the border color to the same color as the parent (if there is no parent, use a transparent border, but there should always be a parent)
//TODO del				styles.put(XHTMLDepictContext.CSS_PROPERTY_BORDER_X_WIDTH_TEMPLATE.apply(getSerializationName(side)), Extent.ZERO_EXTENT1);	//set the border width to zero
//TODO del				styles.put(CSS_PROP_BACKGROUND_COLOR, CSS_VALUE_INHERIT);	//set the background color to inherit the color of its parent
			}
				//set the background color to that of the parent
			if(parentBackgroundColor!=null)	//if the parent component has a background color
			{
				styles.put(CSS_PROP_BACKGROUND_COLOR, parentBackgroundColor);	//set the background color
			}
			else	//if the parent component has no background color
			{
				styles.remove(CSS_PROP_BACKGROUND_COLOR);	//make sure that we're using the default background color (in case the parent class set the background color because of the component's background color property setting)
			}
		}
/*TODO to fix to make a depressed border
			final Orientation orientation=component.getComponentOrientation();	//get this component's orientation
			for(final Border border:Border.values())	//for each logical border
			{
				final Side side=orientation.getSide(border);	//get the absolute side on which this border lies
				final Extent borderExtent=component.getBorderExtent(border);	//get the border extent for this border
				if(!borderExtent.isEmpty())	//if there is a border on this side (to save bandwidth, only include border properties if there is a border; the stylesheet defaults to no border)
				{
					styles.put(CSS_PROPERTY_BORDER_X_WIDTH_TEMPLATE.apply(getSerializationName(side)), borderExtent);	//set the border extent
					styles.put(CSS_PROPERTY_BORDER_X_STYLE_TEMPLATE.apply(getSerializationName(side)), component.getBorderStyle(border));	//indicate the border style for this side
					final Color borderColor=component.getBorderColor(border);	//get the border color for this border
					if(borderColor!=null)	//if a border color is specified
					{
						styles.put(CSS_PROPERTY_BORDER_X_COLOR_TEMPLATE.apply(getSerializationName(side)), borderColor);	//set the border color
					}
				}
		}
*/
		return styles;	//return the styles
	}

}
