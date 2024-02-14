package com.teamproject2k.connect.presentation.ui.common

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.teamproject2k.connect.R
import com.teamproject2k.connect.presentation.utils.ConstantsHelper.MINIMIZED_MAX_LINES

@Composable
fun ExpandingText(
    modifier: Modifier = Modifier,
    text: String,
    context: Context,
    fontSize: TextUnit = 14.sp,
    minimizedMaxLines: Int = MINIMIZED_MAX_LINES
) {
    var isExpanded by remember { mutableStateOf(false) }
    val textLayoutResultState = remember { mutableStateOf<TextLayoutResult?>(null) }
    var isClickable by remember { mutableStateOf(false) }
    var finalText by remember { mutableStateOf(AnnotatedString(text)) }

    val textLayoutResult = textLayoutResultState.value
    LaunchedEffect(textLayoutResult) {
        if (textLayoutResult == null) return@LaunchedEffect
        when {
            isExpanded -> {
                finalText = buildAnnotatedString {
                    append("$text ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp)) {
                        append(context.getString(R.string.show_less))
                    }
                }
            }

            !isExpanded && textLayoutResult.hasVisualOverflow -> {
                val lastCharIndex = textLayoutResult.getLineEnd(minimizedMaxLines - 1)
                val showMoreString = context.getString(R.string.show_more)
                val adjustedText = text
                    .substring(startIndex = 0, endIndex = lastCharIndex)
                    .dropLast(showMoreString.length)
                    .dropLastWhile { it == ' ' || it == '.' }

                finalText = buildAnnotatedString {
                    append("$adjustedText ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp)) {
                        append(context.getString(R.string.show_more))
                    }
                }

                isClickable = true
            }
        }
    }

    Text(
        text = finalText,
        maxLines = if (isExpanded) Int.MAX_VALUE else minimizedMaxLines,
        onTextLayout = { textLayoutResultState.value = it },
        modifier = modifier
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null,
                enabled = isClickable
            ) {
                isExpanded = !isExpanded
            }
            .animateContentSize(),
        fontSize = fontSize,
    )
}