package com.deckbuilder

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DeckbuilderApplication

fun main(args: Array<String>) {
    runApplication<DeckbuilderApplication>(*args)
}
