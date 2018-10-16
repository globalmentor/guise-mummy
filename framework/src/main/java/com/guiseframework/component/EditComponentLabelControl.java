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

package com.guiseframework.component;

import com.guiseframework.component.layout.*;

/**
 * Control that allows a component's label to be edited in-place.
 * @author Garret Wilson
 */
public class EditComponentLabelControl extends AbstractEditComponentTextControl<Component> {

	/** Default constructor with a default label component, default text edit control, and {@link Flow#LINE} layout. */
	public EditComponentLabelControl() {
		this(Flow.LINE); //construct the class with line flow layout
	}

	/**
	 * Flow constructor with a default label component and text edit control.
	 * @param flow The logical axis (line or page) along which information is flowed.
	 * @throws NullPointerException if the flow axis is <code>null</code>.
	 */
	public EditComponentLabelControl(final Flow flow) {
		this(new Label(), flow); //construct the parent class with a default label component
	}

	/**
	 * Label component constructor with default text edit control and {@link Flow#LINE} flow.
	 * @param labelComponent The component the label of which is to be edited.
	 * @throws NullPointerException if the label component is <code>null</code>.
	 */
	public EditComponentLabelControl(final Component labelComponent) {
		this(labelComponent, new TextControl<String>(String.class)); //construct the class with a default text control
	}

	/**
	 * Label component and flow constructor with default text edit control.
	 * @param labelComponent The component the label of which is to be edited.
	 * @param flow The logical axis (line or page) along which information is flowed.
	 * @throws NullPointerException if the label component and/or flow is <code>null</code>.
	 */
	public EditComponentLabelControl(final Component labelComponent, final Flow flow) {
		this(labelComponent, new TextControl<String>(String.class), flow); //construct the class with a default text control
	}

	/**
	 * Label component and value control constructor with default {@link Flow#LINE} flow.
	 * @param labelComponent The component the label of which is to be edited.
	 * @param editControl The control used to edit the label.
	 * @throws NullPointerException if the label component and/or edit control is <code>null</code>.
	 */
	public EditComponentLabelControl(final Component labelComponent, final ValueControl<String> editControl) {
		this(labelComponent, editControl, Flow.LINE); //construct the class with line flow
	}

	/**
	 * Label component, value control, and flow constructor.
	 * @param labelComponent The component the label of which is to be edited.
	 * @param editControl The control used to edit the label.
	 * @param flow The logical axis (line or page) along which information is flowed.
	 * @throws NullPointerException if the label component, value control, and/or flow axis is <code>null</code>.
	 */
	public EditComponentLabelControl(final Component labelComponent, final ValueControl<String> editControl, final Flow flow) {
		super(labelComponent, Component.LABEL_PROPERTY, editControl, flow); //construct the parent class with a flow layout
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version returns the value of {@link Component#getLabel()}
	 * </p>
	 */
	@Override
	protected String getText(final Component editedComponent) {
		return editedComponent.getLabel(); //return the component's label
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version updates the text of the component using {@link Component#setLabel(String)}
	 * </p>
	 */
	@Override
	protected void setText(final Component editedComponent, final String newText) {
		editedComponent.setLabel(newText); //set the component's label		
	}

}
