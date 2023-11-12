package com.example.connect.presentation.ui.home.user_profile

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.common.ErrorCodes
import com.example.connect.common.LoggingHelper
import com.example.connect.common.LoggingLevelEnum
import com.example.connect.common.RequestStatusEnum
import com.example.connect.data.local_db.posts.PostDetails
import com.example.connect.data.local_db.users.UserDetails
import com.example.connect.presentation.ui.auth.AuthenticationActivity
import com.example.connect.presentation.ui.common.LoaderFullScreen
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.SpacerHeight12
import com.example.connect.presentation.ui.common.SpacerHeight24
import com.example.connect.presentation.ui.common.SpacerHeight6
import com.example.connect.presentation.ui.common.SpacerHeight8
import com.example.connect.presentation.ui.common.SpacerWidth12
import com.example.connect.presentation.ui.common.SpacerWidth6
import com.example.connect.presentation.ui.common.SpacerWidth8
import com.example.connect.presentation.ui.common.TextBold18
import com.example.connect.presentation.ui.common.getHeightToMaintainAspectRatio
import com.example.connect.presentation.ui.common.getWidthToMaintainAspectRatio
import com.example.connect.presentation.ui.common.shimmer
import com.example.connect.presentation.ui.theme.OnBlack
import com.example.connect.presentation.ui.theme.WarningColor
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.showToast
import com.example.connect.presentation.utils.HomeNavGraph
import com.example.connect.presentation.utils.enums.PostTypeEnum
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@HomeNavGraph(start = true)
@Destination
@Composable
fun UserProfileScreen() {
    val viewModel: UserProfileViewModel = hiltViewModel()
    val snackBarHostState = SnackbarHostState()
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState =
        SheetState(skipPartiallyExpanded = true, initialValue = SheetValue.Hidden)
    val scaffoldState = BottomSheetScaffoldState(bottomSheetState, snackBarHostState)
    BottomSheetScaffold(sheetContent = {
        BottomSheetSection(viewModel, bottomSheetState)
    }, scaffoldState = scaffoldState, sheetShape = RoundedCornerShape(32.dp)) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            HandleUserDetails(viewModel = viewModel) {
                if (bottomSheetState.isVisible) {
                    coroutineScope.launch {
                        bottomSheetState.hide()
                    }
                } else {
                    coroutineScope.launch {
                        bottomSheetState.show()
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = viewModel.snackBarMessageState.value) {
        if (viewModel.snackBarMessageState.value.isNotBlank()) {
            coroutineScope.launch {
                snackBarHostState.showSnackbar(viewModel.snackBarMessageState.value)
                viewModel.snackBarMessageState.value = ""
            }
        }
    }
    LaunchedEffect(key1 = true) {
        viewModel.getUserDetails()
        viewModel.getPostDetails()
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetSection(viewModel: UserProfileViewModel, bottomSheetState: SheetState) {
    val context = LocalContext.current
    val currentActivity = LocalActivity.current
    val coroutineScope = rememberCoroutineScope()
    var showLogoutDialog by remember {
        mutableStateOf(false)
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        BottomSheetItem(imageVector = Icons.Default.Settings, text = "Settings") {
            // TODO: navigate to settings screen
        }
        BottomSheetItem(
            imageVector = Icons.Default.ExitToApp,
            text = stringResource(id = R.string.logout)
        ) {
            coroutineScope.launch {
                bottomSheetState.hide()
            }
            showLogoutDialog = true
        }
    }
    if (showLogoutDialog) {
        LogoutAlertDialog(onDismiss = { showLogoutDialog = false }) {
            viewModel.logout()
            val intent = Intent(context, AuthenticationActivity::class.java)
            context.startActivity(intent)
            currentActivity.finish()
            showLogoutDialog = false
        }
    }
}


@Composable
fun LogoutAlertDialog(onDismiss: () -> Unit, onOk: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Text(text = stringResource(id = R.string.ok), modifier = Modifier.clickable {
                onOk()
            })
        },
        dismissButton = {
            Text(
                text = stringResource(id = R.string.cancel),
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clickable {
                        onDismiss()
                    }
            )
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(R.string.warning),
                    colorFilter = ColorFilter.tint(WarningColor)
                )
                SpacerWidth12()
                TextBold18(text = stringResource(R.string.logout))
            }
        },
        text = {
            Text(text = stringResource(R.string.do_you_really_want_to_logout_from_the_app))
        }
    )
}

