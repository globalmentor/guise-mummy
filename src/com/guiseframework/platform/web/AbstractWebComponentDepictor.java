package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import static java.util.Collections.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.*;

import javax.mail.internet.ContentType;


import com.garretwilson.text.xml.xhtml.XHTML;
import com.garretwilson.util.Debug;
import com.garretwilson.util.NameValuePair;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.geometry.*;
import com.guiseframework.model.*;
import com.guiseframework.model.ui.PresentationModel;
import com.guiseframework.platform.AbstractComponentDepictor;
import com.guiseframework.platform.XHTMLDepictContext;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Enums.*;
import static com.globalmentor.java.Objects.*;
import static com.guiseframework.model.ui.PresentationModel.*;
import com.guiseframework.style.Color;
import com.guiseframework.style.FontStyle;

import static com.garretwilson.text.FormatUtilities.*;
import static com.garretwilson.text.xml.XMLUtilities.*;
import static com.garretwilson.text.xml.stylesheets.css.XMLCSSConstants.*;
import static com.garretwilson.text.xml.xhtml.XHTML.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**The abstract base class for all <code>application/xhtml+xml</code> depictions.
This implementation knows how to return specialized style IDs for components that are {@link Selectable}.
@param <C> The type of component being controlled.
@author Garret Wilson
*/
public abstract class AbstractWebComponentDepictor<C extends Component> extends AbstractComponentDepictor<C> implements WebComponentDepictor<C>
{

	/**The pattern matching the HTML body start tag.*/
	protected final static Pattern BODY_START_PATTERN=Pattern.compile("<body[^>]*>", Pattern.CASE_INSENSITIVE);	//TODO fix to ensure that there is either a space or > after the string "body"

	/**The pattern matching the HTML body end tag.*/
	protected final static Pattern BODY_END_PATTERN=Pattern.compile("</body[^>]*>", Pattern.CASE_INSENSITIVE);	//TODO fix to ensure that there is either a space or > after the string "body"

	/**@return The web platform on which this depictor is depicting ojects.*/
	public WebPlatform getPlatform() {return (WebPlatform)super.getPlatform();}

	/**Retrieves information and functionality related to the current depiction on the web platform.
	This method delegates to {@link WebPlatform#getDepictContext()}.
	@return A context for the current depiction.
	@exception IllegalStateException if no depict context can be returned in the current depiction state.
	*/
	public WebDepictContext getDepictContext() {return (WebDepictContext)super.getDepictContext();}

	/**The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.*/
	private final URI namespaceURI;

		/**Determines the namespace URI of the XML element.
		@param component The component for which an element namespace URI should be retrieved.
		@return The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
		*/
		public URI getNamespaceURI(final C component) {return namespaceURI;}

	/**The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.*/
	private final String localName;

		/**Determines the local name of the XML element.
		@param component The component for which an element local name should be retrieved.
		@return The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
		*/
		public String getLocalName() {return localName;}

	/**Whether an empty element can be created if there is no content.*/
	private final boolean emptyElementAllowed;

		/**Returns whether an empty element can be created if there is no content.
		@param component The component being rendered.
		@return Whether an empty element can be created if there is no content.
		*/
		public boolean isEmptyElementAllowed(final C component) {return emptyElementAllowed;}

	/**The state of this controller's XML element, if there is one.*/ 
	private WebDepictContext.ElementState elementState=null;

	/**Default constructor with no element representation.*/
	public AbstractWebComponentDepictor()
	{
		this(null, null);	//construct the depictor with no element representation
	}

	/**Element namespace and local name constructor that doesn't create an empty element, even if there is no content.
	@param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	@param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	*/
	public AbstractWebComponentDepictor(final URI namespaceURI, final String localName)
	{
		this(namespaceURI, localName, false);	//don't allow an empty element
	}

	/**Element namespace and local name constructor.
	@param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	@param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	@param isEmptyElementAllowed Whether an empty element can be created if there is no content.
	*/
	public AbstractWebComponentDepictor(final URI namespaceURI, final String localName, final boolean isEmptyElementAllowed)
	{
		this.namespaceURI=namespaceURI;
		this.localName=localName;
		this.emptyElementAllowed=isEmptyElementAllowed;
	}

	/**Determines the identifier to place in the name attribute of the component's XHTML element, if appropriate.
	This is usually the string version of the ID of the component, but some grouped components may use a common name.
	@return An identifier appropriate for the name attribute of the component's XHTML element.
	*/
	public String getDepictName()
	{
		return getPlatform().getDepictIDString(getDepictedObject().getDepictID());	//return the web platform string version of the component's unique ID by default
	}

	/**Orchestrates the entire depiction process for the given component.
	Updating child views is delayed until {@link #depictBody(GC, C)}
	A component for an individual component type should usually not override this method, opting instead to override one of the more fine-grained update view methods.
	@exception IOException if there is an error updating the depiction.
	@see #depictBegin(GC, C)
	@see #depictBody(GC, C)
	@see #depictEnd(GC, C)
	*/
	public void depict() throws IOException
	{
		depictBegin();	//begin the rendering process
		depictBody();	//renders the main part of the component
		depictEnd();	//end the rendering process
		setDepicted(true);	//show that the depiction has been updated
	}

	/**Begins the depiction process.
	This version renders the beginning XML element information, if there is any, leaving the beginning tag open for attributes.
	This version writes common XHTML attributes.
	@exception IOException if there is an error updating the depiction.
	*/
	protected void depictBegin() throws IOException
	{
		final C component=getDepictedObject();	//get the depicted component
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final String localName=getLocalName();	//get the element local name, if there is one
		if(localName!=null)	//if there is an element name
		{
			elementState=depictContext.writeElementBegin(getNamespaceURI(component), localName, isEmptyElementAllowed(component));	//start the element
			writeStyleAttribute(getOuterStyles());	//write the component's outer styles
			if(component.isTooltipEnabled())	//if tooltips are enabled for this component
			{
				String info=component.getInfo();	//get advisory information about the component
				ContentType infoContentType=component.getInfoContentType();	//get the info content type
				if(info==null && component instanceof LabelDisplayableComponent && !((LabelDisplayableComponent)component).isLabelDisplayed())	//if there is no info but this component's label is hidden
				{
					info=component.getLabel();	//use the label, if any
					infoContentType=component.getLabelContentType();	//use the label content type
				}
				if(info!=null)	//if we have advisory information
				{
					final String resolvedInfo=component.getSession().dereferenceString(info);	//resolve the info
					depictContext.writeAttribute(null, ATTRIBUTE_TITLE, AbstractModel.getPlainText(resolvedInfo, infoContentType));	//write the advisory information in the HTML title attribute					
				}
			}
		}
		else	//if there is no element name
		{
			elementState=null;	//show that we have no element state
		}
	}

