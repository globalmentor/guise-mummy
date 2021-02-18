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

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static com.globalmentor.io.Images.*;
import static com.globalmentor.java.OperatingSystem.*;
import static java.time.ZoneOffset.UTC;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.*;
import java.net.URI;
import java.nio.file.Paths;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.imaging.common.bytesource.ByteSourceInputStream;
import org.junit.jupiter.api.*;

import com.drew.imaging.*;
import com.drew.metadata.*;
import com.drew.metadata.exif.*;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.xmp.XmpDirectory;
import com.globalmentor.time.TimeZones;

import io.confound.config.Configuration;
import io.guise.mummy.*;
import io.guise.mummy.mummify.image.BaseImageMummifier.TempOutputStream;
import io.urf.URF.Handle;
import io.urf.model.UrfObject;
import io.urf.model.UrfResourceDescription;

/**
 * Tests of {@link BaseImageMummifier}.
 * @implNote Currently there are no tests to ensure that XMP metadata overrides IPTC metadata. Concentration is on XMP as the canonical metadata, and Exif as
 *           the ubiquitous metadata.
 * @author Garret Wilson
 */
public class BaseImageMummifierTest {

	/**
	 * A sample JPEG image with a mix of Exif and XMP metadata, including:
	 * <dl>
	 * <dt><code>Copyright</code> (Exif <code>0x8298</code>)</dt>
	 * <dd>Copyright (C) 2009 Garret Wilson</dd>
	 * <dt><code>DateTimeOriginal</code> (Exif <code>0x9003</code>)</dt>
	 * <dd><code>2009:08:29 16:51:21</code></dd>
	 * <dt><code>Make</code> (Exif <code>0x010F</code>)</dt>
	 * <dd>Canon</dd>
	 * <dt><code>Software</code> (Exif <code>0x0131</code>)</dt>
	 * <dd>paint.net 4.2.14</dd>
	 * <dt><code>dc:Creator</code> (XMP)</dt>
	 * <dd>Garret Wilson</dd>
	 * <dt><code>dc:Rights</code> (XMP)</dt>
	 * <dd>Copyright © 2009 Garret Wilson</dd>
	 * <dt><code>xmp:CreateDate</code> (XMP)</dt>
	 * <dd><code>2009:08:29 16:51:21.00-07:00</code></dd>
	 * <dd>
	 * </dl>
	 */
	public static final String GATE_TURRET_REDUCED_JPEG_RESOURCE_NAME = "gate-turret-reduced.jpg";
	public static final long GATE_TURRET_REDUCED_JPEG_FILE_SIZE = 83052;
	public static final int GATE_TURRET_REDUCED_JPEG_WIDTH = 972;
	public static final int GATE_TURRET_REDUCED_JPEG_HEIGHT = 648;

	/** A sample JPEG image with no metadata at all. */
	public static final String GATE_TURRET_REDUCED_NO_METADATA_JPEG_RESOURCE_NAME = "gate-turret-reduced-no-metadata.jpg";

	/**
	 * A sample JPEG image with only Exif metadata, including:
	 * <dl>
	 * <dt><code>XPTitle</code> (Exif <code>0x9C9B</code>)</dt>
	 * <dd>Gate and Turret</dd>
	 * <dt><code>ImageDescription</code> (Exif <code>0x010E</code>)</dt>
	 * <dd>Castle turret viewed through a gate.</dd>
	 * <dt><code>Artist</code> (Exif <code>0x010E</code>)</dt>
	 * <dd>Garret Wilson</dd>
	 * <dt><code>Copyright</code> (Exif <code>0x8298</code>)</dt>
	 * <dd>Copyright (C) 2009 Garret Wilson</dd>
	 * <dt><code>DateTimeOriginal</code> (Exif <code>0x9003</code>)</dt>
	 * <dd><code>2009:08:29 16:51:21</code></dd>
	 * <dt><code>Make</code> (Exif <code>0x010F</code>)</dt>
	 * <dd>Canon</dd>
	 * <dt><code>Software</code> (Exif <code>0x0131</code>)</dt>
	 * <dd>paint.net 4.2.14</dd>
	 * <dd>
	 * </dl>
	 */
	public static final String GATE_TURRET_REDUCED_EXIF_JPEG_RESOURCE_NAME = "gate-turret-reduced-exif.jpg";

