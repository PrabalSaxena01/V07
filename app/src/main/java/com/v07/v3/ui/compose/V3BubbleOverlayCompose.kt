package com.v07.v3.ui.compose

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.v07.dashboard.R
import com.v07.v3.core.data.V3Task

/**
 * Custom lifecycle owner that enables Jetpack Compose ComposeViews to be hosted
 * safely inside an Android Service WindowManager overlay.
 */
class OverlayServiceLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    init {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }

    fun clear() {
        store.clear()
    }
}

/**
 * Helper to attach all three required ViewTree owners to a View hosted in WindowManager.
 */
fun bindViewToOverlayLifecycle(view: android.view.View, owner: OverlayServiceLifecycleOwner) {
    view.setViewTreeLifecycleOwner(owner)
    view.setViewTreeViewModelStoreOwner(owner)
    view.setViewTreeSavedStateRegistryOwner(owner)
}

/**
 * Safely converts an Android Drawable into a Compose-friendly ImageBitmap.
 */
fun drawableToBitmap(drawable: Drawable?): Bitmap? {
    if (drawable == null) return null
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 64
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 64
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

/**
 * Jetpack Compose Floating Bubble Pill View.
 * Displays real-time active application usage telemetry in a subtle sovereign pill.
 * Features native drag physics, touch-callout feedback, and tap-to-expand menu.
 */
@Composable
fun FloatingBubblePillCompose(
    appIcon: Drawable?,
    formattedUsage: String,
    onClick: () -> Unit = {},
    onDrag: (dx: Float, dy: Float) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bitmap = remember(appIcon) { drawableToBitmap(appIcon) }
    var isInteracting by remember { mutableStateOf(false) }

    Surface(
        shape = PillShape,
        color = if (isInteracting) Color(0xF2152033) else Color(0xE60B111E),
        border = BorderStroke(1.dp, if (isInteracting) Color(0x9960A5FA) else Color(0x4060A5FA)),
        shadowElevation = if (isInteracting) 14.dp else 8.dp,
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isInteracting = true
                        tryAwaitRelease()
                        isInteracting = false
                    },
                    onTap = { onClick() }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isInteracting = true },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = {
                        isInteracting = false
                        onDragEnd()
                    },
                    onDragCancel = {
                        isInteracting = false
                        onDragEnd()
                    }
                )
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Active App",
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.v07_logo),
                    contentDescription = "Sanctuary",
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = formattedUsage,
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

enum class OverlayModalPage {
    MENU,
    QUICK_THOUGHT,
    CHECKLIST
}

/**
 * Jetpack Compose Full-Screen Action Menu & Sub-dialogs Modal for Floating Overlay.
 */
