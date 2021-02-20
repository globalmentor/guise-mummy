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

import static com.globalmentor.io.ClassResources.*;
import static io.guise.mummy.GuiseMummy.*;
import static io.guise.mummy.mummify.image.BaseImageMummifierTest.*;
import static java.lang.String.format;
import static java.nio.file.Files.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.*;

import com.adobe.internal.xmp.XMPException;
import com.drew.imaging.*;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.*;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.xmp.XmpDirectory;
import com.globalmentor.io.Paths;

import io.guise.mummy.*;

/**
 * A Guise Mummy smoke test.
 * @author Garret Wilson
 */
public class DefaultImageMummifierIT extends BaseEndToEndIT {

	@Override
	protected void populateSiteSourceDirectory(final Path siteSourceDirectory) throws IOException {
		super.populateSiteSourceDirectory(siteSourceDirectory);
		//…/src/site/gate-turret-reduced.jpg
		copy(BaseImageMummifierTest.class, siteSourceDirectory, GATE_TURRET_REDUCED_EXIF_XMP_JPEG_RESOURCE_NAME);
	}

	@Test
	void shouldScaleImageOverProcessThresholdFileSize() throws IOException {
		//…/src/site/gate-turret-reduced.jpg
		final Path sourceImageFile = getSiteSourceDirectory().resolve(GATE_TURRET_REDUCED_EXIF_XMP_JPEG_RESOURCE_NAME);
		assertThat(size(sourceImageFile), is(GATE_TURRET_REDUCED_EXIF_XMP_JPEG_FILE_SIZE));
		final BufferedImage sourceImage = ImageIO.read(sourceImageFile.toFile()); //TODO add more efficient library method to read dimensions directly from reader; see DefaultImageMummifier and https://stackoverflow.com/a/12164026 
		assertThat(sourceImage.getWidth(), is(GATE_TURRET_REDUCED_JPEG_WIDTH));
		assertThat(sourceImage.getHeight(), is(GATE_TURRET_REDUCED_JPEG_HEIGHT));

		getFixtureProjectSettings().put(DefaultImageMummifier.CONFIG_KEY_MUMMY_IMAGE_PROCESS_THRESHOLD_FILE_SIZE, 70_000); //force processing
		getFixtureProjectSettings().put(DefaultImageMummifier.CONFIG_KEY_MUMMY_IMAGE_SCALE_MAX_LENGTH, 600);
		getFixtureProjectSettings().put(DefaultImageMummifier.CONFIG_KEY_MUMMY_IMAGE_COMPRESSION_QUALITY, 0.5);
		mummify(LifeCyclePhase.MUMMIFY);

		//…/target/site/gate-turret-reduced.jpg
		final Path targetImageFile = getSiteTargetDirectory().resolve(GATE_TURRET_REDUCED_EXIF_XMP_JPEG_RESOURCE_NAME);
		assertThat(size(targetImageFile), is(lessThan(GATE_TURRET_REDUCED_EXIF_XMP_JPEG_FILE_SIZE)));
		final BufferedImage targetImage = ImageIO.read(targetImageFile.toFile()); //TODO get dimensions using a more efficient utility method
		assertThat(targetImage.getWidth(), is(600));
		assertThat(targetImage.getHeight(), is(400));
	}

