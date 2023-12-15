package com.example.connect.presentation.ui.home.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connect.R
import com.example.connect.presentation.ui.common.ColorsHelper
import com.example.connect.presentation.ui.common.SpacerHeight12
import com.example.connect.presentation.ui.common.SpacerHeight24
import com.example.connect.presentation.ui.common.SpacerHeight6
import com.example.connect.presentation.ui.common.SpacerWidth12
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination

@HomeNavGraph
@Destination
@Composable
fun SearchUserScreen() {
    Column {
        SpacerHeight12()
        FriendsTitleBar()
        SpacerHeight6()
        FriendsTabs()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsTitleBar() {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text(text = "Friends", fontSize = 22.sp, fontWeight = FontWeight.Bold)
//        SearchBar(
//            modifier = Modifier
//                .wrapContentHeight()
//                .fillMaxWidth()
//                .background(Color.Red)
//                .clip(RoundedCornerShape(16.dp)),
//            query = "Search Friends",
//            onQueryChange = {},
//            onSearch = {},
//            active = true,
//            onActiveChange = {},
//            leadingIcon = {
//                Image(
//                    imageVector = Icons.Default.Search,
//                    contentDescription = "Search Icon"
//                )
//            }
//        ) {
//
//        }
        var text by rememberSaveable { mutableStateOf("") }
        var active by rememberSaveable { mutableStateOf(false) }

        SearchBar(
            modifier = Modifier
                .wrapContentSize()
                .heightIn(0.dp, 100.dp),
            query = text,
            onQueryChange = { text = it },
            onSearch = { active = false },
            active = active,
            onActiveChange = { active = it },
            placeholder = { Text("Hinted search text", color = Color.Gray, fontSize = 15.sp) },
            leadingIcon = {
                Icon(
                    if (active) Icons.Default.ArrowBack else Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null) }
        ) {
            // Add search query suggestions here
        }
        Image(imageVector = Icons.Default.Search, contentDescription = "Search Friends Icon")
    }
}

@Composable
fun FriendsTabs() {
    val itemList = stringArrayResource(id = R.array.friends_tab_list)
    var selectedTabIndexState by remember { mutableIntStateOf(0) }
    TabRow(selectedTabIndex = selectedTabIndexState) {
        itemList.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
                selected = selectedTabIndexState == index,
                onClick = { selectedTabIndexState = index },
                unselectedContentColor = ColorsHelper.gray()
            )
        }
    }
    when (selectedTabIndexState) {
        0 -> FriendsList()
        1 -> FriendRequestsList()
        2 -> SentRequestsList()
    }
}

@Composable
fun FriendsList() {
    val friendList = listOf(
        "Bhupendra Jogi",
        "Bhupendra Jogi",
        "Bhupendra Jogi",
        "Bhupendra Jogi",
        "Bhupendra Jogi"
    )
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        item {
            SpacerHeight12()
            TextGray14(text = "You have ${friendList.size} Friends")
            SpacerHeight16()
        }
        items(5) { index ->
            FriendsItem(friendList[index])
            SpacerHeight24()
        }
    }
}

@Composable
fun TextGray14(text: String) {
    Text(text = text, color = Color.Gray, fontSize = 14.sp)
}

@Composable
fun SpacerHeight16() {
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun FriendsItem(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PosterDetails(
            userName = userName,
            description = "USA mein mai bahot jagah gaya hu",
            onClick = {})
        Image(imageVector = Icons.Default.MoreVert, contentDescription = "Menu Button")
    }
}

@Composable
fun FriendRequestsList() {
    val friendRequestsList = listOf(
        "Bhupendra Jogi",
        "Bhupendra Jogi",
        "Bhupendra Jogi",
        "Bhupendra Jogi",
        "Bhupendra Jogi"
    )
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        item {
            SpacerHeight12()
            TextGray14(text = "You have ${friendRequestsList.size} Pending Requests")
            SpacerHeight16()
        }
        items(5) { index ->
            PosterDetails(
                userName = friendRequestsList[index],
                description = "USA mein mai bahot jagah gaya hu",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                isCTAVisible = true,
                positiveButtonText = "Accept",
                negativeButtonText = "Remove",
                onPositiveButtonClick = {},
                onNegativeButtonClick = {}
            )
            SpacerHeight24()
        }
    }
}

@Composable
fun SentRequestsList() {
    val friendRequestedList = listOf(
        "Bhupendra Jogi",
        "Bhupendra Jogi",
        "Bhupendra Jogi",
        "Bhupendra Jogi",
        "Bhupendra Jogi"
    )
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        item {
            SpacerHeight12()
            TextGray14(text = "You have sent ${friendRequestedList.size} Requests which are Pending")
            SpacerHeight16()
        }
        items(5) { index ->
            FriendsItem(friendRequestedList[index])
            SpacerHeight24()
        }
    }
}

@Composable
fun PosterDetails(
    modifier: Modifier = Modifier,
    userName: String,
    description: String,
    onClick: () -> Unit,
    isCTAVisible: Boolean = false,
    positiveButtonText: String = stringResource(id = R.string.ok),
    negativeButtonText: String = stringResource(id = R.string.cancel),
    onPositiveButtonClick: () -> Unit = {},
    onNegativeButtonClick: () -> Unit = {}
) {
    Row(modifier = modifier.clickable { onClick() }) {

        Image(
            modifier = Modifier
                .size(52.dp)
                .clip(shape = CircleShape),
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = description,
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .wrapContentSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = userName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(text = description, fontSize = 12.sp, color = Color.DarkGray)
            if (isCTAVisible) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(vertical = 4.dp)
                            .clickable {
                                onPositiveButtonClick()
                            }
                            .weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        text = positiveButtonText,
                        fontSize = 14.sp)
                    SpacerWidth12()
                    Text(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.LightGray)
                            .padding(vertical = 4.dp)
                            .clickable {
                                onNegativeButtonClick()
                            }
                            .weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        text = negativeButtonText,
                        fontSize = 14.sp)
                }
            }
        }
    }
}