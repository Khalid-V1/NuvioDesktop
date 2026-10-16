package com.nuvio.app.core.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp

private val SpoilerTextBlurShape = RoundedCornerShape(8.dp)

/**
 * Obscures spoiler text inside a softly rounded layer and removes its
 * accessibility semantics until the caller reveals it.
 */
fun Modifier.spoilerTextBlur(blurred: Boolean): Modifier =
    if (blurred) {
        blur(
            radius = 8.dp,
            edgeTreatment = BlurredEdgeTreatment(SpoilerTextBlurShape),
        ).clearAndSetSemantics { }
    } else {
        this
    }
