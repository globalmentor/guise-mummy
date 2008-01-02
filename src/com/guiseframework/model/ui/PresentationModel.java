package com.guiseframework.model.ui;

import java.net.URI;
import java.util.List;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;
import com.guiseframework.geometry.*;
import com.guiseframework.model.*;
import com.guiseframework.style.*;

import static com.globalmentor.java.ClassUtilities.*;

/**Encapsulation of presentation-related information.
@author Garret Wilson
*/
public interface PresentationModel extends Model, Displayable
{

	/**The bound property of the background color.*/
	public final static String BACKGROUND_COLOR_PROPERTY=getPropertyName(PresentationModel.class, "backgroundColor");
	/**The bound property of the line near border color.*/
	public final static String BORDER_LINE_NEAR_COLOR_PROPERTY=getPropertyName(PresentationModel.class, "borderLineNearColor");
	/**The bound property of the line far border color.*/
	public final static String BORDER_LINE_FAR_COLOR_PROPERTY=getPropertyName(PresentationModel.class, "borderLineFarColor");
	/**The bound property of the page near border color.*/
	public final static String BORDER_PAGE_NEAR_COLOR_PROPERTY=getPropertyName(PresentationModel.class, "borderPageNearColor");
	/**The bound property of the page far border color.*/
	public final static String BORDER_PAGE_FAR_COLOR_PROPERTY=getPropertyName(PresentationModel.class, "borderPageFarColor");	
	/**The bound property of the line near border extent.*/
	public final static String BORDER_LINE_NEAR_EXTENT_PROPERTY=getPropertyName(PresentationModel.class, "borderLineNearExtent");
	/**The bound property of the line far border extent.*/
	public final static String BORDER_LINE_FAR_EXTENT_PROPERTY=getPropertyName(PresentationModel.class, "borderLineFarExtent");
	/**The bound property of the page near border extent.*/
	public final static String BORDER_PAGE_NEAR_EXTENT_PROPERTY=getPropertyName(PresentationModel.class, "borderPageNearExtent");
	/**The bound property of the page far border extent.*/
	public final static String BORDER_PAGE_FAR_EXTENT_PROPERTY=getPropertyName(PresentationModel.class, "borderPageFarExtent");
	/**The bound property of the line near border style.*/
	public final static String BORDER_LINE_NEAR_STYLE_PROPERTY=getPropertyName(PresentationModel.class, "borderLineNearStyle");
	/**The bound property of the line far border style.*/
	public final static String BORDER_LINE_FAR_STYLE_PROPERTY=getPropertyName(PresentationModel.class, "borderLineFarStyle");
	/**The bound property of the page near border style.*/
	public final static String BORDER_PAGE_NEAR_STYLE_PROPERTY=getPropertyName(PresentationModel.class, "borderPageNearStyle");
	/**The bound property of the page far border style.*/
	public final static String BORDER_PAGE_FAR_STYLE_PROPERTY=getPropertyName(PresentationModel.class, "borderPageFarStyle");
	/**The bound property of the color.*/
	public final static String COLOR_PROPERTY=getPropertyName(PresentationModel.class, "color");
	/**The bound property of the line near page near corner arc size.*/
	public final static String CORNER_LINE_NEAR_PAGE_NEAR_ARC_SIZE_PROPERTY=getPropertyName(PresentationModel.class, "cornerLineNearPageNearArcSize");
	/**The bound property of the line far page near corner arc size.*/
	public final static String CORNER_LINE_FAR_PAGE_NEAR_ARC_SIZE_PROPERTY=getPropertyName(PresentationModel.class, "cornerLineFarPageNearArcSize");
	/**The bound property of the line near page far corner arc size.*/
	public final static String CORNER_LINE_NEAR_PAGE_FAR_ARC_SIZE_PROPERTY=getPropertyName(PresentationModel.class, "cornerLineNearPageFarArcSize");
	/**The bound property of the line far page far corner arc size.*/
	public final static String CORNER_LINE_FAR_PAGE_FAR_ARC_SIZE_PROPERTY=getPropertyName(PresentationModel.class, "cornerLineFarPageFarArcSize");
	/**The bound property of the cursor.*/
	public final static String CURSOR_PROPERTY=getPropertyName(PresentationModel.class, "cursor");
	/**The line extent (width in left-to-right top-to-bottom orientation) bound property.*/
	public final static String LINE_EXTENT_PROPERTY=getPropertyName(PresentationModel.class, "lineExtent");
	/**The page extent (height in left-to-right top-to-bottom orientation) bound property.*/
	public final static String PAGE_EXTENT_PROPERTY=getPropertyName(PresentationModel.class, "pageExtent");
	/**The bound property of the font families.*/
	public final static String FONT_FAMILIES_PROPERTY=getPropertyName(PresentationModel.class, "fontFamilies");
	/**The bound property of the font size.*/
	public final static String FONT_SIZE_PROPERTY=getPropertyName(PresentationModel.class, "fontSize");
	/**The bound property of the font style.*/
	public final static String FONT_STYLE_PROPERTY=getPropertyName(PresentationModel.class, "fontStyle");
	/**The bound property of the font weight.*/
	public final static String FONT_WEIGHT_PROPERTY=getPropertyName(PresentationModel.class, "fontWeight");
	/**The bound property of the label font families.*/
	public final static String LABEL_FONT_FAMILIES_PROPERTY=getPropertyName(PresentationModel.class, "labelFontFamilies");
	/**The bound property of the label font size.*/
	public final static String LABEL_FONT_SIZE_PROPERTY=getPropertyName(PresentationModel.class, "labelFontSize");
	/**The bound property of the label font style.*/
	public final static String LABEL_FONT_STYLE_PROPERTY=getPropertyName(PresentationModel.class, "labelFontStyle");
	/**The bound property of the label font weight.*/
	public final static String LABEL_FONT_WEIGHT_PROPERTY=getPropertyName(PresentationModel.class, "labelFontWeight");
	/**The bound property of the line near margin extent.*/
	public final static String MARGIN_LINE_NEAR_EXTENT_PROPERTY=getPropertyName(PresentationModel.class, "marginLineNearExtent");
	/**The bound property of the line far margin extent.*/
	public final static String MARGIN_LINE_FAR_EXTENT_PROPERTY=getPropertyName(PresentationModel.class, "marginLineFarExtent");
	/**The bound property of the page near margin extent.*/
	public final static String MARGIN_PAGE_NEAR_EXTENT_PROPERTY=getPropertyName(PresentationModel.class, "marginPageNearExtent");
	/**The bound property of the page far margin extent.*/
	public final static String MARGIN_PAGE_FAR_EXTENT_PROPERTY=getPropertyName(PresentationModel.class, "marginPageFarExtent");
	/**The opacity bound property.*/
	public final static String OPACITY_PROPERTY=getPropertyName(PresentationModel.class, "opacity");
	/**The bound property of the line near padding extent.*/
	public final static String PADDING_LINE_NEAR_EXTENT_PROPERTY=getPropertyName(PresentationModel.class, "paddingLineNearExtent");
	/**The bound property of the line far padding extent.*/
	public final static String PADDING_LINE_FAR_EXTENT_PROPERTY=getPropertyName(PresentationModel.class, "paddingLineFarExtent");
	/**The bound property of the page near padding extent.*/
	public final static String PADDING_PAGE_NEAR_EXTENT_PROPERTY=getPropertyName(PresentationModel.class, "paddingPageNearExtent");
	/**The bound property of the page far padding extent.*/
	public final static String PADDING_PAGE_FAR_EXTENT_PROPERTY=getPropertyName(PresentationModel.class, "paddingPageFarExtent");
	/**The bound property of the component style ID.*/
	public final static String STYLE_ID_PROPERTY=getPropertyName(PresentationModel.class, "styleID");
	/**The bound property of whether the component has tooltips enabled.*/
	public final static String TOOLTIP_ENABLED_PROPERTY=getPropertyName(PresentationModel.class, "tooltipEnabled");
	/**The bound property of whether the component is visible.*/
	public final static String VISIBLE_PROPERTY=getPropertyName(PresentationModel.class, "visible");

