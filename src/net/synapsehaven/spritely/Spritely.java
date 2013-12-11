package net.synapsehaven.spritely;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.ServerRunner;

public class Spritely //implements Plugins.LocalWebServerPlugin
{
	public static void main(String[] args)
	{
		// This needs to have an actual name, apparently.  "127.0.0.1" was causing
		// problems with multipart/form-data requests.
		int port = 10801;
		File wwwRoot = new File(args.length > 0 ? args[0] : "html");
		
		Spritely spritely = new Spritely();
		
		Server gorp = spritely.new Server(port);
		gorp.addRoot("localhost", wwwRoot);
		gorp.addRoot("127.0.0.1", wwwRoot);
		
		ServerRunner.executeInstance(gorp);
	}
	
	protected final static int maxImageSize = 4000000;
	
	protected Spritely.Session currentSession = null;
	
	//protected BufferedImage workingImage = null;
	//protected File workingImageFile = null;
	
	protected void initNewSession()
	{
		currentSession = new Session();
	}
	
	public void acquireImageFile(File file)
	{
		if (currentSession == null)
			return;
		
		currentSession.acquireImageFile(file);
		
		/*
		if (file.length() > maxImageSize)
			return;
		
		File f = null;
		BufferedImage im = null;
		try {
			im = ImageIO.read(file);
			f = new File("tmp/"+file.getName());
			ImageIO.write(im, "png", f);			
		}
		catch (IOException e) { e.printStackTrace(); }
		
		workingImage = im;
		workingImageFile = f;
		//*/
	}
	
	public File getImageFile()
	{
		return currentSession.imageSet.getWorkingImageFile();
	}
	
	public class Session
	{
		// Why am I using non-static classes for this?
		protected ImageSet imageSet = this.new ImageSet();
		
		public void acquireImageFile(File file)
		{
			imageSet.setWorkingImageFile(file);
		}
		
		public class ImageSet
		{
			protected BufferedImage workingImage = null;
			protected File workingImageFile = null;
			
			private BufferedImage lineDivisions;
			private BufferedImage characterOutlines;
			
			public BufferedImage getWorkingImage() { return workingImage; }
			public BufferedImage getLineDivisionImage() { return lineDivisions; }
			public BufferedImage getCharacterOutlines() { return characterOutlines; }
			
			public File getWorkingImageFile()
			{
				return workingImageFile;
			}
			
			public void setWorkingImageFile(File file)
			{
				if (file.length() > maxImageSize)
					return;
				
				File f = null;
				BufferedImage im = null;
				try {
					im = ImageIO.read(file);
					f = new File("tmp/"+file.getName());
					ImageIO.write(im, "png", f);			
				}
				catch (IOException e) { e.printStackTrace(); }
				
				workingImage = im;
				workingImageFile = f;
			}
		}
	}
	
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
		
		
		// NanoHTTPD-extending methods
		@Override
		public Response serve(String uri, Method method, Map<String,
			String> headers, Map<String, String> parms, Map<String, String> files)
		{
			// A little output
			System.out.println("Gorp serve!");
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
				
				// SPRITELY: Special Methods
				if (uri.startsWith("/:heretakethis"))
				{
					System.out.println(headers.get("content-type").contains("multipart/form-data"));
					
					if (headers.containsKey("content-type") &&
						headers.get("content-type").contains("multipart/form-data"))
					{
						System.out.println("Multipart... Multipart... Muuultiiipaaart...");
						
						Spritely.this.initNewSession();
						
						for (Entry<String,String> e : files.entrySet())
						{
							System.out.println("- file "+e);
							Spritely.this.acquireImageFile(new File(e.getValue()));
						}
					}
					
					return new Response(
						Response.Status.OK,
						"text/plain",
						"tmp/"+Spritely.this.getImageFile().getName());
				}
			}
			
			// If directly serving a file...
			else if (method == Method.GET)
			{
				// SPRITELY: Special GET
				if (uri.startsWith("/:getimage"))
				{
					InputStream in = null;
					try { in = new FileInputStream(Spritely.this.getImageFile()); }
					catch (IOException e) { e.printStackTrace(); }
					return new Response(Response.Status.OK, "image/png", in);
				}
				
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
	}
	
}