	/**
	 * A sample JPEG image with only Exif metadata encoded in UTF-8, including:
	 * <dl>
	 * <dt><code>XPTitle</code> (Exif <code>0x9C9B</code>)</dt>
	 * <dd>Gate and Turret</dd>
	 * <dt><code>ImageDescription</code> (Exif <code>0x010E</code>)</dt>
	 * <dd>Castle turret viewed through a gate.</dd>
	 * <dt><code>Copyright</code> (Exif <code>0x8298</code>)</dt>
	 * <dd>Copyright © 2009 Garret Wilson</dd>
	 * <dt><code>DateTimeOriginal</code> (Exif <code>0x9003</code>)</dt>
	 * <dd><code>2009:08:29 16:51:21</code></dd>
	 * <dt><code>Make</code> (Exif <code>0x010F</code>)</dt>
	 * <dd>Canon</dd>
	 * <dt><code>Software</code> (Exif <code>0x0131</code>)</dt>
	 * <dd>paint.net 4.2.14</dd>
	 * <dd>
	 * </dl>
	 */
	public static final String GATE_TURRET_REDUCED_EXIF_UTF_8_JPEG_RESOURCE_NAME = "gate-turret-reduced-exif-utf-8.jpg";

	/**
	 * A sample JPEG image with Exif and IPTC metadata, including:
	 * <dl>
	 * <dt><code>Copyright</code> (Exif <code>0x8298</code>)</dt>
	 * <dd>Copyright (C) 2009 Garret Wilson</dd>
	 * <dt><code>DateTimeOriginal</code> (Exif <code>0x9003</code>)</dt>
	 * <dd><code>2009:08:29 16:51:21</code></dd>
	 * <dt><code>Make</code> (Exif <code>0x010F</code>)</dt>
	 * <dd>Canon</dd>
	 * <dt><code>Software</code> (Exif <code>0x0131</code>)</dt>
	 * <dd>paint.net 4.2.14</dd>
	 * <dt><code>ObjectName</code> (IPTC-IIM <code>2:05</code>, <code>0x205</code>)</dt>
	 * <dd>Gate and Turret</dd>
	 * <dt><code>Caption</code> (IPTC-IIM <code>2:120</code>, <code>0x0278</code>)</dt>
	 * <dd>Castle turret viewed through a gate.</dd>
	 * <dt><code>CopyrightNotice</code> (IPTC-IIM <code>2:116</code>, <code>0x0274</code>)</dt>
	 * <dd>Copyright © 2009 Garret Wilson</dd>
	 * <dt><code>DateCreated</code> (IPTC-IIM <code>2:55</code>, <code>0x0237</code>)</dt>
	 * <dd><code>2009:08:29</code></dd>
	 * <dt><code>TimeCreated</code> (IPTC-IIM <code>2:60</code>, <code>0x023C</code>)</dt>
	 * <dd><code>16:51:21-07:00</code></dd>
	 * <dd>
	 * </dl>
	 */
	public static final String GATE_TURRET_REDUCED_EXIF_IPTC_JPEG_RESOURCE_NAME = "gate-turret-reduced-exif-iptc.jpg";

