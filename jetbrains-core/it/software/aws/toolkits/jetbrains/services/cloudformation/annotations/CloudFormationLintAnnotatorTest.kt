// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.cloudformation.annotations

import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.runInEdtAndGet
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.yaml.YAMLLanguage
import org.jetbrains.yaml.psi.YAMLFile
import org.junit.Rule
import org.junit.Test
import software.aws.toolkits.jetbrains.utils.rules.JavaCodeInsightTestFixtureRule

class CloudFormationLintAnnotatorTest {

    @Rule
    @JvmField
    val projectRule = JavaCodeInsightTestFixtureRule()

    var cloudFormationLintAnnotator: CloudFormationLintAnnotator = CloudFormationLintAnnotator()

    @Test
    fun canAnnotate() {
        val initialAnnotationResults = cloudFormationLintAnnotator.collectInformation(yamlFile())
        assertThat(initialAnnotationResults).isNotNull
        val errors = cloudFormationLintAnnotator.doAnnotate(initialAnnotationResults)
        assertThat(errors).isNotNull
        assertThat(errors.size).isEqualTo(1)

        // TODO test errorAnnotator.apply(), but I would need to be able to mock a static method
    }

    private fun yamlFile(): YAMLFile = runInEdtAndGet {
        PsiFileFactory.getInstance(projectRule.project).createFileFromText(
            YAMLLanguage.INSTANCE, """
Resources:
  s3Bucket:
    Type: undef
        """.trimIndent()
        ) as YAMLFile
    }
}