	/*The constant value representing a general rounded corner.*/
	public final static Dimensions ROUNDED_CORNER_ARC_SIZE=new Dimensions(0.25, 0.25, Unit.EM);

	/**The normal font weight.
	@see #setFontWeight(double)
	*/
	public final static double FONT_WEIGHT_NORMAL=0.5;

	/**The bold font weight.
	@see #setFontWeight(double)
	*/
	public final static double FONT_WEIGHT_BOLD=0.75;

	/**@return The background color of the component, or <code>null</code> if no background color is specified for this component.*/
	public Color getBackgroundColor();

	/**Sets the background color of the component.
	This is a bound property.
	@param newBackgroundColor The background color of the component, or <code>null</code> if the default background color should be used.
	@see #BACKGROUND_COLOR_PROPERTY 
	*/
	public void setBackgroundColor(final Color newBackgroundColor);

	/**Returns the border color of the indicated border.
	@param border The border for which a border color should be returned.
	@return The border color of the given border, or <code>null</code> if the default border color should be used.
	*/
	public Color getBorderColor(final Border border);

	/**Returns the border color of the line near page near border.
	@return The border color of the border, or <code>null</code> if the default border color should be used.
	*/
	public Color BorderLineNearColor();

	/**Returns the border color of the line far page near border.
	@return The border color of the border, or <code>null</code> if the default border color should be used.
	*/
	public Color BorderLineFarColor();

	/**Returns the border color of the line near page far border.
	@return The border color of the border, or <code>null</code> if the default border color should be used.
	*/
	public Color BorderPageNearColor();

	/**Returns the border color of the line far page far border.
	@return The border color of the border, or <code>null</code> if the default border color should be used.
	*/
	public Color BorderPageFarColor();

