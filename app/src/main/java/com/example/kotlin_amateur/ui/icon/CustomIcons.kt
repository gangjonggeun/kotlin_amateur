
package com.example.kotlin_amateur.ui.icon
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

object CustomIcons {
    val Reply: ImageVector
        get() {
            if (_reply != null) {
                return _reply!!
            }
            _reply = materialIcon(name = "CustomReply") {
                materialPath {
                    // ㄴ 모양의 간단한 답글 아이콘
                    moveTo(4f, 8f)
                    lineTo(4f, 16f)
                    lineTo(12f, 16f)
                    moveTo(4f, 12f)
                    lineTo(8f, 8f)
                    moveTo(4f, 12f)
                    lineTo(8f, 16f)
                }
            }
            return _reply!!
        }
}

private var _reply: ImageVector? = null