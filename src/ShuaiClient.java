import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Scanner;

public class ShuaiClient {

    public static int DEFAULT_PORT = 8888;

    private static volatile int db = 0;

    public static int getDb() {
        return db;
    }

    public static void setDb(int db) {
        ShuaiClient.db = db;
    }

    public static void main(String[] args) {
        try {
            SocketChannel client = SocketChannel.open(new InetSocketAddress(DEFAULT_PORT));
            ByteBuffer buffer = ByteBuffer.allocate(2000);
            WritableByteChannel out = Channels.newChannel(System.out);

            if(client.read(buffer) != -1) {
                buffer.rewind();
                out.write(buffer);
            }

            Scanner scanner = new Scanner(System.in);
            while(true) {
                buffer = ByteBuffer.allocate(2000);
                String input = scanner.nextLine().trim();
                if(input.equals("exit")) break;
                ShuaiRequest request;
                try{
                    request = new ShuaiRequest(input);
                    request.setDb(db);
                }catch (RuntimeException e){
                    continue;
                }
                buffer.put(request.toBytes());
                buffer.rewind();
                client.write(buffer);
                buffer.compact();
                ByteBuffer newRead = ByteBuffer.allocate(2000);
                if(client.read(newRead) != -1) {
                    newRead.flip();
                    ShuaiReply reply = (ShuaiReply) ShuaiReply.backToObject(newRead.array());
                    reply.speakOut();
//                    out.write(newRead);
//                    System.out.println("");
                    newRead.clear();
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



}
