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
import static javax.imageio.ImageIO.*;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.annotation.*;
import javax.imageio.*;
import javax.imageio.ImageWriter;
import javax.imageio.stream.*;

import com.globalmentor.awt.geom.ImmutableDimension2D;

import io.guise.mummy.*;

/**
 * General image mummifier.
 * @implSpec This implementation supports GIF, JPEG, and PNG files.
 * @implSpec This implementation uses <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.desktop/javax/imageio/package-summary.html">Java Image
 *           I/O</a> for image processing.
 * @author Garret Wilson
 */
public class DefaultImageMummifier extends BaseImageMummifier {

	/** @see #CONFIG_KEY_MUMMY_IMAGE_SCALE_COMPRESSION_QUALITY */
	public static final double DEFAULT_SCALE_COMPRESSION_QUALITY = 0.8;

	/** @see #CONFIG_KEY_MUMMY_IMAGE_SCALE_MAX_LENGTH */
	public static final int DEFAULT_SCALE_MAX_LENGTH = 2560;

	/** @see #CONFIG_KEY_MUMMY_IMAGE_PROCESS_THRESHOLD_FILE_SIZE */
	public static final long DEFAULT_SCALE_THRESHOLD_FILE_SIZE = 1_000_000;

	/** No-args constructor. */
	public DefaultImageMummifier() {
		super(Set.of(GIF_MEDIA_TYPE, JPEG_MEDIA_TYPE, PNG_MEDIA_TYPE));
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation scales the image in an attempt to reduce the file size if the file size is above a certain threshold.
	 * @implSpec This implementation delegates to {@link #processImage(MummyContext, Artifact, InputStream, OutputStream)} for scaling.
	 */
	@Override
	public void mummifyFile(final MummyContext context, final Artifact artifact) throws IOException {
		final Path sourceFile = artifact.getSourcePath();
		final long imageScaleThresholdSize = context.getConfiguration().findLong(CONFIG_KEY_MUMMY_IMAGE_PROCESS_THRESHOLD_FILE_SIZE)
				.orElse(DEFAULT_SCALE_THRESHOLD_FILE_SIZE);
		if(size(sourceFile) > imageScaleThresholdSize) { //if the size of the image source file goes over our threshold for scaling
			try (final InputStream inputStream = new BufferedInputStream(newInputStream(sourceFile));
					final OutputStream outputStream = new BufferedOutputStream(newOutputStream(artifact.getTargetPath()))) {
				processImage(context, artifact, inputStream, outputStream);
				return;
			} catch(final IOException ioException) { //provide more context to I/O errors
				throw new IOException(format("Error processing image `%s`: %s", artifact.getSourcePath(), ioException.getLocalizedMessage()), ioException); //TODO i18n
			}
		}

		copy(sourceFile, artifact.getTargetPath(), REPLACE_EXISTING);
	}

	/**
	 * Processes an image from the given input stream and writes the processed image to the given output stream.
	 * @implSpec This implementation scales an image using the AWT to draw on a scaled image using bicubic interpolation and quality-biased rendering.
	 * @param context The context of static site generation.
	 * @param artifact The artifact being generated.
	 * @param inputStream The input stream for reading the source image.
	 * @param outputStream The output stream for writing the target image.
	 * @throws IOException if there is an I/O error during image processing.
	 * @see <a href="http://www.hanhuy.com/pfn/java-image-thumbnail-comparison">A comparison of Java image thumbnailing techniques</a>
	 * @see <a href="https://www.universalwebservices.net/web-programming-resources/java/adjust-jpeg-image-compression-quality-when-saving-images-in-java/">Adjust
	 *      JPEG image compression quality when saving images in Java</a>
	 */
	protected void processImage(@Nonnull final MummyContext context, @Nonnull Artifact artifact, final InputStream inputStream, final OutputStream outputStream)
			throws IOException {
		//load
		final ImageInputStream imageInputStream = createImageInputStream(inputStream); //this stream will not be closed in this method, as it wraps a stream provided by the caller
		if(imageInputStream == null) {
			throw new IOException("No suitable image input stream service provider found.");
		}
		final ImageReader imageReader = findNext(getImageReaders(imageInputStream)) //use the first available image reader
				.orElseThrow(() -> new IOException("No service provider image reader available."));
		final ImageReadParam imageReadParam = imageReader.getDefaultReadParam();
		imageReader.setInput(imageInputStream, true, true); //tell the image reader to read from the image input stream
		final BufferedImage oldImage; //read the first of the images
		try {
			oldImage = imageReader.read(0, imageReadParam); //tell the image reader to read the image
		} finally {
			imageReader.dispose(); //tell the image reader we don't need it any more
		}

		//scale
		final int scaleMaxLength = context.getConfiguration().findInt(CONFIG_KEY_MUMMY_IMAGE_SCALE_MAX_LENGTH).orElse(DEFAULT_SCALE_MAX_LENGTH);
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
		final ImageWriter imageWriter = getImageWriter(imageReader); //get an image writer that corresponds to the reader; this will hopefully conserve the metadata as well
		try {
			final ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam(); //get default parameters for writing the image
			if(imageWriteParam.canWriteCompressed()) { //if the writer can compress images (if we don't do this check, an exception will be thrown if the image writer doesn't support compression, e.g. for PNG files)
				imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); //indicate that we'll explicitly set the compression quality
				final String[] compressionTypes = imageWriteParam.getCompressionTypes(); //get the available compression types, if any
				if(compressionTypes != null && compressionTypes.length > 0) { //if there are compression types, one must be set or an error will be thrown; for example, a GIF provides compression types "LZW" and "lzw"
					imageWriteParam.setCompressionType(compressionTypes[0]); //use the first available compression type
				}
				imageWriteParam.setCompressionQuality(
						(float)context.getConfiguration().findDouble(CONFIG_KEY_MUMMY_IMAGE_SCALE_COMPRESSION_QUALITY).orElse(DEFAULT_SCALE_COMPRESSION_QUALITY));
			}
			final ImageOutputStream imageOutputStream = createImageOutputStream(outputStream); //this stream will not be closed in this method, as it wraps a stream provided by the caller
			if(imageOutputStream == null) {
				throw new IOException("No suitable image output stream service provider found.");
			}
			imageWriter.setOutput(imageOutputStream); //tell the image writer to write to the image output stream
			final IIOImage iioImage = new IIOImage(newImage, null, null); //write no thumbnails TODO fix to preserving the metadata, if any, we got from the original file
			imageWriter.write(null, iioImage, imageWriteParam); //tell the image writer to read the image using the custom parameters
		} finally {
			imageWriter.dispose(); //tell the image writer we don't need it any more
		}
	}

}
