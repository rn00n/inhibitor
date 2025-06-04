package com.rn00n.inhibitor.commons.base

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
) {
    companion object {
        fun <T> of(page: Page<T>): PageResponse<T> {
            return PageResponse(
                content = page.content,
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages
            )
        }

        fun <T> ofList(list: List<T>): PageResponse<T> {
            return PageResponse(
                content = list,
                page = 0,
                size = list.size,
                totalElements = list.size.toLong(),
                totalPages = 1
            )
        }
    }
}