package com.example.connect.presentation.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.models.ChatBean
import com.example.connect.presentation.ui.enums.MediaTypeEnum

@Composable
fun RepliedOnUI(
    modifier: Modifier = Modifier,
    message: ChatBean,
    loggedInUserFirebaseId: String,
    otherUserName: String,
    senderNameColor: Color = MaterialTheme.colorScheme.primary,
    dividerColor: Color = MaterialTheme.colorScheme.primary,
    messageColor: Color = Color.Unspecified,
    repliedOnUiBackgroundColor: Color = ColorsHelper.chatBubbleOtherUserBg(),
    showCancelIconButton: Boolean = true,
    onCancelIconButtonClicked: () -> Unit = {}
) {
    ConstraintLayout(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(repliedOnUiBackgroundColor)
    ) {
        val (verticalDivider, topSection, messageText) = createRefs()
        Box(
            modifier = Modifier
                .constrainAs(verticalDivider) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    width = Dimension.value(4.dp)
                    height = Dimension.fillToConstraints
                }
                .background(dividerColor)
        )
        Row(
            modifier = Modifier
                .padding(end = 8.dp)
                .constrainAs(topSection) {
                    top.linkTo(parent.top, margin = 4.dp)
                    bottom.linkTo(messageText.top)
                    start.linkTo(verticalDivider.end, margin = 8.dp)
                    width = Dimension.preferredWrapContent
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (message.senderId == loggedInUserFirebaseId) stringResource(R.string.you) else otherUserName,
                modifier = if (showCancelIconButton) Modifier.weight(1f) else Modifier,
                color = senderNameColor,
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            if (showCancelIconButton) {
                IconButton(
                    onClick = {
                        onCancelIconButtonClicked()
                    },
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = stringResource(id = R.string.clear)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .padding(end = 8.dp, top = if (message.mediaUrl.isNotBlank()) 2.dp else 0.dp)
                .constrainAs(messageText) {
                    top.linkTo(topSection.bottom, margin = 4.dp)
                    bottom.linkTo(parent.bottom, margin = 4.dp)
                    start.linkTo(verticalDivider.end, margin = 8.dp)
                    width = Dimension.preferredWrapContent
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (message.mediaUrl.isNotBlank()) {
                AsyncImage(
                    model = message.mediaUrl,
                    contentDescription = stringResource(id = R.string.image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                SpacerWidth6()
            }
            Text(
                text = message.message.ifBlank {
                    message.mediaType.replace(
                        MediaTypeEnum.Text.name,
                        ""
                    )
                },
                fontSize = 13.sp,
                maxLines = 3,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
                color = messageColor,
                lineHeight = 20.sp
            )
        }

    }
}
