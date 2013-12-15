package net.synapsehaven.spritely;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.ServerRunner;

public class SpritelyServerPlugin implements Server.Plugin
{
	public static void main(String[] args)
	{
		// This needs to have an actual name, apparently.  "127.0.0.1" was causing
		// problems with multipart/form-data requests.
		int port = 10801;
		File wwwRoot = new File(args.length > 0 ? args[0] : "html");
		
		Spritely spritely = new Spritely();
		
		// Create the default server.
		Server gorp = new Server(port);
		gorp.addRoot("localhost", wwwRoot);
		gorp.addRoot("127.0.0.1", wwwRoot);
		
		// Add the SpritelyServerPlugin.
		gorp.addPlugin(new SpritelyServerPlugin(spritely));
		
		// Testing out a Thread, here...
		class ServerThread extends Thread
		{ public Server serv; }
		ServerThread t = new ServerThread()
		{
			public void run()
			{ ServerRunner.executeInstance(serv); }
		};
		t.serv = gorp;
		
		t.run();
	}
	
	private Spritely spritely;
	
	public SpritelyServerPlugin(Spritely applicationObject)
	{
		this.spritely = applicationObject;
	}
	
	public boolean canHandleRequest(String uri, Method method, Map<String,
		String> headers, Map<String, String> parms, Map<String, String> files)
	{
		// A simple URI syntax check.
		return (uri.indexOf("/:") == 0);
	}
	
	public Response handleRequest(String uri, Method method, Map<String,
		String> headers, Map<String, String> parms, Map<String, String> files)
	{
		System.out.println("SpritelyServerPlugin.handleRequest\n" +
			"  uri: " + uri + "\n" +
			"  method: " + method + "\n" +
			"  headers: " + headers + "\n" +
			"  parms: " + parms + "\n" +
			"  files: " + files);
		
		if (uri.equalsIgnoreCase("/:setimage"))
		{
			// TODO: This assumes the file is valid.  Where should the check for this go?
			File imageFile = new File(files.get("file-0"));
			Map<String,Object> res = this.spritely.setImageFile("default", imageFile);
			
			if (res.get(Spritely.Standard.StatusKey) == Spritely.Status.FAILURE)
			{
				String description = (res.containsKey(Spritely.Standard.DescriptionKey)
					? "ERROR: " + res.get(Spritely.Standard.DescriptionKey) : "");
				return new Response(Response.Status.INTERNAL_ERROR, "text/plain", description);
			}
			
			String relativePath = "";
			
			if (res.containsKey(Spritely.Standard.RelativeFilePathKey))
				relativePath = (String)res.get(Spritely.Standard.RelativeFilePathKey);
			
			return new Response(Response.Status.OK, "text/plain", relativePath);
		}
		else if (uri.equalsIgnoreCase("/:getimage"))
		{
			Map<String,Object> res = this.spritely.getImageFile("default");
			
			// Acquire file, and store extension for mimeType.
			File f = (File)res.get(Spritely.Standard.FileKey);
			String extension = f.getName().replace("^.*\\.", "");
			
			// Create FileInputStream for passing the image file into the Response.
			FileInputStream fin = null;
			try { fin = new FileInputStream(f); }
			catch (FileNotFoundException e) { e.printStackTrace(); }
			
			return new Response(Response.Status.OK, "image/"+extension, fin);
		}
		return null;
	}
}
