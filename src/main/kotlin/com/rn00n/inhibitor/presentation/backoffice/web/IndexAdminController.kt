package com.rn00n.inhibitor.presentation.backoffice.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/backoffice")
class IndexAdminController {

    @GetMapping
    fun index(): String {
        return "backoffice/index"
    }

    @GetMapping("/login")
    fun login(): String {
        return "backoffice/login"
    }

    @GetMapping("/account")
    fun account(
    ): String {
        return "backoffice/account"
    }

    @GetMapping("/client")
    fun client(
    ): String {
        return "backoffice/client"
    }
}