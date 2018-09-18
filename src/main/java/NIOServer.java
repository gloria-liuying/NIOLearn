

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;

/**
 * <p>  </p>
 *
 * @author ly
 * @since 2018/9/12
 */
public class NIOServer implements Runnable {

    private Selector selector;
    //定义两个空间，初始化空间大小单位为字节
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

    public NIOServer(int port){
        init(port);
    }
    public static void main(String[] args){
        new Thread(new NIOServer(9999)).start();
    }

    private void init(int port){
        try{
            System.out.println("server starting at port"+port+"...");
            //开启多路复用器
            this.selector = Selector.open();
            //开启服务通道
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            //非阻塞
            serverChannel.configureBlocking(false);
            //绑定端口
            serverChannel.bind(new InetSocketAddress(port));
            //注册，并标记当前服务通道状态  int - 状态编码  op_accept 连接成功标记位
            //
            serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
            System.out.println("server started");
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        while(true){
            try{
                //阻塞方法，至少一个通道被选中，此方法返回
                //通道是否选择，由注册到多路复用器中通道标记决定
                this.selector.select();
                //返回选中的通道的集合，保存的是通道的标记，相当于通道id
                Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
                while (keys.hasNext()){
                    SelectionKey key = keys.next();
                    //每次删除，下次根据需要生成新的通道列表
                    keys.remove();
                    if(key.isValid()){
                        try{
                            if(key.isAcceptable()){
                                accept(key);
                            }
                        }catch (CancelledKeyException cke){
                            //断开连接
                            key.cancel();
                        }
                    }
                    //可读状态
                    try {
                        if (key.isReadable()) {
                            read(key);
                        }
                    }catch(CancelledKeyException cke){
                        key.cancel();
                    }
                    //可写状态
                    try {
                        if (key.isWritable()) {
                            write(key);
                        }
                    }  catch(CancelledKeyException cke){
                        key.cancel();
                    }
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    private void accept(SelectionKey key){
        try{
            //此通道为init方法中注册到Selector上的ServerSocketChannel
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
            //阻塞方法，当客户端发起请求后返回,此通道和客户端一一对应。
            SocketChannel channel = serverSocketChannel.accept();
            channel.configureBlocking(false);
            //设置对应客户端的通道标记状态，此通道为数据读取使用的
            channel.register(this.selector,SelectionKey.OP_READ);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key){
        try{
            //清空读取的缓存
            this.readBuffer.clear();
            //获取通道
            SocketChannel channel = (SocketChannel)key.channel();
            //将通道中的数据读取到缓存中，就是客户端发送给服务器的数据
            int readLength = channel.read(readBuffer);
            //如果没有写入数据，关闭通道
            if(readLength == -1){
                key.channel().close();
                key.cancel();
                return;
            }
            //NIO中最复杂的操作是Buffer的控制，Buffer中有一个游标，
            //游标信息在操作后不会归零，直接访问Buffer，数据可能不一致，用flip重置游标
            this.readBuffer.flip();
            byte[] datas = new byte[readBuffer.remaining()];
            readBuffer.get(datas);
            System.out.println("from"+channel.getRemoteAddress()+"client:"+new String());
            channel.register(this.selector,SelectionKey.OP_WRITE);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void write(SelectionKey key){
        this.writeBuffer.clear();
        SocketChannel channel = (SocketChannel) key.channel();
        Scanner reader = new Scanner(System.in);
        try {
            System.out.println("put message for send to client");
            String line = reader.nextLine();
            //将控制台写入的数据写入buffer，写入的是自己数组
            writeBuffer.put(line.getBytes("Utf-8"));
            writeBuffer.flip();
            channel.write(writeBuffer);
            channel.register(this.selector,SelectionKey.OP_READ);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

