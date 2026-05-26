package com.deckbuilder.guardrails

object BudgetContext {
    private val holder = ThreadLocal<Int?>()

    fun set(maxUsd: Int?) = holder.set(maxUsd)
    fun get(): Int? = holder.get()
    fun clear() = holder.remove()
}
