package ch.bittailor.filetemplates.wizards;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.bittailor.filetemplates.Activator;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (test).
 */

public class SelectTemplatePage extends WizardPage implements SelectionListener {

	
  private List fList;
  private NodeList fGenerators;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public SelectTemplatePage(ISelection selection) {
		super("wizardPage");
		setTitle("Multi-page Editor File");
		setDescription("This wizard creates files with the selected template generator.");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
	  DocumentBuilder builder;
	  Element root = null;
	  try {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      File file = new File(Activator.getDefault().getTemplateStorage(),"filetemplates.xml");
      if(!file.exists()){
        System.err.println(file.toString()+" does not exist!");
      }
      else
      {
        Document doc = builder.parse(file);
        root = doc.getDocumentElement();
      }
    } catch (ParserConfigurationException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
	  Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.verticalSpacing = 9;
		
		Label label = new Label(container, SWT.NULL);
    label.setText("&Templates:");

		
		fList = new List (container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
    fGenerators = root.getElementsByTagName("generator");
    for (int i = 0; i < fGenerators.getLength(); i++) {
      Element generator = (Element)fGenerators.item(i);
      fList.add(generator.getAttribute("name"));
    }
    
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		fList.setLayoutData(gd);
		fList.addSelectionListener(this);
    
		dialogChanged();
		setControl(container);
	}

	private void dialogChanged() {
	  if(fList.getSelection().length>0){
	    updateStatus(null);
	  }else{
	    updateStatus("no template selected");
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