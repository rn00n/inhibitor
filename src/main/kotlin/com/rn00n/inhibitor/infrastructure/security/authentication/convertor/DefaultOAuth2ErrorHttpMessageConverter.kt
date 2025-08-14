package com.rn00n.inhibitor.infrastructure.security.authentication.convertor

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.AbstractHttpMessageConverter
import org.springframework.http.converter.GenericHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.util.Assert
import org.springframework.util.StringUtils
import java.nio.charset.StandardCharsets

class DefaultOAuth2ErrorHttpMessageConverter : AbstractHttpMessageConverter<OAuth2Error>(
    StandardCharsets.UTF_8,
    MediaType.APPLICATION_JSON,
    MediaType("application", "*+json")
) {
    private val jsonConverter: GenericHttpMessageConverter<Any> = MappingJackson2HttpMessageConverter(ObjectMapper())

    private var errorConverter: Converter<Map<String, String>, OAuth2Error> = DefaultOAuth2ErrorConverter()
    private var errorParametersConverter: Converter<OAuth2Error, Map<String, String>> = DefaultOAuth2ErrorParametersConverter()

    override fun supports(clazz: Class<*>): Boolean =
        OAuth2Error::class.java.isAssignableFrom(clazz)

    @Suppress("UNCHECKED_CAST")
    override fun readInternal(clazz: Class<out OAuth2Error>, inputMessage: HttpInputMessage): OAuth2Error {
        val map = jsonConverter.read(
            object : ParameterizedTypeReference<Map<String, Any>>() {}.type,
            null,
            inputMessage
        ) as Map<String, *>

        val stringMap = map.entries.associate { it.key to it.value?.toString().orEmpty() }
        return errorConverter.convert(stringMap)!!
    }

    override fun writeInternal(error: OAuth2Error, outputMessage: HttpOutputMessage) {
        val map: MutableMap<String, String> = requireNotNull(errorParametersConverter.convert(error)) {
            "errorParametersConverter.convert() must not return null"
        } as MutableMap<String, String>

        map["status"] = ""
        map["error"] = map.remove("error") ?: "unknown_error"
        map["description"] = map.remove("error_description") ?: "인증에 실패했습니다"
        map["code"] = map.remove("error") ?: "unknown_error"
        map.remove("error_uri") ?: ""

        jsonConverter.write(map, MediaType.APPLICATION_JSON, outputMessage)
    }

    fun setErrorConverter(converter: Converter<Map<String, String>, OAuth2Error>) {
        Assert.notNull(converter, "errorConverter cannot be null")
        this.errorConverter = converter
    }

    fun setErrorParametersConverter(converter: Converter<OAuth2Error, Map<String, String>>) {
        Assert.notNull(converter, "errorParametersConverter cannot be null")
        this.errorParametersConverter = converter
    }

    private class DefaultOAuth2ErrorConverter : Converter<Map<String, String>, OAuth2Error> {
        override fun convert(source: Map<String, String>): OAuth2Error {
            val code = source["error"]
            val desc = source["error_description"]
            val uri = source["error_uri"]
            return OAuth2Error(code ?: "invalid_request", desc, uri)
        }
    }

    private class DefaultOAuth2ErrorParametersConverter : Converter<OAuth2Error, Map<String, String>> {
        override fun convert(error: OAuth2Error): Map<String, String> {
            val map = mutableMapOf<String, String>()
            map["error"] = error.errorCode
            if (StringUtils.hasText(error.description)) {
                map["error_description"] = error.description
            }
            if (StringUtils.hasText(error.uri)) {
                map["error_uri"] = error.uri
            }
            return map
        }
    }
}