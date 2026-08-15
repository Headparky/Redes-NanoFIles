package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;

import java.io.DataOutputStream;
import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;

	private DataInputStream dis;
	private DataOutputStream dos;



	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		serverAddr = fserverAddr;
		/*
		 * TODO: (Boletín SocketsTCP) Se crea el socket a partir de la dirección del
		 * servidor (IP, puerto). La creación exitosa del socket significa que la
		 * conexión TCP ha sido establecida.
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Se crean los DataInputStream/DataOutputStream a
		 * partir de los streams de entrada/salida del socket creado. Se usarán para
		 * enviar (dos) y recibir (dis) datos del servidor.
		 */
		socket = new Socket(fserverAddr.getAddress(), fserverAddr.getPort());
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());


	}

	public void test() {
		/*
		 * TODO: (Boletín SocketsTCP) Enviar entero cualquiera a través del socket y
		 * después recibir otro entero, comprobando que se trata del mismo valor.
		 */
		try {
			int enviado = 42;
			dos.write(enviado);
			dos.flush();
			int recibido = dis.readInt();
			if (recibido == enviado) {
				System.out.println("Test OK enviado=" + enviado + ", recibido=" + recibido);
			} else {
				System.err.println("Test FALLO: enviado=" + enviado + ", recibido" + recibido);
			}
		} catch (IOException e) {
			System.err.println("Error en test: " + e.getMessage());
		}
	}

	public FileInfo[] getFileList() throws IOException  {
		PeerMessage req = PeerMessage.listRequest();
		req.writeMessageToOutputStream(dos);;
		dos.flush();
		PeerMessage response = PeerMessage.readMessageFromInputStream(dis);
		if (response.getOpcode() == PeerMessageOps.OPCODE_FILELIST_RESPONSE) {
			return response.getFileList();
		}
		return null;
	}
	
	public PeerMessage downloadFile(String hashSubstring) throws IOException {
		PeerMessage req = new PeerMessage().getFile(hashSubstring);
		req.writeMessageToOutputStream(dos);
		dos.flush();
		return PeerMessage.readMessageFromInputStream(dis);
	}

	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			/**/
		}
	}
	
	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
