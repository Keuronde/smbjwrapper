package ch.swaechter.smbjwrapper;

import ch.swaechter.smbjwrapper.core.AbstractSharedItem;
import ch.swaechter.smbjwrapper.streams.SharedInputStream;
import ch.swaechter.smbjwrapper.streams.SharedOutputStream;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileStandardInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.protocol.commons.buffer.Buffer.BufferException;
import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.smbj.share.File;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;

/**
 * This class represents a shared file.
 *
 * @author Simon Wächter
 */
public final class SharedFile extends AbstractSharedItem<SharedDirectory> {

    /**
     * Create a new shared file based on the shared connection and the path name.
     *
     * @param sharedConnection Shared connection
     * @param pathName         Path name
     */
    public SharedFile(SharedConnection sharedConnection, String pathName) {
        super(sharedConnection, pathName);
    }

    /**
     * Create a new file.
     */
    public void createFile() {
        File file = getDiskShare().openFile(getPath(), EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OVERWRITE_IF, null);
        file.close();
    }

    /**
     * Delete the current file.
     */
    public void deleteFile() {
        getDiskShare().rm(getPath());
    }

    /**
     * Copy the current file to another file on the same server share via server side copy. This does not work as soon
     * the files are on different shares (In that case copy the input/output streams). For more information check out
     * this Samba article: https://wiki.samba.org/index.php/Server-Side_Copy
     *
     * @param destinationSharedFile Other file on the same server share
     * @throws BufferException    Buffer related exception
     * @throws TransportException Transport related exception
     */
    public void copyFileViaServerSideCopy(SharedFile destinationSharedFile) throws Buffer.BufferException, TransportException {
        try (
            File sourceFile = getDiskShare().openFile(getPath(), EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
            File destinationFile = getDiskShare().openFile(destinationSharedFile.getPath(), EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OVERWRITE_IF, null);
        ) {
            sourceFile.remoteCopyTo(destinationFile);
        }
    }

    /**
     * Get the input stream of the file that can be used to download the file.
     *
     * @return Input stream of the shared file
     */
    public InputStream getInputStream() {
        File file = getDiskShare().openFile(getPath(), EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
        return new SharedInputStream(file);
    }

    /**
     * Get the output stream of the file that can be used to upload content to this file.
     *
     * @return Output stream of the shared file
     */

    public OutputStream getOutputStream() {
        return getOutputStream(false);
    }

    /**
     * Get the output stream of the file that can be used to upload and append content to this file.
     *
     * @param appendContent Append content or overwrite it
     * @return Output stream of the shared file
     */

    public OutputStream getOutputStream(boolean appendContent) {
        SMB2CreateDisposition mode = !appendContent ? SMB2CreateDisposition.FILE_OVERWRITE_IF : SMB2CreateDisposition.FILE_OPEN_IF;
        File file = getDiskShare().openFile(getPath(), EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL, mode, null);
        return new SharedOutputStream(file, appendContent);
    }

    /**
     * Get the file size of the shared item.
     *
     * @return File size of the shared items in bytes
     */
    public long getFileSize() {
        FileStandardInformation fileStandardInformation = getDiskShare().getFileInformation(getPath()).getStandardInformation();
        return fileStandardInformation.getEndOfFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SharedFile renameTo(String newFileName, boolean replaceIfExist) {
        try (File file = getDiskShare().openFile(getPath(), EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null)) {
            String newFilePath = getParentPath().getPath() + PATH_SEPARATOR + newFileName;
            file.rename(newFilePath, replaceIfExist);
            return new SharedFile(getSharedConnection(), newFilePath);
        }
    }

    /**
     * Check if the current and the given objects are equals.
     *
     * @param object Given object to compare against
     * @return Status of the check
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof SharedFile) {
            SharedFile sharedFile = (SharedFile) object;
            return getSmbPath().equals(sharedFile.getSmbPath());
        } else {
            return false;
        }
    }

    /**
     * Create a new shared directory via factory.
     *
     * @param pathName Path name of the shared item
     * @return New shared directory
     */
    @Override
    protected SharedDirectory createSharedNodeItem(String pathName) {
        return new SharedDirectory(getSharedConnection(), pathName);
    }
}