	/**Depicts the body of the component.
	This version depicts the children of the component.
	This version increases and decreases the indention level before and after depicting the children, respectively.
	@exception IOException if there is an error updating the depiction.
	@see AbstractComponentDepictor#depictChildren()
	*/
	protected void depictBody() throws IOException
	{
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		depictContext.indent();	//indent the context
		try
		{
			depictChildren();	//update depict the child components
		}
		finally
		{
			depictContext.unindent();	//always unindent the context
		}
	}

	/**Ends the depiction process.
	This version renders the ending XML element information, if there is any.
	@exception IOException if there is an error updating the depiction.
	*/
	protected void depictEnd() throws IOException
	{
		if(elementState!=null && elementState.isOpen())	//if the element is open
		{
			final String namespaceURIString=elementState.getNamespaceURI();	//get the namespace of the open element
			getDepictContext().writeElementEnd(namespaceURIString!=null ? URI.create(namespaceURIString) : null, elementState.getLocalName());	//end the element
		}
		elementState=null;	//release the element state
	}
	
	/**The array of style IDs for axes for quick lookup.*/
	private final static String[] AXIS_STYLE_IDS;

	/**The array of style IDs for line directions for quick lookup.*/
	private final static String[] LINE_DIRECTION_STYLE_IDS;

	/**Returns whether the component is interested in mouse events.
	This version returns <code>true</code> if the component has at least one mouse listener registered.
	@param component The component which may be interested in mouse events.
	@return <code>true</code> if the component is interested in mouse events.
	*/
	protected boolean isMouseListener()
	{
		return getDepictedObject().hasMouseListeners();	//if there are mouse listeners registered for the component, the component is a mouse listener
	}

	/**The thread-safe shared map of class-derived base style IDs keyed to class names.*/
	private final static Map<String, String[]> cachedClassBaseStyleIDs=new ConcurrentHashMap<String, String[]>();

	/**Retrieves the base style IDs for the given component.
	This version returns the default style ID and the specified component style ID, if any.
	If the component is {@link Selectable} and is selected, the {@link SELECTED_CLASS} is returned.
	@param prefix The prefix that needs to be added to each, or <code>null</code> if there is no prefix to add.
	@param suffix The suffix that needs to be added to each, or <code>null</code> if there is no suffix to add.
	@return The base style IDs for the component.
	@see #getDefaultStyleID(C)
	*/
	protected Set<String> getBaseStyleIDs(final String prefix, final String suffix)
	{
		final C component=getDepictedObject();	//get the depicted component
		final Set<String> baseStyleIDs=new HashSet<String>();	//create a new set of base style IDs
//TODO del don't do away with this just yet; it's needed for menus, real-time text validation display, etc.:		if(!component.getSession().getApplication().isThemed())	//if this application isn't themed, provide enough style class information for stylesheet application
		{
			final Class<? extends Component> componentClass=(Class<? extends Component>)component.getClass();	//get the component class
			final String componentClassName=componentClass.getName();	//get the name of the component class
			String[] classBaseStyleIDs=cachedClassBaseStyleIDs.get(componentClassName);	//get the cached base style IDs for this class, if we have them
			if(classBaseStyleIDs==null)	//if we haven't cached the base style IDs for the class (the race condition here is benign and not worth the synchronization overhead)
			{
				final List<Class<? extends Component>> componentClasses=getAncestorClasses(componentClass, Component.class, true, true, false, true, null);	//add all the super classes and implemented interface (ignoring abstract classes); include the component's class
				classBaseStyleIDs=new String[componentClasses.size()];	//create a new array of base IDs for component class
				int i=0;	//keep track of our index
				for(final Class<?> baseStyleIDClass:componentClasses)	//for each component class
				{
					classBaseStyleIDs[i++]=getVariableName(baseStyleIDClass);	//store the base style ID for this class, incrementing the array index
				}
				cachedClassBaseStyleIDs.put(componentClassName, classBaseStyleIDs);	//cache these base style IDs
			}
			for(final String classBaseStyleID:classBaseStyleIDs)	//for each relevant component class base ID
			{
				baseStyleIDs.add(decorateID(classBaseStyleID, prefix, suffix));	//decorate and add the base style ID
			}
		}
		final String styleID=component.getStyleID();	//get the component's style ID
		if(styleID!=null)	//if the component has a style ID explicitly set
		{
			baseStyleIDs.add(decorateID(styleID, prefix, suffix));	//add the component's explicit style ID
		}
		if(!component.isValid())	//if component is not valid
		{
			baseStyleIDs.add(INVALID_CLASS);	//add the "invalid" class to the component
		}
		if(component instanceof Control)	//if the component is a control
		{
			final Control.Status status=((Control)component).getStatus();	//get the control status
			if(status!=null)	//if there is a status
			{
				final String statusStyleID;	//determine which status style ID to use
				switch(status)	//see which status to use
				{
					case WARNING:
						statusStyleID=WARNING_CLASS;
						break;
					case ERROR:	//TODO fix; this can never happen
						statusStyleID=ERROR_CLASS;
						break;
					default:
						throw new AssertionError("Unknown status: "+status);
				}
				baseStyleIDs.add(statusStyleID);	//add the status style ID
			}
		}
		if(component instanceof Selectable && ((Selectable)component).isSelected())	//if this is a selectable component that is selected
		{
			baseStyleIDs.add(SELECTED_CLASS);	//add the "selected" class to the component				
		}
		return baseStyleIDs;	//return the style IDs
	}

