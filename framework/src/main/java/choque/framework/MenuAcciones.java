package choque.framework;

import java.util.*;

abstract class MenuAcciones {

	// Colección de los items del menú.
	private final Map<String, Accion> items;
	private final List<String> ordenIDs;

	MenuAcciones() {
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

	Optional<Accion> getItem(String id) {
		return Optional.ofNullable(items.get(id));
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

	abstract void mostrarMenu(List<String> items);

	void mostrarMenu() {
		mostrarMenu(verOrdenItems());
	}

	public Accion elegirDelMenu(boolean mostrarMenu) throws OpcionInvalidaException {
		if (mostrarMenu) this.mostrarMenu();
		String input = getInputParaMenu();

		// Parsing del input
		String id = input.trim();
		if (!validarId(id))
			throw new OpcionInvalidaException("Id inválido");
		return this.getItem(id)
				.orElseThrow(() -> new OpcionInvalidaException((Object) id));
	}

	abstract String getInputParaMenu();
}
