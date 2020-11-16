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

package io.guise.mummy.mummify.image;

import static com.globalmentor.io.Images.*;
import static com.globalmentor.io.Paths.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.io.*;
import com.globalmentor.net.ContentType;

import io.guise.mummy.*;
import io.guise.mummy.mummify.*;
import io.urf.model.UrfResourceDescription;

/**
 * Base image mummifier that handles common image needs such as metadata extraction.
 * @implSpec This implementation uses the filename extensions and image media types defined in {@link Images#MEDIA_TYPES_BY_FILENAME_EXTENSION}.
 * @apiNote TODO Metadata support is potentially available for more images types than are supported for processing, so metadata-related logic is placed in this
 *          common base class.
 * @author Garret Wilson
 */
public abstract class BaseImageMummifier extends AbstractFileMummifier {

	private final Set<ContentType> supportedMediaTypes;

	/** @return The image media types supported by this mummifier. */
	protected Set<ContentType> getSupportedMediaTypes() {
		return supportedMediaTypes;
	}

	private final Set<String> supportedFilenameExtensions;

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns the filename extensions from {@link Images#MEDIA_TYPES_BY_FILENAME_EXTENSION} that are mapped to the supported media
	 *           types.
	 */
	@Override
	public Set<String> getSupportedFilenameExtensions() {
		return supportedFilenameExtensions;
	}

	/**
	 * Constructor.
	 * @param supportedMediaTypes The supported image media types; only base types (i.e. without parameters) are supported).
	 */
	public BaseImageMummifier(@Nonnull final Set<ContentType> supportedMediaTypes) {
		this.supportedMediaTypes = Set.copyOf(supportedMediaTypes);
		this.supportedFilenameExtensions = Images.MEDIA_TYPES_BY_FILENAME_EXTENSION.entrySet().stream()
				.filter(entry -> this.supportedMediaTypes.contains(entry.getValue())).map(Map.Entry::getKey).collect(toUnmodifiableSet());
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation uses the filename extensions to image media types mappings in {@link Images#MEDIA_TYPES_BY_FILENAME_EXTENSION}.
	 * @throws IllegalArgumentException if the file of the source file is not supported.
	 */
	@Override
	public Optional<ContentType> getArtifactMediaType(final MummyContext context, final Path sourceFile) throws IOException {
		return findFilenameExtension(sourceFile).filter(getSupportedFilenameExtensions()::contains).map(MEDIA_TYPES_BY_FILENAME_EXTENSION::get);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns a simple {@link OpaqueFileArtifact}.
	 */
	@Override
	protected Artifact createArtifact(final Path sourceFile, final Path outputFile, final UrfResourceDescription description) throws IOException {
		return new OpaqueFileArtifact(this, sourceFile, outputFile, description);
	}

	/**
	 * {@inheritDoc} TODO
	 */
	@Override
	protected List<Map.Entry<URI, Object>> loadSourceMetadata(@Nonnull MummyContext context, @Nonnull final Path sourceFile) throws IOException {
		return emptyList(); //TODO
	}

}
