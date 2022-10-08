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

package io.guise.mummy;

import static com.globalmentor.io.Paths.*;
import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.java.Objects.*;
import static java.util.Objects.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.*;

import com.globalmentor.io.Filenames;
import com.globalmentor.net.MediaType;

import io.urf.vocab.content.Content;

/**
 * Base functionality for implementing an artifact query.
 * @author Garret Wilson
 */
public abstract class BaseArtifactQuery implements ArtifactQuery {

	private Stream<Artifact> stream = null;

	private Comparator<Artifact> comparator;

	/**
	 * Sets the stream to provide the source of artifacts for later filtering an other operations. The stream can only be set once for the query.
	 * @param stream The stream source of artifacts.
	 * @throws IllegalStateException if the stream has already been set.
	 */
	protected void setStream(@Nonnull final Stream<Artifact> stream) {
		checkState(this.stream == null, "Query already initialized with artifact source stream.");
		this.stream = requireNonNull(stream);
	}

	/**
	 * Executes the query and returns an iterator to the artifacts.
	 * @implSpec This implementation does not yet support querying all artifacts; setting an initial set of artifacts via {@link #setStream(Stream)} is required.
	 * @throws IllegalStateException if the query has not yet been initialized with an artifact source stream.
	 */
	@Override
	public Iterator<Artifact> iterator() {
		checkState(this.stream != null, "Query has not been initialized by calling a `fromXXX()` method.");
		//from
		Stream<Artifact> stream = this.stream;
		//order by
		if(comparator != null) {
			stream = stream.sorted(comparator);
		}
		return stream.iterator();
	}

	//filter

	/**
	 * Adds an additional filtering of artifacts.
	 * @param predicate A predicate with which to filter the artifacts.
	 * @throws IllegalStateException if the query has not yet been initialized with an artifact source stream.
	 */
	protected void addFilter(@Nonnull final Predicate<? super Artifact> predicate) {
		checkState(this.stream != null, "Query has not been initialized by calling a `fromXXX()` method.");
		this.stream = this.stream.filter(predicate);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation checks the artifact resource property from {@link Artifact#getResourceDescription()} using the property
	 *           {@link Content#TYPE_PROPERTY_TAG}.
	 */
	@Override
	public ArtifactQuery filterContentType(final CharSequence contentTypeMatch) {
		addFilter(artifact -> artifact.getResourceDescription().findPropertyValue(Content.TYPE_PROPERTY_TAG).flatMap(asInstance(MediaType.class))
				.map(mediaType -> mediaType.matches(contentTypeMatch)).orElse(false));
		return this;
	}

	//order by

	/**
	 * Sets or adds a sorting order to the artifacts to be returned by the query. If no sorting has been specified, the given comparator indicates the order. If a
	 * sorting has already been specified, the given comparator indicates the subordinate (e.g. secondary) ordering.
	 * @param comparator The comparator specifying the sort ordering of the artifacts.
	 */
	@SuppressWarnings("unchecked")
	protected void addComparator(@Nonnull final Comparator<? super Artifact> comparator) {
		this.comparator = this.comparator == null ? (Comparator<Artifact>)requireNonNull(comparator) : this.comparator.thenComparing(comparator);
	}

	@Override
	public ArtifactQuery orderByName() {
		addComparator(Comparator.comparing(artifact -> findFilename(artifact.getTargetPath()).orElse(""), Filenames.comparator()));
		return this;
	}

	/**
	 * {@inheritDoc} This implementation adds a comparator using {@link #addComparator(Comparator)}.
	 */
	@Override
	public ArtifactQuery reversedOrder() {
		checkState(this.comparator != null, "Cannot reverse the order, as no order has been specified.");
		addComparator(this.comparator.reversed());
		return this;
	}

}
