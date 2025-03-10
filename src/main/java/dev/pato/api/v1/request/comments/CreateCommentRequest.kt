package notion.api.v1.request.comments

import notion.api.v1.model.pages.PageParent
import dev.pato.api.v1.model.pages.PageProperty

data class CreateCommentRequest
@JvmOverloads
constructor(
    val parent: PageParent? = null,
    val discussionId: String? = null,
    var richText: List<PageProperty.RichText>,
)
