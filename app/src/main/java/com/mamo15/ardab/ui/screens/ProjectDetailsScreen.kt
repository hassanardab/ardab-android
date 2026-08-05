package com.mamo15.ardab.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mamo15.ardab.R
import com.mamo15.ardab.ui.components.AddTransactionBottomSheet
import com.mamo15.ardab.viewmodel.ProjectDetailsViewModel
import com.mamo15.ardab.viewmodel.ProjectDetailsViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
// Add a new composable to show balances, and display it in the Scaffold.

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
    val balances by viewModel.balances.collectAsState()  // new
    val coroutineScope = rememberCoroutineScope()
    var showAddTransaction by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name ?: stringResource(R.string.project_details)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTransaction = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_transaction))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Balance Overview",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Cash: $${"%.2f".format(balances.cash)}")
                        Text(text = "Bank: $${"%.2f".format(balances.bank)}")
                        Text(text = "Loan: $${"%.2f".format(balances.loan)}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction list
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_transactions_yet))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionItem(transaction)
                    }
                }
            }
        }
    }

    if (showAddTransaction) {
        AddTransactionBottomSheet(
            projectId = projectId,
            onSave = { transaction ->
                coroutineScope.launch {
                    viewModel.addTransaction(transaction)
                }
            },
            onDismiss = { showAddTransaction = false }
        )
    }
}

@Composable
fun TransactionItem(transaction: com.mamo15.ardab.data.entity.TransactionEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${transaction.amount} ${transaction.paymentMethod.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = transaction.type.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Text(
                    text = "Date: ${java.time.Instant.ofEpochMilli(transaction.date).atZone(java.time.ZoneId.systemDefault()).toLocalDate()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}