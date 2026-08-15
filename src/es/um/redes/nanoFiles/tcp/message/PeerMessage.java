package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {




	private byte opcode;

	/*
	 * TODO: (Boletín MensajesBinarios) Añadir atributos u otros constructores
	 * específicos para crear mensajes con otros campos, según sea necesario
	 * 
	 */
	
	private String fileName; // Nombre del fichero: FILE_DATA - LIST_RESPONSE
	
	private long fileSize; // Tamaño del fichero: FILE_DATA - LIST_RESPONSE
	
	private String fileHash; // Hash: FILE_DATA - LIST_RESPONSE
	
	private String subcadenaFileHash; // Subcadena del hash: GET_FILE
	
	private byte[] data; // Contenido del fichero / chunk: FILE_DATA
	
	private int numFiles; // Número de ficheros: LIST_RESPONSE
	
	private FileInfo[] fileList;
	
	private String mensajeError; // Mensaje de error: ERROR



	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}


	/*
	 * TODO: (Boletín MensajesBinarios) Crear métodos getter y setter para obtener
	 * los valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	public byte getOpcode() {
		return opcode;
	}

	
	
	public String getFileName() {
		return fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public String getFileHash() {
		return fileHash;
	}

	public byte[] getData() {
		return data;
	}

	public int getNumFiles() {
		return numFiles;
	}

	public FileInfo[] getFileList() {
		return fileList;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public String getSubcadenaFileHash() {
		
		return subcadenaFileHash;
	}
	
	// LIST_REQUEST: solo opcode
	// preguntar porp qué es mejor que lleve el static
	public static PeerMessage listRequest() {
		return new PeerMessage(PeerMessageOps.OPCODE_FILELIST_REQUEST);
	}
	
	// preguntar por qué es mejor que lleve el static
	public PeerMessage getFile(String hash) {
		PeerMessage msg = new PeerMessage(PeerMessageOps.OPCODE_GET_FILE);
		msg.subcadenaFileHash = hash;
		return msg;
	}
	
	public PeerMessage fileData(String fileName, long size, String hash, byte[] data) {
		PeerMessage msg = new PeerMessage(PeerMessageOps.OPCODE_FILE_DATA);
		msg.fileName = fileName;
		msg.fileSize = size;
		msg.fileHash = hash;
		msg.data = data;
		return msg;
	}

	public PeerMessage listReponse(FileInfo[] files) {
		PeerMessage msg = new PeerMessage(PeerMessageOps.OPCODE_FILELIST_RESPONSE);
		msg.numFiles = files.length;
		msg.fileList = files;
		return msg;
	}

	public PeerMessage error(String error) {
		PeerMessage msg = new PeerMessage(PeerMessageOps.OPCODE_ERROR);
		msg.mensajeError = error;
		return msg;
	}
	
	
	
	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * TODO: (Boletín MensajesBinarios) En función del tipo de mensaje, leer del
		 * socket a través del "dis" el resto de campos para ir extrayendo con los
		 * valores y establecer los atributos del un objeto DirMessage que contendrá
		 * toda la información del mensaje, y que será devuelto como resultado. NOTA:
		 * Usar dis.readFully para leer un array de bytes, dis.readInt para leer un
		 * entero, etc.
		 */
		PeerMessage message = new PeerMessage();
		message.opcode = dis.readByte();
		switch (message.opcode) {
		case PeerMessageOps.OPCODE_FILELIST_REQUEST:
			// Solo opcode
			break;
		case PeerMessageOps.OPCODE_FILELIST_RESPONSE:
			message.numFiles = dis.readInt();
			message.fileList = new FileInfo[message.numFiles];
			for (int i=0; i<message.numFiles; i++) {
				String name = dis.readUTF();
				long size = dis.readLong();
				String hash = dis.readUTF();
				message.fileList[i] = new FileInfo(hash, name, size, null);
			}
			break;
		case PeerMessageOps.OPCODE_GET_FILE:
			message.subcadenaFileHash = dis.readUTF();
			break;
		case PeerMessageOps.OPCODE_FILE_DATA:
			message.fileName = dis.readUTF();
			message.fileSize = dis.readLong();
			message.fileHash = dis.readUTF();
			int len = dis.readInt();
			message.data = new byte[len];
			dis.readFully(message.data);
			break;
		case PeerMessageOps.OPCODE_ERROR:
			message.mensajeError = dis.readUTF();
			break;

		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(message.opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * TODO (Boletín MensajesBinarios): Escribir los bytes en los que se codifica el
		 * mensaje en el socket a través del "dos", teniendo en cuenta opcode del
		 * mensaje del que se trata y los campos relevantes en cada caso. NOTA: Usar
		 * dos.write para leer un array de bytes, dos.writeInt para escribir un entero,
		 * etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
			case PeerMessageOps.OPCODE_FILELIST_REQUEST:
				// Solo opcode (nada más)
				break;
			case PeerMessageOps.OPCODE_FILELIST_RESPONSE:
				dos.writeInt(numFiles);
				for (FileInfo fi : fileList) {
					dos.writeUTF(fi.fileName);
					dos.writeLong(fi.fileSize);
					dos.writeUTF(fi.fileHash);
				}
				break;
			case PeerMessageOps.OPCODE_GET_FILE:
				dos.writeUTF(subcadenaFileHash);
				break;
			case PeerMessageOps.OPCODE_FILE_DATA:
				dos.writeUTF(fileName);
				dos.writeLong(fileSize);
				dos.writeUTF(fileHash);
				dos.writeInt(data.length);
				dos.write(data);
				break;
			case PeerMessageOps.OPCODE_ERROR:
				dos.writeUTF(mensajeError);
				break;

		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}




}
