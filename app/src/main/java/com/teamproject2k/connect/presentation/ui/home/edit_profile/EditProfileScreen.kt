package com.teamproject2k.connect.presentation.ui.home.edit_profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.logger.LoggingHelper
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum
import com.teamproject2k.connect.domain.network_utils.RequestStatusEnum
import com.teamproject2k.connect.presentation.ui.common.AppOutlinedTextField
import com.teamproject2k.connect.presentation.ui.common.ColorsHelper
import com.teamproject2k.connect.presentation.ui.common.LoaderButton
import com.teamproject2k.connect.presentation.ui.common.LocalActivity
import com.teamproject2k.connect.presentation.ui.common.OutlinedTextFieldDisabledFeelsLikeEnabled
import com.teamproject2k.connect.presentation.ui.common.SpacerHeight24
import com.teamproject2k.connect.presentation.ui.common.mediaPicker
import com.teamproject2k.connect.presentation.ui.enums.ButtonStateEnum
import com.teamproject2k.connect.presentation.ui.enums.ScreenNameEnum
import com.teamproject2k.connect.presentation.ui.home.base_screen.HomeSharedViewModel
import com.teamproject2k.connect.presentation.ui.models.MediaData
import com.teamproject2k.connect.presentation.utils.ConstantsHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper
import com.teamproject2k.connect.presentation.utils.FunctionHelper.isNetworkAvailable
import com.teamproject2k.connect.presentation.utils.FunctionHelper.showToast
import com.teamproject2k.connect.presentation.utils.HomeNavGraph
import com.teamproject2k.connect.presentation.validation.Validator
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalComposeUiApi::class)
@HomeNavGraph
@Destination
@Composable
fun EditProfileScreen(
    navigator: DestinationsNavigator
) {
    val sharedViewModel: HomeSharedViewModel = hiltViewModel(LocalActivity.current)
    val viewModel: EditProfileViewModel = hiltViewModel()
    if (!viewModel.isDataInitialized) {
        viewModel.initializeData(sharedViewModel.usersDetails)
    }
    val context = LocalContext.current
    val imageResultLauncher =
        mediaPicker { uri ->
            if (viewModel.isProfileUri) {
                viewModel.profilePhotoState.value =
                    MediaData(uri, ConstantsHelper.MEDIA_TYPE_IMAGE)
            } else {
                viewModel.coverPhotoState.value =
                    MediaData(uri, ConstantsHelper.MEDIA_TYPE_IMAGE)
            }
        }

    val keyboardController = LocalSoftwareKeyboardController.current
    val snackBarHostState = remember { SnackbarHostState() }
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
                Row(modifier = Modifier.fillMaxWidth()) {
                    EditProfileGenderPicker(viewModel)
                }
                SpacerHeight24()
                EditProfileDOBPicker(viewModel)
                SpacerHeight24()
                LoaderButton(
                    isEnabled = isButtonEnabled(viewModel),
                    loaderButtonState = viewModel.currentButtonLoadingState,
                    loadingText = stringResource(R.string.updating_details),
                    buttonText = stringResource(id = R.string.update_details),
                    onClick = {
                        keyboardController?.hide()
                        handleButtonClick(
                            viewModel,
                            context
                        )
                    }
                )
            }
        }
    }
    HandleUpdateUserState(viewModel, context, navigator)
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

