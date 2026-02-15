/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.feature.loan.loanTransaction

import androidclient.feature.loan.generated.resources.Res
import androidclient.feature.loan.generated.resources.feature_loan_break_down
import androidclient.feature.loan.generated.resources.feature_loan_id
import androidclient.feature.loan.generated.resources.feature_loan_loan_fees
import androidclient.feature.loan.generated.resources.feature_loan_loan_interest
import androidclient.feature.loan.generated.resources.feature_loan_loan_penalty
import androidclient.feature.loan.generated.resources.feature_loan_loan_transactions
import androidclient.feature.loan.generated.resources.feature_loan_no_transactions
import androidclient.feature.loan.generated.resources.feature_loan_office
import androidclient.feature.loan.generated.resources.feature_loan_principal
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.mifos.core.common.utils.DateHelper
import com.mifos.core.designsystem.component.MifosScaffold
import com.mifos.core.designsystem.component.MifosSweetError
import com.mifos.core.designsystem.icon.MifosIcons
import com.mifos.core.model.objects.account.loan.Transaction
import com.mifos.core.model.objects.account.loan.Type
import com.mifos.core.ui.components.MifosEmptyUi
import com.mifos.core.ui.components.MifosProgressIndicator
import com.mifos.room.entities.accounts.loans.LoanWithAssociationsEntity
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import org.koin.compose.viewmodel.koinViewModel
import template.core.base.designsystem.theme.KptTheme

@Composable
internal fun LoanTransactionsScreen(
    navigateBack: () -> Unit,
    viewModel: LoanTransactionsViewModel = koinViewModel(),
) {
    val uiState by viewModel.loanTransactionsUiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        viewModel.loadLoanTransaction()
    }

    LoanTransactionsScreen(
        uiState = uiState,
        navigateBack = navigateBack,
        onRetry = {
            viewModel.viewModelScope.launch {
                viewModel.loadLoanTransaction()
            }
        },
    )
}

