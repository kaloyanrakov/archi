package com.archimatetool.editor.diagram.actions;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;

import com.archimatetool.editor.tools.GenerateDiffViewCommand;
import com.archimatetool.model.IArchimateDiagramModel;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IArchimateDiagramModel;
/**
 * Action to compare the current diagram view with another and generate a diff view.
 */
public class CompareDiagramViewsAction extends SelectionAction {

    public static final String ID = "CompareDiagramViewsAction"; //$NON-NLS-1$

    public CompareDiagramViewsAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
        setText("Compare with...");
    }

    @Override
    protected boolean calculateEnabled() {
        // Use IDiagramModel.class as the adapter key — same pattern as ViewpointAction
        return getWorkbenchPart().getAdapter(IDiagramModel.class) instanceof IArchimateDiagramModel;
    }

    @Override
    public void run() {
        IArchimateDiagramModel viewA =
            (IArchimateDiagramModel) getWorkbenchPart().getAdapter(IDiagramModel.class);
        if (viewA == null) return;

        GenerateDiffViewCommand command = new GenerateDiffViewCommand(viewA);
        if (command.openDialog(getWorkbenchPart().getSite().getShell())) {
            execute(command);
        }
    }
}