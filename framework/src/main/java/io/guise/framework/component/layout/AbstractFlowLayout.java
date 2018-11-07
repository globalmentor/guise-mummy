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

package io.guise.framework.component.layout;

import static java.util.Objects.*;

import io.guise.framework.component.Component;
import io.guise.framework.component.LayoutComponent;
import io.guise.framework.geometry.Extent;
import io.guise.framework.model.ui.PresentationModel;

import static com.globalmentor.java.Classes.*;

/**
 * A layout that flows information along an axis.
 * @param <T> The type of layout constraints associated with each component.
 * @author Garret Wilson
 */
public abstract class AbstractFlowLayout<T extends AbstractFlowConstraints> extends AbstractLayout<T> {

	/** The bound property of the alignment. */
	public static final String ALIGNMENT_PROPERTY = getPropertyName(AbstractFlowLayout.class, "alignment");
	/** The bound property of the flow. */
	public static final String FLOW_PROPERTY = getPropertyName(AbstractFlowLayout.class, "flow");
	/** The bound property of the gap after flowed components. */
	public static final String GAP_AFTER_PROPERTY = getPropertyName(AbstractFlowLayout.class, "gapAfter");
	/** The bound property of the gap before flowed components. */
	public static final String GAP_BEFORE_PROPERTY = getPropertyName(AbstractFlowLayout.class, "gapBefore");
	/** The bound property of the gap between flowed components. */
	public static final String GAP_BETWEEN_PROPERTY = getPropertyName(AbstractFlowLayout.class, "gapBetween");
	/** The bound property of whether wrapping occurs. */
	public static final String WRAPPED_PROPERTY = getPropertyName(AbstractFlowLayout.class, "wrapped");

	/** The default alignment of components perpendicular to the flow axis in terms relative to the beginning of the alignment axis. */
	private double alignment = 0;

	/** @return The default alignment of components perpendicular to the flow axis in terms relative to the beginning of the alignment axis. */
	public double getAlignment() {
		return alignment;
	}

	/**
	 * Sets the default alignment of components perpendicular to the flow axis. For example, in a left-to-right top-to-bottom orientation flowing along the
	 * {@link Flow#LINE} axis, alignments of 0.0, 0.5, and 1.0 would be equivalent to what are commonly known as <dfn>left</dfn>, <dfn>center</dfn>, and
	 * <dfn>right</dfn> alignments, respectively. In the same orientation flowing along the {@link Flow#PAGE} axis, alignments of 0.0, 0.5, and 1.0 would be
	 * equivalent to what are commonly known as <dfn>top</dfn>, <dfn>middle</dfn>, and <dfn>bottom</dfn> alignments, respectively. This method also acts as a
	 * convenience method by unconditionally updating the alignment of the flow constraints of any child components of this layout's owner. This is a bound
	 * property of type {@link Double}.
	 * @param newAlignment The alignment of components perpendicular to the flow axis in terms relative to the beginning of the alignment axis.
	 * @see #ALIGNMENT_PROPERTY
	 * @see FlowConstraints#setAlignment(double)
	 */
	public void setAlignment(final double newAlignment) {
		if(alignment != newAlignment) { //if the value is really changing
			final double oldAlignment = alignment; //get the current value
			alignment = newAlignment; //update the value
			firePropertyChange(ALIGNMENT_PROPERTY, Double.valueOf(oldAlignment), Double.valueOf(newAlignment));
		}
		final LayoutComponent owner = getOwner(); //get the owner of this layout, if any
		if(owner != null) { //if this layout has an owner
			for(final Component component : getOwner().getChildComponents()) { //for all child components of the owner
				getConstraints(component).setAlignment(newAlignment); //update this child component's constraints with the new alignment value
			}
		}
	}

	/** The logical axis (line or page) along which information is flowed. */
	private Flow flow;

	/** @return The logical axis (line or page) along which information is flowed. */
	public Flow getFlow() {
		return flow;
	}

	/**
	 * Sets the logical axis (line or page) along which information is flowed. This is a bound property.
	 * @param newFlow The logical axis along which information is flowed.
	 * @throws NullPointerException if the given flow is <code>null</code>.
	 * @see #FLOW_PROPERTY
	 */
	public void setFlow(final Flow newFlow) {
		if(flow != requireNonNull(newFlow, "Flow cannot be null.")) { //if the value is really changing
			final Flow oldFlow = flow; //get the old value
			flow = newFlow; //actually change the value
			firePropertyChange(FLOW_PROPERTY, oldFlow, newFlow); //indicate that the value changed
		}
	}

	/** The gap after flowed components. */
	private Extent gapAfter = Extent.ZERO_EXTENT1;

