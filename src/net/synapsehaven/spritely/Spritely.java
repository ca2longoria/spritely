package net.synapsehaven.spritely;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class Spritely //implements Plugins.LocalWebServerPlugin
{
	protected final static int maxImageSize = 4000000;
	
	protected Map<String,Session> sessions = new HashMap<String,Session>();
	
	public Map<String,Object> setImageFile(String id, File f)
	{
		Map<String,Object> ret = new HashMap<String,Object>();
		Session session = null;
		
		if (f == null)
		{
			System.err.println("ERROR: File is null");
			ret.put(Standard.StatusKey, Status.FAILURE);
			ret.put(Standard.DescriptionKey, "File parameter is null");
			return ret;
		}
		
		// If a session matching the id already exists, remove it
		// TODO: A session may constitute more than an image edit; this may
		//       have to change, later.
		if (sessions.containsKey(id))
		{
			if (sessions.get(id).cleanup())
				sessions.remove(id);
			else
			{
				// TODO: A standard error log system would be nice...
				System.err.println(String.format(
					"ERROR: Session Cleanup of file %s failed",
					sessions.get(id).getWorkingImageFile().getName()));
				ret.put(Standard.StatusKey, Status.FAILURE);
				return ret;
			}
		}
		
		session = new Session(f);
		sessions.put(id, session);
		
		// TODO: Some kind of proper value...
		ret.put(Standard.StatusKey, Status.SUCCESS);
		ret.put(Standard.RelativeFilePathKey, session.getWorkingImageFileRelativePath());
		return ret;
	}
	
	public Map<String,Object> getImageFile(String id)
	{
		Map<String,Object> ret = new HashMap<String,Object>();
		
		if (!sessions.containsKey(id))
		{
			// Must translate to some failure response status...
			ret.put(Standard.StatusKey, Status.FAILURE);
			return ret;
		}
		
		ret.put(Standard.FileKey, sessions.get(id).getWorkingImageFile());
		ret.put(Standard.StatusKey, Status.SUCCESS);
		
		return ret;
	}
	
	public class Session
	{
		private static final String localImageFileExtension = "png";
		
		public Session(File imageFile)
		{
			logActivity("session construction for image file: "+imageFile.getName());
			
			this.setWorkingImageFile(imageFile);
		}
		
		private BufferedImage workingImage = null;
		private File workingImageFile = null;
		private long startTime = System.currentTimeMillis();
		private long lastActive = startTime;
		
		public BufferedImage getWorkingImage()
		{ return workingImage; }
		public File getWorkingImageFile()
		{ return workingImageFile; }
		// TODO: Where should the relative file be stored and how should it be handled?
		public String getWorkingImageFileRelativePath()
		{ return "tmp/"+this.getWorkingImageFile().getName(); }
		public String getWorkingImageFileExtension()
		{ return "png"; }
		
		public boolean cleanup()
		{
			logActivity("cleaning up");
			
			if (workingImageFile.delete())
				return  true;
			else
				return false;
		}
		
		@SuppressWarnings("deprecation")
		private void logActivity(String activity)
		{
			lastActive = System.currentTimeMillis();
			Date date = new Date(lastActive);
			
			String logString = String.format("%04d/%02d/%02d_%02d:%02d:%02d> %s",
				date.getYear()+1, date.getMonth()+1, date.getDay()+1,
				date.getHours(),date.getMinutes(), date.getSeconds(),
				activity);
			
			// TODO: Something that ties in to Spritely's logfile setup.
			System.out.println(logString);
		}
		
		private boolean setWorkingImageFile(File file)
		{
			if (file.length() > maxImageSize)
			{
				logActivity(String.format(
					"image file size (%d bytes) greater than maximum (%d)",
					file.length(),
					maxImageSize));
				return false;
			}
			
			File f = null;
			BufferedImage im = null;
			try {
				im = ImageIO.read(file);
				f = new File("tmp/"+file.getName());
				ImageIO.write(im, localImageFileExtension, f);			
			}
			catch (IOException e) { e.printStackTrace(); }
			
			workingImage = im;
			workingImageFile = f;
			
			return true;
		}
	}
	
	
	// TODO: This could work just as easily (or more) as an enum...
	public static class Standard
	{
		public static final String FileKey = "file";
		public static final String RelativeFilePathKey = "relativepath";
		public static final String AbsoluteFilePathKey = "absolutepath";
		public static final String StatusKey = "status";
		public static final String DescriptionKey = "description";
	}
	
	public static enum Status
	{
		SUCCESS,
		FAILURE,
		INVALID_ID
	}
	
}
