package choque.framework;

public interface Accion {
	/**
	 * El programa que se ejecutará.
	 */
	void ejecutar();

	/**
	 * @return El nombre a mostrar en el menú.
	 */
	String nombreItemMenu();

	String descripcionItemMenu();
}
