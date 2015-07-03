package lqwprintvar;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;


public class PrintVar implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {

	private IWorkbenchWindow window;
	
	@Override
	public void run(IAction action) {
		// TODO Auto-generated met
		IEditorPart ep = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        ITextSelection its = (ITextSelection)ep.getEditorSite().getSelectionProvider().getSelection();
        AbstractTextEditor editor = (AbstractTextEditor)ep;
        IDocument iDocument = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        String selectedStr = its.getText();
        int startLine = its.getStartLine();
		
		try {
			int startOffset = iDocument.getLineOffset(startLine);
			String startLineStr = iDocument.get(startOffset, iDocument.getLineLength(startLine));
			String changedStr = startLineStr + "\nSystem.out.println(\"" + selectedStr + ": \" + " + selectedStr + ");\n\n";
			iDocument.replace(startOffset, startLineStr.length(), changedStr);
			System.out.print(changedStr);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		this.window = window;
	}

}
