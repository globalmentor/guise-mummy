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
import static com.globalmentor.java.Objects.*;
import static java.nio.file.Files.*;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.*;
import static org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.annotation.*;

import org.apache.commons.imaging.*;
import org.apache.commons.imaging.common.bytesource.ByteSource;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryType;
import org.apache.commons.imaging.formats.tiff.taginfos.*;
import org.apache.commons.imaging.formats.tiff.write.*;

import com.adobe.internal.xmp.*;
import com.adobe.internal.xmp.properties.XMPProperty;
import com.drew.imaging.*;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.*;
import com.drew.metadata.iptc.*;
import com.drew.metadata.xmp.XmpDirectory;
import com.globalmentor.io.*;
import com.globalmentor.net.MediaType;
import com.globalmentor.time.TimeZones;
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

	private final Set<MediaType> supportedMediaTypes;

	/** @return The image media types supported by this mummifier. */
	protected Set<MediaType> getSupportedMediaTypes() {
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
	public BaseImageMummifier(@Nonnull final Set<MediaType> supportedMediaTypes) {
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
	public Optional<MediaType> getArtifactMediaType(final MummyContext context, final Path sourceFile) throws IOException {
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

	private static final String XMP_CREATE_DATE_PROPERTY_NAME = "CreateDate";
	private static final String PHOTOSHOP_DATE_CREATED_PROPERTY_NAME = "DateCreated";

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
		String artist = null;
		String copyright = null;
		Instant createdAt = null;
		final Metadata imageMetadata;
		try {
			imageMetadata = ImageMetadataReader.readMetadata(inputStream);
		} catch(final ImageProcessingException imageProcessingException) {
			throw new IOException(imageProcessingException.getMessage(), imageProcessingException);
		}
		//XMP; see _XMP Specification Part 1 § 8.3 Dublin Core namespace_
		//XMP arrays are 1-based and do not throw an exception if the array index if invalid.
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
				//dc:creator
				final XMPProperty dcCreator = xmpMeta.getArrayItem(NS_DC, DCMES.TERM_CREATOR.getName(), 1); //first name is highest precedence
				if(dcCreator != null) {
					artist = dcCreator.getValue();
				}
				//dc:rights
				final XMPProperty dcRights = xmpMeta.getLocalizedText(NS_DC, DCMES.TERM_RIGHTS.getName(), null, X_DEFAULT);
				if(dcRights != null) {
					copyright = dcRights.getValue();
				}
				//photoshop:DateCreated
				final XMPDateTime photoshopDateCreated = xmpMeta.getPropertyDate(NS_PHOTOSHOP, PHOTOSHOP_DATE_CREATED_PROPERTY_NAME);
				if(photoshopDateCreated != null && photoshopDateCreated.hasDate()) { //ignore creation times without an indicated date
					createdAt = photoshopDateCreated.getCalendar().toInstant();
				} else {
					//xmp:CreateDate
					final XMPDateTime xmpCreateDate = xmpMeta.getPropertyDate(NS_XMP, XMP_CREATE_DATE_PROPERTY_NAME);
					if(xmpCreateDate != null && xmpCreateDate.hasDate()) { //ignore creation times without an indicated date
						createdAt = xmpCreateDate.getCalendar().toInstant();
					}
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
			//By-line (IIM 2:80, 0x0250)
			if(artist == null) {
				artist = iptcDescriptor.getByLineDescription();
			}
			//CopyrightNotice (IIM 2:116, 0x0274)
			if(copyright == null) {
				copyright = iptcDescriptor.getCopyrightNoticeDescription();
			}
			//DateCreated (IIM 2:55, 0x0237), TimeCreated (IIM 2:60, 0x023C)
			if(createdAt == null) {
				final Date dateTimeCreated = iptcDirectory.getDateCreated();
				if(dateTimeCreated != null) {
					createdAt = dateTimeCreated.toInstant();
				}
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
			//ImageDescription (0x010E)
			if(description == null) {
				description = ifd0Directory.getString(ExifIFD0Directory.TAG_IMAGE_DESCRIPTION);
			}
			//Artist (0x013B)
			if(artist == null) {
				artist = ifd0Directory.getString(ExifIFD0Directory.TAG_ARTIST);
			}
			//Copyright (0x8298)
			if(copyright == null) {
				copyright = ifd0Directory.getString(ExifIFD0Directory.TAG_COPYRIGHT);
			}
			final ExifSubIFDDirectory subIFDDirectory = imageMetadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			if(subIFDDirectory != null) {
				//DateTimeOriginal (0x9003), SubSecTimeOriginal (0x9291), OffsetTimeOriginal (0x9011) 
				if(createdAt == null) {
					final Date dateOriginal = subIFDDirectory.getDateOriginal(TimeZones.UTC);
					if(dateOriginal != null) {
						createdAt = dateOriginal.toInstant();
					}
				}
			}
		}
		final List<Map.Entry<URI, Object>> sourceMetadata = new ArrayList<>();
		if(title != null) {
			sourceMetadata.add(Map.entry(Handle.toTag(Artifact.PROPERTY_HANDLE_TITLE), title));
		}
		if(description != null) {
			sourceMetadata.add(Map.entry(Handle.toTag(Artifact.PROPERTY_HANDLE_DESCRIPTION), description));
		}
		if(artist != null) {
			sourceMetadata.add(Map.entry(Handle.toTag(Artifact.PROPERTY_HANDLE_ARTIST), artist));
		}
		if(copyright != null) {
			sourceMetadata.add(Map.entry(Handle.toTag(Artifact.PROPERTY_HANDLE_COPYRIGHT), copyright));
		}
		if(createdAt != null) {
			sourceMetadata.add(Map.entry(Handle.toTag(Artifact.PROPERTY_HANDLE_CREATED_AT), createdAt));
		}
		return sourceMetadata;
	}

	private static final TagInfoAscii EXIF_TAG_ARTIST = new TagInfoAscii("Artist", 0x013B, -1, TiffDirectoryType.EXIF_DIRECTORY_IFD0);
	private static final TagInfoShort EXIF_TAG_COLOR_SPACE = new TagInfoShort("ColorSpace", 0xA001, TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD);
	private static final TagInfoAscii EXIF_TAG_COPYRIGHT = new TagInfoAscii("Copyright", 0x8298, -1, TiffDirectoryType.EXIF_DIRECTORY_IFD0);
	private static final TagInfoAscii EXIF_TAG_DATE_TIME = new TagInfoAscii("DateTime", 0x0132, 20, TiffDirectoryType.EXIF_DIRECTORY_IFD0); //in IDFD0 unlike its other components
	private static final TagInfoAscii EXIF_TAG_IMAGE_DESCRIPTION = new TagInfoAscii("ImageDescription", 0x010E, -1, TiffDirectoryType.EXIF_DIRECTORY_IFD0);
	private static final TagInfoAscii EXIF_TAG_OFFSET_TIME = new TagInfoAscii("OffsetTimeOriginal", 0x9010, 7, TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD);
	private static final TagInfoAscii EXIF_TAG_OFFSET_TIME_ORIGINAL = new TagInfoAscii("OffsetTimeOriginal", 0x9011, 7,
			TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD);
	private static final TagInfoXpString EXIF_TAG_XP_TITLE = new TagInfoXpString("XPTitle", 0x9C9B, TiffDirectoryType.EXIF_DIRECTORY_IFD0);

	/** The value for the Exif <code>ColorSpace</code> (<code>0xA001</code>) indicating the sRGB color space. */
	private final static short EXIF_COLOR_SPACE_SRGB = 1;

	/**
	 * The formatter for writing the Exif <code>DateTime</code> (<code>0x0132</code>) and <code>DateTimeOriginal</code> (<code>0x9003</code>) instant value as
	 * prescribed in <cite>Exif 2.3.2 § 4.6.5 Exif IFD Attribute Information</cite>.
	 * <p>
	 * Note that this formatter does not assume any time zone, so the value being used for formatting must have already been resolved to some time zone.
	 * </p>
	 */
	static final DateTimeFormatter EXIF_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

	/**
	 * The formatter for writing the Exif <code>SubSecTime</code> (<code>0x9290</code>) and <code>SubSecTimeOriginal</code> (<code>0x9291</code>) subseconds value
	 * as prescribed in <cite>Exif 2.3.2 § 4.6.5 Exif IFD Attribute Information</cite>.
	 * @implNote This implementation truncates the value to a resolution of three digits as per the example in the specification, even though the prose seems to
	 *           indicate that more digits are allowed.
	 */
	static final DateTimeFormatter EXIF_SUB_SEC_TIME_FORMATTER = DateTimeFormatter.ofPattern("SSS");

	/**
	 * The string for representing UTC in the Exif <code>OffsetTime</code> (<code>0x9010</code>) <code>OffsetTimeOriginal</code> (<code>0x9011</code>) tag as
	 * prescribed in <cite>Exif 2.3.2 § 4.6.5 Exif IFD Attribute Information</cite>.
	 */
	static final String EXIF_OFFSET_TIME_UTC = "+00:00";

	/**
	 * Adds appropriate metadata to an existing image. Any exiting metadata is replaced.
	 * @implSpec This implementation supports the following Exif metadata:
	 *           <dl>
	 *           <dt>{@link Artifact#PROPERTY_HANDLE_TITLE}</dt>
	 *           <dd><code>XPTitle</code> (<code>0x9C9B</code>)</dd>
	 *           <dt>{@link Artifact#PROPERTY_HANDLE_DESCRIPTION}</dt>
	 *           <dd><code>ImageDescription</code> (<code>0x010E</code>)</dd>
	 *           <dt>{@link Artifact#PROPERTY_HANDLE_ARTIST}</dt>
	 *           <dd><code>Artist</code> (<code>0x013B</code>)</dd>
	 *           <dt>{@link Artifact#PROPERTY_HANDLE_COPYRIGHT}</dt>
	 *           <dd><code>Copyright</code> (<code>0x8298</code>)</dd>
	 *           <dt>{@link Artifact#PROPERTY_HANDLE_CREATED_AT}</dt>
	 *           <dd><code>DateTimeOriginal</code> (<code>0x9003</code>), <code>SubSecTimeOriginal</code> (<code>0x9291</code>), <code>OffsetTimeOriginal</code>
	 *           (<code>0x9011</code>)</dd>
	 *           </dl>
	 * @implSpec This implementation uses a resolution of three digits for <code>SubSecTime</code> (<code>0x9290</code>) and <code>SubSecTimeOriginal</code>
	 *           (<code>0x9291</code>).
	 * @implSpec If the sRGB color space is requested, it is added as an Exif <code>ColorSpace</code> (<code>0xA001</code>) tag with the value
	 *           {@value #EXIF_COLOR_SPACE_SRGB}.
	 * @implSpec If software identification is given, it is added as an Exif <code>Software</code> (<code>0x0131</code>) tag.
	 * @implSpec If a modification instant is given, it is added as an Exif <code>DateTime</code> (<code>0x0132</code>), <code>SubSecTime</code>
	 *           (<code>0x9290</code>), and <code>OffsetTime</code> (<code>0x9010</code>) tags.
	 * @implSpec This implementation only supports writing Exif metadata to JPEG images.
	 * @implSpec This implementation uses <a href="https://commons.apache.org/proper/commons-imaging/">Apache Commons Imaging</a>.
	 * @param byteSource The byte source containing the processed image.
	 * @param outputStream The output stream for writing the image with added metadata.
	 * @param metadata The description containing the metadata to add.
	 * @param sRGB Whether the added metadata should indicate the sRGB color space.
	 * @param software A string identifying the software generating or updating the image, or <code>null</code> if no software information should be added.
	 * @param modifiedAt The value to use the instant the image was modified, or <code>null</code> if no modification timestamp should be added.
	 * @throws IOException if there is an I/O error adding the metadata.
	 * @see <a href="http://www.hanhuy.com/pfn/java-image-thumbnail-comparison">A comparison of Java image thumbnailing techniques</a>
	 * @see <a href="https://www.universalwebservices.net/web-programming-resources/java/adjust-jpeg-image-compression-quality-when-saving-images-in-java/">Adjust
	 *      JPEG image compression quality when saving images in Java</a>
	 */
	protected static void addImageMetadata(@Nonnull final ByteSource byteSource, @Nonnull final OutputStream outputStream,
			@Nonnull final UrfResourceDescription metadata, final boolean sRGB, @Nullable final String software, @Nullable final Instant modifiedAt)
			throws IOException {
		try {
			final TiffOutputSet tiffOutputSet = new TiffOutputSet();
			final TiffOutputDirectory exifDirectory = tiffOutputSet.getOrCreateRootDirectory(); //getOrCreateExifDirectory() prevents metadata-extractor from seeing values
			//XPTitle (0x9C9B)
			metadata.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_TITLE)
					.ifPresent(throwingConsumer(title -> exifDirectory.add(EXIF_TAG_XP_TITLE, title.toString())));
			//ImageDescription (0x010E)
			metadata.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_DESCRIPTION)
					.ifPresent(throwingConsumer(description -> exifDirectory.add(EXIF_TAG_IMAGE_DESCRIPTION, description.toString())));
			//Artist (0x013B)
			metadata.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_ARTIST)
					.ifPresent(throwingConsumer(artist -> exifDirectory.add(EXIF_TAG_ARTIST, artist.toString())));
			//Copyright (0x8298)
			metadata.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_COPYRIGHT)
					.ifPresent(throwingConsumer(copyright -> exifDirectory.add(EXIF_TAG_COPYRIGHT, copyright.toString())));
			//DateTimeOriginal (0x9003), SubSecTimeOriginal (0x9291), OffsetTimeOriginal (0x9011)
			final TiffOutputDirectory subExifDirectory = tiffOutputSet.getOrCreateExifDirectory();
			metadata.findPropertyValueByHandle(Artifact.PROPERTY_HANDLE_CREATED_AT).flatMap(asInstance(Instant.class)).ifPresent(throwingConsumer(createdAt -> {
				subExifDirectory.add(EXIF_TAG_DATE_TIME_ORIGINAL, EXIF_DATE_TIME_FORMATTER.format(createdAt.atOffset(UTC))); //resolve the time to UTC
				subExifDirectory.add(EXIF_TAG_SUB_SEC_TIME_ORIGINAL, EXIF_SUB_SEC_TIME_FORMATTER.format(createdAt.atOffset(UTC))); //resolve the subseconds to UTC
				subExifDirectory.add(EXIF_TAG_OFFSET_TIME_ORIGINAL, EXIF_OFFSET_TIME_UTC); //indicate that the time is in UTC
			}));
			//ColorSpace (0xA001)
			if(sRGB) {
				subExifDirectory.add(EXIF_TAG_COLOR_SPACE, EXIF_COLOR_SPACE_SRGB);
			}
			//Software (0x0131)
			if(software != null) {
				exifDirectory.add(EXIF_TAG_SOFTWARE, software);
			}
			//DateTime (0x0132), SubSecTime (0x9290), OffsetTime (0x9010)
			if(modifiedAt != null) { //note that DateTime goes in IFD0, while SubSecTime and OffsetTime go in the SubIFD
				exifDirectory.add(EXIF_TAG_DATE_TIME, EXIF_DATE_TIME_FORMATTER.format(modifiedAt.atOffset(UTC))); //resolve the time to UTC
				subExifDirectory.add(EXIF_TAG_SUB_SEC_TIME, EXIF_SUB_SEC_TIME_FORMATTER.format(modifiedAt.atOffset(UTC))); //resolve the subseconds to UTC
				subExifDirectory.add(EXIF_TAG_OFFSET_TIME, EXIF_OFFSET_TIME_UTC); //indicate that the time is in UTC
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
