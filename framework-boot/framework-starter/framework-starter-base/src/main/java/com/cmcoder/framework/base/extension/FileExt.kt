package com.lumispring.framework.base.extension

import com.lumispring.framework.base.model.ErrorCode
import org.springframework.core.io.ClassPathResource
import java.io.*
import java.net.URI
import java.net.URL
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


/**
 * 获取资源文件
 * @param path 资源文件路径
 */
fun getResourceFile(path: String): File {
    return ClassPathResource(path).file
}

/**
 * 将任意对象转换为输入流
 */
fun Any.toInputStream(): InputStream {
    return when (this) {
        is String -> {
            if (this.startsWith("http")) {
                URL(this).openStream()
            } else {
                File(this).inputStream()
            }
        }

        is File -> this.inputStream()
        is URL -> this.openStream()
        is URI -> this.toURL().openStream()
        is Path -> this.toFile().inputStream()
        is ByteArray -> this.inputStream()
        is ByteArrayOutputStream -> this.toByteArray().inputStream()
        is InputStream -> this
        else -> throw ErrorCode.SERVICE_PARAM_VALIDATION_ERROR.exception(log = "不支持的类型转换：${this.javaClass} => InputStream")
    }
}

/**
 * 将字符串转换为文件对象，如果是http链接则下载到临时目录
 * @param fileName  文件名，如果为空则取链接的最后一段
 * @param useCache  是否使用缓存，如果本地有同名文件则直接使用
 */
fun String.toFile(fileName: String? = null, useCache: Boolean = true): File {
    return if (this.startsWith("http")) {
        val targetName = fileName ?: this.takeLastWhile { it.toString() !in setOf(FILE_SEPARATOR, "/") }
        val url = this
        val targetFile = File(TMP_DIR, targetName)
        targetFile.apply {
            if (!exists() || !useCache) {
                URL(url).openStream().copyToFile(this)
                logInfo("从远程下载文件：$url => ${this.absolutePath}")
            }
        }
    } else File(this)
}

/**
 * 将字节数组写入文件
 */
fun ByteArray.toFile(filePath: String) :File{
    val targetFile = File(filePath)
    FileOutputStream(targetFile).use {
        it.write(this)
    }
    return targetFile
}


/**
 * 将文件复制到指定文件
 * @param target 目标文件
 */
fun String.copyToFile(target: File) {
    this.toFile().copyToFile(target)
}

/**
 * 将文件复制到指定文件
 * @param target 目标文件
 */
fun ByteArray.copyToFile(target: File) {
    target.outputStream().use {
        it.write(this)
    }
}


/**
 * 将文件复制到指定文件
 * @param target 目标文件
 */
fun File.copyToFile(target: File) {
    this.toInputStream().readBytes().copyToFile(target)
}

/**
 * 将文件复制到指定文件
 * @param target 目标文件
 */
fun InputStream.copyToFile(target: File) {
    this.readBytes().copyToFile(target)
}

/**
 * 将文件复制到指定文件
 * @param target 目标文件
 */
fun URL.copyToFile(target: File) {
    this.openStream().copyToFile(target)
}

/**
 * 将文件复制到指定文件
 * @param target 目标文件
 */
fun URI.copyToFile(target: File) {
    this.toURL().copyToFile(target)
}

/**
 * 将文件复制到指定文件
 * @param target 目标文件
 */
fun ByteArrayOutputStream.copyToFile(target: File) {
    this.toByteArray().copyToFile(target)
}

/**
 * 将文件复制到指定目录
 * @param targetDir 目标目录
 */
fun String.copyToDir(targetDir: String) {
    this.toFile().copyToDir(targetDir)
}

/**
 * 将文件复制到指定目录
 * @param targetDir 目标目录
 */
fun File.copyToDir(targetDir: String): File {
    val dirPath = File(targetDir).let {
        if (it.isDirectory && it.exists()) {
            targetDir
        } else if (it.mkdirs()) {
            logInfo("创建目录：$targetDir")
            targetDir
        } else {
            throw ErrorCode.SERVICE_FILE_ERROR.exception(log = "创建目录失败：$targetDir")
        }
    }
    return File(dirPath, this.name).also { this.copyToFile(it) }
}

/**
 * 如果文件夹不存在则创建
 * @return 创建后的文件夹
 */
fun File.mkdirIfAbsent(): File {
    if (!this.parentFile.exists()) {
        this.parentFile.mkdirs()
    }
    return this
}

/**
 * 如果文件夹不存在则创建
 * @return 创建后的文件夹
 */
fun String.mkdirIfAbsent() = File(this).mkdirIfAbsent()


/**
 * 将多个文件进行zip压缩
 * @param outputZipFile 输出的zip文件
 */
fun List<File>.toZip(outputZipFile: File): File {
    outputZipFile.mkdirIfAbsent()
    ZipOutputStream(FileOutputStream(outputZipFile)).use { zipOut ->
        this.forEach { file ->
            FileInputStream(file).use { fis ->
                val zipEntry = ZipEntry(file.name)
                zipOut.putNextEntry(zipEntry)
                fis.copyTo(zipOut)
                zipOut.closeEntry()
            }
        }
    }
    return outputZipFile
}

/**
 * 将多个文件进行zip压缩
 * @param outputZipFilePath 输出的zip文件路径 eg: D:/test.zip
 */
fun List<File>.toZip(outputZipFilePath: String) = this.toZip(File(outputZipFilePath))


/**
 * 将zip文件解压到指定目录
 * @param outputDirectory 输出目录，默认为zip文件所在目录
 */
fun File.unZip(outputDirectory: File = this.parentFile) {
    ZipInputStream(BufferedInputStream(FileInputStream(this))).use { zipInput ->
        var entry: ZipEntry?
        while (zipInput.nextEntry.also { entry = it } != null) {
            val entryFile = File(outputDirectory, entry!!.name)
            val entryDir = entryFile.parentFile

            if (!entryDir.exists()) {
                entryDir.mkdirs()
            }

            if (!entry!!.isDirectory) {
                FileOutputStream(entryFile).use { fileOutput ->
                    zipInput.copyTo(fileOutput)
                }
            }
            zipInput.closeEntry()
        }
    }
}

/**
 * 将zip文件解压到指定目录
 * @param outputDirectory 输出目录，默认为zip文件所在目录
 */
fun File.unZip(outputDirectory: String) = this.unZip(File(outputDirectory))