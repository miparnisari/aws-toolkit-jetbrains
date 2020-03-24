// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.cloudwatch.logs.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient
import software.aws.toolkits.jetbrains.core.awsClient
import software.aws.toolkits.resources.message

class DownloadLogStream(private val project: Project, private val logGroup: String, private val alreadyDownloaded: String) : AnAction(message("cloudwatch.logs.save_action"), null, AllIcons.Actions.Menu_saveall), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val client: CloudWatchLogsClient = project.awsClient()
    }
}
