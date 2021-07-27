package com.github.alexisgardin.intellijyamlsorter.services

import com.github.alexisgardin.intellijyamlsorter.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