@Composable
fun BottomSheetItem(imageVector: ImageVector, text: String, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(imageVector = imageVector, contentDescription = text)
            SpacerWidth12()
            Text(text = text)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0x33CCCCCC))
        )
    }
}

@Composable
fun HandleUserDetails(viewModel: UserProfileViewModel, onOptionsMenuClick: () -> Unit) {
    val userDetailsState = viewModel.userDetailsStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    when (userDetailsState.status) {
        RequestStatusEnum.LOADING -> {
            LoaderFullScreen(loadingText = stringResource(R.string.getting_details))
            if (isExceptionHandled) {
                isExceptionHandled = false
            }
        }

        RequestStatusEnum.SUCCESS -> {
            ProfileScreen(userDetailsState.data!!, viewModel, onOptionsMenuClick)
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isExceptionHandled) {
                if (userDetailsState.message == ErrorCodes.NoUserFound) {
                    viewModel.sharedPreference.isUserDetailsEntered = false
                    context.showToast(stringResource(R.string.no_user_found_please_reenter_details))
                    val intent = Intent(context, AuthenticationActivity::class.java)
                    context.startActivity(intent)
                    LocalActivity.current.finish()
                } else {
                    viewModel.snackBarMessageState.value =
                        userDetailsState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                isExceptionHandled = true
            }
            LoggingHelper.logData(
                LoggingLevelEnum.Error,
                ConstantsHelper.ErrorTag,
                "UserProfileScreen",
                userDetailsState.message.toString()
            )
        }

        RequestStatusEnum.NONE -> {
            //no need to handle it
        }
    }
}


@Composable
fun ProfileScreen(
    userDetails: UserDetails,
    viewModel: UserProfileViewModel,
    onOptionsMenuClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImageSection(userDetails, onOptionsMenuClick)
        SpacerHeight12()
        UserDetailsSection(userDetails)
        SpacerHeight24()
        HandleFriendListSection(viewModel = viewModel)
        SpacerHeight24()
        HandlePostSection(viewModel)
    }
}


@Composable
fun ImageSection(userDetails: UserDetails, onOptionsMenuClick: () -> Unit) {
    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (
            coverImageRef, profileImageRef, editImageRef, moreOptionsRef
        ) = createRefs()
        AsyncImage(
            model = userDetails.coverPhoto,
            contentDescription = stringResource(R.string.cover_photo),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray)
                .constrainAs(coverImageRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            contentScale = ContentScale.Crop,
        )
        AsyncImage(
            model = userDetails.profilePhoto,
            contentDescription = stringResource(R.string.profile_image),
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .border(4.dp, Color.White, CircleShape)
                .constrainAs(profileImageRef) {
                    start.linkTo(parent.start, 16.dp)
                    top.linkTo(coverImageRef.bottom)
                    bottom.linkTo(coverImageRef.bottom)
                },
            error = painterResource(id = R.drawable.ic_default_user),
            placeholder = painterResource(id = R.drawable.ic_default_user)
        )

        IconButton(onClick = {
            onOptionsMenuClick()
        },
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier.constrainAs(moreOptionsRef) {
                top.linkTo(coverImageRef.top, 16.dp)
                end.linkTo(coverImageRef.end, 16.dp)
            }
        ) {
            Image(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.more_options)
            )
        }

        IconButton(
            onClick = {
                // TODO: navigate user to user profile edit screen
            },
            modifier = Modifier.constrainAs(editImageRef) {
                top.linkTo(coverImageRef.bottom, 16.dp)
                end.linkTo(parent.end, 16.dp)
            },
        ) {
            Image(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.edit_profile)
            )
        }
    }
}


