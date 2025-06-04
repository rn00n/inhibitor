package com.rn00n.inhibitor.application.auth.filter

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.springframework.http.MediaType
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.get

class JsonToFormDataRequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {

    private val logger = KotlinLogging.logger {}

    private val paramMap: Map<String, Array<String>>

    init {
        val bodyString = StreamUtils.copyToString(request.inputStream, StandardCharsets.UTF_8)

        logger.info { "request:\n$bodyString" }

        // ObjectMapper를 통해 JSON 문자열을 Map으로 파싱
        val mapper = ObjectMapper()
        val parsed: Map<String, Any> = mapper.readValue(bodyString, Map::class.java) as Map<String, Any>

        paramMap = parsed.mapValues { arrayOf(it.value.toString()) }
    }

    override fun getParameter(name: String): String? = paramMap[name]?.firstOrNull()

    override fun getParameterMap(): MutableMap<String, Array<String>> = paramMap.toMutableMap()

    override fun getParameterNames(): Enumeration<String> = Collections.enumeration(paramMap.keys)

    override fun getParameterValues(name: String?): Array<String>? = paramMap[name]

    override fun getHeader(name: String): String? {
        return if (name.equals("Content-Type", ignoreCase = true)) {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE
        } else {
            super.getHeader(name)
        }
    }

    override fun getContentType(): String = MediaType.APPLICATION_FORM_URLENCODED_VALUE
}