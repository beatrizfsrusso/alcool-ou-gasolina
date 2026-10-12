package com.example.myapplication.ui

import java.util.Locale

/**
 * Formatação numérica que respeita o idioma do sistema — em português o separador
 * decimal é a vírgula, em inglês é o ponto.
 */

internal fun formatarPreco(valor: Double): String =
    String.format(Locale.getDefault(), "R$ %.2f", valor)

internal fun formatarNumero(valor: Double): String =
    String.format(Locale.getDefault(), "%.2f", valor)

internal fun formatarCoordenada(valor: Double): String =
    String.format(Locale.getDefault(), "%.5f", valor)
