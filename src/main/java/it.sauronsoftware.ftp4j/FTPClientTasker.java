package it.sauronsoftware.ftp4j;

import it.sauronsoftware.ftp4j.*;
import javafx.concurrent.Task;

import java.io.*;
import java.net.Socket;
import java.util.zip.DeflaterOutputStream;

/**
 * Created by ryanb on 3/13/2016.
 */
public class FTPClientTasker extends FtpClient {

    public FTPClientTasker(){
        super();
    }

    //a couple of methods that return tasks

    public Task<Void> getUploadTask(File file){

        final File fileToUpload = file;
        final double fileSize = file.length();

        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                double bytesTransferredSoFar = 0;
                if(!fileToUpload.exists()) {
                    throw new FileNotFoundException(fileToUpload.getAbsolutePath());
                } else {
                    FileInputStream inputStream = null;

                    try {
                        inputStream = new FileInputStream(fileToUpload);
                    } catch (IOException var27) {
                        throw new FTPDataTransferException(var27);
                    }

                    try {
                        Object var8 = FTPClientTasker.this.getLock();
                        synchronized(FTPClientTasker.this.getLock()) {
                            if(!FTPClientTasker.this.isConnected()) {
                                throw new IllegalStateException("Client not connected");
                            } else if(!FTPClientTasker.this.isAuthenticated()) {
                                throw new IllegalStateException("Client not authenticated");
                            } else {
                                int tp = FTPClientTasker.this.getType();
                                if(tp == 0) {
                                    tp = FTPClientTasker.this.detectType(fileToUpload.getName());
                                }

                                if(tp == 1) {
                                    FTPClientTasker.this.getCommunication().sendFTPCommand("TYPE A");
                                } else if(tp == 2) {
                                    FTPClientTasker.this.getCommunication().sendFTPCommand("TYPE I");
                                }

                                FTPReply r = FTPClientTasker.this.getCommunication().readFTPReply();
                                FTPClientTasker.this.touchAutoNoopTimer();
                                if(!r.isSuccessCode()) {
                                    throw new FTPException(r);
                                } else {
                                    FTPDataTransferConnectionProvider provider = FTPClientTasker.this.openDataTransferChannel();
                                    boolean wasAborted;
                                    if(FTPClientTasker.this.isRestSupported()) {
                                        wasAborted = false;

                                        try {
                                            FTPClientTasker.this.getCommunication().sendFTPCommand("REST " + 0L);
                                            r = FTPClientTasker.this.getCommunication().readFTPReply();
                                            FTPClientTasker.this.touchAutoNoopTimer();
                                            if(r.getCode() != 350 && (r.getCode() != 501 && r.getCode() != 502)) {
                                                throw new FTPException(r);
                                            }

                                            wasAborted = true;
                                        } finally {
                                            if(!wasAborted) {
                                                provider.dispose();
                                            }

                                        }
                                    }

                                    wasAborted = false;
                                    FTPClientTasker.this.getCommunication().sendFTPCommand("STOR " + fileToUpload.getName());

                                    try {
                                        Socket dtConnection;
                                        try {
                                            dtConnection = provider.openDataTransferConnection();
                                        } finally {
                                            provider.dispose();
                                        }

                                        Object e = FTPClientTasker.this.getAbortLock();
                                        synchronized(FTPClientTasker.this.getAbortLock()) {
                                            FTPClientTasker.this.setOngoingDataTransfer(true);
                                            FTPClientTasker.this.setAborted(false);
                                            FTPClientTasker.this.setConsumeAborCommandReply(false);
                                        }

                                        try {
                                            FTPClientTasker.this.setDataTransferOutputStream(dtConnection.getOutputStream());
                                            if(FTPClientTasker.this.isModezEnabled()) {
                                                FTPClientTasker.this.setDataTransferOutputStream(new DeflaterOutputStream(FTPClientTasker.this.getDataTransferOutputStream()));
                                            }

//                                            if(listener != null) {
//                                                listener.started();
//                                            }

                                            if(tp == 1) {
                                                InputStreamReader e2 = new InputStreamReader(inputStream);
                                                OutputStreamWriter l2 = new OutputStreamWriter(FTPClientTasker.this.getDataTransferOutputStream(), FTPClientTasker.this.pickCharset());
                                                char[] buffer = new char[65536];

                                                int l1;
                                                while((l1 = e2.read(buffer)) != -1) {
                                                    l2.write(buffer, 0, l1);
                                                    l2.flush();
                                                    bytesTransferredSoFar += l1;
                                                    updateProgress(bytesTransferredSoFar, fileSize);
                                                }
                                            } else if(tp == 2) {
                                                byte[] e3 = new byte[65536];

                                                int l3;
                                                while((l3 = inputStream.read(e3)) != -1) {
                                                    FTPClientTasker.this.getDataTransferOutputStream().write(e3, 0, l3);
                                                    FTPClientTasker.this.getDataTransferOutputStream().flush();
                                                    bytesTransferredSoFar += l3;
                                                    updateProgress(bytesTransferredSoFar, fileSize);
                                                }
                                            }
                                        } catch (IOException var93) {
                                            IOException e1 = var93;
                                            Object l = FTPClientTasker.this.getAbortLock();
                                            synchronized(FTPClientTasker.this.getAbortLock()){}

                                            try {
                                                if(FTPClientTasker.this.isAborted()) {
                                                    cancelled();
                                                    throw new FTPAbortedException();
                                                }

                                                failed();

                                                throw new FTPDataTransferException("I/O error in data transfer", e1);
                                            } finally {
                                                ;
                                            }
                                        } finally {
                                            if(FTPClientTasker.this.getDataTransferOutputStream() != null) {
                                                try {
                                                    FTPClientTasker.this.getDataTransferOutputStream().close();
                                                } catch (Throwable var89) {
                                                    ;
                                                }
                                            }

                                            try {
                                                dtConnection.close();
                                            } catch (Throwable var88) {
                                                ;
                                            }

                                            FTPClientTasker.this.setDataTransferOutputStream(null);
                                            Object t = FTPClientTasker.this.getAbortLock();
                                            synchronized(FTPClientTasker.this.getAbortLock()) {
                                                wasAborted = FTPClientTasker.this.isAborted();
                                                FTPClientTasker.this.setOngoingDataTransfer(false);
                                                FTPClientTasker.this.setAborted(false);
                                            }
                                        }
                                    } finally {
                                        r = FTPClientTasker.this.getCommunication().readFTPReply();
                                        FTPClientTasker.this.touchAutoNoopTimer();
                                        if(r.getCode() != 150 && r.getCode() != 125) {
                                            throw new FTPException(r);
                                        }

                                        r = FTPClientTasker.this.getCommunication().readFTPReply();
                                        if(!wasAborted && r.getCode() != 226) {
                                            throw new FTPException(r);
                                        }

                                        if(FTPClientTasker.this.isConsumeAborCommandReply()) {
                                            FTPClientTasker.this.getCommunication().readFTPReply();
                                            FTPClientTasker.this.setConsumeAborCommandReply(false);
                                        }

                                    }
                                    succeeded();

                                }
                            }
                        }
                    } catch (IllegalStateException var21) {
                        throw var21;
                    } catch (IOException var22) {
                        throw var22;
                    } catch (FTPIllegalReplyException var23) {
                        throw var23;
                    } catch (FTPException var24) {
                        throw var24;
                    } catch (FTPDataTransferException var25) {
                        throw var25;
                    } catch (FTPAbortedException var26) {
                        throw var26;
                    } finally {
                        if(inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable var20) {
                                ;
                            }
                        }

                    }

                }
                return null;
            }
        };
    }

}