@Composable
fun UserDetailsSection(userDetails: UserDetails) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextBold18(text = FunctionHelper.getFormattedDisplayName(userDetails.name))
        SpacerHeight6()
        Text(text = userDetails.bio, fontSize = 12.sp)
        SpacerHeight12()
        ImageTextItem(
            imageVector = Icons.Default.Person,
            text = userDetails.connectUserId,
            FontWeight.Medium
        )
        SpacerHeight8()
        ImageTextItem(
            Icons.Default.DateRange,
            FunctionHelper.getFormattedDate(userDetails.dateOfBirth)
        )
        SpacerHeight8()
        ImageTextItem(imageVector = Icons.Default.Face, text = userDetails.gender)
    }

}


@Composable
fun ImageTextItem(imageVector: ImageVector, text: String, fontWeight: FontWeight? = null) {
    Row(horizontalArrangement = Arrangement.Center) {
        Image(
            imageVector = imageVector,
            contentDescription = text,
            modifier = Modifier.size(16.dp)
        )
        SpacerWidth6()
        Text(text = text, fontSize = 12.sp, fontWeight = fontWeight)

    }
}

@Composable
fun HandleFriendListSection(viewModel: UserProfileViewModel) {
    val friendsDetailsState = viewModel.friendsDetailsStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    when (friendsDetailsState.status) {
        RequestStatusEnum.LOADING -> {
            FriendsListLoading()
            if (isExceptionHandled) {
                isExceptionHandled = false
            }
        }

        RequestStatusEnum.SUCCESS -> {
            FriendsListSection(friendsList = friendsDetailsState.data!!)
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isExceptionHandled) {
                viewModel.snackBarMessageState.value =
                    friendsDetailsState.message
                        ?: stringResource(id = R.string.something_went_wrong)
                isExceptionHandled = true
            }
            LoggingHelper.logData(
                LoggingLevelEnum.Error,
                ConstantsHelper.ErrorTag,
                "UserProfileScreen",
                friendsDetailsState.message.toString()
            )
        }

        RequestStatusEnum.NONE -> {
            FriendsListSection(listOf())
        }
    }
}

@Composable
fun FriendsListLoading() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextCountSeeAll(
            text = stringResource(id = R.string.friends),
            count = 0,
            showSeeAll = false
        )
        SpacerHeight12()
        Row(modifier = Modifier.fillMaxWidth()) {
            FriendItem(friendDetails = null, modifier = Modifier.weight(1f), showShimmer = true)
            SpacerWidth8()
            FriendItem(friendDetails = null, modifier = Modifier.weight(1f), showShimmer = true)
            SpacerWidth8()
            FriendItem(friendDetails = null, modifier = Modifier.weight(1f), showShimmer = true)
            SpacerWidth8()
            FriendItem(friendDetails = null, modifier = Modifier.weight(1f), showShimmer = true)
        }
    }
}