	/**Retrieves the style IDs for the main part of the component.
	This version returns the base style IDs, along with any drag source, drag handle, or drop target style IDs.
	@param prefix The prefix that needs to be added to each, or <code>null</code> if there is no prefix to add.
	@param suffix The suffix that needs to be added to each, or <code>null</code> if there is no suffix to add.
	@return The style IDs for the body of the component.
	*/
	protected Set<String> getBodyStyleIDs(final String prefix, final String suffix)
	{
		final C component=getDepictedObject();	//get the depicted component
		final Set<String> styleIDs=getBaseStyleIDs(prefix, suffix);	//get the component's base style IDs
		
/*TODO fix
		if(component instanceof Control && !((Control)component).isValid())	//if this is an invalid control TODO improve; testing
		{
			styleIDs.add(INVALID_CLASS);	//add the invalid style ID
		}
*/
		
		if(component.isDragEnabled())	//if dragging is enabled
		{
			styleIDs.add(DRAG_SOURCE_CLASS);	//add the drag source style ID
				//TODO maybe add a separate dropSource class for drag-and-drop (as opposed to move, e.g. frames)
			styleIDs.add(DRAG_HANDLE_CLASS);	//add the drag handle style ID
		}
		if(component.isDropEnabled())	//if dropping is enabled
		{
			styleIDs.add(DROP_TARGET_CLASS);	//add the drag target style ID
		}
		if(isMouseListener())	//if this component is interested in mouse events
		{
			styleIDs.add(MOUSE_LISTENER_CLASS);	//add the mouse listener style ID
		}
		return styleIDs;	//return the complete style IDs
	}

	/**Modifies an ID (such as a component ID or a style ID) by adding a prefix and/or suffix as needed.
	@param id The ID.
	@param prefix The prefix that needs to be added, or <code>null</code> if there is no prefix to add.
	@param suffix The suffix that needs to be added, or <code>null</code> if there is no suffix to add.
	@return The ID, with prefix and/or suffix added as needed.
	*/
	protected static String decorateID(final String id, final String prefix, final String suffix)
	{
		if(prefix==null && suffix==null)	//if neither a prefix nor a suffix is given
		{
			return id;	//return the ID unmodified
		}
		final StringBuilder stringBuilder=new StringBuilder();	//create a new string builder
		if(prefix!=null)	//if there is a prefix
		{
			stringBuilder.append(prefix);	//append the prefix
		}
		stringBuilder.append(id);	//append the ID
		if(suffix!=null)	//if there is a suffix
		{
			stringBuilder.append(suffix);	//append the suffix
		}
		return stringBuilder.toString();	//return the string builder we constructed
	}

	/**Adds flow style IDs to the given set of style IDs based upon the given flow.
	The attribute "axisX" or "axisY" will be added to indicate whether physical flow is on the X axis or the Y axis.
	This attribute "dirLTR" or "dirRTL" will be added to indicate whether flow is left-to-right or right-to-left on the X axis.
	@param styleIDs The set of style IDs to which the flow IDs should be added
	@param flow The logical flow, which will be converted into a physical flow and added to the style IDs.
	@return The style IDs with the physical flow axis and line direction added.
	*/
	protected Set<String> addFlowStyleIDs(final Set<String> styleIDs, final Flow flow)
	{
		final Orientation orientation=getDepictedObject().getComponentOrientation();
		final Axis flowAxis=orientation.getAxis(flow);	//see what axis the menu flows on
		styleIDs.add(AXIS_STYLE_IDS[flowAxis.ordinal()]);	//add the style ID for this axis
		final Flow.Direction lineDirection=orientation.getDirection(Flow.LINE);	//get the line direction TODO fix; assumes that the line is on the X axis
		styleIDs.add(LINE_DIRECTION_STYLE_IDS[lineDirection.ordinal()]);	//add the style ID for this line direction
		return styleIDs;	//return the style IDs
	}

	/**Retrieves the styles for the outer element of the component.
	This version returns the style for color.
	@return The styles for the outer element of the component, mapped to CSS property names.
	*/
	protected Map<String, Object> getOuterStyles()
	{
		final C component=getDepictedObject();	//get the depicted component
		final Map<String, Object> styles=new HashMap<String, Object>();	//create a new map of styles
		if(!component.isDisplayed())	//if the component is not displayed
		{
			styles.put(CSS_PROP_DISPLAY, CSS_DISPLAY_NONE);	//don't display the component
		}
		if(!component.isVisible())	//if the component is not visible
		{
			styles.put(CSS_PROP_VISIBILITY, CSS_VISIBILITY_HIDDEN);	//hide the component
		}
//TODO del when works		styles.put(CSS_PROP_VISIBILITY, component.isVisible() ? CSS_VISIBILITY_INHERIT : CSS_VISIBILITY_HIDDEN);	//show or hide the component (if the component is visible, that really means "visible if possible", i.e. inherit)
/*TODO del when works
		final Extent preferredWidth=component.getPreferredWidth();	//get the component's preferred width
		if(preferredWidth!=null)	//if this component has a preferred width 
		{
			styles.put(CSS_PROP_WIDTH, preferredWidth);	//indicate the width
		}
*/
/**TODO
		if(preferredDimensions!=null)	//if this component has preferred dimensions
		{
			styles.put(CSS_PROP_WIDTH, preferredDimensions.getWidth());	//indicate the width
			styles.put(CSS_PROP_HEIGHT, preferredDimensions.getHeight());	//indicate the height
//TODO fix			styles.put(CSS_PROP_OVERFLOW, CSS_OVERFLOW_HIDDEN);	//TODO fix
		}
*/
/*TODO del when works
		final Extent preferredHeight=component.getPreferredHeight();	//get the component's preferred width
		if(preferredHeight!=null)	//if this component has a preferred height 
		{
			styles.put(CSS_PROP_HEIGHT, preferredHeight);	//indicate the height
		}
*/
//TODO del if not needed		final Color color=component.determineColor();	//determine the component color
		final Color color=getColor();	//get the component color to use
		if(color!=null)	//if the component has a color
		{
			styles.put(CSS_PROP_COLOR, color);	//indicate the color
		}
		final double opacity=component.getOpacity();	//get the component's opacity
		if(opacity<1.0)	//if the opacity isn't 100%
		{
			styles.put(CSS_PROP_OPACITY, Double.valueOf(opacity));	//indicate the opacity
		}
		return styles;	//return the styles
	}

	/**Determines the color for rendering the component.
	This version delegates to {@link Component#getColor()}.
	@return The color to use for this component.
	*/
	protected Color getColor()
	{
		return getDepictedObject().getColor();	//return the component's color
	}

	/**Determines the background color for rendering the component.
	This version delegates to {@link Component#getBackgroundColor()}.
	@return The background color to use for this component.
	*/
	protected Color getBackgroundColor()
	{
		return getDepictedObject().getBackgroundColor();	//return the component's background color
	}

