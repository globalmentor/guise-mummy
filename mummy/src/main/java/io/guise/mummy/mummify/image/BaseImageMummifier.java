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
import static org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import javax.annotation.*;

import org.apache.commons.imaging.*;
import org.apache.commons.imaging.common.bytesource.ByteSource;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoAscii;
import org.apache.commons.imaging.formats.tiff.write.*;

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
 * @implSpec This implementation uses the <a href="https://github.com/drewnoakes/metadata-extractor">metadata-extractor</a> library for reading metadata.
 * @implSpec This implementation provides a way to write metadata to an image using the <a href="https://commons.apache.org/proper/commons-imaging/">Apache
 *           Commons Imaging</a> library.
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

	@SuppressWarnings("unused")
	private static final TagInfoAscii EXIF_TAG_XP_TITLE = new TagInfoAscii("XPTitle", 0x9C9B, -1, TiffDirectoryType.EXIF_DIRECTORY_IFD0); //XPTitle (0x9C9B)
	private static final TagInfoAscii EXIF_TAG_IMAGE_DESCRIPTION = new TagInfoAscii("ImageDescription", 0x010E, -1, TiffDirectoryType.EXIF_DIRECTORY_IFD0); //ImageDescription (270, 0x010E)
	private static final TagInfoAscii EXIF_TAG_COPYRIGHT = new TagInfoAscii("Copyright", 0x8298, -1, TiffDirectoryType.EXIF_DIRECTORY_IFD0); //Copyright (33432, 0x8298)

	/**
	 * Adds appropriate metadata to an existing image. Any exiting metadata is replaced.
	 * @implSpec If software identification is given, it is added as an Exif <code>Software</code> (<code>0x0131</code>) tag.
	 * @implNote This implementation ignores the {@link Artifact#PROPERTY_HANDLE_TITLE} property because Apache Commons Imaging writes corrupted
	 *           <code>XPTitle</code> values; see <a href="https://issues.apache.org/jira/browse/IMAGING-281">IMAGING-281: Simple Exif XPTitle corrupted.</a>
	 * @implSpec This implementation only supports writing Exif metadata to JPEG images.
	 * @implSpec This implementation uses <a href="https://commons.apache.org/proper/commons-imaging/">Apache Commons Imaging</a>.
	 * @param metadata The description containing the metadata to add.
	 * @param byteSource The byte source containing the processed image.
	 * @param outputStream The output stream for writing the image with added metadata.
	 * @param software A string identifying the software generating or updating the image, or <code>null</code> if no software information should be added.
	 * @throws IOException if there is an I/O error adding the metadata.
	 * @see <a href="http://www.hanhuy.com/pfn/java-image-thumbnail-comparison">A comparison of Java image thumbnailing techniques</a>
	 * @see <a href="https://www.universalwebservices.net/web-programming-resources/java/adjust-jpeg-image-compression-quality-when-saving-images-in-java/">Adjust
	 *      JPEG image compression quality when saving images in Java</a>
	 */
	protected static void addImageMetadata(@Nonnull final UrfResourceDescription metadata, @Nonnull final ByteSource byteSource,
			@Nonnull final OutputStream outputStream, @Nullable final String software) throws IOException {
		try {
			final TiffOutputSet tiffOutputSet = new TiffOutputSet();
			final TiffOutputDirectory exifDirectory = tiffOutputSet.getOrCreateRootDirectory(); //getOrCreateExifDirectory() prevents metadata-extractor from seeing values
			//XPTitle (0x9C9B)
			//TODO bring back when IMAGING-281 is fixed
			//			metadata.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_TITLE)
			//					.ifPresent(throwingConsumer(title -> exifDirectory.add(EXIF_XP_TITLE_TAG_INFO, title.toString())));
			//ImageDescription (270, 0x010E)
			metadata.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_DESCRIPTION)
					.ifPresent(throwingConsumer(description -> exifDirectory.add(EXIF_TAG_IMAGE_DESCRIPTION, description.toString())));
			//Copyright (33432, 0x8298)
			metadata.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_COPYRIGHT)
					.ifPresent(throwingConsumer(copyright -> exifDirectory.add(EXIF_TAG_COPYRIGHT, copyright.toString())));
			//Software (0x0131)
			if(software != null) {
				exifDirectory.add(EXIF_TAG_SOFTWARE, software);
			}
			new ExifRewriter().updateExifMetadataLossy(byteSource, outputStream, tiffOutputSet);
		} catch(final ImageReadException | ImageWriteException imageIOException) {
			throw new IOException(imageIOException.getMessage(), imageIOException);
		}
	}

	/**
	 * Temporary output stream that collects content temporarily in memory and allows easy conversion to an output stream, as well as to an Apache Commons Imaging
	 * {@link ByteSource}.
	 * @apiNote This class is necessary because {@link ByteArrayOutputStream} does not provide a way to get an input stream without copying all the collected
	 *          bytes.
	 * @implNote This implementation out of necessity accesses the protected variables <code>buf</code> and <code>count</code> in the parent
	 *           {@link ByteArrayOutputStream}.
	 * @author Garret Wilson
	 * @see <a href="https://stackoverflow.com/q/1225909">Most efficient way to create InputStream from OutputStream</a>
	 * @see #toByteArray()
	 * @see #toByteSource()
	 */
	protected static class TempOutputStream extends ByteArrayOutputStream {

		/**
		 * Returns an input stream to the collected byte content. The returned input stream must not be used concurrently with this output stream.
		 * @return An input stream to the temporary content.
		 */
		public synchronized InputStream toInputStream() {
			return new ByteArrayInputStream(this.buf, 0, this.count);
		}

		/**
		 * Returns a byte source to the collected byte content. The returned byte source must not be used concurrently with this output stream.
		 * @return An byte source to the temporary content.
		 */
		public synchronized ByteSource toByteSource() {
			return new BufByteSource();
		}

		/**
		 * A specialized byte source created directly from the temporary byte array <code>buf</code> in the parent {@link ByteArrayOutputStream}.
		 * @apiNote This implementation is needed because the current implementation of {@link org.apache.commons.imaging.common.bytesource.ByteSourceArray} does
		 *          not provide a constructor that specifies the byte length, assuming the entire byte array is used.
		 * @implNote Code in implementation modified from that in <code>org.apache.commons.imaging.common.bytesource.ByteSourceArray</code>.
		 * @author Garret Wilson
		 * @see <a href="https://issues.apache.org/jira/browse/IMAGING-280">IMAGING-280: Length specifier for ByteSourceArray.</a>
		 */
		private class BufByteSource extends ByteSource {

			public BufByteSource() {
				super(null);
			}

			@Override
			public InputStream getInputStream() {
				return TempOutputStream.this.toInputStream();
			}

			@Override
			public byte[] getBlock(final long startLong, final int length) throws IOException {
				final int start = (int)startLong;
				if((start < 0) || (length < 0) || (start + length < 0) || (start + length > TempOutputStream.this.count)) {
					throw new IOException(
							"Could not read block (block start: " + start + ", block length: " + length + ", data length: " + TempOutputStream.this.count + ").");
				}
				final byte[] result = new byte[length];
				System.arraycopy(TempOutputStream.this.buf, start, result, 0, length);
				return result;
			}

			@Override
			public long getLength() {
				return TempOutputStream.this.count;
			}

			@Override
			public byte[] getAll() throws IOException {
				return TempOutputStream.this.toByteArray(); //copying is required because a standalone array is requested
			}

			@Override
			public String getDescription() {
				return TempOutputStream.this.count + " byte array";
			}

		}

	}

}
