package choque.framework;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ConfigParser {

	/**
	 * Lee del archivo {@code File} y agrega las propiedades al {@code Map}.
	 *
	 * @param archivo El archivo {@link File}
	 * @param props   El mapa al cual éste método debe agregar pares clave-valor.
	 */
	void procesarConfiguracion(File archivo, Map<String, Object> props);

	default List<String> valorALista(String valor) {
		throw new UnsupportedOperationException("método no implementado");
	}
}
