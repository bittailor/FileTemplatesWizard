package ch.bittailor.filetemplates.freemarker;

import java.io.IOException;

import ch.bittailor.filetemplates.Activator;

import junit.framework.TestCase;
import freemarker.template.TemplateException;

public class GeneratorTest extends TestCase {

  public void testGenerate() throws IOException, TemplateException {
    ServiceProvider.instance().setAskSerice(new AskStub());
    Generator generator = new Generator(Activator.class);
    //generator.generate("test.ftl",new OutputStreamWriter(System.out));
    System.out.println("<<>>  "+generator.generateExpresion("${string.name} _ ${string.extendsion}"));  
    
  }

}

class AskStub implements IAsk {

  public Boolean askForABoolean(String key) {
    return true;
  }

  public String askForAString(String key) {
    return key;
  }
  
}
