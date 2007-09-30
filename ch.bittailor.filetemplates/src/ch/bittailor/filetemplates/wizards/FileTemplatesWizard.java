package ch.bittailor.filetemplates.wizards;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.w3c.dom.Element;

import ch.bittailor.filetemplates.Activator;
import ch.bittailor.filetemplates.freemarker.Generator;
import freemarker.template.TemplateException;

public class FileTemplatesWizard extends Wizard implements INewWizard {
  private ISelection fSelection;
  private SelectTemplatePage fPageOne;
  private SelectContainersPage fPageTwo;

  public FileTemplatesWizard() {
    super();
    setNeedsProgressMonitor(true);
  }

  @Override
  public void addPages() {
    fPageOne = new SelectTemplatePage();
    addPage(fPageOne);
    fPageTwo = new SelectContainersPage(fSelection);
    addPage(fPageTwo);
  }

  @Override
  public boolean performFinish() {
    final Element generator = fPageOne.getGenerator();
    final String containerName = fPageTwo.getContainerName();
    IRunnableWithProgress runnable = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try {
          doFinish(containerName, generator, monitor);
        } catch (CoreException e) {
          throw new InvocationTargetException(e);
        } finally {
          monitor.done();
        }
      }
    };
    try {
      getContainer().run(true, false, runnable);
    } catch (InterruptedException e) {
      return false;
    } catch (InvocationTargetException e) {
      Throwable realException = e.getTargetException();
      MessageDialog.openError(getShell(), "Error", realException.getMessage());
      return false;
    }
    return true;
  }

  private void doFinish(String containerName,Element fileName, IProgressMonitor monitor)	throws CoreException {

    monitor.beginTask("generating files ...", 2);
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

    IResource resource = root.findMember(new Path(containerName));
    if (!resource.exists() || !(resource instanceof IContainer)) {
      throwCoreException("Container \"" + containerName + "\" does not exist.");
    }
    IContainer container = (IContainer) resource;

    
    Generator generator;
    try {
      generator = new Generator(Activator.getDefault().getTemplateLocation());
    
      final List<IFile> files = generator.generate(container, monitor, fileName);
      
      monitor.worked(1);
      monitor.setTaskName("opening file(s) for editing...");
      getShell().getDisplay().asyncExec(new Runnable() {
        public void run() {
          IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
          for (IFile file : files) {
            try {
              IDE.openEditor(page, file, true);
            } catch (PartInitException e) {
            }
          }		
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
      throw new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,IStatus.OK,"io problem while processing templates:\n"
          +e.getMessage(),e));
    } catch (TemplateException e) {
      e.printStackTrace();
      throw new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,IStatus.OK,"template problem while processing templates:\n"
          +e.getFTLInstructionStack()+"\n"
          +e.getMessage(),e));
    }
    monitor.worked(1);
  }

  private void throwCoreException(String message) throws CoreException {
    IStatus status =
      new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, message, null);
    throw new CoreException(status);
  }

  public void init(IWorkbench workbench, IStructuredSelection selection) {
    fSelection = selection;
  }

}