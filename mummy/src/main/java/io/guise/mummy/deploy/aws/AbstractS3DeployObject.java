/*
 * Copyright © 2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mummy.deploy.aws;

import static java.util.Collections.*;
import static java.util.Objects.*;

import java.io.*;
import java.util.*;

import javax.annotation.*;

/**
 * Encapsulation of object, such as a Guise Mummy artifact, to be deployed to S3.
 * @implSpec This class provides an implementation of {@link #createContentStreamProvider()} that only requires subclasses to override
 *           {@link #createInputStream()} to retrieve an input stream to the deploy object content.
 * @author Garret Wilson
 */
public abstract class AbstractS3DeployObject implements S3DeployObject {

	private final String key;

	@Override
	public String getKey() {
		return key;
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation includes {@link S3DeployObject#METADATA_CONTENT_FINGERPRINT} metadata as Base64 if a fingerprint is available.
	 * @see #findFingerprint()
	 */
	@Override
	public Map<String, String> getMetadata() {
		return findFingerprint().map(bytes -> Base64.getUrlEncoder().withoutPadding().encodeToString(bytes))
				.map(base64 -> Map.of(METADATA_CONTENT_FINGERPRINT, base64)).orElse(emptyMap());
	}

	/**
	 * Constructor.
	 * @param key The S3 key representing the deployment path of the object in the bucket.
	 */
	public AbstractS3DeployObject(@Nonnull final String key) {
		this.key = requireNonNull(key);
	}

	@Override
	public software.amazon.awssdk.http.ContentStreamProvider createContentStreamProvider() {
		return new ContentStreamProvider();
	}

	/**
	 * Input stream factory method.
	 * @return An new input stream to the source content for this object.
	 * @throws IOException if there is an I/O error creating the input stream.
	 */
	protected abstract InputStream createInputStream() throws IOException;

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns the hash code of the key.
	 * @see #getKey()
	 */
	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation determines equality based on the value of the key.
	 * @see #getKey()
	 */
	@Override
	public boolean equals(@Nullable final Object object) {
		if(this == object) {
			return true;
		}
		if(!(object instanceof S3DeployObject)) {
			return false;
		}
		return getKey().equals(((S3DeployObject)object).getKey());
	}

	/**
	 * Implementation of an AWS content stream provider for an S3 deploy object.
	 * @implSpec This implementation calls {@link AbstractS3DeployObject#createInputStream()} to create new input streams as needed.
	 * @author Garret Wilson
	 * @see software.amazon.awssdk.core.internal.sync.FileContentStreamProvider
	 */
	protected class ContentStreamProvider implements software.amazon.awssdk.http.ContentStreamProvider {

		private InputStream inputStream = null;

		/**
		 * {@inheritDoc}
		 * @implSpec This implementation closes any previous stream that has been opened by this instance.
		 * @throws UncheckedIOException if an I/O error occurred closing an existing input stream or opening a new one.
		 * @see AbstractS3DeployObject#createInputStream()
		 */
		@Override
		public final InputStream newStream() {
			try {
				if(inputStream != null) {
					inputStream.close();
					inputStream = null;
				}
				inputStream = createInputStream();
				return inputStream;
			} catch(final IOException ioException) {
				throw new UncheckedIOException(ioException);
			}
		}

	}

}
