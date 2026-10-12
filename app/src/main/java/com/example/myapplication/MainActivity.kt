package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.myapplication.data.PostoRepository
import com.example.myapplication.ui.DetalheScreen
import com.example.myapplication.ui.FormularioScreen
import com.example.myapplication.ui.ListaScreen
import com.example.myapplication.ui.RotaDetalhe
import com.example.myapplication.ui.RotaFormulario
import com.example.myapplication.ui.RotaLista
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Um único repositório para o app inteiro: todas as telas leem e gravam nele.
        val repositorio = PostoRepository(applicationContext)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppAlcoolOuGasolina(repositorio)
                }
            }
        }
    }
}

@Composable
fun AppAlcoolOuGasolina(repositorio: PostoRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = RotaLista) {
        composable<RotaLista> {
            ListaScreen(
                repositorio = repositorio,
                onNovoPosto = { navController.navigate(RotaFormulario()) },
                onAbrirPosto = { id -> navController.navigate(RotaDetalhe(id)) }
            )
        }

        composable<RotaFormulario> { backStackEntry ->
            val rota = backStackEntry.toRoute<RotaFormulario>()
            FormularioScreen(
                repositorio = repositorio,
                postoId = rota.postoId,
                onVoltar = { navController.popBackStack() }
            )
        }

        composable<RotaDetalhe> { backStackEntry ->
            val rota = backStackEntry.toRoute<RotaDetalhe>()
            DetalheScreen(
                repositorio = repositorio,
                postoId = rota.postoId,
                onVoltar = { navController.popBackStack() },
                onEditar = { id -> navController.navigate(RotaFormulario(id)) },
                onExcluido = { navController.popBackStack() }
            )
        }
    }
}
