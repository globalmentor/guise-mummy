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

import static java.util.Objects.*;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.io.EmptyInputStream;
import com.globalmentor.net.ContentType;

import io.guise.mummy.Artifact;
import io.guise.mummy.mummify.Mummifier;

/**
 * An S3 object for redirecting a key to an artifact at another key.
 * @author Garret Wilson
 * @see Artifact
 */
public class S3ArtifactRedirectDeployObject extends AbstractS3DeployObject {

	private final String redirectTargetKey;

	/** @return The S3 key representing the deployment path in the bucket serving as the target of the redirect. */
	public String getRedirectTargetKey() {
		return redirectTargetKey;
	}

	private final Artifact redirectTargetArtifact;

	/** @return The target artifact of the redirect. */
	public Artifact getTargetArtifact() {
		return redirectTargetArtifact;
	}

	//TODO implement ` x-amz-website-redirect-location` metadata

	/**
	 * Constructor.
	 * @param key The S3 key representing the deployment path of the object in the bucket.
	 * @param redirectTargetKey The S3 key representing the deployment path in the bucket serving as the target of the redirect.
	 * @param redirectTargetArtifact The target artifact of the redirect.
	 */
	public S3ArtifactRedirectDeployObject(@Nonnull final String key, @Nonnull final String redirectTargetKey, @Nonnull final Artifact redirectTargetArtifact) {
		super(key);
		this.redirectTargetKey = requireNonNull(redirectTargetKey);
		this.redirectTargetArtifact = requireNonNull(redirectTargetArtifact);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation includes returns a fingerprint of the redirect target key.
	 * @see Mummifier#FINGERPRINT_ALGORITHM
	 */
	@Override
	public Optional<byte[]> findFingerprint() {
		return Optional.of(Mummifier.FINGERPRINT_ALGORITHM.digest(getRedirectTargetKey()));
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns the <code>0</code>, as redirect objects have no size.
	 */
	@Override
	public long getContentLength() throws IOException {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns <code>"application/octet-stream"</code>.
	 */
	@Override
	public String getContentType() {
		return ContentType.APPLICATION_OCTET_STREAM_CONTENT_TYPE.toString();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns an empty input stream.
	 */
	@Override
	protected InputStream createInputStream() throws IOException {
		return new EmptyInputStream();
	}

}
