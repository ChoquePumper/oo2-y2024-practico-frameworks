package choque.framework.configparsers;

import choque.framework.ConfigParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PropertiesParser implements ConfigParser {
	@Override
	public void procesarConfiguracion(File archivo, Map<String, Object> props) {
		// Abrir y leer el archivo de configuración
		Properties config = new Properties();
		try (var f_reader = new FileReader(archivo)) {
			config.load(f_reader);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("No se encontró el archivo de configuración: " + archivo.getName());
		} catch (IOException e) {
			throw new RuntimeException("Error al leer el archivo del configuración.", e);
		}

		config.stringPropertyNames().forEach(property -> {
			props.put(property, config.getProperty(property));
		});
	}

	/**
	 * Pasa el String a una lista usando ";" como delimitador, y aplica un trim a cada elemento.
	 * No se permiten elementos vacíos.
	 *
	 * @param valor El valor a convertir.
	 * @return {@code List<String>}
	 */
	@Override
	public List<String> valorALista(String valor) {
		return Arrays.stream(valor.split(";"))
				.map(String::trim)
				.peek(elemento -> {
					if (elemento.isEmpty())
						throw new RuntimeException("Error al parsear la propiedad");
				}).toList();
	}
}
