# PetStore App

## Descripción General

PetStore App es una aplicación móvil diseñada para facilitar la conexión entre vendedores de productos para mascotas y clientes interesados en adquirir dichos productos. La plataforma permite a los vendedores registrarse, publicar sus artículos y gestionar sus ventas, mientras que los clientes pueden explorar un catálogo diverso de productos, realizar compras de manera segura y gestionar sus pedidos.

## Funcionalidades Principales

La aplicación se divide en dos roles principales: Cliente y Vendedor.

### Para Clientes:

*   **Registro e Inicio de Sesión:** Los clientes pueden crear una cuenta utilizando su número de teléfono o correo electrónico y contraseña.
*   **Exploración de Productos:** Visualizar un catálogo de productos organizados por categorías.
*   **Búsqueda de Productos:** Encontrar productos específicos utilizando filtros y palabras clave.
*   **Ver Detalles del Producto:** Acceder a información detallada de cada producto, incluyendo descripción, precio, imágenes y reseñas de otros usuarios.
*   **Carrito de Compras:** Agregar productos al carrito y modificar las cantidades.
*   **Proceso de Compra:** Realizar pedidos de forma segura.
*   **Gestión de Pedidos:** Rastrear el estado de los pedidos realizados.
*   **Perfil de Usuario:** Administrar la información personal y direcciones de envío.

### Para Vendedores:

*   **Registro e Inicio de Sesión:** Los vendedores pueden registrar su tienda proporcionando información relevante.
*   **Publicación de Productos:** Agregar nuevos productos al catálogo, especificando detalles como nombre, descripción, precio, stock e imágenes.
*   **Gestión de Inventario:** Actualizar la información de los productos, incluyendo stock y precios.
*   **Gestión de Pedidos:** Visualizar y administrar los pedidos recibidos de los clientes.
*   **Perfil de Vendedor:** Administrar la información de la tienda y datos de contacto.

## Tecnologías Utilizadas

*   **Plataforma:** Android (Desarrollo nativo)
*   **Lenguaje de Programación:** Kotlin
*   **Base de Datos:** Firebase Firestore para el almacenamiento de datos en tiempo real (productos, usuarios, pedidos).
*   **Autenticación:** Firebase Authentication para la gestión de usuarios (registro e inicio de sesión con correo/contraseña y número de teléfono).
*   **Almacenamiento de Archivos:** Firebase Storage para guardar imágenes de productos y perfiles.
*   **Arquitectura:** Se sigue una arquitectura MVVM (Model-View-ViewModel) para una mejor separación de responsabilidades y mantenibilidad del código.
*   **Librerías Adicionales:**
    *   Glide: Para la carga eficiente de imágenes.
    *   Retrofit/OkHttp: (Si aplica, para consumo de APIs externas) - *Actualmente no se utiliza de forma explícita en el Manifest, pero es común en proyectos Android.*
    *   Navigation Component: Para la gestión de la navegación entre pantallas.
    *   Coroutines: Para la gestión de tareas asíncronas.

## Instrucciones de Compilación y Ejecución

1.  **Clonar el Repositorio:**
    ```bash
    git clone https://[URL_DEL_REPOSITORIO].git
    ```
2.  **Configurar Firebase:**
    *   Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
    *   Registra tu aplicación Android con el nombre de paquete `com.nova.petstore` (o el que corresponda según `app/build.gradle.kts`).
    *   Descarga el archivo `google-services.json` y colócalo en el directorio `app/`.
    *   Habilita los servicios de Firebase necesarios: Firestore Database, Firebase Authentication (con los proveedores de Email/Contraseña y Teléfono), y Firebase Storage.
3.  **Abrir el Proyecto en Android Studio:**
    *   Inicia Android Studio.
    *   Selecciona "Open an existing Android Studio project".
    *   Navega hasta el directorio donde clonaste el repositorio y selecciona la carpeta raíz del proyecto.
4.  **Sincronizar Gradle:**
    *   Android Studio debería sincronizar automáticamente los archivos Gradle. Si no, haz clic en "Sync Project with Gradle Files" (el icono del elefante con una flecha).
5.  **Ejecutar la Aplicación:**
    *   Selecciona un emulador o conecta un dispositivo Android.
    *   Haz clic en el botón "Run 'app'" (el icono de play verde).

## Estructura del Proyecto (Simplificada)

```
PetStore/
├── app/                                # Módulo principal de la aplicación
│   ├── build.gradle.kts                # Configuración de compilación del módulo app
│   ├── google-services.json          # Configuración de Firebase (debes añadirlo)
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml     # Manifiesto de la aplicación
│   │   │   ├── java/com/nova/petstore/ # Código fuente Kotlin
│   │   │   │   ├── cliente/            # Funcionalidades específicas del cliente
│   │   │   │   ├── vendedor/           # Funcionalidades específicas del vendedor
│   │   │   │   ├── detalleproducto/    # Lógica para mostrar detalles de un producto
│   │   │   │   ├── adapter/            # Adaptadores para RecyclerViews
│   │   │   │   ├── model/              # Modelos de datos (POJOs/Data Classes)
│   │   │   │   ├── utils/              # Clases de utilidad
│   │   │   │   └── ...                 # Otras actividades y fragmentos principales
│   │   │   ├── res/                    # Recursos (layouts, strings, drawables, etc.)
│   │   │   │   ├── layout/             # Archivos XML de diseño de interfaz
│   │   │   │   ├── values/             # Archivos de valores (strings, colors, styles)
│   │   │   │   └── ...
│   │   ├── androidTest/              # Pruebas de instrumentación
│   │   └── test/                     # Pruebas unitarias
├── build.gradle.kts                    # Configuración de compilación a nivel de proyecto
├── gradle/                             # Archivos de Gradle Wrapper
├── ...                                 # Otros archivos de configuración
└── README.md                           # Este archivo
```

## Futuras Mejoras

*   Implementación de sistema de notificaciones para pedidos y promociones.
*   Integración de pasarelas de pago.
*   Sección de reseñas y calificaciones de productos y vendedores.
*   Chat en tiempo real entre clientes y vendedores.
*   Funcionalidad de "Favoritos" para productos.

---

Este README proporciona una visión general del proyecto PetStore. Para más detalles sobre la implementación específica, consulta el código fuente y los comentarios dentro del mismo.
