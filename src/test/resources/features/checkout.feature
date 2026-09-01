# language: es
Característica: Checkout

  @smoke
  Escenario: Un usuario completa una compra de principio a fin
    Dado que estoy en la página de login
    Cuando ingreso con el usuario "standard_user"
    Y agrego al carrito el producto "Sauce Labs Backpack"
    Y abro el carrito
    Y inicio el checkout
    Y completo mis datos "Agustin", "Silveira" y "5000"
    Entonces el total incluye impuestos
    Cuando confirmo la compra
    Entonces veo la confirmación "Thank you for your order!"
