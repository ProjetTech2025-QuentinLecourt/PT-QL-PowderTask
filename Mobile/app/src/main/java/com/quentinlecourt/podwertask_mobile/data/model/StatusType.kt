package com.quentinlecourt.podwertask_mobile.data.model

import androidx.annotation.DrawableRes
import com.quentinlecourt.podwertask_mobile.R

sealed class StatusType(
    @DrawableRes val iconRes: Int,
    val defaultMessage: String
) {
    object Success : StatusType(
        R.drawable.ic_success_animated,
        "Terminé avec succès"
    )

    object Failed : StatusType(
        R.drawable.ic_error_animated,
        "Échec de l'opération"
    )

//    // Ajoutez d'autres statuts au besoin
//    object Warning : StatusType(
//        R.drawable.ic_warning_animated,
//        "Avertissement"
//    )
}