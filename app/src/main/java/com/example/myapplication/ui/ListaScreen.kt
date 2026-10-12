package com.example.myapplication.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.Posto
import com.example.myapplication.data.PostoRepository
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * Lista os postos cadastrados. Ao tocar em um item, o detalhe é aberto.
 *
 * A lista é relida do repositório a cada entrada na tela, então exclusões e edições
 * feitas nas outras telas aparecem automaticamente ao voltar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaScreen(
    repositorio: PostoRepository,
    onNovoPosto: () -> Unit,
    onAbrirPosto: (String) -> Unit
) {
    var postos by remember { mutableStateOf(emptyList<Posto>()) }
    LaunchedEffect(Unit) { postos = repositorio.listar() }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.lista_titulo)) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onNovoPosto, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.lista_novo_posto))
            }

            if (postos.isEmpty()) {
                Text(
                    text = stringResource(R.string.lista_vazia),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = stringResource(R.string.lista_contagem, postos.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(postos, key = { it.id }) { posto ->
                        ItemPosto(posto = posto, onClick = { onAbrirPosto(posto.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemPosto(posto: Posto, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = posto.nome.ifBlank { stringResource(R.string.detalhe_sem_nome) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "${stringResource(R.string.combustivel_alcool)}: " +
                        formatarPreco(posto.precoAlcool),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${stringResource(R.string.combustivel_gasolina)}: " +
                        formatarPreco(posto.precoGasolina),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemPostoPreview() {
    MyApplicationTheme {
        ItemPosto(
            posto = Posto(
                nome = "Posto Ipiranga",
                precoAlcool = 4.29,
                precoGasolina = 5.79
            ),
            onClick = {}
        )
    }
}
