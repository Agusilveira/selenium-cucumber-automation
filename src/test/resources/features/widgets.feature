# language: es
Característica: Widgets difíciles

  @regresion
  Escenario: Un elemento que carga con retraso se detecta sin esperas fijas
    Dado que abro la página de widgets "/dynamic_loading/2"
    Cuando disparo la carga diferida
    Entonces el texto cargado es "Hello World!"

  @regresion
  Esquema del escenario: Leer contenido dentro de frames anidados
    Dado que abro la página de widgets "/nested_frames"
    Entonces el texto del frame "<ruta>" es "<esperado>"

    Ejemplos:
      | ruta                     | esperado |
      | frame-top > frame-left   | LEFT     |
      | frame-top > frame-middle | MIDDLE   |
      | frame-top > frame-right  | RIGHT    |
      | frame-bottom             | BOTTOM   |

  @regresion
  Esquema del escenario: Manejo de alertas de JavaScript
    Dado que abro la página de widgets "/javascript_alerts"
    Cuando disparo la alerta "<tipo>" y la "<accion>"
    Entonces el resultado indica "<mensaje>"

    Ejemplos:
      | tipo    | accion   | mensaje                           |
      | alert   | acepto   | You successfully clicked an alert |
      | confirm | acepto   | You clicked: Ok                   |
      | confirm | descarto | You clicked: Cancel               |

  @regresion
  Escenario: Leer y validar los datos de una tabla
    Dado que abro la página de widgets "/tables"
    Entonces la tabla tiene 4 filas
    Y la columna "Email" contiene "jdoe@hotmail.com"
