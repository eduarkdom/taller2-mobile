@file:Suppress("UNUSED_EXPRESSION")

package com.example.entrega2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.entrega2.db.AppDatabase.Companion.getInstance
import com.example.entrega2.db.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppProductosUI()
        }
    }
}
enum class Accion {
    LISTAR, CREAR, EDITAR
}
@Composable
fun AppProductosUI() {
    val contexto = LocalContext.current
    val (productos, setProductos) = remember{ mutableStateOf(
        emptyList<Producto>() ) }
    val (seleccion, setSeleccion) = remember{
        mutableStateOf<Producto?>(null) }
    val (accion, setAccion) = remember{
        mutableStateOf(Accion.LISTAR) }

    LaunchedEffect(productos) {
        withContext(Dispatchers.IO) {
            val db = getInstance( contexto )
            setProductos(db.ProductoDao().getAll() )
            Log.v("AppProductosUI", "LaunchedEffect()")
        }
    }
    val onSave = {
        setAccion(Accion.LISTAR)
        setProductos(emptyList())
    }
    when(accion) {
        Accion.CREAR  -> ProductoFormUI(null, onSave)
        Accion.EDITAR -> ProductoFormUI(seleccion, onSave)
        else          -> ProductosListadoUI(
            productos,
            onAdd = { setAccion( Accion.CREAR ) },
            onEdit = { producto ->
                setSeleccion(producto)
                setAccion( Accion.EDITAR)
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosListadoUI(productos:List<Producto>, onAdd:() -> Unit = {},
                       onEdit:(c:Producto) -> Unit = {}) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAdd() },
                icon = {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.create_button))
                },
                text = { Text(stringResource(id = R.string.create_button)) }
            )
        }
    ) { contentPadding ->
        if( productos.isNotEmpty() ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(productos) { producto ->
                    ProductoItemUI(producto) {
                        onEdit(producto)
                    }
                }
            }
        } else {
            Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding), contentAlignment = Alignment.Center) {
                Text("No hay productos que mostrar")
            }
        }
    }
}
@Composable
fun ProductoItemUI(producto: Producto, onClick: () -> Unit = {}) {
    val contexto = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick }
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        Image(
            painter = painterResource(id = R.drawable.carro),
            contentDescription = "Imagen Carro de Compras"
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = producto.comprado,
                    onCheckedChange = { isChecked ->
                        producto.comprado = isChecked
                        val dao = getInstance(contexto).ProductoDao()
                        dao.update(producto)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Transparent)
                )
                if (producto.comprado) {
                    Icon(
                        painter = painterResource(id = R.drawable.cartyes),
                        contentDescription = "Producto comprado"
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.cartnoyet),
                        contentDescription = "Producto no comprado"
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormUI(c: Producto?, onSave: () -> Unit = {}) {
    val contexto = LocalContext.current
    val (producto, setProducto) = remember { mutableStateOf(c?.producto ?: "") }
    val (comprado, setComprado) = remember {
        mutableStateOf(c?.comprado.toString())
    }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.account_box),
                contentDescription = "Imagen de usuario"
            )
            TextField(value = producto, onValueChange = { setProducto(it) }, label = { Text("Nombre Producto") })
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = comprado,
                onValueChange = { setComprado(it) },
                label = { Text("Comprado") }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    val dao = getInstance(contexto).ProductoDao()
                    val product = Producto(c?.id ?: 0, producto, comprado.toBoolean())
                    if (product.id > 0) {
                        dao.update(product)
                    } else {
                        dao.insert(product)
                    }
                    snackbarHostState.showSnackbar("Se ha guardado a ${product.producto}")
                    onSave()
                }
            }) {
                var textoGuardar = "Crear"
                if ((c?.id ?: 0) > 0) {
                    textoGuardar = "Guardar"
                }
                Text(textoGuardar)
            }
            if ((c?.id ?: 0) > 0) {
                Button(onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        val dao = getInstance(contexto).ProductoDao()
                        snackbarHostState.showSnackbar("Eliminando el producto de ${c?.producto}")
                        if (c != null) {
                            dao.delete(c)
     }
onSave()
}
}) {
    Text(stringResource(id = R.string.delete_button))
}
}
}
}
}
