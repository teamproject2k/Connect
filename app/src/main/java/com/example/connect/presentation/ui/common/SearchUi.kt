package com.example.connect.presentation.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.connect.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUi(onSearchQueryChange: (String) -> Unit) {
    var searchString by rememberSaveable {
        mutableStateOf("")
    }
    SearchBar(query = searchString,
        onQueryChange = {
            searchString = it
            onSearchQueryChange(it)
        },
        onSearch = {
            onSearchQueryChange(it)
        },
        active = false,
        onActiveChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = {
            Text(
                text = stringResource(R.string.search_user_by_name_or_user_id),
                color = ColorsHelper.gray()
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search, contentDescription = stringResource(
                    id = R.string.search
                )
            )
        }, trailingIcon = {
            if (searchString.isNotBlank()) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(
                        R.string.clear
                    ),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            searchString = ""
                            onSearchQueryChange("")
                        }
                )
            }
        }) {

    }
}