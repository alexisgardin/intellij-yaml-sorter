package com.github.alexisgardin.intellij.yaml.sorter;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.stream.Collectors;


public class SortYamlAction {
  private DumperOptions options;

  public SortYamlAction() {

    options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
  }


  public String sortYamlDocuments(String yamlText) {
    // Handle multi-document YAML files separated by ---
    if (yamlText.contains("---")) {
      return sortMultiDocumentYaml(yamlText);
    } else {
      return sortSingleDocumentYaml(yamlText);
    }
  }

  private String sortMultiDocumentYaml(String yamlText) {
    // Split documents by --- separator
    String[] documents = yamlText.split("---");
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < documents.length; i++) {
      String document = documents[i].trim();

      if (!document.isEmpty()) {
        // Add document separator
        if (result.length() > 0) {
          result.append("---\n");
        } else {
          result.append("---\n");
        }

        // Process and sort the document
        String sortedDoc = sortSingleDocumentYaml(document);
        result.append(sortedDoc);

        if (!sortedDoc.endsWith("\n")) {
          result.append("\n");
        }
      }
    }

    return result.toString();
  }

  private String sortSingleDocumentYaml(String yamlText) {
    try {
      Yaml yaml = new Yaml(options);
      Map<String, Object> yamlMap = yaml.load(yamlText);

      if (yamlMap == null) {
        return yamlText; // Return original if parsing fails
      }

      final Map<String, Object> sortedMap = sortMap(yamlMap);
      return yaml.dump(sortedMap);
    } catch (Exception ex) {
      // Return original text if parsing fails
      return yamlText;
    }
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
      ArrayList arrayList = (ArrayList) val;

      // Check if this is a string array and sort it
      if (isStringArray(arrayList)) {
        List<String> sortedStrings = ((List<String>) arrayList).stream()
            .sorted()
            .collect(Collectors.toList());
        entry.setValue(new ArrayList<>(sortedStrings));
      } else {
        // Handle arrays of objects
        for (int i = 0; i < arrayList.size(); i++) {
          Object listValue = arrayList.get(i);
          if (listValue instanceof Map) {
            final Map<String, Object> value = sortByKey((Map<String, Object>) listValue);
            arrayList.set(i, value);
            for (Map.Entry<?, ?> recurEntry : value.entrySet()) {
              sortMapRecursive((Map.Entry<String, Object>) recurEntry);
            }
          }
        }
        entry.setValue(arrayList);
      }
    }
  }

  private boolean isStringArray(ArrayList<?> arrayList) {
    if (arrayList.isEmpty()) {
      return false;
    }
    return arrayList.stream().allMatch(item -> item instanceof String);
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
