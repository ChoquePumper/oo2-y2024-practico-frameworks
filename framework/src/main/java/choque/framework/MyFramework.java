package choque.framework;

import choque.framework.configparsers.JSONParser;
import choque.framework.configparsers.PropertiesParser;
import choque.framework.ui.LanternaMenuAcciones;
import choque.framework.ui.LanternaMultiMenuAcciones;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class MyFramework {
	public static final String defaultRutaArchivoPropiedades = "config.properties";
	private static final String propname_acciones = "acciones";
	private static final String propname_menu = "menu";
	private static final String propname_maxThreads = "max-threads";
	private static final Map<String, Object> defaultProps =
			Map.of(propname_menu, "lanterna", propname_maxThreads, 1);
	private final File archivoConfiguracion;
	private final Map<String, Object> props = new HashMap<>(defaultProps);

	private final Map.Entry<String, Supplier<MenuAcciones>>[] menusDisponibles = new Map.Entry[]{
			Map.entry("cli", (Supplier<MenuAcciones>) MenuAccionesCLI::new),
			Map.entry("lanterna-legacy", (Supplier<MenuAcciones>) LanternaMenuAcciones::new),
			Map.entry("lanterna", (Supplier<MenuAcciones>) LanternaMultiMenuAcciones::new),
	};

	private MenuAcciones menuAcciones;
	private boolean salirDelPrograma;
	private Accion accionSalir;

	public MyFramework() {
		this(defaultRutaArchivoPropiedades);
	}

	public MyFramework(String rutaArchivoPropiedades) {
		this(new File(rutaArchivoPropiedades));
	}

	public MyFramework(File archivoConfiguracion) {
		this.archivoConfiguracion = Objects.requireNonNull(archivoConfiguracion);

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

		procesarConfiguracion();
		prepararListaDeAcciones();
	}

	private ConfigParser obtenerConfigParser() {
		// TODO: agregar lógica para determinar que otro parser usar.

		if (archivoConfiguracion.getName().endsWith(".json"))
			return new JSONParser();

		// Valor por defecto: PropertiesParser
		return new PropertiesParser();
	}

	private void procesarConfiguracion() {
		// Obtener un parser y procesar el archivo de configuración
		var parser = obtenerConfigParser();
		parser.procesarConfiguracion(archivoConfiguracion, props);

		// Acciones: si el valor es un String, intentar pasarlo a una lista de Strings
		if (props.get(propname_acciones) instanceof String valor)
			props.put(propname_acciones, parser.valorALista(valor));

		// max-threads: debe ser Integer
		if (props.get(propname_maxThreads) instanceof String valor)
			props.put(propname_maxThreads, Integer.parseInt(valor));

		// Chequeo del tipo de algunos valores.
		try {
			Object o;
			o = (List<String>) props.get(propname_acciones);
			o = props.computeIfPresent(propname_menu, (k, v) -> (String) v);
			o = props.computeIfPresent(propname_maxThreads, (k, v) -> (Integer) v);
		} catch (ClassCastException e) {
			throw new RuntimeException("Tipo de valor en la configuración inválido", e);
		}
	}

	private MenuAcciones obtenerMenuAcciones() {
		String menu = (String) props.get(propname_menu);
		var supplier = Map.ofEntries(menusDisponibles).get(menu);
		if (supplier == null)
			throw new RuntimeException("Menú no disponible: '" + menu + "'");
		return supplier.get();
	}

	private void prepararListaDeAcciones() {
		// Crea el menú según lo especificado en las propiedades
		this.menuAcciones = obtenerMenuAcciones();

		// Usar números como identificadores
		int i = 1;
		//noinspection unchecked
		for (String nombreclase : (List<String>) this.props.get(propname_acciones)) {
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
		var executorService = Executors.newFixedThreadPool((Integer) props.get(propname_maxThreads));
		while (!debeSalirDelPrograma()) {
			mostrarMenu();
			List<Accion> acciones;
			try {
				acciones = elegirDelMenu();
				// Generar una lista de Callable(s) para que lo use ExecutorService
				executorService.invokeAll(acciones.stream().map(this::generarCallable).toList());
			} catch (OpcionInvalidaException e) {
				System.out.println(e.getMessage());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		executorService.shutdown();
		this.menuAcciones.cerrar();
	}

	private Callable<Void> generarCallable(Accion accion) {
		return () -> {
			accion.ejecutar();
			return null;
		};
	}

	private boolean debeSalirDelPrograma() {
		return this.salirDelPrograma;
	}

	private void mostrarMenu() {
		this.menuAcciones.mostrarMenu();
	}

	private List<Accion> elegirDelMenu() throws OpcionInvalidaException {
		return this.menuAcciones.elegirDelMenu(false);
	}


	private static class MenuAccionesCLI extends MenuAcciones {

		private final Scanner scanner;

		MenuAccionesCLI() {
			this(new Scanner(System.in));
		}

		MenuAccionesCLI(Scanner scanner) {
			this.scanner = Objects.requireNonNull(scanner);
		}

		@Override
		public void mostrarMenu(List<String> items) {
			System.out.println();
			System.out.println("Bienvenido, estas son sus opciones:\n");
			for (String idItem : items) {
				Accion accion = getItem(idItem).orElseThrow();
				System.out.printf("%s. %s (%s)\n", idItem, accion.nombreItemMenu(), accion.descripcionItemMenu());
			}
		}

		@Override
		public String getInputParaMenu() {
			System.out.print("Ingrese su opción: ");
			String linea = "";
			linea = scanner.nextLine();
			return linea;
		}
	}
}
