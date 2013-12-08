package net.synapsehaven.spritely;

import java.io.File;

import fi.iki.elonen.ServerRunner;
import fi.iki.elonen.SimpleWebServer;

public class Spritely
{
	public static void main(String[] args)
	{
		String hostname = "127.0.0.1";
		int port = 10801;
		File wwwRoot = new File(args.length > 0 ? args[0] : "html");
		
		SimpleWebServer serv = new SimpleWebServer(hostname, port, wwwRoot, false);
		
		Plugins.LocalWebServerPlugin svgPlugin = new Plugins.ImageSvgXmlPlugin();
		svgPlugin.registerWithSimpleWebServer();
		
		ServerRunner.executeInstance(serv);
	}
	
}
