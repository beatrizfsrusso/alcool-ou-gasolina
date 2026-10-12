package com.example.myapplication.data

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Um posto de combustível cadastrado pelo usuário.
 *
 * É serializado como JSON e gravado em [PostoRepository]. Os campos de localização
 * são opcionais porque o usuário pode negar a permissão de acesso à localização.
 */
@Serializable
data class Posto(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val precoAlcool: Double,
    val precoGasolina: Double,
    val dataCadastro: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    val temLocalizacao: Boolean
        get() = latitude != null && longitude != null
}
