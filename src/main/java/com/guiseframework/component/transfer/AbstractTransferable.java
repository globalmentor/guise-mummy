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

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;

/**
 * An abstract object that can be transferred, such as between components using drag and drop.
 * @param <S> The source of the transfer.
 * @author Garret Wilson
 */
public abstract class AbstractTransferable<S> implements Transferable<S> {

	/** The source of the transferable data. */
	private final S source;

	/** @return The source of the transferable data. */
	public S getSource() {
		return source;
	}

	/**
	 * Determines whether this transferable can transfer data with the given content type. This implementation calls {@link #getContentTypes()}.
	 * @param contentType The type of data requested, which may include wildcards.
	 * @return <code>true</code> if this object can transfer data with the requested content type.
	 */
	public boolean canTransfer(final ContentType contentType) {
		for(final ContentType transferContentType : getContentTypes()) { //for each content type
			if(contentType.hasBaseType(transferContentType)) { //if this content type matches
				return true; //indicate that we found a match
			}
		}
		return false; //indicate that there is no matching content type
	}

	/**
	 * Transfers data of the given class. This implementation delegates to {@link #transfer(ContentType)}.
	 * @param <T> The type of object to be transferred.
	 * @param objectClass The class of object to return.
	 * @return The transferred data object, which may be <code>null</code>.
	 * @throws IllegalArgumentException if the given class is not supported.
	 */
	public <T> T transfer(final Class<T> objectClass) {
		return objectClass.cast(transfer(getObjectContentType(objectClass))); //transfer the object based upon the content type and cast it to the class type
	}

	/**
	 * Source constructor.
	 * @param source The source of the transferable data.
	 * @throws NullPointerException if the provided source is <code>null</code>.
	 */
	public AbstractTransferable(final S source) {
		this.source = checkInstance(source, "Source cannot be null.");
	}
}