@Composable
fun FloatingOverlayMenuModal(
    appIcon: Drawable?,
    appName: String,
    formattedUsage: String,
    tasks: List<V3Task>,
    onClose: () -> Unit,
    onStartFocus: () -> Unit,
    onOpenSanctuary: () -> Unit,
    onTurnOffOverlay: () -> Unit,
    onSaveThought: (String) -> Unit,
    onToggleTask: (V3Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableStateOf(OverlayModalPage.MENU) }
    var thoughtInput by remember { mutableStateOf("") }
    val bitmap = remember(appIcon) { drawableToBitmap(appIcon) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        // Stop clicks on the dialog card from propagating to the backdrop
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF101726),
            border = BorderStroke(1.dp, Color(0x3360A5FA)),
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .clickable(enabled = false) {}
        ) {
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "overlay_page"
            ) { page ->
                when (page) {
                    OverlayModalPage.MENU -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Header with Active App info & Close button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(R.drawable.v07_logo),
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = appName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "$formattedUsage active today",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF93C5FD)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = onClose,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Close",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // 1. Quick Thought
                            OverlayActionButton(
                                icon = Icons.Outlined.ChatBubbleOutline,
                                title = "Quick Thought",
                                subtitle = "Capture reflection to Sanctuary",
                                onClick = { currentPage = OverlayModalPage.QUICK_THOUGHT }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. Directives & Tasks
                            OverlayActionButton(
                                icon = Icons.Outlined.Checklist,
                                title = "Directives & Tasks",
                                subtitle = "View and check off today's tasks",
                                onClick = { currentPage = OverlayModalPage.CHECKLIST }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. Focus Interval
                            OverlayActionButton(
                                icon = Icons.Outlined.Timer,
                                title = "Focus Interval",
                                subtitle = "Start 25-minute quiet session",
                                onClick = {
                                    onClose()
                                    onStartFocus()
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 4. Open V07 Sanctuary
                            OverlayActionButton(
                                icon = Icons.Outlined.AccountBalance,
                                title = "Open V07 Sanctuary",
                                subtitle = "Dashboard, ledger & stats",
                                onClick = {
                                    onClose()
                                    onOpenSanctuary()
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 5. Turn Off Overlay
                            OverlayActionButton(
                                icon = Icons.Outlined.Block,
                                title = "Turn Off Bubble Overlay",
                                subtitle = "Disable floating telemetry pill",
                                isDestructive = true,
                                onClick = {
                                    onClose()
                                    onTurnOffOverlay()
                                }
                            )
                        }
                    }

                    OverlayModalPage.QUICK_THOUGHT -> {
                        var selectedTag by remember { mutableStateOf("General Reflection") }
                        val tagOptions = remember { listOf("General Reflection", "Work Observation", "Quiet Insight", "Personal Growth", "Urgent Thought") }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Capture Reflection",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                IconButton(
                                    onClick = { currentPage = OverlayModalPage.MENU },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Back",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Text(
                                text = "Logged under $appName context",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF93C5FD)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            SanctuaryFormDropdown(
                                label = "Reflection Domain",
                                selectedItem = selectedTag,
                                items = tagOptions,
                                onItemSelected = { selectedTag = it },
                                leadingIcon = Icons.Outlined.Label,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = thoughtInput,
                                onValueChange = { thoughtInput = it },
                                placeholder = {
                                    Text(
                                        text = "What is currently on your mind?",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.5.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    )
                                },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    color = Color.White
                                ),
                                minLines = 3,
                                maxLines = 5,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SanctuaryPalette.PrimaryTeal,
                                    unfocusedBorderColor = Color(0x3360A5FA),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = SanctuaryPalette.PrimaryTeal
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 80.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { currentPage = OverlayModalPage.MENU },
                                    modifier = Modifier.height(44.dp)
                                ) {
                                    Text("Cancel", color = Color(0xFF94A3B8))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val trimmed = thoughtInput.trim()
                                        if (trimmed.isNotBlank()) {
                                            onSaveThought("[$selectedTag] $trimmed")
                                            onClose()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SanctuaryPalette.PrimaryTeal),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(44.dp)
                                ) {
                                    Icon(imageVector = Icons.Outlined.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Reflection", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    OverlayModalPage.CHECKLIST -> {
                        var taskFilter by remember { mutableStateOf("All") }
                        val filterOptions = remember { listOf("All", "High Priority ⚡", "Pending", "Completed") }

                        val filteredTasks = remember(tasks, taskFilter) {
                            when (taskFilter) {
                                "High Priority ⚡" -> tasks.filter { it.priority.equals("high", ignoreCase = true) || it.priority.equals("urgent", ignoreCase = true) }
                                "Pending" -> tasks.filter { !it.status.equals("completed", true) && !it.status.equals("done", true) }
                                "Completed" -> tasks.filter { it.status.equals("completed", true) || it.status.equals("done", true) }
                                else -> tasks
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 480.dp)
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Directives & Tasks",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Today's priority directives",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF93C5FD)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    SanctuaryFilterDropdown(
                                        selectedItem = taskFilter,
                                        items = filterOptions,
                                        onItemSelected = { taskFilter = it },
                                        leadingIcon = Icons.Outlined.FilterList
                                    )

                                    IconButton(
                                        onClick = { currentPage = OverlayModalPage.MENU },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Back",
                                            tint = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (filteredTasks.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (tasks.isEmpty()) "No active tasks for today 🌿" else "No matching directives",
                                        color = Color(0xFF94A3B8),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(filteredTasks) { task ->
                                        val isDone = task.status.equals("completed", ignoreCase = true) ||
                                                task.status.equals("done", ignoreCase = true)

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0x22FFFFFF))
                                                .clickable { onToggleTask(task) }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isDone) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (isDone) SanctuaryPalette.AccentHabits else Color(0xFF94A3B8),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isDone) Color(0xFF94A3B8) else Color.White,
                                                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0x1AFFFFFF),
        border = BorderStroke(1.dp, if (isDestructive) Color(0x33EF4444) else Color(0x1A60A5FA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) Color(0xFFF87171) else Color(0xFF60A5FA),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDestructive) Color(0xFFF87171) else Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
