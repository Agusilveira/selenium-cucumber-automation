# language: es
Característica: Login en SauceDemo

  Antecedentes:
    Dado que estoy en la página de login

  @smoke
  Escenario: Un usuario válido accede al inventario
    Cuando ingreso con el usuario "standard_user"
    Entonces veo la lista de productos
