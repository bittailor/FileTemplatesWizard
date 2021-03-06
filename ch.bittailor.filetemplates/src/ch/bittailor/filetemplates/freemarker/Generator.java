package ch.bittailor.filetemplates.freemarker;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import freemarker.template.*;

public class Generator {

   private Configuration fConfiguration;
   private SimpleHash fDataModel;
   private SimpleHash fGlobal;
   private List<String[]> fOutputs;

   public Generator(String templateLoaction) throws IOException{

      fConfiguration = new Configuration();
      fConfiguration.setDirectoryForTemplateLoading(new File(templateLoaction));
      fConfiguration.setObjectWrapper(new DefaultObjectWrapper());

      fDataModel = new SimpleHash();
      fGlobal = new SimpleHash();
      fDataModel.put("global", fGlobal);
      fDataModel.put("env", System.getenv());
      fDataModel.put("string", new StringHashModel());
      fDataModel.put("boolean", new BooleanHashModel());

      fillGlobals();

      fOutputs = new LinkedList<String[]>();
   }

   private void fillGlobals() {
      fGlobal.put("date", new SimpleDate(new Date(),TemplateDateModel.DATETIME));
      fGlobal.put("user", new SimpleScalar(System.getProperty("user.name", "?")));
   }

   public List<IFile> generate(IContainer container, IProgressMonitor monitor, Element element) throws IOException, TemplateException, CoreException  {

      IProject project = container.getProject();
      if(project==null){
         fGlobal.put("project", new SimpleScalar("?"));
      } else {
         fGlobal.put("project", project.getName());
      }

      try{
         generateAllFiles(element);
      } catch (AbortException abortException) {
         return new LinkedList<IFile>();
      }
      
      
      List<IFile> files = new LinkedList<IFile>();
      for (String[] output : fOutputs) {
         IFile file = container.getFile(new Path(output[0]));
         InputStream stream =  new ByteArrayInputStream(output[1].getBytes());
         if (file.exists()) {
            file.setContents(stream, true, true, monitor);
         } else {
            ensureFolders(file.getParent(), monitor);
            file.create(stream, true, monitor);
         }
         files.add(file);
      }
      return files;
   }

   private void ensureFolders(IContainer container, IProgressMonitor monitor) throws CoreException {
      if (container == null) {
         return;
      }     
      if (container.exists()) {
         return;
      }      
      if (container instanceof IFolder) {
         IFolder folder = (IFolder) container;
         ensureFolders(folder.getParent(), monitor);
         folder.create(true, true, monitor);
      }      
   }

   public void generateAllFiles(Element element) throws IOException, TemplateException{
      NodeList files = element.getElementsByTagName("file");
      for (int i = 0; i < files.getLength() ; i++) {
         generateOneFile((Element)files.item(i));
      }
   }

   public void generateOneFile(Element element) throws IOException, TemplateException{
      String name = element.getElementsByTagName("name").item(0).getTextContent();
      String template = element.getElementsByTagName("template").item(0).getTextContent();
      String output;
      //try {
      String filename = generateExpresion(name);
      fGlobal.put("filename", new SimpleScalar(filename));
      String basename =  filename.substring( 0, filename.lastIndexOf("."));
      fGlobal.put("basename", new SimpleScalar(basename));


      output = generateTemplate(template);
      fOutputs.add(new String[]{filename,output});
      /*
	  } catch (IOException e) {
      e.printStackTrace();
      throw new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,IStatus.OK,"io problem while processing "+template +
          ":\n"+e.getMessage(),e));
    } catch (TemplateException e) {
      e.printStackTrace();
      throw new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,IStatus.OK,"template problem while processing "+template+
          ":\n"+e.getMessage(),e));
    } 
       */
   }

   public String generateTemplate(String templateName) throws IOException, TemplateException{	
      Template template = fConfiguration.getTemplate(templateName);  
      return process(template);
   }

   public String generateExpresion(String expresion) throws IOException, TemplateException{
      StringReader reader = new StringReader(expresion);
      Template template = new Template("name expresion", reader, fConfiguration);
      return process(template);
   }

   public String process(Template template) throws TemplateException, IOException {
      StringWriter writer = new StringWriter();
      template.process(fDataModel, writer);
      return writer.toString();
   }



}
