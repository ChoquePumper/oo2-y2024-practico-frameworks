package choque.framework;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class MyFramework {
	public static final String defaultRutaArchivoPropiedades = "config.properties";
	private static final String propname_acciones = "acciones";

	private List<String> nombreclase_acciones;
	private final String rutaArchivoPropiedades;

	private MenuAcciones menuAcciones;
	private boolean salirDelPrograma;
	private Accion accionSalir;

	private Scanner scanner;

	public MyFramework() {
		this(defaultRutaArchivoPropiedades);
	}

	public MyFramework(String rutaArchivoPropiedades) {
		Objects.requireNonNull(rutaArchivoPropiedades);
		this.rutaArchivoPropiedades = rutaArchivoPropiedades;

		// Crear una Accion para salir del programa: clase anónima.
		this.salirDelPrograma = false;
		accionSalir = new Accion() {

			@Override
			public String nombreItemMenu() {
				return "Salir";
			}

			@Override
			public void ejecutar() {
				System.out.println("Saliendo del programa...");
				salirDelPrograma = true;
			}

			@Override
			public String descripcionItemMenu() {
				return "Salir del programa";
			}
		};

		this.scanner = new Scanner(System.in);

		procesarConfiguracion();
		prepararListaDeAcciones();
	}

	private void procesarConfiguracion() {
		// Abrir y leer el archivo de configuración
		Properties config = new Properties();
		try (var f_reader = new FileReader(rutaArchivoPropiedades)) {
			config.load(f_reader);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("No se encontró el archivo de configuración: " + rutaArchivoPropiedades);
		} catch (IOException e) {
			throw new RuntimeException("Error al leer el archivo del configuración.", e);
		}

		// Leer la propiedad de acciones
		String prop_acciones = config.getProperty(propname_acciones);
		if (Objects.isNull(prop_acciones))
			throw new RuntimeException("Falta la propiedad '" + propname_acciones + "'");

		// Guardar los nombres
		this.nombreclase_acciones = new ArrayList<>();
		Arrays.stream(prop_acciones.split(";"))
				.map(String::trim)
				.peek(nombreClase -> {
					if (nombreClase.isEmpty())
						throw new RuntimeException("Error al parsear la propiedad '" + propname_acciones + "'");
				}).forEach(nombreClase -> nombreclase_acciones.add(nombreClase));

	}

	private void prepararListaDeAcciones() {
		this.menuAcciones = new MenuAccionesCLI(this.scanner);
		// Usar números como identificadores
		int i = 1;
		for (String nombreclase : nombreclase_acciones) {
			this.menuAcciones.agregarItem(Integer.toString(i), ((Accion) instanciarClase(nombreclase)));
			i++;
		}
		// Agregar una acción para salir
		this.menuAcciones.agregarItem("0", this.accionSalir);
	}

	private static Object instanciarClase(String nombreclase) {
		try {
			Class<?> clase = Class.forName(nombreclase);
			// Se usará el constructor por defecto: sin parámetros.
			return clase.getDeclaredConstructor().newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
		         | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("No se pudo crear una instancia de '" + nombreclase + "'", e);
		}
	}

	/**
	 * Ejecuta el framework.
	 */
	public void ejecutar() {
		while (!debeSalirDelPrograma()) {
			mostrarMenu();
			try {
				elegirDelMenu().ejecutar();
			} catch (OpcionInvalidaException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private boolean debeSalirDelPrograma() {
		return this.salirDelPrograma;
	}

	private void mostrarMenu() {
		System.out.println("Bienvenido, estas son sus opciones:\n");
		this.menuAcciones.mostrarMenu();
	}

	private Accion elegirDelMenu() throws OpcionInvalidaException {
		return this.menuAcciones.elegirDelMenu(false);
	}


	private static class MenuAccionesCLI extends MenuAcciones {

		private final Scanner scanner;

		MenuAccionesCLI(Scanner scanner) {
			this.scanner = scanner;
		}

		@Override
		void mostrarMenu(List<String> items) {
			for (String idItem : items) {
				Accion accion = getItem(idItem).orElseThrow();
				System.out.printf("%s. %s (%s)\n", idItem, accion.nombreItemMenu(), accion.descripcionItemMenu());
			}
		}

		@Override
		String getInputParaMenu() {
			System.out.print("Ingrese su opción: ");
			String linea = "";
			linea = scanner.nextLine();
			return linea;
		}
	}
}