	/**Sets the border color of a given border.
	The border color of each border represents a bound property.
	@param border The border for which the border color should be set.
	@param newBorderColor The border color, or <code>null</code> if the default border color should be used.
	@exception NullPointerException if the given border is <code>null</code>. 
	@see #BORDER_LINE_NEAR_COLOR_PROPERTY
	@see #BORDER_LINE_FAR_COLOR_PROPERTY
	@see #BORDER_PAGE_NEAR_COLOR_PROPERTY
	@see #BORDER_PAGE_FAR_COLOR_PROPERTY
	*/
	public void setBorderColor(final Border border, final Color newBorderColor);

	/**Sets the border COLOR of the line near border.
	This is a bound property.
	@param newBorderColor The border color, or <code>null</code> if the default border color should be used.
	@see #BORDER_LINE_NEAR_COLOR_PROPERTY
	*/
	public void setBorderLineNearColor(final Color newBorderColor);

	/**Sets the border color of the line far border.
	This is a bound property.
	@param newBorderColor The border color, or <code>null</code> if the default border color should be used.
	@see #BORDER_LINE_FAR_COLOR_PROPERTY
	*/
	public void setBorderLineFarColor(final Color newBorderColor);

	/**Sets the border color of the page near border.
	This is a bound property.
	@param newBorderColor The border color, or <code>null</code> if the default border color should be used.
	@see #BORDER_PAGE_NEAR_COLOR_PROPERTY
	*/
	public void setBorderPageNearColor(final Color newBorderColor);

	/**Sets the border color of the page far border.
	This is a bound property.
	@param newBorderColor The border color, or <code>null</code> if the default border color should be used.
	@see #BORDER_PAGE_FAR_COLOR_PROPERTY
	*/
	public void setBorderPageFarColor(final Color newBorderColor);

	/**Sets the border color of all borders.
	The border color of each border represents a bound property.
	This is a convenience method that calls {@link #setBorderColor(Border, Color)} for each border.
	@param newBorderColor The border color, or <code>null</code> if the default border color should be used.
	@see #BORDER_LINE_NEAR_COLOR_PROPERTY
	@see #BORDER_LINE_FAR_COLOR_PROPERTY
	@see #BORDER_PAGE_NEAR_COLOR_PROPERTY
	@see #BORDER_PAGE_FAR_COLOR_PROPERTY
	*/
	public void setBorderColor(final Color newBorderColor);
	
	/**Returns the border extent of the indicated border.
	@param border The border for which a border extent should be returned.
	@return The border extent of the given border.
	*/
	public Extent getBorderExtent(final Border border);

	/**Returns the border extent of the line near page near border.
	@return The border extent of the given border.
	*/
	public Extent BorderLineNearExtent();

	/**Returns the border extent of the line far page near border.
	@return The border extent of the given border.
	*/
	public Extent BorderLineFarExtent();

	/**Returns the border extent of the line near page far border.
	@return The border extent of the given border.
	*/
	public Extent BorderPageNearExtent();

	/**Returns the border extent of the line far page far border.
	@return The border extent of the given border.
	*/
	public Extent BorderPageFarExtent();

	/**Sets the border extent of a given border.
	The border extent of each border represents a bound property.
	@param border The border for which the border extent should be set.
	@param newBorderExtent The border extent.
	@exception NullPointerException if the given border and/or border extent is <code>null</code>. 
	@see #BORDER_LINE_NEAR_EXTENT_PROPERTY
	@see #BORDER_LINE_FAR_EXTENT_PROPERTY
	@see #BORDER_PAGE_NEAR_EXTENT_PROPERTY
	@see #BORDER_PAGE_FAR_EXTENT_PROPERTY
	*/
	public void setBorderExtent(final Border border, final Extent newBorderExtent);

	/**Sets the border extent of the line near border.
	This is a bound property.
	@param newBorderExtent The border extent.
	@exception NullPointerException if the given border extent is <code>null</code>.
	@see #BORDER_LINE_NEAR_EXTENT_PROPERTY
	*/
	public void setBorderLineNearExtent(final Extent newBorderExtent);

	/**Sets the border extent of the line far border.
	This is a bound property.
	@param newBorderExtent The border extent.
	@exception NullPointerException if the given border extent is <code>null</code>.
	@see #BORDER_LINE_FAR_EXTENT_PROPERTY
	*/
	public void setBorderLineFarExtent(final Extent newBorderExtent);

	/**Sets the border extent of the page near border.
	This is a bound property.
	@param newBorderExtent The border extent.
	@exception NullPointerException if the given border extent is <code>null</code>.
	@see #BORDER_PAGE_NEAR_EXTENT_PROPERTY
	*/
	public void setBorderPageNearExtent(final Extent newBorderExtent);

	/**Sets the border extent of the page far border.
	This is a bound property.
	@param newBorderExtent The border extent.
	@exception NullPointerException if the given border extent is <code>null</code>.
	@see #BORDER_PAGE_FAR_EXTENT_PROPERTY
	*/
	public void setBorderPageFarExtent(final Extent newBorderExtent);

