/*
 * Copyright © 2020 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.guise.mummy.deploy.aws;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import com.globalmentor.net.MediaType;

import io.urf.URF.Handle;
import io.urf.vocab.content.Content;
import software.amazon.awssdk.http.ContentStreamProvider;

/**
 * Encapsulation of an object, such as a Guise Mummy artifact, to be deployed to S3.
 * <p>
 * Equality of deploy objects is determined solely by the value of {@link #getKey()}.
 * </p>
 * @author Garret Wilson
 */
public interface S3DeployObject {

	/**
	 * The handle of the content fingerprint tag used as S3 object metadata.
	 * @see Content#FINGERPRINT_PROPERTY_TAG
	 */
	public static final String METADATA_CONTENT_FINGERPRINT = Handle.findFromTag(Content.FINGERPRINT_PROPERTY_TAG).orElseThrow(AssertionError::new);

	/** @return The S3 key representing the deployment path of the object in the bucket. */
	public String getKey();

	/** @return The S3 metadata to be deployed along with the object; may be empty. */
	public Map<String, String> getMetadata();

	/**
	 * Returns any known fingerprint for the object.
	 * @apiNote This method is not meant to calculate a fingerprint; it is meant to return any official fingerprint value that has already been calculated.
	 * @return Any known fingerprint for the object.
	 */
	public Optional<byte[]> findFingerprint();

	/**
	 * Returns the size of the content.
	 * @return The length of the content to be deployed for this object.
	 * @throws IOException if there is an I/O error retrieving the content length.
	 */
	public long getContentLength() throws IOException;

	/**
	 * Returns the full Internet media type of the object to be deployed for the purposes of S3.
	 * @apiNote If a suitable content type cannot be determined, this method returns the string form of {@link MediaType#APPLICATION_OCTET_STREAM_MEDIA_TYPE}.
	 * @return The content type of the object as a string for deployment to S3.
	 */
	public String getContentType();

	/**
	 * Creates a new provider for retrieving streams to the object content.
	 * @return A strategy for retrieving the content of the object to deploy.
	 */
	public ContentStreamProvider createContentStreamProvider();

}
