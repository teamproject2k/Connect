package com.teamproject2k.lintrulelibrary.detectors

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.teamproject2k.lintrulelibrary.utils.BaseUtils
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.ActivityNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.ActivityNameIssueText
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.BroadcastReceiverNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.BroadcastReceiverNameIssueText
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.EnumConstantNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.EnumConstantNameIssueText
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.EnumNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.EnumNameIssueText
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.InterfaceNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.InterfaceNameIssueText
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.StateFlowNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.StateFlowNameIssueText
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.StateNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.StateNameIssueText
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.StaticVariableNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.StaticVariableNameIssueText
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.ViewModelNameIssue
import com.teamproject2k.lintrulelibrary.utils.IssuesUtils.ViewModelNameIssueText
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UEnumConstant
import org.jetbrains.uast.UVariable

class RulesDetector : Detector(), Detector.UastScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf(UClass::class.java, UEnumConstant::class.java, UVariable::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitClass(node: UClass) {
                val className = node.name
                if (className != null) {
                    val superClassesList = node.superTypes
                    if (node.isEnum) {
                        handleEnumNameRule(node, context)
                    } else if (node.isInterface) {
                        handleInterfaceNameRule(node, context)
                    } else if (superClassesList.isNotEmpty()) {
                        superClassesList.forEach { superClass ->
                            val superClassName = superClass.name
                            if (superClassName.endsWith("Activity")) {
                                handleActivityNameRule(node, context)
                                return@forEach
                            } else if (superClassName.endsWith("BroadcastReceiver")) {
                                handleBroadcastReceiverNameRule(node, context)
                                return@forEach
                            } else if (superClassName.endsWith("ViewModel")) {
                                handleViewModelNameRule(node, context)
                                return@forEach
                            }
                        }
                    }
                }
            }

            override fun visitEnumConstant(node: UEnumConstant) {
                if (!BaseUtils.isTitleCase(node.name)) {
                    context.report(
                        EnumConstantNameIssue,
                        node,
                        context.getLocation(node as UElement),
                        EnumConstantNameIssueText
                    )
                }
            }

            override fun visitVariable(node: UVariable) {
                val variableName = node.name
                val variableType = node.type
                if (node.isStatic && variableName != null) {
                    if (!BaseUtils.isTitleCase(variableName)) {
                        context.report(
                            StaticVariableNameIssue,
                            node as UElement,
                            context.getLocation(node as UElement),
                            StaticVariableNameIssueText
                        )
                    }
                }
                if (!node.text.contains("fun") && !node.isStatic) {
                    if (variableType.canonicalText.contains("StateFlow") && node.isPhysical) {
                        if (variableName?.endsWith("StateFlow") == false) {
                            context.report(
                                StateFlowNameIssue,
                                node as UElement,
                                context.getLocation(node as UElement),
                                StateFlowNameIssueText
                            )
                        }
                    } else if (variableType.canonicalText.contains("State") && node.isPhysical) {
                        if (variableName?.endsWith("State") == false) {
                            context.report(
                                StateNameIssue,
                                node as UElement,
                                context.getLocation(node as UElement),
                                StateNameIssueText
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleViewModelNameRule(node: UClass, context: JavaContext) {
        val className = node.name
        if (className != null && !className.endsWith("ViewModel")) {
            context.report(
                ViewModelNameIssue,
                node,
                context.getLocation(node as UElement),
                ViewModelNameIssueText
            )
        }
    }


    private fun handleInterfaceNameRule(node: UClass, context: JavaContext) {
        val className = node.name
        if (className != null && className.first() != 'I' && !node.text.contains("annotation")) {
            context.report(
                InterfaceNameIssue,
                node,
                context.getLocation(node as UElement),
                InterfaceNameIssueText
            )
        }
    }

    private fun handleEnumNameRule(node: UClass, context: JavaContext) {
        val className = node.name
        if (className != null && !className.endsWith("Enum")) {
            context.report(
                EnumNameIssue,
                node,
                context.getLocation(node as UElement),
                EnumNameIssueText
            )
        }
    }

    private fun handleActivityNameRule(node: UClass, context: JavaContext) {
        val className = node.name
        if (className != null && !className.endsWith("Activity")) {
            context.report(
                ActivityNameIssue,
                node,
                context.getLocation(node as UElement),
                ActivityNameIssueText
            )
        }

    }

    private fun handleBroadcastReceiverNameRule(node: UClass, context: JavaContext) {
        val className = node.name
        if (className != null && !className.endsWith("Receiver")) {
            context.report(
                BroadcastReceiverNameIssue,
                node,
                context.getLocation(node as UElement),
                BroadcastReceiverNameIssueText
            )
        }

    }

}