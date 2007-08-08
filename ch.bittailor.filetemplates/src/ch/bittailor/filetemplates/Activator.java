package ch.bittailor.filetemplates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ch.bittailor.filetemplates";

	// The shared instance
	private static Activator fPlugin;

  private BundleContext fContext;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		fPlugin = this;
	}

	public String getTemplateStorage(){
	  BufferedReader templatesXml = new BufferedReader( new InputStreamReader(this.getClass().getResourceAsStream("filetemplates.xml")));
	  try {
	    String line = null;
      while((line=templatesXml.readLine())!=null){
        System.out.println(line);
      }
      templatesXml.close();
    } catch (IOException e) {
      e.printStackTrace();
    } finally{
      
    }
	  
	  
	  
	  
	  return "/Users/gekko/Development/workspace/ch.bittailor.filetemplates/test/templates";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		fPlugin = null;
		fContext = context;
    super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return fPlugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
