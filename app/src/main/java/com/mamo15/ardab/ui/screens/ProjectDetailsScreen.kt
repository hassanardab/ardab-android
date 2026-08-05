package com.mamo15.ardab.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mamo15.ardab.R
import com.mamo15.ardab.data.TransactionType
import com.mamo15.ardab.data.entity.TransactionEntity
import com.mamo15.ardab.ui.components.AddTransactionBottomSheet
import com.mamo15.ardab.util.formatCurrency
import com.mamo15.ardab.viewmodel.ProjectDetailsViewModel
import com.mamo15.ardab.viewmodel.ProjectDetailsViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    projectId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: ProjectDetailsViewModel = viewModel(
        factory = ProjectDetailsViewModelFactory(context, projectId)
    )

    val project by viewModel.project.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val balances by viewModel.balances.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // State for bottom sheet
    var showAddBottomSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    // Scroll behavior for a modern collapsing TopAppBar
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = project?.name ?: stringResource(R.string.project_details),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddBottomSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_transaction)) },
                text = { Text(stringResource(R.string.add_transaction)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Balance Card with premium gradient background
            BalanceOverviewCard(balances)

//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Transaction list heading
//            Text(
//                text = "Recent Transactions",
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.onBackground,
//                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
//            )

            // Transactions state handling
            if (transactions.isEmpty()) {
                EmptyStateView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 88.dp // Space for FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = transactions,
                        key = { it.id } // Requires your TransactionEntity to have an 'id' field for animation
                    ) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onLongClick = { editingTransaction = transaction },
                            modifier = Modifier.animateItem() // Smooth insertion/deletion animations
                        )
                    }
                }
            }
        }
    }

    // Bottom sheet for adding / editing
    if (showAddBottomSheet) {
        AddTransactionBottomSheet(
            projectId = projectId,
            onSave = { transaction ->
                coroutineScope.launch {
                    viewModel.addTransaction(transaction)
                    showAddBottomSheet = false
                }
            },
            onDismiss = { showAddBottomSheet = false }
        )
    }

    editingTransaction?.let { transaction ->
        AddTransactionBottomSheet(
            projectId = projectId,
            initialTransaction = transaction,
            onSave = { updatedTransaction ->
                coroutineScope.launch {
                    viewModel.updateTransaction(updatedTransaction)
                    editingTransaction = null
                }
            },
            onDismiss = { editingTransaction = null }
        )
    }
}

@Composable
private fun BalanceOverviewCard(balances: ProjectDetailsViewModel.ProjectBalances) {    // Note: Assuming `balances` has `.cash`, `.bank`, and `.loan` properties

    // Creating a beautiful gradient brush for the card
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp), // More modern, rounded corners
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Balance Overview",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f) // Softer white for subtitles
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // We extract balances mapping assuming the data class properties
                    // Please adjust `balances.cash` etc., if your variable names differ.
                    // (Using Kotlin reflection-like syntax here just as a placeholder since type is hidden in prompt)

                    BalanceItem(
                        label = "Cash",
                        amount = balances.cash,
                        icon = Icons.Default.Money,
                        color = Color.White
                    )
                    BalanceItem(
                        label = "Bank",
                        amount = balances.bank,
                        icon = Icons.Default.AccountBalance,
                        color = Color.White
                    )
                    BalanceItem(
                        label = "Loan",
                        amount = balances.loan,
                        icon = Icons.Default.CreditCard,
                        color = Color(0xFFFFCDD2) // Light red/pink to signify debt while staying visible on gradient
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceItem(label: String, amount: Double, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun EmptyStateView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_transactions_yet),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap the + button to add one",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: TransactionEntity,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME

    // Semantic colors tailored for light/dark modes
    val amountColor = when (transaction.type) {
        TransactionType.INCOME -> Color(0xFF388E3C) // Crisp Green
        TransactionType.LOAN -> Color(0xFF1976D2)   // Crisp Blue
        else -> MaterialTheme.colorScheme.error     // Material Red
    }

    // Determine icon based on transaction type
    val iconVector = when (transaction.type) {
        TransactionType.INCOME -> Icons.Default.TrendingUp
        TransactionType.EXPENSE -> Icons.Default.TrendingDown
        TransactionType.BILL -> Icons.Default.Receipt
        TransactionType.PAYROLL -> Icons.Default.Payments
        TransactionType.LOAN -> Icons.Default.CreditCard
        TransactionType.LOAN_REPAYMENT -> Icons.Default.Payment
        TransactionType.TRANSFER -> Icons.Default.SwapHoriz
    }

    // Format Date beautifully (e.g. "Oct 24, 2023")
    val formattedDate = remember(transaction.date) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val date = java.time.Instant.ofEpochMilli(transaction.date)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")
            date.format(formatter)
        } else {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(transaction.date))
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { /* Optional: show transaction details */ },
                onLongClick = onLongClick
            ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp, // Subtle elevation instead of standard heavy card shadow
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = amountColor.copy(alpha = 0.1f), // Tinted background based on type
                        shape = CircleShape // Modern circular icon background
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = amountColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.type.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Combining Description and Date nicely
                Text(
                    text = "${transaction.description} • $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount and Payment Method Column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+" else ""}${formatCurrency(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.paymentMethod.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}