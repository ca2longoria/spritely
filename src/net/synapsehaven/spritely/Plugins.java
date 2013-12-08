package net.synapsehaven.spritely;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.SimpleWebServer;
import fi.iki.elonen.WebServerPlugin;
import fi.iki.elonen.NanoHTTPD.Response;

public class Plugins
{
	public static interface LocalWebServerPlugin extends WebServerPlugin
	{
		public void registerWithSimpleWebServer();
	}
	
	public static class ImageSvgXmlPlugin implements LocalWebServerPlugin
	{
		@Override public void registerWithSimpleWebServer()
		{
			SimpleWebServer.registerPluginForMimeType(
				new String[]{"null.svg"}, // these are necessary to add extensiosn to MIME_TYPES
				"image/svg+xml", // mime-type
				this, // WebServerPlugin
				null // command line options
			);
		}
		
		@Override public boolean canServeUri(String uri, File rootDir)
		{ return new File(rootDir,uri).exists(); }

		@Override public void initialize(Map<String, String> commandLineOptions) {}
		
		@Override public Response serveFile(String uri, Map<String, String> headers,
			File file, String mimeType)
		{
			FileInputStream fin = null;
			try { fin = new FileInputStream(file); }
			catch (FileNotFoundException e)
			{ e.printStackTrace(); }
			
			return new NanoHTTPD.Response(
				Response.Status.OK, "image/svg+xml", fin);
		}	
	}
}