	/**Retrieves the styles for the body element of the component.
	This version returns the style for background color.
	@return The styles for the body element of the component, mapped to CSS property names.
	*/
	protected Map<String, Object> getBodyStyles()
	{
		final C component=getDepictedObject();	//get the depicted component
		final GuiseSession session=getSession();	//get the Guise session
		final Map<String, Object> styles=new HashMap<String, Object>();	//create a new map of styles
		final Color backgroundColor=getBackgroundColor();	//get the component background color to use
		if(backgroundColor!=null)	//if the component has a background color
		{
			styles.put(CSS_PROP_BACKGROUND_COLOR, backgroundColor);	//set the background color
		}
/**TODO
		if(preferredDimensions!=null)	//if this component has preferred dimensions
		{
			styles.put(CSS_PROP_WIDTH, preferredDimensions.getWidth());	//indicate the width
			styles.put(CSS_PROP_HEIGHT, preferredDimensions.getHeight());	//indicate the height
//TODO fix			styles.put(CSS_PROP_OVERFLOW, CSS_OVERFLOW_HIDDEN);	//TODO fix
		}
*/
		final Orientation orientation=component.getComponentOrientation();	//get this component's orientation
		for(final Border border:Border.values())	//for each logical border
		{
			final Side side=orientation.getSide(border);	//get the absolute side on which this border lies
			final Extent borderExtent=component.getBorderExtent(border);	//get the border extent for this border
			if(borderExtent.getValue()!=0)	//if there is a border on this side (to save bandwidth, only include border properties if there is a border; the stylesheet defaults to no border)
			{
				styles.put(XHTMLDepictContext.CSS_PROPERTY_BORDER_X_WIDTH_TEMPLATE.apply(getSerializationName(side)), borderExtent);	//set the border extent
				styles.put(XHTMLDepictContext.CSS_PROPERTY_BORDER_X_STYLE_TEMPLATE.apply(getSerializationName(side)), component.getBorderStyle(border));	//indicate the border style for this side
				final Color borderColor=component.getBorderColor(border);	//get the border color for this border
				if(borderColor!=null)	//if a border color is specified
				{
					styles.put(XHTMLDepictContext.CSS_PROPERTY_BORDER_X_COLOR_TEMPLATE.apply(getSerializationName(side)), borderColor);	//set the border color
				}
			}
			final Extent marginExtent=component.getMarginExtent(border);	//get the margin extent for this border
			if(marginExtent.getValue()!=0)	//if a non-zero margin extent is specified (the stylesheet specifies a zero default margin)
			{
				styles.put(XHTMLDepictContext.CSS_PROPERTY_MARGIN_X_TEMPLATE.apply(getSerializationName(side)), marginExtent);	//set the margin extent
			}
			final Extent paddingExtent=component.getPaddingExtent(border);	//get the padding extent for this border
			if(paddingExtent.getValue()!=0)	//if a non-zero padding extent is specified (the stylesheet specifies a zero default padding)
			{
				styles.put(XHTMLDepictContext.CSS_PROPERTY_PADDING_X_TEMPLATE.apply(getSerializationName(side)), paddingExtent);	//set the padding extent
			}
		}
		final URI cursorURI=component.getCursor();	//get the URI for the cursor


		final URI resolvedCursorURI=session.resolveURI(component.getCursor());	//get the URI for the cursor and resolve it against the application, resolving resources in the process
		final URI relativeCursorURI=session.getApplication().getBasePath().toURI().relativize(resolvedCursorURI);	//get the relative cursor URI with all resource references resolved
		if(!Cursor.DEFAULT.getURI().equals(relativeCursorURI))	//if this isn't the default cursor (the stylesheet sets all cursors to the default)
		{
/*TODO fix all this by using a "path:" URI in the theme
			styles.put(CSS_PROP_CURSOR, CSSUtilities.toCursorString(session.getApplication(), orientation, relativeCursorURI));	//indicate the cursor
		}
		if(!Cursor.DEFAULT.getURI().equals(session.dereferenceURI(cursorURI)))	//if this isn't the default cursor (the stylesheet sets all cursors to the default)
		{
			styles.put(CSS_PROP_CURSOR, cursorURI);	//indicate the cursor
*/
			styles.put(CSS_PROP_CURSOR, relativeCursorURI);	//indicate the cursor
		}
		final List<String> fontFamilies=component.getFontFamilies();	//get the component's font prioritized list of font families
		if(fontFamilies!=null)	//if this component has specified font families
		{
			styles.put(CSS_PROP_FONT_FAMILY, fontFamilies);	//indicate the font families
		}
		final Extent fontSize=component.getFontSize();	//get the component's font size
		if(fontSize!=null)	//if this component has a font size
		{
			styles.put(CSS_PROP_FONT_SIZE, fontSize);	//indicate the font size
		}
		final FontStyle fontStyle=component.getFontStyle();	//get the component's font style
		if(fontStyle!=FontStyle.NORMAL)	//if this component has something besides a normal font style (the stylesheet defaults to normal)
		{
			styles.put(CSS_PROP_FONT_STYLE, fontStyle);	//indicate the font style
		}
		final double fontWeight=component.getFontWeight();	//get the component's font weight
		if(fontWeight!=FONT_WEIGHT_NORMAL)	//if this component has a non-normal font weight (the stylesheet defaults to normal)
		{
			styles.put(CSS_PROP_FONT_WEIGHT, Double.valueOf(fontWeight));	//indicate the font weight
		}
		final Extent width=orientation.getAxis(Flow.LINE)==Axis.X ? component.getLineExtent() : component.getPageExtent();	//get the component's requested width
		if(width!=null)	//if this component has a requested width 
		{
			styles.put(CSS_PROP_WIDTH, width);	//indicate the width
		}
		final Extent height=orientation.getAxis(Flow.PAGE)==Axis.Y ? component.getPageExtent() : component.getLineExtent();	//get the component's requested width
		if(height!=null)	//if this component has a requested height 
		{
			styles.put(CSS_PROP_HEIGHT, height);	//indicate the height
		}
		return styles;	//return the styles
	}

	/**Writes an ID attribute with the appropriate prefixes and suffixes.
	@param prefix The prefix that needs to be added to each, or <code>null</code> if there is no prefix to add.
	@param suffix The suffix that needs to be added to each, or <code>null</code> if there is no suffix to add.
	@see Component#getDepictID()
	@exception IOException if there is an error rendering the component.
	*/
	protected void writeIDAttribute(final String prefix, final String suffix) throws IOException
	{
		getDepictContext().writeAttribute(null, ATTRIBUTE_ID, decorateID(getPlatform().getDepictIDString(getDepictedObject().getDepictID()), prefix, suffix));	//write the ID with the correct prefix and suffix
	}

