# language: es
Característica: Login en SauceDemo

  Antecedentes:
    Dado que estoy en la página de login

  @smoke
  Escenario: Un usuario válido accede al inventario
    Cuando ingreso con el usuario "standard_user"
    Entonces veo la lista de productos

  Escenario: Un usuario bloqueado no puede entrar
    Cuando ingreso con el usuario "locked_out_user"
    Entonces veo el mensaje de error "Sorry, this user has been locked out."

  @regresion
  Esquema del escenario: Distintos usuarios obtienen distinto resultado
    Cuando ingreso con el usuario "<usuario>" y la contraseña "<contrasenia>"
    Entonces el resultado del login es "<resultado>"

    Ejemplos:
      | usuario             | contrasenia  | resultado |
      | standard_user       | secret_sauce | exito     |
      | problem_user        | secret_sauce | exito     |
      | locked_out_user     | secret_sauce | error     |
      | standard_user       | mala_clave   | error     |
      | usuario_inexistente | secret_sauce | error     |

  @regresion
  Escenario: Un usuario con la app lenta igual llega al inventario
    Cuando ingreso con el usuario "performance_glitch_user"
    Entonces veo la lista de productos
