package com.mamo15.ardab.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mamo15.ardab.R
import com.mamo15.ardab.data.ReportType

// Extension to get icon for each report type
fun ReportType.getIcon(): ImageVector = when (this) {
    ReportType.FINANCIAL -> Icons.Default.AccountBalance
    ReportType.EMPLOYEE -> Icons.Default.People
    ReportType.VENDOR -> Icons.Default.LocalShipping
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    var selectedReportType by remember { mutableStateOf(ReportType.FINANCIAL) }

    // Mock data with extra details
    val projects = listOf(
        ReportItem("Project Alpha", 34, "Last updated: 2 days ago"),
        ReportItem("Project Beta", 12, "Last updated: 1 week ago"),
        ReportItem("Project Delta", 8, "Last updated: 3 days ago")
    )
    val employees = listOf(
        ReportItem("Sarah Jenkins", 5, "Total advances: 2"),
        ReportItem("Michael Scott", 3, "Total advances: 0"),
        ReportItem("Elena Rodriguez", 7, "Total advances: 1")
    )
    val vendors = listOf(
        ReportItem("Tech Supplies Co.", 9, "Last invoice: 15/05"),
        ReportItem("Global Logistics", 4, "Last invoice: 10/05"),
        ReportItem("Office Depot", 6, "Last invoice: 12/05")
    )

    val items = when (selectedReportType) {
        ReportType.FINANCIAL -> projects
        ReportType.EMPLOYEE -> employees
        ReportType.VENDOR -> vendors
    }

    // Map ReportType to display name resource IDs
    val typeDisplayNameRes = when (selectedReportType) {
        ReportType.FINANCIAL -> R.string.report_type_financial
        ReportType.EMPLOYEE -> R.string.report_type_employee
        ReportType.VENDOR -> R.string.report_type_vendor
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reports_title)) },
                actions = {
                    IconButton(onClick = { /* open filter/sort */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.filter))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* generate all reports */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = stringResource(R.string.generate_all))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Report type chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ReportType.values().forEach { type ->
                    val labelRes = when (type) {
                        ReportType.FINANCIAL -> R.string.report_type_financial
                        ReportType.EMPLOYEE -> R.string.report_type_employee
                        ReportType.VENDOR -> R.string.report_type_vendor
                    }
                    FilterChip(
                        selected = selectedReportType == type,
                        onClick = { selectedReportType = type },
                        label = { Text(stringResource(labelRes)) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Count label
            Text(
                text = stringResource(R.string.reports_items_count, items.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // List of report items
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Report,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.reports_empty_title, stringResource(typeDisplayNameRes)),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items) { item ->
                        ReportItemCard(
                            title = item.title,
                            subtitle = item.subtitle,
                            icon = selectedReportType.getIcon(),
                            onGenerateClick = {
                                // Handle report generation
                            }
                        )
                    }
                }
            }
        }
    }
}

data class ReportItem(
    val title: String,
    val count: Int,
    val subtitle: String
)

@Composable
fun ReportItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onGenerateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon with background circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onGenerateClick,
                modifier = Modifier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.generate))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewReportsScreen() {
    MaterialTheme {
        ReportsScreen()
    }
}