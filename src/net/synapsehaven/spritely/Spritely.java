package net.synapsehaven.spritely;

import java.io.File;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.ServerRunner;
import fi.iki.elonen.SimpleWebServer;
import fi.iki.elonen.NanoHTTPD.Response;

public class Spritely implements Plugins.LocalWebServerPlugin
{
	public static void main(String[] args)
	{
		// This needs to have an actual name, apparently.  "127.0.0.1" was causing
		// problems with multipart/form-data requests.
		String hostname = "localhost";
		int port = 10801;
		File wwwRoot = new File(args.length > 0 ? args[0] : "html");
		
		//SimpleWebServer serv = new SimpleWebServer(hostname, port, wwwRoot, false);
		
		//Plugins.LocalWebServerPlugin svgPlugin = new Plugins.ImageSvgXmlPlugin();
		//svgPlugin.registerWithSimpleWebServer();
		
		Spritely spritely = new Spritely();
		//spritely.registerWithSimpleWebServer();
		
		//ServerRunner.executeInstance(serv);
		
		Gorp gorp = new Gorp(port);
		ServerRunner.executeInstance(gorp);
	}
	
	
	public static class Gorp extends NanoHTTPD
	{
		public Gorp(int port)
		{
			super(port);
		}
		
		@Override
		public Response serve()
		{
			
		}
	}
	
	
	@Override public void registerWithSimpleWebServer()
	{
		SimpleWebServer.registerPluginForMimeType(
			new String[]{"null.spritely"}, // these are necessary to add extensiosn to MIME_TYPES
			"text/plain", // mime-type
			this, // WebServerPlugin
			null // command line options
		);
	}
	
	@Override public boolean canServeUri(String uri, File rootDir)
	{
		return true;
	}
	
	@Override public void initialize(Map<String, String> commandLineOptions) {}
	
	@Override public Response serveFile(String uri, Map<String, String> headers, File file, String mimeType)
	{
		System.out.println(String.format("%s, %s, %s, %s", uri,headers,file,mimeType));
		System.out.println("Superimposed GOODNESS!?!?!");
		return new NanoHTTPD.Response(Response.Status.OK, "text/plain", "SpritelyAction! "+uri);
	}
	
	
}
