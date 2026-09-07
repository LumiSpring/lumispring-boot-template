package com.lumispring.framework.base.extension

import org.apache.commons.codec.binary.Base64
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset
import java.security.*
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

const val ENCRYPT_MD5 = "MD5"
const val ENCRYPT_SHA1 = "SHA1"
const val ENCRYPT_SHA224 = "SHA224"
const val ENCRYPT_SHA256 = "SHA256"
const val ENCRYPT_SHA384 = "SHA384"
const val ENCRYPT_SHA512 = "SHA512"

const val ENCRYPT_HMAC_MD5 = "HmacMD5"
const val ENCRYPT_HMAC_SHA1 = "HmacSHA1"
const val ENCRYPT_HMAC_SHA224 = "HmacSHA224"
const val ENCRYPT_HMAC_SHA256 = "HmacSHA256"
const val ENCRYPT_HMAC_SHA384 = "HmacSHA384"
const val ENCRYPT_HMAC_SHA512 = "HmacSHA512"

const val ENCRYPT_RSA = "RSA"


const val DEFAULT_RSA_KEY_LENGTH = 1024
const val DEFAULT_RSA_TRANSFORMATION = "RSA"


/**
 * ByteArray转换成16进制字符串
 * https://stackoverflow.com/a/21178195/8546297
 */
fun ByteArray.toHex(): String {
    val result = StringBuilder()
    forEach {
        result.append(Character.forDigit((it.toInt() shr 4) and 0xF, 16))
        result.append(Character.forDigit(it.toInt() and 0xF, 16))
    }
    return result.toString()
}

fun ByteArray.toSafeHex(): String {
    return Base64.encodeBase64URLSafeString(this)
}

/**
 * ByteArray转成字符串
 * @param charset 字符集
 */
fun ByteArray.byteToString(charset: Charset = UTF8): String {
    return String(this, charset)
}

/**
 * 16进制字符串转换成ByteArray
 *
 */
fun String.hexToByteArray(): ByteArray {
    val data = ByteArray(length / 2)
    for (i in 0 until length step 2)
        data[i / 2] = ((Character.digit(this[i], 16) shl 4) + Character.digit(this[i + 1], 16)).toByte()
    return data
}
/*
  在密码学中，hash算法（散列函数）的作用主要是用于消息摘要和签名，换句话说，它主要用于对整个消息的完整性进行校验。
 */
/*
  MD5：https://zh.wikipedia.org/wiki/MD5
  MD5消息摘要算法（英语：MD5 Message-Digest Algorithm），一种被广泛使用的密码散列函数，
  可以产生出一个128位（16字节）的散列值（hash value），用于确保信息传输完整一致。
  1996年后被证实存在弱点，可以被加以破解，对于需要高度安全性的数据，专家一般建议改用其他算法，如SHA-1。
  2004年，证实MD5算法无法防止碰撞（collision），因此无法适用于安全性认证，如SSL公开密钥认证或是数字签名等用途。
 */

/**
 * hash 函数模板
 *
 * @param algorithmType
 * @param data
 */
private fun hashFunc(algorithmType: String, data: String): String {
    return try {
        val md = MessageDigest.getInstance(algorithmType)
        md.digest(data.toByteArray()).toHex()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
        ""
    }
}

private fun hashFuncForFile(type: String, inputStream: FileInputStream): String {
    return try {
        val md = MessageDigest.getInstance(type)
        val input = DigestInputStream(inputStream, md)
        val buffer = ByteArray(8192)
        input.use {
            while (input.read(buffer) != -1);
        }
        md.digest().toHex()
    } catch (e: NoSuchAlgorithmException) {
        ""
    }
}

/**
 * md5加密
 *
 * @param salt 盐值
 */
@JvmOverloads
fun String.md5(salt: String = ""): String = hashFunc(ENCRYPT_MD5, this + salt)

/**
 * 文件MD5加密
 *
 */
fun File.md5(): String = hashFuncForFile(ENCRYPT_MD5, inputStream())

/*
  SHA家族：https://zh.wikipedia.org/wiki/SHA%E5%AE%B6%E6%97%8F
  安全散列算法（英语：Secure Hash Algorithm，缩写为SHA）是一个密码散列函数家族，是FIPS所认证的安全散列算法。
  能计算出一个数字消息所对应到的，长度固定的字符串（又称消息摘要）的算法。且若输入的消息不同，它们对应到不同字符串的概率很高。
 */

/**
 * SHA1加密
 */
fun String.sha1(): String = hashFunc(ENCRYPT_SHA1, this)

/**
 * SHA224加密
 */
fun String.sha224(): String = hashFunc(ENCRYPT_SHA224, this)

/**
 * SHA256加密
 */
