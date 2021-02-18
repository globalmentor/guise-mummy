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

import static com.globalmentor.collections.iterators.Iterators.*;
import static com.globalmentor.io.Images.*;
import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static javax.imageio.ImageIO.*;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

import javax.annotation.*;
import javax.imageio.*;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.*;

import com.globalmentor.awt.geom.ImmutableDimension2D;
import com.globalmentor.io.Images;
import com.globalmentor.java.Objects;
import com.globalmentor.net.ContentType;

import io.confound.config.Configuration;
import io.guise.mummy.*;
import io.urf.model.UrfResourceDescription;
import io.urf.vocab.content.Content;

/**
 * General image mummifier.
 * @implSpec This implementation supports GIF, JPEG, and PNG files. Reading metadata is supported from XMP, IPTC, and Exif. When processing a primary image, all
 *           metadata will be retained if possible. When processing an image aspect, in order to reduce file size all image metadata will be discarded; a small
 *           subset of normalized Exif metadata will then be added back, but only for JPEG images. This subset will include the Guise software information from
 *           {@link MummyContext#getMummifierIdentification()}.
 * @implSpec This implementation supports configured image aspects.
 * @implSpec This implementation uses <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.desktop/javax/imageio/package-summary.html">Java Image
 *           I/O</a> for image processing.
 * @implSpec This implementation uses <a href="https://commons.apache.org/proper/commons-imaging/">Apache Commons Imaging</a> for adding Exif image metadata.
 * @implSpec This mummifier orchestrates mummification of any aspectual image artifacts, and generation of aspects is determined wholly by whether the main
 *           image artifact is being generated.
 * @author Garret Wilson
 */
public class DefaultImageMummifier extends BaseImageMummifier {

	/** @see #CONFIG_KEY_MUMMY_IMAGE_WITH_ASPECTS */
	public static final Set<String> DEFAULT_ASPECT_IDS = emptySet();

	/** @see #CONFIG_KEY_MUMMY_IMAGE_COMPRESSION_QUALITY */
	public static final double DEFAULT_COMPRESSION_QUALITY = 0.8;

	/** @see #CONFIG_KEY_MUMMY_IMAGE_SCALE_MAX_LENGTH */
	public static final int DEFAULT_SCALE_MAX_LENGTH = 2560;

	/** @see #CONFIG_KEY_MUMMY_IMAGE_PROCESS_THRESHOLD_FILE_SIZE */
	public static final long DEFAULT_SCALE_THRESHOLD_FILE_SIZE = 800_000;

	/** No-args constructor. */
	public DefaultImageMummifier() {
		super(Set.of(GIF_MEDIA_TYPE, JPEG_MEDIA_TYPE, PNG_MEDIA_TYPE));
	}

