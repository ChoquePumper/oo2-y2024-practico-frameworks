package choque.framework;

import java.io.Serial;

public class OpcionInvalidaException extends Exception {
	@Serial
	private static final long serialVersionUID = -626995644159115971L;

	public OpcionInvalidaException(Object seleccion) {
		super("Opcion invalida: " + seleccion.toString());
	}
}
