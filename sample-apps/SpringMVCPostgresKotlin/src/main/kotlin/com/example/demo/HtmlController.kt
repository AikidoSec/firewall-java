package com.example.demo

import com.example.demo.models.Pet
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class HtmlController {

    @GetMapping("/")
    fun home(model: Model): String {
        return "index" // This will resolve to src/main/resources/templates/index.html
    }

    @GetMapping("/create")
    fun create(model: Model): String {
        return "create" // This will resolve to src/main/resources/templates/create.html
    }

    @GetMapping("/read_file")
    fun readFile(model: Model): String {
        return "read" // This will resolve to src/main/resources/templates/read.html
    }

    @GetMapping("/request")
    fun request(model: Model): String {
        return "request" // This will resolve to src/main/resources/templates/request.html
    }

    @GetMapping("/exec")
    fun exec(model: Model): String {
        return "execute" // This will resolve to src/main/resources/templates/execute.html
    }

    @GetMapping("/pet_page/{id}")
    fun petPage(@PathVariable("id") id: Int): String {
        return "pet_page"
    }

    @GetMapping("/benchmark")
    @ResponseBody
    fun benchmarkRoute(): String {
        Thread.sleep(1);
        return "OK"
    }
}
