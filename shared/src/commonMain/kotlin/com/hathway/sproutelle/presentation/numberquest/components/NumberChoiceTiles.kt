package com.hathway.sproutelle.presentation.numberquest.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hathway.sproutelle.presentation.numberquest.NumberChoice

@Composable
fun NumberChoiceTiles(
    choices: List<NumberChoice>,
    selectedChoiceId: String?,
    isCorrect: Boolean?,
    isTablet: Boolean,
    onChoiceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val row1 = choices.take(2)
    val row2 = choices.drop(2)
    val isLocked = isCorrect == true

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (isTablet) 18.dp else 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 18.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            row1.forEachIndexed { index, choice ->
                val isSelected = selectedChoiceId == choice.id
                NumberChoiceItem(
                    choice = choice,
                    isSelected = isSelected,
                    isCorrect = if (isSelected) isCorrect else null,
                    isTablet = isTablet,
                    entranceIndex = index,
                    enabled = !isLocked,
                    onClick = { onChoiceClick(choice.id) }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 18.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            row2.forEachIndexed { index, choice ->
                val isSelected = selectedChoiceId == choice.id
                NumberChoiceItem(
                    choice = choice,
                    isSelected = isSelected,
                    isCorrect = if (isSelected) isCorrect else null,
                    isTablet = isTablet,
                    entranceIndex = 2 + index,
                    enabled = !isLocked,
                    onClick = { onChoiceClick(choice.id) }
                )
            }
        }
    }
}