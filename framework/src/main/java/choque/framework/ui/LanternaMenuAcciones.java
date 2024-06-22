package choque.framework.ui;

import choque.framework.Accion;
import choque.framework.MenuAcciones;
import choque.framework.MenuCerradoException;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.List;

public class LanternaMenuAcciones extends MenuAcciones {
	final WindowBasedTextGUI textGUI;

	private String opcionSeleccionada;

	public LanternaMenuAcciones() {
		// Setup terminal and screen layers
		Terminal terminal = null;
		Screen screen = null;
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
		var builder = new ActionListDialogBuilder()
				.setTitle("Menú de acciones")
				.setDescription("Seleccione una acción")
				.setCanCancel(false);
		opcionSeleccionada = null;

		// Agregar acciones
		items.forEach((idItem) -> {
			Accion accion = getItem(idItem).orElseThrow();
			String label = accion.nombreItemMenu() + ": " + accion.descripcionItemMenu();
			builder.addAction(label, () -> {
				opcionSeleccionada = idItem;
			});
		});

		builder.build().showDialog(textGUI);
	}

	@Override
	public String getInputParaMenu() throws MenuCerradoException {
		if (opcionSeleccionada == null) throw new MenuCerradoException();
		return opcionSeleccionada;
	}

	@Override
	public void cerrar() {
		try {
			textGUI.getScreen().close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
