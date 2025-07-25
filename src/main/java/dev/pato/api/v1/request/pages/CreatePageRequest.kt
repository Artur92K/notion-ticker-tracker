package notion.api.v1.request.pages

import notion.api.v1.model.blocks.Block
import notion.api.v1.model.common.Cover
import notion.api.v1.model.common.Icon
import notion.api.v1.model.pages.PageParent
import dev.pato.api.v1.model.pages.PageProperty

data class CreatePageRequest
@JvmOverloads
constructor(
    val parent: PageParent,
    val properties: Map<String, PageProperty>,
    var children: List<Block>? = null,
    val icon: Icon? = null,
    val cover: Cover? = null,
)
