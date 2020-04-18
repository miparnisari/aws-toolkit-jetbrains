// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0
package software.aws.toolkits.jetbrains.services.cloudformation.annotations

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

/**
 *
 * Process the files in a language by an external annotation tool ("linter").
 * External annotators are expected to be (relatively) slow and are started
 * after regular annotators have completed their work.
 *
 * Annotators work in three steps:
 * 1. [.collectInformation] is called to collect some data about a file needed for launching a tool
 * 2. collected data is passed to [.doAnnotate] which executes a tool and collect highlighting data
 * 3. highlighting data is applied to a file by [.apply]
 *
 */
class ErrorAnnotator : ExternalAnnotator<InitialAnnotationResults, List<ErrorAnnotation>>() {

    /**
     * Collects initial information needed for launching a tool. This method is called within a read action;
     * non-[DumbAware][com.intellij.openapi.project.DumbAware] annotators are skipped during indexing.
     *
     * @param psiFile a file to annotate
     * @return information to pass to [)][.doAnnotate], or `null` if not applicable
     */
    override fun collectInformation(psiFile: PsiFile): InitialAnnotationResults {
        val pathToTemplate = psiFile.virtualFile.path
        return InitialAnnotationResults(pathToTemplate)
    }

    /**
     * Collects full information required for annotation. This method is intended for long-running activities
     * and will be called outside a read action; implementations should either avoid accessing indices and PSI or
     * perform needed checks and locks themselves.
     *
     * @param initialAnnotationResults initial information gathered by [.collectInformation]
     * @return annotations to pass to [.apply]
     */
    override fun doAnnotate(initialAnnotationResults: InitialAnnotationResults): List<ErrorAnnotation> {
        val linter = Linter()
        return linter.execute(initialAnnotationResults)
    }

    /**
     * Applies collected annotations to the given annotation holder. This method is called within a read action.
     *
     * @param file a file to annotate
     * @param annotationResult annotations collected in #doAnnotate
     * @param holder a container for receiving annotations
     */
    override fun apply(
        file: PsiFile,
        annotationResult: List<ErrorAnnotation>,
        holder: AnnotationHolder
    ) {
        val document = FileDocumentManager.getInstance().getDocument(file.virtualFile)
        annotationResult.forEach { error ->
            val startOffset = document?.getLineStartOffset(error.location?.start?.lineNumber!! - 1)
                            ?.plus(error.location.start.columnNumber - 1)
                            ?: 0
            val endOffset = document?.getLineStartOffset(error.location?.end?.lineNumber!! - 1)
                            ?.plus(error.location.end.columnNumber - 1)
                            ?: 0
            val textRange = TextRange(startOffset, endOffset)
            holder.createAnnotation(error.severity, textRange, error.message)
        }
    }
}
