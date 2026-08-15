package es.um.redes.nanoFiles.udp.message;


public class DirMessage {

	public static final int PACKET_MAX_SIZE = 65507;
	private static final char DELIMITER = ':';
	private static final char END_LINE = '\n';

	private static final String FIELDNAME_OPERATION = "operation";
	private static final String FIELDNAME_PROTOCOL = "protocol";
	private static final String FIELDNAME_FILES = "files";
	private static final String FIELDNAME_PEERS = "peers";
	private static final String FIELDNAME_NICKNAME = "nickname";
	private static final String FIELDNAME_PORT = "port";
	private static final String FIELDNAME_HASH = "hash";
	private static final String FIELDNAME_FILENAME = "filename";
	private static final String FIELDNAME_FILESIZE = "filesize";
	private static final String FIELDNAME_FILEHASH = "filehash";
	private static final String FIELDNAME_FILEDATA = "filedata";

	private String operation = DirMessageOps.OPERATION_INVALID;
	private String protocolId;
	private String nickname;
	private Integer port;
	private String files;
	private String peers;
	private String hash;
	private String filename;
	private Long filesize;
	private String filehash;
	private String filedata;

	public DirMessage(String op) {
		this.operation = op;
	}

	public DirMessage(String op, String protID) {
		this.operation = op;
		this.protocolId = protID;
	}

	public DirMessage(String op, String protID, String nick, int puerto) {
		this.operation = op;
		this.protocolId = protID;
		this.nickname = nick;
		this.port = puerto;
	}

	public DirMessage(String op, String protID, String hash) {
		this.operation = op;
		this.protocolId = protID;
		this.hash = hash;
	}

	public DirMessage(String op, String protID, String filename, long filesize, String filehash, byte[] filedata) {
		this.operation = op;
		this.protocolId = protID;
		this.filename = filename;
		this.filesize = filesize;
		this.filehash = filehash;
		this.filedata = filedata != null ? java.util.Base64.getEncoder().encodeToString(filedata) : null;
	}

	public String getOperation() {
		return operation;
	}

	public String getProtocolId() {
		return protocolId;
	}

