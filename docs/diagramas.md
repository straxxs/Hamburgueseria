# Documentación del diseño

Los diagramas están en formato [Mermaid](https://mermaid.js.org/): se ven renderizados
en GitHub, en VS Code (extensión "Markdown Preview Mermaid Support") y se pueden
exportar a PNG/SVG pegando el código en <https://mermaid.live>.

## 1. Diagrama de clases (capa de dominio)

```mermaid
classDiagram
    direction LR

    class ArticuloMenu {
        <<abstract>>
        -int id
        -String codigo
        -String nombre
        -BigDecimal precio
        -boolean disponible
        -boolean activo
        +getTipo()* String
        +getDescripcion() String
    }
    class Producto {
        -Categoria categoria
        +getTipo() String
        +getDescripcion() String
    }
    class Combo {
        -List~ItemCombo~ items
        +getTipo() String
        +getDescripcion() String
        +agregarItem(Producto, int)
        +quitarItem(ItemCombo)
    }
    class ItemCombo {
        -Producto producto
        -int cantidad
    }
    class Categoria {
        -int id
        -String nombre
        -boolean activo
    }
    class Cliente {
        -int id
        -String dni
        -String nombre
        -String apellido
        -String telefono
        -String email
        -LocalDate fechaNacimiento
        -boolean activo
        +getNombreCompleto() String
    }
    class Carrito {
        -Map~String, ItemCarrito~ items
        +agregar(ArticuloMenu, int)
        +quitar(ArticuloMenu)
        +getTotal() BigDecimal
        +vaciar()
    }
    class ItemCarrito {
        -ArticuloMenu articulo
        -int cantidad
        +getSubtotal() BigDecimal
    }
    class Pedido {
        -int id
        -LocalDateTime fecha
        -EstadoPedido estado
        -BigDecimal total
        +confirmar()
        +cancelar()
    }
    class DetallePedido {
        -String tipoArticulo
        -int articuloId
        -String nombre
        -BigDecimal precioUnitario
        -int cantidad
        +getSubtotal() BigDecimal
    }
    class EstadoPedido {
        <<enumeration>>
        PENDIENTE
        CONFIRMADO
        CANCELADO
    }

    ArticuloMenu <|-- Producto
    ArticuloMenu <|-- Combo
    Categoria "1" <-- "0..*" Producto : pertenece a
    Combo "1" *-- "1..*" ItemCombo : se compone de
    ItemCombo "0..*" --> "1" Producto
    Carrito "1" *-- "0..*" ItemCarrito
    ItemCarrito "0..*" --> "1" ArticuloMenu
    Cliente "1" <-- "0..*" Pedido : realiza
    Pedido "1" *-- "1..*" DetallePedido
    Pedido --> EstadoPedido
```

## 2. Arquitectura en capas

```mermaid
flowchart TB
    subgraph GUI["Capa de presentación (hamburgueseria.gui)"]
        A[MenuPrincipal]
        B[PanelPedidos / PanelClientes / PanelProductos / PanelCombos / PanelCategorias]
        T[TablaModelo + UI]
    end
    subgraph BLL["Capa de lógica de negocio (hamburgueseria.modelo + hamburgueseria.servicio)"]
        S[CatalogoService / ClienteService / PedidoService]
        M[Entidades de dominio]
    end
    subgraph DAL["Capa de acceso a datos (hamburgueseria.datos)"]
        D[CategoriaDatos / ProductoDatos / ComboDatos / ClienteDatos / PedidoDatos]
        C[Conexion]
    end
    DB[(MySQL)]
    GUI --> BLL --> DAL --> DB
```

## 3. Diagrama Entidad-Relación (DER)

```mermaid
erDiagram
    CATEGORIA ||--o{ PRODUCTO : "clasifica"
    COMBO ||--|{ COMBO_PRODUCTO : "se compone de"
    PRODUCTO ||--o{ COMBO_PRODUCTO : "integra"
    CLIENTE ||--o{ PEDIDO : "realiza"
    PEDIDO ||--|{ DETALLE_PEDIDO : "contiene"
    PRODUCTO |o--o{ DETALLE_PEDIDO : "se vende en"
    COMBO |o--o{ DETALLE_PEDIDO : "se vende en"

    CATEGORIA {
        int id_categoria PK
        varchar nombre UK
        tinyint activo
    }
    PRODUCTO {
        int id_producto PK
        varchar codigo UK
        varchar nombre
        decimal precio
        tinyint disponible
        tinyint activo
        int id_categoria FK
    }
    COMBO {
        int id_combo PK
        varchar codigo UK
        varchar nombre
        decimal precio
        tinyint disponible
        tinyint activo
    }
    COMBO_PRODUCTO {
        int id_combo PK, FK
        int id_producto PK, FK
        int cantidad
    }
    CLIENTE {
        int id_cliente PK
        varchar dni UK
        varchar nombre
        varchar apellido
        varchar telefono
        varchar email
        date fecha_nacimiento
        tinyint activo
    }
    PEDIDO {
        int id_pedido PK
        int id_cliente FK
        datetime fecha
        enum estado
        decimal total
    }
    DETALLE_PEDIDO {
        int id_detalle PK
        int id_pedido FK
        enum tipo_articulo
        int id_producto FK
        int id_combo FK
        varchar nombre_articulo
        decimal precio_unitario
        int cantidad
    }
```

**Relaciones:**

| Relación | Tipo |
|---|---|
| Categoría — Producto | 1 a N |
| Combo — Producto (vía `combo_producto`) | N a M |
| Cliente — Pedido | 1 a N |
| Pedido — Detalle de pedido | 1 a N |
| Detalle — Producto / Combo | N a 1 (exactamente uno de los dos, validado con `CHECK`) |

## 4. Elementos de base de datos exigidos

| Elemento | Dónde |
|---|---|
| Stored procedures | `sp_crear_pedido`, `sp_agregar_detalle`, `sp_cambiar_estado_pedido` (llamados desde `PedidoDatos`) |
| Triggers | `trg_detalle_bi` (solo pedidos pendientes), `trg_detalle_ai` (recalcula el total), `trg_pedido_bu` (reglas de estado), `trg_categoria_au` (baja de categoría deshabilita sus productos) |
| Transacciones | `PedidoDatos.registrar` / `cambiarEstado` (commit/rollback) y `ComboDatos.insertar` / `actualizar` |