@Composable
internal fun LoanTransactionsScreen(
    uiState: LoanTransactionsUiState,
    navigateBack: () -> Unit,
    onRetry: () -> Unit,
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    MifosScaffold(
        snackbarHostState = snackbarHostState,
        title = stringResource(Res.string.feature_loan_loan_transactions),
        onBackPressed = navigateBack,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {
            when (uiState) {
                is LoanTransactionsUiState.ShowFetchingError -> {
                    MifosSweetError(
                        message = uiState.message,
                        onclick = onRetry,
                    )
                }

                is LoanTransactionsUiState.ShowLoanTransaction -> {
                    if (uiState.loanWithAssociations.transactions.isEmpty()) {
                        MifosEmptyUi(text = stringResource(Res.string.feature_loan_no_transactions))
                    } else {
                        LoanTransactionsContent(uiState.loanWithAssociations.transactions)
                    }
                }

                LoanTransactionsUiState.ShowProgressBar -> {
                    MifosProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun LoanTransactionsContent(
    transactions: List<Transaction>,
) {
    LazyColumn {
        items(transactions) { transaction ->
            LoanTransactionsItemRow(transaction = transaction)
        }
    }
}

@Composable
private fun LoanTransactionsItemRow(transaction: Transaction) {
    val density = LocalDensity.current
    var showDetails by rememberSaveable {
        mutableStateOf(false)
    }
    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.md),
            shape = KptTheme.shapes.extraSmall,
            onClick = { showDetails = !showDetails },
            colors = CardDefaults.cardColors(
                containerColor = KptTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = KptTheme.spacing.sm, vertical = KptTheme.spacing.sm)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (!showDetails) MifosIcons.ArrowDown else MifosIcons.ArrowUp,
                        contentDescription = "",
                        tint = KptTheme.colorScheme.onSurface,
                    )

                    Text(
                        modifier = Modifier
                            .weight(3f)
                            .padding(start = KptTheme.spacing.sm),
                        text = DateHelper.getDateAsString(transaction.date),
                        style = KptTheme.typography.bodyLarge,
                        color = KptTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        modifier = Modifier
                            .weight(3.3f)
                            .padding(start = KptTheme.spacing.sm),
                        text = transaction.type?.value.toString(),
                        style = KptTheme.typography.bodyLarge,
                        color = KptTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        modifier = Modifier
                            .weight(2.7f)
                            .padding(start = KptTheme.spacing.sm),
                        text = transaction.amount.toString(),
                        style = KptTheme.typography.bodyLarge,
                        color = KptTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showDetails,
            enter = slideInVertically {
                with(density) { -40.dp.roundToPx() }
            } + expandVertically(
                expandFrom = Alignment.Top,
            ) + fadeIn(
                initialAlpha = 0.3f,
            ),
            exit = slideOutVertically() + shrinkVertically() + fadeOut(),
        ) {
            LoanTransactionsItemDetailsCard(transaction = transaction)
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = KptTheme.spacing.md))
    }
}

@Composable
private fun LoanTransactionsItemDetailsCard(
    transaction: Transaction,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KptTheme.spacing.md),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(KptTheme.elevation.level1),
        shape = KptTheme.shapes.extraSmall,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.feature_loan_id),
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = transaction.id.toString(),
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.feature_loan_office),
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = transaction.officeName.toString(),
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = KptTheme.spacing.md, bottom = KptTheme.spacing.sm),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(Res.string.feature_loan_break_down),
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(color = KptTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

            Box(
                modifier = Modifier
                    .background(KptTheme.colorScheme.secondaryContainer),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(KptTheme.spacing.xs),
                ) {
                    Text(
                        modifier = Modifier.weight(2.5f),
                        text = stringResource(Res.string.feature_loan_principal),
                        style = KptTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = KptTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Start,
                    )

                    Text(
                        modifier = Modifier.weight(2.5f),
                        text = stringResource(Res.string.feature_loan_loan_interest),
                        style = KptTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = KptTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        modifier = Modifier.weight(2.5f),
                        text = stringResource(Res.string.feature_loan_loan_fees),
                        style = KptTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = KptTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        modifier = Modifier.weight(2.5f),
                        text = stringResource(Res.string.feature_loan_loan_penalty),
                        style = KptTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = KptTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.End,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(KptTheme.spacing.xs),
            ) {
                Text(
                    modifier = Modifier.weight(2.5f),
                    text = transaction.principalPortion.toString(),
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                )

                Text(
                    modifier = Modifier
                        .weight(2.5f)
                        .padding(horizontal = KptTheme.spacing.xs),
                    text = transaction.interestPortion.toString(),
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Text(
                    modifier = Modifier.weight(2.5f),
                    text = transaction.feeChargesPortion.toString(),
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Text(
                    modifier = Modifier
                        .weight(2.5f)
                        .padding(start = KptTheme.spacing.xs),
                    text = transaction.penaltyChargesPortion.toString(),
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

private class LoanTransactionsPreviewProvider : PreviewParameterProvider<LoanTransactionsUiState> {
    val transaction =
        Transaction(
            id = 23,
            officeName = "Main office",
            date = listOf(2024, 6, 1),
            principalPortion = 121.2,
            penaltyChargesPortion = 32323.232,
            overpaymentPortion = 23232.23,
            feeChargesPortion = 323.3,
            interestPortion = 232.3,
            type = Type(
                value = "Repayment",
            ),
        )

    override val values: Sequence<LoanTransactionsUiState>
        get() = sequenceOf(
            LoanTransactionsUiState.ShowFetchingError(""),
            LoanTransactionsUiState.ShowProgressBar,
            LoanTransactionsUiState.ShowLoanTransaction(
                LoanWithAssociationsEntity(
                    transactions = listOf(
                        transaction,
                        transaction,
                        transaction,
                        transaction,
                        transaction,
                        transaction,
                        transaction,
                        transaction,
                        transaction,
                        transaction,
                    ),
                ),
            ),
        )
}

@Composable
@Preview
private fun PreviewLoanTransactions(
    @PreviewParameter(LoanTransactionsPreviewProvider::class) loanTransactionsUiState: LoanTransactionsUiState,
) {
    LoanTransactionsScreen(
        uiState = loanTransactionsUiState,
        navigateBack = {},
        onRetry = {},
    )
}