fun String.sha256(): String = hashFunc(ENCRYPT_SHA256, this)

/**
 * SHA384加密
 */
fun String.sha384(): String = hashFunc(ENCRYPT_SHA384, this)

/**
 * SHA512加密
 */
fun String.sha512(): String = hashFunc(ENCRYPT_SHA512, this)

/*
  HMAC：https://zh.wikipedia.org/wiki/%E9%87%91%E9%91%B0%E9%9B%9C%E6%B9%8A%E8%A8%8A%E6%81%AF%E9%91%91%E5%88%A5%E7%A2%BC
  密钥散列消息认证码（英语：Keyed-hash message authentication code，缩写为HMAC），
  又称散列消息认证码（Hash-based message authentication code），是一种通过特别计算方式之后产生的消息认证码（MAC），
  使用密码散列函数，同时结合一个加密密钥。它可以用来保证数据的完整性，同时可以用来作某个消息的身份验证。
 */

/**
 * HMAC 模板函数
 *
 * @param data 加密数据
 * @param algorithmType
 * @param key 密钥
 * @param safeUrlString 是否使用Base64 url安全编码
 */
private fun hmacFunc(data: String, algorithmType: String, key: String, safeUrlString:Boolean = false): String {
    return try {
        val mac = Mac.getInstance(algorithmType)
        val secretKey = SecretKeySpec(key.toByteArray(), mac.algorithm)
        mac.init(secretKey)
        if (safeUrlString) mac.doFinal(data.toByteArray()).toSafeHex()
        else mac.doFinal(data.toByteArray()).toHex()
    } catch (e: InvalidKeyException) {
        e.printStackTrace()
        ""
    }
}

/**
 * HMAC 模板函数
 *
 * @param data 加密数据，字节数组
 * @param algorithmType
 * @param key 密钥
 * @param safeUrlString 是否使用Base64 url安全编码
 */
private fun hmacFunc(data: ByteArray, algorithmType: String, key: String, safeUrlString:Boolean = false): String {
    return try {
        val mac = Mac.getInstance(algorithmType)
        val secretKey = SecretKeySpec(key.toByteArray(), mac.algorithm)
        mac.init(secretKey)
        if (safeUrlString) mac.doFinal(data).toSafeHex()
        else mac.doFinal(data).toHex()
    } catch (e: InvalidKeyException) {
        e.printStackTrace()
        ""
    }
}


/**
 * 获取HMAC-MD5 加密
 *
 * @param key
 * @param safeUrlString 是否使用Base64 url安全编码
 */
fun String.hmacMD5(key: String, safeUrlString:Boolean = false): String = hmacFunc(this, ENCRYPT_HMAC_MD5, key, safeUrlString)
fun ByteArray.hmacMD5(key: String, safeUrlString:Boolean = false): String = hmacFunc(this, ENCRYPT_HMAC_MD5, key, safeUrlString)

/**
 * 获取HMAC-SHA1 加密
 *
 * @param key
 * @param safeUrlString 是否使用Base64 url安全编码
 */
fun String.hmacSHA1(key: String, safeUrlString:Boolean = false): String = hmacFunc(this, ENCRYPT_HMAC_SHA1, key, safeUrlString)
fun ByteArray.hmacSHA1(key: String, safeUrlString:Boolean = false): String = hmacFunc(this, ENCRYPT_HMAC_SHA1, key, safeUrlString)

/**
 * 获取HMAC-SHA224 加密
 *
 * @param key
 * @param safeUrlString 是否使用Base64 url安全编码
 */
fun String.hmacSHA224(key: String, safeUrlString:Boolean = false): String = hmacFunc(this, ENCRYPT_HMAC_SHA224, key, safeUrlString)
fun ByteArray.hmacSHA224(key: String, safeUrlString:Boolean = false): String = hmacFunc(this, ENCRYPT_HMAC_SHA224, key, safeUrlString)

/**
 * 获取HMAC-SHA256 加密
 *
 * @param key
 * @param safeUrlString 是否使用Base64 url安全编码
 */
fun String.hmacSHA256(key: String, safeUrlString:Boolean = false): String = hmacFunc(this, ENCRYPT_HMAC_SHA256, key, safeUrlString)
fun ByteArray.hmacSHA256(key: String, safeUrlString:Boolean = false): String = hmacFunc(this, ENCRYPT_HMAC_SHA256, key, safeUrlString)

/**
 * 获取HMAC-SHA384 加密
 *
 * @param key
 * @param safeUrlString 是否使用Base64 url安全编码
 */
fun String.hmacSHA384(key: String, safeUrlString:Boolean = false): String = hmacFunc(this, ENCRYPT_HMAC_SHA384, key, safeUrlString)
fun ByteArray.hmacSHA384(key: String, safeUrlString:Boolean = false): String = hmacFunc(this, ENCRYPT_HMAC_SHA384, key, safeUrlString)

