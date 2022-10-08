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

package io.guise.framework.component.layout;

/**
 * A layout that flows information along an axis.
 * @author Garret Wilson
 */
public class FlowLayout extends AbstractFlowLayout<FlowConstraints> {

	@Override
	public Class<? extends FlowConstraints> getConstraintsClass() {
		return FlowConstraints.class;
	}

	/** Default constructor with {@link Flow#PAGE} layout. */
	public FlowLayout() {
		this(Flow.PAGE); //construct the class with page flow layout
	}

	/**
	 * Flow constructor with no wrapping.
	 * @param flow The logical axis (line or page) along which information is flowed.
	 * @throws NullPointerException if the flow axis is <code>null</code>.
	 */
	public FlowLayout(final Flow flow) {
		this(flow, false); //construct the class with no wrapping
	}

	/**
	 * Flow and wrap constructor.
	 * @param flow The logical axis (line or page) along which information is flowed.
	 * @param wrapped Whether flowed children should be wrapped when the flow extent is reached.
	 * @throws NullPointerException if the flow axis is <code>null</code>.
	 */
	public FlowLayout(final Flow flow, final boolean wrapped) {
		super(flow, wrapped); //construct the parent class
	}

	@Override
	public FlowConstraints createDefaultConstraints() {
		final FlowConstraints flowConstraints = new FlowConstraints(); //create a default constraints object
		flowConstraints.setAlignment(getAlignment()); //set the default alignment
		return flowConstraints; //return the new flow constraints
	}
}
