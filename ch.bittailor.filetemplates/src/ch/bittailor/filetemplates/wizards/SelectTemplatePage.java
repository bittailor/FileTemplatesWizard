package ch.bittailor.filetemplates.wizards;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.bittailor.filetemplates.Activator;

public class SelectTemplatePage extends WizardPage implements SelectionListener {


   private List fList;
   private NodeList fGenerators;
   private Label fInfo;

   public SelectTemplatePage() {
      super("Select Generator");
      setTitle("File Template Generator");
      setDescription("This wizard creates files with the selected template generator.");
   }

   /**
    * @see IDialogPage#createControl(Composite)
    */
   public void createControl(Composite parent) {
      try {
         Activator activator = Activator.getDefault();
         String xmlLocation = activator.getTemplateLocation()+"/"+Activator.FILETEMPLATES_XML;
         InputStream xml = new FileInputStream(xmlLocation);    
         Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
         Element root = document.getDocumentElement();
         fGenerators = root.getElementsByTagName("generator"); 
      } catch (Exception e) {
         Activator.logThrowable(e);
         throw new Error("problem loading templates definition file "+Activator.FILETEMPLATES_XML,e);
      }


      Composite container = new Composite(parent, SWT.NULL);
      GridLayout layout = new GridLayout();
      container.setLayout(layout);
      layout.numColumns = 1;
      layout.verticalSpacing = 9;

      Label label = new Label(container, SWT.NULL);
      label.setText("Select a generator");

      fList = new List (container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
      for (int i = 0; i < fGenerators.getLength(); i++) {
         Element generator = (Element)fGenerators.item(i);
         fList.add(generator.getAttribute("name"));
      }

      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      fList.setLayoutData(gd);
      fList.addSelectionListener(this);

      fInfo = new Label(container, SWT.WRAP);
      fInfo.setText(" \n ");
      dialogChanged();
      setControl(container);
   }

   private void dialogChanged() {
      if(fList.getSelection().length>0){
         updateStatus(null);
         Node description = getGenerator().getElementsByTagName("description").item(0);
         if (description!=null) {
            fInfo.setText(((Element)description).getTextContent());        
         }else{
            fInfo.setText("No description");
         }
         fList.getParent().layout(true);
      }else{
         updateStatus("no generator selected");
      }
   }

   private void updateStatus(String message) {
      setErrorMessage(message);
      setPageComplete(message == null);
   }

   public void widgetDefaultSelected(SelectionEvent e) {  
   }

   public void widgetSelected(SelectionEvent e) {
      dialogChanged();
   }

   public Element getGenerator() {
      return (Element)fGenerators.item(fList.getSelectionIndex());
   }

}