/**
 * 获取HMAC-SHA512 加密
 *
 * @param key
 * @param safeUrlString 是否使用Base64 url安全编码
 */
fun String.hmacSHA512(key: String, safeUrlString:Boolean = false): String = hmacFunc(this, ENCRYPT_HMAC_SHA512, key, safeUrlString)
fun ByteArray.hmacSHA512(key: String, safeUrlString:Boolean = false): String = hmacFunc(this, ENCRYPT_HMAC_SHA512, key, safeUrlString)

/*
  对称加密算法
  这类算法在加密和解密时使用相同的密钥，或是使用两个可以简单地相互推算的密钥。
  常见的对称加密算法有DES、3DES、AES、Blowfish、IDEA、RC5、RC6。
 */


/**
 * 非对称加密算法
 * 非对称加密算法是密码学中的一类重要算法，它使用了一对密钥，公钥和私钥。
 * 公钥是公开的，所有人都可以获得，私钥则是保密的，只有持有私钥的人才能解密。
 * 常见的非对称加密算法有RSA、DSA、ECC。
 */

/**
 * 生成RSA的密钥对
 *
 * @param keyLength 密钥长度
 */
@JvmOverloads
fun generateRSAKeyPair(keyLength: Int = DEFAULT_RSA_KEY_LENGTH): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance(ENCRYPT_RSA)
    keyPairGenerator.initialize(keyLength)
    return keyPairGenerator.genKeyPair()
}

/**
 * 在生成的密钥对中获取公钥和私钥
 *
 * @param isPublicKey true 公钥 false 私钥
 */
fun getRSAKey(keyPair: KeyPair, isPublicKey: Boolean): ByteArray =
    if (isPublicKey) (keyPair.public as RSAPublicKey).encoded else (keyPair.private as RSAPrivateKey).encoded

/**
 * 用私钥对信息生成数字签名
 *
 * @param privateKey
 * @param algorithm 签名算法
 *
 * @return ByteArray
 */
