package com.djoudini.iptv.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategorySelectScreen(
    categories: List<SelectableCategory>,
    onToggle: (String) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Wähle deine Kategorien", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onSelectAll(true) }) { Text("Alle auswählen") }
            TextButton(onClick = { onSelectAll(false) }) { Text("Alle abwählen") }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(0.6f),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(categories) { selectable ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectable.isSelected,
                        onCheckedChange = { onToggle(selectable.entity.id) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = selectable.entity.name,
                        color = if (selectable.entity.isAdult) Color.Red else Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNext, modifier = Modifier.width(200.dp)) {
            Text("Synchronisieren")
        }
    }
}
