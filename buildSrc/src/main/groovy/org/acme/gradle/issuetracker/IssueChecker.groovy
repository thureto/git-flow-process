package org.acme.gradle.issuetracker

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class IssueChecker extends DefaultTask {

    String url = "https://api.github.com/repos/thureto/git-flow-process/milestones"

    @TaskAction
    void check() {
        validate(new URL(url).text)
    }


    void validate(String json) {
        def currentRelease = new groovy.json.JsonSlurper().parseText(json).find { entry ->
            entry.title == project.version.toString()
        }
        if (currentRelease != null) {
            assert currentRelease.state == "open"
            if (currentRelease.open_issues != 0) {
                throw new GradleException("Cannot release version '${project.version.toString()}' with unresolved issues. Number of open issues: ${currentRelease.open_issues}\n" +
                        "See https://github.com/thureto/git-flow-process/issues?milestone=${currentRelease.number}&state=open/ for details!")
            }
        }
    }
}
