package es.um.redes.nanoFiles.logic;

import java.nio.file.*;

import java.util.Map;

import java.net.InetSocketAddress;
import java.io.IOException;
import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.util.*;



import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicP2P {
	// Servidor TCP local para compartir ficheros con otros peers
	private NFServer fileServer = null;


	protected NFControllerLogicP2P() {
	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean startFileServer() {
	    if (fileServer != null) {
	        System.out.println("File server is already running");
	        return true;
	    }

	   try {
		   fileServer = new NFServer();
		   Thread serverThread = new Thread(fileServer);
		   serverThread.setDaemon(true); // se para cuando la aplicación termina
		   serverThread.start();
		   System.out.println("Servidor corriendo en el puerto %d%n" + fileServer.getPort());
		   return true;
	   } catch (IOException e) {
		   System.err.println("No se puede crear un NFServer" + e.getMessage());
		   fileServer = null;
		   return false;
	   }
	}
	

	protected void testTCPServer() {
		assert (NanoFiles.testModeTCP);
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		assert (fileServer == null);
		try {

			fileServer = new NFServer();
			/*
			 * (Boletín SocketsTCP) Inicialmente, se creará un NFServer y se ejecutará su
			 * método "test" (servidor minimalista en primer plano, que sólo puede atender a
			 * un cliente conectado). Posteriormente, se desactivará "testModeTCP" para
			 * implementar un servidor en segundo plano, que se ejecute en un hilo
			 * secundario para permitir que este hilo (principal) siga procesando comandos
			 * introducidos mediante el shell.
			 */
			fileServer.test();
			
			// Este código es inalcanzable: el método 'test' nunca retorna...
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("Cannot start the file server");
			fileServer = null;
		}
	}

	public void testTCPClient() {

		assert (NanoFiles.testModeTCP);
		/*
		 * (Boletín SocketsTCP) Inicialmente, se creará un NFConnector (cliente TCP)
		 * para conectarse a un servidor que esté escuchando en la misma máquina y un
		 * puerto fijo. Después, se ejecutará el método "test" para comprobar la
		 * comunicación mediante el socket TCP. Posteriormente, se desactivará
		 * "testModeTCP" para implementar la descarga de un fichero desde múltiples
		 * servidores.
		 */

		try {
			NFConnector nfConnector = new NFConnector(new InetSocketAddress(NFServer.PORT));
			nfConnector.test();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Método para listar los ficheros de un peer concreto vía TCP e imprimirlos por
	 * pantalla.
	 * 
	 * @param La dirección del peer cuyos ficheros se quiere listar
	 * @return Verdadero si se ha obtenido exitosamente el listado de fichero del
	 *         peer
	 */
	protected boolean listPeerFiles(InetSocketAddress peerAddr) {
		boolean success = false;
		try {
			NFConnector connector = new NFConnector(peerAddr);
			FileInfo[] files = connector.getFileList();
			connector.close();
			if (files != null) {
				System.out.println("Ficheros disponibles en: " + peerAddr);
				FileInfo.printToSysout(files);
				success = true;
			} 
		} catch (IOException e) {
			System.err.println("Error al conectar con el peer: " + e.getMessage());
		}

		return success;
	}

	/**
	 * Descarga un fichero identificado por subcadena de hash desde uno o varios
	 * peers. Si se pasa "*" como nickname, usa el directorio para localizar los
	 * peers que tienen el hash.
	 */
	protected boolean downloadFromPeers(NFControllerLogicDir dirLogic, String targetPeerNickname,
			String targetHashSubstring) {
		// TODO: localizar peers con el hash solicitado (o uno concreto) y delegar en
		// downloadFileFromServers
		//boolean success = false;
		Map<String, InetSocketAddress> peers = dirLogic.fetchPeerList();
		if (peers == null || peers.isEmpty()) {
			System.err.println("No hay peers registrados en el directorio.");
			return false;
		}
		InetSocketAddress peerAddr = peers.get(targetPeerNickname);
		if (peerAddr == null) {
			System.err.println("Peer " + targetPeerNickname + " no encontrado.");
			return false;
		}
		return downloadFileFromServers(new InetSocketAddress[] {peerAddr}, targetHashSubstring);
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList   La lista de direcciones de los servidores a los
	 *                            que se conectará
	 * @param targetHashSubstring Subcadena del hash del fichero a descargar
	 */
	protected boolean downloadFileFromServers(InetSocketAddress[] serverAddressList, String targetHashSubstring) {
		boolean downloaded = false;

		if (serverAddressList.length == 0) {
			System.err.println("* Cannot start download - No list of server addresses provided");
			return false;
		}
		// TODO: crear conectores TCP solo a los servidores que confirmen el hash
		// pedido, obtener nombre remoto, reservar nombre local sin colisiones, alternar
		// descarga de chunks y verificar hash final. Cerrar los sockets al terminar.

		try {
			NFConnector connector = new NFConnector(serverAddressList[0]);
			PeerMessage response = connector.downloadFile(targetHashSubstring);
			connector.close();
			
			if (response.getOpcode() == PeerMessageOps.OPCODE_ERROR) {
				System.err.println("Error Servidor: " + response.getMensajeError());
				downloaded = false;
			}
			if (response.getOpcode() == PeerMessageOps.OPCODE_FILE_DATA) {
				Path destino = FileNameUtil.chooseAvailableName(response.getFileName());
				Files.write(destino, response.getData());
				String checksum = FileDigest.computeFileChecksumString(destino.toString());
				System.out.println("Descargando: " + destino + " (" + response.getData().length + " bytes )");
				System.out.println("HASH: " + checksum);
				if (checksum.equals(response.getFileHash())) {
					System.out.println("Comprobación Hash: OK");
				} else {
					System.err.println("Comprobación Hash: FAILED");
				}
				downloaded = true;
			}
		} catch (IOException e) {
			System.err.println("Error al descargar del peer: " + e.getMessage());
		}


		return downloaded;
	}

	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	

	// NFControllerLogicP2P.java
	protected int getServerPort() {
	    return fileServer != null ? fileServer.getPort() : 0;  // Puerto REAL del servidor
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	protected void stopFileServer() {
		/*
		 * TODO: Enviar señal para detener nuestro servidor de ficheros en segundo plano
		 */
		if (fileServer != null) {
			fileServer.stopServer();
			fileServer = null;
		}


	}

	protected boolean serving() {
		return fileServer != null && fileServer.getPort() > 0;

	}

}
