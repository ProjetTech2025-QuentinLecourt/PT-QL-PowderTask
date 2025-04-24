package com.quentinlecourt.podwertask_mobile.data.model

/**
 * Modèle de la liste d'utilisateur liés à une machine/balance
 */

data class UserInList(
    val first_name: String,
    val last_name: String
)

data class UsersListResponse(
    val users: List<UserInList>
)