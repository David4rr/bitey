package com.bitey.app.feature.fatetable

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitey.app.core.ui.neumorphic.minimalistCard
import com.bitey.app.core.ui.theme.BiteyOrange
import com.bitey.app.core.ui.theme.LocalNeumorphicTheme
import com.bitey.app.core.ui.theme.StickerDieCutWhite
import com.bitey.app.feature.fatetable.component.*
import kotlinx.coroutines.launch

@Composable
fun FateTableScreen(
    onNavigateToNewEntry: () -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    viewModel: FateTableViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val theme = LocalNeumorphicTheme.current

    val rotationAnimatable = remember { Animatable(uiState.currentRotationAngle) }
    var lastHapticSliceIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(rotationAnimatable.value) {
        if (uiState.candidates.isNotEmpty()) {
            val sliceAngle = 360f / uiState.candidates.size
            val currentSlice = (rotationAnimatable.value / sliceAngle).toInt()
            if (currentSlice != lastHapticSliceIndex) {
                lastHapticSliceIndex = currentSlice
                if (uiState.isSpinning) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FateTableHeader(
            candidateCount = uiState.candidates.size,
            isSpinning = uiState.isSpinning,
            onShuffle = { viewModel.shuffleCandidates() },
            selectedFilter = uiState.selectedFilter,
            selectedTagId = uiState.selectedTagId,
            availableTags = uiState.availableTags,
            onSelectFilter = { viewModel.selectFilter(it) },
            onSelectTag = { viewModel.selectTag(it) },
            onNavigateBack = onNavigateBack
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.candidates.size < 2) {
            InsufficientCandidatesPrompt(
                totalEntriesCount = uiState.totalEntriesCount,
                onCaptureClick = onNavigateToNewEntry,
                onResetFilters = {
                    viewModel.selectFilter(FateSourceFilter.ALL)
                    viewModel.selectTag(null)
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .minimalistCard(cornerRadius = 180.dp, elevation = 0.dp)
                )

                WheelCanvas(
                    candidates = uiState.candidates,
                    rotationAngle = rotationAnimatable.value,
                    modifier = Modifier.fillMaxSize().padding(14.dp)
                )

                WheelPointerIndicator(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 2.dp)
                )

                CenterHubCap()
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val spinResult = viewModel.calculateSpinTarget(uiState.candidates)
                    if (spinResult != null) {
                        viewModel.onSpinStarted()
                        coroutineScope.launch {
                            rotationAnimatable.animateTo(
                                targetValue = spinResult.targetAngle,
                                animationSpec = tween(
                                    durationMillis = 4600,
                                    easing = CubicBezierEasing(0.12f, 0.8f, 0.2f, 1f)
                                )
                            )
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onSpinCompleted(spinResult.targetAngle, spinResult.winningEntry)
                        }
                    }
                },
                enabled = !uiState.isSpinning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BiteyOrange,
                    contentColor = StickerDieCutWhite,
                    disabledContainerColor = BiteyOrange.copy(alpha = 0.6f),
                    disabledContentColor = StickerDieCutWhite.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (uiState.isSpinning) "Serendipity In Motion..." else "SPIN THE TABLE",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (uiState.showWinningDialog && uiState.winningEntry != null) {
        WinningDishDialog(
            entryWithTags = uiState.winningEntry!!,
            onDismiss = { viewModel.dismissWinningDialog() },
            onNavigate = {
                val lat = uiState.winningEntry!!.entry.latitude
                val lng = uiState.winningEntry!!.entry.longitude
                if (lat != null && lng != null) {
                    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(uiState.winningEntry!!.entry.title)})")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            }
        )
    }
}
