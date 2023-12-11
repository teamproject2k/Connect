package com.example.connect.presentation.ui.home.edit_profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.ui.common.AppOutlinedTextField
import com.example.connect.presentation.ui.common.LoaderButton
import com.example.connect.presentation.ui.common.OutlinedTextFieldDisabledFeelsLikeEnabled
import com.example.connect.presentation.ui.common.SpacerHeight24
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.HomeNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalComposeUiApi::class)
@HomeNavGraph
@Destination
@Composable
fun EditProfileScreen(
    userDetails: UsersBean,
    navigator: DestinationsNavigator
) {

    val viewModel: EditProfileViewModel = hiltViewModel()
    if (!viewModel.isDataInitialized) {
        viewModel.initializeStates(userDetails)
    }

    val context = LocalContext.current
    HandleUpdateUserState(viewModel, context, navigator)

    val imageResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            //uri will be null in case user doesn't select any image
//            if (uri != null) {
//                if (viewModel.isProfileUri) {
//                    viewModel.profilePhotoState.value =
//                        PostMediaData(uri, ConstantsHelper.MediaTypeImage)
//                } else {
//                    viewModel.coverPhotoState.value =
//                        PostMediaData(uri, ConstantsHelper.MediaTypeImage)
//                }
//            }
        }

    val keyboardController = LocalSoftwareKeyboardController.current
    val snackBarHostState = SnackbarHostState()
    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }) {
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            EditProfileImageSection(viewModel, imageResultLauncher)
            SpacerHeight24()
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                EditProfileNameInputTextField(viewModel)
                SpacerHeight24()
                BioInputTextField(viewModel)
                SpacerHeight24()
                EditProfileGenderPicker(viewModel)
                SpacerHeight24()
                EditProfileDOBPicker(viewModel)
                SpacerHeight24()

                LoaderButton(
                    loaderButtonState = viewModel.currentButtonLoadingState,
                    loadingText = stringResource(R.string.updating_account),
                    buttonText = stringResource(id = R.string.update_account),
                    onClick = {
                        keyboardController?.hide()
                        handleButtonClick(
                            viewModel,
                            context,
                            navigator
                        )
                    }
                )
            }
            LaunchedEffect(key1 = viewModel.snackBarMessageState.value) {
                if (viewModel.snackBarMessageState.value.isNotBlank()) {
                    snackBarHostState.showSnackbar(
                        viewModel.snackBarMessageState.value,
                        duration = SnackbarDuration.Short
                    )
                    viewModel.snackBarMessageState.value = ""
                }
            }
        }
    }
}

