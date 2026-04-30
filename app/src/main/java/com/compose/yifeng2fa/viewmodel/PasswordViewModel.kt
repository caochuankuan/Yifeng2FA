package com.compose.yifeng2fa.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.compose.yifeng2fa.data.AppDatabase
import com.compose.yifeng2fa.data.PasswordEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).passwordDao()
    private val prefs = application.getSharedPreferences("yifeng2fa_settings", Context.MODE_PRIVATE)

    private val _accounts = MutableStateFlow<List<PasswordEntity>>(emptyList())
    val accounts: StateFlow<List<PasswordEntity>> = _accounts

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _showPassword = MutableStateFlow(prefs.getBoolean("password_show_password", true))
    val showPassword: StateFlow<Boolean> = _showPassword

    init {
        viewModelScope.launch {
            _searchQuery.collectLatest { query ->
                if (query.isBlank()) {
                    dao.getAll().collectLatest {
                        _accounts.value = it
                    }
                } else {
                    dao.search(query).collectLatest {
                        _accounts.value = it
                    }
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleShowPassword() {
        val newValue = !_showPassword.value
        _showPassword.value = newValue
        prefs.edit().putBoolean("password_show_password", newValue).apply()
    }

    fun getAccountById(id: Long) = dao.getById(id)

    fun addAccount(
        websiteName: String,
        websiteUrl: String,
        account: String,
        password: String,
        note: String,
        category: String = ""
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            dao.insert(
                PasswordEntity(
                    websiteName = websiteName,
                    websiteUrl = websiteUrl,
                    account = account,
                    password = password,
                    note = note,
                    category = category,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    fun updateAccount(entity: PasswordEntity) {
        viewModelScope.launch {
            dao.update(entity.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteAccount(entity: PasswordEntity) {
        viewModelScope.launch {
            dao.delete(entity)
        }
    }

    fun deleteAccountsByIds(ids: List<Long>) {
        viewModelScope.launch {
            dao.deleteByIds(ids)
        }
    }

    fun importAccounts(importedAccounts: List<PasswordEntity>) {
        viewModelScope.launch {
            importedAccounts.forEach { account ->
                dao.insert(account.copy(id = 0))
            }
        }
    }
}
