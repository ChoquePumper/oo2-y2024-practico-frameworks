package choque.framework;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class MyFramework {
	public static final String defaultRutaArchivoPropiedades = "config.properties";
	private static final String propname_acciones = "acciones";
	private List<Accion> acciones;
	private List<String> nombreclase_acciones;
	private final String rutaArchivoPropiedades;

	private boolean salirDelPrograma;
	private Accion accionSalir;

	private Scanner scanner;

	public MyFramework(String rutaArchivoPropiedades) {
		Objects.requireNonNull(rutaArchivoPropiedades);
		this.rutaArchivoPropiedades = rutaArchivoPropiedades;
		procesarConfiguracion();
		prepararListaDeAcciones();

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
		nombreclase_acciones = new ArrayList<>();
		for (String nombreclase : prop_acciones.split(";")) {
			nombreclase = nombreclase.trim();
			if (nombreclase.isEmpty())
				throw new RuntimeException("Error al parsear la propiedad '" + propname_acciones + "'");
			nombreclase_acciones.add(nombreclase);
		}
	}

	private void prepararListaDeAcciones() {
		acciones = new ArrayList<>();
		for (String nombreclase : nombreclase_acciones) {
			acciones.add((Accion) instanciarClase(nombreclase));
		}
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
		this.scanner = new Scanner(System.in);
		while (!debeSalirDelPrograma()) {
			mostrarMenu();
			try {
				getAccion(elegirDelMenu()).ejecutar();
			} catch (OpcionInvalidaException e) {
				System.out.println(e.getMessage());
			}
		}
		this.scanner.close();
	}

	private boolean debeSalirDelPrograma() {
		return this.salirDelPrograma;
	}

	private Accion getAccion(int num) throws OpcionInvalidaException {
		if (num == 0) {
			return accionSalir;
		}
		try {
			return acciones.get(num - 1);
		} catch (IndexOutOfBoundsException e) {
			throw new OpcionInvalidaException(num);
		}

	}

	private void mostrarMenu() {
		System.out.println("Bienvenido, estas son sus opciones:\n");
		int num = 1;
		for (Accion accion : acciones) {
			mostrarOpcionDelMenu(num, accion);
			num++;
		}
		mostrarOpcionDelMenu(0, accionSalir);
		System.out.println();
	}

	private void mostrarOpcionDelMenu(int num, Accion accion) {
		System.out.printf("%d. %s (%s)\n", num, accion.nombreItemMenu(), accion.descripcionItemMenu());
	}

	private int elegirDelMenu() throws OpcionInvalidaException {
		System.out.print("Ingrese su opción: ");
		String linea = "";
		try {
			linea = scanner.nextLine();
			return Integer.parseInt(linea);
		} catch (NumberFormatException e) {
			throw new OpcionInvalidaException(linea);
		}
	}

}
