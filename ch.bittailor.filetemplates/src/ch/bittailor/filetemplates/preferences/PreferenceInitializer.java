package ch.bittailor.filetemplates.preferences;

import java.io.*;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ch.bittailor.filetemplates.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

   @Override
   public void initializeDefaultPreferences() {
      IPreferenceStore store = Activator.getDefault().getPreferenceStore();
      store.setDefault(PreferenceConstants.P_TEMPLATES_LOCATION, System.getProperty("user.home")+File.separator+".bittailor"+File.separator+"filetemplates");
   }

}
