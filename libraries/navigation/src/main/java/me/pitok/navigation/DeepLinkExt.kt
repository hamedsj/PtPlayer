package me.pitok.navigation

import android.net.Uri
import android.os.Parcelable
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import java.io.Serializable

fun NavController.interModuleNavigate(
    link: Uri,
    navOptions: NavOptions? = defaultNavOptions,
    clearBackStack: Boolean = false,
    popUpInclusive: Boolean= false,
    destinationId: Int? = null
) {
    if (popUpInclusive && destinationId != null) {
        interModuleNavigate(
            link,
            null,
            { _, _ -> },
            buildPopUpInclusiveNavOptions(destinationId),
            clearBackStack = clearBackStack
        )
    }else {
        interModuleNavigate(
            link,
            null,
            { _, _ -> },
            requireNotNull(navOptions),
            clearBackStack = clearBackStack
        )
    }
}

fun NavController.interModuleNavigate(
    link: Uri,
    serializableData: Serializable?,
    navOptions: NavOptions? = defaultNavOptions,
    clearBackStack: Boolean = false,
    popUpInclusive: Boolean= false,
    destinationId: Int? = null
) {
    if (popUpInclusive && destinationId != null){
        interModuleNavigate(
            link,
            serializableData,
            ExtraDataDataSource::storeExtraData,
            buildPopUpInclusiveNavOptions(destinationId),
            clearBackStack
        )
    }else {
        interModuleNavigate(
            link,
            serializableData,
            ExtraDataDataSource::storeExtraData,
            requireNotNull(navOptions),
            clearBackStack
        )
    }
}

fun NavController.interModuleNavigate(
    link: Uri,
    parcelableData: Parcelable?,
    navOptions: NavOptions = defaultNavOptions,
    clearBackStack: Boolean = false,
    popUpInclusive: Boolean= false,
    destinationId: Int? = null
) {
    if (popUpInclusive && destinationId != null){
        interModuleNavigate(
            link,
            parcelableData,
            ExtraDataDataSource::storeExtraData,
            buildPopUpInclusiveNavOptions(destinationId),
            clearBackStack
        )
    }else {
        interModuleNavigate(link, parcelableData, ExtraDataDataSource::storeExtraData, navOptions)
    }
}

private fun <T : Any> NavController.interModuleNavigate(
    link: Uri,
    data: T?,
    storeData: (sign: String, data: T) -> Unit,
    navOptions: NavOptions,
    clearBackStack: Boolean = false
) {
    val signedLink = if (data != null) {
        val sign = System.currentTimeMillis().toString()
        storeData.invoke(sign, data)
        link.buildUpon()
            .encodedQuery("${ExtraDataDataSource.INTER_MODULE_NAVIGATION_EXTRA_DATA}=$sign")
            .build()
    } else {
        link
    }
    if (clearBackStack) popBackStack()
    navigate(signedLink, navOptions)
}

private val defaultNavOptions = NavOptions.Builder()
    .setEnterAnim(R.anim.nav_default_enter_anim)
    .setExitAnim(R.anim.nav_default_exit_anim)
    .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
    .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
    .build()

private fun buildPopUpInclusiveNavOptions(
    destinationId: Int
): NavOptions =
    NavOptions.Builder()
        .setEnterAnim(R.anim.nav_default_enter_anim)
        .setExitAnim(R.anim.nav_default_exit_anim)
        .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
        .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
        .setPopUpTo(destinationId,false)
        .build()
