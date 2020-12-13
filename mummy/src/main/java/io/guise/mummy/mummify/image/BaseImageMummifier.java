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

import static com.adobe.internal.xmp.XMPConst.*;
import static com.globalmentor.io.Images.*;
import static com.globalmentor.io.Paths.*;
import static java.nio.file.Files.*;
import static java.util.stream.Collectors.*;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import javax.annotation.*;

import com.adobe.internal.xmp.*;
import com.adobe.internal.xmp.properties.XMPProperty;
import com.drew.imaging.*;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.*;
import com.drew.metadata.iptc.*;
import com.drew.metadata.xmp.XmpDirectory;
import com.globalmentor.io.*;
import com.globalmentor.net.ContentType;
import com.globalmentor.vocab.dcmi.DCMES;

import io.guise.mummy.*;
import io.guise.mummy.mummify.*;
import io.urf.URF.Handle;
import io.urf.model.UrfResourceDescription;

/**
 * Base image mummifier that handles common image needs such as metadata extraction.
 * @implSpec This implementation uses the filename extensions and image media types defined in {@link Images#MEDIA_TYPES_BY_FILENAME_EXTENSION}.
 * @apiNote Metadata support is potentially available for more images types than are supported for processing, so metadata-related logic is placed in this
 *          common base class.
 * @author Garret Wilson
 */
public abstract class BaseImageMummifier extends AbstractFileMummifier implements ImageMummifier {

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
		return findFilenameExtension(sourceFile).map(Filenames.Extensions::normalize).filter(getSupportedFilenameExtensions()::contains)
				.map(MEDIA_TYPES_BY_FILENAME_EXTENSION::get); //the media type filename extensions are already in normal form
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
	 * {@inheritDoc}
	 * @implSpec This implementation opens an input stream to the given file and then extract the source metadata by calling
	 *           {@link #loadSourceMetadata(MummyContext, InputStream, String)}.
	 */
	@Override
	protected List<Map.Entry<URI, Object>> loadSourceMetadata(@Nonnull final MummyContext context, @Nonnull final Path sourceFile) throws IOException {
		try (final InputStream inputStream = new BufferedInputStream(newInputStream(sourceFile))) {
			return loadSourceMetadata(context, inputStream, sourceFile.toString());
		}
	}

	/**
	 * Loads metadata stored in the source file itself.
	 * @implSpec This implementation loads metadata using {@link ImageMetadataReader}.
	 * @implSpec This implementation does not return entries with duplicate keys.
	 * @param context The context of static site generation.
	 * @param inputStream The input stream from which to to load the source metadata.
	 * @param name The full identifier of the source, such as a path or URL.
	 * @return Metadata stored in the source file being mummified, consisting of resolved URI tag names and values. The name-value pairs may have duplicate names.
	 * @throws IOException if there is an I/O error retrieving the metadata, including incorrectly formatted metadata.
	 */
	protected List<Map.Entry<URI, Object>> loadSourceMetadata(@Nonnull final MummyContext context, @Nonnull final InputStream inputStream,
			@Nonnull final String name) throws IOException {
		String title = null;
		String description = null;
		String copyright = null;
		final Metadata imageMetadata;
		try {
			imageMetadata = ImageMetadataReader.readMetadata(inputStream);
		} catch(final ImageProcessingException imageProcessingException) {
			throw new IOException(imageProcessingException.getMessage(), imageProcessingException);
		}
		//XMP
		final XmpDirectory xmpDirectory = imageMetadata.getFirstDirectoryOfType(XmpDirectory.class);
		if(xmpDirectory != null) {
			final XMPMeta xmpMeta = xmpDirectory.getXMPMeta();
			try {
				//dc:title
				final XMPProperty dcTitle = xmpMeta.getLocalizedText(NS_DC, DCMES.TERM_TITLE.getName(), null, X_DEFAULT);
				if(dcTitle != null) {
					title = dcTitle.getValue();
				}
				//dc:description
				final XMPProperty dcDescription = xmpMeta.getLocalizedText(NS_DC, DCMES.TERM_DESCRIPTION.getName(), null, X_DEFAULT);
				if(dcDescription != null) {
					description = dcDescription.getValue();
				}
				//dc:rights
				final XMPProperty dcRights = xmpMeta.getLocalizedText(NS_DC, DCMES.TERM_RIGHTS.getName(), null, X_DEFAULT);
				if(dcRights != null) {
					copyright = dcRights.getValue();
				}
			} catch(final XMPException xmpException) {
				throw new IOException(xmpException.getMessage(), xmpException);
			}
		}
		//IPTC
		final IptcDirectory iptcDirectory = imageMetadata.getFirstDirectoryOfType(IptcDirectory.class);
		if(iptcDirectory != null) {
			final IptcDescriptor iptcDescriptor = new IptcDescriptor(iptcDirectory);
			//ObjectName (IIM 2:05, 0x205)
			if(title == null) {
				title = iptcDescriptor.getObjectNameDescription();
			}
			//Caption (IIM 2:120, 0x0278)
			if(description == null) {
				description = iptcDescriptor.getCaptionDescription();
			}
			//CopyrightNotice (IIM 2:116, 0x0274)
			if(copyright == null) {
				copyright = iptcDescriptor.getCopyrightNoticeDescription();
			}
		}
		//Exif
		final ExifIFD0Directory ifd0Directory = imageMetadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		if(ifd0Directory != null) {
			final ExifIFD0Descriptor exifIFD0Descriptor = new ExifIFD0Descriptor(ifd0Directory);
			//XPTitle (0x9C9B)
			if(title == null) {
				title = exifIFD0Descriptor.getWindowsTitleDescription();
			}
			//ImageDescription (270, 0x010E)
			if(description == null) {
				description = ifd0Directory.getString(ExifIFD0Directory.TAG_IMAGE_DESCRIPTION);
			}
			//Copyright (33432, 0x8298)
			if(copyright == null) {
				copyright = ifd0Directory.getString(ExifIFD0Directory.TAG_COPYRIGHT);
			}
		}
		final List<Map.Entry<URI, Object>> sourceMetadata = new ArrayList<>();
		if(title != null) {
			sourceMetadata.add(Map.entry(Handle.toTag(Artifact.PROPERTY_HANDLE_TITLE), title));
		}
		if(description != null) {
			sourceMetadata.add(Map.entry(Handle.toTag(Artifact.PROPERTY_HANDLE_DESCRIPTION), description));
		}
		if(copyright != null) {
			sourceMetadata.add(Map.entry(Handle.toTag(Artifact.PROPERTY_HANDLE_COPYRIGHT), copyright));
		}
		return sourceMetadata;
	}

}
