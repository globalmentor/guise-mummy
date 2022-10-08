/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.component;

import io.guise.framework.component.layout.Flow;

/**
 * Control that allows a text component's text to be edited in-place.
 * @author Garret Wilson
 */
public class EditComponentTextControl extends AbstractEditComponentTextControl<TextBox> {

	/** Default constructor with a default text component, default text edit control, and {@link Flow#LINE} layout. */
	public EditComponentTextControl() {
		this(Flow.LINE); //construct the class with line flow layout
	}

	/**
	 * Flow constructor with a default text component and text edit control.
	 * @param flow The logical axis (line or page) along which information is flowed.
	 * @throws NullPointerException if the flow axis is <code>null</code>.
	 */
	public EditComponentTextControl(final Flow flow) {
		this(new TextBox(), flow); //construct the parent class with a default text component
	}

	/**
	 * Text component constructor with default text edit control and {@link Flow#LINE} flow.
	 * @param textComponent The component the text of which is to be edited.
	 * @throws NullPointerException if the text component is <code>null</code>.
	 */
	public EditComponentTextControl(final TextBox textComponent) {
		this(textComponent, new TextControl<String>(String.class)); //construct the class with a default text control
	}

	/**
	 * Text component and flow constructor with default text edit control.
	 * @param textComponent The component the text of which is to be edited.
	 * @param flow The logical axis (line or page) along which information is flowed.
	 * @throws NullPointerException if the text component and/or flow is <code>null</code>.
	 */
	public EditComponentTextControl(final TextBox textComponent, final Flow flow) {
		this(textComponent, new TextControl<String>(String.class), flow); //construct the class with a default text control
	}

	/**
	 * Text component and value control constructor with default {@link Flow#LINE} flow.
	 * @param textComponent The component the text of which is to be edited.
	 * @param editControl The control used to edit the text.
	 * @throws NullPointerException if the text component and/or edit control is <code>null</code>.
	 */
	public EditComponentTextControl(final TextBox textComponent, final ValueControl<String> editControl) {
		this(textComponent, editControl, Flow.LINE); //construct the class with line flow
	}

	/**
	 * Text component, value control, and flow constructor.
	 * @param textComponent The component the text of which is to be edited.
	 * @param editControl The control used to edit the text.
	 * @param flow The logical axis (line or page) along which information is flowed.
	 * @throws NullPointerException if the text component, value control, and/or flow axis is <code>null</code>.
	 */
	public EditComponentTextControl(final TextBox textComponent, final ValueControl<String> editControl, final Flow flow) {
		super(textComponent, TextBox.TEXT_PROPERTY, editControl, flow); //construct the parent class with a flow layout
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version returns the value of {@link TextBox#getText()}
	 * </p>
	 */
	@Override
	protected String getText(final TextBox editedComponent) {
		return editedComponent.getText(); //return the component's text
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version updates the text of the component using {@link TextBox#setText(String)}
	 * </p>
	 */
	@Override
	protected void setText(final TextBox editedComponent, final String newText) {
		editedComponent.setText(newText); //set the component's text		
	}

}
