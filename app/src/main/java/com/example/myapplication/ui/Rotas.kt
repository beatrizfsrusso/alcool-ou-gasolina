package com.example.myapplication.ui

import kotlinx.serialization.Serializable

/** Rotas de navegação type-safe (Navigation Compose 2.8+). */

@Serializable
data object RotaLista

/** [postoId] nulo significa "novo posto"; preenchido significa edição. */
@Serializable
data class RotaFormulario(val postoId: String? = null)

@Serializable
data class RotaDetalhe(val postoId: String)
