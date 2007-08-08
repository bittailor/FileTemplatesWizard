package ch.bittailor.filetemplates.freemarker;

import java.util.HashMap;
import java.util.Map;

import freemarker.template.TemplateModelException;

public abstract class CacheModel<T> {

  private Map<String, T> fCache;

  public CacheModel() {
    fCache = new HashMap<String, T>();
  }

  public T getCached(String key){
    if (!fCache.containsKey(key)){
      fCache.put(key, (ask(key)));
    }
    return fCache.get(key);
  }
  
  public boolean isEmpty() throws TemplateModelException {
    return false;
  }
  
  protected abstract T ask(String key);

}