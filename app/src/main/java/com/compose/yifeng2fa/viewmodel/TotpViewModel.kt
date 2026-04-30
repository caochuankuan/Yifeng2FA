package com.compose.yifeng2fa.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.compose.yifeng2fa.data.AppDatabase
import com.compose.yifeng2fa.data.TotpEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class SortOrder {
    DATE_ASC, DATE_DESC, ISSUER_ASC
}

class TotpViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).totpDao()

    private val _accounts = MutableStateFlow<List<TotpEntity>>(emptyList())
    val accounts: StateFlow<List<TotpEntity>> = _accounts

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _showCodes = MutableStateFlow(true)
    val showCodes: StateFlow<Boolean> = _showCodes

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _sortOrder.collectLatest { order ->
                val flow = when (order) {
                    SortOrder.DATE_ASC -> dao.getAllAsc()
                    SortOrder.DATE_DESC -> dao.getAllDesc()
                    SortOrder.ISSUER_ASC -> dao.getAllByIssuer()
                }
                flow.collectLatest {
                    _accounts.value = it
                }
            }
        }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun toggleShowCodes() {
        _showCodes.value = !_showCodes.value
    }

    fun getAccountById(id: Long) = dao.getById(id)

    fun addAccount(
        issuer: String,
        accountName: String,
        secret: String,
        algorithm: String = "SHA1",
        digits: Int = 6,
        period: Int = 30
    ) {
        viewModelScope.launch {
            dao.insert(
                TotpEntity(
                    issuer = issuer,
                    accountName = accountName,
                    secret = secret.uppercase().replace(" ", ""),
                    algorithm = algorithm,
                    digits = digits,
                    period = period
                )
            )
        }
    }

    fun deleteAccount(entity: TotpEntity) {
        viewModelScope.launch {
            dao.delete(entity)
        }
    }

    fun updateAccount(entity: TotpEntity) {
        viewModelScope.launch {
            dao.update(entity)
        }
    }

    fun importAccounts(importedAccounts: List<TotpEntity>) {
        viewModelScope.launch {
            importedAccounts.forEach { account ->
                dao.insert(account.copy(id = 0)) // Ensure new IDs are generated
            }
        }
    }
}
