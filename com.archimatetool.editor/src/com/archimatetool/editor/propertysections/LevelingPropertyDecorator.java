package com.archimatetool.editor.propertysections;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;

import com.archimatetool.editor.ui.textrender.TextRenderer;
import com.archimatetool.editor.views.tree.commands.MoveObjectCommand;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IDiagramModelArchimateObject;
import com.archimatetool.model.IFolder;

public class LevelingPropertyDecorator implements IPropertyDecorator {

    private static final String PROPERTY_KEY = "Model Level"; //$NON-NLS-1$
    private static final String LABEL_EXPRESSION = "${property:Model Level} ${name}"; //$NON-NLS-1$
    private static final String[] RESTRICTED_VALUES = {"", "L1", "L2", "L3"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    @Override
    public String getPropertyKey() {
        return PROPERTY_KEY;
    }

    @Override
    public String[] getRestrictedValues() {
        return RESTRICTED_VALUES;
    }

    @Override
    public void contributeCommands(IArchimateElement element, String newValue, CompoundCommand cmd) {
        contributeFolderCommand(element, newValue, cmd);
        contributeLabelExpressionCommands(element, newValue, cmd);
    }

    private void contributeFolderCommand(IArchimateElement element, String newValue, CompoundCommand cmd) {
        IArchimateModel model = element.getArchimateModel();
        if(model == null) return;

        IFolder currentFolder = (IFolder) element.eContainer();

        if(!newValue.isEmpty()) {
            IFolder targetFolder = getOrCreateLevelFolder(model, element, newValue);
            if(targetFolder != null && !targetFolder.equals(currentFolder)) {
                cmd.add(new MoveObjectCommand(targetFolder, element));
            }
        }
        else {
            IFolder rootFolder = model.getDefaultFolderForObject(element);
            if(rootFolder != null && !rootFolder.equals(currentFolder)) {
                cmd.add(new MoveObjectCommand(rootFolder, element));
            }
        }
    }

    private void contributeLabelExpressionCommands(IArchimateElement element, String newValue, CompoundCommand cmd) {
        final String newExpr = !newValue.isEmpty() ? LABEL_EXPRESSION : null;

        for(IDiagramModelArchimateObject dmao : element.getReferencingDiagramObjects()) {
            final IDiagramModelArchimateObject finalDmao = dmao;

            cmd.add(new Command() {
                private String oldExpr;

                @Override
                public void execute() {
                    oldExpr = finalDmao.getFeatures().getString(TextRenderer.FEATURE_NAME, null);
                    if(newExpr != null) {
                        finalDmao.getFeatures().putString(TextRenderer.FEATURE_NAME, newExpr);
                    }
                    else {
                        finalDmao.getFeatures().remove(TextRenderer.FEATURE_NAME);
                    }
                }

                @Override
                public void undo() {
                    if(oldExpr != null) {
                        finalDmao.getFeatures().putString(TextRenderer.FEATURE_NAME, oldExpr);
                    }
                    else {
                        finalDmao.getFeatures().remove(TextRenderer.FEATURE_NAME);
                    }
                }
            });
        }
    }

    private IFolder getOrCreateLevelFolder(IArchimateModel model, IArchimateElement element, String levelValue) {
        IFolder rootFolder = model.getDefaultFolderForObject(element);
        if(rootFolder == null) return null;

        for(IFolder sub : rootFolder.getFolders()) {
            if(levelValue.equals(sub.getName())) {
                return sub;
            }
        }

        IFolder newFolder = IArchimateFactory.eINSTANCE.createFolder();
        newFolder.setName(levelValue);
        rootFolder.getFolders().add(newFolder);
        return newFolder;
    }
}