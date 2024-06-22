TP10-frameworks
---

# Cómo usar

Compile el módulo "framework" y genere un archivo jar.

Instancie la clase `MyFramework`. Puede pasar un archivo como parámetro en el
constructor, que por omisión es "config.properties". Luego llame al método
`ejecutar()`.

En el archivo de configuración se deben indicar los nombres de las clases que
implementen la interfaz `Accion` cuyos métodos son los siguientes:

```java

void ejecutar(); // El programa que se ejecutará

String nombreItemMenu(); // El nombre de la acción

String descripcionItemMenu(); // Una breve descripción de la acción
```

Puede revisar el módulo "utilizacion" para ver algunas implementaciones de
ejemplo.

## Ejemplo de programa

```java
void main() {
	new MyFramework("config.properties").ejecutar();
}
```

Durante la ejecución, se mostrará un menú con la lista de acciones que haya
indicado en el archivo de configuración. Podrá seleccionar una o varias.

## Archivo de configuración

Es un archivo de texto plano con formato de pares clave-valor.
El framework soporta los formatos ".properties" y ".json".

Claves admitidas:

- `acciones`: una lista de nombres de clases que implementen la interfaz
  `Accion` del framework. Nota: en ".properties" se usa ";" como delimitador.
  _(**requerido**)_
- `max-threads`: un número que indica la cantidad máxima de acciones que se
  pueden ejecutar en simultáneo. _(Valor por defecto: 2)_
- `menu`: nombre de la interfaz de menú que puede ser una de las siguientes:
    - `lanterna` _(valor por defecto)_
    - `lanterna-legacy`
    - `cli`

### Ejemplos de configuración

En ".properties":

```properties
# Esta linea es un comentario
acciones=choque.utilizacion.AccionUno; choque.utilizacion.AccionDos
#menu=cli
#menu=lanterna-legacy
menu=lanterna
max-threads=4
```

En ".json":

```json
{
  "acciones": [
    "choque.utilizacion.AccionUno",
    "choque.utilizacion.AccionDos"
  ],
  "menu": "cli",
  "max-threads": 2
}
```