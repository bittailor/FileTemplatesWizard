package ch.bittailor.filetemplates.freemarker;


import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;


public class StringHashModel extends CacheModel<String> implements TemplateHashModel {

  public TemplateModel get(String key) throws TemplateModelException {
    return new SimpleScalar(getCached(key)) ;
  }

  @Override
  protected String ask(String key) {
    return ServiceProvider.instance().getAskSerice().askForAString(key);
  }

}
