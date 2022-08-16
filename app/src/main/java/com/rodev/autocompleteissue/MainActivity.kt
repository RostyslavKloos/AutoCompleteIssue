package com.rodev.autocompleteissue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.rodev.autocompleteissue.ui.theme.AutoCompleteIssueTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoCompleteIssueTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        AutocompleteDropdownWithFilteringInside(countries = provideCountries())
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun AutocompleteDropdownWithFilteringInside(
    modifier: Modifier = Modifier,
    countries: List<String>,
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val defaultLocale = remember { Locale.getDefault() }
    val lowerCaseSearchQuery = remember(query) { query.lowercase(defaultLocale) }
    var suggestions by remember { mutableStateOf(countries) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardOptions = remember {
        KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
    }
    val keyboardActions = remember {
        KeyboardActions(
            onNext = {
                expanded = false
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        )
    }

    LaunchedEffect(lowerCaseSearchQuery) {
        withContext(Dispatchers.Default) {
            suggestions = when (lowerCaseSearchQuery.isEmpty()) {
                true -> countries
                false -> {
                    countries.filter { country ->
                        country.lowercase(defaultLocale)
                            .startsWith(lowerCaseSearchQuery) && country != query
                    }
                }
            }
            expanded = true
        }
    }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = query,
            onValueChange = {
                query = it
            },
            label = { Text("Label") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
        if (suggestions.isNotEmpty() && expanded) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                suggestions.forEach { selectionOption ->
                    DropdownMenuItem(
                        onClick = {
                            query = selectionOption
                            expanded = false
                        }
                    ) {
                        Text(text = selectionOption)
                    }
                }
            }
        }
    }
}

private fun provideCountries(): List<String> {
    val locales = Locale.getAvailableLocales()
    val countries = ArrayList<String>()
    for (locale in locales) {
        val country: String = locale.displayCountry
        if (country.trim { it <= ' ' }.isNotEmpty() && !countries.contains(country)) {
            countries.add(country)
        }
    }
    countries.sort()

    return countries
}
