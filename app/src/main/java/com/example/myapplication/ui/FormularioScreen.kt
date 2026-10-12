package com.example.myapplication.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.Posto
import com.example.myapplication.data.PostoRepository
import com.example.myapplication.util.Localizacao
import java.util.Locale
import java.util.UUID

/**
 * Formulário de cadastro/edição de um posto.
 *
 * Reúne as três capacidades pedidas na atividade: cálculo de qual combustível compensa,
 * captura da localização do usuário (com pedido de permissão em tempo de execução) e
 * gravação/atualização do posto no armazenamento local.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    repositorio: PostoRepository,
    postoId: String?,
    onVoltar: () -> Unit
) {
    val contexto = LocalContext.current
    val postoExistente = remember(postoId) { postoId?.let { repositorio.buscar(it) } }

    var nome by rememberSaveable { mutableStateOf(postoExistente?.nome.orEmpty()) }
    var precoAlcool by rememberSaveable {
        mutableStateOf(postoExistente?.let { formatarNumero(it.precoAlcool) }.orEmpty())
    }
    var precoGasolina by rememberSaveable {
        mutableStateOf(postoExistente?.let { formatarNumero(it.precoGasolina) }.orEmpty())
    }
    // O estado do switch é lido do repositório e regravado a cada mudança, para
    // sobreviver ao fechamento do app (requisito 1).
    var usarLimite70 by rememberSaveable { mutableStateOf(repositorio.usarLimite70()) }
    var latitude by remember { mutableStateOf(postoExistente?.latitude) }
    var longitude by remember { mutableStateOf(postoExistente?.longitude) }
    var resultado by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var buscandoLocalizacao by remember { mutableStateOf(false) }

    val limitePercentual = if (usarLimite70) 0.70 else 0.75

    fun capturarLocalizacao() {
        buscandoLocalizacao = true
        Localizacao.obterLocalizacao(contexto) { local ->
            buscandoLocalizacao = false
            if (local != null) {
                latitude = local.latitude
                longitude = local.longitude
                aviso = null
            } else {
                aviso = contexto.getString(R.string.localizacao_indisponivel)
            }
        }
    }

    val launcherPermissao = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissoes ->
        if (permissoes.values.any { it }) {
            capturarLocalizacao()
        } else {
            aviso = contexto.getString(R.string.permissao_negada)
        }
    }

    fun salvar() {
        val alcool = precoAlcool.replace(",", ".").toDoubleOrNull()
        val gasolina = precoGasolina.replace(",", ".").toDoubleOrNull()
        if (alcool == null || gasolina == null || alcool <= 0 || gasolina <= 0) {
            aviso = contexto.getString(R.string.erro_precos_invalidos)
            return
        }
        repositorio.salvar(
            Posto(
                id = postoExistente?.id ?: UUID.randomUUID().toString(),
                nome = nome.trim(),
                precoAlcool = alcool,
                precoGasolina = gasolina,
                // Em edição preserva a data original do cadastro.
                dataCadastro = postoExistente?.dataCadastro ?: System.currentTimeMillis(),
                latitude = latitude,
                longitude = longitude
            )
        )
        onVoltar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (postoExistente == null) R.string.form_titulo_novo
                            else R.string.form_titulo_editar
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.voltar)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text(stringResource(R.string.form_nome)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precoAlcool,
                onValueChange = { precoAlcool = it },
                label = { Text(stringResource(R.string.form_preco_alcool)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precoGasolina,
                onValueChange = { precoGasolina = it },
                label = { Text(stringResource(R.string.form_preco_gasolina)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "${(limitePercentual * 100).toInt()}%")
                Switch(
                    checked = usarLimite70,
                    onCheckedChange = {
                        usarLimite70 = it
                        repositorio.definirLimite70(it)
                    }
                )
                Text(
                    text = stringResource(R.string.form_limite_switch),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    if (Localizacao.temPermissao(contexto)) {
                        capturarLocalizacao()
                    } else {
                        launcherPermissao.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !buscandoLocalizacao
            ) {
                Icon(Icons.Default.Place, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.form_usar_localizacao))
            }

            val lat = latitude
            val lng = longitude
            Text(
                text = when {
                    buscandoLocalizacao -> stringResource(R.string.form_buscando_localizacao)
                    lat != null && lng != null -> stringResource(
                        R.string.form_localizacao_atual,
                        formatarCoordenada(lat),
                        formatarCoordenada(lng)
                    )
                    else -> stringResource(R.string.form_sem_localizacao)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    resultado = calcularResultado(
                        contexto = contexto,
                        precoAlcool = precoAlcool,
                        precoGasolina = precoGasolina,
                        nome = nome,
                        limitePercentual = limitePercentual
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.form_calcular))
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = resultado ?: stringResource(R.string.form_placeholder_resultado),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            aviso?.let { mensagem ->
                Text(
                    text = mensagem,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(onClick = { salvar() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.form_salvar))
            }
        }
    }
}

/** Regra de decisão do app: compensa álcool se a razão álcool/gasolina ficar abaixo do limite. */
private fun calcularResultado(
    contexto: Context,
    precoAlcool: String,
    precoGasolina: String,
    nome: String,
    limitePercentual: Double
): String {
    val alcool = precoAlcool.replace(",", ".").toDoubleOrNull()
    val gasolina = precoGasolina.replace(",", ".").toDoubleOrNull()

    if (alcool == null || gasolina == null || alcool <= 0 || gasolina <= 0) {
        return contexto.getString(R.string.erro_precos_invalidos)
    }

    val razao = alcool / gasolina
    val combustivel = contexto.getString(
        if (razao <= limitePercentual) R.string.combustivel_alcool
        else R.string.combustivel_gasolina
    )
    val percentual = String.format(Locale.getDefault(), "%.1f", razao * 100)

    return if (nome.isNotBlank()) {
        contexto.getString(R.string.resultado_com_posto, nome, combustivel, percentual)
    } else {
        contexto.getString(R.string.resultado_sem_posto, combustivel, percentual)
    }
}
