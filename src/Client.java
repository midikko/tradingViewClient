import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by midikko on 13.11.16.
 */
public class Client  extends Thread{

    private String address;
    private int portNumber;
    private String pathToFiles;
    private BufferedReader serverSocketReader;
    private PrintWriter serverSocketWriter;
    private boolean isOnline = true;
    private List<String> files;

    @Override
    public void run() {
        config();
        try (Socket socket = new Socket(address, portNumber);){
            socket.setSoTimeout(5000);
            serverSocketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverSocketWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            readIntro();
            while(isOnline){
                switch (getNextCommand()){
                    case 1 : {
                        readAndPrintFilesList();
                    } break;
                    case 2 : {
                        downloadFile(socket.getInputStream());
                    } break;
                    case 3 : {
                        isOnline=false;
                        sayBye();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.run();
    }

    private void sayBye() throws IOException {
        serverSocketWriter.println("3");
        serverSocketWriter.flush();
    }

    private void downloadFile(InputStream stream) throws IOException {
        serverSocketWriter.println("2");
        serverSocketWriter.flush();
        System.out.println("Укажите номер файла");
        int fileNum = Integer.parseInt(Main.consoleReader.readLine());
        serverSocketWriter.println(files.get(fileNum));
        serverSocketWriter.flush();
        saveFile(fileNum,stream);
    }

    private void saveFile(int fileIndex,InputStream stream) throws IOException {
        byte[] buffer = new byte[1024];
        try(FileOutputStream fos = new FileOutputStream(pathToFiles + File.separator + files.get(fileIndex));){
            int count;
            InputStream in = stream;
            try{
                while ((count = in.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                    fos.flush();
                }
            }catch (SocketTimeoutException e){
                System.out.println("Мы скачали файл.");
            }
        }
    }

    private void readIntro() throws IOException {
        try{
            String hello = serverSocketReader.readLine();
            if(hello.equalsIgnoreCase("-1")){
                System.out.println("Сервер выключается и в данный моент не доступен.");
            }else{
                System.out.println(hello);
            }
        }catch (SocketTimeoutException e){
            System.out.println("Сервер не отвечает");
        }

    }

    private int getNextCommand() throws IOException {
        System.out.println("Выберите команду");
        System.out.println("1. Получить список файлов");
        System.out.println("2. Скачать файл");
        System.out.println("3. Остановить клиент");
        return Integer.parseInt(Main.consoleReader.readLine());
    }

    private void config(){
        try{
            System.out.println("Введите адрес сервера (localhost по умолчанию): ");
            address = Main.consoleReader.readLine();

            System.out.println("Введите номер порта, на котором размещен сервер (4444 по умолчанию): ");
            String port = Main.consoleReader.readLine();
            portNumber = port.isEmpty() ? 4444 : Integer.parseInt(port);

            System.out.println("Введите путь к скачиваемым файлам (\"files/\" по умолчанию): ");
            pathToFiles = Main.consoleReader.readLine();
            pathToFiles = pathToFiles.isEmpty() ? "files/" : pathToFiles;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAndPrintFilesList() throws IOException {
        serverSocketWriter.println("1");
        serverSocketWriter.flush();
        try{
            String filesString = serverSocketReader.readLine();
            List<String> list = Arrays.asList(filesString.split(","));
            files=list;
            for(int i = 0 ; i<list.size();i++){
                System.out.println(i+ " " + list.get(i));
            }
        }catch (SocketTimeoutException e){
            System.out.println("Сервер не отвечает");
        }
    }
}
