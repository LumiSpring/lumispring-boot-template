package com.lumispring.framework.base.extension

import java.nio.file.FileSystems

/**
 * 临时文件目录
 */
val TMP_DIR: String = System.getProperty("java.io.tmpdir")

/**
 * 项目根目录
 */
val PROJECT_ROOT_DIR: String = System.getProperty("user.dir")

/**
 * 资源目录
 */
val RESOURCE_DIR: String = ClassLoader.getSystemClassLoader().getResource("")?.path ?: "${PROJECT_ROOT_DIR}/src/main/resources"

/**
 * 文件分隔符
 */
val FILE_SEPARATOR: String = FileSystems.getDefault().separator

/**
 * 操作系统名称
 */
val OS_NAME:  String = System.getProperty("os.name")


/**
 * 执行系统命令行命令
 */
fun String.runCommand(): String {
    val process = Runtime.getRuntime().exec(this)
    process.waitFor()
    // 返回执行结果
    return process.inputReader().lines().map { it }.toList().joinToString("\n").also {
        process.destroy()
    }
}