	/**Writes ID and class attributes with the appropriate prefixes and suffixes.
	@param prefix The prefix that needs to be added to each, or <code>null</code> if there is no prefix to add.
	@param suffix The suffix that needs to be added to each, or <code>null</code> if there is no suffix to add.
	@param styleIDs Additional style IDs to include, without the given prefix and suffix.
	@see #writeIDAttribute(String, String)
	@see #getBaseStyleIDs(String, String)
	@exception IOException if there is an error rendering the component.
	*/
	protected void writeIDClassAttributes(final String prefix, final String suffix, final String... styleIDs) throws IOException
	{
		writeIDAttribute(prefix, suffix);	//write the ID with the correct prefix and suffix
		final Set<String> allStyleIDs=getBaseStyleIDs(prefix, suffix);	//get the base style IDs with the correct prefixes and suffixes
		addAll(allStyleIDs, styleIDs);	//add the given style IDs to the default style IDs
		writeClassAttribute(allStyleIDs);	//write the style IDs		
	}

	/**Writes ID and class attributes for the body of the component.
	@param prefix The prefix that needs to be added to each, or <code>null</code> if there is no prefix to add.
	@param suffix The suffix that needs to be added to each, or <code>null</code> if there is no suffix to add.
	@see Component#getDepictID()
	@see #getBodyStyleIDs(String, String)
	@exception IOException if there is an error rendering the component.
	*/
	protected void writeBodyIDClassAttributes(final String prefix, final String suffix) throws IOException
	{
		getDepictContext().writeAttribute(null, ATTRIBUTE_ID, decorateID(getPlatform().getDepictIDString(getDepictedObject().getDepictID()), prefix, suffix));	//write the ID with the correct prefix and suffix
		writeClassAttribute(getBodyStyleIDs(prefix, suffix));	//write the base style IDs with the correct prefixes and suffixes		
	}
	
	/**Writes a message indicating any errors related to the component.
	The error message will be resolved so that any contained resource references will be properly written.
	@exception IOException if there is an error rendering the component.
	*/
	protected void writeErrorMessage() throws IOException
	{
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final String errorMessage=getErrorMessage();	//get the error message, if any
//TODO del test; we currently always need this element for AJAX, until our AJAX routines can create new non-text children		if(errorMessage!=null)	//if the component has errors
		{
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//<xhtml:div> (component-error)
			writeIDClassAttributes(null, COMPONENT_ERROR_CLASS_SUFFIX);	//write the error ID and class
			if(errorMessage!=null)	//if the component has errors
			{
				depictContext.write(getSession().dereferenceString(errorMessage));	//write the error information
			}
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//</xhtml:div>
		}
	}

	/**Writes an XHTML class attribute with the given style IDs.
	@param styleIDs The style IDs to write.
	@exception IOException if there is an error writing the attribute.
	*/
	protected void writeClassAttribute(final Set<String> styleIDs) throws IOException
	{
		if(!styleIDs.isEmpty())	//if there is at least one style ID
		{
			final String styleClass=formatList(new StringBuilder(), SPACE_CHAR, styleIDs).toString();	//concatenate the style IDs using a space delimiter
			getDepictContext().writeAttribute(null, ATTRIBUTE_CLASS, styleClass);	//write the style class attribute			
		}
	}

	/**Writes an XHTML style attribute with the given styles.
	If no styles are provided, the style attribute is not written.
	@param styles The map of styles to write, each keyed to a CSS style property.
	@exception IOException if there is an error writing the attribute.
	*/
	protected void writeStyleAttribute(final Map<String, Object> styles) throws IOException
	{
		if(!styles.isEmpty())	//if there is at least one style
		{
			getDepictContext().writeAttribute(null, ATTRIBUTE_STYLE, getDepictContext().getCSSStyleString(styles, getDepictedObject().getComponentOrientation()));	//construct the style and write the style attribute
		}
	}

	/**Writes an XHTML direction attribute for the X axis direction only if the given component has an orientation explicitly set.
	@exception IOException if there is an error writing the attribute.
	@see Component#getOrientation()
	@see #writeDirectionAttribute(GC, C, Orientation)
	*/
	protected void writeDirectionAttribute() throws IOException
	{
		final Orientation orientation=getDepictedObject().getOrientation();	//get the component's defined orientation
		if(orientation!=null) //if this component has an orientation specified
		{
			writeDirectionAttribute(orientation, orientation.getFlow(Axis.X));	//write the direction attribute for line flow
		}
	}

	/**Writes an XHTML direction attribute with the direction of the given orientation for the given flow.
	@param orientation The orientation information for the component.
	@param flow The flow axis which direction is to represent.
	@exception IOException if there is an error writing the attribute.
	*/
	protected void writeDirectionAttribute(final Orientation orientation, final Flow flow) throws IOException
	{
		final String direction;	//determine the XHTML direction value
		final Flow.Direction orientationDirection=orientation.getDirection(flow);	//get the direction of the orientation on this flow axis
		switch(orientationDirection)	//see which direction is given
		{
			case INCREASING:
				direction=DIR_LTR;	//left-to-right
				break;
			case DECREASING:
				direction=DIR_RTL;	//right-to-left
				break;
			default:
				throw new AssertionError("Unrecognized flow direction: "+orientationDirection);
		}
		getDepictContext().writeAttribute(null, ATTRIBUTE_DIR, direction);	//write the direction in the XHTML dir attribute
	}

	/**Determines if the given component has label content.
	This implementation delegates to {@link #hasLabelContent(LabelModel)}.
	@return <code>true</code> if the component contains some label content, such as an icon or label text.
	*/
	protected boolean hasLabelContent() throws IOException
	{
		return hasLabelContent((LabelModel)getDepictedObject());	//see if the component, considered as a label model, has label content
	}

	/**Determines if the given component has label content.
	This implementation delegates to {@link #hasLabelContent(LabelModel, boolean, boolean)}.
	@param includeIcon <code>true</code> if the icon should be considered for label content.
	@param includeLabel <code>true</code> if the label text should be considered for label content.
	@return <code>true</code> if the component contains some label content, such as an icon or label text.
	*/
	protected boolean hasLabelContent(final boolean includeIcon, final boolean includeLabel) throws IOException
	{
		return hasLabelContent((LabelModel)getDepictedObject(), true, true);	//see if the component, considered as a label model, has label content
	}

