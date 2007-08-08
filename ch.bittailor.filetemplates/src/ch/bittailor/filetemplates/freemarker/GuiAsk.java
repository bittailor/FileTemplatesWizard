package ch.bittailor.filetemplates.freemarker;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

public class GuiAsk implements IAsk {

  public Boolean askForABoolean(String key) {       
    Display dialog = Display.getDefault();
    AskForABoolean asker = new AskForABoolean(key);
    dialog.syncExec(asker);    
    return asker.getAnswer();
  }

  public String askForAString(String key) {
    Display dialog = Display.getDefault();
    AskForAString asker = new AskForAString(key);
    dialog.syncExec(asker);    
    return asker.getAnswer();
  }

}

class AskForAString implements Runnable{
  private String fKey;
  private String fAnswer;
  
  public AskForAString(String key) {
    fKey = key;
    fAnswer = key;
  }

  public void run() {
    InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),
        "File Template Generator", "String value for "+fKey, fKey, null);
    if (dlg.open() == Window.OK) {
      fAnswer = dlg.getValue();
    }
  }

  public String getAnswer() {
    return fAnswer;
  }

}

class AskForABoolean implements Runnable{
  private String fKey;
  private Boolean fAnswer;
  
  public AskForABoolean(String key) {
    fKey = key;
    fAnswer = true;
  }

  public void run() {
    MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(),
        "File Template Generator", null, "Boolean value for "+fKey, 
        MessageDialog.QUESTION, new String[] { "true","false" },0); 
    fAnswer = dialog.open() == 0;
  }

  public Boolean getAnswer() {
    return fAnswer;
  }

}
