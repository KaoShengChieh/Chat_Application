import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class FileReceiver {
	public static void main(String[] args) {
		FileReceiver nioServer = new FileReceiver();
		SocketChannel socketChannel = nioServer.createServerSocketChannel();
		nioServer.readFileFromSocket(socketChannel, args[0]);
	}

	public SocketChannel createServerSocketChannel() {
		ServerSocketChannel serverSocketChannel = null;
		SocketChannel socketChannel = null;
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(9999));
			socketChannel = serverSocketChannel.accept();
			System.out.println("Connection established...." + socketChannel.getRemoteAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return socketChannel;
	}

	public void readFileFromSocket(SocketChannel socketChannel, String fileName) {
		RandomAccessFile aFile = null;
		try {
			aFile = new RandomAccessFile("data/" + fileName, "rw");
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			FileChannel fileChannel = aFile.getChannel();
			while (socketChannel.read(buffer) > 0) {
				buffer.flip();
				fileChannel.write(buffer);
				buffer.clear();
			}
			Thread.sleep(1000);
			fileChannel.close();
			System.out.println("End of file reached..Closing channel");
			socketChannel.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
