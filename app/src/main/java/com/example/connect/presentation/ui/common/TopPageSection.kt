package com.example.connect.presentation.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connect.R
import com.example.connect.presentation.ui.theme.ConnectTheme

@Composable
fun TopPageSection(
    headingText: String,
    subHeadingText: String,
    sectionHeadingText: String,
    sectionSubHeadingText: AnnotatedString? = null
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            TextBoldOnPrimary28(text = headingText)
            SpacerHeight6()
            TextBoldOnPrimary28(text = subHeadingText)
        }
        SpacerHeight18()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextBold18(
                text = sectionHeadingText,
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
            )
            if (sectionSubHeadingText != null) {
                SpacerHeight18()
                Text(
                    text = sectionSubHeadingText,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp
                )
            }
            SpacerHeight24()
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun PreviewTopPageSection() {
    ConnectTheme {
        TopPageSection(
            stringResource(R.string.welcome),
            stringResource(R.string.let_s_connect),
            stringResource(R.string.log_in),
            buildAnnotatedString {
                append(
                    stringResource(R.string.an_otp_will_be_send_to_the_below_entered_mobile_number)
                )
            }
        )
    }
}