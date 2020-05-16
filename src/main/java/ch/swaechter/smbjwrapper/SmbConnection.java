package ch.swaechter.smbjwrapper;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.share.DiskShare;

import java.io.IOException;

/**
 * This class is responsible for managing the SMB connection and session.
 *
 * @author Simon Wächter
 */
public class SmbConnection implements AutoCloseable {

    /**
     * New or reused connection.
     */
    private final Connection connection;

    /**
     * Disk share for the SMB access.
     */
    private final DiskShare diskShare;

    /**
     * Server name of the server.
     */
    private final String serverName;

    /**
     * Share name of the server
     */
    private final String shareName;

    /**
     * Create a new SMB connection to the server. Be aware, that the connection itself is not thread safe.
     *
     * @param serverName            Server name of the server
     * @param shareName             Share name of the server
     * @param authenticationContext Authentication used to authenticate against
     * @throws IOException Exception in case of a problem
     */
    public SmbConnection(String serverName, String shareName, AuthenticationContext authenticationContext) throws IOException {
        this(serverName, shareName, authenticationContext, SmbConfig.builder().build());
    }

    /**
     * Create a new SMB connection to the server with a custom SMB client configuration. Be aware, that the connection itself is not thread safe.
     *
     * @param serverName            Server name of the server
     * @param shareName             Share name of the server
     * @param authenticationContext Authentication used to authenticate against
     * @param smbConfig             Custom SMB configuration
     * @throws IOException Exception in case of a problem
     */
    public SmbConnection(String serverName, String shareName, AuthenticationContext authenticationContext, SmbConfig smbConfig) throws IOException {
        SMBClient smbClient = new SMBClient(smbConfig);
        this.connection = smbClient.connect(serverName);
        this.diskShare = (DiskShare) connection.authenticate(authenticationContext).connectShare(shareName);
        this.serverName = serverName;
        this.shareName = shareName;
    }

    /**
     * Get the disk share to access the server.
     *
     * @return Disk share to access the server
     */
    public DiskShare getDiskShare() {
        return diskShare;
    }

    /**
     * Get the server name of the server.
     *
     * @return Name of the server
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Get the share name of the server.
     *
     * @return Name of the share
     */
    public String getShareName() {
        return shareName;
    }

    /**
     * Close the connection.
     *
     * @throws IOException Exception in case of a problem
     */
    @Override
    public void close() throws IOException {
        connection.close();
    }
}
