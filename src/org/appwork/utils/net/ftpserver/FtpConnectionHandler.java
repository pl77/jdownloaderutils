/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.ftpserver
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.ftpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * @author daniel
 * 
 */
public abstract class FtpConnectionHandler {

    private final DefaultFilelistFormatter filelistFormatter;

    public FtpConnectionHandler() {
        filelistFormatter = new DefaultFilelistFormatter();
    }

    /**
     * @return
     */
    public FtpConnectionState createNewConnectionState() {

        return new FtpConnectionState();
    }

    /**
     * @param string
     * @return TODO
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public abstract ArrayList<FtpFile> getFileList(FtpConnectionState connectionState, String string) throws UnsupportedEncodingException, IOException, FtpFileNotExistException;

    /**
     * @return
     */
    public FilelistFormatter getFilelistFormatter() {
        return filelistFormatter;
    }

    public abstract FTPUser getUser(final String user);

    public abstract String getWelcomeMessage(FtpConnectionState ftpConnection);

    /**
     * @param dir
     * @param connectionState
     * @return
     */
    public abstract void onDirectoryUp(FtpConnectionState connectionState) throws FtpFileNotExistException;

    public abstract String onLoginFailedMessage(FtpConnectionState ftpConnection);

    public abstract String onLoginSuccessRequest(FtpConnectionState connectionState);

    public abstract String onLogoutRequest(FtpConnectionState connectionState);

    /**
     * @param connectionState
     * @param cwd
     * @return
     */
    public abstract void setCurrentDirectory(FtpConnectionState connectionState, String cwd) throws FtpFileNotExistException;

    /**
     * @param outputStream
     * @param connectionState
     * @param param
     */
    public abstract long onRETR(OutputStream outputStream, FtpConnectionState connectionState, String param) throws IOException, FtpFileNotExistException;

    /**
     * @param connectionState
     * @param buildParameter
     */
    public abstract void makeDirectory(FtpConnectionState connectionState, String buildParameter) throws FtpFileNotExistException;

    /**
     * @param inputStream
     * @param connectionState
     * @param buildParameter
     * @return
     * @throws FtpFileNotExistException 
     * @throws IOException 
     */
    public abstract long onSTOR(InputStream inputStream, FtpConnectionState connectionState,boolean append, String buildParameter) throws FtpFileNotExistException, IOException;

}
