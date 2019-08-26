/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.cli;

import static com.globalmentor.io.Files.*;
import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.java.OperatingSystem.*;
import static com.globalmentor.net.HTTP.*;
import static com.globalmentor.net.URIs.*;
import static io.guise.mummy.GuiseMummy.*;
import static java.nio.file.Files.*;
import static java.util.Collections.*;
import static org.fusesource.jansi.Ansi.*;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import javax.annotation.*;

import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.fusesource.jansi.Ansi;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.slf4j.event.Level;

import com.github.dtmo.jfiglet.*;
import com.globalmentor.application.*;
import com.globalmentor.net.URIs;

import io.clogr.Clogr;
import io.confound.config.Configuration;
import io.confound.config.ConfigurationException;
import io.confound.config.file.ResourcesConfigurationManager;
import io.guise.catalina.webresources.SiteRoot;
import io.guise.mummy.*;
import picocli.CommandLine.*;

/**
 * Command-line interface for Guise tasks.
 * @author Garret Wilson
 */
@Command(name = "guise", description = "Command-line interface for Guise tasks.", versionProvider = GuiseCli.MetadataProvider.class, mixinStandardHelpOptions = true)
public class GuiseCli extends BaseCliApplication {

	public static final String CONFIG_KEY_SERVER_DIRECTORY = "server.directory";
	public static final String CONFIG_KEY_SERVER_PORT = "server.port";

	/** The default server port used by the <code>serve</code> command. */
	private final static int DEFAULT_SERVER_PORT = 4040;

	/**
	 * Constructor.
	 * @param args The command line arguments.
	 */
	public GuiseCli(@Nonnull final String[] args) {
		super(args);
		//bridge JUL to SLF4J for Tomcat logging
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

	/**
	 * Main program entry method.
	 * @param args Program arguments.
	 */
	public static void main(@Nonnull final String[] args) {
		Application.start(new GuiseCli(args));
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version defaults to {@link Level#INFO} log level.
	 */
	@Override
	protected void updateLogLevel() {
		final Level logLevel = isDebug() ? Level.DEBUG : Level.INFO;
		Clogr.getLoggingConcern().setLogLevel(logLevel);
	}

	/**
	 * Prints startup app information, including application banner, name, and version.
	 * @throws ConfigurationException if some configuration information isn't present.
	 */
	protected void printAppInfo() {
		final FigletRenderer figletRenderer;
		final Configuration appConfiguration;
		try {
			appConfiguration = ResourcesConfigurationManager.loadConfigurationForClass(getClass())
					.orElseThrow(ResourcesConfigurationManager::createConfigurationNotFoundException);
			figletRenderer = new FigletRenderer(FigFontResources.loadFigFontResource(FigFontResources.BIG_FLF));
		} catch(final IOException ioException) {
			throw new ConfigurationException(ioException);
		}
		System.out.print(ansi().fg(Ansi.Color.GREEN));
		System.out.print(figletRenderer.renderText("Guise"));
		System.out.println(ansi().reset());
		System.out.println(appConfiguration.getString(CONFIG_KEY_NAME) + " " + appConfiguration.getString(CONFIG_KEY_VERSION));
		System.out.println();
	}

	@Command(description = "Validates a Guise project before mummification.")
	public void validate(
			@Parameters(paramLabel = "<project>", description = "The base directory of the project to mummify.%nDefaults to the current working directory.", arity = "0..1") @Nullable Path argProjectDirectory,
			@Option(names = "--site-source-dir", description = "The source root directory of the site to mummify.%nDefaults to @|bold src/site/|@ relative to the project base directory.") @Nullable Path argSiteSourceDirectory,
			@Option(names = "--site-target-dir", description = "The target root directory into which the site will be generated; will be created if needed.%nDefaults to @|bold target/site/|@ relative to the project base directory.") @Nullable Path argSiteTargetDirectory,
			@Option(names = "--site-description-target-dir", description = "The target root directory into which the site description will be generated; will be created if needed.%nDefaults to @|bold target/site-description/|@ relative to the project base directory.") @Nullable Path argSiteDescriptionTargetDirectory,
			@Option(names = {"--debug", "-d"}, description = "Turns on debug level logging.") final boolean debug) {

		setDebug(debug); //TODO inherit from base class; see https://github.com/remkop/picocli/issues/649

		printAppInfo();

		final Path projectDirectory = argProjectDirectory != null ? argProjectDirectory : getWorkingDirectory();

		final GuiseMummy mummifier = new GuiseMummy();
		try {
			final GuiseProject project = GuiseMummy.createProject(projectDirectory.toAbsolutePath(), argSiteSourceDirectory, argSiteTargetDirectory,
					argSiteDescriptionTargetDirectory);

			getLogger().info("Validate...");
			getLogger().info("Project directory: {}", projectDirectory);
			getLogger().info("Site source directory: {}", project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY));
			getLogger().info("Site target directory: {}", project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY));
			getLogger().info("Site description target directory: {}", project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY));

			mummifier.mummify(project, GuiseMummy.LifeCyclePhase.VALIDATE);
		} catch(final IllegalArgumentException | IOException exception) {
			getLogger().error("{}", exception.getMessage());
			getLogger().debug("{}", exception.getMessage(), exception);
		}
	}

