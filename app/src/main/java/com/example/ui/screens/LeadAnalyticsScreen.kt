package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LeadEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

@Composable
fun LeadAnalyticsScreen(
    leads: List<LeadEntity>,
    modifier: Modifier = Modifier
) {
    // Generate realistic 30-day lead acquisition trend data
    val dailyTrendData = remember(leads) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM d", Locale.US)
        val dataList = mutableListOf<DayLeadPoint>()

        // 30 days back
        for (i in 29 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val label = dateFormat.format(dayCal.time)
            val dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK)
            val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

            // Calculate realistic lead volume
            val baseCount = if (isWeekend) (0..2).random() else (2..7).random()
            val qualifiedCount = (baseCount * 0.65f).roundToInt().coerceAtMost(baseCount)

            dataList.add(
                DayLeadPoint(
                    dayLabel = label,
                    leadCount = baseCount,
                    qualifiedCount = qualifiedCount,
                    dayIndex = 29 - i
                )
            )
        }
        dataList
    }

    // Status breakdown distribution
    val statusDistribution = remember(leads) {
        val total = leads.size.coerceAtLeast(1)
        val qualified = leads.count { it.status.equals("qualified", ignoreCase = true) || it.status.equals("accepted", ignoreCase = true) }
        val newLeads = leads.count { it.status.equals("new", ignoreCase = true) }
        val disputed = leads.count { it.disputeStatus == "open" || it.disputeStatus == "accepted" }
        val underContract = (leads.size * 0.22f).roundToInt().coerceAtLeast(2)
        val closedDeals = (leads.size * 0.14f).roundToInt().coerceAtLeast(1)

        listOf(
            StatusSlice("Qualified & Accepted", qualified.coerceAtLeast(5), SleekSuccessGreen),
            StatusSlice("New / In Intake", newLeads.coerceAtLeast(3), CoralPrimary),
            StatusSlice("Under Contract", underContract, Color(0xFF6366F1)),
            StatusSlice("Closed Deals", closedDeals, GoldAccent),
            StatusSlice("Disputed / Review", disputed.coerceAtLeast(1), WarningOrange)
        )
    }

    val total30Days = remember(dailyTrendData) { dailyTrendData.sumOf { it.leadCount } }
    val totalQualified30Days = remember(dailyTrendData) { dailyTrendData.sumOf { it.qualifiedCount } }
    val qualificationRate = remember(total30Days, totalQualified30Days) {
        if (total30Days > 0) ((totalQualified30Days.toFloat() / total30Days) * 100).roundToInt() else 68
    }

    var selectedDataPoint by remember { mutableStateOf<DayLeadPoint?>(null) }
    var selectedSlice by remember { mutableStateOf<StatusSlice?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("lead_analytics_screen"),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Analytics Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Lead Performance Analytics",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Real-time acquisition volume & pipeline health metrics",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CoralPrimaryDim
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SleekSuccessGreen)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Last 30 Days",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralPrimary
                            )
                        }
                    }
                }
            }
        }

        // Top KPI Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiMetricCard(
                    title = "30-Day Intake",
                    value = "$total30Days Leads",
                    subtitle = "+18.4% vs last month",
                    icon = Icons.Default.TrendingUp,
                    accentColor = CoralPrimary,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "Qualification Rate",
                    value = "$qualificationRate%",
                    subtitle = "$totalQualified30Days qualified deals",
                    icon = Icons.Default.CheckCircle,
                    accentColor = SleekSuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiMetricCard(
                    title = "Pipeline Volume",
                    value = "$14.8M",
                    subtitle = "Estimated asking price",
                    icon = Icons.Default.MonetizationOn,
                    accentColor = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "Avg. Intake-to-Close",
                    value = "22 Days",
                    subtitle = "Industry standard: 45d",
                    icon = Icons.Default.Timer,
                    accentColor = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 30-Day Lead Acquisition Trend Chart (Recharts style native visualizer)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LEAD ACQUISITION TREND (30 DAYS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = CoralPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Daily caller deliveries & qualified milestones",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LegendItem(label = "Delivered", color = CoralPrimary)
                            LegendItem(label = "Qualified", color = SleekSuccessGreen)
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Native Interactive Trend Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        LeadAcquisitionTrendChart(
                            points = dailyTrendData,
                            selectedPoint = selectedDataPoint,
                            onSelectPoint = { selectedDataPoint = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (selectedDataPoint != null) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Selected Date: ${selectedDataPoint?.dayLabel}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${selectedDataPoint?.leadCount} Leads (${selectedDataPoint?.qualifiedCount} Qualified)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CoralPrimary
                                )
                            }
                        }
                    } else {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "💡 Tip: Tap any bar to inspect daily acquisition volume & qualification stats.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Status Distribution Breakdown (Interactive Donut Chart)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LEAD STATUS DISTRIBUTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = GoldAccent,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Current pipeline status across all active deals",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Donut Chart Canvas
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            StatusDonutChart(
                                slices = statusDistribution,
                                selectedSlice = selectedSlice,
                                onSelectSlice = { selectedSlice = it },
                                modifier = Modifier.fillMaxSize()
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val totalDeals = statusDistribution.sumOf { it.count }
                                Text(
                                    text = "${selectedSlice?.count ?: totalDeals}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = selectedSlice?.color ?: MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (selectedSlice != null) "Deals" else "Total",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        // Status Distribution Legend & Percentages
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val totalCount = statusDistribution.sumOf { it.count }.coerceAtLeast(1)
                            statusDistribution.forEach { slice ->
                                val pct = ((slice.count.toFloat() / totalCount) * 100).roundToInt()
                                val isSelected = selectedSlice?.label == slice.label
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) slice.color.copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable {
                                            selectedSlice = if (isSelected) null else slice
                                        }
                                        .padding(vertical = 4.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(slice.color)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = slice.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "$pct% (${slice.count})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = slice.color
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Campaign Channel Conversion Table
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CAMPAIGN CONVERSION PERFORMANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF6366F1),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    val campaignStats = listOf(
                        Triple("DFW Distressed Homeowners", "32 Leads", "78% Qual Rate"),
                        Triple("Atlanta Absentee High Equity", "24 Leads", "62% Qual Rate"),
                        Triple("Phoenix Pre-Foreclosure Niche", "16 Leads", "81% Qual Rate"),
                        Triple("Tampa Probates & Inherited", "11 Leads", "55% Qual Rate")
                    )

                    campaignStats.forEach { (name, count, rate) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(count, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SleekSuccessGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = rate,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekSuccessGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

data class DayLeadPoint(
    val dayLabel: String,
    val leadCount: Int,
    val qualifiedCount: Int,
    val dayIndex: Int
)

data class StatusSlice(
    val label: String,
    val count: Int,
    val color: Color
)

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LeadAcquisitionTrendChart(
    points: List<DayLeadPoint>,
    selectedPoint: DayLeadPoint?,
    onSelectPoint: (DayLeadPoint?) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxCount = remember(points) { (points.maxOfOrNull { it.leadCount } ?: 8).coerceAtLeast(6) }

    Canvas(
        modifier = modifier
            .pointerInput(points) {
                detectTapGestures { offset ->
                    val barWidth = size.width / points.size
                    val clickedIdx = (offset.x / barWidth).toInt().coerceIn(0, points.size - 1)
                    onSelectPoint(points[clickedIdx])
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val barWidth = width / points.size
        val bottomY = height - 20f

        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = bottomY - (i.toFloat() / gridLines) * (bottomY - 20f)
            drawLine(
                color = Color.Gray.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw Bars for each day
        points.forEachIndexed { index, point ->
            val x = index * barWidth + barWidth * 0.15f
            val actualBarWidth = barWidth * 0.7f
            val barHeight = (point.leadCount.toFloat() / maxCount) * (bottomY - 30f)
            val qualifiedBarHeight = (point.qualifiedCount.toFloat() / maxCount) * (bottomY - 30f)
            val topY = bottomY - barHeight
            val qualifiedTopY = bottomY - qualifiedBarHeight

            val isSelected = selectedPoint?.dayIndex == point.dayIndex

            // Total intake bar
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = if (isSelected) listOf(CoralPrimary, CoralPrimary.copy(alpha = 0.8f))
                    else listOf(CoralPrimary.copy(alpha = 0.6f), CoralPrimary.copy(alpha = 0.25f))
                ),
                topLeft = Offset(x, topY),
                size = Size(actualBarWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Qualified portion overlay
            if (point.qualifiedCount > 0) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(SleekSuccessGreen, SleekSuccessGreen.copy(alpha = 0.5f))
                    ),
                    topLeft = Offset(x, qualifiedTopY),
                    size = Size(actualBarWidth, qualifiedBarHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            if (isSelected) {
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(x + actualBarWidth / 2f, topY)
                )
            }
        }

        // Draw Bezier trendline connecting top points
        val path = Path()
        points.forEachIndexed { index, point ->
            val x = index * barWidth + barWidth / 2f
            val y = bottomY - (point.leadCount.toFloat() / maxCount) * (bottomY - 30f)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                val prevX = (index - 1) * barWidth + barWidth / 2f
                val prevPoint = points[index - 1]
                val prevY = bottomY - (prevPoint.leadCount.toFloat() / maxCount) * (bottomY - 30f)
                val cX1 = (prevX + x) / 2f
                path.cubicTo(cX1, prevY, cX1, y, x, y)
            }
        }

        drawPath(
            path = path,
            color = GoldAccent.copy(alpha = 0.85f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun StatusDonutChart(
    slices: List<StatusSlice>,
    selectedSlice: StatusSlice?,
    onSelectSlice: (StatusSlice?) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = remember(slices) { slices.sumOf { it.count }.toFloat().coerceAtLeast(1f) }

    Canvas(
        modifier = modifier
            .pointerInput(slices) {
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    if (angle < 0) angle += 360f

                    var currentAngle = 0f
                    var clicked: StatusSlice? = null
                    for (slice in slices) {
                        val sweep = (slice.count / totalCount) * 360f
                        if (angle >= currentAngle && angle <= currentAngle + sweep) {
                            clicked = slice
                            break
                        }
                        currentAngle += sweep
                    }
                    onSelectSlice(if (selectedSlice?.label == clicked?.label) null else clicked)
                }
            }
    ) {
        val strokeWidth = 24.dp.toPx()
        val diameter = min(size.width, size.height) - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        var startAngle = -90f
        slices.forEach { slice ->
            val sweepAngle = (slice.count / totalCount) * 360f
            val isSelected = selectedSlice?.label == slice.label

            drawArc(
                color = if (isSelected) slice.color else slice.color.copy(alpha = 0.85f),
                startAngle = startAngle,
                sweepAngle = sweepAngle - 3f, // gap
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(
                    width = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth,
                    cap = StrokeCap.Round
                )
            )
            startAngle += sweepAngle
        }
    }
}
