import java.nio.ByteBuffer;

/**
 * <p>  </p>
 *
 * @author ly
 * @since 2018/9/18
 */
public class TestBuffer {
    /**
     * Buffer的应用固定逻辑
     * 写操作顺序
     * 1.clear（）
     * 2.put() ->写操作
     * 3.get() ->重置游标
     * 4.SocketChannel.write(buffer); ->将缓存数据发送到网络的另一端
     * 5.clear（）
     *
     * 读操作顺序
     * 1.clear()
     * 2.SocketChannel.read(); ->从网络中读取数据
     * 3.buffer.flip()
     * 4.buffer.get()  ->读取数据
     * 5.buffer.clear()
     * @param args
     */
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        byte[] temp = new byte[]{1,2,3};
        //brfore:java.nio.HeapByteBuffer[pos=0 lim=16 cap=16]
        //pos：游标位置  ，lim：限制数量，cap：最大容zx量
        System.out.println("brfore:"+buffer);
        buffer.put(temp);
        System.out.println("after"+buffer);
        //重置游标
        buffer.flip();
        System.out.println("reset"+buffer);
        for(int i=0;i<buffer.remaining();i++){
            int data = buffer.get(i);
            System.out.println(i+"--"+data);
        }
    }

}