	/**Sets the border extent of all borders.
	The border extent of each border represents a bound property.
	This is a convenience method that calls {@link #setBorderExtent(Border, Extent)} for each border.
	@param newBorderExtent The border extent.
	@exception NullPointerException if the given border extent is <code>null</code>.
	@see #BORDER_LINE_NEAR_EXTENT_PROPERTY
	@see #BORDER_LINE_FAR_EXTENT_PROPERTY
	@see #BORDER_PAGE_NEAR_EXTENT_PROPERTY
	@see #BORDER_PAGE_FAR_EXTENT_PROPERTY
	*/
	public void setBorderExtent(final Extent newBorderExtent);
	
	/**Returns the border style of the indicated border.
	@param border The border for which a border style should be returned.
	@return The border style of the given border.
	*/
	public LineStyle getBorderStyle(final Border border);

	/**Returns the border style of the line near page near border.
	@return The border style of the given border.
	*/
	public LineStyle BorderLineNearStyle();

	/**Returns the border style of the line far page near border.
	@return The border style of the given border.
	*/
	public LineStyle BorderLineFarStyle();

	/**Returns the border style of the line near page far border.
	@return The border style of the given border.
	*/
	public LineStyle BorderPageNearStyle();

	/**Returns the border style of the line far page far border.
	@return The border style of the given border.
	*/
	public LineStyle BorderPageFarStyle();

	/**Sets the border style of a given border.
	The border style of each border represents a bound property.
	@param border The border for which the border style should be set.
	@param newBorderStyle The border style.
	@exception NullPointerException if the given border and/or border style is <code>null</code>. 
	@see #BORDER_LINE_NEAR_STYLE_PROPERTY
	@see #BORDER_LINE_FAR_STYLE_PROPERTY
	@see #BORDER_PAGE_NEAR_STYLE_PROPERTY
	@see #BORDER_PAGE_FAR_STYLE_PROPERTY
	*/
	public void setBorderStyle(final Border border, final LineStyle newBorderStyle);

	/**Sets the border style of the line near border.
	This is a bound property.
	@param newBorderStyle The border style.
	@exception NullPointerException if the given border style is <code>null</code>.
	@see #BORDER_LINE_NEAR_STYLE_PROPERTY
	*/
	public void setBorderLineNearStyle(final LineStyle newBorderStyle);

	/**Sets the border style of the line far border.
	This is a bound property.
	@param newBorderStyle The border style.
	@exception NullPointerException if the given border style is <code>null</code>.
	@see #BORDER_LINE_FAR_STYLE_PROPERTY
	*/
	public void setBorderLineFarStyle(final LineStyle newBorderStyle);

	/**Sets the border style of the page near border.
	This is a bound property.
	@param newBorderStyle The border style.
	@exception NullPointerException if the given border style is <code>null</code>.
	@see #BORDER_PAGE_NEAR_STYLE_PROPERTY
	*/
	public void setBorderPageNearStyle(final LineStyle newBorderStyle);

	/**Sets the border style of the page far border.
	This is a bound property.
	@param newBorderStyle The border style.
	@exception NullPointerException if the given border style is <code>null</code>.
	@see #BORDER_PAGE_FAR_STYLE_PROPERTY
	*/
	public void setBorderPageFarStyle(final LineStyle newBorderStyle);

	/**Sets the border style of all borders.
	The border style of each border represents a bound property.
	This is a convenience method that calls {@link #setBorderStyle(Border, LineStyle)} for each border.
	@param newBorderStyle The border style.
	@exception NullPointerException if the given border style is <code>null</code>.
	@see #BORDER_LINE_NEAR_STYLE_PROPERTY
	@see #BORDER_LINE_FAR_STYLE_PROPERTY
	@see #BORDER_PAGE_NEAR_STYLE_PROPERTY
	@see #BORDER_PAGE_FAR_STYLE_PROPERTY
	*/
	public void setBorderStyle(final LineStyle newBorderStyle);

	/**@return The foreground color of the component, or <code>null</code> if no foreground color is specified for this component.*/
	public Color getColor();

	/**Sets the foreground color of the component.
	This is a bound property.
	@param newColor The foreground color of the component, or <code>null</code> if the default foreground color should be used.
	@see #COLOR_PROPERTY 
	*/
	public void setColor(final Color newColor);

	/**Returns the arc size for the indicated corner.
	@param corner The corner for which an arc size should be returned.
	@return The dimensions indicating the two radiuses of the given corner arc, or dimensions of zero if the corner should not be rounded.
	*/
	public Dimensions getCornerArcSize(final Corner corner);

	/**Returns the arc size for the line near page near corner.
	@return The dimensions indicating the two radiuses of the corner arc, or dimensions of zero if the corner should not be rounded.
	*/
	public Dimensions getCornerLineNearPageNearArcSize();

