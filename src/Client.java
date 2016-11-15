import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

/**
 * Created by midikko on 13.11.16.
 */
public class Client  extends Thread{

    private String address;
    private int portNumber;
    private String pathToFiles;
    private BufferedReader reader;
    private BufferedWriter writer;
    private boolean isOnline = true;
    private List<String> files;

    @Override
    public void run() {
        config();
        try (Socket socket = new Socket(address, portNumber);){
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
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
        writer.write("3");
        writer.newLine();
        writer.flush();
    }

    private void downloadFile(InputStream stream) throws IOException {
        System.out.println("Укажите номер файла");
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        int fileNum = Integer.parseInt(bufferRead.readLine());
        writer.write("2");
        writer.newLine();
        writer.flush();
        writer.write(files.get(fileNum));
        writer.newLine();
        writer.flush();
        saveFile(fileNum,stream);
    }

    private void saveFile(int fileIndex,InputStream stream) throws IOException {
        FileOutputStream fos = new FileOutputStream(pathToFiles + File.separator + files.get(fileIndex));
        byte[] buffer = new byte[1024];
        int count = stream.read(buffer);
        fos.write(buffer, 0, count);
        fos.flush();
        while ((count = stream.read(buffer,0,stream.available()>buffer.length?buffer.length:stream.available())) > 0) {
            fos.write(buffer, 0, count);
            fos.flush();
        }
        fos.close();
        System.out.println("Мы скачали файл.");
    }

    private void readIntro() throws IOException {
        String hello = reader.readLine();
        System.out.println(hello);
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
        writer.write("1");
        writer.newLine();
        writer.flush();
        List<String> list = Arrays.asList(reader.readLine().split(","));
        files=list;
        for(int i = 0 ; i<list.size();i++){
            System.out.println(i+ " " + list.get(i));
        }
    }
}
