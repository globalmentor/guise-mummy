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

import static com.globalmentor.util.Optionals.*;
import static java.util.Objects.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.annotation.*;

import io.guise.mummy.Artifact;
import io.urf.vocab.content.Content;
import software.amazon.awssdk.core.internal.util.Mimetype;
import software.amazon.awssdk.core.sync.RequestBody;

/**
 * An S3 object for deploying a Guise Mummy artifact.
 * @author Garret Wilson
 * @see Artifact
 */
public class S3ArtifactDeployObject extends AbstractS3DeployObject {

	private final Artifact artifact;

	/** @return The artifact with the contents to be deployed in the bucket as the object. */
	public Artifact getArtifact() {
		return artifact;
	}

	/**
	 * Constructor.
	 * @param key The S3 key representing the deployment path of the object in the bucket.
	 * @param artifact The artifact with the contents to be deployed in the bucket as the object.
	 */
	public S3ArtifactDeployObject(@Nonnull final String key, @Nonnull final Artifact artifact) {
		super(key);
		this.artifact = requireNonNull(artifact);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation includes returns the {@link Content#FINGERPRINT_PROPERTY_TAG} property of the resource description if present.
	 * @see Content#FINGERPRINT_PROPERTY_TAG
	 */
	@Override
	public Optional<byte[]> findFingerprint() {
		return filterAsInstance(getArtifact().getResourceDescription().findPropertyValue(Content.FINGERPRINT_PROPERTY_TAG), byte[].class); //TODO improve URF to use immutable byte string
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns the size of the artifact's target file.
	 * @see Artifact#getTargetPath()
	 */
	@Override
	public long getContentLength() throws IOException {
		return Files.size(getArtifact().getTargetPath());
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns the {@link Content#TYPE_PROPERTY_TAG} property of the artifact resource description if present; otherwise returns
	 *           {@link Mimetype#getMimetype(Path)}.
	 * @implNote Retrieving a default content type from {@link Mimetype#getMimetype(Path)} is equivalent to what happens when {@link RequestBody#fromFile(Path)}
	 *           is called when deploying an object directly from a file.
	 * @see Content#TYPE_PROPERTY_TAG
	 */
	@Override
	public String getContentType() {
		return getArtifact().getResourceDescription().findPropertyValue(Content.TYPE_PROPERTY_TAG).map(Object::toString)
				.orElseGet(() -> Mimetype.getInstance().getMimetype(getArtifact().getTargetPath()));
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation creates a new input stream to the artifact's target file.
	 * @see Artifact#getTargetPath()
	 */
	@Override
	protected InputStream createInputStream() throws IOException {
		return Files.newInputStream(getArtifact().getTargetPath());
	}

}