	/** @return The gap after flowed components. */
	public Extent getGapAfter() {
		return gapAfter;
	}

	/**
	 * Sets the gap after flowed components. This is a bound property.
	 * @param newGapAfter The gap after flowed components.
	 * @throws NullPointerException if the given gap is <code>null</code>.
	 * @see #GAP_AFTER_PROPERTY
	 */
	public void setGapAfter(final Extent newGapAfter) {
		if(!gapAfter.equals(requireNonNull(newGapAfter, "Gap after cannot be null."))) { //if the value is really changing
			final Extent oldGapAfter = gapAfter; //get the old value
			gapAfter = newGapAfter; //actually change the value
			firePropertyChange(GAP_AFTER_PROPERTY, oldGapAfter, newGapAfter); //indicate that the value changed
		}
	}

	/** The gap before flowed components. */
	private Extent gapBefore = Extent.ZERO_EXTENT1;

	/** @return The gap before flowed components. */
	public Extent getGapBefore() {
		return gapBefore;
	}

	/**
	 * Sets the gap before flowed components. This is a bound property.
	 * @param newGapBefore The gap before flowed components.
	 * @throws NullPointerException if the given gap is <code>null</code>.
	 * @see #GAP_BEFORE_PROPERTY
	 */
	public void setGapBefore(final Extent newGapBefore) {
		if(!gapBefore.equals(requireNonNull(newGapBefore, "Gap before cannot be null."))) { //if the value is really changing
			final Extent oldGapBefore = gapBefore; //get the old value
			gapBefore = newGapBefore; //actually change the value
			firePropertyChange(GAP_BEFORE_PROPERTY, oldGapBefore, newGapBefore); //indicate that the value changed
		}
	}

	/** The gap between flowed components. */
	private Extent gapBetween = Extent.ZERO_EXTENT1;

	/** @return The gap between flowed components. */
	public Extent getGapBetween() {
		return gapBetween;
	}

	/**
	 * Sets the gap between flowed components. This is a bound property.
	 * @param newGapBetween The gap between flowed components.
	 * @throws NullPointerException if the given gap is <code>null</code>.
	 * @see #GAP_BETWEEN_PROPERTY
	 */
	public void setGapBetween(final Extent newGapBetween) {
		if(!gapBetween.equals(requireNonNull(newGapBetween, "Gap between cannot be null."))) { //if the value is really changing
			final Extent oldGapBetween = gapBetween; //get the old value
			gapBetween = newGapBetween; //actually change the value
			firePropertyChange(GAP_BETWEEN_PROPERTY, oldGapBetween, newGapBetween); //indicate that the value changed
		}
	}

	/** Whether flowed children are wrapped when the flow extent is reached. */
	private boolean wrapped;

	/**
	 * @return Whether the description is displayed.
	 * @see PresentationModel#isDisplayed()
	 */
	public boolean isWrapped() {
		return wrapped;
	}

	/**
	 * Sets whether flowed children are wrapped when the flow extent is reached. This is a bound property of type {@link Boolean}.
	 * @param newWrapped Whether flowed children should be wrapped when the flow extent is reached.
	 * @see #WRAPPED_PROPERTY
	 */
	public void setWrapped(final boolean newWrapped) {
		if(wrapped != newWrapped) { //if the value is really changing
			final boolean oldWrapped = wrapped; //get the current value
			wrapped = newWrapped; //update the value
			firePropertyChange(WRAPPED_PROPERTY, Boolean.valueOf(oldWrapped), Boolean.valueOf(newWrapped));
		}
	}

	/**
	 * Sets the gap before, between, and after flowed components. This is a convenience method that sets each of the gaps to the same value. Each gap represents a
	 * bound property.
	 * @param newGap The gap before, between, and after flowed components.
	 * @throws NullPointerException if the given gap is <code>null</code>.
	 * @see #GAP_BEFORE_PROPERTY
	 * @see #GAP_BETWEEN_PROPERTY
	 * @see #GAP_AFTER_PROPERTY
	 */
	public void setGap(final Extent newGap) {
		setGapBefore(newGap); //set each of the gaps to the same value
		setGapBetween(newGap);
		setGapAfter(newGap);
	}

	/**
	 * Flow and wrap constructor.
	 * @param flow The logical axis (line or page) along which information is flowed.
	 * @param wrapped Whether flowed children should be wrapped when the flow extent is reached.
	 * @throws NullPointerException if the flow axis is <code>null</code>.
	 */
	public AbstractFlowLayout(final Flow flow, final boolean wrapped) {
		this.flow = requireNonNull(flow, "Flow cannot be null."); //store the flow
		this.wrapped = wrapped; //set the wrapped state
	}

}
