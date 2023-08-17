package my.doctrina.app.data

import android.content.Context
import my.doctrina.app.R

const val MAIN_LINK = "https://mobile.doctrina.app/"
const val MATERIALS_LINK = "https://mobile.doctrina.app/materials"
const val FEEDBACK_LINK = "https://mobile.doctrina.app/feedback"
const val FAVORITE_LINK = "https://mobile.doctrina.app/favorite"
const val SETTINGS_LINK = "https://mobile.doctrina.app/settings"
//const val MATERIALS_ID_LINK = "https://mobile.doctrina.app/materials?ID=1"

data class MenuItem(
    val url: String,
    val isSubItem: Boolean = false
) {
    var subItem: MenuItem? = null

    fun getName(context: Context): String {
        return when (url) {
            MAIN_LINK -> ""
            MATERIALS_LINK -> context.getString(R.string.header_materials)
            FEEDBACK_LINK -> context.getString(R.string.header_feedback)
            FAVORITE_LINK -> ""
            SETTINGS_LINK -> context.getString(R.string.header_settings)
//            MATERIALS_ID_LINK -> context.getString(R.string.header_materials)
            else -> {
                ""
            }
        }
    }
}
