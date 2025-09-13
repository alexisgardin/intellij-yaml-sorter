package com.github.alexisgardin.intellij.yaml.sorter;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class SortYamlWithPreviewAction extends AnAction {
    private final SortYamlAction sortYamlAction = new SortYamlAction();

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        final String defaultExtension = psiFile.getFileType().getDefaultExtension();

        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(
            project != null && (defaultExtension.equals("yml") || defaultExtension.equals("yaml")));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Get all the required data from data keys
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        final Project project = e.getData(CommonDataKeys.PROJECT);
        if (editor == null || project == null) {
            return;
        }
        final Document document = editor.getDocument();
        final PsiFile data = e.getDataContext().getData(CommonDataKeys.PSI_FILE);

        if (data == null) {
            return;
        }

        final String originalText = data.getText();
        final String sortedText = sortYamlAction.sortYamlDocuments(originalText);

        // Check if there are any changes
        if (originalText.equals(sortedText)) {
            // No changes needed
            com.intellij.openapi.ui.Messages.showInfoMessage(
                project,
                "The YAML file is already sorted.",
                "YAML Sorter"
            );
            return;
        }

        // Show embedded diff preview with Apply/Cancel buttons
        YamlSortPreviewDialog dialog = new YamlSortPreviewDialog(project, originalText, sortedText);
        boolean shouldApply = dialog.showAndConfirm();

        // Apply changes if user confirmed
        if (shouldApply) {
            WriteCommandAction.runWriteCommandAction(project, "Sort YAML", "YAML Sorter", () ->
                document.setText(sortedText)
            );
        }
    }
}