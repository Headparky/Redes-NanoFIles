package es.um.redes.nanoFiles.udp.client;

import java.util.*;
import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	/**
	 * Puerto en el que atienden los servidores de directorio
	 */
	private static final int DIRECTORY_PORT = 6868;
	/**
	 * Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
	 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
	 * recuperar el control
	 */
	private static final int TIMEOUT = 1000;
	/**
	 * Número de intentos máximos para obtener del directorio una respuesta a una
	 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
	 * cuenta como un intento.
	 */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;

	/**
	 * Socket UDP usado para la comunicación con el directorio
	 */
	private DatagramSocket socket;
	/**
	 * Dirección de socket del directorio (IP:puertoUDP)
	 */
	private InetSocketAddress directoryAddress;
	/**
	 * Nombre/IP del host donde se ejecuta el directorio
	 */
	private String directoryHostname;

	public static class DownloadedFile {
		public final String filename;
		public final long filesize;
		public final byte[] data;
		public final String filehash;

		public DownloadedFile(String filename, long fsize, byte[] data, String filehash) {
			this.filename = filename;
			this.filesize = fsize;
			this.data = data;
			this.filehash = filehash;
		}
	}

	public DirectoryConnector(String hostname) throws IOException {
		// Guardamos el string con el nombre/IP del host
		directoryHostname = hostname;
		/*
		 * TODO: (Boletín SocketsUDP) Convertir el string 'hostname' a InetAddress y
		 * guardar la dirección de socket (address:DIRECTORY_PORT) del directorio en el
		 * atributo directoryAddress, para poder enviar datagramas a dicho destino.
		 */
		InetAddress dirAddress = InetAddress.getByName(directoryHostname);
		directoryAddress = new InetSocketAddress(dirAddress, DIRECTORY_PORT);
		/*
		 * TODO: (Boletín SocketsUDP) Crea el socket UDP en cualquier puerto para enviar
		 * datagramas al directorio
		 */

		socket = new DatagramSocket();

	}

	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 */
	private byte[] sendAndReceiveDatagrams(byte[] requestData) {
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		byte response[] = null;
		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"directoryAddress\"");
			System.exit(-1);

		}
		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"socket\"");
			System.exit(-1);
		}
		/*
		 * TODO: (Boletín SocketsUDP) Enviar datos en un datagrama al directorio y
		 * recibir una respuesta. El array devuelto debe contener únicamente los datos
		 * recibidos, *NO* el búfer de recepción al completo.
		 */
		/*
		 * TODO: (Boletín SocketsUDP) Una vez el envío y recepción asumiendo un canal
		 * confiable (sin pérdidas) esté terminado y probado, debe implementarse un
		 * mecanismo de retransmisión usando temporizador, en caso de que no se reciba
		 * respuesta en el plazo de TIMEOUT. En caso de salte el timeout, se debe volver
		 * a enviar el datagrama y tratar de recibir respuestas, reintentando como
		 * máximo en MAX_NUMBER_OF_ATTEMPTS ocasiones.
		 */
		/*
		 * TODO: (Boletín SocketsUDP) Las excepciones que puedan lanzarse al
		 * leer/escribir en el socket deben ser capturadas y tratadas en este método. Si
		 * se produce una excepción de entrada/salida (error del que no es posible
		 * recuperarse), se debe informar y terminar el programa.
		 */
		/*
		 * NOTA: Las excepciones deben tratarse de la más concreta a la más genérica.
		 * SocketTimeoutException es más concreta que IOException.
		 */
		//DatagramPacket packet = new DatagramPacket(requestData, requestData.length, directoryAddress);
		int intento = 0;
		boolean success = false;
		while (intento < MAX_NUMBER_OF_ATTEMPTS && !success) {
			try {
				socket.setSoTimeout(TIMEOUT);
				DatagramPacket packet = new DatagramPacket(requestData, requestData.length, directoryAddress);
				socket.send(packet);
				System.out.println("Intento " + (intento + 1) + ": Datagrama enviado");
				DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);
				socket.receive(responsePacket);
				response = Arrays.copyOf(responseData, responsePacket.getLength());
				success = true;
				System.out.println("Respuesta recibida correctamente.");
			} catch (SocketTimeoutException e) {
				intento++;
				System.err.println("Timeout en intento: " + intento + ": No se recibió respuesta");
				if (intento >= MAX_NUMBER_OF_ATTEMPTS) {
					System.err.println("Se alcanzó el número máximo de intentos (" + MAX_NUMBER_OF_ATTEMPTS + ")");
					System.exit(-1);
				}
			} catch (IOException e) {
				System.err.println("Error de comunicación con el directorio: " + e.getMessage());
				e.printStackTrace();
				System.exit(-1);
			}
		}

		if (response != null && response.length == responseData.length) {
			System.err.println("Your response is as large as the datagram reception buffer!!\n"
					+ "You must extract from the buffer only the bytes that belong to the datagram!");
		}
		return response;
	}

	/**
	 * Método para probar la comunicación con el directorio mediante el envío y
	 * recepción de mensajes sin formatear ("en crudo")
	 * 
	 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
	 */
	public boolean testSendAndReceive() {
		/*
		 * TODO: (Boletín SocketsUDP) Probar el correcto funcionamiento de
		 * sendAndReceiveDatagrams. Se debe enviar un datagrama con la cadena "ping" y
		 * comprobar que la respuesta recibida empieza por "pingok". En tal caso,
		 * devuelve verdadero, falso si la respuesta no contiene los datos esperados.
		 */
		boolean success = false;
		String mensaje = "ping";
		byte[] dataToServer = mensaje.getBytes();
		byte[] response = sendAndReceiveDatagrams(dataToServer);
		if (response != null) {
			String respuesta = new String(response);
			if (respuesta.startsWith("pingok")) {
				success = true;
			}
		}

		return success;
	}

	public String getDirectoryHostname() {
		return directoryHostname;
	}

	/**
	 * Método para "hacer ping" al directorio, comprobar que está operativo y que
	 * usa un protocolo compatible. Este método no usa mensajes bien formados.
	 * 
	 * @return Verdadero si
	 */
	public boolean pingDirectoryRaw() {
		/*
		 * TODO: (Boletín EstructuraNanoFiles) Basándose en el código de
		 * "testSendAndReceive", contactar con el directorio, enviándole nuestro
		 * PROTOCOL_ID (ver clase NanoFiles). Se deben usar mensajes "en crudo" (sin un
		 * formato bien definido) para la comunicación.
		 * 
		 * PASOS: 1.Crear el mensaje a enviar (String "ping&protocolId"). 2.Crear un
		 * datagrama con los bytes en que se codifica la cadena : 4.Enviar datagrama y
		 * recibir una respuesta (sendAndReceiveDatagrams). : 5. Comprobar si la cadena
		 * recibida en el datagrama de respuesta es "welcome", imprimir si éxito o
		 * fracaso. 6.Devolver éxito/fracaso de la operación. boolean success = false;
		 * String mensaje = "ping"; byte[] dataToServer = mensaje.getBytes(); byte[]
		 * response = sendAndReceiveDatagrams(dataToServer); if (response != null) {
		 * String respuesta = new String(response); if (respuesta.startsWith("pingok"))
		 * { success = true; } }
		 * 
		 * return success;
		 */
		boolean success = false;
		String ping = "ping&" + NanoFiles.PROTOCOL_ID;
		byte[] dataToServer = ping.getBytes();
		byte[] responseData = sendAndReceiveDatagrams(dataToServer);
		if (responseData != null) {
			String response = new String(responseData);
			if (response.equals("welcome")) {
				success = true;
				System.out.println("Directorio alcanzable y funcionando.");
			} else {
				System.err.println("Respuesta inesperada por parte del directorio: " + response);
			}
		}

		return success;
	}

	public void close() {
		if (socket != null && !socket.isClosed()) {
			socket.close();
		}
	}
	
	/**
	 * Método para "hacer ping" al directorio, comprobar que está operativo y que es
	 * compatible.
	 * 
	 * @return Verdadero si el directorio está operativo y es compatible
	 */
	public boolean pingDirectory() {
		/*
		 * TODO: (Boletín MensajesASCII) Hacer ping al directorio 1.Crear el mensaje a
		 * enviar (objeto DirMessage) con atributos adecuados (operation, etc.) NOTA:
		 * Usar como operaciones las constantes definidas en la clase DirMessageOps :
		 * 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		 * 3.Crear un datagrama con los bytes en que se codifica la cadena : 4.Enviar
		 * datagrama y recibir una respuesta (sendAndReceiveDatagrams). : 5.Convertir
		 * respuesta recibida en un objeto DirMessage (método DirMessage.fromString)
		 * 6.Extraer datos del objeto DirMessage y procesarlos 7.Devolver éxito/fracaso
		 * de la operación
		 */

		/*
		 * boolean success = false; String ping = "ping&" + NanoFiles.PROTOCOL_ID;
		 * byte[] dataToServer = ping.getBytes(); byte[] responseData =
		 * sendAndReceiveDatagrams(dataToServer); if (responseData != null) { String
		 * response = new String(responseData); if (response.equals("welcome")) {
		 * success = true; System.out.println("Directorio alcanzable y funcionando."); }
		 * else { System.err.println("Respuesta inesperada por parte del directorio: " +
		 * response); } }
		 * 
		 * 
		 * return success;
		 */
		boolean success = false;
		DirMessage message = new DirMessage(DirMessageOps.OPERATION_PING, NanoFiles.PROTOCOL_ID);
		String messageStr = message.toString();
		byte[] dataToServer = messageStr.getBytes();
		byte[] responseData = sendAndReceiveDatagrams(dataToServer);
		if (responseData != null) {
			String response = new String(responseData);
			DirMessage responseMessage = DirMessage.fromString(response);
			if (DirMessageOps.OPERATION_PING.equals(responseMessage.getOperation())
					&& NanoFiles.PROTOCOL_ID.equals(responseMessage.getProtocolId())) {
				success = true;
				System.out.println("Directorio alcanzable y compatible ");
			} else {
				System.err.println("Respuesta de ping inválida: " + response);
			}
		}
		return success;
	}

	/**
	 * Método para dar de alta como servidor de ficheros en el puerto indicado.
	 * 
	 * @param serverPort El puerto TCP en el que este peer sirve ficheros a otros
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y acepta la lista de ficheros, falso en caso contrario.
	 */
	public boolean registerFileServer(int serverPort) {
		// TODO: Ver TODOs en pingDirectory y seguir esquema similar
		boolean success = false;
		DirMessage request = new DirMessage(
			    DirMessageOps.OPERATION_REGISTER_SERVER,
			    NanoFiles.PROTOCOL_ID,
			    NanoFiles.peerNickname,
			    serverPort
			);


		byte[] dataToServer = request.toString().getBytes();
		byte[] responseData = sendAndReceiveDatagrams(dataToServer);
		if (responseData != null) {
			String responseStr = new String(responseData);
			DirMessage responseMessage = DirMessage.fromString(responseStr);
			if (DirMessageOps.OPERATION_REGISTER_SERVER.equals(responseMessage.getOperation())
					&& NanoFiles.PROTOCOL_ID.equals(responseMessage.getProtocolId())) {
				success = true;
				System.out.println("Registrado como servidor: " + NanoFiles.peerNickname);
			} else {
				System.err.println("No se ha podido registrar como servidor");
			}
		}

		return success;
	}
	
	

	/**
	 * Método para obtener la lista de ficheros alojados en el directorio. Para cada
	 * fichero se debe obtener un objeto FileInfo con nombre, tamaño y hash.
	 * 
	 * @return Los ficheros disponibles en el directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 */
	public FileInfo[] getFileList() {
		FileInfo[] filelist = new FileInfo[0];
		DirMessage request = new DirMessage(DirMessageOps.OPERATION_GET_FILES, NanoFiles.PROTOCOL_ID);
		byte[] dataToServer = request.toString().getBytes();
		byte[] responseData = sendAndReceiveDatagrams(dataToServer);
		if (responseData != null) {
			String responseStr = new String(responseData);
			/*
			System.out.println("===DEBUG===");
			System.out.println("Bytes recibidos: " + responseData.length);
			System.out.println("responseStr.length(): " + responseStr.length());
			System.out.println("responseStr: [" + responseStr.replace("\n", "\\n") + "]");
			System.out.println("====================");
*/
			DirMessage responseMessage = DirMessage.fromString(responseStr);
			if (DirMessageOps.OPERATION_GET_FILES.equals(responseMessage.getOperation())
					&& NanoFiles.PROTOCOL_ID.equals(responseMessage.getProtocolId())) {
				String fileStr = responseMessage.getFiles();
				/*
				System.out.println("=== DEBUG FILESTR ===");
				System.out.println("fileStr.length(): " + fileStr.length());
				System.out.println("fileStr: [" + fileStr.replace("\n", "\\n") + "]");
				System.out.println("====================");
				*/
				if (fileStr != null && !fileStr.isEmpty()) {
					filelist = parseFileList(fileStr);
					System.out.println("Ficheros que se reciben del directorio (" + filelist.length + ")");
				}
			} else {
				System.err.println("Respuesta inválida de dirfiles");
			}
		}
		return filelist;
	}

	private FileInfo[] parseFileList(String fileStr) {
	    if (fileStr == null || fileStr.trim().isEmpty()) {
	        return new FileInfo[0];
	    }
	    
	    String[] lines = fileStr.split("\n");
	    List<FileInfo> files = new ArrayList<>();
	    
	    for (String line : lines) {
	        line = line.trim();
	        if (line.isEmpty()) continue;
	        
	        String[] parts = line.trim().split("\\s+");
	        if (parts.length >= 3 && parts[parts.length - 1].matches("[0-9a-f]{40}")) {
	            String hash = parts[parts.length - 1];
	            String name = parts[0];
	            try {
	                long size = Long.parseLong(parts[parts.length - 2]);
	                files.add(new FileInfo(hash, name, size, null));
	                System.out.printf("Parseado: %s (%d bytes, hash %s)\n", name, size, hash);
	            } catch (NumberFormatException e) {
	                System.err.println("Error parseando tamaño: " + line);
	            }
	        }
	    }
	    return files.toArray(new FileInfo[0]);
	}




	public Map<String, InetSocketAddress> getPeerList() {
		Map<String, InetSocketAddress> peers = new LinkedHashMap<String, InetSocketAddress>();
		DirMessage request = new DirMessage(DirMessageOps.OPERATION_GET_PEERS, NanoFiles.PROTOCOL_ID);
		byte[] dataToServer = request.toString().getBytes();
		byte[] responseData = sendAndReceiveDatagrams(dataToServer);
		if (responseData != null) {
			String responseStr = new String(responseData);
			DirMessage responseMessage = DirMessage.fromString(responseStr);
			if (DirMessageOps.OPERATION_GET_PEERS.equals(responseMessage.getOperation()) && NanoFiles.PROTOCOL_ID.equals(responseMessage.getProtocolId())) {
				String peersStr = responseMessage.getPeers();
				if (peersStr != null && !peersStr.trim().isEmpty()) {
					parsePeerList(peersStr, peers);
				}
			}
		}
		return peers;
	}
	
	private void parsePeerList(String peersStr, Map<String, InetSocketAddress> peers) {
		String[] lineas = peersStr.split("\n");
		for (String line : lineas) {
			if (line.trim().isEmpty()) continue;
			try {
				String[] partes = line.split(":");
				if (partes.length == 3) {
					String nick = partes[0];
					String ip = partes[1];
					int port = Integer.parseInt(partes[2]);
					InetAddress address = InetAddress.getByName(ip);
					peers.put(nick, new InetSocketAddress(address, port));
				}
			} catch (Exception e) {
				System.err.println("Error al parsear el peer.");
			}
		}
	}

	public Map<String, InetSocketAddress[]> searchFilesByHash(String hashSubstring) {
		Map<String, InetSocketAddress[]> results = new LinkedHashMap<String, InetSocketAddress[]>();

		return results;
	}

	public DownloadedFile downloadFileFromDirectory(String hashSubstring) {
		DirMessage request = new DirMessage(DirMessageOps.OPERATION_DOWNLOAD_DIR, NanoFiles.PROTOCOL_ID, hashSubstring);
		byte[] dataToServer = request.toString().getBytes();
		byte[] responseData = sendAndReceiveDatagrams(dataToServer);
		if (responseData == null) {
			return null;
		}
		DirMessage response = DirMessage.fromString(new String(responseData));
		if (!DirMessageOps.OPERATION_DOWNLOAD_DIR.equals(response.getOperation())) {
			return null;
		}
		
		if (response.getFiledata() == null) {
			System.err.println("* Directory error: " + response.getFilename());
			return null;
		}
		byte[] data = Base64.getDecoder().decode(response.getFiledata());
		return new DownloadedFile(response.getFilename(), response.getFilesize(), data, response.getFilehash());
	}

	/**
	 * Método para darse de baja como servidor de ficheros.
	 * 
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y ha dado de baja sus ficheros.
	 */
	public boolean unregisterFileServer() {
	    boolean success = false;
	    DirMessage request = new DirMessage(DirMessageOps.OPERATION_UNREGISTER_SERVER, NanoFiles.PROTOCOL_ID);
	    request.setNickname(NanoFiles.peerNickname);
	    
	    byte[] dataToServer = request.toString().getBytes();
	    byte[] responseData = sendAndReceiveDatagrams(dataToServer);
	    
	    if (responseData != null) {
	        String responseStr = new String(responseData);
	        DirMessage responseMessage = DirMessage.fromString(responseStr);
	        if (DirMessageOps.OPERATION_UNREGISTER_SERVER.equals(responseMessage.getOperation())
	            && NanoFiles.PROTOCOL_ID.equals(responseMessage.getProtocolId())) {
	            success = true;
	            System.out.println("Dado de baja como servidor: " + NanoFiles.peerNickname);
	        }
	    }
	    return success;
	}


}
