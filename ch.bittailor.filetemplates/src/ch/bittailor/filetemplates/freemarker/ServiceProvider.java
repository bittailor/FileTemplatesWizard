package ch.bittailor.filetemplates.freemarker;

public class ServiceProvider {

   static private ServiceProvider sInstance;

   private IAsk fAskSerice;


   // TODO ? synchronize
   static public ServiceProvider instance(){
      if (sInstance==null){
         sInstance = new ServiceProvider();
      }
      return sInstance;
   }

   private ServiceProvider() {
      fAskSerice = new GuiAsk();
   }

   public IAsk getAskSerice() {
      return fAskSerice;
   }

   public void setAskSerice(IAsk askSerice) {
      fAskSerice = askSerice;
   }


}
