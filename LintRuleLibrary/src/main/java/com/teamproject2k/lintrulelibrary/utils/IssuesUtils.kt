package com.teamproject2k.lintrulelibrary.utils

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.teamproject2k.lintrulelibrary.detectors.RulesDetector

object IssuesUtils {
    const val ActivityNameIssueText = "Activity name must end with activity"
    val ActivityNameIssue = Issue.create(
        "ActivityNameRule",
        ActivityNameIssueText,
        ActivityNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )

    const val BroadcastReceiverNameIssueText = "Broadcast Receiver name must end with Receiver"
    val BroadcastReceiverNameIssue = Issue.create(
        "BroadcastReceiverNameRule",
        BroadcastReceiverNameIssueText,
        BroadcastReceiverNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )

    const val EnumNameIssueText = "Enum class must end with Enum"
    val EnumNameIssue = Issue.create(
        "EnumNameRule",
        EnumNameIssueText,
        EnumNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )

    const val ViewModelNameIssueText = "ViewModel name must end with ViewModel"
    val ViewModelNameIssue = Issue.create(
        "ViewModelNameRule",
        ViewModelNameIssueText,
        ViewModelNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )

    const val InterfaceNameIssueText = "Interface name must start with I"
    val InterfaceNameIssue = Issue.create(
        "InterfaceNameRule",
        InterfaceNameIssueText,
        InterfaceNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )

    const val EnumConstantNameIssueText = "Enum constants must be title case"
    val EnumConstantNameIssue = Issue.create(
        "EnumConstantsNameRule",
        EnumConstantNameIssueText,
        EnumConstantNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )

    const val StaticVariableNameIssueText = "Static variables must be title case"
    val StaticVariableNameIssue = Issue.create(
        "StaticVariableNameRule",
        StaticVariableNameIssueText,
        StaticVariableNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )

    const val StateFlowNameIssueText = "variables of type StateFlow must end with StateFlow"
    val StateFlowNameIssue = Issue.create(
        "StateFlowNameRule",
        StateFlowNameIssueText,
        StateFlowNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )
    const val StateNameIssueText = "variables of type State must end with State"
    val StateNameIssue = Issue.create(
        "StateNameRule",
        StateNameIssueText,
        StateNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )
}