@Composable
fun FriendsListSection(friendsList: List<UserDetails>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextCountSeeAll(
            text = stringResource(id = R.string.friends),
            count = friendsList.size,
            showSeeAll = friendsList.size > ConstantsHelper.UserProfileFriendColumns
        ) {
            // TODO: navigate to see all screen
        }
        SpacerHeight12()
        if (friendsList.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getHeightToMaintainAspectRatio(
                            horizontalPadding = 16.dp,
                            verticalPadding = 0.dp,
                            itemsRequiredPerRow = 4,
                            itemsHorizontalPadding = 8.dp,
                            noOfRows = 1,
                            itemsVerticalPadding = 0.dp
                        )
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.no_friends_added),
                    fontSize = 14.sp,
                )
                SpacerWidth8()
                Text(text = stringResource(R.string.add_friends),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        // TODO: navigate user to add post screen
                    })
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                val postCount = minOf(friendsList.count(), ConstantsHelper.UserProfileFriendColumns)
                repeat(postCount) { index ->
                    FriendItem(friendDetails = friendsList[index], modifier = Modifier.weight(1f)) {

                    }
                    SpacerWidth8()
                }
                repeat(ConstantsHelper.UserProfileFriendColumns - postCount) {
                    SpacerWidth8()
                    FriendItem(friendDetails = null, modifier = Modifier.weight(1f)) {
                        //no need to handle
                    }
                }
            }
        }
    }
}


@Composable
fun FriendItem(
    friendDetails: UserDetails?,
    showShimmer: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val updatedModifier = if (friendDetails != null) {
        modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    } else {
        modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
    }
    Column(modifier = if (showShimmer) updatedModifier.shimmer() else updatedModifier) {
        if (friendDetails == null || showShimmer) return
        AsyncImage(
            model = friendDetails.profilePhoto,
            contentDescription = friendDetails.name,
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user)
        )
    }

}


@Composable
fun HandlePostSection(viewModel: UserProfileViewModel) {
    val postDetailState = viewModel.postDetailsStateFlow.collectAsState().value
    var isExceptionHandled by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    when (postDetailState.status) {
        RequestStatusEnum.LOADING -> {
            PostLoadingSection()
            if (isExceptionHandled) {
                isExceptionHandled = false
            }
        }

        RequestStatusEnum.SUCCESS -> {
            PostSection(postDetailsList = postDetailState.data!!)
        }

        RequestStatusEnum.EXCEPTION -> {
            if (!isExceptionHandled) {
                if (postDetailState.message == ErrorCodes.NoUserFound) {
                    viewModel.sharedPreference.isUserDetailsEntered = false
                    context.showToast(stringResource(R.string.no_user_found_please_reenter_details))
                    val intent = Intent(context, AuthenticationActivity::class.java)
                    context.startActivity(intent)
                    LocalActivity.current.finish()
                } else {
                    viewModel.snackBarMessageState.value =
                        postDetailState.message
                            ?: stringResource(id = R.string.something_went_wrong)
                }
                isExceptionHandled = true
            }
            LoggingHelper.logData(
                LoggingLevelEnum.Error,
                ConstantsHelper.ErrorTag,
                "UserProfileScreen",
                postDetailState.message.toString()
            )
        }

        RequestStatusEnum.NONE -> {
            //no need to handle it
        }
    }
}


@Composable
fun PostLoadingSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextCountSeeAll(
            text = stringResource(id = R.string.posts),
            count = 0,
            showSeeAll = false
        )
        SpacerHeight12()
        Row(modifier = Modifier.fillMaxWidth()) {
            PostItem(
                postDetails = null, showShimmer = true, modifier = Modifier.size(
                    getWidthToMaintainAspectRatio(
                        horizontalPadding = 16.dp,
                        itemsRequiredPerRow = 3,
                        itemsHorizontalPadding = 8.dp
                    )
                )
            )
            SpacerWidth8()
            PostItem(
                postDetails = null, showShimmer = true, modifier = Modifier.size(
                    getWidthToMaintainAspectRatio(
                        horizontalPadding = 16.dp,
                        itemsRequiredPerRow = 3,
                        itemsHorizontalPadding = 8.dp
                    )
                )
            )
            SpacerWidth8()
            PostItem(
                postDetails = null, showShimmer = true, modifier = Modifier.size(
                    getWidthToMaintainAspectRatio(
                        horizontalPadding = 16.dp,
                        itemsRequiredPerRow = 3,
                        itemsHorizontalPadding = 8.dp
                    )
                )
            )
        }
    }
}

