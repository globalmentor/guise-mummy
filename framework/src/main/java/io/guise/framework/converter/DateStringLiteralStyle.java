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

package io.guise.framework.converter;

/** The style of a date in its string literal form. */
public enum DateStringLiteralStyle {
	/** A completely numeric representation, such as 12.13.52. */
	SHORT,
	/** A medium representation, such as Jan 12, 1952. */
	MEDIUM,
	/** A long representation, such as January 12, 1952. */
	LONG,
	/** A completely specified representation, such as Tuesday, April 12, 1952 AD. */
	FULL,
	/** The day of the week, such as Tuesday. */
	DAY_OF_WEEK,
	/** The abbreviated day of the week, such as Tue. */
	DAY_OF_WEEK_SHORT,
	/** The month of the year, such as January. */
	MONTH_OF_YEAR,
	/** The abbreviated month of the year, such as Jan. */
	MONTH_OF_YEAR_SHORT;
}
