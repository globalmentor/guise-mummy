/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
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
import java.net.URI;
import java.util.*;

import static com.globalmentor.java.Arrays.*;
import static com.globalmentor.java.Enums.*;
import static com.globalmentor.java.Numbers.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;
import static com.globalmentor.w3c.spec.CSS.*;

import com.globalmentor.collections.iterators.ReverseIterator;
import com.globalmentor.w3c.spec.CSS;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.geometry.Axis;
import com.guiseframework.geometry.Extent;
import com.guiseframework.geometry.Side;
import com.guiseframework.platform.XHTMLDepictContext;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**
 * The abstract base class for all <code>application/xhtml+xml</code> composite components that use layouts.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public abstract class AbstractWebLayoutComponentDepictor<C extends LayoutComponent> extends AbstractWebComponentDepictor<C> {

	/** The style classes for each region in a row. */
	protected static final String ROW_REGION_CLASSES[] = new String[] { LAYOUT_REGION_LEFT_CLASS, LAYOUT_REGION_CENTER_CLASS, LAYOUT_REGION_RIGHT_CLASS };

	/** The style classes for each region in a column. */
	protected static final String COLUMN_REGION_CLASSES[] = new String[] { LAYOUT_REGION_TOP_CLASS, LAYOUT_REGION_CENTER_CLASS, LAYOUT_REGION_BOTTOM_CLASS };

	/** Default constructor with no element representation. */
	public AbstractWebLayoutComponentDepictor() {
		this(null, null); //construct the strategy with no element representation
	}

	/**
	 * Element namespace and local name constructor that doesn't create an empty element, even if there is no content.
	 * @param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	 */
	public AbstractWebLayoutComponentDepictor(final URI namespaceURI, final String localName) {
		this(namespaceURI, localName, false); //don't allow an empty element
	}

	/**
	 * Element namespace and local name constructor.
	 * @param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	 * @param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	 * @param isEmptyElementAllowed Whether an empty element can be created if there is no content.
	 */
	public AbstractWebLayoutComponentDepictor(final URI namespaceURI, final String localName, final boolean isEmptyElementAllowed) {
		super(namespaceURI, localName, isEmptyElementAllowed); //construct the parent class
	}

	/**
	 * {@inheritDoc} This method does appropriate layout based upon the container's layout definition.
	 * @see Container#getLayout()
	 */
	@Override
	protected void depictChildren() throws IOException {
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		final Orientation orientation = component.getComponentOrientation(); //get the component's orientation
		final Layout<?> layout = component.getLayout(); //get the layout of this component
		if(layout instanceof AbstractFlowLayout) { //if this is a flow layout
			final AbstractFlowLayout<?> flowLayout = (AbstractFlowLayout<?>)layout; //cast the layout to a flow layout
			final Flow flow = flowLayout.getFlow(); //get the flow
			final Axis flowAxis = orientation.getAxis(flow); //get the axis of the flow
			final Flow.Direction flowDirection = orientation.getDirection(flow); //get the direction of the flow
			final Flow alignFlow = flow == Flow.LINE ? Flow.PAGE : Flow.LINE; //the align flow is perpendicular to the layout flow
			final Axis alignFlowAxis = orientation.getAxis(alignFlow); //get the axis of the align flow
			final Flow.Direction alignFlowDirection = orientation.getDirection(alignFlow); //get the direction of the align flow
			final boolean wrapped = flowLayout.isWrapped(); //see if the flow should be wrapped
			final Iterator<Component> childComponentIterator; //we'll get an iterator to child components in the correct direction, based upon the component's orientation, flow, axis, etc.
			final Map<String, Object> styles = new HashMap<String, Object>(); //create a new map of styles; we'll re-use this map, clearing it each time
			int childIndex = -1; //keep track of which child we're writing; we're not writing any child, yet
			depictContext.write("\n"); //format the output
			depictContext.writeIndent(); //write an indentation
			if(wrapped) {
				if(flowAxis == Axis.Y) { //if wrapping is specified on the Y axis
					throw new IllegalStateException("Flow layout wrapping not supported on the Y axis.");
				}
				childComponentIterator = component.getChildComponents().iterator(); //get an iterator to child components in logical order TODO i18n fix flow direction
				depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div>
				writeIDAttribute(null, COMPONENT_LAYOUT_CLASS_SUFFIX); //id="id-layout"
				depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_FLOW_X_CLASS); //class="layout-flow-x"
				while(childComponentIterator.hasNext()) { //for each visible child component in the container, wrap the component in a span with the correct style
					final Component childComponent = childComponentIterator.next(); //get the next child component
					++childIndex; //update the child index
					depictContext.write("\n"); //format the output
					depictContext.writeIndent(); //write an indentation
					depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div>
					depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_FLOW_X_CHILD_CLASS); //class="layout-flow-x-child"
					styles.clear(); //clear our map of styles
					final double alignment = flowLayout.getConstraints(childComponent).getAlignment(); //get the alignment of this component
					final String verticalAlign = getAlign(alignment, alignFlowAxis, alignFlowDirection); //determine the valign string from the alignment value
					styles.put(CSS_PROP_VERTICAL_ALIGN, verticalAlign); //align the child correctly vertically (Firefox 2.x inline boxes need *some* value here to correctly align even identically-sized children)
					final Extent gapBefore = childIndex == 0 ? flowLayout.getGapBefore() : flowLayout.getGapBetween(); //if this isn't the first component, use the between-components spacing amount
					final Extent gapAfter = childComponentIterator.hasNext() ? Extent.ZERO_EXTENT1 : flowLayout.getGapAfter(); //if this is the last component, we'll add the requested amount of space after the component as well
					if(gapBefore.getValue() != 0) { //if there is a gap before (the stylesheet should set the default to zero)
						styles.put(CSS_PROP_PADDING_LEFT, gapBefore); //insert padding before the components
					}
					if(gapAfter.getValue() != 0) { //if there is a gap after (the stylesheet should set the default to zero)
						styles.put(CSS_PROP_PADDING_RIGHT, gapAfter); //insert padding after the components
					}
					writeStyleAttribute(styles); //write the styles
					styles.put(CSS_PROP_DISPLAY, CSS_DISPLAY_INLINE_BLOCK); //set each child to act as an inline block so that it will flow
					writeStyleAttribute(styles); //write the styles
					updateFlowChildView(childComponent, flowAxis); //update this child's view
					depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div>
				}
				depictContext.write("\n"); //format the output
				depictContext.writeIndent(); //indent the ending tag
				depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div>
			} else { //if the flow layout isn't wrapped
				///*TODO fix DIV-based vertical flow
				if(flowAxis == Axis.Y) { //TODO testing new vertical flow
					depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div>
					writeIDAttribute(null, COMPONENT_LAYOUT_CLASS_SUFFIX); //id="id-layout"
					depictContext.writeAttribute(null, ATTRIBUTE_CLASS, flowAxis == Axis.X ? LAYOUT_FLOW_X_CLASS : LAYOUT_FLOW_Y_CLASS); //class="layout-flow-x/y"
					writeDirectionAttribute(orientation, flow); //explicitly write the direction ("ltr" or "rtl") for this flow so that the orientation will be taken into account
					childComponentIterator = flowDirection == Flow.Direction.INCREASING ? component.getChildComponents().iterator() : new ReverseIterator<Component>(
							component.getChildComponents()); //get an iterator to child components in the correct direction
					while(childComponentIterator.hasNext()) { //for each visible child component in the container, wrap the component in a span with the correct style
						final Component childComponent = childComponentIterator.next(); //get the next child component
						++childIndex; //update the child index
						depictContext.write("\n"); //format the output
						depictContext.writeIndent(); //write an indentation
						depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div>

						depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_FLOW_Y_CHILD_CLASS); //class="layout-flow-y-child"
						//*/
						/*TODO fix alignment
												final double alignment=flowLayout.getConstraints(childComponent).getAlignment();	//get the alignment of this component
												final String tdAlign=getAlign(alignment, alignFlowAxis, alignFlowDirection);	//determine the align string from the alignment value TODO does the direction attribute take care of this automatically?
												depictContext.writeAttribute(null, ELEMENT_TD_ATTRIBUTE_ALIGN, tdAlign);	//align="tdAlign"
						*/
						styles.clear(); //clear our map of styles
						final Extent gapBefore = childIndex == 0 ? flowLayout.getGapBefore() : flowLayout.getGapBetween(); //if this isn't the first component, use the between-components spacing amount
						final Extent gapAfter = childComponentIterator.hasNext() ? Extent.ZERO_EXTENT1 : flowLayout.getGapAfter(); //if this is the last component, we'll add the requested amount of space after the component as well
						if(gapBefore.getValue() != 0) { //if there is a gap before (the stylesheet should set the default to zero)
							styles.put(CSS_PROP_PADDING_TOP, gapBefore); //insert padding before the components
						}
						if(gapAfter.getValue() != 0) { //if there is a gap after (the stylesheet should set the default to zero)
							styles.put(CSS_PROP_PADDING_BOTTOM, gapAfter); //insert padding after the components
						}
						writeStyleAttribute(styles); //write the styles
						//				/*TODO fix DIV-based vertical flow
						updateFlowChildView(childComponent, flowAxis); //update this child's view
						depictContext.writeIndent(); //indent the ending tag
						depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div>
						depictContext.write("\n"); //format the output
					}
					depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div>
				} else {
					//*/				
					depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TABLE); //<xhtml:table>
					writeIDAttribute(null, COMPONENT_LAYOUT_CLASS_SUFFIX); //id="id-layout"
					depictContext.writeAttribute(null, ATTRIBUTE_CLASS, flowAxis == Axis.X ? LAYOUT_FLOW_X_CLASS : LAYOUT_FLOW_Y_CLASS); //class="layout-flow-x/y"
					styles.clear(); //clear our map of styles
					styles.put(CSS_PROP_BORDER_COLLAPSE, CSS_BORDER_COLLAPSE_COLLAPSE); //collapse the table cells
					writeStyleAttribute(styles); //write the table's styles
					writeDirectionAttribute(orientation, flow); //explicitly write the direction ("ltr" or "rtl") for this flow so that the orientation will be taken into account
					depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TBODY); //<xhtml:tbody> (IE will not show dynamically-allocated tables without a tbody)
					switch(flowAxis) { //see on which axis we should flow
						case X: //horizontal flow
							childComponentIterator = component.getChildComponents().iterator(); //get an iterator to child components in logical order---we compensated for orientation by setting the table direction attribute
							depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TR); //<xhtml:tr>
							depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_FLOW_X_CLASS); //class="layout-flow-x"
							while(childComponentIterator.hasNext()) { //for each visible child component in the container, wrap the component in a span with the correct style
								final Component childComponent = childComponentIterator.next(); //get the next child component
								++childIndex; //update the child index
								depictContext.write("\n"); //format the output
								depictContext.writeIndent(); //write an indentation
								depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TD); //<xhtml:td>
								depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_FLOW_X_CHILD_CLASS); //class="layout-flow-x-child"
								final double alignment = flowLayout.getConstraints(childComponent).getAlignment(); //get the alignment of this component
								final String tdVAlign = getAlign(alignment, alignFlowAxis, alignFlowDirection); //determine the valign string from the alignment value
								depictContext.writeAttribute(null, ELEMENT_TD_ATTRIBUTE_VALIGN, tdVAlign); //valign="tdVAlign"						
								styles.clear(); //clear our map of styles
								final Extent gapBefore = childIndex == 0 ? flowLayout.getGapBefore() : flowLayout.getGapBetween(); //if this isn't the first component, use the between-components spacing amount
								final Extent gapAfter = childComponentIterator.hasNext() ? Extent.ZERO_EXTENT1 : flowLayout.getGapAfter(); //if this is the last component, we'll add the requested amount of space after the component as well
								if(gapBefore.getValue() != 0) { //if there is a gap before (the stylesheet should set the default to zero)
									styles.put(CSS_PROP_PADDING_LEFT, gapBefore); //insert padding before the components
								}
								if(gapAfter.getValue() != 0) { //if there is a gap after (the stylesheet should set the default to zero)
									styles.put(CSS_PROP_PADDING_RIGHT, gapAfter); //insert padding after the components
								}
								writeStyleAttribute(styles); //write the styles							
								updateFlowChildView(childComponent, flowAxis); //update this child's view
								depictContext.writeIndent(); //indent the ending tag
								depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TD); //</xhtml:td>
							}
							depictContext.write("\n"); //format the output
							depictContext.writeIndent(); //indent the ending tag
							depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TR); //</xhtml:tr>					
							depictContext.write("\n"); //format the output
							break;
						case Y: //vertical flow
							childComponentIterator = flowDirection == Flow.Direction.INCREASING ? component.getChildComponents().iterator() : new ReverseIterator<Component>(
									component.getChildComponents()); //get an iterator to child components in the correct direction
							while(childComponentIterator.hasNext()) { //for each visible child component in the container, wrap the component in a span with the correct style
								final Component childComponent = childComponentIterator.next(); //get the next child component
								++childIndex; //update the child index
								depictContext.write("\n"); //format the output
								depictContext.writeIndent(); //write an indentation
								depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TR); //<xhtml:tr>
								depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_FLOW_Y_CLASS); //class="layout-flow-x"
								depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TD); //<xhtml:td>
								depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_FLOW_Y_CHILD_CLASS); //class="layout-flow-y-child"
								final double alignment = flowLayout.getConstraints(childComponent).getAlignment(); //get the alignment of this component
								final String tdAlign = getAlign(alignment, alignFlowAxis, alignFlowDirection); //determine the align string from the alignment value TODO does the direction attribute take care of this automatically?
								depictContext.writeAttribute(null, ELEMENT_TD_ATTRIBUTE_ALIGN, tdAlign); //align="tdAlign"
								styles.clear(); //clear our map of styles
								final Extent gapBefore = childIndex == 0 ? flowLayout.getGapBefore() : flowLayout.getGapBetween(); //if this isn't the first component, use the between-components spacing amount
								final Extent gapAfter = childComponentIterator.hasNext() ? Extent.ZERO_EXTENT1 : flowLayout.getGapAfter(); //if this is the last component, we'll add the requested amount of space after the component as well
								if(gapBefore.getValue() != 0) { //if there is a gap before (the stylesheet should set the default to zero)
									styles.put(CSS_PROP_PADDING_TOP, gapBefore); //insert padding before the components
								}
								if(gapAfter.getValue() != 0) { //if there is a gap after (the stylesheet should set the default to zero)
									styles.put(CSS_PROP_PADDING_BOTTOM, gapAfter); //insert padding after the components
								}
								writeStyleAttribute(styles); //write the styles							
								updateFlowChildView(childComponent, flowAxis); //update this child's view
								depictContext.writeIndent(); //indent the ending tag
								depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TD); //</xhtml:td>
								depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TR); //</xhtml:tr>					
								depictContext.write("\n"); //format the output
							}
							break;
						default:
							throw new IllegalArgumentException("Unsupported axis: " + flowAxis);
					}
					depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TBODY); //</xhtml:tbody>
					depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TABLE); //</xhtml:table>
				}
				//			/*TODO fix DIV-based vertical flow
			}
			//*/			

			/*TODO fix			
						
						
						
						final String styleID;	//we'll determine the style ID for the child container elements
						final Axis flowAxis=flowLayout.getAxis();	//get the flow axis
						switch(flowAxis) {	//see on which axis we should flow
							case X:	//horizontal flow
								styleID=LAYOUT_FLOW_X_CHILD_CLASS;	//use the horizontal flow style class
								break;
							case Y:	//vertical flow
								styleID=LAYOUT_FLOW_Y_CHILD_CLASS;	//use the vertical flow style class
								break;
							case Z:	//depth flow
							default:
								throw new IllegalArgumentException("Z axis flowing currently unsupported.");
						}			
						for(final Component childComponent:component) {	//for each visible child component in the container, wrap the component in a span with the correct style
							if(childComponent.isVisible()) {	//if this component is visible TODO do we really want to do this? it may be better just to leave the wrapper material, and delegate to the child component so that it will at least be notified
								if(flowAxis==Axis.Y) {	//if we're flowing vertically
									write(context, "\n");	//format the output
								}
								writeElementBegin(context, XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//<xhtml:span>
								writeAttribute(context, null, ATTRIBUTE_CLASS, styleID);	//class="styleID"
								writeElementBeginClose(context);	//>
								childComponent.depict();	//update the child view
								writeElementEnd(context, XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//</xhtml:span>
								if(flowAxis==Axis.Y) {	//if we're flowing vertically
									write(context, "\n");	//format the output
								}
							}
						}
			*/
		}
		/*TODO fix
				else if(layout instanceof MenuLayout) {	//if this is a menu layout
					super.updateChildViews(context, component);	//lay out the children normally
				}
		*/
		else if(layout instanceof RegionLayout) { //if this is a region layout
			final RegionLayout regionLayout = (RegionLayout)layout; //cast the layout to a region layout
			final Flow spanFlow = regionLayout.getSpanFlow(); //see which flow is being spanned
			final Axis spanAxis = orientation.getAxis(spanFlow); //get the axis the span should flow on
			final Component topComponent = regionLayout.getComponent(Region.getRegion(orientation, Flow.PAGE, 0)); //get the component for the top
			final Component[] rowComponents = new Component[Region.FLOW_REGION_COUNT]; //get the row components
			rowComponents[1] = regionLayout.getComponent(Region.CENTER); //the center column always gets the center component, regardless of the orientation
			//for the table columns, use absolute regions rather than compensating for flow direction, and use the XHTML direction attribute to allow this to be changed by the client
			final Flow flowX; //we'll see which flow goes along the X axis
			final Axis lineAxis = orientation.getAxis(Flow.LINE); //get the axis of line flow
			switch(lineAxis) { //see on which axis lines flow
				case X: //if lines flow along the X axis
					flowX = Flow.LINE; //show that lines are horizontal
					rowComponents[0] = regionLayout.getComponent(Region.LINE_START); //line start
					rowComponents[Region.FLOW_REGION_COUNT - 1] = regionLayout.getComponent(Region.LINE_END); //line end
					break;
				case Y: //if lines flow along the Y axis
					flowX = Flow.PAGE; //show that pages are horizontal
					rowComponents[0] = regionLayout.getComponent(Region.PAGE_START); //page start
					rowComponents[Region.FLOW_REGION_COUNT - 1] = regionLayout.getComponent(Region.PAGE_END); //page end
					break;
				default:
					throw new AssertionError("Unexpected line axis: " + lineAxis);
			}
			final Component leftComponent = rowComponents[0]; //create an alias for the left component in case we need to use it
			final Component centerComponent = rowComponents[1]; //create an alias for the center component in case we need to use it
			final Component rightComponent = rowComponents[rowComponents.length - 1]; //create an alias for the right component in case we need to use it
			final int rowComponentCount = getInstanceCount(rowComponents); //we'll see how many components are in the row
			final Component bottomComponent = regionLayout.getComponent(Region.getRegion(orientation, Flow.PAGE, Region.FLOW_REGION_COUNT - 1)); //get the component for the bottom
			if(topComponent != null || rowComponentCount > 0 || bottomComponent != null) { //if there is a component in the layout
				depictContext.write("\n"); //format the output
				depictContext.writeIndent(); //write an indentation
				depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TABLE); //<xhtml:table>
				writeIDAttribute(null, COMPONENT_LAYOUT_CLASS_SUFFIX); //id="id-layout"
				depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_REGION_CLASS); //class="layout-region"
				final Map<String, Object> tableStyles = new HashMap<String, Object>(); //create a new map of styles
				if(regionLayout.isFixed()) { //if this is a fixed region layout
					tableStyles.put(CSS_PROP_TABLE_LAYOUT, CSS_TABLE_LAYOUT_FIXED); //indicate a fixed table layout
					tableStyles.put(CSS_PROP_WIDTH, "100%"); //indicate that the table should take up all available horizontal space
				}
				tableStyles.put(CSS_PROP_BORDER_COLLAPSE, CSS_BORDER_COLLAPSE_COLLAPSE); //collapse the table cells (needed to prevent extra space in IE)
				writeStyleAttribute(tableStyles); //write the table's styles

				///*TODO del; messes up frames
				/*TODO fix; we may be able to get around this by checking the parent
								else if(!(component.getParent() instanceof Frame)) {	//TODO testing
									final Map<String, Object> tableStyles=new HashMap<String, Object>();	//create a new map of styles
									tableStyles.put(CSS_PROP_WIDTH, "100%");	//indicate that the table should take up all available horizontal space TODO check orientation
									writeStyleAttribute(tableStyles);	//write the table's styles
								}
				*/
				//*/
				writeDirectionAttribute(orientation, flowX); //explicitly write the direction ("ltr" or "rtl") for this flow so that the orientation will be taken into account
				depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TBODY); //<xhtml:tbody> (IE will not show dynamically-allocated tables without a tbody)
				depictContext.indent(); //increase the indentation
				try {
					switch(spanAxis) { //see which components should span rows or columns
						case X: //if we should span the top and bottom components horizontally
							if(topComponent != null) { //if there is a top component TODO fix to determine if the top and bottom component should span columns or vice-versa, based upon the orientation
								if(regionLayout.isFixed() && rowComponentCount > 0) { //if this is a fixed layout with row component, write dummy components for the rows so that fixed layout will know their sizes
									depictContext.write("\n"); //format the output
									depictContext.writeIndent(); //write an indentation
									depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TR); //<xhtml:tr>					
									for(int columnIndex = 0; columnIndex < Region.FLOW_REGION_COUNT; ++columnIndex) { //for each column
										final Component rowComponent = rowComponents[columnIndex]; //get the component for this column
										if(rowComponent != null) { //if we have a component for this column
											depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TD); //<xhtml:td>
											beginRegion(regionLayout, regionLayout.getConstraints(rowComponent), orientation, true, false); //write the size for this region
											depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TD); //</xhtml:td>					
										}
									}
									depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TR); //</xhtml:tr>									
								}
								depictContext.write("\n"); //format the output
								depictContext.writeIndent(); //write an indentation
								depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TR); //<xhtml:tr>					
								depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_REGION_CLASS); //class="layout-region"
								depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TD); //<xhtml:td>
								depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_REGION_TOP_CLASS); //class="layout-region-top"
								if(rowComponentCount > 1) { //if there are more than one row component
									depictContext.writeAttribute(null, ELEMENT_TD_ATTRIBUTE_COLSPAN, Integer.toString(rowComponentCount)); //colspan="rowComponentCount"						
								}
								beginRegion(regionLayout, regionLayout.getConstraints(topComponent), orientation); //write the styles for this region
								topComponent.depict(); //update the top child view
								depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TD); //</xhtml:td>					
								depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TR); //</xhtml:tr>
							}
							if(rowComponentCount > 0) { //if there are any row components
								depictContext.write("\n"); //format the output
								depictContext.writeIndent(); //write an indentation
								depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TR); //<xhtml:tr>					
								depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_REGION_CLASS); //class="layout-region"
								for(int columnIndex = 0; columnIndex < Region.FLOW_REGION_COUNT; ++columnIndex) { //for each column
									final Component rowComponent = rowComponents[columnIndex]; //get the component for this column
									if(rowComponent != null) { //if we have a component for this column
										depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TD); //<xhtml:td>
										depictContext.writeAttribute(null, ATTRIBUTE_CLASS, ROW_REGION_CLASSES[columnIndex]); //class="layout-region-left/center/right"
										beginRegion(regionLayout, regionLayout.getConstraints(rowComponent), orientation); //write the styles for this region
										rowComponent.depict(); //update the child view for the component in this column
										depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TD); //</xhtml:td>					
									}
								}
								depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TR); //</xhtml:tr>
							}
							if(bottomComponent != null) { //if there is a top component TODO fix to determine if the top and bottom component should span columns or vice-versa, based upon the orientation
								depictContext.write("\n"); //format the output
								depictContext.writeIndent(); //write an indentation
								depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TR); //<xhtml:tr>					
								depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_REGION_CLASS); //class="layout-region"
								depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TD); //<xhtml:td>
								depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_REGION_BOTTOM_CLASS); //class="layout-region-bottom"
								if(rowComponentCount > 0) { //if there are more than one row component
									depictContext.writeAttribute(null, ELEMENT_TD_ATTRIBUTE_COLSPAN, Integer.toString(rowComponentCount)); //colspan="rowComponentCount"						
								}
								beginRegion(regionLayout, regionLayout.getConstraints(bottomComponent), orientation); //write the styles for this region
								bottomComponent.depict(); //update the bottom child view
								depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TD); //</xhtml:td>					
								depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TR); //</xhtml:tr>
							}
							break;
						case Y: //if we should span the left and right components vertically
						{
							final Component[] columnComponents = new Component[] { topComponent, centerComponent, bottomComponent }; //get the column components---even the null ones
							final int columnComponentCount = getInstanceCount(columnComponents); //we'll see how many components are in the column
							depictContext.write("\n"); //format the output
							depictContext.writeIndent(); //write an indentation
							depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TR); //<xhtml:tr>					
							depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_REGION_CLASS); //class="layout-region"
							if(leftComponent != null) { //if there is a left component
								depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TD); //<xhtml:td>
								depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_REGION_LEFT_CLASS); //class="layout-region-left"
								if(columnComponentCount > 1) { //if there are more than one row component
									depictContext.writeAttribute(null, ELEMENT_TD_ATTRIBUTE_ROWSPAN, Integer.toString(columnComponentCount)); //rowspan="columnComponentCount"						
								}
								beginRegion(regionLayout, regionLayout.getConstraints(leftComponent), orientation); //write the styles for this region
								leftComponent.depict(); //update the left child view
								depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TD); //</xhtml:td>
							}
							int centerColumnRowIndex = 0; //keep track of which center row comopnent we're on
							for(; centerColumnRowIndex < columnComponents.length && columnComponents[centerColumnRowIndex] == null; ++centerColumnRowIndex)
								; //skip the null center column components
							if(centerColumnRowIndex < columnComponents.length) { //if there is a center column component
								final Component columnComponent = columnComponents[centerColumnRowIndex]; //get this column component
								depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TD); //<xhtml:td>
								depictContext.writeAttribute(null, ATTRIBUTE_CLASS, COLUMN_REGION_CLASSES[centerColumnRowIndex]); //class="layout-region-top/center/bottom"
								/*TODO del
																	if(columnComponentCount>1) {	//if there are more than one row component
																		depictContext.writeAttribute(null, ELEMENT_TD_ATTRIBUTE_ROWSPAN, Integer.toString(columnComponentCount));	//rowspan="columnComponentCount"						
																	}
								*/
								beginRegion(regionLayout, regionLayout.getConstraints(columnComponent), orientation); //write the styles for this region
								columnComponent.depict(); //update the child view for the component in this column
								depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TD); //</xhtml:td>					
							}
							if(rightComponent != null) { //if there is a right component
								depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TD); //<xhtml:td>
								depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_REGION_RIGHT_CLASS); //class="layout-region-right"
								if(columnComponentCount > 1) { //if there are more than one row component
									depictContext.writeAttribute(null, ELEMENT_TD_ATTRIBUTE_ROWSPAN, Integer.toString(columnComponentCount)); //rowspan="columnComponentCount"						
								}
								beginRegion(regionLayout, regionLayout.getConstraints(rightComponent), orientation); //write the styles for this region
								rightComponent.depict(); //update the right child view
								depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TD); //</xhtml:td>
							}
							depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TR); //</xhtml:tr>
							for(++centerColumnRowIndex; centerColumnRowIndex < columnComponents.length; ++centerColumnRowIndex) { //for each of the remaining center column components (the left and right components will have already been written, and span down the correct number of rows)
								final Component columnComponent = columnComponents[centerColumnRowIndex]; //get this column component
								if(columnComponent != null) { //if there is a column component for this row
									depictContext.write("\n"); //format the output
									depictContext.writeIndent(); //write an indentation
									depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TR); //<xhtml:tr>					
									depictContext.writeAttribute(null, ATTRIBUTE_CLASS, LAYOUT_REGION_CLASS); //class="layout-region"
									depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_TD); //<xhtml:td>
									depictContext.writeAttribute(null, ATTRIBUTE_CLASS, COLUMN_REGION_CLASSES[centerColumnRowIndex]); //class="layout-region-top/center/bottom"
									beginRegion(regionLayout, regionLayout.getConstraints(columnComponent), orientation); //write the styles for this region
									columnComponent.depict(); //update the child view for the component in this column
									depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TD); //</xhtml:td>					
									depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TR); //</xhtml:tr>
								}
							}
						}
							break;
						default: //if we don't recognize the span axis
							throw new AssertionError("Unrecognized span axis: " + spanAxis);
					}
				} finally {
					depictContext.unindent(); //always unindent
				}
				depictContext.write("\n"); //format the output
				depictContext.writeIndent(); //indent the ending tag
				depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TBODY); //</xhtml:tbody>
				depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_TABLE); //</xhtml:table>
				depictContext.write("\n"); //format the output
			}
		} else if(layout instanceof CardLayout) { //if this is a card layout
			final CardLayout cardLayout = (CardLayout)layout; //cast the layout to a card layout
			final Component selectedCard = cardLayout.getValue(); //get the selected component
			if(selectedCard != null) { //if a component is selected
				selectedCard.depict(); //update the component view
			}
			for(final Component card : component.getChildComponents()) { //set the views of the non-visible cards as updated (this will keep them from being sent to the client before they are needed)
				if(card != selectedCard) { //if this is not the selected card
					AbstractComponent.setDepicted(card, true); //the other cards might as well have had their views updates, as they aren't shown					
				}
			}
		} else { //if we don't recognize the layout
			throw new IllegalArgumentException("Unrecognized layout " + layout);
		}
	}

	/**
	 * Determines if any children of this container are visible.
	 * @return <code>true</code> if there is at least one visible child component.
	 */
	/*TODO del if not needed
		protected boolean isChildComponentVisible(final C component)
		{
			for(final Component childComponent:component) {	//for each child component
				if(childComponent.isVisible()) {	//if this child component is visible
					return true;	//show that we found a visible child component
				}
			}
			return false;	//indicate that we could not find any visible child components
		}
	*/

	/**
	 * Writes an XHTML style attribute for the given region, if style information is needed for that region.
	 * <p>
	 * This version writes alignment and style attributes.
	 * </p>
	 * @param regionLayout The region layout being rendered.
	 * @param regionConstraints The constraints for the region about to be written.
	 * @param orientation The orientation of the component for which the region is being rendered.
	 * @throws IOException if there is an error writing the attribute.
	 */
	protected void beginRegion(final RegionLayout regionLayout, final RegionConstraints regionConstraints, final Orientation orientation) throws IOException {
		beginRegion(regionLayout, regionConstraints, orientation, true, true); //begin the region including size and other styles
	}

	/**
	 * Writes an XHTML style attribute for the given region, if style information is needed for that region.
	 * <p>
	 * This version writes alignment and style attributes.
	 * </p>
	 * @param regionLayout The region layout being rendered.
	 * @param regionConstraints The constraints for the region about to be written.
	 * @param orientation The orientation of the component for which the region is being rendered.
	 * @param includeSize Whether size information should be included.
	 * @param includeStyles Whether non-size-related style information should be included.
	 * @throws IOException if there is an error writing the attribute.
	 */
	protected void beginRegion(final RegionLayout regionLayout, final RegionConstraints regionConstraints, final Orientation orientation,
			final boolean includeSize, final boolean includeStyles) throws IOException {
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final Map<String, Object> styles = new HashMap<String, Object>(); //create a new map of styles
		if(includeSize) { //if we should include the size
			for(final Flow flow : Flow.values()) { //for each flow, write an alignment attribute and the width or height
				final double alignment = regionConstraints.getAlignment(flow); //get the alignment
				final Axis axis = orientation.getAxis(flow); //get the axis of the flow
				final Flow.Direction direction = orientation.getDirection(flow); //get the direction of the flow
				final String align = getAlign(alignment, axis, direction); //determine the align string from the alignment value TODO does the direction attribute take care of this automatically in rtl configuraitons?
				final String alignAttributeName = axis == Axis.X ? ELEMENT_TD_ATTRIBUTE_ALIGN : ELEMENT_TD_ATTRIBUTE_VALIGN; //use the correct <td> attribute for the fline
				depictContext.writeAttribute(null, alignAttributeName, align); //align="align"
				final Extent extent = regionConstraints.getExtent(flow); //get the extent for this flow
				if(extent != null) { //if this region has a requested extent for this flow
					styles.put(axis == Axis.X ? CSS_PROP_WIDTH : CSS_PROP_HEIGHT, extent); //indicate the width or height				
				}
			}
		}
		if(includeStyles) { //if we should include other styles
			for(final Border border : Border.values()) { //for each logical border
				final Side side = orientation.getSide(border); //get the absolute side on which this border lies
				final Extent paddingExtent = regionConstraints.getPaddingExtent(border); //get the padding extent for this border
				if(paddingExtent.getValue() != 0) { //if a non-zero padding extent is specified (the stylesheet specifies a zero default padding)
					styles.put(XHTMLDepictContext.CSS_PROPERTY_PADDING_X_TEMPLATE.apply(getSerializationName(side)), paddingExtent); //set the padding extent
				}
			}
		}
		writeStyleAttribute(styles); //write the styles
	}

	/**
	 * Determines the alignment string for an alignment on a particular axis.
	 * <p>
	 * This implementation converts the alignment to a value by determining into which third the alignment value falls.
	 * </p>
	 * <p>
	 * Note that these values also work on the {@link Axis#Y} axis for the CSS {@link CSS#CSS_PROP_VERTICAL_ALIGN} property.
	 * </p>
	 * @param alignment The alignment value to convert.
	 * @param axis The axis of alignment.
	 * @param direction The direction of flow along the axis.
	 * @return The string alignment value, appropriate for a table cell <code>align</code> or <code>valign</code>.
	 * @see #TD_ALIGN_LEFT
	 * @see #TD_ALIGN_CENTER
	 * @see #TD_ALIGN_RIGHT
	 * @see #TD_VALIGN_TOP
	 * @see #TD_VALIGN_MIDDLE
	 * @see #TD_VALIGN_BOTTOM
	 */
	protected String getAlign(final double alignment, final Axis axis, final Flow.Direction direction) {
		switch(axis) { //see which axis this is
			case X:
				if(alignment < ONE_THIRD_DOUBLE) { //if the alignment is in the bottom third
					return direction == Flow.Direction.INCREASING ? TD_ALIGN_LEFT : TD_ALIGN_RIGHT; //get the near alignment, depending on flow direction
				} else if(alignment < TWO_THIRDS_DOUBLE) { //if the alignment is in the middle third
					return TD_ALIGN_CENTER; //the middle alignment is always "center"
				} else { //if the alignment is in the top third
					return direction == Flow.Direction.INCREASING ? TD_ALIGN_RIGHT : TD_ALIGN_LEFT; //get the far alignment, depending on flow direction
				}
			case Y:
				if(alignment < ONE_THIRD_DOUBLE) { //if the alignment is in the bottom third
					return direction == Flow.Direction.INCREASING ? TD_VALIGN_TOP : TD_VALIGN_BOTTOM; //get the near alignment, depending on flow direction
				} else if(alignment < TWO_THIRDS_DOUBLE) { //if the alignment is in the middle third
					return TD_VALIGN_MIDDLE; //the middle alignment is always "middle"
				} else { //if the alignment is in the top third
					return direction == Flow.Direction.INCREASING ? TD_VALIGN_BOTTOM : TD_VALIGN_TOP; //get the far alignment, depending on flow direction
				}
			default:
				throw new AssertionError("Axis " + axis + " not supported.");
		}

	}

	/**
	 * Updates a child view in a flow.
	 * <p>
	 * When this method is called, an enclosing element will have been started for the child.
	 * </p>
	 * @param childComponent The child component the view of which is to be updated.
	 * @param axis The axis along which flow is occurring.
	 * @throws IOException if there is an error updating child views.
	 */
	protected void updateFlowChildView(final Component childComponent, final Axis axis) throws IOException {
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		depictContext.write("\n"); //format the output
		depictContext.indent(); //increase the indentation
		try {
			depictContext.writeIndent(); //write an indentation
			childComponent.depict(); //update the child view
			depictContext.write("\n"); //format the output
		} finally {
			depictContext.unindent(); //always unindent
		}
	}

}