	/**
	 * A sample JPEG image with Exif and XMP metadata, including:
	 * <dl>
	 * <dt><code>Copyright</code> (Exif <code>0x8298</code>)</dt>
	 * <dd>Copyright (C) 2009 Garret Wilson</dd>
	 * <dt><code>DateTimeOriginal</code> (Exif <code>0x9003</code>)</dt>
	 * <dd><code>2009:08:29 16:51:21</code></dd>
	 * <dt><code>Make</code> (Exif <code>0x010F</code>)</dt>
	 * <dd>Canon</dd>
	 * <dt><code>Software</code> (Exif <code>0x0131</code>)</dt>
	 * <dd>paint.net 4.2.14</dd>
	 * <dt><code>dc:Title</code> (XMP)</dt>
	 * <dd>Gate and Turret</dd>
	 * <dt><code>dc:Description</code> (XMP)</dt>
	 * <dd>Castle turret viewed through a gate.</dd>
	 * <dt><code>dc:Rights</code> (XMP)</dt>
	 * <dd>Copyright © 2009 Garret Wilson</dd>
	 * <dt><code>dc:Creator</code> (XMP)</dt>
	 * <dd>Garret Wilson</dd>
	 * <dt><code>xmp:CreateDate</code> (XMP)</dt>
	 * <dd><code>2009:08:29 16:51:21.00-07:00</code></dd>
	 * <dd>
	 * </dl>
	 */
	public static final String GATE_TURRET_REDUCED_EXIF_XMP_JPEG_RESOURCE_NAME = "gate-turret-reduced-exif-xmp.jpg";
	public static final long GATE_TURRET_REDUCED_EXIF_XMP_JPEG_FILE_SIZE = 83309;

	/**
	 * A sample JPEG image with only XMP metadata, including:
	 * <dl>
	 * <dt><code>dc:Title</code> (XMP)</dt>
	 * <dd>Gate and Turret</dd>
	 * <dt><code>dc:Description</code> (XMP)</dt>
	 * <dd>Castle turret viewed through a gate.</dd>
	 * <dt><code>dc:Rights</code> (XMP)</dt>
	 * <dd>Copyright © 2009 Garret Wilson</dd>
	 * <dt><code>dc:Creator</code> (XMP)</dt>
	 * <dd>Garret Wilson</dd>
	 * <dt><code>xmp:CreateDate</code> (XMP)</dt>
	 * <dd><code>2009:08:29 16:51:21.00-07:00</code></dd>
	 * <dd>
	 * </dl>
	 */
	public static final String GATE_TURRET_REDUCED_XMP_JPEG_RESOURCE_NAME = "gate-turret-reduced-xmp.jpg";

	private MummyContext fixtureContext;

	@BeforeEach
	protected void setupFixture() {
		final GuiseProject project = new DefaultGuiseProject(getWorkingDirectory(), Configuration.empty());
		fixtureContext = new StubMummyContext(project);
	}

	private BaseImageMummifier testMummifier;

	@BeforeEach
	private void setupTestMummifier() throws IOException {
		testMummifier = new BaseImageMummifier(Set.of(JPEG_MEDIA_TYPE)) {

			@Override
			protected void mummifyFile(MummyContext context, CorporealSourceArtifact artifact) throws IOException {
				throw new AssertionError();
			}
		};
	}

	/** @see BaseImageMummifier#getArtifactMediaType(MummyContext, java.nio.file.Path) */
	@Test
	void testGetArtifactMediaType() throws IOException {
		assertThat(testMummifier.getArtifactMediaType(fixtureContext, Paths.get("test.jpg")), isPresentAndIs(JPEG_MEDIA_TYPE));
		assertThat(testMummifier.getArtifactMediaType(fixtureContext, Paths.get("test.JPG")), isPresentAndIs(JPEG_MEDIA_TYPE));
		assertThat(testMummifier.getArtifactMediaType(fixtureContext, Paths.get("test.jpeg")), isPresentAndIs(JPEG_MEDIA_TYPE));
		assertThat(testMummifier.getArtifactMediaType(fixtureContext, Paths.get("test.png")), isEmpty());
	}

