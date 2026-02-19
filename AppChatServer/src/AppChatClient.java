
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class AppChatClient extends JFrame {

    private JTextArea taChat;
    private JTextField tfMessage;
    private JTextField tfRecipient;
    private JButton btnSend;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String serverAddress = "1.1.1.1"; // Endereço do servidor
    private int port = 12345; // Porta do servidor
    private DefaultListModel<String> userListModel; // Modelo para o JList
    private JList<String> userList;

    public AppChatClient() {

        // Configurações da janela
        setTitle("Chat Client");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        taChat = new JTextArea();
        taChat.setEditable(false);
        add(new JScrollPane(taChat), BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Adicionando o label e o campo de texto para o destinatário
        JPanel recipientPanel = new JPanel();
        recipientPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel lblRecipient = new JLabel("Destinatário:");
        recipientPanel.add(lblRecipient);
        tfRecipient = new JTextField(15);
        recipientPanel.add(tfRecipient);
        panel.add(recipientPanel, BorderLayout.NORTH);

        // Adicionando o label e o campo de texto para a mensagem
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel lblMessage = new JLabel("Mensagem:");
        messagePanel.add(lblMessage);
        tfMessage = new JTextField(20);
        messagePanel.add(tfMessage);
        panel.add(messagePanel, BorderLayout.CENTER);

        // Botão de envio
        btnSend = new JButton("Enviar");
        panel.add(btnSend, BorderLayout.EAST);

        add(panel, BorderLayout.SOUTH);

        // Criar o JList para mostrar os usuários conectados
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane scrollPane = new JScrollPane(userList);
        add(scrollPane, BorderLayout.WEST);

        // Ação do botão de enviar
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Conectar ao servidor
        connectToServer();

        // Iniciar a thread de recebimento de mensagens
        new Thread(new MessageReceiver()).start();
    }

    private void connectToServer() {
        try {
            // Selecionar o Servidor
            String iphost = JOptionPane.showInputDialog("Digite o ip do servidor:");
            serverAddress = iphost;
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Enviar o nome do cliente
            String name = JOptionPane.showInputDialog("Digite seu nome:");
            setTitle("Chat Client - " + name);
            out.println(name);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendMessage() {
        String recipient = tfRecipient.getText();
        String message = tfMessage.getText();
        if (!message.isEmpty() && !recipient.isEmpty()) {
            out.println("/send " + recipient + " " + message); // Envia a mensagem para o servidor
            taChat.append("Você (para " + recipient + "): " + message + "\n");
            tfMessage.setText("");
        }
    }

    private class MessageReceiver implements Runnable {

        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/userlist")) {
                        // Atualiza o JList com a lista de usuários recebida
                        updateUserList(message);
                    } else {
                        taChat.append(message + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void updateUserList(String message) {

        // Extrai os usuários da mensagem e atualiza o JList
        String userList = message.replace("/userlist ", "");
        String[] users = userList.split(",");

        // Limpa a lista de usuários existente
        userListModel.clear();

        // Adiciona cada usuário em uma linha do JList
        for (String user : users) {
            userListModel.addElement(user);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AppChatClient().setVisible(true);
            }
        });
    }
}
