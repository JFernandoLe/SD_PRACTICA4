import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorPrincipal {
    private static final int PUERTO = 5000;
    private static final List<String> SERVIDORES = Arrays.asList("localhost:6000", "localhost:6001");

    public static void main(String[] args) {
        try (ServerSocket servidorPrincipal = new ServerSocket(PUERTO)) {
            System.out.println("Servidor Principal escuchando en el puerto " + PUERTO);

            int indiceServidor = 0;
            while (true) {
                Socket cliente = servidorPrincipal.accept();
                System.out.println("Cliente conectado desde: " + cliente.getInetAddress());

                // Seleccionar un servidor secundario de forma alternada
                String[] datosServidor = SERVIDORES.get(indiceServidor).split(":");
                String host = datosServidor[0];
                int puerto = Integer.parseInt(datosServidor[1]);

                // Redirigir al servidor secundario
                new Thread(new ClienteRedirigido(cliente, host, puerto)).start();

                // Alternar el servidor para balanceo
                indiceServidor = (indiceServidor + 1) % SERVIDORES.size();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClienteRedirigido implements Runnable {
    private Socket cliente;
    private String servidorHost;
    private int servidorPuerto;

    public ClienteRedirigido(Socket cliente, String servidorHost, int servidorPuerto) {
        this.cliente = cliente;
        this.servidorHost = servidorHost;
        this.servidorPuerto = servidorPuerto;
    }

    @Override
    public void run() {
        try (Socket servidorSecundario = new Socket(servidorHost, servidorPuerto);
             BufferedReader brCliente = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
             PrintWriter pwServidor = new PrintWriter(servidorSecundario.getOutputStream(), true);
             BufferedReader brServidor = new BufferedReader(new InputStreamReader(servidorSecundario.getInputStream()));
             PrintWriter pwCliente = new PrintWriter(cliente.getOutputStream(), true)) {

            String mensaje = brCliente.readLine();
            pwServidor.println(mensaje);
            String respuesta = brServidor.readLine();
            pwCliente.println(respuesta);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
