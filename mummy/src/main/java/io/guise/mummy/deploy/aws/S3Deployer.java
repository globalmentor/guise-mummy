/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import java.io.IOException;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.collections.*;
import com.globalmentor.net.ContentType;

import io.clogr.Clogged;
import io.guise.mummy.*;
import io.guise.mummy.deploy.Deployer;
import io.urf.vocab.content.Content;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;

/**
 * Deploys a site to AWS S3.
 * @author Garret Wilson
 */
public class S3Deployer implements Deployer, Clogged {

	private final String bucket;

	/** @return The destination S3 bucket for deployment. */
	public String getBucket() {
		return bucket;
	}

	private final S3Client s3Client;

	/** @return The client for connecting to S3. */
	protected S3Client getS3Client() {
		return s3Client;
	}

	private final ReverseMap<Artifact, String> artifactKeys = new DecoratorReverseMap<>(new LinkedHashMap<>(), new HashMap<>());

	/**
	 * Bucket constructor. The default region will be used (usually configured externally).
	 * @param bucket The bucket into which the site should be deployed.
	 */
	public S3Deployer(@Nonnull String bucket) {
		this(null, bucket);
	}

	/**
	 * Region and bucket constructor.
	 * @param region The AWS region, or <code>null</code> if the default region should be used (usually configured externally).
	 * @param bucket The bucket into which the site should be deployed.
	 */
	public S3Deployer(@Nullable final Region region, @Nonnull String bucket) {
		this.bucket = requireNonNull(bucket);
		final S3ClientBuilder s3ClientBuilder = S3Client.builder();
		if(region != null) {
			s3ClientBuilder.region(region);
		}
		s3Client = s3ClientBuilder.build();
	}

	@Override
	public void deploy(@Nonnull final MummyContext context, @Nonnull Artifact rootArtifact) throws IOException {

		//#plan
		plan(context, rootArtifact);

		//#put
		put(context);

		//#prune
		prune(context);

	}

	/**
	 * Plans deployment of a site.
	 * @param context The context of static site generation.
	 * @param rootArtifact The root artifact of the site being deployed.
	 * @throws IOException if there is an I/O error during site deployment planning.
	 */
	protected void plan(@Nonnull final MummyContext context, @Nonnull Artifact rootArtifact) throws IOException {
		plan(context, rootArtifact, rootArtifact);
	}

	/**
	 * Plans deployment of a site for an artifact and its comprised artifacts.
	 * @param context The context of static site generation.
	 * @param rootArtifact The root artifact of the site being deployed.
	 * @param artifact The current artifact for which deployment is being planned.
	 * @throws IOException if there is an I/O error during site deployment planning.
	 */
	protected void plan(@Nonnull final MummyContext context, @Nonnull Artifact rootArtifact, @Nonnull Artifact artifact) throws IOException {
		if(!(artifact instanceof DirectoryArtifact)) { //don't deploy anything for directories TODO improve semantics, especially after the root artifact type changes; maybe use CollectionArtifact
			final String key = context.relativizeTargetReference(rootArtifact, artifact).toString();
			getLogger().debug("Planning deployment for artifact {}, S3 key `{}`.", artifact, key);
			artifactKeys.put(artifact, key);
		}
		if(artifact instanceof CompositeArtifact) {
			for(final Artifact comprisedArtifact : (Iterable<Artifact>)((CompositeArtifact)artifact).comprisedArtifacts()::iterator) {
				plan(context, rootArtifact, comprisedArtifact);
			}
		}
	}

	/**
	 * Transfers content for deployment.
	 * @apiNote This is the main deployment method, which actually deploys content.
	 * @param context The context of static site generation.
	 * @throws IOException if there is an I/O error during putting.
	 */
	protected void put(@Nonnull final MummyContext context) throws IOException {
		final S3Client s3Client = getS3Client();
		final String bucket = getBucket();
		for(final Map.Entry<Artifact, String> artifactKeyEntry : artifactKeys.entrySet()) {
			final Artifact artifact = artifactKeyEntry.getKey();
			final String key = artifactKeyEntry.getValue();
			getLogger().debug("Deploying artifact {} to S3 key `{}`.", artifact, key);
			final PutObjectRequest.Builder putBuilder = PutObjectRequest.builder().bucket(bucket).key(key);
			//set content-type if found
			Content.findContentType(artifact.getResourceDescription()).map(ContentType::toString).ifPresent(putBuilder::contentType);
			s3Client.putObject(putBuilder.build(), RequestBody.fromFile(artifact.getTargetPath()));
		}
	}

	/**
	 * Prunes any objects that don't exist in the site.
	 * @apiNote This process can occur even when actual putting is being performed concurrently, as existing objects that are in the site are left undisturbed.
	 *          There is no need to determine if the existing object is out of date, as it will be replaced if it hasn't been already. Only files no longer in the
	 *          site are removed.
	 * @param context The context of static site generation.
	 * @throws IOException if there is an I/O error during pruning.
	 */
	protected void prune(@Nonnull final MummyContext context) throws IOException {
		final S3Client s3Client = getS3Client();
		final String bucket = getBucket();
		ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder().bucket(bucket).build();
		ListObjectsV2Response listObjectsResponse;
		do {
			listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);
			for(final S3Object s3Object : listObjectsResponse.contents()) {
				final String key = s3Object.key();
				if(!artifactKeys.containsValue(key)) { //if this object isn't in our site, delete it
					getLogger().debug("Pruning S3 object `{}`.", key);
					s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
				}
			}
			listObjectsRequest = ListObjectsV2Request.builder().bucket(bucket).continuationToken(listObjectsResponse.nextContinuationToken()).build();
		} while(listObjectsResponse.isTruncated());
	}

}
