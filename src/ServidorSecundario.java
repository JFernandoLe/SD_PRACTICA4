import java.io.*;
import java.net.*;

public class ServidorSecundario {
    private int puerto;

    public ServidorSecundario(int puerto) {
        this.puerto = puerto;
    }

    public void iniciar() {
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("Servidor Secundario escuchando en el puerto " + puerto);

            while (true) {
                Socket cliente = servidor.accept();
                new Thread(new ManejadorCliente(cliente)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ManejadorCliente implements Runnable {
        private Socket cliente;

        public ManejadorCliente(Socket cliente) {
            this.cliente = cliente;
        }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                 PrintWriter pw = new PrintWriter(cliente.getOutputStream(), true)) {

                String mensaje = br.readLine();
                System.out.println("Procesando mensaje: " + mensaje);
                pw.println("Servidor " + cliente.getLocalPort() + " recibió: " + mensaje);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int puerto = Integer.parseInt(args[0]);
        new ServidorSecundario(puerto).iniciar();
    }
}

