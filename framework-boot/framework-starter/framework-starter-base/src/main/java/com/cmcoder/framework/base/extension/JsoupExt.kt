package com.lumispring.framework.base.extension

import org.jsoup.nodes.Element

fun Element.style(style: String?) :Element{
    if (style.isNullOrEmpty()) return this
    return this.attr("style", style)
}

fun Element.addStyle(style: String?) :Element{
    if (style.isNullOrEmpty()) return this
    val oldStyle = this.attr("style")
    val newStyle = if (oldStyle.isNotNullOrEmpty()) "$oldStyle;$style" else style
    return this.attr("style", newStyle)
}

fun Element.height(height: String?) :Element{
    if (height.isNullOrEmpty()) return this
    return this.addStyle("height:$height")
}

fun Element.width(width: String?) :Element{
    if (width.isNullOrEmpty()) return this
    return this.addStyle("width:$width")
}

fun Element.border(border: String?) :Element{
    if (border.isNullOrEmpty()) return this
    return this.addStyle("border:$border")
}

fun Element.borderTop(borderTop: String?) :Element{
    if (borderTop.isNullOrEmpty()) return this
    return this.addStyle("border-top:$borderTop")
}

fun Element.borderBottom(borderBottom: String?) :Element{
    if (borderBottom.isNullOrEmpty()) return this
    return this.addStyle("border-bottom:$borderBottom")
}

fun Element.borderLeft(borderLeft: String?) :Element{
    if (borderLeft.isNullOrEmpty()) return this
    return this.addStyle("border-left:$borderLeft")
}

fun Element.borderRight(borderRight: String?) :Element{
    if (borderRight.isNullOrEmpty()) return this
    return this.addStyle("border-right:$borderRight")
}

fun Element.borderRadius(borderRadius: String?) :Element{
    if (borderRadius.isNullOrEmpty()) return this
    return this.addStyle("border-radius:$borderRadius")
}

fun Element.padding(padding: String?) :Element{
    if (padding.isNullOrEmpty()) return this
    return this.addStyle("padding:$padding")
}

/**
 * 使用flex布局将元素内容居中
 */
fun Element.flexCenter(): Element {
    return this.addStyle("display:flex; justify-content:center; align-items:center")
}

/**
 * 使用margin:0 auto将元素内容居中
 */
fun Element.marginCenter(): Element {
    return this.addStyle("margin:0 auto")
}