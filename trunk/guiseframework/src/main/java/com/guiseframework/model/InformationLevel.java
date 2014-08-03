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

package com.guiseframework.model;

/**
 * Levels of logged or reported information. The first two levels, {@link #TRACE} and {@link #INFO}, are debug-only log levels; their information is meant for
 * system analysis use, and are usually relegated to the debug log file.
 * @author Garret Wilson
 */
public enum InformationLevel {
	/** Indicates the program's execution path. */
	TRACE,
	/** Indicates useful information that should nonetheless not be logged. */
	INFO,
	/** Specific information which should be logged but which are adversity-neutral. */
	LOG,
	/** Information used for tracking actions for later reports. */
	TRACK,
	/** Indications that conditions are possibly adverse. */
	WARN,
	/** Indicates an unexpected condition representing an error. */
	ERROR;

};
