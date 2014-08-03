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

package com.guiseframework.component.transfer;

import com.globalmentor.net.ContentType;

/**
 * An object that can be transferred, such as between components using drag and drop.
 * @param <S> The source of the transfer.
 * @author Garret Wilson
 */
public interface Transferable<S> {

	/** @return The source of the transferable data. */
	public S getSource();

	/** @return The content types available for this transfer. */
	public ContentType[] getContentTypes();

	/**
	 * Determines whether this transferable can transfer data with the given content type.
	 * @param contentType The type of data requested, which may include wildcards.
	 * @return <code>true</code> if this object can transfer data with the requested content type.
	 */
	public boolean canTransfer(final ContentType contentType);

	/**
	 * Transfers data using the given content type.
	 * @param contentType The type of data expected.
	 * @return The transferred data, which may be <code>null</code>.
	 * @throws IllegalArgumentException if the given content type is not supported.
	 */
	public Object transfer(final ContentType contentType);

	/**
	 * Transfers data of the given class.
	 * @param <T> The type of object to be transferred.
	 * @param objectClass The class of object to return.
	 * @return The transferred data object, which may be <code>null</code>.
	 * @throws IllegalArgumentException if the given class is not supported.
	 */
	public <T> T transfer(final Class<T> objectClass);
}
