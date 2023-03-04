//
// This is free and unencumbered software released into the public domain.
//
// Anyone is free to copy, modify, publish, use, compile, sell, or
// distribute this software, either in source code form or as a compiled
// binary, for any purpose, commercial or non-commercial, and by any
// means.
//
// In jurisdictions that recognize copyright laws, the author or authors
// of this software dedicate any and all copyright interest in the
// software to the public domain. We make this dedication for the benefit
// of the public at large and to the detriment of our heirs and
// successors. We intend this dedication to be an overt act of
// relinquishment in perpetuity of all present and future rights to this
// software under copyright law.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
// IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
// OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
// For more information, please refer to <https://unlicense.org>
//
package com.github.jonforshort.androidlocalvpn.ui.main

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.github.jonforshort.androidlocalvpn.R
import com.github.jonforshort.androidlocalvpn.ui.theme.AndroidLocalVpnTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

@Composable
internal fun PolicyTab(
    applicationsSettings: State<List<ApplicationSettings>>,
    onApplicationSettingTapped: (VpnPolicy, ApplicationSettings) -> Unit,
    onResetApplicationSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        VpnPolicy.ALLOW.capitalizedName(),
        VpnPolicy.DISALLOW.capitalizedName()
    )
    val isDropDownMenuExpanded = remember { mutableStateOf(false) }
    val selectedItem = remember { mutableStateOf(items.first()) }

    Column(modifier) {

        AllowOrDisallowApplicationsDropDownMenu(
            modifier = Modifier.fillMaxWidth(),
            selectedItem = selectedItem.value,
            items = items,
            isExpanded = isDropDownMenuExpanded.value,
            onDismissRequest = {
                isDropDownMenuExpanded.value = false
            },
            onItemSelected = {
                selectedItem.value = it
            },
            onDropDownMenuClicked = {
                isDropDownMenuExpanded.value = !isDropDownMenuExpanded.value
            })

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .weight(1f)
        ) {
            ApplicationSettings(
                selectedPolicy = VpnPolicy.valueOf(selectedItem.value.uppercase()),
                applicationsSettings = applicationsSettings,
                onApplicationSettingTapped = onApplicationSettingTapped,
                modifier = Modifier
                    .matchParentSize()
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onResetApplicationSettings,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Save user selection"
                )
                Text(
                    text = "Save"
                )
            }

            OutlinedButton(
                onClick = onResetApplicationSettings,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Clear user selection"
                )
                Text(
                    text = "Clear"
                )
            }
        }
    }
}

private fun VpnPolicy.capitalizedName() =
    name.lowercase().replaceFirstChar { it.titlecase(Locale.ROOT) }

@Composable
private fun AllowOrDisallowApplicationsDropDownMenu(
    modifier: Modifier,
    selectedItem: String,
    items: List<String>,
    isExpanded: Boolean = false,
    onDismissRequest: () -> Unit,
    onItemSelected: (String) -> Unit,
    onDropDownMenuClicked: () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onDropDownMenuClicked,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Drop down item"
            )
            Text(selectedItem)
        }
    }

    DropdownMenu(
        modifier = modifier,
        expanded = isExpanded,
        onDismissRequest = onDismissRequest,
    ) {
        items.forEach {
            DropdownMenuItem(
                onClick = {
                    onItemSelected(it)
                    onDismissRequest()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = it)
            }
        }
    }
}

@Composable
private fun ApplicationSettings(
    selectedPolicy: VpnPolicy,
    applicationsSettings: State<List<ApplicationSettings>>,
    onApplicationSettingTapped: (VpnPolicy, ApplicationSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier) {
        applicationsSettings.value.forEach { applicationSetting ->
            item(key = applicationSetting.packageName) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            modifier = Modifier.height(40.dp),
                            bitmap = applicationSetting.appIcon.toBitmap().asImageBitmap(),
                            contentDescription = "Application icon"
                        )

                        Text(
                            text = applicationSetting.appName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 40.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )

                        val triState = when {
                            selectedPolicy == VpnPolicy.ALLOW && applicationSetting.policy == VpnPolicy.ALLOW -> ToggleableState.On
                            selectedPolicy == VpnPolicy.DISALLOW && applicationSetting.policy == VpnPolicy.DISALLOW -> ToggleableState.On
                            else -> ToggleableState.Off
                        }

                        TriStateCheckbox(state = triState, onClick = {
                            onApplicationSettingTapped(selectedPolicy, applicationSetting)
                        })
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PolicyTabPreview() {
    val context = LocalContext.current

    AndroidLocalVpnTheme {
        PolicyTab(applicationsSettings = MutableStateFlow(
            listOf(
                ApplicationSettings(
                    appName = "test test test",
                    packageName = "com.test",
                    policy = VpnPolicy.DEFAULT,
                    appIcon = AppCompatResources.getDrawable(
                        context, R.drawable.ic_launcher_background
                    )!!
                )
            )
        ).collectAsState(),
            onApplicationSettingTapped = { _, _ -> },
            onResetApplicationSettings = {})
    }
}