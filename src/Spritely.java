
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.ServerRunner;
import fi.iki.elonen.SimpleWebServer;
import fi.iki.elonen.WebServerPlugin;
public class Spritely
{
	public static void main(String[] args)
	{
		String hostname = "127.0.0.1";
		int port = 10801;
		File wwwRoot = new File(args.length > 0 ? args[0] : "html");
		
		SimpleWebServer serv = new SimpleWebServer(hostname, port, wwwRoot, false);
		
		LocalWebServerPlugin svgPlugin = new ImageSvgXmlPlugin();
		svgPlugin.registerWithSimpleWebServer();
		
		ServerRunner.executeInstance(serv);
	}
	
	
	private static interface LocalWebServerPlugin extends WebServerPlugin
	{
		public void registerWithSimpleWebServer();
	}
	
	private static class ImageSvgXmlPlugin implements LocalWebServerPlugin
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
