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

package com.guiseframework.converter;

/**
 * Exception representing a conversion error.
 * @author Garret Wilson
 */
public class ConversionException extends Exception {

	/** The value being converted, which may be of any type, or <code>null</code> if the value being converted is not available. */
	private final Object value;

	/** @return The value being converted, which may be of any type, or <code>null</code> if the value being converted is not available. */
	public Object getValue() {
		return value;
	}

	/**
	 * Constructs a new exception with <code>null</code> as its detail message. The cause is not initialized, and may subsequently be initialized by a call to
	 * {@link Throwable#initCause(java.lang.Throwable)}.
	 */
	public ConversionException() {
		this((String)null); //construct the class with no message
	}

	/**
	 * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently be initialized by a call to
	 * {@link Throwable#initCause(java.lang.Throwable)}.
	 * @param message The detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
	 */
	public ConversionException(final String message) {
		this(message, null); //construct the class with the message and no value
	}

	/**
	 * Constructs a new exception with the specified detail message and value object. The cause is not initialized, and may subsequently be initialized by a call
	 * to {@link Throwable#initCause(java.lang.Throwable)}.
	 * @param message The detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
	 * @param value The value being converted, which may be of any type, or <code>null</code> if the value being converted is not available.
	 */
	public ConversionException(final String message, final Object value) {
		this(message, null, value); //construct the class with no cause
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of <code>(cause==null ? null : cause.toString())</code>.
	 * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method), or <code>null</code> if the cause is nonexistent or unknown.
	 */
	public ConversionException(final Throwable cause) {
		this(cause != null ? cause.toString() : null, cause); //construct the class with a cause message, if possible
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
	 * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method), or <code>null</code> if the cause is nonexistent or unknown.
	 */
	public ConversionException(final String message, final Throwable cause) {
		this(message, cause, null); //construct the class, indicating that no value is available
	}

	/**
	 * Constructs a new exception with the specified detail message, cause, and value object.
	 * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
	 * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method), or <code>null</code> if the cause is nonexistent or unknown.
	 * @param value The value being converted, which may be of any type, or <code>null</code> if the value being converted is not available.
	 */
	public ConversionException(final String message, final Throwable cause, final Object value) {
		super(message, cause); //construct the parent class with the message and cause
		this.value = value; //save the value
	}

}
