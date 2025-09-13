package com.github.alexisgardin.intellij.yaml.sorter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SortYamlAction extends AnAction {
  private DumperOptions options;

  public SortYamlAction() {

    options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
  }

  @Override
  public void update(AnActionEvent e) {
    VirtualFile virtualFile = e.getData(CommonDataKeys.EDITOR).getVirtualFile();
    final String defaultExtension = virtualFile.getFileType().getDefaultExtension();

    Project project = e.getProject();
    e.getPresentation().setEnabledAndVisible(
        project != null && (defaultExtension.equals("yml") || defaultExtension.equals("yaml")));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    // Get all the required data from data keys
    final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
    final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
    final Document document = editor.getDocument();

    Yaml yaml = new Yaml(options);

    final PsiFile data = e.getDataContext().getData(CommonDataKeys.PSI_FILE);
    Map<String, Object> yamlMap =
        yaml.load(data.getText());
    final Map<String, Object> stringObjectMap = sortMap(yamlMap);
    System.out.println(stringObjectMap);
    final String dump = yaml.dump(stringObjectMap);
    System.out.println(dump);
    WriteCommandAction.runWriteCommandAction(project, () ->
        document.setText(dump)
    );
  }

  public String mapToYaml(Map<String, Object> map) {
    Yaml yaml = new Yaml(options);
    return yaml.dump(map);
  }

  public Map<String, Object> sortMap(Map<String, Object> initMap) {
    final Map<String, Object> sortedMap = this.sortByKey(initMap);
    for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
      sortMapRecursive(entry);
    }
    return sortedMap;
  }

  public void sortMapRecursive(Map.Entry<String, Object> entry) {
    final Object val = entry.getValue();
    if (val instanceof Map) {
      final Map<String, Object> value = sortByKey(
          (Map<String, Object>) val);
      entry.setValue(value);
      for (Map.Entry<String, Object> recurEntry : value.entrySet()) {
        sortMapRecursive(recurEntry);
      }
    } else if (val instanceof ArrayList) {
      for (int i = 0; i < ((ArrayList) val).size(); i++) {
        Object listValue = ((ArrayList) val).get(i);
        if (listValue instanceof Map) {
          final Map<String, Object> value = sortByKey((Map<String, Object>) listValue);
          ((ArrayList) val).set(i, value);
          for (Map.Entry<?, ?> recurEntry : value.entrySet()) {
            sortMapRecursive((Map.Entry<String, Object>) recurEntry);
          }
        }
      }
      entry.setValue(val);
    }
  }

  private <K extends String, V> Map<K, V> sortByKey(Map<K, V> map) {
    List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
    list.sort(Map.Entry.comparingByKey());

    Map<K, V> result = new LinkedHashMap<>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }

    return result;
  }
}
