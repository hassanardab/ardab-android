package com.mamo15.ardab.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mamo15.ardab.R
import com.mamo15.ardab.data.PaymentMethod
import com.mamo15.ardab.data.TransactionType
import com.mamo15.ardab.data.entity.TransactionEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Helper: format raw digits with commas (keeps Latin digits)
fun formatNumberWithCommas(raw: String): String {
    if (raw.isEmpty()) return ""
    val parts = raw.split('.')
    val integerPart = parts[0]
    val decimalPart = if (parts.size > 1) parts[1] else ""
    val formattedInteger = integerPart.reversed().chunked(3).joinToString(",").reversed()
    return if (decimalPart.isNotEmpty()) "$formattedInteger.$decimalPart" else formattedInteger
}

fun commaVisualTransformation(): VisualTransformation = VisualTransformation { text ->
    val raw = text.text
    val formatted = formatNumberWithCommas(raw)
    TransformedText(
        text = AnnotatedString(formatted),
        offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val rawPart = raw.substring(0, offset)
                return formatNumberWithCommas(rawPart).length
            }
            override fun transformedToOriginal(offset: Int): Int {
                val formattedPart = formatted.substring(0, offset)
                return formattedPart.replace(",", "").length
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddTransactionBottomSheet(
    projectId: String,
    onSave: (TransactionEntity) -> Unit,
    onDismiss: () -> Unit,
    initialTransaction: TransactionEntity? = null   // null = add mode
) {
    val isEditMode = initialTransaction != null

    // Date handling with Calendar (API 23 compatible)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val calendar = Calendar.getInstance()

    var selectedType by remember { mutableStateOf(initialTransaction?.type ?: TransactionType.INCOME) }
    var selectedMethod by remember { mutableStateOf(initialTransaction?.paymentMethod ?: PaymentMethod.CASH) }

    // For transfers: source and destination payment methods
    var sourceMethod by remember { mutableStateOf(PaymentMethod.BANK) }
    var destMethod by remember { mutableStateOf(PaymentMethod.CASH) }

    var rawAmount by remember { mutableStateOf(if (isEditMode) initialTransaction.amount.toString() else "") }
    var description by remember { mutableStateOf(initialTransaction?.description ?: "") }
    var date by remember {
        mutableStateOf(
            if (isEditMode) {
                dateFormat.format(initialTransaction.date)
            } else {
                dateFormat.format(calendar.time)
            }
        )
    }

    var typeExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            dateFormat.parse(date)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isAmountValid = rawAmount.toDoubleOrNull()?.let { it > 0 } == true
    val coroutineScope = rememberCoroutineScope()

    // Focus management
    val focusManager = LocalFocusManager.current
    val amountFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        amountFocusRequester.requestFocus()
    }

    // When type changes to TRANSFER, reset source/dest to sensible defaults if needed
    LaunchedEffect(selectedType) {
        if (selectedType == TransactionType.TRANSFER) {
            if (sourceMethod == destMethod) {
                // Ensure they are different
                destMethod = if (sourceMethod == PaymentMethod.CASH) PaymentMethod.BANK else PaymentMethod.CASH
            }
        }
    }

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditMode) "Edit Transaction" else stringResource(R.string.add_transaction),
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                }
            }

            HorizontalDivider()

            // Date picker
            OutlinedCard(
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                        Text(text = date, style = MaterialTheme.typography.bodyLarge)
                        Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.pick_date))
                    }
                }
            }

            // Transaction type dropdown
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
                    TransactionType.entries.forEach { type ->
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

            // Payment method selection – different for transfer vs normal
            if (selectedType == TransactionType.TRANSFER) {
                // Transfer: show source and destination
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Source
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "From",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PaymentMethod.entries.forEach { method ->
                                FilterChip(
                                    selected = sourceMethod == method,
                                    onClick = {
                                        if (method != destMethod) sourceMethod = method
                                    },
                                    label = { Text(stringResource(method.getDisplayNameRes())) },
                                    modifier = Modifier.weight(1f),
                                    enabled = method != destMethod
                                )
                            }
                        }
                    }
                    // Destination
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "To",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PaymentMethod.entries.forEach { method ->
                                FilterChip(
                                    selected = destMethod == method,
                                    onClick = {
                                        if (method != sourceMethod) destMethod = method
                                    },
                                    label = { Text(stringResource(method.getDisplayNameRes())) },
                                    modifier = Modifier.weight(1f),
                                    enabled = method != sourceMethod
                                )
                            }
                        }
                    }
                }
            } else {
                // Normal: single payment method chips
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
                        PaymentMethod.entries.forEach { method ->
                            FilterChip(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method },
                                label = { Text(stringResource(method.getDisplayNameRes())) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Amount field with Next IME action
            OutlinedTextField(
                value = rawAmount,
                onValueChange = { newRaw ->
                    var filtered = newRaw.filter { it.isDigit() || it == '.' }
                    val dotIndex = filtered.indexOfFirst { it == '.' }
                    if (dotIndex != -1) {
                        filtered = filtered.take(dotIndex + 1) + filtered.substring(dotIndex + 1).replace(".", "")
                    }
                    rawAmount = filtered
                },
                label = { Text(stringResource(R.string.amount)) },
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                trailingIcon = {
                    if (rawAmount.isNotEmpty()) {
                        IconButton(onClick = { rawAmount = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear))
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { descriptionFocusRequester.requestFocus() }
                ),
                isError = rawAmount.isNotEmpty() && !isAmountValid,
                supportingText = {
                    if (rawAmount.isNotEmpty() && !isAmountValid) {
                        Text(stringResource(R.string.enter_valid_amount))
                    }
                },
                visualTransformation = commaVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(amountFocusRequester),
                shape = MaterialTheme.shapes.medium
            )

            // Description field with Done IME action
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (isAmountValid) {
                            saveTransaction(
                                parsedAmount = rawAmount.toDoubleOrNull() ?: 0.0,
                                dateFormat = dateFormat,
                                date = date,
                                projectId = projectId,
                                selectedType = selectedType,
                                selectedMethod = selectedMethod,
                                sourceMethod = sourceMethod,
                                destMethod = destMethod,
                                description = description,
                                initialTransaction = initialTransaction,
                                onSave = onSave,
                                onDismiss = onDismiss,
                                focusManager = focusManager,
                                coroutineScope = coroutineScope
                            )
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(descriptionFocusRequester),
                shape = MaterialTheme.shapes.medium
            )

            // Action buttons
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
                        val parsedAmount = rawAmount.toDoubleOrNull() ?: 0.0
                        if (parsedAmount > 0) {
                            saveTransaction(
                                parsedAmount = parsedAmount,
                                dateFormat = dateFormat,
                                date = date,
                                projectId = projectId,
                                selectedType = selectedType,
                                selectedMethod = selectedMethod,
                                sourceMethod = sourceMethod,
                                destMethod = destMethod,
                                description = description,
                                initialTransaction = initialTransaction,
                                onSave = onSave,
                                onDismiss = onDismiss,
                                focusManager = focusManager,
                                coroutineScope = coroutineScope
                            )
                        }
                    },
                    enabled = isAmountValid,
                    modifier = Modifier.weight(2f)
                ) {
                    Text(if (isEditMode) "Update" else stringResource(R.string.save))
                }
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            date = dateFormat.format(millis)
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

// Helper function to handle saving (single transaction or transfer pair)
private fun saveTransaction(
    parsedAmount: Double,
    focusManager: FocusManager,
    dateFormat: SimpleDateFormat,
    date: String,
    projectId: String,
    selectedType: TransactionType,
    selectedMethod: PaymentMethod,
    sourceMethod: PaymentMethod,
    destMethod: PaymentMethod,
    description: String,
    initialTransaction: TransactionEntity?,
    onSave: (TransactionEntity) -> Unit,
    onDismiss: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    if (parsedAmount <= 0) return

    val dateMillis = try {
        dateFormat.parse(date)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    if (selectedType == TransactionType.TRANSFER) {
        // Create two transactions: expense from source, income to destination
        val expenseTransaction = TransactionEntity(
            id = "", // new id
            projectId = projectId,
            amount = parsedAmount,
            type = TransactionType.EXPENSE,
            paymentMethod = sourceMethod,
            description = "Transfer to ${destMethod.name} - $description",
            date = dateMillis
        )
        val incomeTransaction = TransactionEntity(
            id = "",
            projectId = projectId,
            amount = parsedAmount,
            type = TransactionType.INCOME,
            paymentMethod = destMethod,
            description = "Transfer from ${sourceMethod.name} - $description",
            date = dateMillis
        )

        coroutineScope.launch {
            onSave(expenseTransaction)
            onSave(incomeTransaction)
            onDismiss()
            focusManager.clearFocus()
        }
    } else {
        // Normal single transaction
        val transaction = TransactionEntity(
            id = initialTransaction?.id ?: "",
            projectId = projectId,
            amount = parsedAmount,
            type = selectedType,
            paymentMethod = selectedMethod,
            description = description,
            date = dateMillis
        )
        coroutineScope.launch {
            onSave(transaction)
            onDismiss()
            focusManager.clearFocus()
        }
    }
}

// Helper functions to get display names
fun TransactionType.getDisplayNameRes(): Int = when (this) {
    TransactionType.INCOME -> R.string.transaction_type_income
    TransactionType.EXPENSE -> R.string.transaction_type_expense
    TransactionType.BILL -> R.string.transaction_type_bill
    TransactionType.PAYROLL -> R.string.transaction_type_payroll
    TransactionType.LOAN -> R.string.transaction_type_loan
    TransactionType.LOAN_REPAYMENT -> R.string.transaction_type_loan_repayment
    TransactionType.TRANSFER -> R.string.transaction_type_transfer
}

fun PaymentMethod.getDisplayNameRes(): Int = when (this) {
    PaymentMethod.CASH -> R.string.payment_method_cash
    PaymentMethod.BANK -> R.string.payment_method_bank
    PaymentMethod.CREDIT -> R.string.payment_method_credit
}