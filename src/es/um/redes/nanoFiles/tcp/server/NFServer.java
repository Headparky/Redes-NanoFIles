package es.um.redes.nanoFiles.tcp.server;

import java.nio.file.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;




public class NFServer implements Runnable {

	public static final int PORT = 10000;



	private ServerSocket serverSocket = null;

	public NFServer() throws IOException {
		/*
		 * TODO: (Boletín SocketsTCP) Crear una direción de socket a partir del puerto
		 * especificado (PORT)
		 */
		
		/*
		 * TODO: (Boletín SocketsTCP) Crear un socket servidor y ligarlo a la dirección
		 * de socket anterior
		 */
		
		serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress(0)); // Puerto libre

	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación.
	 * 
	 */
	public void test() {
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println(
					"[fileServerTestMode] Failed to run file server, server socket is null or not bound to any port");
			return;
		} else {
			System.out.println("[fileServerTestMode] NFServer running on " 
					+ serverSocket.getLocalSocketAddress() + ".");
		}

		while (true) {
			try {
				Socket socket = serverSocket.accept();
				System.out.println("\nNuevo cliente conectado: "
						+ socket.getInetAddress().toString() + ":" + socket.getPort());

				serveFilesToClient(socket);

				socket.close();

			} catch (IOException e) {
				System.err.println("Error aceptando conexión: " + e.getMessage());
			}
		}
	}

	/**
	 * Método que ejecuta el hilo principal del servidor en segundo plano, esperando
	 * conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		/*
		 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
		 * otros peers que soliciten descargar ficheros
		 */
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				System.out.println("Nuevo cliente conectado: " + socket.getInetAddress() + ":" + socket.getPort());
				NFServerThread thread = new NFServerThread(socket);
				thread.start();
			//	serveFilesToClient(socket);
			} catch (IOException e) {
				if (!serverSocket.isClosed()) {
					System.err.println("Error aceptando conexión: " + e.getMessage());
				}
			}
		}
		/*
		 * TODO: (Boletín SocketsTCP) Al establecerse la conexión con un peer, la
		 * comunicación con dicho cliente se hace en el método
		 * serveFilesToClient(socket), al cual hay que pasarle el socket devuelto por
		 * accept
		 */
		/*
		 * TODO: (Boletín TCPConcurrente) Crear un hilo nuevo de la clase
		 * NFServerThread, que llevará a cabo la comunicación con el cliente que se
		 * acaba de conectar, mientras este hilo vuelve a quedar a la escucha de
		 * conexiones de nuevos clientes (para soportar múltiples clientes). Si este
		 * hilo es el que se encarga de atender al cliente conectado, no podremos tener
		 * más de un cliente conectado a este servidor.
		 */




	}
	/*
	 * TODO: (Boletín SocketsTCP) Añadir métodos a esta clase para: 1) Arrancar el
	 * servidor en un hilo nuevo que se ejecutará en segundo plano 2) Detener el
	 * servidor (stopserver) 3) Obtener el puerto de escucha del servidor etc.
	 */

	public int getServerPort() {
		if (serverSocket != null && serverSocket.isBound() && !serverSocket.isClosed()) {
			return serverSocket.getLocalPort();
		}
		return 0;
	}

	public void stopServer() {
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
		} catch (IOException e) {
			System.err.println("Error deteniendo el servidor: " + e.getMessage());
		}
	}



	/**
	 * Método de clase que implementa el extremo del servidor del protocolo de
	 * transferencia de ficheros entre pares.
	 * 
	 * @param socket El socket para la comunicación con un cliente que desea
	 *               descargar ficheros.
	 */
	public static void serveFilesToClient(Socket socket) {
		/*
		 * TODO: (Boletín SocketsTCP) Crear dis/dos a partir del socket
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Mientras el cliente esté conectado, leer mensajes
		 * de socket, convertirlo a un objeto PeerMessage y luego actuar en función del
		 * tipo de mensaje recibido, enviando los correspondientes mensajes de
		 * respuesta.
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Para servir un fichero, hay que localizarlo a
		 * partir de su hash (o subcadena) en nuestra base de datos de ficheros
		 * compartidos. Los ficheros compartidos se pueden obtener con
		 * NanoFiles.db.getFiles(). Los métodos lookupHashSubstring y
		 * lookupFilenameSubstring de la clase FileInfo son útiles para buscar ficheros
		 * coincidentes con una subcadena dada del hash o del nombre del fichero. El
		 * método lookupFilePath() de FileDatabase devuelve la ruta al fichero a partir
		 * de su hash completo.
		 */
		/*
		try {
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

			int recibido = dis.readInt();
			System.out.println("[NFServer] Entero recibido del cliente: " + recibido);

			dos.writeInt(recibido);
			dos.flush();

			System.out.println("[NFServer] Entero reenviado al cliente: " + recibido);

		} catch (IOException e) {
			System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
		}
		*/
		try {
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			
			boolean conectado = true;
			while (conectado) {
				PeerMessage request = PeerMessage.readMessageFromInputStream(dis);
				switch (request.getOpcode()) {
					case PeerMessageOps.OPCODE_FILELIST_REQUEST: {
						FileInfo[] files = NanoFiles.db.getFiles();
						PeerMessage response = new PeerMessage().listReponse(files);
						response.writeMessageToOutputStream(dos);
						dos.flush();
						System.out.println("Enviada lista de " + files.length + " ficheros");
						break;
					}
					case PeerMessageOps.OPCODE_GET_FILE: {
						String hashSub = request.getSubcadenaFileHash();
						FileInfo[] matches = FileInfo.lookupHashSubstring(NanoFiles.db.getFiles(), hashSub);
						if (matches.length == 0) {
							PeerMessage err = new PeerMessage().error("Fichero no encontrado: " + hashSub);
							err.writeMessageToOutputStream(dos);
							dos.flush();
						} else if (matches.length > 1) {
							PeerMessage err = new PeerMessage().error("Hash ambiguo, varios ficheros coinciden: " + hashSub);
							err.writeMessageToOutputStream(dos);
							dos.flush();
						} else {
							FileInfo fi = matches[0];
							String path = NanoFiles.db.lookupFilePath(fi.fileHash);
							byte[] data = Files.readAllBytes(Paths.get(path));
							PeerMessage response = new PeerMessage().fileData(fi.fileName, fi.fileSize, fi.fileHash, data);
							response.writeMessageToOutputStream(dos);
							dos.flush();
							System.out.println("Enviado fichero: " + fi.fileName);
						}
						conectado = false;
						break;
					}
					default:
						System.err.println("Opcode inesperado: " + request.getOpcode());
						conectado = false;
				}
			}
		} catch (IOException e) {
			System.out.println("Cliente desconectado.");
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				
			}
		}

	}

	// NFServer.java
	public int getPort() {
	    return serverSocket != null && !serverSocket.isClosed() ? serverSocket.getLocalPort() : 0;
	}


}
