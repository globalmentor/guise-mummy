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

import static io.guise.mummy.GuiseMummy.*;
import static java.util.Objects.*;

import java.io.IOException;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.collections.*;
import com.globalmentor.net.*;
import com.globalmentor.text.StringTemplate;

import io.clogr.Clogged;
import io.guise.mummy.*;
import io.guise.mummy.deploy.Deployer;
import io.urf.vocab.content.Content;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;

/**
 * Deploys a site to AWS S3.
 * @author Garret Wilson
 */
public class S3Deployer implements Deployer, Clogged {

	public static final String SITE_CONFIG_KEY_DEPLOYMENT_REGION = "deploy.target.region";
	public static final String SITE_CONFIG_KEY_DEPLOYMENT_BUCKET = "deploy.target.bucket";

	/**
	 * The policy template for setting a bucket to public read access. There is one parameter:
	 * <ol>
	 * <li>bucket</li>
	 * </ol>
	 */
	protected static final StringTemplate POLICY_TEMPLATE_PUBLIC_READ_GET_OBJECT = StringTemplate.builder().text( //@formatter:off
			"{" + 
			"	\"Version\": \"2012-10-17\"," + 
			"	\"Statement\": [{" + 
			"		\"Sid\": \"PublicReadGetObject\"," + 
			"		\"Effect\": \"Allow\"," + 
			"		\"Principal\": \"*\"," + 
			"		\"Action\": [\"s3:GetObject\"]," + 
			"		\"Resource\": [\"arn:aws:s3:::").parameter(StringTemplate.STRING_PARAMETER).text("/*\"]" + 
			"	}]" + 
			"}").build();	//@formatter:on

	private final Region region;