	/**Determines if the given label model has label content.
	This implementation delegates to {@link #hasLabelContent(LabelModel, boolean, boolean)}.
	@param labelModel The label model containing the label information.
	@return <code>true</code> if the label model contains some label content, such as an icon or label text.
	*/
	protected boolean hasLabelContent(final LabelModel labelModel) throws IOException
	{
		return hasLabelContent(labelModel, true, true);	//see if there is content, including the icon and the label
	}

	/**Determines if the given label model has label content.
	@param labelModel The label model containing the label information.
	@param includeIcon <code>true</code> if the icon should be considered for label content.
	@param includeLabel <code>true</code> if the label text should be considered for label content.
	@return <code>true</code> if the label model contains some label content, such as an icon or label text.
	*/
	protected boolean hasLabelContent(final LabelModel labelModel, final boolean includeIcon, final boolean includeLabel) throws IOException
	{
		return (includeIcon && labelModel.getGlyphURI()!=null) || (includeLabel && labelModel.getLabel()!=null);	//see if there is label text or an icon
	}

	/**Writes a label element for a component, taking into account the label's content type.
	If no label content is present, no action occurs.
	This method calls {@link #writeLabelContent()}. 
	@param forID The ID of the element with which this label is associated, or <code>null</code> if the label should not be associated with any particular element.
	@param styleIDs Additional style IDs to include, without the given prefix and suffix.
	@exception IOException if there is an error writing the label.
	@see #hasLabelContent()
	@see #writeLabelContent()
	*/
	protected void writeLabel(final String forID, final String... styleIDs) throws IOException
	{
		if(hasLabelContent())	//if there is label content
		{
			final WebDepictContext depictContext=getDepictContext();	//get the depict context
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_LABEL, false);	//<xhtml:label> (component-label) (require a separate ending tag for safety, even though we know the label won't be empty---an empty label would corrupt the DOM tree in IE6)
			writeIDClassAttributes(null, COMPONENT_LABEL_CLASS_SUFFIX, styleIDs);	//write the ID and class for the label element, adding any style IDs given
			if(forID!=null)	//if this label is associated with another element
			{
				depictContext.writeAttribute(null, ELEMENT_LABEL_ATTRIBUTE_FOR, forID);	//for="forID"
			}
			writeLabelContent();	//write the content of the label
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_LABEL);	//</xhtml:label>
		}
	}

	/**Writes the label content of the component, taking into account the label's content type.
	Label styles will be written if needed.
	If no label content is present, no action occurs.
	This implementation delegates to {@link #writeLabelContent(LabelModel)}.
	@exception IOException if there is an error writing the label content.
	*/
	protected void writeLabelContent() throws IOException
	{
		writeLabelContent((LabelModel)getDepictedObject());	//write the label, considering the component as a label model
	}

	/**Writes the label content of the component, taking into account the label's content type.
	Label styles will be written if needed.
	If no label content is present, no action occurs.
	This implementation delegates to {@link #writeLabelContent(LabelModel, boolean, boolean)}.
	@param includeIcon <code>true</code> if the icon should be considered for label content.
	@param includeLabel <code>true</code> if the label text should be considered for label content.
	@exception IOException if there is an error writing the label content.
	*/
	protected void writeLabelContent(final boolean includeIcon, final boolean includeLabel) throws IOException
	{
		writeLabelContent((LabelModel)getDepictedObject(), includeIcon, includeLabel);	//write the label, considering the component as a label model
	}

	/**Writes the label content of the given label model, taking into account the label's content type.
	Label styles will be written if needed.
	If no label content is present, no action occurs.
	This method delegates to {@link #writeLabelContent(LabelModel, PresentationModel)}. 
	@param labelModel The label model containing the label information.
	@exception IOException if there is an error writing the label.
	*/
	protected void writeLabelContent(final LabelModel labelModel) throws IOException
	{
		writeLabelContent(labelModel, getDepictedObject());	//write the label content, using the component's label styles
	}

	/**Writes the label content of the given label model, taking into account the label's content type.
	Label styles will be written if needed.
	If no label content is present, no action occurs.
	This method delegates to {@link #writeLabelContent(LabelModel, PresentationModel, boolean, boolean)}. 
	@param labelModel The label model containing the label content.
	@param uiModel The user interface model containing the label styles.
	@exception IOException if there is an error writing the label.
	*/
	protected void writeLabelContent(final LabelModel labelModel, final PresentationModel uiModel) throws IOException
	{
		writeLabelContent(labelModel, uiModel, true, true);	//write the label content, including icon and label text
	}
	
	/**Writes the label content of the given label model, taking into account the label's content type.
	Label styles will be written if needed.
	If no label content is present, no action occurs.
	This method delegates to {@link #writeText(String, ContentType)}. 
	@param labelModel The label model containing the label content.
	@param includeIcon <code>true</code> if the icon should be considered for label content.
	@param includeLabel <code>true</code> if the label text should be considered for label content.
	@exception IOException if there is an error writing the label.
	*/
	protected void writeLabelContent(final LabelModel labelModel, final boolean includeIcon, final boolean includeLabel) throws IOException
	{
		writeLabelContent(labelModel, getDepictedObject(), includeIcon, includeLabel);	//write the label content, using the component's label styles		
	}

	/**Writes the label content of the given label model, taking into account the label's content type.
	Label styles will be written if needed.
	If no label content is present, no action occurs.
	This method delegates to {@link #writeText(String, ContentType)}. 
	@param labelModel The label model containing the label information.
	@param uiModel The UI model containing the label style information.
	@param includeIcon <code>true</code> if the icon should be considered for label content.
	@param includeLabel <code>true</code> if the label text should be considered for label content.
	@exception IOException if there is an error writing the label.
	@see #hasLabelContent(LabelModel, boolean, boolean)
	@see #getLabelStyles(LabelModel, PresentationModel)
	@see #writeText(String, ContentType)
	*/
	protected void writeLabelContent(final LabelModel labelModel, final PresentationModel uiModel, final boolean includeIcon, final boolean includeLabel) throws IOException
	{
		if(hasLabelContent(labelModel, includeIcon, includeLabel))	//if there is label content
		{
			final WebDepictContext depictContext=getDepictContext();	//get the depict context
			writeStyleAttribute(getLabelStyles(labelModel, uiModel));	//write the label's styles
			final GuiseSession session=getSession();	//get the session
			final String label=labelModel.getLabel();	//determine the label text, if there is any
			final String resolvedLabel=label!=null ? session.dereferenceString(label) : null;	//resolve the label, if there is a label
			final ContentType labelContentType=labelModel.getLabelContentType();	//get the label content type
			final URI icon=labelModel.getGlyphURI();	//get the label icon, if any
			if(includeIcon && icon!=null)	//if there is an icon
			{
				depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IMG, true);	//<xhtml:img>
	//			TODO fix			writeClassAttribute(context, getBaseStyleIDs(component, null, COMPONENT_BODY_CLASS_POSTFIX));	//write the base style IDs with a "-body" suffix
				depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, depictContext.getDepictionURI(icon).toString());	//src="icon"
				//TODO fix to use description or something else, and always write an alt, even if there is no information
				depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_ALT, resolvedLabel!=null ? AbstractModel.getPlainText(resolvedLabel, labelContentType) : "");	//alt="label"
				depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IMG);	//</html:img>
			}
			if(includeLabel && resolvedLabel!=null)	//if there is a label
			{
				if(includeIcon && icon!=null)	//if there was an icon, write a separator
				{
					depictContext.write(' ');	//separate the label and icon
				}
				writeText(resolvedLabel, labelContentType);	//write the text appropriately for its content type
			}
		}
	}

	/**Writes the content of the given label component, taking into account the label's content type.
	This method calls {@link #writeText(GC, String, ContentType)}. 
	@param context Guise context information.
	@param component The controlled component.
	@param labelModel The model representing the label to write.
	@exception IOException if there is an error writing the label.
	@exception NullPointerException if the provided label and/or content type is <code>null</code>.
	@see #writeText(GC, String, ContentType)
	*/
