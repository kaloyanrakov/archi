package com.archimatetool.editor.propertysections;

import java.util.Set;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;

import com.archimatetool.editor.ui.textrender.TextRenderer;
import com.archimatetool.editor.views.tree.commands.MoveObjectCommand;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IDiagramModelArchimateObject;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IProperties;

public class LevelingPropertyDecorator implements IPropertyDecorator {

    // Public — single source of truth for the key name
    public static final String PROPERTY_MODEL_LEVEL = "Model Level"; //$NON-NLS-1$

    // Public — UserPropertiesSection references this for the dropdown
    public static final String[] MODEL_LEVEL_VALUES = {
        "Level 1", //$NON-NLS-1$
        "Level 2", //$NON-NLS-1$
        "Level 3"  //$NON-NLS-1$
    };

    // Public — UserPropertiesSection uses this in READ_ONLY_KEYS
    public static final Set<String> MODEL_LEVEL_KEYS = Set.of(PROPERTY_MODEL_LEVEL);

    // Private — the empty string prefix is a UI concern, not exposed
    private static final String[] RESTRICTED_VALUES;
    static {
        RESTRICTED_VALUES = new String[MODEL_LEVEL_VALUES.length + 1];
        RESTRICTED_VALUES[0] = ""; //$NON-NLS-1$
        System.arraycopy(MODEL_LEVEL_VALUES, 0, RESTRICTED_VALUES, 1, MODEL_LEVEL_VALUES.length);
    }

    private static final String LABEL_EXPRESSION = "${property:Model Level} ${name}"; //$NON-NLS-1$

    @Override
    public String getPropertyKey() {
        return PROPERTY_MODEL_LEVEL;
    }

    @Override
    public boolean appliesTo(IProperties target) {
        return target instanceof IArchimateElement;
    }

    @Override
    public String[] getRestrictedValues() {
        return RESTRICTED_VALUES;
    }

    /**
     * For LevelingPropertyDecorator the restricted values are fixed —
     * they don't depend on the element, so delegate to the no-arg version.
     */
    @Override
    public String[] getRestrictedValues(IProperties element) {
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