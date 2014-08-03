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
import java.util.HashMap;
import java.util.Map;

import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.geometry.*;
import com.guiseframework.model.*;
import com.guiseframework.model.ui.PresentationModel;
import com.guiseframework.platform.DepictContext;
import com.guiseframework.platform.XHTMLDepictContext;

import static com.globalmentor.java.Enums.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**
 * Strategy for rendering a table component as an XHTML <code>&lt;table&gt;</code> element.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebTableDepictor<C extends Table> extends AbstractWebComponentDepictor<C> {

	/** Default constructor using the XHTML <code>&lt;table&gt;</code> element. */
	public WebTableDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_TABLE); //represent <xhtml:table>
	}

	/**
	 * Retrieves the styles for the outer element of the component. This version combines the body styles with the outer styles.
	 * @return The styles for the outer element of the component, mapped to CSS property names.
	 * @see AbstractWebComponentDepictor#getBodyStyles()
	 */
	protected Map<String, Object> getOuterStyles() {
		final Map<String, Object> outerStyles = super.getOuterStyles(); //get the default outer styles
		outerStyles.putAll(getBodyStyles()); //add the styles for the body
		return outerStyles; //return the combined styles		
	}

	/**
	 * Begins the rendering process.
	 * @throws IOException if there is an error rendering the component.
	 */
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		writeIDClassAttributes(null, null); //write the ID and class, with no prefixes or suffixes
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction
		if(hasLabelContent()) { //if there is label content
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_CAPTION); //<xhtml:caption>
			writeClassAttribute(getBaseStyleIDs(null, COMPONENT_LABEL_CLASS_SUFFIX)); //write the base style IDs with a "-label" suffix
			writeLabelContent(); //write the label content
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_CAPTION); //</xhtml:caption>
		}
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_THEAD); //<xhtml:thead>
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TR); //<xhtml:tr>
		for(final TableColumnModel<?> column : getDepictedObject().getColumns()) { //for each column
			if(column.isVisible()) { //if the column is visible
				updateHeaderView(component, column); //update the header
			}
		}
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TR); //</xhtml:tr>
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_THEAD); //</xhtml:thead>
	}

	/**
	 * Renders the body of the component.
	 * @throws IOException if there is an error rendering the component.
	 */
	protected void depictBody() throws IOException {
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TBODY); //<xhtml:tbody>		
		writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX); //write the ID and class for the body
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction
		super.depictBody(); //render the default main part of the component
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TBODY); //</xhtml:tbody>
	}

	/**
	 * Updates the views of any children.
	 * @throws IOException if there is an error updating the child views.
	 * @see DepictContext.State#UPDATE_VIEW
	 */
	protected void depictChildren() throws IOException {
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		//don't do the default updating of child views, because we control all the writing in the order we want
		final int rowCount = component.getRowCount(); //find out the number of rows
		final int displayRowStartIndex = component.getDisplayRowStartIndex(); //see which row to start with
		final int displayRowCount = component.getDisplayRowCount(); //see how many rows to show
		final int displayRowEndIndex = displayRowCount >= 0 ? Math.min(displayRowStartIndex + displayRowCount, rowCount) : rowCount; //if the display row count is restricted, take that into account when calculating the ending index, but don't go past the last row
		for(int rowIndex = displayRowStartIndex; rowIndex < displayRowEndIndex; ++rowIndex) { //for each row index
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TR); //<xhtml:tr>
			for(final TableColumnModel<?> column : component.getColumns()) { //for each column
				if(column.isVisible()) { //if the column is visible TODO shouldn't we update this column anyway? TODO move visibility to table
					updateCellView(component, rowIndex, column); //update the view for this cell
				}
			}
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TR); //</xhtml:tr>
		}
	}

	/**
	 * Updates the view of a column header.
	 * @param <T> The type of value contained in the cells of the column.
	 * @param tableModel The component model.
	 * @param column The cell column.
	 * @throws IOException if there is an error updating the cell view.
	 */
	protected <T> void updateHeaderView(final TableModel tableModel, final TableColumnModel<T> column) throws IOException {
		if(hasLabelContent(column)) { //if there is label content
			final WebDepictContext depictContext = getDepictContext(); //get the depict context
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TH); //<xhtml:th>
			final String styleID = column.getStyleID(); //get the column style ID
			if(styleID != null) { //if this column has a style
				depictContext.writeAttribute(null, ATTRIBUTE_CLASS, styleID); //write the style class attribute
			}
			writeLabelContent(column, getDepictedObject().getColumnUIModel(column)); //write the column label content
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TH); //</xhtml:th>
		}
	}

	/**
	 * Retrieves the styles for the label of the component. This method will be used both for the table caption and for each column header, distinguished by the
	 * UI model passed. If a column is being rendered, this version adds border styles.
	 * @param labelModel The label model containing the label content.
	 * @param uiModel The model containing the label style information.
	 * @return The styles for the label of the component, mapped to CSS property names.
	 */
	protected Map<String, Object> getLabelStyles(final LabelModel labelModel, final PresentationModel uiModel) {
		final Map<String, Object> labelStyles = super.getLabelStyles(labelModel, uiModel); //get the default label styles
		final WebPlatform platform = getPlatform(); //get the platform
		final C component = getDepictedObject(); //get the component
		if(uiModel == component) { //if the table's label is being rendered
			//TODO fix
		} else { //if a column's label is being rendered
			final Orientation orientation = component.getComponentOrientation(); //get this component's orientation
			for(final Border border : Border.values()) { //for each logical border
				final Side side = orientation.getSide(border); //get the absolute side on which this border lies
				/*TODO fix
							final Extent borderExtent=component.getBorderExtent(border);	//get the border extent for this border
							if(!borderExtent.isEmpty()) {	//if there is a border on this side (to save bandwidth, only include border properties if there is a border; the stylesheet defaults to no border)
								styles.put(CSS_PROPERTY_BORDER_X_WIDTH_TEMPLATE.apply(getSerializationName(side)), borderExtent);	//set the border extent
								styles.put(CSS_PROPERTY_BORDER_X_STYLE_TEMPLATE.apply(getSerializationName(side)), component.getBorderStyle(border));	//indicate the border style for this side
								final Color<?> borderColor=component.getBorderColor(border);	//get the border color for this border
								if(borderColor!=null) {	//if a border color is specified
									styles.put(CSS_PROPERTY_BORDER_X_COLOR_TEMPLATE.apply(getSerializationName(side)), borderColor);	//set the border color
								}
							}
							final Extent marginExtent=component.getMarginExtent(border);	//get the margin extent for this border
							if(!marginExtent.isEmpty()) {	//if a non-zero margin extent is specified (the stylesheet specifies a zero default margin)
								styles.put(CSS_PROPERTY_MARGIN_X_TEMPLATE.apply(getSerializationName(side)), marginExtent);	//set the margin extent
							}
				*/
				final Extent paddingExtent = uiModel.getPaddingExtent(border); //get the padding extent for this border of the current column
				if(!paddingExtent.isEmpty()) { //if a non-zero padding extent is specified (the stylesheet specifies a zero default padding)
					labelStyles.put(XHTMLDepictContext.CSS_PROPERTY_PADDING_X_TEMPLATE.apply(getSerializationName(side)), paddingExtent); //set the padding extent
				}
			}
			/*TODO fix
					final List<String> fontFamilies=component.getFontFamilies();	//get the component's font prioritized list of font families
					if(fontFamilies!=null) {	//if this component has specified font families 
						styles.put(CSS_PROP_FONT_FAMILY, fontFamilies);	//indicate the font families
					}
					final Extent fontSize=component.getFontSize();	//get the component's font size
					if(fontSize!=null) {	//if this component has a font size 
						styles.put(CSS_PROP_FONT_SIZE, fontSize);	//indicate the font size
					}
					final Extent width=orientation.getAxis(Flow.LINE)==Axis.X ? component.getLineExtent() : component.getPageExtent();	//get the component's requested width
					if(width!=null) {	//if this component has a requested width 
						styles.put(CSS_PROP_WIDTH, width);	//indicate the width
					}
					final Extent height=orientation.getAxis(Flow.PAGE)==Axis.Y ? component.getPageExtent() : component.getLineExtent();	//get the component's requested width
					if(height!=null) {	//if this component has a requested height 
						styles.put(CSS_PROP_HEIGHT, height);	//indicate the height
					}
			*/
		}
		return labelStyles;
	}

	/**
	 * Updates the view of a column.
	 * @param <T> The type of value contained in the cells of the column.
	 * @param tableModel The component model.
	 * @param rowIndex The zero-based cell row index.
	 * @param column The cell column.
	 * @throws IOException if there is an error updating the cell view.
	 */
	protected <T> void updateCellView(final TableModel tableModel, final int rowIndex, final TableColumnModel<T> column) throws IOException {
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TD); //<xhtml:td>
		final String styleID = column.getStyleID(); //get the column style ID
		if(styleID != null) { //if this column has a style
			depictContext.writeAttribute(null, ATTRIBUTE_CLASS, styleID); //write the style class attribute
		}
		final Map<String, Object> styles = new HashMap<String, Object>(); //create a new map of styles
		final Orientation orientation = component.getComponentOrientation(); //get this component's orientation
		for(final Border border : Border.values()) { //for each logical border
			final Side side = orientation.getSide(border); //get the absolute side on which this border lies
			/*TODO fix
						final Extent borderExtent=component.getBorderExtent(border);	//get the border extent for this border
						if(!borderExtent.isEmpty()) {	//if there is a border on this side (to save bandwidth, only include border properties if there is a border; the stylesheet defaults to no border)
							styles.put(CSS_PROPERTY_BORDER_X_WIDTH_TEMPLATE.apply(getSerializationName(side)), borderExtent);	//set the border extent
							styles.put(CSS_PROPERTY_BORDER_X_STYLE_TEMPLATE.apply(getSerializationName(side)), component.getBorderStyle(border));	//indicate the border style for this side
							final Color<?> borderColor=component.getBorderColor(border);	//get the border color for this border
							if(borderColor!=null) {	//if a border color is specified
								styles.put(CSS_PROPERTY_BORDER_X_COLOR_TEMPLATE.apply(getSerializationName(side)), borderColor);	//set the border color
							}
						}
						final Extent marginExtent=component.getMarginExtent(border);	//get the margin extent for this border
						if(!marginExtent.isEmpty()) {	//if a non-zero margin extent is specified (the stylesheet specifies a zero default margin)
							styles.put(CSS_PROPERTY_MARGIN_X_TEMPLATE.apply(getSerializationName(side)), marginExtent);	//set the margin extent
						}
			*/
			final Extent paddingExtent = component.getColumnPaddingExtent(column, border); //get the padding extent for this border of the current column
			if(!paddingExtent.isEmpty()) { //if a non-zero padding extent is specified (the stylesheet specifies a zero default padding)
				styles.put(XHTMLDepictContext.CSS_PROPERTY_PADDING_X_TEMPLATE.apply(getSerializationName(side)), paddingExtent); //set the padding extent
			}
		}
		/*TODO fix
				final List<String> fontFamilies=component.getFontFamilies();	//get the component's font prioritized list of font families
				if(fontFamilies!=null) {	//if this component has specified font families 
					styles.put(CSS_PROP_FONT_FAMILY, fontFamilies);	//indicate the font families
				}
				final Extent fontSize=component.getFontSize();	//get the component's font size
				if(fontSize!=null) {	//if this component has a font size 
					styles.put(CSS_PROP_FONT_SIZE, fontSize);	//indicate the font size
				}
				final Extent width=orientation.getAxis(Flow.LINE)==Axis.X ? component.getLineExtent() : component.getPageExtent();	//get the component's requested width
				if(width!=null) {	//if this component has a requested width 
					styles.put(CSS_PROP_WIDTH, width);	//indicate the width
				}
				final Extent height=orientation.getAxis(Flow.PAGE)==Axis.Y ? component.getPageExtent() : component.getLineExtent();	//get the component's requested width
				if(height!=null) {	//if this component has a requested height 
					styles.put(CSS_PROP_HEIGHT, height);	//indicate the height
				}
		*/
		writeStyleAttribute(styles); //write the cell styles
		final Component cellComponent = component.getComponent(new TableModel.Cell<T>(rowIndex, column)); //get the component information for this cell
		cellComponent.depict(); //update the component's view
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TD); //</xhtml:td>
	}

	/**
	 * Ends the rendering process.
	 * @throws IOException if there is an error rendering the component.
	 */
	protected void depictEnd() throws IOException {
		super.depictEnd(); //do the default ending rendering
	}

}
