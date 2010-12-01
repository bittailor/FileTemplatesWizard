package ch.bittailor.filetemplates.freemarker;

import java.util.*;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.Display;

public class GuiAsk implements IAsk {
   
   Map<String, String> fStringHistory = new HashMap<String, String>();
  
   public Boolean askForABoolean(String key) {       
      Display dialog = Display.getDefault();
      AskForABoolean asker = new AskForABoolean(key);
      synchExecute(dialog, asker);
      return asker.getAnswer();
   }
   
   public String askForAString(String key) {
      Display dialog = Display.getDefault();
      AskForAString asker = new AskForAString(key,fStringHistory.get(key));
      synchExecute(dialog, asker);  
      fStringHistory.put(key,asker.getAnswer());
      return asker.getAnswer();
   }

   private void synchExecute(Display dialog, Runnable asker) {
      try{
         dialog.syncExec(asker);         
      } catch (SWTException exception) {
         if (exception.getCause() instanceof RuntimeException) {
            RuntimeException runtimeException = (RuntimeException) exception.getCause();
            throw runtimeException;
         }
         throw exception;
      }
   }

   public static String toReadableKey(String key){
      if (key.length()==0){
         return key;
      }
      StringBuilder readableKey = new StringBuilder();
      readableKey.append(Character.toUpperCase(key.charAt(0)));
      for (int i = 1; i < key.length() ; i++) {
         if(Character.isUpperCase(key.charAt(i))){
            readableKey.append(" ");
         }
         readableKey.append(key.charAt(i));
      }
      return readableKey.toString();
   }
}

class AskForAString implements Runnable{
   
   private String fKey;
   private String fAnswer;
   private String fHistory;

   public AskForAString(String key, String history) {
      fKey = key;
      fAnswer = key;
      fHistory = history;
   }

   public void run() {
      String initialValue;
      if(fHistory != null)  {
         initialValue = fHistory; 
      } else {
         initialValue = fKey;
      }
      
      InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),
               "File Template Generator", "String Value For: "+GuiAsk.toReadableKey(fKey), initialValue, null);
      
      int returnCode = dlg.open();
      if(returnCode == Window.CANCEL){
         throw new AbortException();
      }
      if (returnCode == Window.OK) {
         fAnswer = dlg.getValue();
      }
   }

   public String getAnswer() {
      return fAnswer;
   }

}

class AskForABoolean implements Runnable{
   
   private static final int DIALOG_ESC = -1;
   
   private String fKey;
   private Boolean fAnswer;

   public AskForABoolean(String key) {
      fKey = key;
      fAnswer = true;
   }

   public void run() {
      MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(),
               "File Template Generator", null, "Choose: "+GuiAsk.toReadableKey(fKey), 
               MessageDialog.QUESTION, new String[] { "true","false" },0); 
      int returnCode = dialog.open();
      if(returnCode == DIALOG_ESC){
         throw new AbortException();
      }
      fAnswer = returnCode == 0;
   }

   public Boolean getAnswer() {
      return fAnswer;
   }

}
