package notion.api.v1.model.blocks

import com.google.gson.annotations.SerializedName
import java.util.*
import notion.api.v1.model.common.ObjectType
import dev.pato.api.v1.model.pages.PageProperty
import notion.api.v1.model.users.User

open class BookmarkBlock
@JvmOverloads
constructor(
    @SerializedName("object") override val objectType: ObjectType = ObjectType.Block,
    override val type: BlockType = BlockType.Bookmark,
    override var id: String? = UUID.randomUUID().toString(),
    override var createdTime: String? = null,
    override var createdBy: User? = null,
    override var lastEditedTime: String? = null,
    override var lastEditedBy: User? = null,
    override var hasChildren: Boolean? = null,
    override var archived: Boolean? = null,
    override var parent: BlockParent? = null,
    val bookmark: Element? = null,
    override val requestId: String? = null,
    override var inTrash: Boolean? = null,
) : Block {

  // for other JVM languages
  constructor(
      bookmark: Element,
      id: String? = UUID.randomUUID().toString(),
      hasChildren: Boolean? = null,
      createdTime: String? = null,
      createdBy: User? = null,
      archived: Boolean? = null,
      lastEditedTime: String? = null,
      lastEditedBy: User? = null,
      parent: BlockParent? = null,
  ) : this(
      ObjectType.Block,
      BlockType.Bookmark,
      id,
      createdTime,
      createdBy,
      lastEditedTime,
      lastEditedBy,
      hasChildren,
      archived,
      parent,
      bookmark)

  open class Element
  @JvmOverloads
  constructor(
      val url: String? = null,
      val caption: List<PageProperty.RichText>? = null,
  )
}
