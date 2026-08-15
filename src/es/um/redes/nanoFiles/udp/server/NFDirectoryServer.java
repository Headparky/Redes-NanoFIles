package es.um.redes.nanoFiles.udp.server;

import java.nio.file.*;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.LinkedHashMap;
import java.util.Map;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;


public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/*
	 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros alojados, servidores
	 * registrados, etc.
	 */
	/**
	 * Lista de ficheros alojados en el directorio.
	 */
	private FileInfo[] directoryFiles;
	/**
	 * Lista de servidores registrados (IP, puerto TCP).
	 */
	private LinkedHashMap<String, InetSocketAddress> registeredPeers;

	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability, String directoryFilesPath) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * Cargar los ficheros del directorio compartido.
		 */
		File dir = new File(directoryFilesPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		directoryFiles = FileInfo.loadFilesFromFolder(directoryFilesPath);
		System.out.println("* Directory loaded " + directoryFiles.length + " files from " + directoryFilesPath);
		/*
		 * TODO: (Boletín SocketsUDP) Inicializar el atributo socket: Crear un socket
		 * UDP ligado al puerto especificado por el argumento directoryPort en la
		 * máquina local,
		 */
		socket = new DatagramSocket(DIRECTORY_PORT);
		/*
		 * TODO: (Boletín SocketsUDP) Inicializar atributos que mantienen el estado del
		 * servidor de directorio: peers registrados, etc.)
		 */
		registeredPeers = new LinkedHashMap<>();


		if (NanoFiles.testModeUDP) {
			if (socket == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public DatagramPacket receiveDatagram() throws IOException {
		DatagramPacket datagramReceivedFromClient = null;
		boolean datagramReceived = false;
		while (!datagramReceived) {
			/*
			 * TODO: (Boletín SocketsUDP) Crear un búfer para recibir datagramas y un
			 * datagrama asociado al búfer (datagramReceivedFromClient)
			 */
			byte[] bufferRecepcion = new byte[DirMessage.PACKET_MAX_SIZE];
			datagramReceivedFromClient = new DatagramPacket(bufferRecepcion, bufferRecepcion.length);
			/*
			 * TODO: (Boletín SocketsUDP) Recibimos a través del socket un datagrama
			 */
			socket.receive(datagramReceivedFromClient);

			
			if (datagramReceivedFromClient == null) {
				System.err.println("[testMode] NFDirectoryServer.receiveDatagram: code not yet fully functional.\n"
						+ "Check that all TODOs have been correctly addressed!");
				System.exit(-1);
			} else {
				// Vemos si el mensaje debe ser ignorado (simulación de un canal no confiable)
				double rand = Math.random();
				if (rand < messageDiscardProbability) {
					System.err.println(
							"Directory ignored datagram from " + datagramReceivedFromClient.getSocketAddress());
				} else {
					datagramReceived = true;
					System.out
							.println("Directory received datagram from " + datagramReceivedFromClient.getSocketAddress()
									+ " of size " + datagramReceivedFromClient.getLength() + " bytes.");
				}
			}

		}

		return datagramReceivedFromClient;
	}

	public void runTest() throws IOException {

		System.out.println("[testMode] Directory starting...");

		System.out.println("[testMode] Attempting to receive 'ping' message...");
		DatagramPacket rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);

		System.out.println("[testMode] Attempting to receive 'ping&PROTOCOL_ID' message...");
		rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);
	}

	private void sendResponseTestMode(DatagramPacket pkt) throws IOException {
		/*
		 * TODO: (Boletín SocketsUDP) Construir un String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración.
		 */
		String messageFromClient = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println("Data received: " + messageFromClient);
		/*
		 * TODO: (Boletín SocketsUDP) Después, usar la cadena para comprobar que su
		 * valor es "ping"; en ese caso, enviar como respuesta un datagrama con la
		 * cadena "pingok". Si el mensaje recibido no es "ping", se informa del error y
		 * se envía "invalid" como respuesta.
		 */
		/*
		 * TODO: (Boletín Estructura-NanoFiles) Ampliar el código para que, en el caso
		 * de que la cadena recibida no sea exactamente "ping", comprobar si comienza
		 * por "ping&" (es del tipo "ping&PROTOCOL_ID", donde PROTOCOL_ID será el
		 * identificador del protocolo diseñado por el grupo de prácticas (ver
		 * NanoFiles.PROTOCOL_ID). Se debe extraer el "protocol_id" de la cadena
		 * recibida y comprobar que su valor coincide con el de NanoFiles.PROTOCOL_ID,
		 * en cuyo caso se responderá con "welcome" (en otro caso, "denied").
		 */

		String response;
		if (messageFromClient.equals("ping")) {
			response = "pingok";
		} else if (messageFromClient.startsWith("ping&")) {
			String protocolID = messageFromClient.substring(messageFromClient.indexOf('&') + 1);
			if (protocolID.equals(NanoFiles.PROTOCOL_ID)) {
				response = "welcome";
			} else {
				response = "denied";
			}
		} else {
			System.err.println("Mensaje inválido: " + messageFromClient);
			response = "invalid";
		}
		byte[] dataToClient = response.getBytes();
		InetSocketAddress clientAddress = (InetSocketAddress) pkt.getSocketAddress();
		DatagramPacket packetToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddress);
		System.out.println("Enviando respuesta: " + response);
		socket.send(packetToClient);
		
	}

	public void run() throws IOException {

		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio
			DatagramPacket rcvDatagram = receiveDatagram();

			sendResponse(rcvDatagram);

		}
	}

	private void sendResponse(DatagramPacket pkt) throws IOException {
		/*
		 * TODO: (Boletín MensajesASCII) Construir String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración. Después, usar la cadena para construir un objeto
		 * DirMessage que contenga en sus atributos los valores del mensaje. A partir de
		 * este objeto, se podrá obtener los valores de los campos del mensaje mediante
		 * métodos "getter" para procesar el mensaje y consultar/modificar el estado del
		 * servidor.
		 */
		String messageFromClient = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println("Data received (boletín ASCII): " + messageFromClient);
		DirMessage message = DirMessage.fromString(messageFromClient);
		String operation = message.getOperation();
		String protocolID = message.getProtocolId();
		/*
		 * TODO: Una vez construido un objeto DirMessage con el contenido del datagrama
		 * recibido, obtener el tipo de operación solicitada por el mensaje y actuar en
		 * consecuencia, enviando uno u otro tipo de mensaje en respuesta.
		 */
		// String operation = DirMessageOps.OPERATION_INVALID; // TODO: Cambiar!
		
		/*
		 * TODO: (Boletín MensajesASCII) Construir un objeto DirMessage (msgToSend) con
		 * la respuesta a enviar al cliente, en función del tipo de mensaje recibido,
		 * leyendo/modificando según sea necesario el "estado" guardado en el servidor
		 * de directorio (atributos files, etc.). Los atributos del objeto DirMessage
		 * contendrán los valores adecuados para los diferentes campos del mensaje a
		 * enviar como respuesta (operation, etc.)
		 */
		DirMessage responseMessage = null;
		/*
		 * TODO: (Boletín MensajesASCII) Construimos un mensaje de respuesta que indique
		 * el éxito/fracaso del ping (compatible, incompatible), y lo devolvemos como
		 * resultado del método.
		 */
		/*
		 * TODO: (Boletín MensajesASCII) Imprimimos por pantalla el resultado de
		 * procesar la petición recibida (éxito o fracaso) con los datos relevantes, a
		 * modo de depuración en el servidor
		 */
		switch (operation) {
			case DirMessageOps.OPERATION_PING: {
			
			/*
			 * TODO: (Boletín MensajesASCII) Comprobamos si el protocolId del mensaje del
			 * cliente coincide con el nuestro.
			 */
				if (protocolID.equals(NanoFiles.PROTOCOL_ID)) {
					System.out.println("Ping compatible cuyo protocolo tiene un ID: " + protocolID);
					responseMessage = new DirMessage(DirMessageOps.OPERATION_PING, NanoFiles.PROTOCOL_ID);
				} else {
					System.out.println("Ping incompatible. CLIENT Protocol: " + protocolID + " - " + "CLIENT Protocol EXPECTED: " + NanoFiles.PROTOCOL_ID);
					responseMessage = new DirMessage(DirMessageOps.OPERATION_PING, DirMessageOps.PROTOCOL_INCOMPATIBLE);
				}
			
				break;
			}
			
			case DirMessageOps.OPERATION_GET_FILES: {
			    System.out.println("Procesando dirfiles...");
			    StringBuilder sb = new StringBuilder();
			    for (int i = 0; i < directoryFiles.length; i++) {
			        sb.append(directoryFiles[i].toString());
			        if (i < directoryFiles.length - 1) {
			            sb.append("\n");
			        }
			    }
			    responseMessage = new DirMessage(DirMessageOps.OPERATION_GET_FILES, NanoFiles.PROTOCOL_ID);
			    responseMessage.setFiles(sb.toString());

			    break;
			}
			
			case DirMessageOps.OPERATION_GET_PEERS: {
				System.out.println("Procesando peers...");
				StringBuilder sb = new StringBuilder();
				for (Map.Entry<String, InetSocketAddress> entry : registeredPeers.entrySet()) {
					String nick = entry.getKey();
					InetSocketAddress address = entry.getValue();
					sb.append(nick).append(":").append(address.getAddress().getHostAddress()).append(":").append(address.getPort()).append("\n");
				}
				responseMessage = new DirMessage(DirMessageOps.OPERATION_GET_PEERS, NanoFiles.PROTOCOL_ID);
				responseMessage.setPeers(sb.toString());
				break;
			}
			
			case DirMessageOps.OPERATION_REGISTER_SERVER: {
				System.out.println("Procesando server...");
				String nick = message.getNickname();
				int port = message.getPort();
				InetSocketAddress clientAddress = (InetSocketAddress) pkt.getSocketAddress();
				InetSocketAddress peerAddress = new InetSocketAddress(clientAddress.getAddress(), port);
				registeredPeers.put(nick, peerAddress);
				System.out.println("Registrado peer: " + nick + "@" + peerAddress.getAddress().getHostAddress() + ":" + peerAddress.getPort());
				responseMessage = new DirMessage(DirMessageOps.OPERATION_REGISTER_SERVER, NanoFiles.PROTOCOL_ID, nick, port);
				break;
			}
			
			case DirMessageOps.OPERATION_UNREGISTER_SERVER: {
			    System.out.println("Procesando quit...");
			    String nick = message.getNickname();
			    registeredPeers.remove(nick);
			    System.out.println("Peer dado de baja: " + nick);
			    DirMessage resp = new DirMessage(DirMessageOps.OPERATION_UNREGISTER_SERVER, NanoFiles.PROTOCOL_ID);
			    resp.setNickname(nick);
			    responseMessage = resp;
			    break;
			}
			
			case DirMessageOps.OPERATION_GET_PEER_FILES: {
				System.out.println("Procesando peerfiles...");
				String nick = message.getNickname();
				InetSocketAddress addr = registeredPeers.get(nick);
				responseMessage = new DirMessage(DirMessageOps.OPERATION_GET_PEER_FILES, NanoFiles.PROTOCOL_ID);
				if (addr != null) {
					responseMessage.setPeers(nick + ":" + addr.getAddress().getHostAddress() + ":" + addr.getPort());
				}
				break;
			}
			
			case DirMessageOps.OPERATION_DOWNLOAD_PEER_FILE: {
				System.out.println("Procesando peerdl...");
				String nick = message.getNickname();
				InetSocketAddress addr = registeredPeers.get(nick);
				responseMessage = new DirMessage(DirMessageOps.OPERATION_DOWNLOAD_PEER_FILE, NanoFiles.PROTOCOL_ID);
				if (addr != null) {
					responseMessage.setPeers(nick + ":" + addr.getAddress().getHostAddress() + ":" + addr.getPort());
				}
				break;
			}
			
			case DirMessageOps.OPERATION_DOWNLOAD_DIR: {
			    System.out.println("Procesando dirdl...");
			    String hashSub = message.getHash();
			    FileInfo[] matches = FileInfo.lookupHashSubstring(directoryFiles, hashSub);
			    if (matches.length == 0) {
			        responseMessage = new DirMessage(DirMessageOps.OPERATION_DOWNLOAD_DIR, NanoFiles.PROTOCOL_ID);
			        System.err.println("Fichero no encontrado para la subcadena del hash: " + hashSub);
			    } else if (matches.length > 1) {
			        responseMessage = new DirMessage(DirMessageOps.OPERATION_DOWNLOAD_DIR, NanoFiles.PROTOCOL_ID);
			        System.err.println("Subcadena del hash ambigua (mas de 1 fichero coincidente): " + hashSub);
			    } else {
			        FileInfo fi = matches[0];
			        byte[] data = Files.readAllBytes(Paths.get(fi.filePath));
			        responseMessage = new DirMessage(DirMessageOps.OPERATION_DOWNLOAD_DIR,
			            NanoFiles.PROTOCOL_ID, fi.fileName, fi.fileSize, fi.fileHash, data);
			    }
			    break;
			}
			
			default:
				System.err.println("Unexpected message operation: \"" + operation + "\"");
				System.exit(-1);
		}

		/*
		 * TODO: (Boletín MensajesASCII) Convertir a String el objeto DirMessage
		 * (msgToSend) con el mensaje de respuesta a enviar, extraer los bytes en que se
		 * codifica el string y finalmente enviarlos en un datagrama
		 */
		String str = responseMessage.toString();
		byte[] dataStr = str.getBytes();
		InetSocketAddress address = (InetSocketAddress) pkt.getSocketAddress();
		DatagramPacket dataPkt = new DatagramPacket(dataStr, dataStr.length, address);
		System.out.println("Enviando respuesta : " + str);
		socket.send(dataPkt);
	}
}
