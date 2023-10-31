package com.example.connect.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Dropdown(
    expanded: MutableState<Boolean>,
    selectedItem: MutableState<String>,
    itemList: List<String>
) {
    val focusRequester = FocusRequester()

    Column {
        DropdownMenu(
            modifier = Modifier
                .fillMaxWidth(0.9f),
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            itemList.forEach { item ->
                DropdownMenuItem(onClick = {
                    selectedItem.value = item
                    expanded.value = false
                },
                    text = {
                        Text(item)
                    }
                )
            }
        }

        OutlinedIconButton(
            onClick = {
                expanded.value = true; focusRequester.requestFocus()
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row {
                Text(
                    text = selectedItem.value,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                )
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.KeyboardArrowDown),
                    contentDescription = "Dropdown arrow",
                    modifier = Modifier
                        .padding(end = 16.dp)
                )
            }
        }
    }
}