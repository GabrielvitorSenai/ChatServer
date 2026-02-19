
import java.io.*;
import java.net.*;
import java.util.*;

public class AppChatServer {
    private static final int PORT = 12345;
    private static ServerSocket serverSocket;
    private static Set<String> clients = new HashSet<>();
    private static Map<String, PrintWriter> clientOutputs = new HashMap<>();

    public static void main(String[] args) throws Exception {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor aguardando conexões...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                // Criação de nova thread para cada cliente conectado
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String clientName;

        // Construtor
        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                // Receber o nome do cliente
                // out.println("Digite seu nome:");
                clientName = in.readLine();
                synchronized (clients) {
                    clients.add(clientName);
                    clientOutputs.put(clientName, out);
                }
                System.out.println(clientName + " entrou no chat.");

                // Enviar a lista de usuários para o novo cliente
                listUsers();

                // Enviar mensagens do cliente para o servidor
                String message;
                while ((message = in.readLine()) != null) {
                    // System.out.println("MSG:= "+ message);
                    if (message.startsWith("/send")) {
                        // Comando para enviar mensagem para outro cliente
                        String[] parts = message.split(" ", 3);
                        if (parts.length == 3) {
                            String target = parts[1];
                            String msg = parts[2];
                            sendMessageToClient(target, msg);
                        }
                    } else {
                        System.out.println(clientName + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Remover cliente da lista ao desconectar
                synchronized (clients) {
                    clients.remove(clientName);
                    clientOutputs.remove(clientName);
                }
                // Enviar a lista de usuários atualizada para todos os clientes
                listUsers();
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendMessageToClient(String target, String message) {
            PrintWriter targetOut = clientOutputs.get(target);
            if (targetOut != null) {
                targetOut.println(clientName + " diz: " + message);
            } else {
                out.println("Usuário " + target + " não encontrado.");
            }
        }

        private void listUsers() {
            synchronized (clients) {
                // Criar a lista de usuários conectados, separada por vírgula
                String userList = String.join(",", clients);
                String userListMessage = "/userlist " + userList;

                // Enviar a lista de usuários para todos os clientes
                for (PrintWriter clientOut : clientOutputs.values()) {
                    clientOut.println(userListMessage);
                }
            }

        }
    }
}