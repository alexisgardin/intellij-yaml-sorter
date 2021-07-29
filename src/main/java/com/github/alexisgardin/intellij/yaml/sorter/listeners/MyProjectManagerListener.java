package com.github.alexisgardin.intellij.yaml.sorter.listeners;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;

public class MyProjectManagerListener
    implements ProjectManagerListener {

  public void projectOpened(Project project) {
    System.out.println("project open");
  }
}
