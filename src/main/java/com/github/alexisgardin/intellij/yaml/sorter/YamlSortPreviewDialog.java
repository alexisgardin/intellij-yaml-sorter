package com.github.alexisgardin.intellij.yaml.sorter;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class YamlSortPreviewDialog extends DialogWrapper {
    private final Project project;
    private final String originalText;
    private final String sortedText;
    private boolean applyChanges = false;
    private DiffRequestPanel diffPanel;

    public YamlSortPreviewDialog(@Nullable Project project, @NotNull String originalText, @NotNull String sortedText) {
        super(project, true);
        this.project = project;
        this.originalText = originalText;
        this.sortedText = sortedText;

        setTitle("YAML Sort Preview");
        setModal(true);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        try {
            // Get YAML file type for proper syntax highlighting
            FileType yamlFileType = FileTypeManager.getInstance().getFileTypeByExtension("yml");

            // Create diff contents with proper file type
            DiffContentFactory contentFactory = DiffContentFactory.getInstance();
            DiffContent beforeContent = contentFactory.create(originalText, yamlFileType);
            DiffContent afterContent = contentFactory.create(sortedText, yamlFileType);

            // Create a simple diff request
            SimpleDiffRequest request = new SimpleDiffRequest(
                "YAML Sort Preview",
                beforeContent,
                afterContent,
                "Before (Original)",
                "After (Sorted)"
            );

            // Create the diff panel
            diffPanel = DiffManager.getInstance().createRequestPanel(project, getDisposable(), null);
            diffPanel.setRequest(request);

            JPanel mainPanel = new JPanel(new BorderLayout());

            // Add info label at the top
            JLabel infoLabel = new JLabel("Review the changes that will be applied to your YAML file:");
            infoLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            mainPanel.add(infoLabel, BorderLayout.NORTH);

            // Add the diff panel
            mainPanel.add(diffPanel.getComponent(), BorderLayout.CENTER);

            mainPanel.setPreferredSize(new Dimension(1000, 700));
            return mainPanel;

        } catch (Exception e) {
            // Fallback to simple text display if diff viewer fails
            JPanel fallbackPanel = new JPanel(new BorderLayout());
            JLabel errorLabel = new JLabel("Could not create diff preview. Click 'Apply Changes' to proceed with sorting.");
            errorLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            fallbackPanel.add(errorLabel, BorderLayout.CENTER);
            fallbackPanel.setPreferredSize(new Dimension(600, 400));
            return fallbackPanel;
        }
    }

    @Override
    protected Action @NotNull [] createActions() {
        Action applyAction = new AbstractAction("Apply Changes") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                applyChanges = true;
                close(OK_EXIT_CODE);
            }
        };
        applyAction.putValue(Action.DEFAULT, Boolean.TRUE);

        Action cancelAction = new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                applyChanges = false;
                close(CANCEL_EXIT_CODE);
            }
        };

        return new Action[]{applyAction, cancelAction};
    }

    @Override
    protected void dispose() {
        if (diffPanel != null) {
            Disposer.dispose(diffPanel);
        }
        super.dispose();
    }

    public boolean showAndConfirm() {
        show();
        return applyChanges;
    }
}