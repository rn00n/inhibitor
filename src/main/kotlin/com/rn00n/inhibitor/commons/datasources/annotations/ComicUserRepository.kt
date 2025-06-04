package com.rn00n.inhibitor.commons.datasources.annotations

import org.springframework.stereotype.Repository

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Repository
annotation class ComicUserRepository
