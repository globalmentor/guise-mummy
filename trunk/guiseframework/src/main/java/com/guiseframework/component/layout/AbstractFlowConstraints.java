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

package com.guiseframework.component.layout;

import static com.globalmentor.java.Classes.*;

/**Abstract constraints on individual component flow.
@author Garret Wilson
*/
public class AbstractFlowConstraints extends AbstractConstraints
{
	/**The bound property of the alignment.*/
	public final static String ALIGNMENT_PROPERTY=getPropertyName(AbstractFlowConstraints.class, "alignment");

	/**The alignment of a component perpendicular to the flow axis in terms relative to the beginning of the alignment axis.*/
	private double alignment=0;

		/**@return The alignment of a component perpendicular to the flow axis in terms relative to the beginning of the alignment axis.*/
		public double getAlignment() {return alignment;}

		/**Sets the alignment of a component perpendicular to the flow axis.
		For example, in a left-to-right top-to-bottom orientation flowing along the {@link Flow#LINE} axis,
		alignments of 0.0, 0.5, and 1.0 would be equivalent to what are commonly known as <dfn>left</dfn>, <dfn>center</dfn>, and <dfn>right</dfn> alignments, respectively. 
		In the same orientation flowing along the {@link Flow#PAGE} axis,
		alignments of 0.0, 0.5, and 1.0 would be equivalent to what are commonly known as <dfn>top</dfn>, <dfn>middle</dfn>, and <dfn>bottom</dfn> alignments, respectively. 
		This is a bound property of type {@link Double}.
		@param newAlignment The alignment of a component perpendicular to the flow axis in terms relative to the beginning of the alignment axis.
		@see #ALIGNMENT_PROPERTY
		*/
		public void setAlignment(final double newAlignment)
		{
			if(alignment!=newAlignment)	//if the value is really changing
			{
				final double oldAlignment=alignment;	//get the current value
				alignment=newAlignment;	//update the value
				firePropertyChange(ALIGNMENT_PROPERTY, Double.valueOf(oldAlignment), Double.valueOf(newAlignment));
			}
		}

}
