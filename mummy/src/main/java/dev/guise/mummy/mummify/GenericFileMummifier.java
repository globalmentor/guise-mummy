/*
 * Copyright © 2026 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package dev.guise.mummy.mummify;

import static com.globalmentor.io.Files.*;
import static com.globalmentor.net.MediaType.*;
import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.*;
import static java.util.Collections.*;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;

import com.globalmentor.io.Paths;
import com.globalmentor.net.MediaType;

import dev.guise.mummy.*;

/// General-purpose mummifier for files without a specialized mummifier.
///
/// Like [OpaqueFileMummifier], this mummifier copies the file unchanged during mummification. Unlike `OpaqueFileMummifier`,
/// it attempts to determine the artifact's media type from the source filename extension using a built-in table of
/// common Internet media types.
///
/// @apiNote This mummifier is intended to serve as the system default for files that no other mummifier claims.
///          `OpaqueFileMummifier` remains available for cases where even extension-based detection is undesirable.
/// @author Garret Wilson
/// @see OpaqueFileMummifier
public class GenericFileMummifier extends AbstractFileMummifier {

	/// Common Internet media types indexed by lowercase filename extension.
	///
	/// @see <a href="https://github.com/apache/httpd/blob/trunk/docs/conf/mime.types">Apache HTTP Server mime.types</a>
	/// @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/MIME_types/Common_types">Mozilla Common MIME types</a>
	/// @see <a href="https://github.com/apache/tomcat/blob/main/java/org/apache/catalina/startup/MimeTypeMappings.properties">Apache Tomcat MIME type mappings</a>
	public static final Map<String, MediaType> DEFAULT_MEDIA_TYPES_BY_FILENAME_EXTENSION = Map.ofEntries( //TODO move to a common library
			//## text
			Map.entry("css", of(TEXT_PRIMARY_TYPE, "css")), // Cascading Style Sheets (Apache, Mozilla, Tomcat)
			Map.entry("csv", of(TEXT_PRIMARY_TYPE, "csv")), // Comma-separated values (Apache, Mozilla, Tomcat)
			Map.entry("htm", of(TEXT_PRIMARY_TYPE, "html")), // HyperText Markup Language (Apache, Mozilla, Tomcat)
			Map.entry("html", of(TEXT_PRIMARY_TYPE, "html")), // HyperText Markup Language (Apache, Mozilla, Tomcat)
			Map.entry("ics", of(TEXT_PRIMARY_TYPE, "calendar")), // iCalendar format (Apache, Mozilla, Tomcat)
			Map.entry("js", of(TEXT_PRIMARY_TYPE, "javascript")), // JavaScript (Apache, Mozilla, Tomcat)
			Map.entry("md", of(TEXT_PRIMARY_TYPE, "markdown")), // Markdown (Mozilla)
			Map.entry("mjs", of(TEXT_PRIMARY_TYPE, "javascript")), // JavaScript module (Apache, Mozilla, Tomcat)
			Map.entry("txt", of(TEXT_PRIMARY_TYPE, "plain")), // plain text (Apache, Mozilla, Tomcat)
			//## images
			Map.entry("apng", of(IMAGE_PRIMARY_TYPE, "apng")), // Animated Portable Network Graphics (Mozilla)
			Map.entry("avif", of(IMAGE_PRIMARY_TYPE, "avif")), // AVIF image (Apache, Mozilla, Tomcat)
			Map.entry("bmp", of(IMAGE_PRIMARY_TYPE, "bmp")), // Bitmap Graphics (Apache, Mozilla, Tomcat)
			Map.entry("gif", of(IMAGE_PRIMARY_TYPE, "gif")), // Graphics Interchange Format (Apache, Mozilla, Tomcat)
			Map.entry("heic", of(IMAGE_PRIMARY_TYPE, "heic")), // HEIC image (Apple) (Apache)
			Map.entry("heif", of(IMAGE_PRIMARY_TYPE, "heif")), // HEIF image (Apple) (Apache)
			Map.entry("ico", of(IMAGE_PRIMARY_TYPE, "vnd.microsoft.icon")), // Icon format (Apache, Mozilla, Tomcat): Apache: `image/x-icon`, Tomcat: `image/x-icon`
			Map.entry("jpeg", of(IMAGE_PRIMARY_TYPE, "jpeg")), // JPEG image (Apache, Mozilla, Tomcat)
			Map.entry("jpg", of(IMAGE_PRIMARY_TYPE, "jpeg")), // JPEG image (Apache, Mozilla, Tomcat)
			Map.entry("jxl", of(IMAGE_PRIMARY_TYPE, "jxl")), // JPEG XL (Apache, Tomcat)
			Map.entry("png", of(IMAGE_PRIMARY_TYPE, "png")), // Portable Network Graphics (Apache, Mozilla, Tomcat)
			Map.entry("svg", of(IMAGE_PRIMARY_TYPE, "svg+xml")), // Scalable Vector Graphics (Apache, Mozilla, Tomcat)
			Map.entry("svgz", of(IMAGE_PRIMARY_TYPE, "svg+xml")), // Scalable Vector Graphics (compressed) (Apache, Tomcat)
			Map.entry("tif", of(IMAGE_PRIMARY_TYPE, "tiff")), // Tagged Image File Format (Apache, Mozilla, Tomcat)
			Map.entry("tiff", of(IMAGE_PRIMARY_TYPE, "tiff")), // Tagged Image File Format (Apache, Mozilla, Tomcat)
			Map.entry("webp", of(IMAGE_PRIMARY_TYPE, "webp")), // WebP image (Apache, Mozilla, Tomcat)
			//## audio
			Map.entry("aac", of(AUDIO_PRIMARY_TYPE, "aac")), // AAC audio (Apache, Mozilla, Tomcat): Apache: `audio/x-aac`, Tomcat: `audio/x-aac`
			Map.entry("flac", of(AUDIO_PRIMARY_TYPE, "flac")), // FLAC audio (Apache, Tomcat): Apache: `audio/x-flac`
			Map.entry("m4a", of(AUDIO_PRIMARY_TYPE, "mp4")), // MPEG-4 audio (Apache, Tomcat)
			Map.entry("mid", of(AUDIO_PRIMARY_TYPE, "midi")), // Musical Instrument Digital Interface (Apache, Mozilla, Tomcat)
			Map.entry("midi", of(AUDIO_PRIMARY_TYPE, "midi")), // Musical Instrument Digital Interface (Apache, Mozilla, Tomcat)
			Map.entry("mp3", of(AUDIO_PRIMARY_TYPE, "mpeg")), // MP3 audio (Apache, Mozilla, Tomcat)
			Map.entry("oga", of(AUDIO_PRIMARY_TYPE, "ogg")), // Ogg audio (Apache, Mozilla, Tomcat)
			Map.entry("ogg", of(AUDIO_PRIMARY_TYPE, "ogg")), // Ogg audio (Apache, Tomcat)
			Map.entry("opus", of(AUDIO_PRIMARY_TYPE, "ogg")), // Opus audio in Ogg container (Apache, Mozilla, Tomcat)
			Map.entry("wav", of(AUDIO_PRIMARY_TYPE, "wav")), // Waveform Audio Format (Apache, Mozilla, Tomcat): Apache: `audio/x-wav`, Tomcat: `audio/x-wav`
			Map.entry("weba", of(AUDIO_PRIMARY_TYPE, "webm")), // WebM audio (Apache, Mozilla, Tomcat)
			//## video
			Map.entry("avi", of(VIDEO_PRIMARY_TYPE, "x-msvideo")), // AVI: Audio Video Interleave (Apache, Mozilla, Tomcat)
			Map.entry("m4v", of(VIDEO_PRIMARY_TYPE, "x-m4v")), // MPEG-4 video (Apple) (Apache, Tomcat): Tomcat: `video/mp4`
			Map.entry("mkv", of(VIDEO_PRIMARY_TYPE, "x-matroska")), // Matroska video (Apache, Tomcat)
			Map.entry("mov", of(VIDEO_PRIMARY_TYPE, "quicktime")), // QuickTime video (Apache, Tomcat)
			Map.entry("mp4", of(VIDEO_PRIMARY_TYPE, "mp4")), // MP4 video (Apache, Mozilla, Tomcat)
			Map.entry("mpeg", of(VIDEO_PRIMARY_TYPE, "mpeg")), // MPEG video (Apache, Mozilla, Tomcat)
			Map.entry("mpg", of(VIDEO_PRIMARY_TYPE, "mpeg")), // MPEG video (Apache, Tomcat)
			Map.entry("ogv", of(VIDEO_PRIMARY_TYPE, "ogg")), // Ogg video (Apache, Mozilla, Tomcat)
			Map.entry("qt", of(VIDEO_PRIMARY_TYPE, "quicktime")), // QuickTime video (Apache, Tomcat)
			Map.entry("ts", of(VIDEO_PRIMARY_TYPE, "mp2t")), // MPEG transport stream (Apache, Mozilla, Tomcat)
			Map.entry("webm", of(VIDEO_PRIMARY_TYPE, "webm")), // WebM video (Apache, Mozilla, Tomcat)
			Map.entry("3gp", of(VIDEO_PRIMARY_TYPE, "3gpp")), // 3GPP audio/video container (Apache, Mozilla, Tomcat)
			Map.entry("3g2", of(VIDEO_PRIMARY_TYPE, "3gpp2")), // 3GPP2 audio/video container (Apache, Mozilla, Tomcat)
			//## fonts
			Map.entry("eot", of(APPLICATION_PRIMARY_TYPE, "vnd.ms-fontobject")), // MS Embedded OpenType fonts (Apache, Mozilla, Tomcat)
			Map.entry("otf", of("font", "otf")), // OpenType font (Apache, Mozilla, Tomcat)
			Map.entry("ttc", of("font", "collection")), // TrueType font collection (Apache, Tomcat)
			Map.entry("ttf", of("font", "ttf")), // TrueType font (Apache, Mozilla, Tomcat)
			Map.entry("woff", of("font", "woff")), // Web Open Font Format (Apache, Mozilla, Tomcat)
			Map.entry("woff2", of("font", "woff2")), // Web Open Font Format 2 (Apache, Mozilla, Tomcat)
			//## application
			Map.entry("abw", of(APPLICATION_PRIMARY_TYPE, "x-abiword")), // AbiWord document (Apache, Mozilla, Tomcat)
			Map.entry("arc", of(APPLICATION_PRIMARY_TYPE, "x-freearc")), // Archive document (Apache, Mozilla, Tomcat)
			Map.entry("atom", of(APPLICATION_PRIMARY_TYPE, "atom+xml")), // Atom syndication feed (Apache, Tomcat)
			Map.entry("azw", of(APPLICATION_PRIMARY_TYPE, "vnd.amazon.ebook")), // Amazon Kindle eBook (Apache, Mozilla, Tomcat)
			Map.entry("bin", of(APPLICATION_PRIMARY_TYPE, "octet-stream")), // binary data (Apache, Mozilla, Tomcat)
			Map.entry("bz", of(APPLICATION_PRIMARY_TYPE, "x-bzip")), // BZip archive (Apache, Mozilla, Tomcat)
			Map.entry("bz2", of(APPLICATION_PRIMARY_TYPE, "x-bzip2")), // BZip2 archive (Apache, Mozilla, Tomcat)
			Map.entry("cda", of(APPLICATION_PRIMARY_TYPE, "x-cdf")), // CD audio (Mozilla)
			Map.entry("csh", of(APPLICATION_PRIMARY_TYPE, "x-csh")), // C-Shell script (Apache, Mozilla, Tomcat)
			Map.entry("doc", of(APPLICATION_PRIMARY_TYPE, "msword")), // Microsoft Word (Apache, Mozilla, Tomcat)
			Map.entry("docx", of(APPLICATION_PRIMARY_TYPE, "vnd.openxmlformats-officedocument.wordprocessingml.document")), // Microsoft Word OpenXML (Apache, Mozilla, Tomcat)
			Map.entry("epub", of(APPLICATION_PRIMARY_TYPE, "epub+zip")), // Electronic publication (Apache, Mozilla, Tomcat)
			Map.entry("gz", of(APPLICATION_PRIMARY_TYPE, "gzip")), // GZip compressed archive (Mozilla, Tomcat): Tomcat: `application/x-gzip`
			Map.entry("jar", of(APPLICATION_PRIMARY_TYPE, "java-archive")), // Java Archive (Apache, Mozilla, Tomcat)
			Map.entry("json", of(APPLICATION_PRIMARY_TYPE, "json")), // JSON format (Apache, Mozilla, Tomcat)
			Map.entry("jsonld", of(APPLICATION_PRIMARY_TYPE, "ld+json")), // JSON-LD format (Mozilla)
			Map.entry("m3u8", of(APPLICATION_PRIMARY_TYPE, "vnd.apple.mpegurl")), // HLS streaming playlist (Apache, Tomcat)
			Map.entry("mpkg", of(APPLICATION_PRIMARY_TYPE, "vnd.apple.installer+xml")), // Apple Installer Package (Apache, Mozilla, Tomcat)
			Map.entry("odp", of(APPLICATION_PRIMARY_TYPE, "vnd.oasis.opendocument.presentation")), // OpenDocument presentation (Apache, Mozilla, Tomcat)
			Map.entry("ods", of(APPLICATION_PRIMARY_TYPE, "vnd.oasis.opendocument.spreadsheet")), // OpenDocument spreadsheet (Apache, Mozilla, Tomcat)
			Map.entry("odt", of(APPLICATION_PRIMARY_TYPE, "vnd.oasis.opendocument.text")), // OpenDocument text (Apache, Mozilla, Tomcat)
			Map.entry("ogx", of(APPLICATION_PRIMARY_TYPE, "ogg")), // Ogg (Apache, Mozilla, Tomcat)
			Map.entry("pdf", of(APPLICATION_PRIMARY_TYPE, "pdf")), // Adobe Portable Document Format (Apache, Mozilla, Tomcat)
			Map.entry("php", of(APPLICATION_PRIMARY_TYPE, "x-httpd-php")), // PHP (Mozilla)
			Map.entry("ppt", of(APPLICATION_PRIMARY_TYPE, "vnd.ms-powerpoint")), // Microsoft PowerPoint (Apache, Mozilla, Tomcat)
			Map.entry("pptx", of(APPLICATION_PRIMARY_TYPE, "vnd.openxmlformats-officedocument.presentationml.presentation")), // Microsoft PowerPoint OpenXML (Apache, Mozilla, Tomcat)
			Map.entry("rar", of(APPLICATION_PRIMARY_TYPE, "vnd.rar")), // RAR archive (Apache, Mozilla, Tomcat): Apache: `application/x-rar-compressed`, Tomcat: `application/x-rar-compressed`
			Map.entry("rss", of(APPLICATION_PRIMARY_TYPE, "rss+xml")), // RSS syndication feed (Apache, Tomcat)
			Map.entry("rtf", of(APPLICATION_PRIMARY_TYPE, "rtf")), // Rich Text Format (Apache, Mozilla, Tomcat)
			Map.entry("sh", of(APPLICATION_PRIMARY_TYPE, "x-sh")), // Bourne shell script (Apache, Mozilla, Tomcat)
			Map.entry("tar", of(APPLICATION_PRIMARY_TYPE, "x-tar")), // Tape Archive (Apache, Mozilla, Tomcat)
			Map.entry("vsd", of(APPLICATION_PRIMARY_TYPE, "vnd.visio")), // Microsoft Visio (Apache, Mozilla, Tomcat)
			Map.entry("wasm", of(APPLICATION_PRIMARY_TYPE, "wasm")), // WebAssembly (Apache, Tomcat)
			Map.entry("webmanifest", of(APPLICATION_PRIMARY_TYPE, "manifest+json")), // Web application manifest (Mozilla)
			Map.entry("xhtml", of(APPLICATION_PRIMARY_TYPE, "xhtml+xml")), // XHTML (Apache, Mozilla, Tomcat)
			Map.entry("xls", of(APPLICATION_PRIMARY_TYPE, "vnd.ms-excel")), // Microsoft Excel (Apache, Mozilla, Tomcat)
			Map.entry("xlsx", of(APPLICATION_PRIMARY_TYPE, "vnd.openxmlformats-officedocument.spreadsheetml.sheet")), // Microsoft Excel OpenXML (Apache, Mozilla, Tomcat)
			Map.entry("xml", of(APPLICATION_PRIMARY_TYPE, "xml")), // XML (Apache, Mozilla, Tomcat)
			Map.entry("xul", of(APPLICATION_PRIMARY_TYPE, "vnd.mozilla.xul+xml")), // XUL (Apache, Mozilla, Tomcat)
			Map.entry("zip", of(APPLICATION_PRIMARY_TYPE, "zip")), // ZIP archive (Apache, Mozilla, Tomcat)
			Map.entry("7z", of(APPLICATION_PRIMARY_TYPE, "x-7z-compressed")) // 7-zip archive (Apache, Mozilla, Tomcat)
	);

	/// Constructor.
	public GenericFileMummifier() {
	}

	@Override
	public Set<String> getSupportedFilenameExtensions() {
		return emptySet();
	}

	/// {@inheritDoc}
	/// @implSpec This implementation looks up the source filename extension in [#DEFAULT_MEDIA_TYPES_BY_FILENAME_EXTENSION].
	@Override
	public Optional<MediaType> getArtifactMediaType(final MummyContext context, final Path sourcePath) throws IOException {
		return Paths.findFilenameExtension(sourcePath).map(com.globalmentor.io.Filenames.Extensions::normalize)
				.flatMap(ext -> Optional.ofNullable(DEFAULT_MEDIA_TYPES_BY_FILENAME_EXTENSION.get(ext)));
	}

	/// {@inheritDoc}
	/// @implSpec This implementation returns an empty list, as generic files have no known source metadata beyond their media type.
	@Override
	protected List<Entry<URI, Object>> loadSourceMetadata(final MummyContext context, final Path sourceFile) throws IOException {
		return emptyList();
	}

	/// {@inheritDoc}
	/// @implSpec This implementation merely copies the file with no further action.
	@Override
	public void mummifyFile(final MummyContext context, final CorporealSourceArtifact artifact) throws IOException {
		final Path sourceFile = artifact.getSourcePath();
		checkArgumentRegularFile(sourceFile);
		copy(sourceFile, artifact.getTargetPath(), REPLACE_EXISTING);
	}

}