	/**Returns the arc size for the line far page near corner.
	@return The dimensions indicating the two radiuses of the corner arc, or dimensions of zero if the corner should not be rounded.
	*/
	public Dimensions getCornerLineFarPageNearArcSize();

	/**Returns the arc size for the line near page far corner.
	@return The dimensions indicating the two radiuses of the corner arc, or dimensions of zero if the corner should not be rounded.
	*/
	public Dimensions getCornerLineNearPageFarArcSize();

	/**Returns the arc size for the line far page far corner.
	@return The dimensions indicating the two radiuses of the corner arc, or dimensions of zero if the corner should not be rounded.
	*/
	public Dimensions getCornerLineFarPageFarArcSize();

	/**Sets the arc size of a given corner.
	The radius of each corner represents a bound property.
	@param corner The corner for which the arc size should be set.
	@param newCornerArcSize The dimensions indicating the two radiuses of the corner, or dimensions of zero if the corner should not be rounded.
	@exception NullPointerException if the given corner and/or arc size is <code>null</code>. 
	@see #CORNER_LINE_NEAR_PAGE_NEAR_ARC_SIZE_PROPERTY
	@see #CORNER_LINE_FAR_PAGE_NEAR_ARC_SIZE_PROPERTY
	@see #CORNER_LINE_NEAR_PAGE_FAR_ARC_SIZE_PROPERTY
	@see #CORNER_LINE_FAR_PAGE_FAR_ARC_SIZE_PROPERTY
	*/
	public void setCornerArcSize(final Corner corner, final Dimensions newCornerArcSize);

	/**Sets the arc size of the line near page near corner.
	This is a bound property.
	@param newCornerArcSize The dimensions indicating the two radiuses of the corner, or dimensions of zero if the corner should not be rounded.
	@exception NullPointerException if the given size is <code>null</code>. 
	@see #CORNER_LINE_NEAR_PAGE_NEAR_ARC_SIZE_PROPERTY
	*/
	public void setCornerLineNearPageNearArcSize(final Dimensions newCornerArcSize);

	/**Sets the arc size of the line far page near corner.
	This is a bound property.
	@param newCornerArcSize The dimensions indicating the two radiuses of the corner, or dimensions of zero if the corner should not be rounded.
	@exception NullPointerException if the given size is <code>null</code>. 
	@see #CORNER_LINE_FAR_PAGE_NEAR_ARC_SIZE_PROPERTY
	*/
	public void setCornerLineFarPageNearArcSize(final Dimensions newCornerArcSize);

	/**Sets the arc size of the line near page far corner.
	This is a bound property.
	@param newCornerArcSize The dimensions indicating the two radiuses of the corner, or dimensions of zero if the corner should not be rounded.
	@exception NullPointerException if the given size is <code>null</code>. 
	@see #CORNER_LINE_NEAR_PAGE_FAR_ARC_SIZE_PROPERTY
	*/
	public void setCornerLineNearPageFarArcSize(final Dimensions newCornerArcSize);

	/**Sets the arc size of the line far page far corner.
	This is a bound property.
	@param newCornerArcSize The dimensions indicating the two radiuses of the corner, or dimensions of zero if the corner should not be rounded.
	@exception NullPointerException if the given size is <code>null</code>. 
	@see #CORNER_LINE_FAR_PAGE_FAR_ARC_SIZE_PROPERTY
	*/
	public void setCornerLineFarPageFarArcSize(final Dimensions newCornerArcSize);

	/**Sets the arc size of all corners.
	The radius of each corner represents a bound property.
	This is a convenience method that calls {@link #setCornerArcSize(Corner, Dimensions)} for each corner.
	@param newCornerArcSize The dimensions indicating the two radiuses of the corners, or dimensions of zero if the corners should not be rounded.
	@exception NullPointerException if the given arc size is <code>null</code>. 
	@see #CORNER_LINE_NEAR_PAGE_NEAR_ARC_SIZE_PROPERTY
	@see #CORNER_LINE_FAR_PAGE_NEAR_ARC_SIZE_PROPERTY
	@see #CORNER_LINE_NEAR_PAGE_FAR_ARC_SIZE_PROPERTY
	@see #CORNER_LINE_FAR_PAGE_FAR_ARC_SIZE_PROPERTY
	*/
	public void setCornerArcSize(final Dimensions newCornerArcSize);

	/**@return The cursor URI, which may be a resource URI.*/
	public URI getCursor();

	/**Sets the URI of the cursor.
	This is a bound property.
	@param newCursor The new URI of the cursor, which may be a resource URI.
	@exception NullPointerException if the given cursor URI is <code>null</code>.
	@see #CURSOR_PROPERTY
	*/
	public void setCursor(final URI newCursor);

	/**Returns the extent of the indicated flow.
	@param flow The flow for which an extent should be returned.
	@return The extent of the given flow.
	*/
	public Extent getExtent(final Flow flow);