/*TODO del if not needed
	protected void writeLabelContent(final GC context, final C component, final LabelModel labelModel) throws IOException
	{
		if(labelModel.getIcon()!=null || labelModel.getIconResourceKey()!=null || labelModel.getLabel()!=null || labelModel.getLabelResourceKey()!=null)	//if there is a label TODO fix HasLabel
		{
			final GuiseSession session=component.getSession();	//get the session
			final String label=session.determineString(labelModel.getLabel(), labelModel.getLabelResourceKey());	//determine the label text, if there is any
			final URI icon=session.determineURI(labelModel.getIcon(), labelModel.getIconResourceKey());	//determine the label icon
			if(icon!=null)	//if there is an icon
			{
				context.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IMG, true);	//<xhtml:img>
	//			TODO fix			writeClassAttribute(context, getBaseStyleIDs(component, null, COMPONENT_BODY_CLASS_POSTFIX));	//write the base style IDs with a "-body" suffix
				context.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, depictContext.getDepictURI(icon).toString());	//src="icon"
				//TODO fix to use description or something else, and always write an alt, even if there is no information
				context.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_ALT, label!=null ? AbstractModel.getPlainText(label, labelModel.getLabelContentType()) : "");	//alt="label"
				context.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IMG);	//</html:img>
			}
			if(label!=null)	//if there is a label
			{
				if(icon!=null)	//if there was an icon, write a separator
				{
					context.write(" ");	//separate the label and icon
				}
				writeText(context, label, labelModel.getLabelContentType());	//write the text appropriately for its content type
			}
		}
	}
*/

	/**Retrieves the styles for the label of the component.
	This version delegates to {@link #getLabelStyles(LabelModel)} using the component as the label model.
	@param component The component for which styles should be retrieved.
	@return The styles for the label of the component, mapped to CSS property names.
	*/
	protected final Map<String, Object> getLabelStyles()
	{
		return getLabelStyles(getDepictedObject());	//use the component for the label content
	}

	/**Retrieves the styles for the label of the component.
	This version delegates to {@link #getLabelStyles(LabelModel, PresentationModel)} using the component as the UI model.
	@param labelModel The label model containing the label content.
	@return The styles for the label of the component, mapped to CSS property names.
	*/
	protected final Map<String, Object> getLabelStyles(final LabelModel labelModel)
	{
		return getLabelStyles(labelModel, getDepictedObject());	//use the component for the style information
	}

	/**Retrieves the styles for the label of the component.
	@param labelModel The label model containing the label content.
	@param uiModel The model containing the label style information.
	@return The styles for the label of the component, mapped to CSS property names.
	*/
	protected Map<String, Object> getLabelStyles(final LabelModel labelModel, final PresentationModel uiModel)
	{
		final Map<String, Object> labelStyles=new HashMap<String, Object>();	//create a new map of styles
		final List<String> labelFontFamilies=uiModel.getLabelFontFamilies();	//get the label's font prioritized list of font families
		if(labelFontFamilies!=null)	//if this label has specified font families 
		{
			labelStyles.put(CSS_PROP_FONT_FAMILY, labelFontFamilies);	//indicate the font families
		}
		final Extent labelFontSize=uiModel.getLabelFontSize();	//get the label's font size
		if(labelFontSize!=null)	//if this label has a font size 
		{
			labelStyles.put(CSS_PROP_FONT_SIZE, labelFontSize);	//indicate the font size
		}
		final FontStyle fontStyle=uiModel.getLabelFontStyle();	//get the label's font style
		if(fontStyle!=FontStyle.NORMAL)	//if this label has something besides a normal font style (the stylesheet defaults to normal)
		{
			labelStyles.put(CSS_PROP_FONT_STYLE, fontStyle);	//indicate the font style
		}
		final double fontWeight=uiModel.getLabelFontWeight();	//get the label's font weight
		if(fontWeight!=FONT_WEIGHT_NORMAL)	//if this label has a non-normal font weight (the stylesheet defaults to normal)
		{
			labelStyles.put(CSS_PROP_FONT_WEIGHT, Double.valueOf(fontWeight));	//indicate the font weight
		}
		return labelStyles;	//return the styles
	}

	/**Writes text, taking the content type into consideration.
	XHTML text, for example, will first have its outer elements (including <code>&lt;body&gt;</code>) stripped away.
	@param text The text to write.
	@param contentType The content type of the text.
	@exception IOException if there is an error writing the text.
	@exception NullPointerException if the provided text and/or content type is <code>null</code>.
	*/
	protected void writeText(String text, final ContentType contentType) throws IOException	//TODO actually parse and then serialize the content; otherwise, there may be embedded character references that, when sent via AJAX, will cause the XML to be invalid
	{
		checkInstance(text, "Text cannot be null");
		checkInstance(contentType, "Content type cannot be null");
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final boolean isXML=isXML(contentType);	//see if this is XML
		final boolean isHTML=isHTML(contentType);	//see if this is HTML
		if(isXML || isHTML)	//if this is XML or HTML
		{
			if(isHTML(contentType))	//if this is HTML
			{
				final Matcher bodyStartMatcher=BODY_START_PATTERN.matcher(text);	//get a matcher to search the text for the body start tag
				if(bodyStartMatcher.find())	//if we find a body start tag
				{
					final Matcher bodyEndMatcher=BODY_END_PATTERN.matcher(text);	//get a matcher to search the text for the body end tag
					if(bodyEndMatcher.find())	//if we find a body ending tag
					{
						text=text.substring(bodyStartMatcher.end(), bodyEndMatcher.start());	//use only the text between the body tags
					}
				}
			}
			depictContext.writeLiteral(text);	//as this is XML, write the text directly with no encoding
		}
		else	//if this is anything besides XML or HTML
		{
			depictContext.write(text);	//encode and write the text
		}
	}

	/**Writes parameters as hidden, disabled inputs.
	@param parameters The parameters to write.
	@exception IOException if there is an error rendering the component.
	*/
	protected void writeParameterInputs(final NameValuePair<String, String>... parameters) throws IOException
	{
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		for(final NameValuePair<String, String> parameter:parameters)	//for each parameter
		{
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_INPUT, true);	//<xhtml:input>
			depictContext.writeAttribute(null, ATTRIBUTE_NAME, parameter.getName());	//name="parameterName"
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_TYPE, INPUT_TYPE_HIDDEN);	//type="hidden"
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_DISABLED, INPUT_DISABLED_DISABLED);	//disabled="disabled"
			depictContext.writeAttribute(null, ATTRIBUTE_VALUE, parameter.getValue());	//value="parameterValue" TODO maybe encode this to get around attribute value normalization			
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_INPUT);	//</xhtml:input>
		}
	}

	/**Called when a bound property of the component is changed.
	Because XHTML views do not generate any content for invisible views, this version marks as not updated the view of the parent container, if any. 
	@param propertyChangeEvent An event object describing the event source and the property that has changed.
	*/