@Composable
fun EditProfileImageSection(
    viewModel: EditProfileViewModel,
    imageResultLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>
) {

    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (
            coverImageRef, profileImageRef, editCoverRef, editProfileRef, connectIdRef
        ) = createRefs()
        AsyncImage(
            model = viewModel.coverPhotoState.value?.uri,
            contentDescription = stringResource(R.string.cover_photo),
            modifier = Modifier
                .fillMaxWidth()
                .height(ConstantsHelper.CoverImageHeight)
                .background(Color.LightGray)
                .constrainAs(coverImageRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            contentScale = ContentScale.Crop
        )
        AsyncImage(
            model = viewModel.profilePhotoState.value?.uri,
            contentDescription = stringResource(R.string.profile_image),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(ConstantsHelper.ProfileImageHeight)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .border(4.dp, Color.White, CircleShape)
                .constrainAs(profileImageRef) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(coverImageRef.bottom)
                    bottom.linkTo(coverImageRef.bottom)
                },
            error = painterResource(id = R.drawable.ic_default_user),
            placeholder = painterResource(id = R.drawable.ic_default_user)
        )

        // Update Cover Image
        IconButton(onClick = {
            viewModel.isProfileUri = false
            imageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier.constrainAs(editCoverRef) {
                top.linkTo(coverImageRef.top, 16.dp)
                end.linkTo(coverImageRef.end, 16.dp)
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = stringResource(R.string.capture_image)
            )
        }

        // Update Profile Image
        IconButton(onClick = {
            viewModel.isProfileUri = true
            imageResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier.constrainAs(editProfileRef) {
                bottom.linkTo(profileImageRef.bottom, 8.dp)
                end.linkTo(profileImageRef.end)
            }
        ) {
            Image(
                painterResource(id = R.drawable.ic_camera),
                contentDescription = stringResource(R.string.capture_image)
            )
        }

        Text(
            text = viewModel.connectUserIdState.value,
            modifier = Modifier.constrainAs(connectIdRef) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(profileImageRef.bottom, 2.dp)
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditProfileNameInputTextField(viewModel: EditProfileViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    AppOutlinedTextField(
        value = FunctionHelper.getFormattedDisplayName(viewModel.userNameState.value),
        onValueChange = { updatedValue ->
            if (updatedValue.length <= ConstantsHelper.NameMaxCharacterLimit) {
                viewModel.userNameState.value = updatedValue
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.name_can_t_contain_digits_or_special_characters)
                FunctionHelper.vibrateDevice(context)
                keyboardController?.hide()
            }
        },
        label = {
            Text(text = stringResource(R.string.full_name))
        },
        leadingIcon = {
            Image(
                imageVector = Icons.Rounded.Face,
                contentDescription = stringResource(id = R.string.full_name)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = true
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BioInputTextField(viewModel: EditProfileViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    AppOutlinedTextField(
        value = viewModel.userBioState.value,
        onValueChange = { updatedValue ->
            if (updatedValue.length <= ConstantsHelper.BioMaxCharacterLimit) {
                viewModel.userBioState.value = updatedValue
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(
                        R.string.bio_can_t_be_more_than_limited_characters,
                        ConstantsHelper.BioMaxCharacterLimit
                    )
                FunctionHelper.vibrateDevice(context)
                keyboardController?.hide()
            }
        },
        label = {
            Text(text = stringResource(R.string.bio))
        },
        leadingIcon = {
            Image(
                painter = painterResource(id = R.drawable.ic_bio),
                contentDescription = stringResource(id = R.string.bio)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        maxLines = 5
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileGenderPicker(viewModel: EditProfileViewModel) {
    val genderList = stringArrayResource(id = R.array.gender_list)
    var isDialogVisible by remember {
        mutableStateOf(false)
    }
    OutlinedTextFieldDisabledFeelsLikeEnabled(
        value = viewModel.selectedGenderState.value,
        modifier = Modifier
            .fillMaxWidth(),
        leadingIcon = {
            Image(
                imageVector = Icons.Rounded.Person,
                contentDescription = stringResource(id = R.string.gender)
            )
        },
        label = { Text(text = stringResource(R.string.gender)) },
        trailingIcon = {
            Image(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = stringResource(id = R.string.gender),
                modifier = Modifier.scale(if (isDialogVisible) -1f else 1f)
            )
        },
        showEnabledByDefault = false
    ) {
        isDialogVisible = true
    }
    if (isDialogVisible) {
        DropdownMenu(
            expanded = true, onDismissRequest = { isDialogVisible = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth(.9f)
        ) {
            genderList.forEach { gender ->
                DropdownMenuItem(text = { Text(text = gender) }, onClick = {
                    viewModel.selectedGenderState.value = gender
                    isDialogVisible = false
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDOBPicker(viewModel: EditProfileViewModel) {
    var showDatePickerState by remember {
        mutableStateOf(false)
    }
    val dateSelectionState = rememberDatePickerState(initialDisplayMode = DisplayMode.Input)
    OutlinedTextFieldDisabledFeelsLikeEnabled(
        value = if (viewModel.selectedDOBState.longValue != -1L) FunctionHelper.getFormattedDate(
            viewModel.selectedDOBState.longValue
        ) else "",
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Image(
                imageVector = Icons.Rounded.DateRange,
                contentDescription = stringResource(id = R.string.date_of_birth)
            )
        },
        label = { Text(text = stringResource(R.string.date_of_birth)) },
        showEnabledByDefault = false
    ) {
        showDatePickerState = true
    }
    if (showDatePickerState) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerState = false },
            confirmButton = {
                Text(text = stringResource(R.string.ok), modifier = Modifier
                    .padding(end = 16.dp, bottom = 16.dp)
                    .clickable {
                        if (dateSelectionState.selectedDateMillis != null) {
                            viewModel.selectedDOBState.longValue =
                                dateSelectionState.selectedDateMillis!!
                        }
                        showDatePickerState = false
                    })
            },
            dismissButton = {
                Text(text = stringResource(R.string.cancel), modifier = Modifier
                    .padding(bottom = 16.dp, end = 16.dp)
                    .clickable {
                        showDatePickerState = false
                    })
            }
        ) {
            DatePicker(state = dateSelectionState, showModeToggle = true, dateValidator = {
                val calender = Calendar.getInstance()
                val enteredDate = Date(it)
                enteredDate.before(calender.time)
            })
        }
    }
}

private fun handleButtonClick(
    viewModel: EditProfileViewModel,
    context: Context,
    navigator: DestinationsNavigator
) {
//    if (!Validator.isValidName(viewModel.userNameState.value)) {
//        viewModel.snackBarMessageState.value = context.getString(R.string.please_enter_name)
//        FunctionHelper.vibrateDevice(context)
//    } else if (!Validator.isValidBio(viewModel.userBioState.value)) {
//        viewModel.snackBarMessageState.value = context.getString(R.string.please_enter_valid_bio)
//        FunctionHelper.vibrateDevice(context)
//    } else if (!Validator.isValidGender(viewModel.selectedGenderState.value, context)) {
//        viewModel.snackBarMessageState.value = context.getString(R.string.please_select_your_gender)
//        FunctionHelper.vibrateDevice(context)
//    } else if (!Validator.isValidDob(viewModel.selectedDOBState.longValue)) {
//        viewModel.snackBarMessageState.value = context.getString(R.string.please_select_your_date_of_birth)
//        FunctionHelper.vibrateDevice(context)
//    } else {
//        if (viewModel.fireBaseAuth.currentUser != null) {
//            if (context.isNetworkAvailable()) {
//                viewModel.updateUserProfile()
//            } else {
//                viewModel.snackBarMessageState.value =
//                    context.getString(R.string.no_internet_connection)
//            }
//        } else {
//            context.showToast(context.getString(R.string.some_error_occurred_please_login_again))
//            navigator.popBackStack()
//        }
//    }
}

@Composable
fun HandleUpdateUserState(
    viewModel: EditProfileViewModel,
    context: Context,
    navigator: DestinationsNavigator
) {

//    val uiState = viewModel.updateUserStateFlow.collectAsState().value
//
//    when (uiState.status) {
//        RequestStatusEnum.LOADING -> {
//            viewModel.currentButtonLoadingState.value = ButtonLoadingEnum.Loading
//        }
//
//        RequestStatusEnum.SUCCESS -> {
//            viewModel.currentButtonLoadingState.value = ButtonLoadingEnum.NotLoading
//            context.showToast(stringResource(R.string.user_details_updated_successfully))
//            navigator.popBackStack()
//        }
//
//        RequestStatusEnum.EXCEPTION -> {
//            viewModel.snackBarMessageState.value =
//                if (uiState.message.isNullOrBlank()) context.getString(R.string.something_went_wrong)
//                else uiState.message.toString()
//            viewModel.currentButtonLoadingState.value = ButtonLoadingEnum.NotLoading
//            LoggingHelper.logData(
//                LoggingLevelEnum.Error,
//                ConstantsHelper.ErrorTag,
//                "EditProfileScreen",
//                uiState.message.toString()
//            )
//        }
//
//        else -> {}
//    }
}