	/**Returns the extent of the line flow.
	In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>width</dfn>.
	@return The extent of the flow, or <code>null</code> if no preferred extent has been specified
	*/
	public Extent getLineExtent();

	/**Returns the extent of the page flow.
	In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>height</dfn>.
	@return The extent of the flow, or <code>null</code> if no preferred extent has been specified
	*/
	public Extent getPageExtent();

	/**Sets the extent of a given flow.
	The extent of each flow represents a bound property.
	@param flow The flow for which the extent should be set.
	@param newExtent The new requested extent of the component, or <code>null</code> there is no extent preference.
	@exception NullPointerException if the given flow is <code>null</code>. 
	@see #LINE_EXTENT_PROPERTY
	@see #PAGE_EXTENT_PROPERTY
	*/
	public void setExtent(final Flow flow, final Extent newExtent);

	/**Sets the extent of the line flow.
	In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>width</dfn>.
	This is a bound property.
	@param newExtent The new requested extent of the component, or <code>null</code> there is no extent preference.
	@see #LINE_EXTENT_PROPERTY
	*/
	public void setLineExtent(final Extent newExtent);

	/**Sets the extent of the page flow.
	In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>height</dfn>.
	This is a bound property.
	@param newExtent The new requested extent of the component, or <code>null</code> there is no extent preference.
	@see #PAGE_EXTENT_PROPERTY
	*/
	public void setPageExtent(final Extent newExtent);

	/**@return The prioritized list of font family names, or <code>null</code> if no font family names have been specified.*/
	public List<String> getFontFamilies();

	/**Sets the font families of the component.
	This is a bound property.
	@param newFontFamilies The new prioritized list of font family names, or <code>null</code> if no font family names are specified.
	@see #FONT_FAMILIES_PROPERTY 
	*/
	public void setFontFamilies(final List<String> newFontFamilies);

	/**@return The size of the font from baseline to baseline, or <code>null</code> if no font size has been specified.*/
	public Extent getFontSize();

	/**Sets the font size of the component.
	This is a bound property.
	@param newFontSize The new size of the font from baseline to baseline, or <code>null</code> there is no font size specified.
	@see #FONT_SIZE_PROPERTY 
	*/
	public void setFontSize(final Extent newFontSize);

	/**@return The style of the font.*/
	public FontStyle getFontStyle();

	/**Sets the style of the font.
	This is a bound property.
	@param newFontStyle The style of the font.
	@exception NullPointerException if the given font style is <code>null</code>.
	@see #FONT_STYLE_PROPERTY
	*/
	public void setFontStyle(final FontStyle newFontStyle);

	/**@return The weight of a font relative to a normal value of 0.5.*/
	public double getFontWeight();

	/**Sets the weight of the font.
	The weight of a font relative to a normal value of 0.5.
	A font weight of 0.75 is equivalent to a bold font.
	It is recommended that the constant variables {@link #FONT_WEIGHT_NORMAL} and {@link #FONT_WEIGHT_BOLD} be used for the most compatibility across platforms.
	This is a bound property of type {@link Double}.
	@param newFontWeight The weight of a font relative to a normal value of 0.5.
	@see #FONT_WEIGHT_PROPERTY
	@see #FONT_WEIGHT_NORMAL
	@see #FONT_WEIGHT_BOLD
	*/
	public void setFontWeight(final double newFontWeight);

	/**@return The prioritized list of label font family names, or <code>null</code> if no label font family names have been specified.*/
	public List<String> getLabelFontFamilies();

	/**Sets the font families of the label.
	This is a bound property.
	@param newLabelFontFamilies The new prioritized list of label font family names, or <code>null</code> if no label font family names are specified.
	@see #LABEL_FONT_FAMILIES_PROPERTY 
	*/
	public void setLabelFontFamilies(final List<String> newLabelFontFamilies);

	/**@return The size of the label font from baseline to baseline, or <code>null</code> if no label font size has been specified.*/
	public Extent getLabelFontSize();

	/**Sets the label font size of the component.
	This is a bound property.
	@param newLabelFontSize The new size of the label font from baseline to baseline, or <code>null</code> there is no label font size specified.
	@see #LABEL_FONT_SIZE_PROPERTY 
	*/
	public void setLabelFontSize(final Extent newLabelFontSize);

	/**@return The style of the label font.*/
	public FontStyle getLabelFontStyle();

	/**Sets the style of the label font.
	This is a bound property.
	@param newLabelFontStyle The style of the label font.
	@exception NullPointerException if the given label font style is <code>null</code>.
	@see #LABEL_FONT_STYLE_PROPERTY
	*/
	public void setLabelFontStyle(final FontStyle newLabelFontStyle);

	/**@return The weight of the label font relative to a normal value of 0.5.*/
	public double getLabelFontWeight();