	/** @return The specified region, if any; missing if the default region should be used (usually configured externally). */
	public Optional<Region> findRegion() {
		return Optional.ofNullable(region);
	}

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
	 * Context constructor. Retrieves the bucket and optional region from the site configuration.
	 * @param context The context of static site generation.
	 */
	public S3Deployer(@Nonnull final MummyContext context) {
		this(context.getConfiguration().findString(SITE_CONFIG_KEY_DEPLOYMENT_REGION).map(Region::of).orElse(null),
				context.getConfiguration().getString(SITE_CONFIG_KEY_DEPLOYMENT_BUCKET));
	}

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
		this.region = region;
		this.bucket = requireNonNull(bucket);
		final S3ClientBuilder s3ClientBuilder = S3Client.builder();
		if(region != null) {
			s3ClientBuilder.region(region);
		}
		s3Client = s3ClientBuilder.build();
	}

	@Override
	public void prepare(final MummyContext context) throws IOException {
		getLogger().info("Preparing to deploy to AWS region `{}` S3 bucket `{}`.", findRegion(), bucket);
		final S3Client s3Client = getS3Client();

		//create bucket
		final String bucket = getBucket();
		final boolean bucketExists = bucketExists(bucket);
		getLogger().debug("Bucket `{}` exists? {}.", bucket, bucketExists);
		final boolean bucketHasPolicy;
		final boolean bucketHasWebsiteConfiguration;
		if(bucketExists) { //if the bucket exists, see if it has a policy
			bucketHasPolicy = bucketHasPolicy(bucket);
			bucketHasWebsiteConfiguration = bucketHasWebsiteConfiguration(bucket);
		} else { //create the bucket if it doesn't exist
			getLogger().info("Creating S3 bucket `{}` in AWS region `{}`.", bucket, findRegion().orElse(Region.US_EAST_1)); //default region as per the API
			final CreateBucketConfiguration.Builder createBucketConfigurationBuilder = CreateBucketConfiguration.builder();
			findRegion().ifPresent(region -> createBucketConfigurationBuilder.locationConstraint(region.id()));
			s3Client.createBucket(builder -> builder.bucket(bucket).createBucketConfiguration(createBucketConfigurationBuilder.build()));
			bucketHasPolicy = false; //the bucket doesn't have a policy yet, as we just created it
			bucketHasWebsiteConfiguration = false; //the bucket isn't configured for a web site, because it is new
		}

		//set bucket policy
		if(!bucketHasPolicy) { //if the bucket doesn't already have a policy (leave any existing policy alone)
			getLogger().info("Setting policy of S3 bucket `{}` to public.", bucket);
			s3Client.putBucketPolicy(builder -> builder.bucket(bucket).policy(POLICY_TEMPLATE_PUBLIC_READ_GET_OBJECT.apply(bucket)));
		}

		//configure bucket for web site
		if(!bucketHasWebsiteConfiguration) {
			getLogger().info("Configuring S3 bucket `{}` for web site access.", bucket);
			//TODO use some separate configuration access for the "content name"
			final boolean isNameBare = context.getConfiguration().findBoolean(CONFIG_KEY_PAGE_NAMES_BARE).orElse(false);
			final String indexDocumentSuffix = isNameBare ? "index" : "index.html"; //TODO get from configuration
			final IndexDocument indexDocument = IndexDocument.builder().suffix(indexDocumentSuffix).build();
			s3Client.putBucketWebsite(builder -> builder.bucket(bucket).websiteConfiguration(configuration -> configuration.indexDocument(indexDocument).build()));
		}
	}

	/**
	 * Determines whether the bucket exists.
	 * @param bucket The bucket to check.
	 * @return <code>true</code> if the bucket exists; otherwise <code>false</code>.
	 * @throws SdkException if some error occurred, such as insufficient permissions.
	 */
	protected boolean bucketExists(@Nonnull final String bucket) throws SdkException {
		try {
			getS3Client().headBucket(builder -> builder.bucket(bucket));
			return true;
		} catch(final NoSuchBucketException noSuchBucketException) {
			return false;
		}
	}

	/**
	 * Determines whether a bucket has a policy.
	 * @param bucket The bucket to check.
	 * @return <code>true</code> if the bucket has a policy; otherwise <code>false</code>.
	 * @throws SdkException if some error occurred, such as insufficient permissions.
	 */
	protected boolean bucketHasPolicy(@Nonnull final String bucket) throws SdkException {
		try {
			getS3Client().getBucketPolicy(builder -> builder.bucket(bucket));
			return true;
		} catch(final S3Exception s3Exception) {
			if(s3Exception.statusCode() == HTTP.SC_NOT_FOUND) {
				return false;
			}
			throw s3Exception;
		}
	}

	/**
	 * Determines whether a bucket has a web site configuration.
	 * @param bucket The bucket to check.
	 * @return <code>true</code> if the bucket has a web site configuration; otherwise <code>false</code>.
	 * @throws SdkException if some error occurred, such as insufficient permissions.
	 */
	protected boolean bucketHasWebsiteConfiguration(@Nonnull final String bucket) throws SdkException {
		try {
			getS3Client().getBucketWebsite(builder -> builder.bucket(bucket));
			return true;
		} catch(final S3Exception s3Exception) {
			if(s3Exception.statusCode() == HTTP.SC_NOT_FOUND) {
				return false;
			}
			throw s3Exception;
		}
	}

	@Override
	public void deploy(@Nonnull final MummyContext context, @Nonnull Artifact rootArtifact) throws IOException {

		getLogger().info("Deploying to AWS region `{}` S3 bucket `{}`.", findRegion(), bucket);

		//#plan
		plan(context, rootArtifact);

		//#put
		put(context);

		//#prune
		prune(context);

		//TODO catch AwsServiceException, SdkClientException, S3Exception; probably in each lower level; rethrow as I/O exception
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
			getLogger().info("Planning deployment for artifact {}, S3 key `{}`.", artifact, key);
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
			getLogger().info("Deploying artifact {} to S3 key `{}`.", artifact, key);
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
					getLogger().info("Pruning S3 object `{}`.", key);
					s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
				}
			}
			listObjectsRequest = ListObjectsV2Request.builder().bucket(bucket).continuationToken(listObjectsResponse.nextContinuationToken()).build();
		} while(listObjectsResponse.isTruncated());
	}

}