	@Command(description = "Cleans a site by removing the site target directory.")
	public void clean(
			@Parameters(paramLabel = "<project>", description = "The base directory of the project to mummify.%nDefaults to the current working directory.", arity = "0..1") @Nullable Path argProjectDirectory,
			@Option(names = "--site-target-dir", description = "The target root directory of the site to be removed; will be created if needed.%nDefaults to @|bold target/site/|@ relative to the project base directory.") @Nullable Path argSiteTargetDirectory,
			@Option(names = "--site-description-target-dir", description = "The target root directory of the site description to be removed; will be created if needed.%nDefaults to @|bold target/site-description/|@ relative to the project base directory.") @Nullable Path argSiteDescriptionTargetDirectory,
			@Option(names = {"--debug", "-d"}, description = "Turns on debug level logging.") final boolean debug) {

		setDebug(debug); //TODO inherit from base class; see https://github.com/remkop/picocli/issues/649

		printAppInfo();

		final Path projectDirectory = argProjectDirectory != null ? argProjectDirectory : getWorkingDirectory();

		try {
			final GuiseProject project = GuiseMummy.createProject(projectDirectory.toAbsolutePath(), null, argSiteTargetDirectory, argSiteDescriptionTargetDirectory);
			final Path siteTargetDirectory = project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY);
			final Path siteDescriptionTargetDirectory = project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY);

			getLogger().info("Clean...");
			getLogger().info("Project directory: {}", projectDirectory);
			getLogger().info("Site target directory: {}", siteTargetDirectory);
			getLogger().info("Site description target directory: {}", siteDescriptionTargetDirectory);

