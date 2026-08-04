package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramBlueDark

@Composable
fun TelegramDrawer(
    userProfile: UserProfile,
    onSavedMessagesClick: () -> Unit,
    onNewGroupClick: () -> Unit,
    onNewChannelClick: () -> Unit,
    onContactsClick: () -> Unit,
    onCallsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
    ) {
        // User Profile Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(TelegramBlue, TelegramBlueDark)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                AvatarView(
                    title = userProfile.name,
                    avatarUrl = userProfile.avatarUrl,
                    size = 64.dp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = userProfile.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = userProfile.phone,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = userProfile.username,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Items
        DrawerItem(
            icon = Icons.Default.Bookmark,
            title = "Запазени съобщения",
            testTag = "drawer_saved_messages",
            onClick = {
                onCloseDrawer()
                onSavedMessagesClick()
            }
        )

        DrawerItem(
            icon = Icons.Default.GroupAdd,
            title = "Нова група",
            testTag = "drawer_new_group",
            onClick = {
                onCloseDrawer()
                onNewGroupClick()
            }
        )

        DrawerItem(
            icon = Icons.Default.Campaign,
            title = "Нов канал",
            testTag = "drawer_new_channel",
            onClick = {
                onCloseDrawer()
                onNewChannelClick()
            }
        )

        DrawerItem(
            icon = Icons.Default.Contacts,
            title = "Контакти",
            testTag = "drawer_contacts",
            onClick = {
                onCloseDrawer()
                onContactsClick()
            }
        )

        DrawerItem(
            icon = Icons.Default.Call,
            title = "Обаждания",
            testTag = "drawer_calls",
            onClick = {
                onCloseDrawer()
                onCallsClick()
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        DrawerItem(
            icon = Icons.Default.Settings,
            title = "Настройки",
            testTag = "drawer_settings",
            onClick = {
                onCloseDrawer()
                onSettingsClick()
            }
        )

        // Dark mode quick switch
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDarkModeToggle(!userProfile.isDarkMode) }
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("drawer_dark_mode_toggle")
        ) {
            Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = "Night Mode",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "Нощен режим",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = userProfile.isDarkMode,
                onCheckedChange = onDarkModeToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = TelegramBlue
                )
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "About",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Telegram v10.5.0 (Bulgaria)",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    title: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
