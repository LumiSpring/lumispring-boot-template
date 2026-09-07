package com.lumispring.framework.base.extension

import net.coobird.thumbnailator.Thumbnailator
import net.coobird.thumbnailator.Thumbnails
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

/**
 * 输入流转图片
 */
fun InputStream.toImage(): BufferedImage {
    this.use {
        return ImageIO.read(this) ?: throw IllegalArgumentException("Invalid image stream")
    }
}

fun String.toImage(): BufferedImage {
    return this.toInputStream().toImage()
}

fun ByteArray.toImage(): BufferedImage {
    return this.toInputStream().toImage()
}

/**
 * 无损压缩图片，保持原图片尺寸
 */
fun BufferedImage.compress(): ByteArray {
    val height = this.height
    val width = this.width

    val newBufferImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics = newBufferImage.graphics
    val scaleImg = this.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH)
    graphics.drawImage(scaleImg, 0, 0, null)
    graphics.dispose()
    return ByteArrayOutputStream().use {
        ImageIO.write(newBufferImage, "jpg", it)
        it.toByteArray()
    }
}

/**
 * 压缩图片
 */
fun ByteArray.compressImage(): ByteArray {
    return this.inputStream().toImage().compress()
}


/**
 * 压缩图片
 */
fun File.compressImage(targetFile: File = this) {
    this.inputStream().toImage().compress().copyToFile(targetFile)
}

/**
 * 处理图片
 * @param quality 图片质量
 * @param scale 图片缩放比例
 * @param width 图片宽度
 * @param height 图片高度
 * @param rotate 图片旋转角度
 */
fun BufferedImage.handleImage(
    quality: Double = 1.0,
    scale: Double = 1.0,
    width: Int? = null,
    height: Int? = null,
    rotate: Double? = null,
): ByteArray {
    val outputStream = ByteArrayOutputStream()
    Thumbnails.of(this).apply {
        if (width.isNotNullOrEmpty() && height.isNotNullOrEmpty()) {
            forceSize(width, height)
        } else {
            this.scale(scale)
        }
        rotate?.let { this.rotate(rotate) }
    }.outputQuality(quality).toOutputStream(outputStream)
    return outputStream.toByteArray()
}

/**
 * 处理图片
 * @param quality 图片质量
 * @param scale 图片缩放比例
 * @param width 图片宽度
 * @param height 图片高度
 * @param rotate 图片旋转角度
 */
fun ByteArray.handleImage(
    quality: Double = 1.0,
    scale: Double = 1.0,
    width: Int? = null,
    height: Int? = null,
    rotate: Double? = null,
): ByteArray {
    return this.toImage().handleImage(quality, scale, width, height, rotate)
}