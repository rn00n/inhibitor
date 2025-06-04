package com.rn00n.inhibitor.commons.security.legacy

import org.springframework.security.crypto.codec.Base64
import org.springframework.security.crypto.codec.Hex
import org.springframework.security.crypto.codec.Utf8
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class ShaPasswordEncoder(
    private val algorithm: String,
    private var iterations: Int = 1,
    private var encodeHashAsBase64: Boolean,
) {

    init {
        this.getMessageDigest()
    }

    fun matches(rawPassword: String?, encodedPassword: String?, salt: String?): Boolean {
        val encodePasswordFromRawPassword: String? = encodePassword(rawPassword, salt)
        return encodePasswordFromRawPassword == encodedPassword
    }

    fun encodePassword(rawPass: String?, salt: Any?): String? {
        val saltedPass: String = this.mergePasswordAndSalt(rawPass, salt, false)
        val messageDigest: MessageDigest = this.getMessageDigest()
        var digest = messageDigest.digest(Utf8.encode(saltedPass))

        for (i in 1..<this.iterations) {
            digest = messageDigest.digest(digest)
        }

        return if (this.getEncodeHashAsBase64()) Utf8.decode(Base64.encode(digest)) else String(Hex.encode(digest))
    }

    fun mergePasswordAndSalt(password: String?, salt: Any?, strict: Boolean): String {
        var password = password
        if (password == null) {
            password = ""
        }

        require(
            !(strict && salt != null && (salt.toString().lastIndexOf("{") != -1 || salt.toString().lastIndexOf(
                "}"
            ) != -1))
        ) { "Cannot use { or } in salt.toString()" }
        return if (salt != null && "" != salt) password + "{" + salt.toString() + "}" else password
    }

    fun getEncodeHashAsBase64(): Boolean {
        return this.encodeHashAsBase64
    }

    @Throws(IllegalArgumentException::class)
    private fun getMessageDigest(): MessageDigest {
        try {
            return MessageDigest.getInstance(this.algorithm)
        } catch (var2: NoSuchAlgorithmException) {
            throw IllegalArgumentException("No such algorithm [" + this.algorithm + "]")
        }
    }
}