	/**
	 * {@inheritDoc}
	 * @implSpec If the file size threshold for image processing is passed, this implementation creates an aspectual artifact with configured aspects (e.g.
	 *           <code>"preview"</code>).
	 * @see ImageMummifier#CONFIG_KEY_MUMMY_IMAGE_WITH_ASPECTS
	 */
	@Override
	protected Artifact createArtifact(final MummyContext context, final Path sourceFile, final Path outputFile, final UrfResourceDescription description)
			throws IOException {
		final Configuration config = context.getConfiguration();
		if(size(sourceFile) > config.findLong(CONFIG_KEY_MUMMY_IMAGE_PROCESS_THRESHOLD_FILE_SIZE).orElse(DEFAULT_SCALE_THRESHOLD_FILE_SIZE)) {
			final Set<String> aspects = config.findCollection(CONFIG_KEY_MUMMY_IMAGE_WITH_ASPECTS).map(ids -> ids.stream().map(Object::toString).collect(toSet()))
					.orElse(DEFAULT_ASPECT_IDS);
			return DefaultSourceFileArtifact.builder(this, sourceFile, outputFile).withDescription(description).withAspects(aspects).build();
		}
		return super.createArtifact(context, sourceFile, outputFile, description);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation scales the image in an attempt to reduce the file size if the file size is above a certain threshold.
	 * @implSpec This implementation delegates to {@link #processImage(MummyContext, Artifact, InputStream, OutputStream, boolean)} for scaling.
	 * @implSpec This implementation delegates to
	 *           {@link #addImageMetadata(org.apache.commons.imaging.common.bytesource.ByteSource, OutputStream, UrfResourceDescription, boolean, String, Instant)}
	 *           to add metadata to the image after processing.
	 */
	@Override
	public void mummifyFile(final MummyContext context, final CorporealSourceArtifact artifact) throws IOException {
		final long imageScaleThresholdSize = context.getConfiguration().findLong(CONFIG_KEY_MUMMY_IMAGE_PROCESS_THRESHOLD_FILE_SIZE)
				.orElse(DEFAULT_SCALE_THRESHOLD_FILE_SIZE);
		if(artifact.getSourceSize(context) > imageScaleThresholdSize) { //if the size of the image source file goes over our threshold for scaling

			final boolean isImageJpeg = artifact.getResourceDescription().findPropertyValue(Content.TYPE_PROPERTY_TAG).flatMap(Objects.asInstance(ContentType.class))
					.<Boolean>map(Images.JPEG_MEDIA_TYPE::hasBaseType).orElse(false);
			final boolean isImageAspect = artifact.getResourceDescription().hasPropertyValue(AspectualArtifact.PROPERTY_TAG_MUMMY_ASPECT); //e.g. preview or thumbnail
			final boolean isKeepProcessMetadata = !isImageAspect; //to keep file size down, discard metadata during processing for image aspects (but add back a tiny bit later) 
			final boolean isPostProcessWriteMetadataSupported = isImageJpeg && !isKeepProcessMetadata; //if we are discarding metadata during processing, write some basic metadata later for JPEG images
			final boolean isProcessTerminal = !isPostProcessWriteMetadataSupported; //	//if we don't support writing metadata post-processing, we'll write directly to the file when processing
			//process image
			final OutputStream processOutputStream; //remember the stream used for output (even though it will be closed) 
			try (final InputStream inputStream = new BufferedInputStream(artifact.openSource(context)); //use a TempOutputStream for later use if processing isn't terminal
					final OutputStream outputStream = (processOutputStream = isProcessTerminal ? new BufferedOutputStream(newOutputStream(artifact.getTargetPath()))
							: new TempOutputStream())) {
				processImage(context, artifact, inputStream, processOutputStream, isKeepProcessMetadata);
			} catch(final IOException ioException) { //provide more context to I/O errors
				throw new IOException(format("Error processing image `%s`: %s", artifact.getSourcePath(), ioException.getLocalizedMessage()), ioException); //TODO i18n
			}

			//add metadata and stream to output file (if supported)
			if(isPostProcessWriteMetadataSupported) {
				final boolean sRGB = true; //processing the image with Java Image I/O converts it to sRGB if it wasn't already
				final TempOutputStream tempOutputStream = (TempOutputStream)processOutputStream;
				try (final OutputStream outputStream = new BufferedOutputStream(newOutputStream(artifact.getTargetPath()))) {
					addImageMetadata(tempOutputStream.toByteSource(), outputStream, artifact.getResourceDescription(), sRGB, context.getMummifierIdentification(),
							Instant.now());
				} catch(final IOException ioException) { //provide more context to I/O errors
					throw new IOException(format("Error processing image `%s`: %s", artifact.getSourcePath(), ioException.getLocalizedMessage()), ioException); //TODO i18n
				}
			}

		} else {
			copy(artifact.getSourcePath(), artifact.getTargetPath(), REPLACE_EXISTING); //TODO abstract the copy, here and in OpaqueFileMummifier
		}

		if(artifact instanceof AspectualArtifact) { //mummify any image aspects TODO generalize within framework
			for(final Artifact aspectArtifact : ((AspectualArtifact)artifact).getAspects()) {
				aspectArtifact.getResourceDescription().removeProperty(Content.MODIFIED_AT_PROPERTY_TAG); //use the absence of the `content/modifiedAt` property as a proxy flag to force writing (force the content to be considered dirty)
				mummify(context, aspectArtifact);
			}
		}
	}

	/**
	 * Processes an image from the given input stream and writes the processed image to the given output stream. Image aspect are recognized and processed
	 * accordingly.
	 * @implSpec This implementation scales an image using the AWT to draw on a scaled image using bicubic interpolation and quality-biased rendering.
	 * @implSpec This implementation preserves no metadata.
	 * @param context The context of static site generation.
	 * @param artifact The artifact being generated.
	 * @param inputStream The input stream for reading the source image.
	 * @param outputStream The output stream for writing the target image.
	 * @param keepMetadata <code>true</code> if the metadata in the original image should be maintained, or <code>false</code> if all metadata should be discarded
	 *          during processing.
	 * @throws IOException if there is an I/O error during image processing.
	 * @see <a href="http://www.hanhuy.com/pfn/java-image-thumbnail-comparison">A comparison of Java image thumbnailing techniques</a>
	 * @see <a href="https://www.universalwebservices.net/web-programming-resources/java/adjust-jpeg-image-compression-quality-when-saving-images-in-java/">Adjust
	 *      JPEG image compression quality when saving images in Java</a>
	 * @see AspectualArtifact#PROPERTY_TAG_MUMMY_ASPECT
	 */
	protected void processImage(@Nonnull final MummyContext context, @Nonnull Artifact artifact, final InputStream inputStream, final OutputStream outputStream,
			final boolean keepMetadata) throws IOException {
		final Optional<String> foundAspect = artifact.getResourceDescription().findPropertyValue(AspectualArtifact.PROPERTY_TAG_MUMMY_ASPECT).map(Object::toString);
		//determine the correct configuration keys based upon the aspect, if any
		final String configKeyScaleMaxLength = foundAspect.map(aspect -> format(CONFIG_KEY_FORMAT_MUMMY_IMAGE_ASPECT___SCALE_MAX_LENGTH, aspect))
				.orElse(CONFIG_KEY_MUMMY_IMAGE_SCALE_MAX_LENGTH);
		final String configKeyCompressionQuality = foundAspect.map(aspect -> format(CONFIG_KEY_FORMAT_MUMMY_IMAGE_ASPECT___COMPRESSION_QUALITY, aspect))
				.orElse(CONFIG_KEY_MUMMY_IMAGE_COMPRESSION_QUALITY);

		final int imageIndex = 0; //this processing logic assumes that that the first image is the one being processed

		//load
		final ImageInputStream imageInputStream = createImageInputStream(inputStream); //this stream will not be closed in this method, as it wraps a stream provided by the caller
		if(imageInputStream == null) {
			throw new IOException("No suitable image input stream service provider found.");
		}
		final BufferedImage oldImage; //read the first of the images
		final IIOMetadata oldImageMetadata;
		final ImageWriter imageWriter; //determine the writer based on the reader, so we do that while the reader is still valid
		{ //use the reader in a separate scope to keep it from being accidentally used after it is disposed
			final ImageReader imageReader = findNext(getImageReaders(imageInputStream)) //use the first available image reader
					.orElseThrow(() -> new IOException("No service provider image reader available."));
			try {
				final ImageReadParam imageReadParam = imageReader.getDefaultReadParam();
				imageReader.setInput(imageInputStream, true, !keepMetadata); //tell the image reader to read from the image input stream, ignoring metadata if we shouldn't keep metadata
				oldImage = imageReader.read(imageIndex, imageReadParam); //tell the image reader to read the image
				oldImageMetadata = keepMetadata ? imageReader.getImageMetadata(imageIndex) : null; //get any metadata associated with the image if we have been asked to keep it
				imageWriter = getImageWriter(imageReader); //get an image writer that corresponds to the reader
			} finally {
				imageReader.dispose(); //tell the image reader we don't need it any more
			}
		}

		//scale
		final int scaleMaxLength = context.getConfiguration().findInt(configKeyScaleMaxLength).orElse(DEFAULT_SCALE_MAX_LENGTH);
		final int oldWidth = oldImage.getWidth();
		final int oldHeight = oldImage.getHeight();
		final BufferedImage newImage;
		if(oldWidth > scaleMaxLength || oldHeight > scaleMaxLength) { //if this image needs scaled
			final Dimension2D scaledDimensions = ImmutableDimension2D.of(oldWidth, oldHeight).constrainedBy(scaleMaxLength, scaleMaxLength);
			final int newWidth = (int)scaledDimensions.getWidth(); //take the floor value; don't round up to prevent going outside the constraining dimensions
			final int newHeight = (int)scaledDimensions.getHeight();

			//this technique, modified from http://www.hanhuy.com/pfn/java-image-thumbnail-comparison , produces images virtually identical to JAI subsample average but is really slow---but leaves no black lines like the current JAI
			final Image scaledImage = oldImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
			final int oldImageType = oldImage.getType();
			final int newImageType = oldImageType != BufferedImage.TYPE_CUSTOM ? oldImageType //use the existing image type if it isn't custom
					: (oldImage.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB; //otherwise use RGB unless ARGB is needed for transparency
			newImage = new BufferedImage(newWidth, newHeight, newImageType);
			final Graphics2D graphics = newImage.createGraphics();
			try {
				graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				graphics.drawImage(scaledImage, null, null);
			} finally {
				graphics.dispose();
			}
			scaledImage.flush();
		} else { //if the image doesn't need scaled
			newImage = oldImage; //use the original image unchanged
		}

		//write
		try {
			final ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam(); //get default parameters for writing the image
			if(imageWriteParam.canWriteCompressed()) { //if the writer can compress images (if we don't do this check, an exception will be thrown if the image writer doesn't support compression, e.g. for PNG files)
				imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); //indicate that we'll explicitly set the compression quality
				final String[] compressionTypes = imageWriteParam.getCompressionTypes(); //get the available compression types, if any
				if(compressionTypes != null && compressionTypes.length > 0) { //if there are compression types, one must be set or an error will be thrown; for example, a GIF provides compression types "LZW" and "lzw"
					imageWriteParam.setCompressionType(compressionTypes[0]); //use the first available compression type
				}
				imageWriteParam.setCompressionQuality((float)context.getConfiguration().findDouble(configKeyCompressionQuality).orElse(DEFAULT_COMPRESSION_QUALITY));
				if(imageWriteParam instanceof JPEGImageWriteParam) {
					//Important: Optimize the Huffman tables (guaranteeing Huffman tables) as a workaround to avoid a
					//"javax.imageio.IIOException: Missing Huffman code table entry" inside JPEGImageWriter.writeImage()
					//for some images; see https://stackoverflow.com/a/62240696 .
					((JPEGImageWriteParam)imageWriteParam).setOptimizeHuffmanTables(true);
				}
			}
			final ImageOutputStream imageOutputStream = createImageOutputStream(outputStream); //this stream will not be closed in this method, as it wraps a stream provided by the caller
			if(imageOutputStream == null) {
				throw new IOException("No suitable image output stream service provider found.");
			}
			imageWriter.setOutput(imageOutputStream); //tell the image writer to write to the image output stream
			final IIOImage iioImage = new IIOImage(newImage, null, oldImageMetadata); //write with no thumbnails, but try to keep metadata (if we read and kept any)
			imageWriter.write(null, iioImage, imageWriteParam); //tell the image writer to read the image using the custom parameters
		} finally {
			imageWriter.dispose(); //tell the image writer we don't need it any more
		}
	}

}