	/**Sets the weight of the label font.
	The weight of the label font relative to a normal value of 0.5.
	A font weight of 0.75 is equivalent to a bold font.
	It is recommended that the constant variables {@link #FONT_WEIGHT_NORMAL} and {@link #FONT_WEIGHT_BOLD} be used for the most compatibility across platforms.
	This is a bound property of type {@link Double}.
	@param newLabelFontWeight The weight of the label font relative to a normal value of 0.5.
	@see #FONT_WEIGHT_PROPERTY
	@see #FONT_WEIGHT_NORMAL
	@see #FONT_WEIGHT_BOLD
	*/
	public void setLabelFontWeight(final double newLabelFontWeight);

	/**Returns the margin extent of the indicated border.
	@param border The border for which a margin extent should be returned.
	@return The margin extent of the given border.
	*/
	public Extent getMarginExtent(final Border border);

	/**Returns the margin extent of the line near page near border.
	@return The margin extent of the given border.
	*/
	public Extent getMarginLineNearExtent();

	/**Returns the margin extent of the line far page near border.
	@return The margin extent of the given border.
	*/
	public Extent getMarginLineFarExtent();

	/**Returns the margin extent of the line near page far border.
	@return The margin extent of the given border.
	*/
	public Extent getMarginPageNearExtent();

	/**Returns the margin extent of the line far page far border.
	@return The margin extent of the given border.
	*/
	public Extent getMarginPageFarExtent();

	/**Sets the margin extent of a given border.
	The margin extent of each border represents a bound property.
	@param border The border for which the margin extent should be set.
	@param newMarginExtent The margin extent.
	@exception NullPointerException if the given border and/or margin extent is <code>null</code>. 
	@see #MARGIN_LINE_NEAR_EXTENT_PROPERTY
	@see #MARGIN_LINE_FAR_EXTENT_PROPERTY
	@see #MARGIN_PAGE_NEAR_EXTENT_PROPERTY
	@see #MARGIN_PAGE_FAR_EXTENT_PROPERTY
	*/
	public void setMarginExtent(final Border border, final Extent newMarginExtent);

	/**Sets the margin extent of the line near border.
	This is a bound property.
	@param newMarginExtent The margin extent.
	@exception NullPointerException if the given margin extent is <code>null</code>. 
	@see #MARGIN_LINE_NEAR_EXTENT_PROPERTY
	*/
	public void setMarginLineNearExtent(final Extent newMarginExtent);

	/**Sets the margin extent of the line far border.
	This is a bound property.
	@param newMarginExtent The margin extent, or <code>null</code> if the default margin extent should be used.
	@exception NullPointerException if the given margin extent is <code>null</code>. 
	@see #MARGIN_LINE_FAR_EXTENT_PROPERTY
	*/
	public void setMarginLineFarExtent(final Extent newMarginExtent);

	/**Sets the margin extent of the page near border.
	This is a bound property.
	@param newMarginExtent The margin extent, or <code>null</code> if the default margin extent should be used.
	@exception NullPointerException if the given margin extent is <code>null</code>. 
	@see #MARGIN_PAGE_NEAR_EXTENT_PROPERTY
	*/
	public void setMarginPageNearExtent(final Extent newMarginExtent);

	/**Sets the margin extent of the page far border.
	This is a bound property.
	@param newMarginExtent The margin extent, or <code>null</code> if the default margin extent should be used.
	@exception NullPointerException if the given margin extent is <code>null</code>. 
	@see #MARGIN_PAGE_FAR_EXTENT_PROPERTY
	*/
	public void setMarginPageFarExtent(final Extent newMarginExtent);

	/**Sets the margin extent of all borders.
	The margin extent of each border represents a bound property.
	This is a convenience method that calls {@link #setMarginExtent(Border, Extent)} for each border.
	@param newMarginExtent The margin extent.
	@exception NullPointerException if the given margin extent is <code>null</code>. 
	@see #MARGIN_LINE_NEAR_EXTENT_PROPERTY
	@see #MARGIN_LINE_FAR_EXTENT_PROPERTY
	@see #MARGIN_PAGE_NEAR_EXTENT_PROPERTY
	@see #MARGIN_PAGE_FAR_EXTENT_PROPERTY
	*/
	public void setMarginExtent(final Extent newMarginExtent);

	/**@return The opacity of the entire component in the range (0.0-1.0), with a default of 1.0.*/
	public double getOpacity();

	/**Sets the opacity of the entire component.
	This is a bound property of type {@link Double}.
	@param newOpacity The new opacity of the entire component in the range (0.0-1.0).
	@exception IllegalArgumentException if the given opacity is not within the range (0.0-1.0).
	@see #OPACITY_PROPERTY 
	*/
	public void setOpacity(final double newOpacity);

	/**Returns the padding extent of the indicated border.
	@param border The border for which a padding extent should be returned.
	@return The padding extent of the given border.
	*/
	public Extent getPaddingExtent(final Border border);

	/**Returns the padding extent of the line near page near border.
	@return The padding extent of the given border.
	*/
	public Extent getPaddingLineNearExtent();

