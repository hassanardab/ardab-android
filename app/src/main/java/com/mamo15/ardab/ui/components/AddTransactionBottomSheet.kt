package com.mamo15.ardab.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mamo15.ardab.R
import com.mamo15.ardab.data.PaymentMethod
import com.mamo15.ardab.data.Transaction
import com.mamo15.ardab.data.TransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    projectId: String,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    // State
    var selectedType by remember { mutableStateOf(TransactionType.INCOME) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }

    var typeExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.now().toEpochDay() * 24 * 60 * 60 * 1000
    )

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val isAmountValid = amount.toDoubleOrNull()?.let { it > 0 } == true

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---- Header ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.add_transaction),
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }

            Divider()

            // ---- Date (custom row) ----
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.date_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.pick_date)
                        )
                    }
                }
            }

            // ---- Transaction Type (dropdown) ----
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = !typeExpanded }
            ) {
                OutlinedTextField(
                    value = stringResource(selectedType.getDisplayNameRes()),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.transaction_type)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    TransactionType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(stringResource(type.getDisplayNameRes())) },
                            onClick = {
                                selectedType = type
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            // ---- Payment Method (chips) ----
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.payment_method),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PaymentMethod.values().forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(stringResource(method.getDisplayNameRes())) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ---- Amount (with currency prefix & clear) ----
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.amount)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (amount.isNotEmpty()) {
                        IconButton(onClick = { amount = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = amount.isNotEmpty() && !isAmountValid,
                supportingText = {
                    if (amount.isNotEmpty() && !isAmountValid) {
                        Text(stringResource(R.string.enter_valid_amount))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            // ---- Description ----
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            // ---- Action Buttons ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                        if (parsedAmount > 0) {
                            val newTransaction = Transaction(
                                id = UUID.randomUUID().toString(),
                                projectId = projectId,
                                type = selectedType,
                                method = selectedMethod,
                                amount = parsedAmount,
                                description = description,
                                date = date
                            )
                            onSave(newTransaction)
                            onDismiss()
                        }
                    },
                    enabled = isAmountValid,
                    modifier = Modifier.weight(2f)
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }

    // ---- Date Picker Dialog ----
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            date = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ---- Helpers ----
fun TransactionType.getDisplayNameRes(): Int = when (this) {
    TransactionType.INCOME -> R.string.transaction_type_income
    TransactionType.EXPENSE -> R.string.transaction_type_expense
    TransactionType.BILL -> R.string.transaction_type_bill
    TransactionType.PAYROLL -> R.string.transaction_type_payroll
    TransactionType.LOAN -> R.string.transaction_type_loan
    TransactionType.TRANSFER -> R.string.transaction_type_transfer
}

fun PaymentMethod.getDisplayNameRes(): Int = when (this) {
    PaymentMethod.CASH -> R.string.payment_method_cash
    PaymentMethod.BANK -> R.string.payment_method_bank
    PaymentMethod.CREDIT -> R.string.payment_method_credit
}