@JvmOverloads
fun String.rsaSign(privateKey: ByteArray, algorithm: String = "MD5withRSA"): String {
    return try {
        val pkcs8KeySpec = PKCS8EncodedKeySpec(privateKey)
        val keyFactory = KeyFactory.getInstance(ENCRYPT_RSA)
        val priKey = keyFactory.generatePrivate(pkcs8KeySpec)

        val signature = Signature.getInstance(algorithm)
        signature.initSign(priKey)
        signature.update(this.toByteArray())
        signature.sign().base64Encode2Str()
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

/**
 * 用私钥对信息生成数字签名
 *
 * @param privateKey
 * @param algorithm 签名算法
 *
 * @return ByteArray
 */
@JvmOverloads
fun ByteArray.rsaSign(privateKey: ByteArray, algorithm: String = "MD5withRSA"): String {
    return try {
        val pkcs8KeySpec = PKCS8EncodedKeySpec(privateKey)
        val keyFactory = KeyFactory.getInstance("RSA")
        val priKey = keyFactory.generatePrivate(pkcs8KeySpec)

        val signature = Signature.getInstance(algorithm)
        signature.initSign(priKey)
        signature.update(this)
        signature.sign().base64Encode2Str()
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

/**
 * 验证数字签名
 * this为未加密的原始数据
 *
 * @param publicKey
 * @param sign Base64 编码过的签名
 * @param algorithm 签名算法
 *
 * @return
 */
@JvmOverloads
fun String.rsaVerifySign(publicKey: ByteArray, sign: String, algorithm: String = "MD5withRSA"): Boolean {
    return try {
        val x509KeySpec = X509EncodedKeySpec(publicKey)
        val keyFactory = KeyFactory.getInstance(ENCRYPT_RSA)
        val pubKey = keyFactory.generatePublic(x509KeySpec)

        val signature = Signature.getInstance(algorithm)
        signature.initVerify(pubKey)
        signature.update(this.toByteArray())
        signature.verify(sign.toByteArray().base64Decode())
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * 验证数字签名
 * this为未加密的原始数据
 *
 * @param publicKey
 * @param sign Base64 编码过的签名
 * @param algorithm 签名算法
 *
 * @return
 */
@JvmOverloads
fun ByteArray.rsaVerifySign(publicKey: ByteArray, sign: String, algorithm: String = "MD5withRSA"): Boolean {
    return try {
        val x509KeySpec = X509EncodedKeySpec(publicKey)
        val keyFactory = KeyFactory.getInstance(ENCRYPT_RSA)
        val pubKey = keyFactory.generatePublic(x509KeySpec)

        val signature = Signature.getInstance(algorithm)
        signature.initVerify(pubKey)
        signature.update(this)
        signature.verify(sign.toByteArray().base64Decode())
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

private fun rsaEncryptOrDecryptByPublicKey(
    data: ByteArray,
    publicKey: ByteArray,
    isEncrypt: Boolean,
    transformation: String = DEFAULT_RSA_TRANSFORMATION
): ByteArray {
    return try {
        //获取公钥
        val x509KeySpec = X509EncodedKeySpec(publicKey)
        val keyFactory = KeyFactory.getInstance(ENCRYPT_RSA)
        val pubKey = keyFactory.generatePublic(x509KeySpec)

        //加/解密
        val cipher = Cipher.getInstance(transformation)
        cipher.init(if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE, pubKey)
        return cipher.doFinal(data)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyArray<Byte>().toByteArray()
    }
}

/**
 * RSA公钥加密
 *
 * @param publicKey
 * @param transformation
 */
@JvmOverloads
fun String.rsaEncryptByPublicKey(publicKey: ByteArray, transformation: String = DEFAULT_RSA_TRANSFORMATION): ByteArray =
    rsaEncryptOrDecryptByPublicKey(this.toByteArray(), publicKey, true, transformation)

/**
 * RSA公钥加密
 *
 * @param publicKey
 * @param transformation
 */
@JvmOverloads
fun ByteArray.rsaEncryptByPublicKey(
    publicKey: ByteArray,
    transformation: String = DEFAULT_RSA_TRANSFORMATION
): ByteArray =
    rsaEncryptOrDecryptByPublicKey(this, publicKey, true, transformation)

/**
 * RSA公钥解密
 *
 * @param publicKey
 * @param transformation
 */
@JvmOverloads
fun String.rsaDecryptByPublicKey(publicKey: ByteArray, transformation: String = DEFAULT_RSA_TRANSFORMATION): ByteArray =
    rsaEncryptOrDecryptByPublicKey(this.toByteArray(), publicKey, false, transformation)

/**
 * RSA公钥解密
 *
 * @param publicKey
 * @param transformation
 */
@JvmOverloads
fun ByteArray.rsaDecryptByPublicKey(
    publicKey: ByteArray,
    transformation: String = DEFAULT_RSA_TRANSFORMATION
): ByteArray =
    rsaEncryptOrDecryptByPublicKey(this, publicKey, false, transformation)

private fun rsaEncryptOrDecryptByPrivateKey(
    data: ByteArray,
    privateKey: ByteArray,
    isEncrypt: Boolean,
    transformation: String = DEFAULT_RSA_TRANSFORMATION
): ByteArray {
    return try {
        //获取私钥
        val pkcs8KeySpec = PKCS8EncodedKeySpec(privateKey)
        val keyFactory = KeyFactory.getInstance(ENCRYPT_RSA)
        val priKey = keyFactory.generatePrivate(pkcs8KeySpec)

        //加/解密
        val cipher = Cipher.getInstance(transformation)
        cipher.init(if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE, priKey)
        return cipher.doFinal(data)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyArray<Byte>().toByteArray()
    }
}

/**
 * RSA私钥加密
 *
 * @param privateKey
 * @param transformation
 */
@JvmOverloads
fun String.rsaEncryptByPrivateKey(
    privateKey: ByteArray,
    transformation: String = DEFAULT_RSA_TRANSFORMATION
): ByteArray =
    rsaEncryptOrDecryptByPrivateKey(this.toByteArray(), privateKey, true, transformation)

/**
 * RSA私钥加密
 *
 * @param privateKey
 * @param transformation
 */
@JvmOverloads
fun ByteArray.rsaEncryptByPrivateKey(
    privateKey: ByteArray,
    transformation: String = DEFAULT_RSA_TRANSFORMATION
): ByteArray =
    rsaEncryptOrDecryptByPrivateKey(this, privateKey, true, transformation)

/**
 * RSA私钥解密
 *
 * @param privateKey
 * @param transformation
 */
@JvmOverloads
fun String.rsaDecryptByPrivateKey(
    privateKey: ByteArray,
    transformation: String = DEFAULT_RSA_TRANSFORMATION
): ByteArray =
    rsaEncryptOrDecryptByPrivateKey(this.toByteArray(), privateKey, false, transformation)

/**
 * RSA私钥解密
 *
 * @param privateKey
 * @param transformation
 */
@JvmOverloads
fun ByteArray.rsaDecryptByPrivateKey(
    privateKey: ByteArray,
    transformation: String = DEFAULT_RSA_TRANSFORMATION
): ByteArray =
    rsaEncryptOrDecryptByPrivateKey(this, privateKey, false, transformation)