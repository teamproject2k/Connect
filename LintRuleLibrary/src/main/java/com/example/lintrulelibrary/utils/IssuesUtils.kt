package com.example.lintrulelibrary.utils

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.example.lintrulelibrary.detectors.RulesDetector

object IssuesUtils {
    val ActivityNameIssueText = "Activity name must end with activity"
    val ActivityNameIssue = Issue.create(
        "ActivityNameRule",
        ActivityNameIssueText,
        ActivityNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )

    val BroadcastReceiverNameIssueText = "Broadcast Receiver name must end with Receiver"
    val BroadcastReceiverNameIssue = Issue.create(
        "BroadcastReceiverNameRule",
        BroadcastReceiverNameIssueText,
        BroadcastReceiverNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )

    val EnumNameIssueText = "Enum class must end with Enum"
    val EnumNameIssue = Issue.create(
        "EnumNameRule",
        EnumNameIssueText,
        EnumNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )

    val ViewModelNameIssueText = "ViewModel name must end with ViewModel"
    val ViewModelNameIssue = Issue.create(
        "ViewModelNameRule",
        ViewModelNameIssueText,
        ViewModelNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )

    val InterfaceNameIssueText = "Interface name must start with I"
    val InterfaceNameIssue = Issue.create(
        "InterfaceNameRule",
        InterfaceNameIssueText,
        InterfaceNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )

    val EnumConstantNameIssueText = "Enum constants must be title case"
    val EnumConstantNameIssue = Issue.create(
        "EnumConstantsNameRule",
        EnumConstantNameIssueText,
        EnumConstantNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )

    val StaticVariableNameIssueText = "Static variables must be title case"
    val StaticVariableNameIssue = Issue.create(
        "StaticVariableNameRule",
        StaticVariableNameIssueText,
        StaticVariableNameIssueText,
        Category.CORRECTNESS,
        6,
        Severity.WARNING,
        Implementation(RulesDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )
}