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

package io.guise.framework.component.transfer;

import static java.util.Objects.*;

import com.globalmentor.net.MediaType;

import static com.globalmentor.java.Classes.*;

/**
 * An abstract object that can be transferred, such as between components using drag and drop.
 * @param <S> The source of the transfer.
 * @author Garret Wilson
 */
public abstract class AbstractTransferable<S> implements Transferable<S> {

	/** The source of the transferable data. */
	private final S source;

	@Override
	public S getSource() {
		return source;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation calls {@link #getContentTypes()}.
	 * </p>
	 */
	@Override
	public boolean canTransfer(final MediaType contentType) {
		for(final MediaType transferContentType : getContentTypes()) { //for each content type
			if(contentType.hasBaseType(transferContentType)) { //if this content type matches
				return true; //indicate that we found a match
			}
		}
		return false; //indicate that there is no matching content type
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation delegates to {@link #transfer(MediaType)}.
	 * </p>
	 */
	@Override
	public <T> T transfer(final Class<T> objectClass) {
		return objectClass.cast(transfer(getObjectMediaType(objectClass))); //transfer the object based upon the content type and cast it to the class type
	}

	/**
	 * Source constructor.
	 * @param source The source of the transferable data.
	 * @throws NullPointerException if the provided source is <code>null</code>.
	 */
	public AbstractTransferable(final S source) {
		this.source = requireNonNull(source, "Source cannot be null.");
	}
}