	@Test
	public void testAspectualImages() throws IOException, ImageProcessingException, XMPException {
		getFixtureProjectSettings().put(DefaultImageMummifier.CONFIG_KEY_MUMMY_IMAGE_PROCESS_THRESHOLD_FILE_SIZE, 70_000); //force processing
		getFixtureProjectSettings().put(DefaultImageMummifier.CONFIG_KEY_MUMMY_IMAGE_SCALE_MAX_LENGTH, 600);
		getFixtureProjectSettings().put(DefaultImageMummifier.CONFIG_KEY_MUMMY_IMAGE_COMPRESSION_QUALITY, 0.5);
		getFixtureProjectSettings().put(ImageMummifier.CONFIG_KEY_MUMMY_IMAGE_WITH_ASPECTS, List.of("thumbnail")); //turn on thumbnail generation
		getFixtureProjectSettings().put(format(ImageMummifier.CONFIG_KEY_FORMAT_MUMMY_IMAGE_ASPECT___SCALE_MAX_LENGTH, "thumbnail"), 300);
		getFixtureProjectSettings().put(format(ImageMummifier.CONFIG_KEY_FORMAT_MUMMY_IMAGE_ASPECT___COMPRESSION_QUALITY, "thumbnail"), 0.5);
		mummify(LifeCyclePhase.MUMMIFY);

		//…/target/site/gate-turret-reduced.jpg
		final Path targetPrimaryImageFile = getSiteTargetDirectory().resolve(GATE_TURRET_REDUCED_EXIF_XMP_JPEG_RESOURCE_NAME);
		final long targetPrimaryImageFileSize = size(targetPrimaryImageFile);
		assertThat("Primary image file is correct size.", targetPrimaryImageFileSize, is(lessThan(GATE_TURRET_REDUCED_EXIF_XMP_JPEG_FILE_SIZE)));
		final BufferedImage targetPrimaryImage = ImageIO.read(targetPrimaryImageFile.toFile()); //TODO get dimensions using a more efficient utility method
		assertThat("Primary image has correct width.", targetPrimaryImage.getWidth(), is(600));
		assertThat("Primary image is correct height.", targetPrimaryImage.getHeight(), is(400));
		try (final InputStream inputStream = new BufferedInputStream(newInputStream(targetPrimaryImageFile))) {
			final Metadata extractedMetadata = ImageMetadataReader.readMetadata(inputStream);
			final ExifIFD0Directory ifd0Directory = extractedMetadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			assertThat("Primary image Exif metadata is still present.", ifd0Directory, is(not(nullValue())));
			final ExifIFD0Descriptor exifIFD0Descriptor = new ExifIFD0Descriptor(ifd0Directory);
			assertThat("Primary image Exif IFD0 metadata is still present.", exifIFD0Descriptor, is(not(nullValue())));
			//TODO bring back when metadata-extractor [#270](https://github.com/drewnoakes/metadata-extractor/issues/270) is fixed
			//			assertThat("Primary image Exif IFD0 existing metadata appears to have been updated correctly.", ifd0Directory.getString(ExifIFD0Directory.TAG_COPYRIGHT),
			//					is("Copyright © 2009 Garret Wilson")); //Copyright (was only ASCII in original Exif metadata)
			assertThat("Primary image Exif IFD0 metadata has had camera make removed.", ifd0Directory.getString(ExifIFD0Directory.TAG_MAKE), is(nullValue())); //Make (as representative of metadata removed)
			assertThat("Primary image Exif IFD0 metadata has had software identifier updated.", ifd0Directory.getString(ExifIFD0Directory.TAG_SOFTWARE),
					is(GuiseMummy.LABEL)); //Software (as representative of metadata updated)
			assertThat("No IPTC metadata was added.", extractedMetadata.getFirstDirectoryOfType(IptcDirectory.class), is(nullValue()));
			final XmpDirectory xmpDirectory = extractedMetadata.getFirstDirectoryOfType(XmpDirectory.class);
			assertThat("Primary image XMP metadata was removed.", xmpDirectory, is(nullValue()));
		}

		//…/target/site/gate-turret-reduced-thumbnail.jpg
		final Path targetThumbnailImageFile = Paths.appendFilenameBase(targetPrimaryImageFile, "-thumbnail");
		final long targetThumbnailImageFileSize = size(targetThumbnailImageFile);
		assertThat("Thumbnail image file is correct size.", targetThumbnailImageFileSize, is(lessThan(targetPrimaryImageFileSize)));
		final BufferedImage targetThumbnailImage = ImageIO.read(targetThumbnailImageFile.toFile()); //TODO get dimensions using a more efficient utility method
		assertThat("Thumbnail image has correct width.", targetThumbnailImage.getWidth(), is(300));
		assertThat("Thumbnail image is correct height.", targetThumbnailImage.getHeight(), is(200));
		try (final InputStream inputStream = new BufferedInputStream(newInputStream(targetThumbnailImageFile))) {
			final Metadata extractedMetadata = ImageMetadataReader.readMetadata(inputStream);
			final ExifIFD0Directory ifd0Directory = extractedMetadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			assertThat("Thumbnail image Exif metadata is present.", ifd0Directory, is(not(nullValue())));
			final ExifIFD0Descriptor exifIFD0Descriptor = new ExifIFD0Descriptor(ifd0Directory);
			assertThat("Thumbnail image Exif IFD0 metadata present.", exifIFD0Descriptor, is(not(nullValue())));
			//TODO bring back when metadata-extractor [#270](https://github.com/drewnoakes/metadata-extractor/issues/270) is fixed
			//			assertThat("Thumbnail image Exif IFD0 existing metadata appears to have been updated correctly.", ifd0Directory.getString(ExifIFD0Directory.TAG_COPYRIGHT),
			//					is("Copyright © 2009 Garret Wilson")); //Copyright (was only ASCII in original Exif metadata)
			assertThat("Thumbnail image Exif IFD0 metadata appears to have been added correctly.", ifd0Directory.getString(ExifIFD0Directory.TAG_IMAGE_DESCRIPTION),
					is("Castle turret viewed through a gate.")); //ImageDescription (was not present in original Exif metadata)
			assertThat("Thumbnail image Exif IFD0 metadata has had camera make removed.", ifd0Directory.getString(ExifIFD0Directory.TAG_MAKE), is(nullValue())); //Make (as representative of metadata removed)
			assertThat("Thumbnail image Exif IFD0 metadata has had software identifier updated.", ifd0Directory.getString(ExifIFD0Directory.TAG_SOFTWARE),
					is(GuiseMummy.LABEL)); //Software (as representative of metadata updated)
			assertThat("No IPTC metadata was added.", extractedMetadata.getFirstDirectoryOfType(IptcDirectory.class), is(nullValue()));
			final XmpDirectory xmpDirectory = extractedMetadata.getFirstDirectoryOfType(XmpDirectory.class);
			assertThat("Thumbnail image XMP metadata was removed.", xmpDirectory, is(nullValue()));
		}

	}

}
