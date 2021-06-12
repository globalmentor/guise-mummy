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

import static com.globalmentor.net.URIs.*;
import static java.util.Objects.*;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.io.EmptyInputStream;
import com.globalmentor.net.MediaType;

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

	/**
	 * Determines if this object redirect is one that requires a routing rule.
	 * @implSpec A routing rule is required if the key to redirect is a collection, for two reasons: first, depending on the target, the entire key prefix may
	 *           need to be replaced (which can only be done via routing rules); and secondly (most importantly), S3 does not handle object redirects for
	 *           collections (ending in a slash, e.g. <code>foo/bar/</code>) anyway.
	 * @return <code>true</code> if this redirect can only be specified properly using a routing rule.
	 */
	public boolean isRoutingRuleRequired() {
		return isCollectionPath(getKey());
	}

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
	 * The metadata key indicating the website redirect location for redirect objects.
	 * @apiNote This key is provided here to serve as part of the fingerprint; actual designation of the redirect that ultimately results in this key being set
	 *          for an object is performed elsewhere.
	 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/how-to-page-redirect.html">AWS S3 configuring a webpage redirect</a>
	 */
	private static final String METADATA_AMAZON_WEBSITE_REDIRECT_LOCATION = "x-amz-website-redirect-location";

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation includes returns a fingerprint including the redirect target key.
	 * @see Mummifier#FINGERPRINT_ALGORITHM
	 */
	@Override
	public Optional<byte[]> findFingerprint() {
		//Along with the redirect target key (most important), include the class name and the redirect header in the digest (arbitrary extra information)
		//so as to lower the (already extremely low) risk of some text file containing the same content.
		return Optional.of(Mummifier.FINGERPRINT_ALGORITHM.digest(getClass().getName(), METADATA_AMAZON_WEBSITE_REDIRECT_LOCATION, getRedirectTargetKey()));
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
		return MediaType.APPLICATION_OCTET_STREAM_MEDIA_TYPE.toString();
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
