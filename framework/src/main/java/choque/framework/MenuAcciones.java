package choque.framework;

import java.util.*;
import java.util.stream.IntStream;

public abstract class MenuAcciones {

	// Colección de los items del menú.
	private final Map<String, Accion> items;
	private final List<String> ordenIDs;

	public MenuAcciones() {
		this.items = new HashMap<>();
		this.ordenIDs = new ArrayList<>();
	}

	void agregarItem(String id, Accion accion) {
		Objects.requireNonNull(id, "Se debe especificar un id");
		Objects.requireNonNull(accion);

		if (!validarId(id = id.trim()))
			throw new RuntimeException("El id es inválido.");

		if (items.containsKey(id))
			throw new RuntimeException("Ya existe un elemento con id " + id);

		items.put(id, accion);
		ordenIDs.add(id);
	}

	private static boolean validarId(String id) {
		char[] chars = id.toCharArray();
		Character[] objChars = new Character[chars.length];
		Arrays.setAll(objChars, i -> chars[i]);
		return Arrays.stream(objChars).allMatch(Character::isLetterOrDigit);
	}

	public Optional<Accion> getItem(String id) {
		return Optional.ofNullable(items.get(id));
	}

	public List<Accion> getItems(String... ids) {
		return Arrays.stream(ids).map(items::get).toList();
	}

	List<String> verOrdenItems() {
		return List.copyOf(ordenIDs);
	}

	void reordenar(String... ids) {
		reordenar(Arrays.asList(ids));
	}

	void reordenar(List<String> ids) {
		List<String> resto = ordenIDs.stream().dropWhile(ids::contains).toList();
		ordenIDs.removeAll(resto);
		// TODO: implementar el reordenado.
		ordenIDs.addAll(resto);
	}

	public abstract void mostrarMenu(List<String> items);

	void mostrarMenu() {
		mostrarMenu(verOrdenItems());
	}

	public List<Accion> elegirDelMenu(boolean mostrarMenu) throws OpcionInvalidaException, MenuCerradoException {
		if (mostrarMenu) this.mostrarMenu();
		String input = getInputParaMenu();

		// Parsing del input
		List<String> ids = Arrays.stream(input.split(",")).map(String::trim).toList();
		List<Accion> acciones = this.getItems(ids.toArray(String[]::new));

		// Lanzar excepción si falta alguna Accion
		List<String> faltantes = IntStream.range(0, ids.size()).filter(i -> acciones.get(i) == null)
				.mapToObj(ids::get).toList();
		if (!faltantes.isEmpty())
			throw new OpcionInvalidaException("Opciones inválidas: " + String.join(",", faltantes));

		return acciones.stream().dropWhile(Objects::isNull).toList();
	}

	public abstract String getInputParaMenu() throws MenuCerradoException;

	/**
	 * Sobreescribible.
	 */
	public void cerrar() {
	}
}
