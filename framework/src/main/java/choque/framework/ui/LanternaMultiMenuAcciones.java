package choque.framework.ui;

import choque.framework.Accion;
import choque.framework.MenuAcciones;
import choque.framework.MenuCerradoException;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class LanternaMultiMenuAcciones extends MenuAcciones {

	private final WindowBasedTextGUI textGUI;
	private String seleccionado;
	private MenuAccionesDialog dialog;

	public LanternaMultiMenuAcciones() {
		// Setup terminal and screen layers
		Terminal terminal;
		Screen screen;
		try {
			terminal = new DefaultTerminalFactory().createTerminal();
			screen = new TerminalScreen(terminal);
			screen.startScreen();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Setup WindowBasedTextGUI for dialogs
		textGUI = new MultiWindowTextGUI(screen);
	}

	@Override
	public void mostrarMenu(List<String> items) {
		List<ItemAccion> itemAccionList = items.stream()
				.map(idItem -> {
					Accion accion = getItem(idItem).orElseThrow();
					return new ItemAccion(idItem, accion.nombreItemMenu(), accion.descripcionItemMenu());
				})
				.toList();
		seleccionado = null;

		dialog = new MenuAccionesDialog("Menu de acciones", itemAccionList);
		List<String> elements = (List<String>) dialog.showDialog(textGUI);
		if (elements != null)
			seleccionado = String.join(",", elements);
	}

	@Override
	public String getInputParaMenu() throws MenuCerradoException {
		// No es la mejor forma. Limitaciones de la API de Lanterna.
		// Cuando el emulador de terminal (Swing) se cierra, también se cierra
		// el Dialog y devuelve null. Entonces con eso puedo determinar que se
		// ha cerrado por otro medio y considerarlo como cerrado con [x].
		if (seleccionado == null) throw new MenuCerradoException();
		return seleccionado;
	}

	private record ItemAccion(String idItem, String nombre, String descripcion) {
		ItemAccion {
			Objects.requireNonNull(idItem);
			Objects.requireNonNull(nombre);
			Objects.requireNonNull(descripcion);
		}

		@Override
		public String toString() {
			return String.format("%s: %s", nombre, descripcion);
		}
	}

	private static class MenuAccionesDialog extends DialogWindow {

		private CheckBoxList<ItemAccion> cbList;
		private List<String> retorno;

		/**
		 * Default constructor, takes a title for the dialog and runs code shared for dialogs
		 *
		 * @param title Title of the window
		 */
		protected MenuAccionesDialog(String title, List<ItemAccion> items) {
			super(title);
			this.setHints(List.of(Hint.CENTERED));

			// Armar cuadro de diálogo
			var panel = new Panel();
			panel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
			panel.addComponent(new Label("Seleccione una o más acciones de la lista:"));

			// Agregar los items
			cbList = new CheckBoxList<ItemAccion>();
			items.forEach(cbList::addItem);
			panel.addComponent(cbList);

			// Botón Ejecutar
			var btnEjecutar = new Button("Ejecutar");
			btnEjecutar.addListener(this::alPulsarEjecutar);
			panel.addComponent(btnEjecutar);

			this.setComponent(panel);
		}

		private void alPulsarEjecutar(Button button) {
			// Al pulsar el botón, recolectar los items seleccionados y cerrar.
			retorno = cbList.getCheckedItems().stream().map(ItemAccion::idItem).toList();
			this.close();
		}

		@Override
		public Object showDialog(WindowBasedTextGUI textGUI) {
			super.showDialog(textGUI);
			return retorno;
		}
	}

	@Override
	public void cerrar() {
		try {
			if (dialog != null)
				dialog.close();
			textGUI.getScreen().close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
