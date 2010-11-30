package ch.bittailor.filetemplates.freemarker;


import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class BooleanHashModel extends CacheModel<Boolean> implements TemplateHashModel {

   public TemplateModel get(String key) throws TemplateModelException {
      return new SimpleBoolean(getCached(key));
   }

   @Override
   protected Boolean ask(String key) {
      return ServiceProvider.instance().getAskSerice().askForABoolean(key);
   }

}

class SimpleBoolean implements TemplateBooleanModel
{
   private Boolean fValue;

   public SimpleBoolean(Boolean value) {
      fValue = value;
   }

   public boolean getAsBoolean() throws TemplateModelException {
      return fValue.booleanValue();
   }

}

