package com.aml_sakr.fitlife.feature.auth.auth_ui.shared

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.aml_sakr.fitlife.core.ui.R as CoreUiR
import com.aml_sakr.fitlife.feature.auth.auth_ui.R

@Composable
internal fun AuthScreenHeader(
    titleResId: Int,
    descriptionResId: Int
) {
    Text(
        text = stringResource(R.string.auth_brand_name),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.headlineLarge
    )
    Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_sm)))
    Text(
        text = stringResource(titleResId),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_sm)))
    Text(
        text = stringResource(descriptionResId),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(dimensionResource(CoreUiR.dimen.space_xl)))
}
