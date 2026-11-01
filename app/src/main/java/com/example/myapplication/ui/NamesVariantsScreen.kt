package com.example.myapplication.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Move
import com.example.myapplication.data.MoveAlias
import com.example.myapplication.data.MoveIdentity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamesVariantsScreen(
    move: Move,
    variants: List<Move>,
    onSave: (MoveIdentity, () -> Unit, () -> Unit) -> Unit,
    onCreateVariant: (String, String, String, (String) -> Unit, () -> Unit) -> Unit,
    onOpenVariant: (String) -> Unit,
    onBack: () -> Unit,
) {
    val de = LocalStrings.current === StringsDe
    var baseline by rememberSaveable(move.id) { mutableStateOf(move.identity) }
    var draft by rememberSaveable(move.id) { mutableStateOf(move.identity) }
    var aliasName by rememberSaveable(move.id) { mutableStateOf("") }
    var aliasMaster by rememberSaveable(move.id) { mutableStateOf("") }
    var aliasSource by rememberSaveable(move.id) { mutableStateOf("") }
    var variantName by rememberSaveable(move.id) { mutableStateOf("") }
    var variantMaster by rememberSaveable(move.id) { mutableStateOf("") }
    var variantSource by rememberSaveable(move.id) { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var creating by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmBack by remember { mutableStateOf(false) }
    val dirty = draft != baseline

    fun save(leave: Boolean) {
        if (!dirty || saving) return
        saving = true
        error = null
        val current = draft
        onSave(current, {
            baseline = current
            saving = false
            if (leave) onBack()
        }, {
            saving = false
            error = if (de) "Speichern fehlgeschlagen. Bitte erneut versuchen." else "Save failed. Please try again."
        })
    }
    val back: () -> Unit = { if (dirty) confirmBack = true else onBack() }
    BackHandler(onBack = back)

    Scaffold(
        topBar = { TopAppBar(
            title = { Text(if (de) "Namen & Varianten" else "Names & variants") },
            navigationIcon = { AppIconButton(onClick = back) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, if (de) "Zurück" else "Back")
            } }
        ) },
        bottomBar = { Surface {
            AppActionButton(
                onClick = { save(false) }, enabled = dirty && !saving && !creating,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).heightIn(min = 56.dp)
            ) { Text(if (saving) { if (de) "Wird gespeichert …" else "Saving …" } else if (de) "Namen speichern" else "Save names") }
        } }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(move.displayName, style = MaterialTheme.typography.headlineSmall)
            Text(
                if (move.identity.baseMoveId != null) {
                    if (de) "Diese Variante bleibt eine eigene Übung. Angaben zu Meister und Quelle sind persönlich und nicht von der App geprüft."
                    else "This variant remains a separate exercise. Master and source details are personal and have not been verified by the app."
                } else if (de) "Der Originalname und die historischen Quellen bleiben erhalten. Eigene Angaben sind persönlich und nicht von der App geprüft."
                else "The original name and historical sources remain unchanged. Your entries are personal and have not been verified by the app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(if (de) "Trainingsname & Ansage" else "Training name & call", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(selected = draft.preferredAlias == null, onClick = { draft = draft.copy(preferredAlias = null) })
                Text(move.displayName)
            }
            draft.aliases.forEach { alias ->
                Column {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = draft.preferredAlias == alias.name,
                            onClick = { draft = draft.copy(preferredAlias = alias.name) })
                        Text(alias.name, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            draft = draft.copy(
                                aliases = draft.aliases.filterNot { it.name == alias.name },
                                preferredAlias = draft.preferredAlias?.takeUnless { it == alias.name }
                            )
                        }) { Text(if (de) "Entfernen" else "Remove") }
                    }
                    if (alias.master.isNotBlank() || alias.source.isNotBlank()) Text(
                        (if (de) "Eigene Angabe: " else "Your information: ") +
                            listOf(alias.master, alias.source).filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                if (de) "Bei App-Stimme wird dieser Name angesagt. Eine eigene Audioaufnahme bleibt unverändert."
                else "The app voice speaks this name. Your own audio recording remains unchanged.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()
            Text(if (de) "Weiteren Namen hinzufügen" else "Add another name", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(aliasName, { aliasName = it; error = null },
                label = { Text(if (de) "Name" else "Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(aliasMaster, { aliasMaster = it },
                label = { Text(if (de) "Meister / Tradition (optional)" else "Master / tradition (optional)") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(aliasSource, { aliasSource = it },
                label = { Text(if (de) "Quelle (optional)" else "Source (optional)") },
                modifier = Modifier.fillMaxWidth())
            AppActionButton(tone = ActionTone.Secondary, onClick = {
                val name = aliasName.trim()
                if (name.equals(move.displayName, ignoreCase = true) || draft.aliases.any { it.name.equals(name, ignoreCase = true) }) {
                    error = if (de) "Diesen Namen gibt es bereits." else "This name already exists."
                } else {
                    draft = draft.copy(aliases = draft.aliases + MoveAlias(name, aliasMaster.trim(), aliasSource.trim()))
                    aliasName = ""; aliasMaster = ""; aliasSource = ""; error = null
                }
            }, enabled = aliasName.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text(if (de) "Namen übernehmen" else "Add name")
            }

            if (move.identity.baseMoveId != null) {
                HorizontalDivider()
                Text(if (de) "Angaben zu dieser Variante" else "About this variant", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(draft.master, { draft = draft.copy(master = it) },
                    label = { Text(if (de) "Meister / Tradition (optional)" else "Master / tradition (optional)") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(draft.source, { draft = draft.copy(source = it) },
                    label = { Text(if (de) "Quelle (optional)" else "Source (optional)") },
                    modifier = Modifier.fillMaxWidth())
            }

            HorizontalDivider()
            Text(if (de) "Varianten" else "Variants", style = MaterialTheme.typography.titleMedium)
            if (variants.isEmpty()) Text(
                if (de) "Noch keine eigenen Varianten." else "No personal variants yet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            variants.forEach { variant ->
                TextButton(onClick = { onOpenVariant(variant.id) }) { Text(variant.trainingName) }
            }
            Text(
                if (de) "Eine andere Ausführung wird eine eigene Übung und kann getrennt im Training ausgewählt werden."
                else "A different execution becomes its own exercise and can be selected separately in training.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(variantName, { variantName = it; error = null },
                label = { Text(if (de) "Name der Variante" else "Variant name") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(variantMaster, { variantMaster = it },
                label = { Text(if (de) "Meister / Tradition (optional)" else "Master / tradition (optional)") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(variantSource, { variantSource = it },
                label = { Text(if (de) "Quelle (optional)" else "Source (optional)") },
                modifier = Modifier.fillMaxWidth())
            AppActionButton(onClick = {
                val name = variantName.trim()
                if (name.equals(move.displayName, ignoreCase = true) ||
                    variants.any { it.displayName.equals(name, ignoreCase = true) }
                ) {
                    error = if (de) "Diese Übung gibt es bereits. Gib der Variante einen eigenen Namen."
                        else "This exercise already exists. Give the variant a distinct name."
                } else {
                    creating = true
                    error = null
                    onCreateVariant(name, variantMaster.trim(), variantSource.trim(),
                        { id -> creating = false; onOpenVariant(id) },
                        { creating = false; error = if (de) "Variante konnte nicht erstellt werden." else "Could not create variant." })
                }
            }, enabled = variantName.isNotBlank() && !dirty && !creating && !saving,
                modifier = Modifier.fillMaxWidth()) {
                Text(if (creating) { if (de) "Wird erstellt …" else "Creating …" }
                    else if (de) "Variante anlegen" else "Create variant")
            }
            if (dirty) Text(
                if (de) "Speichere zuerst deine Namensänderungen, bevor du eine Variante anlegst."
                else "Save your name changes before creating a variant.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
    if (confirmBack) AlertDialog(
        onDismissRequest = { confirmBack = false },
        title = { Text(if (de) "Änderungen speichern?" else "Save changes?") },
        text = { Text(if (de) "Deine Namensänderungen sind noch nicht gespeichert." else "Your name changes have not been saved yet.") },
        confirmButton = { TextButton(onClick = { confirmBack = false; save(true) }) { Text(if (de) "Speichern" else "Save") } },
        dismissButton = { Column {
            TextButton(onClick = { confirmBack = false; onBack() }) { Text(if (de) "Verwerfen" else "Discard") }
            TextButton(onClick = { confirmBack = false }) { Text(if (de) "Weiter bearbeiten" else "Continue editing") }
        } }
    )
}
