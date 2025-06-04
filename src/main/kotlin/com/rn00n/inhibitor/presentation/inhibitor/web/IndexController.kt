package com.rn00n.inhibitor.presentation.inhibitor.web

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping
class IndexController {

    @GetMapping("/")
    fun index(
        request: HttpServletRequest,
        model: Model
    ): String {
        model.addAttribute("title", "Hello!")
        model.addAttribute("url", request.requestURL)
        return "index"
    }

}