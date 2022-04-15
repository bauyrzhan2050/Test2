import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class Server {
    // ДЗ
    //1) Разобраться с кодом
    //2)Если клиент отправить /stat то сервер должен вернуть клиенту не эхо, а сообщение вида
    // 'Количество сообщений + n'
    //3*) При отправке с клиента смс вида /w user1 hello эта смс доожно отправится только клиенту с именем user1
    private int port;
    private List<ClientHandler> clients;
    public Server(int port) {
        this.port = port;
        clients = new ArrayList<>();
        try(ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Сервер запущен на порту 8189");
            while (true) {
                System.out.println("Ожидаем подключение клиентов...");
                Socket socket = serverSocket.accept(); // block
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(ClientHandler clientHandler) {

        clients.add(clientHandler);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler clientHandler) {

        clients.remove(clientHandler);
        broadcastClientsList();
    }
    public void sendPrivateMessage(ClientHandler sender, String receiver, String message){
        for(ClientHandler c: clients){
            if(c.getUsername().equals(receiver)){
                c.sendMessage("От: " + sender.getUsername()+ " Сообщение: " + message);
                sender.sendMessage("Пользователю: " + receiver + " Сообщение: " + message);
                return;

            }
        }
        sender.sendMessage("Невозможно отправить сообзение польхователю " + receiver + " Такого пользователя нет в сети" );
    }

    public void broadcastMessage(String message) {
        for(ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }
    }
    public void broadcastClientsList(){
        StringBuilder sb= new StringBuilder("/clients_list ");
        for(ClientHandler c: clients){
            sb.append(c.getUsername()).append(" ");
        }
        sb.setLength(sb.length()-1);
        String clientsList= sb.toString();
        for(ClientHandler c : clients){
            c.sendMessage(clientsList);
        }
    }

    public boolean isUserOnline(String username) {
        for(ClientHandler clientHandler : clients) {
            if(clientHandler.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

}
