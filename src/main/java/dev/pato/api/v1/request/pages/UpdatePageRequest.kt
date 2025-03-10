package notion.api.v1.request.pages

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import notion.api.v1.model.common.Cover
import notion.api.v1.model.common.Icon
import dev.pato.api.v1.model.pages.PageProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdatePageRequest
@JvmOverloads
constructor(
    @JsonIgnore
    @Transient val pageId: String,
    val properties: Map<String, PageProperty>,
    val archived: Boolean? = null,
    val icon: Icon? = null,
    val cover: Cover? = null,
)
