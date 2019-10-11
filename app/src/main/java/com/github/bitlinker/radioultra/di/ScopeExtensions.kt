package com.github.bitlinker.radioultra.di

import org.koin.core.Koin
import org.koin.core.qualifier.TypeQualifier
import org.koin.core.scope.Scope
import org.koin.ext.getFullName

private fun Any.getScopeName() = TypeQualifier(this::class)

private fun Any.getScopeId() = this::class.getFullName() + "@" + System.identityHashCode(this)

fun Any.getOrCreateObjectScope(koin: Koin): Scope {
    return koin.getOrCreateScope(this::class.getFullName() + "@" + System.identityHashCode(this), getScopeName())
}