	/**
	 * @see BaseImageMummifier#loadSourceMetadata(MummyContext, InputStream, String)
	 * @see #GATE_TURRET_REDUCED_EXIF_JPEG_RESOURCE_NAME
	 */
	@Test
	void testLoadSourceMetadataExif() throws IOException {
		final Map<URI, Object> metadata;
		try (final InputStream inputStream = getClass().getResourceAsStream(GATE_TURRET_REDUCED_EXIF_JPEG_RESOURCE_NAME)) {
			metadata = testMummifier.loadSourceMetadata(fixtureContext, inputStream, GATE_TURRET_REDUCED_EXIF_JPEG_RESOURCE_NAME).stream()
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_TITLE)), is("Gate and Turret"));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_DESCRIPTION)), is("Castle turret viewed through a gate."));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_ARTIST)), is("Garret Wilson"));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_COPYRIGHT)), is("Copyright (C) 2009 Garret Wilson"));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_CREATED_AT)), is(ZonedDateTime.of(2009, 8, 29, 16, 51, 21, 0, UTC).toInstant()));
	}

	/**
	 * @see BaseImageMummifier#loadSourceMetadata(MummyContext, InputStream, String)
	 * @see #GATE_TURRET_REDUCED_EXIF_UTF_8_JPEG_RESOURCE_NAME
	 */
	@Disabled("Reenable when metadata-extractor bug #270 is fixed.")
	@Test
	void testLoadSourceMetadataExifUtf8() throws IOException {
		final Map<URI, Object> metadata;
		try (final InputStream inputStream = getClass().getResourceAsStream(GATE_TURRET_REDUCED_EXIF_UTF_8_JPEG_RESOURCE_NAME)) {
			metadata = testMummifier.loadSourceMetadata(fixtureContext, inputStream, GATE_TURRET_REDUCED_EXIF_UTF_8_JPEG_RESOURCE_NAME).stream()
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_TITLE)), is("Gate and Turret"));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_DESCRIPTION)), is("Castle turret viewed through a gate."));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_COPYRIGHT)), is("Copyright © 2009 Garret Wilson"));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_CREATED_AT)), is(ZonedDateTime.of(2009, 8, 29, 16, 51, 21, 0, UTC).toInstant()));
	}

	/**
	 * IPTC metadata should override Exif metadata
	 * @see BaseImageMummifier#loadSourceMetadata(MummyContext, InputStream, String)
	 * @see #GATE_TURRET_REDUCED_EXIF_IPTC_JPEG_RESOURCE_NAME
	 */
	@Test
	void testLoadSourceMetadataExifIptc() throws IOException {
		final Map<URI, Object> metadata;
		try (final InputStream inputStream = getClass().getResourceAsStream(GATE_TURRET_REDUCED_EXIF_IPTC_JPEG_RESOURCE_NAME)) {
			metadata = testMummifier.loadSourceMetadata(fixtureContext, inputStream, GATE_TURRET_REDUCED_EXIF_IPTC_JPEG_RESOURCE_NAME).stream()
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_TITLE)), is("Gate and Turret"));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_DESCRIPTION)), is("Castle turret viewed through a gate."));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_COPYRIGHT)), is("Copyright © 2009 Garret Wilson"));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_CREATED_AT)),
				is(ZonedDateTime.of(2009, 8, 29, 16, 51, 21, 0, ZoneOffset.ofHours(-7)).toInstant()));
	}

	/**
	 * XMP metadata should override Exif metadata
	 * @see BaseImageMummifier#loadSourceMetadata(MummyContext, InputStream, String)
	 * @see #GATE_TURRET_REDUCED_EXIF_XMP_JPEG_RESOURCE_NAME
	 */
	@Test
	void testLoadSourceMetadataExifXmp() throws IOException {
		final Map<URI, Object> metadata;
		try (final InputStream inputStream = getClass().getResourceAsStream(GATE_TURRET_REDUCED_EXIF_XMP_JPEG_RESOURCE_NAME)) {
			metadata = testMummifier.loadSourceMetadata(fixtureContext, inputStream, GATE_TURRET_REDUCED_EXIF_XMP_JPEG_RESOURCE_NAME).stream()
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_TITLE)), is("Gate and Turret"));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_DESCRIPTION)), is("Castle turret viewed through a gate."));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_ARTIST)), is("Garret Wilson"));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_COPYRIGHT)), is("Copyright © 2009 Garret Wilson"));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_CREATED_AT)),
				is(ZonedDateTime.of(2009, 8, 29, 16, 51, 21, 0, ZoneOffset.ofHours(-7)).toInstant()));
	}

	/**
	 * @see BaseImageMummifier#loadSourceMetadata(MummyContext, InputStream, String)
	 * @see #GATE_TURRET_REDUCED_XMP_JPEG_RESOURCE_NAME
	 */
	@Test
	void testLoadSourceMetadataXmp() throws IOException {
		final Map<URI, Object> metadata;
		try (final InputStream inputStream = getClass().getResourceAsStream(GATE_TURRET_REDUCED_XMP_JPEG_RESOURCE_NAME)) {
			metadata = testMummifier.loadSourceMetadata(fixtureContext, inputStream, GATE_TURRET_REDUCED_XMP_JPEG_RESOURCE_NAME).stream()
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_TITLE)), is("Gate and Turret"));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_DESCRIPTION)), is("Castle turret viewed through a gate."));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_ARTIST)), is("Garret Wilson"));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_COPYRIGHT)), is("Copyright © 2009 Garret Wilson"));
		assertThat(metadata.get(Handle.toTag(Artifact.PROPERTY_HANDLE_CREATED_AT)),
				is(ZonedDateTime.of(2009, 8, 29, 16, 51, 21, 0, ZoneOffset.ofHours(-7)).toInstant()));
	}

	/** @see BaseImageMummifier#EXIF_DATE_TIME_FORMATTER */
	@Test
	void testExifDateTimeOriginalFormatter() {
		assertThat(BaseImageMummifier.EXIF_DATE_TIME_FORMATTER
				.format(ZonedDateTime.of(2009, 8, 29, 16, 51, 21, (int)MILLISECONDS.toNanos(789), UTC).toInstant().atOffset(UTC)), is("2009:08:29 16:51:21"));
	}

	/** @see BaseImageMummifier#EXIF_SUB_SEC_TIME_FORMATTER */
	@Test
	void testExifSubSecTimeOriginalFormatter() {
		assertThat(BaseImageMummifier.EXIF_SUB_SEC_TIME_FORMATTER
				.format(ZonedDateTime.of(2009, 8, 29, 16, 51, 21, (int)MILLISECONDS.toNanos(789), UTC).toInstant().atOffset(UTC)), is("789"));
	}

	/**
	 * @see BaseImageMummifier#addImageMetadata(org.apache.commons.imaging.common.bytesource.ByteSource, OutputStream, UrfResourceDescription, boolean, String,
	 *      Instant)
	 */
	@Test
	void testAddImageMetadata() throws IOException, ImageProcessingException, MetadataException {
		try (final InputStream inputStream = getClass().getResourceAsStream(GATE_TURRET_REDUCED_NO_METADATA_JPEG_RESOURCE_NAME);
				final TempOutputStream tempOutputStream = new TempOutputStream()) {
			//add image metadata
			final UrfResourceDescription metadata = new UrfObject();
			//TODO bring back when [IMAGING-281](https://issues.apache.org/jira/browse/IMAGING-281) is fixed
			//			metadata.setPropertyValue(Handle.toTag(Artifact.PROPERTY_HANDLE_TITLE), "Test Title");
			//TODO bring back when metadata-extractor [#270](https://github.com/drewnoakes/metadata-extractor/issues/270) is fixed
			//			metadata.setPropertyValue(Handle.toTag(Artifact.PROPERTY_HANDLE_DESCRIPTION), "Touché");
			//			metadata.setPropertyValue(Handle.toTag(Artifact.PROPERTY_HANDLE_COPYRIGHT), "Copyright © 2021 GlobalMentor, Inc.");
			metadata.setPropertyValue(Handle.toTag(Artifact.PROPERTY_HANDLE_DESCRIPTION), "This is a test image.");
			metadata.setPropertyValue(Handle.toTag(Artifact.PROPERTY_HANDLE_COPYRIGHT), "Copyright (C) 2021 GlobalMentor, Inc.");
			final Instant createdAt = ZonedDateTime.of(2021, 2, 15, 16, 34, 8, (int)MILLISECONDS.toNanos(789), UTC).toInstant();
			final Instant modifiedAt = ZonedDateTime.of(2021, 2, 16, 6, 34, 57, (int)MILLISECONDS.toNanos(321), UTC).toInstant();
			metadata.setPropertyValue(Handle.toTag(Artifact.PROPERTY_HANDLE_CREATED_AT), createdAt);
			BaseImageMummifier.addImageMetadata(new ByteSourceInputStream(inputStream, null), tempOutputStream, metadata, true, "Foo App", modifiedAt);
			//extract and verify added metadata using metadata-extractor
			final Metadata extractedMetadata = ImageMetadataReader.readMetadata(tempOutputStream.toInputStream());
			final ExifIFD0Directory ifd0Directory = extractedMetadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			assertThat("Exif IFD0 metadata was added.", ifd0Directory, is(not(nullValue())));
			//TODO bring back when [IMAGING-281](https://issues.apache.org/jira/browse/IMAGING-281) is fixed
			//			final ExifIFD0Descriptor exifIFD0Descriptor = new ExifIFD0Descriptor(ifd0Directory);
			//			assertThat(exifIFD0Descriptor.getWindowsTitleDescription(), is("Test Title")); //XPTitle
			//TODO bring back when metadata-extractor [#270](https://github.com/drewnoakes/metadata-extractor/issues/270) is fixed
			//			assertThat(ifd0Directory.getString(ExifIFD0Directory.TAG_IMAGE_DESCRIPTION), is("Touché")); //ImageDescription
			//			assertThat(ifd0Directory.getString(ExifIFD0Directory.TAG_COPYRIGHT), is("Copyright © 2021 GlobalMentor, Inc.")); //Copyright
			assertThat("Exif image description was added.", ifd0Directory.getString(ExifIFD0Directory.TAG_IMAGE_DESCRIPTION), is("This is a test image.")); //ImageDescription
			assertThat("Exif copyright was added.", ifd0Directory.getString(ExifIFD0Directory.TAG_COPYRIGHT), is("Copyright (C) 2021 GlobalMentor, Inc.")); //Copyright
			assertThat("Exif software identification was added.", ifd0Directory.getString(ExifIFD0Directory.TAG_SOFTWARE), is("Foo App")); //Software
			final ExifSubIFDDirectory subIFDDirectory = extractedMetadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			assertThat("Exif SubIFD metadata was added.", subIFDDirectory, is(not(nullValue())));
			assertThat("Exif original date/time tags were added.", Optional.ofNullable(subIFDDirectory.getDateOriginal(TimeZones.UTC)).map(Date::toInstant),
					isPresentAndIs(createdAt));
			assertThat("Exif modified date/time tags were added.", Optional.ofNullable(subIFDDirectory.getDateModified(TimeZones.UTC)).map(Date::toInstant),
					isPresentAndIs(modifiedAt));
			assertThat("Exif color space designator for sRGB was added.", subIFDDirectory.getInt(ExifSubIFDDirectory.TAG_COLOR_SPACE), is(1)); //ColorSpace
			assertThat("No XMP metadata was added.", extractedMetadata.getFirstDirectoryOfType(XmpDirectory.class), is(nullValue()));
			assertThat("No IPTC metadata was added.", extractedMetadata.getFirstDirectoryOfType(IptcDirectory.class), is(nullValue()));
		}
	}

}
