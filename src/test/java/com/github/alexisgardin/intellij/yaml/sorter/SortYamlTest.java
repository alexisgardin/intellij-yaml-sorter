package com.github.alexisgardin.intellij.yaml.sorter;

import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

public class SortYamlTest {

  @Test
  public void testYamlSorting() {
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

  @Test
  public void testStringArraySorting() {
    String unsortedContent = readResourceFile("string-arrays-unsorted.yml");
    String expectedSortedContent = readResourceFile("string-arrays-sorted.yml");

    SortYamlAction sortYamlAction = new SortYamlAction();
    String actualSorted = sortYamlAction.sortYamlDocuments(unsortedContent);

    // Normalize line endings and whitespace for comparison
    String normalizedExpected = normalizeYaml(expectedSortedContent);
    String normalizedActual = normalizeYaml(actualSorted);

    Assert.assertEquals(normalizedExpected, normalizedActual);
  }

  @Test
  public void testMultiDocumentYamlSorting() {
    String unsortedContent = readResourceFile("multi-document-unsorted.yml");
    String expectedSortedContent = readResourceFile("multi-document-sorted.yml");

    SortYamlAction sortYamlAction = new SortYamlAction();
    String actualSorted = sortYamlAction.sortYamlDocuments(unsortedContent);

    // Normalize line endings and whitespace for comparison
    String normalizedExpected = normalizeYaml(expectedSortedContent);
    String normalizedActual = normalizeYaml(actualSorted);

    Assert.assertEquals(normalizedExpected, normalizedActual);
  }

  @Test
  public void testSingleDocumentWithoutSeparator() {
    // Test that single documents still work correctly
    String yamlContent = "z_key: value1\na_key: value2\nm_key: value3";
    String expected = "a_key: value2\nm_key: value3\nz_key: value1\n";

    SortYamlAction sortYamlAction = new SortYamlAction();
    String actual = sortYamlAction.sortYamlDocuments(yamlContent);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testEmptyArrayHandling() {
    String yamlContent = "config:\n  empty_array: []\n  normal_key: value";

    SortYamlAction sortYamlAction = new SortYamlAction();
    String result = sortYamlAction.sortYamlDocuments(yamlContent);

    // Should not crash and should maintain empty array
    Assert.assertTrue(result.contains("empty_array: []"));
    Assert.assertTrue(result.contains("normal_key: value"));
  }

  @Test
  public void testMixedArrayTypes() {
    String yamlContent = "config:\n  string_array:\n    - zebra\n    - alpha\n  object_array:\n    - name: zebra\n      value: 1\n    - name: alpha\n      value: 2";

    SortYamlAction sortYamlAction = new SortYamlAction();
    String result = sortYamlAction.sortYamlDocuments(yamlContent);

    // String array should be sorted alphabetically (alpha should come before zebra)
    int alphaIndex = result.indexOf("- alpha");
    int zebraIndex = result.indexOf("- zebra");
    Assert.assertTrue("Alpha should come before zebra in sorted order", alphaIndex < zebraIndex);
    // Object array should have keys sorted but maintain object order
    Assert.assertTrue(result.contains("name: zebra"));
    Assert.assertTrue(result.contains("name: alpha"));
  }

  @Test
  public void testSortingUtilityClass() {
    String originalYaml = "z_key: value1\na_key: value2\nm_key: value3";
    String expectedSorted = "a_key: value2\nm_key: value3\nz_key: value1\n";

    SortYamlAction sortYamlAction = new SortYamlAction();
    String actualSorted = sortYamlAction.sortYamlDocuments(originalYaml);

    // Test the core sorting logic utility class
    Assert.assertEquals("Content should be sorted correctly", expectedSorted, actualSorted);
    Assert.assertNotEquals("Original and sorted should be different", originalYaml, actualSorted);
  }

  @Test
  public void testNoChangesDetection() {
    String alreadySorted = "a_key: value1\nz_key: value2";

    SortYamlAction sortYamlAction = new SortYamlAction();
    String result = sortYamlAction.sortYamlDocuments(alreadySorted);

    // Should detect that content is already sorted
    Assert.assertEquals("Already sorted content should remain unchanged", alreadySorted, result.trim());
  }

  private String readResourceFile(String filename) {
    InputStream inputStream = SortYamlTest.class.getClassLoader().getResourceAsStream(filename);
    if (inputStream == null) {
      throw new IllegalArgumentException("File not found: " + filename);
    }

    return new BufferedReader(new InputStreamReader(inputStream))
        .lines()
        .collect(Collectors.joining("\n"));
  }

  private String normalizeYaml(String yaml) {
    // Remove trailing whitespace and normalize line endings
    return yaml.trim().replaceAll("\\r\\n", "\n").replaceAll("\\s+$", "");
  }
}
