package net.synapsehaven.spritely;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fi.iki.elonen.NanoHTTPD;


public class Server extends NanoHTTPD
{
	public Server(int port)
	{
		super(port);
		
		// NanoHTTPD Text
		this.addFileExtensionMimeTypeAssociation("txt", MIME_PLAINTEXT);
		this.addFileExtensionMimeTypeAssociation("htm", MIME_HTML);
		this.addFileExtensionMimeTypeAssociation("html", MIME_HTML);
		
		// Other Text
		this.addFileExtensionMimeTypeAssociation("js", "text/javascript");
		this.addFileExtensionMimeTypeAssociation("js", "text/css");
		this.addFileExtensionMimeTypeAssociation("js", "text/php"); // ?
		
		// Images
		this.addFileExtensionMimeTypeAssociation("bmp", "image/bmp");
		this.addFileExtensionMimeTypeAssociation("gif", "image/gif");
		this.addFileExtensionMimeTypeAssociation("jpeg", "image/jpeg");
		this.addFileExtensionMimeTypeAssociation("jpg", "image/jpeg");
		this.addFileExtensionMimeTypeAssociation("png", "image/png");
		this.addFileExtensionMimeTypeAssociation("svg", "image/svg+xml");
	}
	
	// Variable declaration
	protected Map<String,File> rootMap = new HashMap<String,File>();
	protected Map<String,String> extensionMimeTypes = new HashMap<String,String>(); 
	protected List<Plugin> plugins = new ArrayList<Plugin>();
	
	// Hostname-Root Directory Associations
	public File addRoot(String hostname, File rootDir)
	{
		rootMap.put(hostname, rootDir);
		return rootDir;
	}
	public File addRoot(String hostname, String path)
	{ return this.addRoot(hostname, new File(path)); }
	
	
	// File Extensions and Mime Types
	public void addFileExtensionMimeTypeAssociation(String ext, String mimeType)
	{
		extensionMimeTypes.put(ext, mimeType);
	}
	public List<String> getMimeTypes()
	{ return new ArrayList<String>(extensionMimeTypes.values()); }
	public Map<String,String> getFileExtensionMimeTypeAssociations()
	{ return Collections.unmodifiableMap(extensionMimeTypes); }
	
	// Plugin-including methods
	public void addPlugin(Plugin plugin)
	{ if (!plugins.contains(plugin)) this.plugins.add(plugin); }
	
	// NanoHTTPD-extending methods
	@Override
	public Response serve(String uri, Method method, Map<String,
		String> headers, Map<String, String> parms, Map<String, String> files)
	{
		// A little output
		System.out.println("headers: "+headers);
		System.out.println("files: "+files);
		
		// 
		String host = headers.get("host");
		File rootDir = null;
		for (Entry<String,File> a : rootMap.entrySet())
		{
			System.out.println(a+" "+host);
			if (a.getKey().equalsIgnoreCase(host.replaceFirst(":[0-9]{1,5}$", "")))
			{
				rootDir = a.getValue();
				break;
			}
		}
		
		// If committing some nefarious deed...
		if (method == Method.POST)
		{
			// NOTE: Let us organize by behavior or action...
			System.out.println("POST: okay, uri: "+uri);
			
			
		}
		
		// If directly serving a file...
		else if (method == Method.GET)
		{
			String path = rootDir.getPath() + uri;
			System.out.println("path: "+path);
			File f = new File(rootDir.getPath() + uri);
			
			if (f.exists() && f.isFile())
			{
				FileInputStream fin = null;
				try { fin = new FileInputStream(f); }
				catch (FileNotFoundException e) { e.printStackTrace(); }
				
				String ext = "txt";
				if (f.getName().contains("."))
				{
					ext = f.getName().substring(f.getName().lastIndexOf('.')+1);
					System.out.println("file extension: "+ext);
				}
				
				if (extensionMimeTypes.containsKey(ext.toLowerCase()))
				{
					String mimeType = extensionMimeTypes.get(ext.toLowerCase());
					return new Response(Response.Status.OK, mimeType, fin);
				}
			}
		}
		
		return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT,
			"No can the do, dearest.");
	}
	
	public static abstract class Plugin
	{
		public abstract boolean canHandleRequest();
		public abstract Map<String,Object> handleRequest();
	}
}
