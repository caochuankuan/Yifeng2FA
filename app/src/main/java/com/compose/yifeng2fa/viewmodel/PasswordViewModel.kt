package com.compose.yifeng2fa.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.compose.yifeng2fa.data.AppDatabase
import com.compose.yifeng2fa.data.PasswordEntity
import com.compose.yifeng2fa.data.StrongPasswordHistoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).passwordDao()
    private val historyDao = AppDatabase.getDatabase(application).strongPasswordHistoryDao()
    private val prefs = application.getSharedPreferences("yifeng2fa_settings", Context.MODE_PRIVATE)

    private val _accounts = MutableStateFlow<List<PasswordEntity>>(emptyList())
    val accounts: StateFlow<List<PasswordEntity>> = _accounts

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _showPassword = MutableStateFlow(prefs.getBoolean("password_show_password", true))
    val showPassword: StateFlow<Boolean> = _showPassword

    private val _strongPasswordHistory = MutableStateFlow<List<StrongPasswordHistoryEntity>>(emptyList())
    val strongPasswordHistory: StateFlow<List<StrongPasswordHistoryEntity>> = _strongPasswordHistory

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
        viewModelScope.launch {
            historyDao.getAll().collectLatest {
                _strongPasswordHistory.value = it
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

    fun addStrongPasswordHistory(
        password: String,
        keyword1: String,
        keyword2: String,
        length: Int,
        useUppercase: Boolean,
        useLowercase: Boolean,
        useNumbers: Boolean,
        useSymbols: Boolean
    ) {
        viewModelScope.launch {
            historyDao.insert(
                StrongPasswordHistoryEntity(
                    password = password,
                    keyword1 = keyword1,
                    keyword2 = keyword2,
                    length = length,
                    useUppercase = useUppercase,
                    useLowercase = useLowercase,
                    useNumbers = useNumbers,
                    useSymbols = useSymbols,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteStrongPasswordHistoryByIds(ids: List<Long>) {
        viewModelScope.launch {
            historyDao.deleteByIds(ids)
        }
    }

    fun generateStrongPassword(
        keyword1: String,
        keyword2: String,
        length: Int,
        useUppercase: Boolean,
        useLowercase: Boolean,
        useNumbers: Boolean,
        useSymbols: Boolean
    ): String {
        val charPool = buildString {
            if (useLowercase) append("abcdefghijklmnopqrstuvwxyz")
            if (useUppercase) append("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
            if (useNumbers) append("0123456789")
            if (useSymbols) append("!@#$%&*")
        }

        if (charPool.isEmpty()) return ""

        val random = java.security.SecureRandom()

        val kw1 = keyword1.trim()
        val kw2 = keyword2.trim()
        val keywords = listOf(kw1, kw2).filter { it.isNotEmpty() }
        val keywordsLength = keywords.sumOf { it.length }

        val minSeparators = if (keywords.size > 1) keywords.size - 1 else 0
        val totalLength = maxOf(length, keywordsLength + minSeparators)

        val passwordChars = CharArray(totalLength) { charPool[random.nextInt(charPool.length)] }

        val keywordIndices = mutableSetOf<Int>()
        if (keywords.isNotEmpty()) {
            val availableSlots = (0 until totalLength).toMutableList()

            keywords.forEach { word ->
                val wordLen = word.length
                val maxStart = availableSlots.size - wordLen
                if (maxStart >= 0) {
                    val slotIndex = random.nextInt(maxStart + 1)
                    val startPos = availableSlots[slotIndex]

                    for (i in word.indices) {
                        val pos = startPos + i
                        passwordChars[pos] = word[i]
                        keywordIndices.add(pos)
                    }

                    for (i in 0 until wordLen) {
                        availableSlots.remove(startPos + i)
                    }
                }
            }
        }

        val requiredChars = mutableListOf<Char>()
        if (useLowercase) {
            val lowers = "abcdefghijklmnopqrstuvwxyz".toList()
            requiredChars.add(lowers[random.nextInt(lowers.size)])
        }
        if (useUppercase) {
            val uppers = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toList()
            requiredChars.add(uppers[random.nextInt(uppers.size)])
        }
        if (useNumbers) {
            val nums = "0123456789".toList()
            requiredChars.add(nums[random.nextInt(nums.size)])
        }
        if (useSymbols) {
            val syms = "!@#$%&*".toList()
            requiredChars.add(syms[random.nextInt(syms.size)])
        }

        val nonKeywordIndices = mutableListOf<Int>()
        for (i in passwordChars.indices) {
            if (i !in keywordIndices) {
                nonKeywordIndices.add(i)
            }
        }

        requiredChars.forEachIndexed { index, char ->
            if (index < nonKeywordIndices.size) {
                passwordChars[nonKeywordIndices[index]] = char
            }
        }

        for (i in nonKeywordIndices.size - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val idxI = nonKeywordIndices[i]
            val idxJ = nonKeywordIndices[j]
            val temp = passwordChars[idxI]
            passwordChars[idxI] = passwordChars[idxJ]
            passwordChars[idxJ] = temp
        }

        return String(passwordChars)
    }
}