/*TODO del
	protected void componentPropertyChange(final PropertyChangeEvent propertyChangeEvent)
	{
		super.componentPropertyChange(propertyChangeEvent);	//respond normally to the property change
		if(Component.VISIBLE_PROPERTY.equals(propertyChangeEvent.getPropertyName()))	//if the visibility is changing
		{
			final CompositeComponent parent=getComponent().getParent();	//get the component's parent
			if(parent!=null)	//if this component has a parent
			{
				parent.getView().setUpdated(false);	//tell the parent view it needs to be updated
			}
		}
	}
*/

	/**Determines if this view can be partially updated.
	@param context Guise context information.
	@param component The controlled component.
	@return true if this part of this view can be updated without rendering the view from scratch.
	@exception IOException if there is an error rendering the component.
	*/
/*TODO del if can't be salvaged
	protected boolean isPartiallyUpdateable(final GC context, final C component) throws IOException
	{
		final Set<String> modifiedProperties=getModifiedProperties();	//get the modified properties
			//TODO change to isOnlyStyleModified() or something
		return !modifiedProperties.contains(GENERAL_COMPONENT_PROPERTY)	//if a general modification did not occur
				&& modifiedProperties.size()==1	//and if only one property was modified
				&& (modifiedProperties.contains(Displayable.DISPLAYED_PROPERTY) || modifiedProperties.contains(Component.VISIBLE_PROPERTY));	//and that one property was the "displayed" or "visible" property
	}
*/

	/**Updates a part of the view, based upon the modified properties.
	@param context Guise context information.
	@param component The controlled component.
	@exception IOException if there is an error rendering the component.
	*/
/*TODO del if can't be salvaged
	protected void updatePartial(final GC context, final C component) throws IOException
	{
			//TODO fix for non-style attributes
		context.writeElementBegin(null, "attribute");	//<attribute> TODO decide on namespace; use constant
		context.writeAttribute(null, "id", component.getID());	//id="componentID"	//TODO use constant
		context.writeAttribute(null, "name", ATTRIBUTE_STYLE);	//name="style"	//TODO use constant
		final Map<String, Object> styles=getOuterStyles(context, component);	//write the component's outer styles
		if(styles!=null)	//if there are styles
		{
			context.writeAttribute(null, "value", CSSUtilities.toString(styles));	//construct the style and write the attribute value						
		}
		context.writeElementEnd(null, "attribute");	//</attribute> TODO decide on namespace; use constant
	}
*/

	/**Initializes the axis and direction style ID arrays for quick lookup.
	@see #AXIS_STYLE_IDS
	@see #LINE_DIRECTION_STYLE_IDS
	*/
	static
	{
		final Axis[] axes=Axis.values();	//get the available axes
		final int axisCount=axes.length;	//find out how many axes their are
		AXIS_STYLE_IDS=new String[axisCount];	//create an array of axis style IDs
		for(int i=axisCount-1; i>=0; --i)	//for each axis
		{
			final String axisStyleID;		//well determine the style ID for this axis
			final Axis axis=axes[i];	//get this axis
			switch(axis)	//see which axis this is
			{
				case X:
					axisStyleID=AXIS_X_CLASS;	//"axisX"
					break;
				case Y:
					axisStyleID=AXIS_Y_CLASS;	//"axisY"
					break;
				case Z:
					axisStyleID=AXIS_Z_CLASS;	//"axisZ"
					break;
				default:	//if we don't recognize the axis
					throw new AssertionError("Unsupported axis: "+axis);
			}
			AXIS_STYLE_IDS[i]=axisStyleID;	//store the style ID for this axis
		}
		final Flow.Direction[] directions=Flow.Direction.values();	//get the available flow directions
		final int directionCount=directions.length;	//find out how flow directionsaxes their are
		LINE_DIRECTION_STYLE_IDS=new String[directionCount];	//create an array of direction style IDs
		for(int i=directionCount-1; i>=0; --i)	//for each direction
		{
			final String lineDirectionStyleID;		//well determine the style ID for this line direction
			final Flow.Direction direction=directions[i];	//get this direction
			switch(direction)	//see which axis this is
			{
				case INCREASING:
					lineDirectionStyleID=DIR_LTR_CLASS;	//"dirLTR"
					break;
				case DECREASING:
					lineDirectionStyleID=DIR_RTL_CLASS;	//"dirRTL"
					break;
				default:	//if we don't recognize the direction
					throw new AssertionError("Unsupported direction: "+direction);
			}
			LINE_DIRECTION_STYLE_IDS[i]=lineDirectionStyleID;	//store the style ID for this line direction
		}
	}

}
