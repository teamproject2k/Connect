package com.teamproject2k.lintrulelibrary

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.ActivityNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.BroadcastReceiverNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.EnumConstantNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.EnumNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.InterfaceNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.StateFlowNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.StateNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.StaticVariableNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.ViewModelNameIssue

class RulesIssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(
            EnumConstantNameIssue,
            BroadcastReceiverNameIssue,
            EnumNameIssue,
            InterfaceNameIssue,
            ActivityNameIssue,
            ViewModelNameIssue,
            StaticVariableNameIssue,
            StateFlowNameIssue,
            StateNameIssue
        )
}