@Composable
fun PostSection(postDetailsList: List<PostDetails>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextCountSeeAll(
            text = stringResource(id = R.string.posts),
            count = postDetailsList.size,
            showSeeAll = false
        )
        SpacerHeight12()
        if (postDetailsList.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getHeightToMaintainAspectRatio(
                            horizontalPadding = 16.dp,
                            verticalPadding = 0.dp,
                            itemsRequiredPerRow = 4,
                            itemsHorizontalPadding = 8.dp,
                            noOfRows = 1,
                            itemsVerticalPadding = 0.dp
                        )
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.no_posts_added),
                    fontSize = 14.sp,
                )
                SpacerWidth8()
                Text(text = stringResource(R.string.add_post),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        // TODO: navigate user to add post screen
                    })
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .height(
                        getHeightToMaintainAspectRatio(
                            horizontalPadding = 16.dp,
                            verticalPadding = 0.dp,
                            itemsRequiredPerRow = 3,
                            itemsHorizontalPadding = 8.dp,
                            noOfRows = ceil(postDetailsList.size.toFloat() / 3).toInt(),
                            itemsVerticalPadding = 8.dp
                        )
                    )
                    .background(Color.Red),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(postDetailsList) { details ->
                    PostItem(
                        postDetails = details,
                        modifier = Modifier.size(
                            getWidthToMaintainAspectRatio(
                                horizontalPadding = 16.dp,
                                itemsRequiredPerRow = 3,
                                itemsHorizontalPadding = 8.dp
                            )
                        ),
                    ) {
                    }
                }
            }
        }
    }
}


@Composable
fun PostItem(
    postDetails: PostDetails?,
    modifier: Modifier = Modifier,
    showShimmer: Boolean = false,
    onClick: () -> Unit = {}
) {
    val updatedModifier = if (postDetails != null) {
        modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            }
    } else {
        modifier
            .clip(RoundedCornerShape(8.dp))
    }
    Box(
        modifier = if (showShimmer) {
            updatedModifier.shimmer()
        } else {
            updatedModifier
        }
    ) {
        if (showShimmer || postDetails == null) return
        if (postDetails.postType == PostTypeEnum.Text.name) {
            PostTextOnlyItem(caption = postDetails.caption)
        } else {
            var isImageLoadingFailed by remember {
                mutableStateOf(false)
            }
            if (!isImageLoadingFailed) {
                AsyncImage(
                    model = postDetails.thumbnailUrl,
                    contentDescription = postDetails.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = {
                        isImageLoadingFailed = true
                    }
                )
            } else {
                PostTextOnlyItem(caption = postDetails.caption.ifBlank { stringResource(R.string.unable_to_load_post) })
            }
            if (postDetails.postType.contains(PostTypeEnum.Video.name)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OnBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = stringResource(R.string.play_video),
                    )
                }
            }
        }
    }
}


@Composable
fun PostTextOnlyItem(caption: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(8.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = caption,
            fontSize = 12.sp,
            color = Color.White,
            lineHeight = 12.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TextCountSeeAll(
    text: String,
    count: Int,
    areBracketsVisible: Boolean = true,
    showSeeAll: Boolean = true,
    onSeeAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextCountItem(text = text, count = count, areBracketsVisible)
        if (showSeeAll) {
            Text(
                text = stringResource(R.string.see_all),
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    onSeeAllClick()
                }
            )
        }
    }
}


@Composable
fun TextCountItem(text: String, count: Int, isCountSurroundedByBracket: Boolean = true) {
    Text(text = buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(text)
        }
        append(" ")
        withStyle(SpanStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp)) {
            val countText = if (isCountSurroundedByBracket) "($count)" else count.toString()
            append(countText)
        }
    }, textAlign = TextAlign.Center)
}

@Preview
@Composable
fun PreviewUserDetailsScreen() {
    UserProfileScreen()
}