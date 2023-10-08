package com.example.lintrulelibrary

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue
import com.example.lintrulelibrary.utils.IssuesUtils.ActivityNameIssue
import com.example.lintrulelibrary.utils.IssuesUtils.BroadcastReceiverNameIssue
import com.example.lintrulelibrary.utils.IssuesUtils.EnumConstantNameIssue
import com.example.lintrulelibrary.utils.IssuesUtils.EnumNameIssue
import com.example.lintrulelibrary.utils.IssuesUtils.InterfaceNameIssue
import com.example.lintrulelibrary.utils.IssuesUtils.StaticVariableNameIssue
import com.example.lintrulelibrary.utils.IssuesUtils.ViewModelNameIssue

class RulesIssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(
            EnumConstantNameIssue,
            BroadcastReceiverNameIssue,
            EnumNameIssue,
            InterfaceNameIssue,
            ActivityNameIssue,
            ViewModelNameIssue,
            StaticVariableNameIssue
        )
}