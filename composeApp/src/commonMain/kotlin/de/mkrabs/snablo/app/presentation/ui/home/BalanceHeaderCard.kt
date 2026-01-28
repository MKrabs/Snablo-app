package de.mkrabs.snablo.app.presentation.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import de.mkrabs.snablo.app.util.formatPriceEu

@Composable
fun BalanceHeaderCard(
    balance: Double,
    isLoading: Boolean,
    onTopUp: () -> Unit,
    onProfile: () -> Unit
) {
    // Etwas höher, damit displaySmall nicht abgeschnitten wird
    val balanceRowHeight = 42.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Balance", style = MaterialTheme.typography.labelMedium)

                Box(
                    modifier = Modifier.height(balanceRowHeight),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (isLoading) {
                        val transition = rememberInfiniteTransition(label = "balanceShimmer")
                        // Animiert den "Lichtstreifen" horizontal über den Skeleton-Balken.
                        val shimmerX = transition.animateFloat(
                            initialValue = -220f,
                            targetValue = 220f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 900),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "shimmerX"
                        )

                        val base = MaterialTheme.colorScheme.surfaceVariant
                        val highlight = androidx.compose.ui.graphics.Color.LightGray

                        val shimmerBrush = Brush.linearGradient(
                            colors = listOf(
                                base,
                                highlight,
                                base
                            ),
                            start = androidx.compose.ui.geometry.Offset(shimmerX.value - 140f, 0f),
                            end = androidx.compose.ui.geometry.Offset(shimmerX.value + 140f, 0f)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(140.dp)
                                    .background(
                                        brush = shimmerBrush,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            )
                        }
                    } else {
                        Text(
                            formatPriceEu(balance),
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onTopUp) {
                    Text("+")
                }
                IconButton(onClick = onProfile) {
                    Text("👤")
                }
            }
        }
    }
}