	public void setProtocolID(String protocolIdent) {
		this.protocolId = protocolIdent;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getPort() {
		return port == null ? 0 : port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getFiles() {
		return files;
	}

	public void setFiles(String files) {
		this.files = files;
	}

	public String getPeers() {
		return peers;
	}

	public void setPeers(String peers) {
		this.peers = peers;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getFilesize() {
		return filesize == null ? 0L : filesize;
	}

	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}

	public String getFilehash() {
		return filehash;
	}

	public void setFilehash(String filehash) {
		this.filehash = filehash;
	}

	public String getFiledata() {
		return filedata;
	}

	public void setFiledata(String filedata) {
		this.filedata = filedata;
	}

	public static DirMessage fromString(String message) {
	    String[] lines = message.split("\n");
	    DirMessage m = null;
	    StringBuilder filesContent = new StringBuilder();
	    StringBuilder peersContent = new StringBuilder();
	    boolean inFilesSection = false;
	    boolean inPeersSection = false;

	    for (String lineaActual : lines) {
	        if (lineaActual == null || lineaActual.trim().isEmpty()) continue;
	        
	        String line = lineaActual.trim();
	        int idx = line.indexOf(DELIMITER);
	        
	        
	        if (idx < 0) {
	            if (inFilesSection && m != null) {
	                if (filesContent.length() > 0) filesContent.append("\n");
	                filesContent.append(line);
	            } else if (inPeersSection && m != null) {
	                if (peersContent.length() > 0) peersContent.append("\n");
	                peersContent.append(line);
	            }
	            continue;
	        }
	        
	        String fieldName = line.substring(0, idx).trim().toLowerCase();
	        String value = line.substring(idx + 1).trim();

	        switch (fieldName) {
	            case FIELDNAME_OPERATION:
	                m = new DirMessage(value);
	                break;
	            case FIELDNAME_PROTOCOL:
	                if (m != null) m.setProtocolID(value);
	                break;
	            case FIELDNAME_NICKNAME:
	                inFilesSection = false; inPeersSection = false;
	                if (m != null) m.setNickname(value);
	                break;
	            case FIELDNAME_PORT:
	                inFilesSection = false; inPeersSection = false;
	                if (m != null && !value.isEmpty()) m.setPort(Integer.parseInt(value));
	                break;
	            case FIELDNAME_FILES:
	                inFilesSection = true;
	                inPeersSection = false;
	                if (m != null && !value.isEmpty()) filesContent.append(value);
	                break;
	            case FIELDNAME_PEERS:
	                inPeersSection = true;
	                inFilesSection = false;
	                if (m != null && !value.isEmpty()) peersContent.append(value);
	                break;
	            case FIELDNAME_HASH:
	                inFilesSection = false; inPeersSection = false;
	                if (m != null) m.setHash(value);
	                break;
	            case FIELDNAME_FILENAME:
	            	inFilesSection = false;
	            	inPeersSection = false;
	            	if (m != null) {
	            		m.setFilename(value);
	            	}
	            	break;
	            case FIELDNAME_FILESIZE:
	            	inFilesSection = false;
	            	inPeersSection = false;
	            	if (m != null && !value.isEmpty()) {
	            		m.setFilesize(Long.parseLong(value));
	            	}
	            	break;
	            case FIELDNAME_FILEHASH:
	            	inFilesSection = false;
	            	inPeersSection = false;
	            	if (m != null) {
	            		m.setFilehash(value);
	            	}
	            	break;
	            case FIELDNAME_FILEDATA:
	            	inFilesSection = false;
	            	inPeersSection = false;
	            	if (m != null) {
	            		m.setFiledata(value);
	            	}
	            	break;
	               
	            default:
	                // Si estamos en sección peers y la línea tiene ":" (ej: "zoe8:127.0.0.1:41875")
	                // no coincide con ningún fieldName conocido ---> es otro peer
	                if (inPeersSection && m != null) {
	                    if (peersContent.length() > 0) peersContent.append("\n");
	                    peersContent.append(line); // incluye los ":" de la IP:puerto
	                } else if (inFilesSection && m != null) {
	                    if (filesContent.length() > 0) filesContent.append("\n");
	                    filesContent.append(line);
	                }
	                break;
	        }
	    }
	    
	    if (m != null && filesContent.length() > 0) {
	        m.setFiles(filesContent.toString());
	    }
	    if (m != null && peersContent.length() > 0) {
	        m.setPeers(peersContent.toString());
	    }
	    
	    return m;
	}

	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(FIELDNAME_OPERATION).append(DELIMITER).append(operation).append(END_LINE);

		if (DirMessageOps.OPERATION_PING.equals(operation)) {
			if (protocolId != null) {
				sb.append(FIELDNAME_PROTOCOL).append(DELIMITER).append(protocolId).append(END_LINE);
			}
		} else if (DirMessageOps.OPERATION_GET_FILES.equals(operation)) {
			if (protocolId != null) {
				sb.append(FIELDNAME_PROTOCOL).append(DELIMITER).append(protocolId).append(END_LINE);
			}
			if (files != null) {
				sb.append(FIELDNAME_FILES).append(DELIMITER).append(files).append(END_LINE);
			}
		} else if (DirMessageOps.OPERATION_GET_PEERS.equals(operation)) {
			if (protocolId != null) {
				sb.append(FIELDNAME_PROTOCOL).append(DELIMITER).append(protocolId).append(END_LINE);
			}
			if (peers != null) {
				sb.append(FIELDNAME_PEERS).append(DELIMITER).append(peers).append(END_LINE);
			}
		} else if (DirMessageOps.OPERATION_REGISTER_SERVER.equals(operation)) {
			if (protocolId != null) {
				sb.append(FIELDNAME_PROTOCOL).append(DELIMITER).append(protocolId).append(END_LINE);
			}
			if (nickname != null) {
				sb.append(FIELDNAME_NICKNAME).append(DELIMITER).append(nickname).append(END_LINE);
			}
			if (port != null) {
				sb.append(FIELDNAME_PORT).append(DELIMITER).append(port).append(END_LINE);
			}
		} else if (DirMessageOps.OPERATION_UNREGISTER_SERVER.equals(operation)) {
			if (protocolId != null) {
				sb.append(FIELDNAME_PROTOCOL).append(DELIMITER).append(protocolId).append(END_LINE);
			}
			if (nickname != null) {
				sb.append(FIELDNAME_NICKNAME).append(DELIMITER).append(nickname).append(END_LINE);
			}
		} else if (DirMessageOps.OPERATION_DOWNLOAD_DIR.equals(operation)) {
			if (protocolId != null) {
				sb.append(FIELDNAME_PROTOCOL).append(DELIMITER).append(protocolId).append(END_LINE);
			}
			if (hash != null) {
				sb.append(FIELDNAME_HASH).append(DELIMITER).append(hash).append(END_LINE);
			}
			if (filename != null) {
				sb.append(FIELDNAME_FILENAME).append(DELIMITER).append(filename).append(END_LINE);
			}
			if (filesize != null) {
				sb.append(FIELDNAME_FILESIZE).append(DELIMITER).append(filesize).append(END_LINE);
			}
			if (filehash != null) {
				sb.append(FIELDNAME_FILEHASH).append(DELIMITER).append(filehash).append(END_LINE);
			}
			if (filedata != null) {
				sb.append(FIELDNAME_FILEDATA).append(DELIMITER).append(filedata).append(END_LINE);
			}
		} else if (DirMessageOps.OPERATION_GET_PEER_FILES.equals(operation)) {
			if (protocolId != null) {
				sb.append(FIELDNAME_PROTOCOL).append(DELIMITER).append(protocolId).append(END_LINE);
			}
			if (nickname != null) {
				sb.append(FIELDNAME_NICKNAME).append(DELIMITER).append(nickname).append(END_LINE);
			}
			if (peers != null) {
				sb.append(FIELDNAME_PEERS).append(DELIMITER).append(peers).append(END_LINE);
			}
		} else if (DirMessageOps.OPERATION_DOWNLOAD_PEER_FILE.equals(operation)) {
			if (protocolId != null) {
				sb.append(FIELDNAME_PROTOCOL).append(DELIMITER).append(protocolId).append(END_LINE);
			}
			if (nickname != null) {
				sb.append(FIELDNAME_NICKNAME).append(DELIMITER).append(nickname).append(END_LINE);
			}
			if (hash != null) {
				sb.append(FIELDNAME_HASH).append(DELIMITER).append(hash).append(END_LINE);
			}
		}

		sb.append(END_LINE);
		return sb.toString();
	}
}