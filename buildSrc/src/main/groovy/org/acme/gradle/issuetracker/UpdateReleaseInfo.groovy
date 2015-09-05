package org.acme.gradle.issuetracker

import groovyx.net.http.RESTClient
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class UpdateReleaseInfo extends DefaultTask {

    String url = "https://api.github.com/repos/thureto/git-flow-process/milestones"

    @TaskAction
    void check() {
        int versionId = getVersionId()
        if (versionId != null) {
            closeVersion(versionId)
        }

    }

    void closeVersion(int milestoneNo) {
        def rest = new RESTClient("https://api.github.com")
        String userPassBase64 = "${System.getProperty('githubUsername')}:x-oauth-basic".toString().bytes.encodeBase64()
        rest.patch(
                headers: ['User-Agent': 'Gradle 1.12', "Authorization": "Basic $userPassBase64"],
                contentType: groovyx.net.http.ContentType.JSON,
                path: "/repos/thureto/git-flow-process/milestones/3",
                body: [state: "closed", description: String.format("released via Gradle release process at %s", new Date().toString())]
        )
    }

    int getVersionId() {
        def json = download()
        println "project.version.toString() " + project.version.toString()
        def currentRelease = new groovy.json.JsonSlurper().parseText(json).find { entry ->
            entry.title == project.version.toString()
        }
        if (currentRelease != null) {
            return currentRelease.number
        }
        return null
    }

    def download() {
        try {
            return new URL(url).text
        } catch (IOException e) {
            throw new GradleException("Unable to acquire versions info. I've tried this url: '$url'.\n"
                    + "If you don't have the network connection please run with '--offline' or exclude this task from execution via '-x'."
                    , e)
        }
    }
}
