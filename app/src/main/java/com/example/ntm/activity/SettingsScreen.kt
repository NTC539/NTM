package com.example.ntm.activity

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ntm.R
import com.example.ntm.ui.viewmodel.MainViewModel
import com.example.ntm.utils.PathUtils

// Перечисление для категорий
enum class SettingsCategory {
    MAIN, STORAGE
}

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    // Храним текущую открытую категорию (по умолчанию - главный экран)
    var currentCategory by remember { mutableStateOf(SettingsCategory.MAIN) }

    // В зависимости от выбранной категории показываем нужный контент
    when (currentCategory) {
        SettingsCategory.MAIN -> {
            SettingsMainContent(
                onCategoryClick = { category -> currentCategory = category }
            )
        }
        SettingsCategory.STORAGE -> {
            StorageSettingsContent(
                viewModel = viewModel,
                onBackClick = { currentCategory = SettingsCategory.MAIN }
            )
        }
    }
}

// ==========================================
// 1. ГЛАВНЫЙ ЭКРАН НАСТРОЕК
// ==========================================
@Composable
private fun SettingsMainContent(
    onCategoryClick: (SettingsCategory) -> Unit
) {
    val options = listOf(
        SettingsOption(R.string.settings_appearance, Icons.Outlined.Palette) { /* Открыть внешний вид */ },
        SettingsOption(R.string.settings_storage_title, Icons.Outlined.Storage) { onCategoryClick(SettingsCategory.STORAGE) },
        SettingsOption(R.string.settings_sync, Icons.Outlined.Sync) { /* Открыть синхронизацию */ },
        SettingsOption(R.string.settings_about, Icons.Outlined.Info) { /* Открыть "О приложении" */ }
    )

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn {
            items(options) { option ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { option.onClick() }.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(option.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(option.titleResId),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ==========================================
// 2. ЭКРАН ХРАНИЛИЩА (Внутри настроек)
// ==========================================
@Composable
private fun StorageSettingsContent(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scanPaths by viewModel.scanPaths.collectAsState()
    val excludedPaths by viewModel.excludedPaths.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    val scanFolderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                PathUtils.getPathFromTreeUri(context, it)?.let { path -> viewModel.addScanPath(path) }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Ошибка доступа", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val excludeFolderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                PathUtils.getPathFromTreeUri(context, it)?.let { path -> viewModel.addExcludedPath(path) }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Ошибка доступа", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Верхняя панель с кнопкой "Назад"
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = stringResource(R.string.settings_storage_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
            item { SectionHeader("Пути для сканирования") { scanFolderPicker.launch(null) } }
            if (scanPaths.isEmpty()) {
                item { Text("Сканируется папка Music по умолчанию", modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(scanPaths) { pathEntity ->
                PathItem(pathEntity.path) { viewModel.removeScanPath(pathEntity.path) }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item { SectionHeader("Исключенные пути") { excludeFolderPicker.launch(null) } }
            items(excludedPaths) { pathEntity ->
                PathItem(pathEntity.path) { viewModel.removeExcludedPath(pathEntity.path) }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
            item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showClearDialog = true }.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_clear_db), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.settings_clear_db_sub), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.dialog_clear_db_title)) },
            text = { Text(stringResource(R.string.dialog_clear_db_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMediaLibrary(); showClearDialog = false }) {
                    Text(stringResource(R.string.btn_clear), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

// ==========================================
// ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ
// ==========================================
data class SettingsOption(
    val titleResId: Int,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun SectionHeader(title: String, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        IconButton(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun PathItem(path: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(path, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium)
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}