			if(exists(siteTargetDirectory)) {
				deleteFileTree(siteTargetDirectory);
			}
			if(!siteTargetDirectory.equals(siteDescriptionTargetDirectory)) {
				if(exists(siteDescriptionTargetDirectory)) {
					deleteFileTree(siteDescriptionTargetDirectory);
				}
			}
		} catch(final IllegalArgumentException | IOException exception) {
			getLogger().error("{}", exception.getMessage());
			getLogger().debug("{}", exception.getMessage(), exception);
		}
	}

	@Command(description = "Mummifies a site by generating a static version.")
	public void mummify(
			@Parameters(paramLabel = "<project>", description = "The base directory of the project to mummify.%nDefaults to the current working directory.", arity = "0..1") @Nullable Path argProjectDirectory,
			@Option(names = "--site-source-dir", description = "The source root directory of the site to mummify.%nDefaults to @|bold src/site/|@ relative to the project base directory.") @Nullable Path argSiteSourceDirectory,
			@Option(names = "--site-target-dir", description = "The target root directory into which the site will be generated; will be created if needed.%nDefaults to @|bold target/site/|@ relative to the project base directory.") @Nullable Path argSiteTargetDirectory,
			@Option(names = "--site-description-target-dir", description = "The target root directory into which the site description will be generated; will be created if needed.%nDefaults to @|bold target/site-description/|@ relative to the project base directory.") @Nullable Path argSiteDescriptionTargetDirectory,
			@Option(names = {"--debug", "-d"}, description = "Turns on debug level logging.") final boolean debug) {

		setDebug(debug); //TODO inherit from base class; see https://github.com/remkop/picocli/issues/649

		printAppInfo();

		final Path projectDirectory = argProjectDirectory != null ? argProjectDirectory : getWorkingDirectory();

		final GuiseMummy mummifier = new GuiseMummy();
		try {
			final GuiseProject project = GuiseMummy.createProject(projectDirectory.toAbsolutePath(), argSiteSourceDirectory, argSiteTargetDirectory,
					argSiteDescriptionTargetDirectory);

			getLogger().info("Mummify...");
			getLogger().info("Project directory: {}", projectDirectory);
			getLogger().info("Site source directory: {}", project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY));
			getLogger().info("Site target directory: {}", project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY));
			getLogger().info("Site description target directory: {}", project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY));

			mummifier.mummify(project, GuiseMummy.LifeCyclePhase.MUMMIFY);
		} catch(final IllegalArgumentException | IOException exception) {
			getLogger().error("{}", exception.getMessage());
			getLogger().debug("{}", exception.getMessage(), exception);
		}
	}

	@Command(description = "Deploys a site after generating a static version.")
	public void deploy(
			@Parameters(paramLabel = "<project>", description = "The base directory of the project to deploy.%nDefaults to the current working directory.", arity = "0..1") @Nullable Path argProjectDirectory,
			@Option(names = "--site-source-dir", description = "The source root directory of the site to mummify.%nDefaults to @|bold src/site/|@ relative to the project base directory.") @Nullable Path argSiteSourceDirectory,
			@Option(names = "--site-target-dir", description = "The target root directory into which the site will be generated; will be created if needed.%nDefaults to @|bold target/site/|@ relative to the project base directory.") @Nullable Path argSiteTargetDirectory,
			@Option(names = "--site-description-target-dir", description = "The target root directory into which the site description will be generated; will be created if needed.%nDefaults to @|bold target/site-description/|@ relative to the project base directory.") @Nullable Path argSiteDescriptionTargetDirectory,
			@Option(names = {"--browse", "-b"}, description = "Opens a browser to the site after starting the server.") final boolean browse,
			@Option(names = {"--debug", "-d"}, description = "Turns on debug level logging.") final boolean debug) {

		setDebug(debug); //TODO inherit from base class; see https://github.com/remkop/picocli/issues/649

		printAppInfo();

		final Path projectDirectory = argProjectDirectory != null ? argProjectDirectory : getWorkingDirectory();

		final GuiseMummy mummifier = new GuiseMummy();
		try {
			final GuiseProject project = GuiseMummy.createProject(projectDirectory.toAbsolutePath(), argSiteSourceDirectory, argSiteTargetDirectory,
					argSiteDescriptionTargetDirectory);

			getLogger().info("Mummify...");
			getLogger().info("Project directory: {}", projectDirectory);
			getLogger().info("Site source directory: {}", project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY));
			getLogger().info("Site target directory: {}", project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY));
			getLogger().info("Site description target directory: {}", project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY));

			mummifier.mummify(project, GuiseMummy.LifeCyclePhase.DEPLOY);
		} catch(final IllegalArgumentException | IOException exception) {
			getLogger().error("{}", exception.getMessage());
			getLogger().debug("{}", exception.getMessage(), exception);
			return; //TODO improve error handling
		}

		//launch the browser; see https://stackoverflow.com/a/5226244/421049
		if(browse && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {

			mummifier.getDeployUrls().stream().findFirst().ifPresent(deployUrl -> {
				try {
					Desktop.getDesktop().browse(deployUrl);
				} catch(final IOException exception) {
					getLogger().error("{}", exception.getMessage());
					getLogger().debug("{}", exception.getMessage(), exception);
					return; //TODO improve error handling
				}
			});
		}

	}

	/** The relative path of the server base directory; meant to be used in conjunction with the temporary directory. */
	private static final Path SERVER_RELATIVE_BASE_DIRECTORY = Paths.get("guise", "mummy", "server"); //TODO use constants

	@Command(description = "Starts a web server for exploring the site in the target directory.")
	public void serve(
			@Parameters(paramLabel = "<project>", description = "The base directory of the project being served.%nDefaults to the current working directory.", arity = "0..1") @Nullable Path argProjectDirectory,
			@Option(names = "--site-target-dir", description = "The target root directory of the site to be served.%nDefaults to @|bold target/site/|@ relative to the project base directory.") @Nullable Path argSiteTargetDirectory,
			@Option(names = "--site-description-target-dir", description = "The target root directory of the description of the site to be served.%nDefaults to @|bold target/site-description/|@ relative to the project base directory.") @Nullable Path argSiteDescriptionTargetDirectory,
			@Option(names = {"--port", "-p"}, description = "Specifies the server port.%nDefaults to @|bold " + DEFAULT_SERVER_PORT + "|@.") Integer argPort,
			@Option(names = {"--browse", "-b"}, description = "Opens a browser to the site after starting the server.") final boolean browse,
			@Option(names = {"--debug", "-d"}, description = "Turns on debug level logging.") final boolean debug) {

		setDebug(debug); //TODO inherit from base class; see https://github.com/remkop/picocli/issues/649

		printAppInfo();

		final Path projectDirectory = argProjectDirectory != null ? argProjectDirectory : getWorkingDirectory();

		final Path siteTargetDirectory;
		final Path siteDescriptionTargetDirectory;
		final Path serverBaseDirectory;
		final int port;
		try {
			final GuiseProject project = GuiseMummy.createProject(projectDirectory.toAbsolutePath(), null, argSiteTargetDirectory, argSiteDescriptionTargetDirectory);
			siteTargetDirectory = project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY);
			siteDescriptionTargetDirectory = project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY);

			//As per the servlet specification, Tomcat requires a base directory, but currently puts nothing inside it except for a `work` directory.
			//This implementation therefore uses a common server directory location relative to the system temporary directory.
			//In the future if Guise takes advantage of more server functionality and the server needs to store things,
			//perhaps by default a subdirectory within the temp folder could be made to parallel that of the project.
			serverBaseDirectory = getTempDirectory().resolve(SERVER_RELATIVE_BASE_DIRECTORY);
			port = argPort != null ? argPort : project.getConfiguration().findInt(CONFIG_KEY_SERVER_PORT).orElse(DEFAULT_SERVER_PORT);

			checkArgument(isDirectory(siteTargetDirectory), "Site target directory %s does not exist.", siteTargetDirectory); //TODO improve error handling; see https://github.com/remkop/picocli/issues/672

			getLogger().info("Serve...");
			getLogger().info("Project directory: {}", projectDirectory);
			getLogger().info("Site target directory: {}", project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY));
			getLogger().info("Site description target directory: {}", project.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY));
			getLogger().info("Server base directory: {}", serverBaseDirectory);
			getLogger().info("Server port: {}", port);
		} catch(final IllegalArgumentException | IOException exception) {
			getLogger().error("{}", exception.getMessage());
			getLogger().debug("{}", exception.getMessage(), exception);
			return; //TODO improve error handling
		}

		final Tomcat tomcat = new Tomcat();
		tomcat.setPort(port);
		//set the base directory to the build directory (possibly needed in the future for e.g. JSP temporary directory; used now as the base for the context temp directory)
		tomcat.setBaseDir(serverBaseDirectory.toAbsolutePath().toString()); //base directory must be set before creating a connector
		tomcat.getConnector(); //create a default connector; required; see https://stackoverflow.com/a/49011424/421049

		final Context context = tomcat.addContext("", siteTargetDirectory.toAbsolutePath().toString());
		context.setResources(new SiteRoot(siteDescriptionTargetDirectory));

		final Wrapper defaultServlet = context.createWrapper(); //TODO use constants below
		defaultServlet.setName("default");
		defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
		defaultServlet.addInitParameter("debug", debug ? "11" : "0"); //TODO use constant; see https://tomcat.apache.org/tomcat-9.0-doc/default-servlet.html
		defaultServlet.addInitParameter("listings", Boolean.FALSE.toString()); //TODO use constant
		defaultServlet.setLoadOnStartup(1);

		context.addChild(defaultServlet);
		context.addServletMappingDecoded(ROOT_PATH, "default"); //TODO use constant
		//TODO later add JSP servlet mappings
		DEFAULT_MIME_TYPES_BY_FILENAME_EXTENSION.forEach((extension, mimeType) -> context.addMimeMapping(extension, mimeType));

		//TODO decide how to determine the welcome file; we can either:
		//* get from configuration, as with `mummify` command (decide whether the configuration is at the project or site level)
		//* write some metadata file to the site-description indicating the welcome file
		context.addWelcomeFile("index.html"); //TODO get from configuration, as with `mummify` command
		context.addWelcomeFile("index"); //TODO get from configuration, as with `mummify` command

		try {
			tomcat.start(); //start the server
		} catch(final LifecycleException lifecycleException) {
			getLogger().error("{}", lifecycleException.getMessage());
			getLogger().debug("{}", lifecycleException.getMessage(), lifecycleException);
			return; //TODO improve error handling
		}

		final URI siteLocalUrl = URIs.createURI(HTTP_URI_SCHEME, null, LOCALHOST_DOMAIN, port, ROOT_PATH, null, null);
		getLogger().info("Serving site at {}.", "<" + siteLocalUrl + ">");

		//launch the browser; see https://stackoverflow.com/a/5226244/421049
		if(browse && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(siteLocalUrl);
			} catch(final IOException exception) {
				getLogger().error("{}", exception.getMessage());
				getLogger().debug("{}", exception.getMessage(), exception);
				return; //TODO improve error handling
			}
		}

		tomcat.getServer().await();
	}

	/** Strategy for providing version and other information from the configuration. */
	static class MetadataProvider extends AbstractMetadataProvider {
		public MetadataProvider() {
			super(GuiseCli.class);
		}
	}

	/**
	 * Default MIME mapping for the server.
	 * @implNote Taken from the {@link Tomcat} source code.
	 */
	private static final Map<String, String> DEFAULT_MIME_TYPES_BY_FILENAME_EXTENSION;

	static { //TODO update; switch to use Tomcat map when it becomes public, or transfer to some common resource file
		final Map<String, String> mimeTypesByFilenameExtension = new HashMap<>();
		final String[] mimeMappings = {"abs", "audio/x-mpeg", "ai", "application/postscript", "aif", "audio/x-aiff", "aifc", "audio/x-aiff", "aiff", "audio/x-aiff",
				"aim", "application/x-aim", "art", "image/x-jg", "asf", "video/x-ms-asf", "asx", "video/x-ms-asf", "au", "audio/basic", "avi", "video/x-msvideo", "avx",
				"video/x-rad-screenplay", "bcpio", "application/x-bcpio", "bin", "application/octet-stream", "bmp", "image/bmp", "body", "text/html", "cdf",
				"application/x-cdf", "cer", "application/pkix-cert", "class", "application/java", "cpio", "application/x-cpio", "csh", "application/x-csh", "css",
				"text/css", "dib", "image/bmp", "doc", "application/msword", "dtd", "application/xml-dtd", "dv", "video/x-dv", "dvi", "application/x-dvi", "eps",
				"application/postscript", "etx", "text/x-setext", "exe", "application/octet-stream", "gif", "image/gif", "gtar", "application/x-gtar", "gz",
				"application/x-gzip", "hdf", "application/x-hdf", "hqx", "application/mac-binhex40", "htc", "text/x-component", "htm", "text/html", "html", "text/html",
				"ief", "image/ief", "jad", "text/vnd.sun.j2me.app-descriptor", "jar", "application/java-archive", "java", "text/x-java-source", "jnlp",
				"application/x-java-jnlp-file", "jpe", "image/jpeg", "jpeg", "image/jpeg", "jpg", "image/jpeg", "js", "application/javascript", "jsf", "text/plain",
				"jspf", "text/plain", "kar", "audio/midi", "latex", "application/x-latex", "m3u", "audio/x-mpegurl", "mac", "image/x-macpaint", "man", "text/troff",
				"mathml", "application/mathml+xml", "me", "text/troff", "mid", "audio/midi", "midi", "audio/midi", "mif", "application/x-mif", "mov", "video/quicktime",
				"movie", "video/x-sgi-movie", "mp1", "audio/mpeg", "mp2", "audio/mpeg", "mp3", "audio/mpeg", "mp4", "video/mp4", "mpa", "audio/mpeg", "mpe",
				"video/mpeg", "mpeg", "video/mpeg", "mpega", "audio/x-mpeg", "mpg", "video/mpeg", "mpv2", "video/mpeg2", "nc", "application/x-netcdf", "oda",
				"application/oda", "odb", "application/vnd.oasis.opendocument.database", "odc", "application/vnd.oasis.opendocument.chart", "odf",
				"application/vnd.oasis.opendocument.formula", "odg", "application/vnd.oasis.opendocument.graphics", "odi", "application/vnd.oasis.opendocument.image",
				"odm", "application/vnd.oasis.opendocument.text-master", "odp", "application/vnd.oasis.opendocument.presentation", "ods",
				"application/vnd.oasis.opendocument.spreadsheet", "odt", "application/vnd.oasis.opendocument.text", "otg",
				"application/vnd.oasis.opendocument.graphics-template", "oth", "application/vnd.oasis.opendocument.text-web", "otp",
				"application/vnd.oasis.opendocument.presentation-template", "ots", "application/vnd.oasis.opendocument.spreadsheet-template ", "ott",
				"application/vnd.oasis.opendocument.text-template", "ogx", "application/ogg", "ogv", "video/ogg", "oga", "audio/ogg", "ogg", "audio/ogg", "spx",
				"audio/ogg", "flac", "audio/flac", "anx", "application/annodex", "axa", "audio/annodex", "axv", "video/annodex", "xspf", "application/xspf+xml", "pbm",
				"image/x-portable-bitmap", "pct", "image/pict", "pdf", "application/pdf", "pgm", "image/x-portable-graymap", "pic", "image/pict", "pict", "image/pict",
				"pls", "audio/x-scpls", "png", "image/png", "pnm", "image/x-portable-anymap", "pnt", "image/x-macpaint", "ppm", "image/x-portable-pixmap", "ppt",
				"application/vnd.ms-powerpoint", "pps", "application/vnd.ms-powerpoint", "ps", "application/postscript", "psd", "image/vnd.adobe.photoshop", "qt",
				"video/quicktime", "qti", "image/x-quicktime", "qtif", "image/x-quicktime", "ras", "image/x-cmu-raster", "rdf", "application/rdf+xml", "rgb",
				"image/x-rgb", "rm", "application/vnd.rn-realmedia", "roff", "text/troff", "rtf", "application/rtf", "rtx", "text/richtext", "sh", "application/x-sh",
				"shar", "application/x-shar",
				/*"shtml", "text/x-server-parsed-html",*/
				"sit", "application/x-stuffit", "snd", "audio/basic", "src", "application/x-wais-source", "sv4cpio", "application/x-sv4cpio", "sv4crc",
				"application/x-sv4crc", "svg", "image/svg+xml", "svgz", "image/svg+xml", "swf", "application/x-shockwave-flash", "t", "text/troff", "tar",
				"application/x-tar", "tcl", "application/x-tcl", "tex", "application/x-tex", "texi", "application/x-texinfo", "texinfo", "application/x-texinfo", "tif",
				"image/tiff", "tiff", "image/tiff", "tr", "text/troff", "tsv", "text/tab-separated-values", "txt", "text/plain", "ulw", "audio/basic", "ustar",
				"application/x-ustar", "vxml", "application/voicexml+xml", "xbm", "image/x-xbitmap", "xht", "application/xhtml+xml", "xhtml", "application/xhtml+xml",
				"xls", "application/vnd.ms-excel", "xml", "application/xml", "xpm", "image/x-xpixmap", "xsl", "application/xml", "xslt", "application/xslt+xml", "xul",
				"application/vnd.mozilla.xul+xml", "xwd", "image/x-xwindowdump", "vsd", "application/vnd.visio", "wav", "audio/x-wav", "wbmp", "image/vnd.wap.wbmp",
				"wml", "text/vnd.wap.wml", "wmlc", "application/vnd.wap.wmlc", "wmls", "text/vnd.wap.wmlsc", "wmlscriptc", "application/vnd.wap.wmlscriptc", "wmv",
				"video/x-ms-wmv", "wrl", "model/vrml", "wspolicy", "application/wspolicy+xml", "Z", "application/x-compress", "z", "application/x-compress", "zip",
				"application/zip"};
		for(int i = 0; i < mimeMappings.length;) {
			mimeTypesByFilenameExtension.put(mimeMappings[i++], mimeMappings[i++]);
		}
		DEFAULT_MIME_TYPES_BY_FILENAME_EXTENSION = unmodifiableMap(mimeTypesByFilenameExtension);
	}

}
