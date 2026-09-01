# language: es
Característica: Carrito de compras

  Antecedentes:
    Dado que estoy en la página de login
    Cuando ingreso con el usuario "standard_user"

  @smoke
  Escenario: Agregar y quitar productos actualiza el contador
    Cuando agrego al carrito el producto "Sauce Labs Backpack"
    Y agrego al carrito el producto "Sauce Labs Bike Light"
    Entonces el contador del carrito muestra 2
    Cuando quito del carrito el producto "Sauce Labs Backpack"
    Entonces el contador del carrito muestra 1

  Escenario: El carrito lista los productos agregados
    Cuando agrego al carrito el producto "Sauce Labs Backpack"
    Y abro el carrito
    Entonces el carrito contiene "Sauce Labs Backpack"

  @regresion
  Esquema del escenario: Ordenar el inventario reordena los productos
    Cuando ordeno los productos por "<criterio>"
    Entonces los productos quedan ordenados por "<criterio>"

    Ejemplos:
      | criterio           |
      | nombre ascendente  |
      | nombre descendente |
      | precio ascendente  |
      | precio descendente |
