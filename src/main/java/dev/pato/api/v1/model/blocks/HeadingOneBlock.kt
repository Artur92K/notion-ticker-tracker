package notion.api.v1.model.blocks

import com.google.gson.annotations.SerializedName
import java.util.*
import notion.api.v1.model.common.BlockColor
import notion.api.v1.model.common.ObjectType
import dev.pato.api.v1.model.pages.PageProperty
import notion.api.v1.model.users.User

open class HeadingOneBlock
@JvmOverloads
constructor(
    @SerializedName("object") override val objectType: ObjectType = ObjectType.Block,
    override val type: BlockType = BlockType.HeadingOne,
    override var id: String? = UUID.randomUUID().toString(),
    override var createdTime: String? = null,
    override var createdBy: User? = null,
    override var lastEditedTime: String? = null,
    override var lastEditedBy: User? = null,
    override var hasChildren: Boolean? = null,
    override var archived: Boolean? = null,
    override var parent: BlockParent? = null,
    @SerializedName("heading_1") val heading1: Element,
    override val requestId: String? = null,
    override var inTrash: Boolean? = null,
) : Block {

  // for other JVM languages
  constructor(
      heading1: Element,
      id: String? = UUID.randomUUID().toString(),
      hasChildren: Boolean? = null,
      createdTime: String? = null,
      createdBy: User? = null,
      lastEditedTime: String? = null,
      lastEditedBy: User? = null,
      parent: BlockParent? = null,
  ) : this(
      objectType = ObjectType.Block,
      type = BlockType.HeadingOne,
      id = id,
      createdTime = createdTime,
      createdBy = createdBy,
      lastEditedTime = lastEditedTime,
      lastEditedBy = lastEditedBy,
      hasChildren = hasChildren,
      parent = parent,
      heading1 = heading1)

  open class Element
  @JvmOverloads
  constructor(
      var richText: List<PageProperty.RichText>,
      var isToggleable: Boolean? = null,
      var color: BlockColor? = null,
      var children: List<Block>? = null,
  )
}
