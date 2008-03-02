package com.guiseframework.platform.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import com.guiseframework.component.*;
import com.guiseframework.event.AbstractNavigateActionListener;

import static com.globalmentor.text.xml.stylesheets.css.XMLCSSConstants.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**Strategy for rendering a menu as an accordion menu using the XHTML <code>&lt;ol&gt;</code> element.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebAccordionMenuDepictor<C extends Menu> extends AbstractWebMenuDepictor<C>	//TODO move most of this code up into a default XHTMLMenuView
{

	/**Default constructor using the XHTML <code>&lt;div&gt;</code> element.*/
	public WebAccordionMenuDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//represent <xhtml:div>
	}

	/**Returns whether the component is interested in mouse events.
	This version returns <code>true</code>, as menus are always interested in what the mouse is doing.
	@param component The component which may be interested in mouse events.
	@return <code>true</code> if the component is interested in mouse events.
	*/
/*TODO fix
	protected boolean isMouseListener(final C component)	//TODO move up to AbstractXHTMLMenuView when all menus transfer their mouse logic out of the JavaScript
	{
		return !component.getModel().isOpen();	//only do rollovers for menus that are not open already 
//		return true;	//always listen for mouse events
	}
*/

	/**Begins the rendering process.
	This version wraps the component in a decorator element.
	@exception IOException if there is an error rendering the component.
	@exception IllegalArgumentException if the given value control represents a value type this controller doesn't support.
	*/
	protected void depictBegin() throws IOException
	{
		super.depictBegin();	//do the default beginning rendering
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final C component=getDepictedObject();	//get the component
//TODO del Debug.trace("updating accordian menu; is rollover?", component.isRollover());
		final boolean mouseListener=!component.isOpen();	//if the component isn't open, listen for mouse events
				//TODO check---why are we writing mouse events either way? remove this logic
		if(mouseListener)	//if we should listen for mouse events
		{
			writeIDClassAttributes(null, null, MOUSE_LISTENER_CLASS);	//write the ID and class attributes with no prefixes or suffixes, but add the mouse listener suffix
		}
		else	//if we shouldn't listen for mouse events
		{
			writeIDClassAttributes(null, null, MOUSE_LISTENER_CLASS);	//write the ID and class attributes with no prefixes or suffixes			
		}
//TODO fix		writeIDClassAttributes(context, component, null, null, MOUSE_LISTENER_CLASS);	//write the ID and class attributes with no prefixes or suffixes, but add the mouse listener suffix
		writeDirectionAttribute();	//write the component direction, if this component specifies a direction
		
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_A);	//<xhtml:a>
		writeIDClassAttributes(null, COMPONENT_LINK_CLASS_SUFFIX);	//write the ID and class attributes for the link
		final AbstractNavigateActionListener navigateActionListener=WebLinkDepictor.getNavigateActionListener(component);	//get any registered navigate action listener
			//write the href attribute, using the navigation URI if available
			//resolve the URI against the application, because navigation, when it occurs, will do the same
			//use at least an empty string so that the link will be recognized as such by all browsers
		depictContext.writeAttribute(null, ELEMENT_A_ATTRIBUTE_HREF, navigateActionListener!=null ? depictContext.getDepictionURI(navigateActionListener.getNavigationURI()).toString() : "");
		final String target=navigateActionListener!=null ? navigateActionListener.getViewportID() : null;	//if there is a navigate action listener, get it's viewport ID, if any
		if(target!=null)	//if a target is given
		{
			depictContext.writeAttribute(null, ELEMENT_A_ATTRIBUTE_TARGET, target);	//write the target attribute
		}
		writeLabel(decorateID(getPlatform().getDepictIDString(component.getDepictID()), null, COMPONENT_BODY_CLASS_SUFFIX));	//write the label for the menu body, if there is a label
//TODO del		writeLabel(context, component, decorateID(component.getID(), null, COMPONENT_BODY_CLASS_SUFFIX), ACTION_CLASS);	//write the label for the menu body, if there is a label
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_A);	//</xhtml:a>
		
//TODO fix		writeLabel(context, component, component.getModel());	//write the label, if there is one		
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//<xhtml:div> (component-body)
		writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX);	//write the ID and class attributes for the body
		final Map<String, Object> bodyStyles=new HashMap<String, Object>();	//create a new map of styles
		final boolean showOpen=component.isOpen() || (component.isRolloverOpenEnabled() && component.isRollover());	//show the menu as open if it is open or is in rollover state with rollover open enabled
		final String display=showOpen ? CSS_DISPLAY_BLOCK : CSS_DISPLAY_NONE;	//only show the body if the menu is open
		bodyStyles.put(CSS_PROP_DISPLAY, display);	//show or hide the body based upon open state
		writeStyleAttribute(bodyStyles);	//write the body style
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_OL);	//<xhtml:ol> (component-children)
		writeIDClassAttributes(null, COMPONENT_CHILDREN_CLASS_SUFFIX);	//write the ID and class attributes for the children		
	}


	/**Updates the views of any children.
	@exception IOException if there is an error updating the child views.
	*/
	protected void depictChildren() throws IOException
	{
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		depictContext.write("\n");	//format the output
			//don't do the default updating of child views, because we control generation wrapper elements around each child
		for(Component childComponent:getDepictedObject())	//for each child component
		{
			depictContext.writeIndent();	//write an indentation
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_LI);	//<xhtml:li>  (component-child)
			writeIDClassAttributes(null, COMPONENT_CHILD_CLASS_SUFFIX);	//write the ID and class attributes for the child
/*TODO improve by switching to the styles-based attribute writer
			if(!component.isDisplayed())	//if the component is not displayed
			{
				styles.put(CSS_PROP_DISPLAY, CSS_DISPLAY_NONE);	//don't display the component
			}
			if(!component.isVisible())	//if the component is not visible
			{
				styles.put(CSS_PROP_VISIBILITY, CSS_VISIBILITY_HIDDEN);	//hide the component
			}
*/
			if(!childComponent.isDisplayed())	//if the child component is not displayed
			{
				depictContext.writeAttribute(null, "style", "display:none;");	//don't display the wrapper, either (this is necessary for IE, which still leaves a space for the component) TODO use a constant
			}
			
			childComponent.depict();	//update the child view
			depictContext.write("\n");	//format the output
			depictContext.writeIndent();	//write an indentation
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_LI);	//</xhtml:li> (component-child)
			depictContext.write("\n");	//format the output
		}
	}

	/**Ends the rendering process.
	This version closes the decorator elements.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictEnd() throws IOException
	{
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_OL);	//</xhtml:ol> (component-children)
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//</xhtml:div> (component-body)
//TODO fix		writeErrorMessage();	//write the error message, if any
		super.depictEnd();	//do the default ending rendering
	}
}
