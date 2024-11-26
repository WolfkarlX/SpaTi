package com.example.spaTi.ui.spahistoricalappointments

import com.example.spaTi.R
import java.util.regex.Pattern

data class ValidationResult(
    val isValid: Boolean,
    val errorMessageResId: Int? = null
)

class ReportValidator(
    private val minLength: Int = 10,
    private val maxLength: Int = 100,
    private val allowedCharactersPattern: Pattern = Pattern.compile("^[a-zA-Z0-9\\s.,!?'-]+$")
) {
    sealed class ValidationRule {
        data class MinLength(val length: Int) : ValidationRule()
        data class MaxLength(val length: Int) : ValidationRule()
        data object NonEmpty : ValidationRule()
        data object ValidCharacters : ValidationRule()
        data object NonAggressive : ValidationRule()
    }

    fun validate(text: String, rules: Set<ValidationRule> = defaultRules()): ValidationResult {
        if (text.isEmpty() && rules.contains(ValidationRule.NonEmpty)) {
            return ValidationResult(false, R.string.enter_reportreason)
        }

        rules.forEach { rule ->
            when (rule) {
                is ValidationRule.MinLength -> {
                    if (text.length < rule.length) {
                        return ValidationResult(false, R.string.reportreason_short)
                    }
                }
                is ValidationRule.MaxLength -> {
                    if (text.length > rule.length) {
                        return ValidationResult(false, R.string.reportreason_long)
                    }
                }
                ValidationRule.ValidCharacters -> {
                    if (!allowedCharactersPattern.matcher(text).matches()) {
                        return ValidationResult(false, R.string.reportreason_hasspecialcharacters)
                    }
                }
                ValidationRule.NonAggressive -> {
                    if (!isNonAggressive(text)) {
                        return ValidationResult(false, R.string.reportreason_isoffensive)
                    }
                }
                ValidationRule.NonEmpty -> { /* Already handled above */ }
            }
        }

        return ValidationResult(true)
    }

    private fun defaultRules() = setOf(
        ValidationRule.NonEmpty,
        ValidationRule.MinLength(minLength),
        ValidationRule.MaxLength(maxLength),
        ValidationRule.ValidCharacters,
        ValidationRule.NonAggressive
    )

    private fun isNonAggressive(text: String): Boolean {
        return !HarmfulWordsRepository.containsHarmfulWords(text)
    }

    companion object {
        // Can be moved to a separate file
        object HarmfulWordsRepository {
            private val harmfulKeywords = setOf(
                // English words
                "hate", "violence", "abuse", "threat", "insult", "attack",
                "racist", "discrimination", "harm", "bully", "harass",
                "offend", "kill", "murder", "curse", "terrorist", "assault",

                // Spanish words
                "odio", "violencia", "abuso", "amenaza", "insulto", "ataque",
                "racista", "discriminación", "daño", "acosar", "hostigar",
                "ofender", "matar", "asesinar", "maldecir", "terrorista", "agresión",
                "golpear", "lastimar", "perjudicar", "xenófobo", "intimidar",

                // Offensive words
                "mamahuevo", "mamapinga", "pendejo", "pendeja", "cabron", "cabrona",
                "gilipollas", "estupido", "estupida", "idiota", "imbecil", "tarado",
                "puta", "puto", "zorra", "perra", "cabrón", "chingar", "chingada",
                "mierda", "coño", "carajo", "verga", "jodido", "joder", "malparido",
                "maldita", "maldito", "culero", "hijueputa", "desgraciado", "infeliz",
                "baboso", "babosa", "pelotudo", "forro", "lameculo", "cagado", "mamón"
            )

            fun containsHarmfulWords(text: String): Boolean {
                return text.lowercase().split("\\s+".toRegex())
                    .any { it in harmfulKeywords }
            }
        }
    }
}