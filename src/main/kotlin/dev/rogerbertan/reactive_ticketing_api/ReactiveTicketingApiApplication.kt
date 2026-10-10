package dev.rogerbertan.reactive_ticketing_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReactiveTicketingApiApplication

fun main(args: Array<String>) {
	runApplication<ReactiveTicketingApiApplication>(*args)
}
