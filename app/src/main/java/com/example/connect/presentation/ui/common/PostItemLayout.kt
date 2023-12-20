package com.example.connect.presentation.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connect.R
import com.example.connect.domain.models.UsersBean

@Composable
fun PostItemLayout(usersDetails: UsersBean) {
    Column {
        PostTopSection(usersDetails)
        DividerLightGrayAlpha40()
        PostBottomSection()
    }
}

@Composable
fun PostTopSection(usersDetails: UsersBean) {
    UserDetailsSection(
        user = usersDetails,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun PostBottomSection() {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { /*TODO*/ }) {
                Image(
                    imageVector = Icons.Outlined.Circle,
                    contentDescription = stringResource(R.string.like_post)
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Image(
                    imageVector = Icons.Outlined.Comment,
                    contentDescription = stringResource(R.string.comment_on_post)
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Image(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = stringResource(R.string.share_post)
                )
            }
        }
        Text(
            text = stringResource(R.string.like_count_likes, 594223),
            modifier = Modifier.padding(horizontal = 16.dp),
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )

        PostCaption()

        SpacerHeight6()
        Text(
            text = stringResource(R.string.view_all_comment_count_comments, 725),
            modifier = Modifier.padding(horizontal = 16.dp),
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = ColorsHelper.gray()
        )
    }
}

@Composable
fun PostCaption() {
    SpacerHeight6()
    val originalText =
        "aryanmishra@1 : Diwana kar raha hai tera roop sunehera, musalsal kar raha hai mujhko ab ye chehra"

    val annotatedString = buildAnnotatedString {
        append(originalText)
        val colonIndex = originalText.indexOf(':')
        addStyle(
            style = SpanStyle(fontWeight = FontWeight.Bold),
            start = 0,
            end = colonIndex
        )
        addStyle(
            style = SpanStyle(textDecoration = TextDecoration.None),
            start = colonIndex + 1,
            end = originalText.length
        )
    }

    Text(
        text = annotatedString,
        modifier = Modifier.padding(horizontal = 16.dp),
        fontSize = 13.sp
    )
}


