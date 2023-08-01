package my.doctrina.app.data

data class MenuLinks(
    val name: String,
    val url: String,
    val isSubItem: Boolean
) {
    var subItem: MenuLinks? = null
}
