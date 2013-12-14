package net.synapsehaven.spritely;

import java.io.File;
import java.util.Map;

import net.synapsehaven.spritely.Server;
import fi.iki.elonen.ServerRunner;

public class SpritelyServerPlugin extends Server.Plugin
{
	public static void main(String[] args)
	{
		// This needs to have an actual name, apparently.  "127.0.0.1" was causing
		// problems with multipart/form-data requests.
		int port = 10801;
		File wwwRoot = new File(args.length > 0 ? args[0] : "html");
		
		Spritely spritely = new Spritely();
		
		Server gorp = new Server(port);
		gorp.addRoot("localhost", wwwRoot);
		gorp.addRoot("127.0.0.1", wwwRoot);
		gorp.addPlugin(new SpritelyServerPlugin());
		
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
	
	public boolean canHandleRequest()
	{ return false; }
	
	public Map<String,Object> handleRequest()
	{ return null; }
}
