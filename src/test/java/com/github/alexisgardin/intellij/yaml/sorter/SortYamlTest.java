package com.github.alexisgardin.intellij.yaml.sorter;


import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;


public class SortYamlTest {

  @Test
  public void testYamlSorting(){
    InputStream unsorted = SortYamlTest.class.getClassLoader().getResourceAsStream("unsorted.yml");
    InputStream sorted = SortYamlTest.class.getClassLoader().getResourceAsStream("sorted.yml");

    SortYamlAction sortYamlAction = new SortYamlAction();

    Yaml sortedYaml = new Yaml();
    Yaml unsortedYaml = new Yaml();

    final Map<String, Object> sortedMap = sortedYaml.load(sorted);
    final Map<String, Object> unsortedMap = unsortedYaml.load(unsorted);
    final Map<String, Object> testSortedMap = sortYamlAction.sortMap(unsortedMap);
    Assert.assertEquals(sortYamlAction.mapToYaml(sortedMap), sortYamlAction.mapToYaml(testSortedMap));
  }
}
