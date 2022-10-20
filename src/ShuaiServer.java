import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.*;

public class ShuaiServer {

    public static int DEFAULT_PORT = 8888;

    public static ExecutorService executor = Executors.newCachedThreadPool();

    public static ConcurrentLinkedDeque<ShuaiDB> dbs = new ConcurrentLinkedDeque<ShuaiDB>(){{
        add(new ShuaiDB());
        add(new ShuaiDB());
        add(new ShuaiDB());
    }};

    public static void main(String[] args) {
        System.out.println("Listening for connections on port 8888");
        //for initialize
        ShuaiCommand shuaiCommand;
        ShuaiServer shuaiServer;

        ServerSocketChannel serverSocketChannel;
        Selector selector;
        try{
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(DEFAULT_PORT));
            serverSocketChannel.configureBlocking(false);
            selector = Selector.open();
            serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
            return ;
        }

        while(true) {
            try{
                selector.selectNow();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while(iterator.hasNext()) {

                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    if(key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        System.out.println("Accepted connection from " + client);
                        client.configureBlocking(false);
                        SelectionKey key2 = client.register(selector,SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                        ByteBuffer input = ByteBuffer.allocate(2000);
                        input.put(ShuaiConstants.WELCOME);
                        input.rewind();
                        key2.attach(input);
//                        while(!client.finishConnect());
                        client.write(input);
                        input.compact();
                    }else if(key.isReadable()) {
                        executor.submit(new ShuaiTask(key));
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                } catch (IOException e) {
                    key.cancel();
                    try{
                        key.channel().close();
                    }catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }

        }

    }
}
