package program;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ManejadoraSQL {

	private Connection con;
	private Statement st;
	private ResultSet rs;

	public void conexionOpen() {
		String userName = "root";
		String password = "admin";
		String baseDeDatos = "reservas";
		String url = "jdbc:mysql://localhost/" + baseDeDatos;

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(url, userName, password);
			System.out.println("Conexion exitosa");
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
	}

	public ResultSet ejecutarConsulta(String consulta) {
		ResultSet resultSet = null;

		try {
			st = con.createStatement();
			resultSet = st.executeQuery(consulta);

		} catch (SQLException e) {
			System.out.println("ERROR al ejecutar consulta: " + e.getMessage());
		}
		return resultSet;
	}

	public String[] mostrarTabla(String consulta) {
		String[] datos = new String[10];
		try {
			st = con.createStatement();
			rs = st.executeQuery(consulta);
			while (rs.next()) {
				for (int i = 1; i <= 10; i++) {
					datos[i - 1] = rs.getString(i);
				}
				for (String dato : datos) {
					System.out.println(dato + " ");
				}
				System.out.println();
			}
			cerrarConexion();
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		return datos;
	}

	public void cerrarConexion() {
		try {
			con.close();
			System.out.println("Conexion ha sido cerrada");
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
	}

	public void insertarDatos(String consulta) {
		try {
			Statement st = null;
			st = con.createStatement();
			st.executeUpdate(consulta);
			System.out.println("Campo registrado ");
		} catch (Exception e) {
			System.out.println("Error:" + e.getMessage());
		}
	}

}
