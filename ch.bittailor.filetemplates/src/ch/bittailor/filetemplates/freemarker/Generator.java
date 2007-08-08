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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ch.bittailor.filetemplates.Activator;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;

public class Generator {
	
	private Configuration fConfiguration;
  private String fTemplateDirectory;
  private SimpleHash fRoot;
  private List<String[]> fOutputs;
  private SimpleHash fGlobal;
  
	public Generator(String templateDirectory){
	  fTemplateDirectory = templateDirectory;
	  fRoot = new SimpleHash();
	  createGlobals();
	  fRoot.put("env", System.getenv());
    fRoot.put("string", new StringHashModel());
    fRoot.put("boolean", new BooleanHashModel());
    fOutputs = new LinkedList<String[]>();
	}

  private void createGlobals() {
    fGlobal = new SimpleHash();
    fRoot.put("global", fGlobal);
    fGlobal.put("date", new SimpleDate(new Date(),TemplateDateModel.DATETIME));
    fGlobal.put("user", new SimpleScalar(System.getProperty("user.name", "?")));
  }
	
	public List<IFile> generate(IContainer container, IProgressMonitor monitor, Element element) throws CoreException {
    generateAllFiles(element);
    List<IFile> files = new LinkedList<IFile>();
    for (String[] output : fOutputs) {
      IFile file = container.getFile(new Path(output[0]));
      InputStream stream =  new ByteArrayInputStream(output[1].getBytes());
      if (file.exists()) {
        file.setContents(stream, true, true, monitor);
      } else {
        file.create(stream, true, monitor);
      }
      files.add(file);
    }
	  return files;
	}
	
	public void generateAllFiles(Element element) throws CoreException{
	  NodeList files = element.getElementsByTagName("file");
	  for (int i = 0; i < files.getLength() ; i++) {
	    generateOneFile((Element)files.item(i));
    }
  }
	
	public void generateOneFile(Element element) throws CoreException{
	  String name = element.getElementsByTagName("name").item(0).getTextContent();
	  String template = element.getElementsByTagName("template").item(0).getTextContent();
    String output;
    try {
      String filename = generateExpresion(name);
      fGlobal.put("filename", new SimpleScalar(filename));
      output = generateTemplate(template);
      fOutputs.add(new String[]{filename,output});
    } catch (IOException e) {
      throw new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,"io problem while processing "+template,e));
    } catch (TemplateException e) {
      throw new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,"template problem while processing "+template,e));
    } catch (Throwable throwable){
      throwable.printStackTrace();
      throw new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,"unknown problem while processing "+template,throwable));
    }
	}
	
	public String generateTemplate(String templateName) throws IOException, TemplateException{	
		Template template = getConfiguration().getTemplate(templateName);  
		return process(template);
	}
	
	public String generateExpresion(String expresion) throws IOException, TemplateException{
    StringReader reader = new StringReader(expresion);
    Template template = new Template("name expresion", reader, fConfiguration);
    return process(template);
	}
	
	public String process(Template template) throws TemplateException, IOException {
    StringWriter writer = new StringWriter();
    template.process(fRoot, writer);
    return writer.toString();
  }
	
	
	
  private Configuration getConfiguration() throws IOException {
    if (fConfiguration==null){
      fConfiguration = new Configuration();
      fConfiguration.setDirectoryForTemplateLoading(new File(fTemplateDirectory));
      fConfiguration.setObjectWrapper(new DefaultObjectWrapper());
    }
    return fConfiguration;
  }
	
}
