package com.example.myapplication.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.OpenSourceCatalog
import com.example.myapplication.data.loadOpenSourceCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicensesScreen(german: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var retry by remember { mutableIntStateOf(0) }
    val result by produceState<Result<OpenSourceCatalog>?>(null, retry) {
        value = null
        value = withContext(Dispatchers.IO) { runCatching { loadOpenSourceCatalog(context.assets) } }
    }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val catalog = result?.getOrNull()
    val selected = catalog?.libraries?.find { it.id == selectedId }
    val back = { if (selectedId != null) selectedId = null else onBack() }
    BackHandler(selectedId != null) { selectedId = null }
    Scaffold(topBar = {
        TopAppBar(title = { Text(if (german) "Open-Source-Lizenzen" else "Open-source licenses") },
            navigationIcon = {
                AppIconButton(onClick = back) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = if (german) "Zurück" else "Back")
                }
            })
    }) { padding ->
        when {
            result == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            catalog == null -> Column(Modifier.padding(padding).padding(16.dp)) {
                Text(if (german) "Die Lizenzhinweise konnten nicht geladen werden." else "The license notices could not be loaded.")
                TextButton(onClick = { retry++ }) { Text(if (german) "Erneut versuchen" else "Try again") }
            }
            selected != null -> key(selected.id) {
                LazyColumn(Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Text(selected.name, style = MaterialTheme.typography.headlineSmall)
                        Text("Version ${selected.version}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(selected.id, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = { runCatching { uriHandler.openUri(selected.url) }.onFailure {
                                android.widget.Toast.makeText(context,
                                    if (german) "Kein Browser zum Öffnen verfügbar." else "No browser available.",
                                    android.widget.Toast.LENGTH_SHORT).show()
                            } }) {
                            Text(if (german) "Projektwebseite öffnen" else "Open project website")
                        }
                    }
                    val sections = listOf(
                        (if (german) "Lizenztexte" else "License texts") to selected.licenses,
                        (if (german) "Urheber- und Quellenhinweise" else "Copyright and attribution notices") to selected.notices,
                        (if (german) "Mitgelieferte Quelldaten" else "Included source data") to selected.sources)
                    sections.forEach { (title, ids) ->
                        if (ids.isNotEmpty()) {
                            item { Text(title, style = MaterialTheme.typography.titleMedium) }
                            ids.forEach { id ->
                                // Paragraphs keep long license/source documents efficient and readable.
                                val paragraphs = catalog.texts.getValue(id).split(Regex("""\n\s*\n"""))
                                    .flatMap { it.chunked(4000) }
                                items(paragraphs) { paragraph ->
                                    SelectionContainer { Text(paragraph, style = MaterialTheme.typography.bodyMedium) }
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                val filtered = catalog.libraries.filter {
                    it.name.contains(query, ignoreCase = true) || it.id.contains(query, ignoreCase = true)
                }
                Column(Modifier.fillMaxSize().padding(padding)) {
                    Text(if (german) "Diese Bibliotheken helfen, Haulauf möglich zu machen. Alle Lizenztexte sind offline verfügbar."
                        else "These libraries help make Haulauf possible. All license texts are available offline.",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true,
                        label = { Text(if (german) "Bibliothek suchen" else "Search libraries") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
                    if (filtered.isEmpty()) Text(if (german) "Keine passende Bibliothek gefunden." else "No matching library found.",
                        modifier = Modifier.padding(16.dp))
                    LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
                        items(filtered, key = { it.id }) { library ->
                            Row(Modifier.fillMaxWidth().clickable(role = Role.Button) { selectedId = library.id }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Text(library.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Version ${library.version}", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}
