package program;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;

public class ManejadorUniversal {
	private static ArrayList<ArrayList<String>> reservas = new ArrayList<>();

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
			System.out.println("¿Qué desea hacer?: ");
			System.out.println("1: Convertir XML to JSON");
			System.out.println("2: Convertir JSON to XML");
			System.out.println("3: Convertir en MySQL");
			System.out.println("4: Recuperar datos de la base de datos MySQL");
			System.out.println("5: Pasar de JSON a JSON ");
			System.out.println("6: Pasar de XML a XML ");
			int opcion = scanner.nextInt();
			scanner.nextLine();
			procesarOpcion(opcion, scanner);
	}

	private static void procesarOpcion(int opcion,Scanner scanner) {
		System.out.println("Ingrese la ubicación del archivo (XML o JSON): ");
		String file = scanner.nextLine();
		File fichero = new File(file);
		if (!fichero.exists()) {
			System.out.println("Fichero no encontrado");
		} else {
			switch (opcion) {
			case 1:
				procesarOpcionLeerArchivo(file, "xml");
				break;
			case 2:
				procesarOpcionLeerArchivo(file, "json");
				break;
			case 3:
				procesarOpcionInsertarDatos(file);
				break;
			case 4:
				procesarOpcionExportarReservas();
				break;
			case 5:
				procesarOpcionJSONtoJSON(file);
				break;
			case 6:
				procesarOpcionXMLtoXML(file);
				break;
			default:
				System.out.println("Opción no válida");
				break;
			}
		}
		
	}

	private static void procesarOpcionJSONtoJSON(String file) {
		String contenido = leerContenidoArchivo(file);
		reservas = extraerValorJSON(contenido);
		generarJSON(reservas, "nuevo_fichero.json");
		System.out.println("JSON convertido y guardado correctamente.");
	}
	private static void procesarOpcionXMLtoXML(String file) {
		String contenido = leerContenidoArchivo(file);
		reservas = extraerValorXML(contenido);
		generarXML(reservas, "nuevo_fichero.xml");
		System.out.println("JSON convertido y guardado correctamente.");
	}

	private static void procesarOpcionLeerArchivo(String file, String formato) {
		String contenido = leerContenidoArchivo(file);
		reservas = (formato.equals("xml")) ? extraerValorXML(contenido) : extraerValorJSON(contenido);
		for (ArrayList<String> v : reservas) {
			System.out.println(v);
		}
		if (formato.equals("xml")) {
			generarJSON(reservas, "fichero.json");
		} else {
			generarXML(reservas,"fichero.xml");
		}
	}

	private static void procesarOpcionInsertarDatos(String file) {
		ManejadoraSQL conexion = new ManejadoraSQL();
		conexion.conexionOpen();
		String contenido = leerContenidoArchivo(file);
		if (file.endsWith(".json")) {
			reservas = extraerValorJSON(contenido);
		} else {
			reservas = extraerValorXML(contenido);
		}
		String consulta = generarConsultaSQL(reservas);
		String[] consultasArray = consulta.split(";");
		for (String consultaIndividual : consultasArray) {
			if (!consultaIndividual.trim().isEmpty()) {
				System.out.println(consultaIndividual);
				conexion.insertarDatos(consultaIndividual);
			}
		}
		conexion.cerrarConexion();
	}

	private static void procesarOpcionExportarReservas() {
		System.out.println("En qué tipo desea guardar? 1: XML 2: JSON?");
		Scanner sc = new Scanner(System.in);
		int tipo = sc.nextInt();

		switch (tipo) {
		case 1:
			exportarReservas("xml");
			break;
		case 2:
			exportarReservas("json");
			break;
		default:
			System.out.println("Opción no válida");
			break;
		}
	}

	public static String leerContenidoArchivo(String filePath) {
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			StringBuilder contenido = new StringBuilder();
			String linea;
			while ((linea = reader.readLine()) != null) {
				contenido.append(linea).append("\n");
			}
			return contenido.toString();
		} catch (IOException e) {
			System.out.println("Error al leer el archivo: " + e.getMessage());
			return null;
		}
	}

	private static String generarConsultaSQL(ArrayList<ArrayList<String>> reservas) {
		String consultaSQL = "INSERT INTO reservas (nombre, telefono, fecha_evento, tipo_evento, n_personas, tipo_cocina, n_jornadas, n_habitaciones, tipo_mesa, n_comensales) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', %s, %s, %s, %s)";
		StringBuilder consultas = new StringBuilder();
		for (ArrayList<String> evento : reservas) {
			String tipoEvento = evento.get(3);
			if (tipoEvento.equals("Banquete")) {
				consultas.append(String.format(consultaSQL, evento.get(0), evento.get(1), evento.get(2), tipoEvento,
						evento.get(4), evento.get(5), "NULL", "NULL", "'" + evento.get(6) + "'",
						"'" + evento.get(7) + "'")).append(";\n");
			} else if (tipoEvento.equals("Jornada")) {
				consultas.append(String.format(consultaSQL, evento.get(0), evento.get(1), evento.get(2), tipoEvento,
						evento.get(4), evento.get(5), "NULL", "NULL", "NULL", "NULL")).append(";\n");
			} else if (tipoEvento.equals("Congreso")) {
				consultas.append(String.format(consultaSQL, evento.get(0), evento.get(1), evento.get(2), tipoEvento,
						evento.get(4), evento.get(5), "'" + evento.get(6) + "'", "'" + evento.get(7) + "'", "NULL",
						"NULL")).append(";\n");
			}
		}
		return consultas.toString();
	}

	public static void exportarReservas(String formato) {
		reservas.addAll(obtenerReservasDesdeBD());
		switch (formato.toLowerCase()) {
		case "xml":
			generarXML(reservas,"reservas.xml");
			System.out.println("Reservas exportadas a XML correctamente.");
			break;
		case "json":
			generarJSON(reservas, "reservas.json");
			System.out.println("Reservas exportadas a JSON correctamente.");
			break;
		default:
			System.out.println("Formato no válido. Use 'xml' o 'json'.");
			break;
		}
	}

	private static ArrayList<ArrayList<String>> obtenerReservasDesdeBD() {
		ArrayList<ArrayList<String>> reservas = new ArrayList<>();
		ManejadoraSQL manejadoraSQL = new ManejadoraSQL();
		try (ResultSet resultSet = manejadoraSQL.ejecutarConsulta("SELECT * FROM reservas" )) {
			while (resultSet.next()) {
				ArrayList<String> reserva = new ArrayList<>();
				reserva.add(resultSet.getNString("nombre"));
				reserva.add(resultSet.getNString("telefono"));
				reserva.add(resultSet.getNString("fecha_evento"));
				reserva.add(resultSet.getNString("tipo_evento"));
				reserva.add(resultSet.getNString("n_personas"));
				reserva.add(resultSet.getNString("tipo_cocina"));
				String tipoEvento = resultSet.getNString("tipo_evento");
				if ("Banquete".equals(tipoEvento)) {
					reserva.add(resultSet.getString("tipo_mesa"));
					reserva.add(resultSet.getString("n_comensales"));
				} else if ("Congreso".equals(tipoEvento)) {
					reserva.add(resultSet.getString("n_jornadas"));
					reserva.add(resultSet.getString("n_habitaciones"));
				}
				reservas.add(reserva);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return reservas;
	}
	
	private static void generarXML(ArrayList<ArrayList<String>> reservas, String nombreArchivo) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo))) {
			writer.write("<reservas>\n");
			for (ArrayList<String> reserva : reservas) {
				writer.write("  <reserva>\n");
				ArrayList<String> etiquetas = obtenerEtiquetas();
				for (int i = 0; i < reserva.size(); i++) {
					String etiqueta = etiquetas.get(i);
					String valor = reserva.get(i);
					writer.write(String.format("    <%s>%s</%s>\n", etiqueta, valor, etiqueta));
				}
				writer.write("  </reserva>\n");
			}
			writer.write("</reservas>\n");
			System.out.println("Archivo "+ nombreArchivo+"XML generado correctamente.");
		} catch (IOException e) {
			System.out.println("Error al generar el archivo XML: " + e.getMessage());
		}
	}

	private static void generarJSON(ArrayList<ArrayList<String>> reservas, String nombreArchivo) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo))) {
			writer.write("{\n  \"reservas\": {\n    \"reserva\": [\n");
			for (ArrayList<String> reserva : reservas) {
				writer.write(" {\n");
				ArrayList<String> etiquetas = obtenerEtiquetas();
				for (int i = 0; i < reserva.size(); i++) {
					String etiqueta = etiquetas.get(i);
					String valor = reserva.get(i);
					writer.write(String.format("   \"%s\": \"%s\"", etiqueta, valor));
					if (i < reserva.size() - 1) {
						writer.write(",");
					}
					writer.write("\n");
				}
				writer.write(" }");
				if (reservas.indexOf(reserva) < reservas.size() - 1) {
					writer.write(",");
				}
				writer.write("\n");
			}
			writer.write(" ]\n  }\n}\n");
			System.out.println("Archivo " + nombreArchivo + " JSON generado correctamente.");
		} catch (IOException e) {
			System.out.println("Error al generar el archivo JSON: " + e.getMessage());
		}
	}

	private static ArrayList<String> obtenerEtiquetas() {
		ArrayList<String> etiquetas = new ArrayList<>();
		etiquetas.add("nombre");
		etiquetas.add("telefono");
		etiquetas.add("fecha_evento");
		etiquetas.add("tipo_evento");
		etiquetas.add("asistentes");
		etiquetas.add("tipo_cocina");
		etiquetas.add("tipo_mesa");
		etiquetas.add("n_comensales");
		etiquetas.add("numeroJornadas");
		etiquetas.add("numeroHabitaciones");

		return etiquetas;
	}

	private static ArrayList<ArrayList<String>> extraerValorXML(String xml) {
		ArrayList<ArrayList<String>> resultados = new ArrayList<>();
		String[] registros = xml.split("<reserva>|</reserva>");

		for (String registro : registros) {
			if (!registro.trim().isEmpty()) {
				String tipoEvento = obtenerValorCampo(registro, "tipo_evento");
				String[] campos = obtenerCamposPorTipoEvento(tipoEvento);
				if (campos != null) {
					ArrayList<String> valores = obtenerValoresPorCampos(registro, campos);
					if (!valores.isEmpty()) {
						resultados.add(valores);
					}
				}
			}
		}
		return resultados;
	}

	private static String[] obtenerCamposPorTipoEvento(String tipoEvento) {
		String[] valoresJornada = { "nombre", "telefono", "fecha_evento", "tipo_evento", "asistentes", "tipo_cocina" };
		String[] valoresBanquete = { "nombre", "telefono", "fecha_evento", "tipo_evento", "asistentes", "tipo_cocina",
				"tipo_mesa", "n_comensales" };
		String[] valoresCongreso = { "nombre", "telefono", "fecha_evento", "tipo_evento", "asistentes", "tipo_cocina",
				"numeroJornadas", "numeroHabitaciones" };
		if ("Jornada".equals(tipoEvento)) {
			return valoresJornada;
		} else if ("Banquete".equals(tipoEvento)) {
			return valoresBanquete;
		} else if ("Congreso".equals(tipoEvento)) {
			return valoresCongreso;
		} else {
			return null;
		}
	}

	private static ArrayList<String> obtenerValoresPorCampos(String registro, String[] campos) {
		ArrayList<String> valores = new ArrayList<>();
		for (String campo : campos) {
			String valorCampo = obtenerValorCampo(registro, campo);
			if (valorCampo != null && !valorCampo.isEmpty()) {
				valores.add(valorCampo);
			}
		}
		return valores;
	}

	private static String obtenerValorCampo(String registro, String campoBuscado) {
		String inicioEtiqueta = "<" + campoBuscado + ">";
		String finEtiqueta = "</" + campoBuscado + ">";
		int inicio = registro.indexOf(inicioEtiqueta);
		int fin = registro.indexOf(finEtiqueta);
		if (inicio != -1 && fin != -1) {
			inicio += inicioEtiqueta.length();
			return registro.substring(inicio, fin).trim();
		}
		return null;
	}

	public static ArrayList<ArrayList<String>> extraerValorJSON(String json) {
		ArrayList<ArrayList<String>> resultados = new ArrayList<>();
		int inicio = json.indexOf("{");
		int fin = json.indexOf("}", inicio);
		while (inicio != -1 && fin != -1) {
			String registro = json.substring(inicio + 1, fin);
			if (!registro.trim().isEmpty()) {
				String tipoEvento = obtenerValorCampoJSON(registro, "tipo_evento");
				String[] campos = obtenerCamposPorTipoEvento(tipoEvento);
				if (campos != null) {
					ArrayList<String> valores = obtenerValoresPorCamposJSON(registro, campos);
					if (!valores.isEmpty()) {
						resultados.add(valores);
					}
				}
			}
			inicio = json.indexOf("{", fin);
			fin = json.indexOf("}", inicio);
		}
		return resultados;
	}

	private static String obtenerValorCampoJSON(String registro, String campoBuscado) {
		String patron = "\"" + campoBuscado + "\": \"";
		int inicio = registro.indexOf(patron);
		if (inicio != -1) {
			inicio += patron.length();
			int fin = registro.indexOf("\"", inicio);
			return registro.substring(inicio, fin).trim();
		}
		return null;
	}

	private static ArrayList<String> obtenerValoresPorCamposJSON(String registro, String[] campos) {
		ArrayList<String> valores = new ArrayList<>();
		for (String campo : campos) {
			String valorCampo = obtenerValorCampoJSON(registro, campo);
			if (valorCampo != null && !valorCampo.isEmpty()) {
				valores.add(valorCampo);
			}
		}
		return valores;
	}
}