@Composable
private fun EditProfileImageSection(
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
                .background(ColorsHelper.lightGray())
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
                .border(4.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
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
private fun EditProfileNameInputTextField(viewModel: EditProfileViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    AppOutlinedTextField(
        value = FunctionHelper.getFormattedDisplayName(viewModel.userNameState.value),
        onValueChange = { updatedValue ->
            if (updatedValue.length <= ConstantsHelper.NAME_MAX_CHAR_LIMIT) {
                viewModel.userNameState.value = updatedValue
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(
                        R.string.name_cannot_be_greater_than_max_characters,
                        ConstantsHelper.NAME_MAX_CHAR_LIMIT
                    )
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
private fun BioInputTextField(viewModel: EditProfileViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    AppOutlinedTextField(
        value = viewModel.userBioState.value,
        onValueChange = { updatedValue ->
            if (updatedValue.length <= ConstantsHelper.BIO_MAX_CHAR_LIMIT) {
                viewModel.userBioState.value = updatedValue
            } else {
                viewModel.snackBarMessageState.value =
                    context.getString(
                        R.string.bio_can_t_be_more_than_limited_characters,
                        ConstantsHelper.BIO_MAX_CHAR_LIMIT
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
private fun EditProfileGenderPicker(viewModel: EditProfileViewModel) {
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
private fun EditProfileDOBPicker(viewModel: EditProfileViewModel) {
    var showDatePickerState by remember {
        mutableStateOf(false)
    }
    val dateSelectionState = rememberDatePickerState(initialDisplayMode = DisplayMode.Picker)
    OutlinedTextFieldDisabledFeelsLikeEnabled(
        value = if (viewModel.selectedDOBState.longValue != -1L) FunctionHelper.getFormattedDateTime(
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

@Composable
private fun HandleUpdateUserState(
    viewModel: EditProfileViewModel,
    context: Context,
    navigator: DestinationsNavigator
) {
    val updateUserState = viewModel.updateUserStateFlow.collectAsState().value
    var isResponseHandled by remember {
        mutableStateOf(false)
    }

    when (updateUserState.status) {
        RequestStatusEnum.Loading -> {
            viewModel.currentButtonLoadingState.value = ButtonStateEnum.Loading
            isResponseHandled = false
        }

        RequestStatusEnum.Success -> {
            if (!isResponseHandled) {
                context.showToast(stringResource(R.string.user_details_updated_successfully))
                navigator.popBackStack()
                viewModel.currentButtonLoadingState.value = ButtonStateEnum.Success
                isResponseHandled = true
            }

        }

        RequestStatusEnum.Exception -> {
            if (!isResponseHandled) {
                viewModel.snackBarMessageState.value =
                    if (updateUserState.message.isNullOrBlank()) {
                        context.getString(R.string.something_went_wrong)
                    } else {
                        updateUserState.message.toString()
                    }
                viewModel.currentButtonLoadingState.value = ButtonStateEnum.Error
                LoggingHelper.logData(
                    LoggingLevelEnum.Error,
                    ConstantsHelper.ERROR_TAG,
                    ScreenNameEnum.EditProfileScreen.name,
                    updateUserState.message.toString()
                )
                isResponseHandled = true
            }
        }

        else -> {}
    }
}

/**
 * Handles the click event of the edit profile button.
 *
 * @param viewModel The view model for the edit profile screen.
 * @param context The context of the activity.
 */
private fun handleButtonClick(
    viewModel: EditProfileViewModel,
    context: Context
) {
    when (val userNameValidationResponseCode =
        Validator.isValidName(viewModel.userNameState.value)) {
        1 -> {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.please_enter_name)
            FunctionHelper.vibrateDevice(context)
            return
        }

        2 -> {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.invalid_name)
            FunctionHelper.vibrateDevice(context)
            return
        }

        3 -> {
            viewModel.snackBarMessageState.value =
                context.getString(
                    R.string.name_cannot_be_greater_than_max_characters,
                    ConstantsHelper.NAME_MAX_CHAR_LIMIT
                )
            FunctionHelper.vibrateDevice(context)
            return
        }

        else -> {
            if (userNameValidationResponseCode != 0) {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.something_went_wrong)
                FunctionHelper.vibrateDevice(context)
                return
            }
        }
    }
    when (val genderValidationResponseCode =
        Validator.isValidGender(viewModel.selectedGenderState.value, context)) {
        1 -> {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.please_select_your_gender)
            FunctionHelper.vibrateDevice(context)
            return
        }

        2 -> {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.invalid_gender)
            FunctionHelper.vibrateDevice(context)
            return
        }

        else -> {
            if (genderValidationResponseCode != 0) {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.something_went_wrong)
                FunctionHelper.vibrateDevice(context)
                return
            }
        }
    }
    when (val dobValidationResponseCode =
        Validator.isValidDob(viewModel.selectedDOBState.longValue)) {
        1 -> {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.please_select_your_date_of_birth)
            FunctionHelper.vibrateDevice(context)
            return
        }

        2 -> {
            viewModel.snackBarMessageState.value =
                context.getString(R.string.invalid_date_of_birth)
            FunctionHelper.vibrateDevice(context)
            return
        }

        else -> {
            if (dobValidationResponseCode != 0) {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.something_went_wrong)
                FunctionHelper.vibrateDevice(context)
                return
            }
        }
    }
    when (val bioValidationResponseCode = Validator.isValidBio(viewModel.userBioState.value)) {
        1 -> {
            viewModel.snackBarMessageState.value = context.getString(R.string.please_enter_bio)
            FunctionHelper.vibrateDevice(context)
            return
        }

        2 -> {
            viewModel.snackBarMessageState.value =
                context.getString(
                    R.string.bio_can_t_be_more_than_limited_characters,
                    ConstantsHelper.BIO_MAX_CHAR_LIMIT
                )
            FunctionHelper.vibrateDevice(context)
            return
        }

        else -> {
            if (bioValidationResponseCode != 0) {
                viewModel.snackBarMessageState.value =
                    context.getString(R.string.something_went_wrong)
                FunctionHelper.vibrateDevice(context)
                return
            }
        }
    }
    if (context.isNetworkAvailable()) {
        viewModel.updateUserProfile()
    } else {
        viewModel.snackBarMessageState.value =
            context.getString(R.string.no_internet_connection)
        FunctionHelper.vibrateDevice(context)
    }
}


/**
 * Checks if the edit profile button should be enabled.
 *
 * @param viewModel The view model for the edit profile screen.
 * @return True if the button should be enabled, false otherwise.
 */
private fun isButtonEnabled(viewModel: EditProfileViewModel): Boolean {
    var result = true
    if (
        viewModel.userNameState.value.isBlank()
        || viewModel.selectedGenderState.value.isBlank()
        || viewModel.userBioState.value.isBlank()
        || viewModel.selectedDOBState.longValue == -1L
    ) {
        result = false
    }

    if (result) {
        if (
            viewModel.userNameState.value == viewModel.userDetails.name
            && viewModel.selectedGenderState.value == viewModel.userDetails.gender
            && viewModel.selectedDOBState.longValue == viewModel.userDetails.dateOfBirth
            && viewModel.userBioState.value == viewModel.userDetails.bio
        ) {
            result = false
        }
    }
    if (viewModel.profilePhotoState.value?.uri.toString()
            .isNotBlank() && viewModel.profilePhotoState.value?.uri.toString() != viewModel.userDetails.profilePhoto
    ) {
        result = true
    }

    if (viewModel.coverPhotoState.value?.uri.toString()
            .isNotBlank() && viewModel.coverPhotoState.value?.uri.toString() != viewModel.userDetails.coverPhoto
    ) {
        result = true
    }

    return result
}