	/**Returns the padding extent of the line far page near border.
	@return The padding extent of the given border.
	*/
	public Extent getPaddingLineFarExtent();

	/**Returns the padding extent of the line near page far border.
	@return The padding extent of the given border.
	*/
	public Extent getPaddingPageNearExtent();

	/**Returns the padding extent of the line far page far border.
	@return The padding extent of the given border.
	*/
	public Extent getPaddingPageFarExtent();

	/**Sets the padding extent of a given border.
	The padding extent of each border represents a bound property.
	@param border The border for which the padding extent should be set.
	@param newPaddingExtent The padding extent.
	@exception NullPointerException if the given border and/or padding extent is <code>null</code>. 
	@see #PADDING_LINE_NEAR_EXTENT_PROPERTY
	@see #PADDING_LINE_FAR_EXTENT_PROPERTY
	@see #PADDING_PAGE_NEAR_EXTENT_PROPERTY
	@see #PADDING_PAGE_FAR_EXTENT_PROPERTY
	*/
	public void setPaddingExtent(final Border border, final Extent newPaddingExtent);

	/**Sets the padding extent of the line near border.
	This is a bound property.
	@param newPaddingExtent The padding extent.
	@exception NullPointerException if the given padding extent is <code>null</code>. 
	@see #PADDING_LINE_NEAR_EXTENT_PROPERTY
	*/
	public void setPaddingLineNearExtent(final Extent newPaddingExtent);

	/**Sets the padding extent of the line far border.
	This is a bound property.
	@param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	@exception NullPointerException if the given padding extent is <code>null</code>. 
	@see #PADDING_LINE_FAR_EXTENT_PROPERTY
	*/
	public void setPaddingLineFarExtent(final Extent newPaddingExtent);

	/**Sets the padding extent of the page near border.
	This is a bound property.
	@param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	@exception NullPointerException if the given padding extent is <code>null</code>. 
	@see #PADDING_PAGE_NEAR_EXTENT_PROPERTY
	*/
	public void setPaddingPageNearExtent(final Extent newPaddingExtent);

	/**Sets the padding extent of the page far border.
	This is a bound property.
	@param newPaddingExtent The padding extent, or <code>null</code> if the default padding extent should be used.
	@exception NullPointerException if the given padding extent is <code>null</code>. 
	@see #PADDING_PAGE_FAR_EXTENT_PROPERTY
	*/
	public void setPaddingPageFarExtent(final Extent newPaddingExtent);

	/**Sets the padding extent of all borders.
	The padding extent of each border represents a bound property.
	This is a convenience method that calls {@link #setPaddingExtent(Border, Extent)} for each border.
	@param newPaddingExtent The padding extent.
	@exception NullPointerException if the given padding extent is <code>null</code>. 
	@see #PADDING_LINE_NEAR_EXTENT_PROPERTY
	@see #PADDING_LINE_FAR_EXTENT_PROPERTY
	@see #PADDING_PAGE_NEAR_EXTENT_PROPERTY
	@see #PADDING_PAGE_FAR_EXTENT_PROPERTY
	*/
	public void setPaddingExtent(final Extent newPaddingExtent);

	/**@return The Guise session that owns this component.*/
	public GuiseSession getSession();

	/**@return The style identifier, or <code>null</code> if there is no style ID.*/
	public String getStyleID();

	/**Identifies the style for the component.
	This is a bound property.
	@param newStyleID The style identifier, or <code>null</code> if there is no style ID.
	@see #STYLE_ID_PROPERTY
	*/
	public void setStyleID(final String newStyleID);

	/**@return Whether the component is visible.
	@see #isDisplayed()
	*/
	public boolean isVisible();

	/**Sets whether the component is visible.
	This is a bound property of type {@link Boolean}.
	@param newVisible <code>true</code> if the component should be visible, else <code>false</code>.
	@see #VISIBLE_PROPERTY
	@see #setDisplayed(boolean)
	*/
	public void setVisible(final boolean newVisible);

	/**@return Whether the component is displayed or has no representation, taking up no space.
	@see #isVisible()
	*/
	public boolean isDisplayed();

	/**Sets whether the component is displayed or has no representation, taking up no space.
	This is a bound property of type {@link Boolean}.
	@param newDisplayed <code>true</code> if the component should be displayed, else <code>false</code> if the component should take up no space.
	@see #DISPLAYED_PROPERTY
	@see #setVisible(boolean)
	*/
	public void setDisplayed(final boolean newDisplayed);

	/**@return Whether tooltips are enabled for this component.*/
	public boolean isTooltipEnabled();

	/**Sets whether tooltips are enabled for this component.
	Tooltips contain information from the component model's "info" property.
	This is a bound property of type {@link Boolean}.
	@param newTooltipEnabled <code>true</code> if the component should display tooltips, else <code>false</code>.
	@see Model#getInfo()
	@see #TOOLTIP_ENABLED_PROPERTY
	*/
	public void setTooltipEnabled(final boolean newTooltipEnabled);

}
