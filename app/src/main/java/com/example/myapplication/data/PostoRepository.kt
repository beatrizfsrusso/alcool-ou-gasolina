package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Persistência local dos postos usando SharedPreferences + JSON (kotlinx.serialization).
 *
 * A lista inteira é gravada como uma única string JSON. É suficiente para o volume
 * de dados desta atividade (lista de postos de um usuário).
 */
class PostoRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val listaSerializer = ListSerializer(Posto.serializer())

    /** Todos os postos cadastrados, na ordem de inserção. */
    fun listar(): List<Posto> {
        val bruto = prefs.getString(CHAVE_POSTOS, null) ?: return emptyList()
        return try {
            json.decodeFromString(listaSerializer, bruto)
        } catch (e: Exception) {
            // JSON corrompido (ex.: versão antiga do app): descarta em vez de derrubar a tela.
            emptyList()
        }
    }

    fun buscar(id: String): Posto? = listar().firstOrNull { it.id == id }

    /** Insere o posto ou substitui o existente com o mesmo id (update do CRUD). */
    fun salvar(posto: Posto) {
        val atuais = listar().toMutableList()
        val indice = atuais.indexOfFirst { it.id == posto.id }
        if (indice >= 0) atuais[indice] = posto else atuais.add(posto)
        gravar(atuais)
    }

    /** Remove o posto com o id informado (delete do CRUD). */
    fun excluir(id: String) {
        gravar(listar().filterNot { it.id == id })
    }

    private fun gravar(postos: List<Posto>) {
        prefs.edit()
            .putString(CHAVE_POSTOS, json.encodeToString(listaSerializer, postos))
            .apply()
    }

    /**
     * Estado do switch (limite de 70% x 75%), persistido entre execuções do app —
     * é o que permite restaurá-lo quando o usuário volta ao aplicativo.
     */
    fun usarLimite70(): Boolean = prefs.getBoolean(CHAVE_LIMITE_70, false)

    fun definirLimite70(valor: Boolean) {
        prefs.edit().putBoolean(CHAVE_LIMITE_70, valor).apply()
    }

    private companion object {
        const val PREFS_NAME = "alcool_ou_gasolina"
        const val CHAVE_POSTOS = "postos"
        const val CHAVE_LIMITE_70 = "usar_limite_70"
    }
}
