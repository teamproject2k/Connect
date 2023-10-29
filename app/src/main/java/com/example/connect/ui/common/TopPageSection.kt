package com.example.connect.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.connect.R
import com.example.connect.ui.theme.ConnectTheme

@Composable
fun TopPageSection(headingText: String, subHeadingText: String, sectionHeadingText: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
            TextBlack18(
                text = sectionHeadingText,
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
            )
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
            stringResource(R.string.log_in)
        )
    }
}