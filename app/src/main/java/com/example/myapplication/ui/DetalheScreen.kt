package com.example.myapplication.ui

import android.content.ActivityNotFoundException
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.PostoRepository
import com.example.myapplication.util.Localizacao
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * Detalhes de um posto: valores gravados, data do cadastro, localização e as ações
 * de editar, excluir e abrir a localização no mapa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalheScreen(
    repositorio: PostoRepository,
    postoId: String,
    onVoltar: () -> Unit,
    onEditar: (String) -> Unit,
    onExcluido: () -> Unit
) {
    val contexto = LocalContext.current
    val posto = remember(postoId) { repositorio.buscar(postoId) }
    var confirmarExclusao by remember { mutableStateOf(false) }
    var aviso by remember { mutableStateOf<String?>(null) }

    // O posto pode ter sido excluído em outra tela: sai da tela de detalhe.
    if (posto == null) {
        LaunchedEffect(postoId) { onExcluido() }
        return
    }

    val formatoData = remember {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
    }
    val limite = if (repositorio.usarLimite70()) 0.70 else 0.75
    val melhorCombustivel = if (posto.precoGasolina > 0 &&
        posto.precoAlcool / posto.precoGasolina <= limite
    ) {
        R.string.combustivel_alcool
    } else {
        R.string.combustivel_gasolina
    }

    val nomeExibido = posto.nome.ifBlank { stringResource(R.string.detalhe_sem_nome) }
    val lat = posto.latitude
    val lng = posto.longitude
    val temLocalizacao = lat != null && lng != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detalhe_titulo)) },
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
            Text(
                text = nomeExibido,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinhaDetalhe(
                        rotulo = stringResource(R.string.combustivel_alcool),
                        valor = formatarPreco(posto.precoAlcool)
                    )
                    LinhaDetalhe(
                        rotulo = stringResource(R.string.combustivel_gasolina),
                        valor = formatarPreco(posto.precoGasolina)
                    )
                    HorizontalDivider()
                    LinhaDetalhe(
                        rotulo = stringResource(R.string.detalhe_recomendacao),
                        valor = stringResource(melhorCombustivel)
                    )
                    LinhaDetalhe(
                        rotulo = stringResource(R.string.detalhe_data),
                        valor = formatoData.format(Date(posto.dataCadastro))
                    )
                    LinhaDetalhe(
                        rotulo = stringResource(R.string.detalhe_localizacao),
                        valor = if (temLocalizacao) {
                            "${formatarCoordenada(lat)} , ${formatarCoordenada(lng)}"
                        } else {
                            stringResource(R.string.detalhe_localizacao_indisponivel)
                        }
                    )
                }
            }

            aviso?.let { mensagem ->
                Text(
                    text = mensagem,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    if (lat != null && lng != null) {
                        val intent = Localizacao.intentMapa(contexto, lat, lng, posto.nome)
                        if (intent == null) {
                            aviso = contexto.getString(R.string.mapa_indisponivel)
                        } else {
                            try {
                                contexto.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                aviso = contexto.getString(R.string.mapa_indisponivel)
                            }
                        }
                    }
                },
                enabled = temLocalizacao,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.detalhe_ver_mapa))
            }

            OutlinedButton(
                onClick = { onEditar(posto.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.detalhe_editar))
            }

            OutlinedButton(
                onClick = { confirmarExclusao = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.detalhe_excluir),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = { confirmarExclusao = false },
            title = { Text(stringResource(R.string.excluir_titulo)) },
            text = { Text(stringResource(R.string.excluir_mensagem, nomeExibido)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                        repositorio.excluir(posto.id)
                        onExcluido()
                    }
                ) {
                    Text(stringResource(R.string.detalhe_excluir))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarExclusao = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}

@Composable
private fun LinhaDetalhe(rotulo: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
