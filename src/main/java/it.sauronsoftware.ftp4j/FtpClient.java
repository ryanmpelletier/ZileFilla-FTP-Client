package it.sauronsoftware.ftp4j;

/**
 * Created by ryanb on 3/13/2016.
 */

import it.sauronsoftware.ftp4j.connectors.DirectConnector;
import it.sauronsoftware.ftp4j.extrecognizers.DefaultTextualExtensionRecognizer;
import it.sauronsoftware.ftp4j.listparsers.DOSListParser;
import it.sauronsoftware.ftp4j.listparsers.EPLFListParser;
import it.sauronsoftware.ftp4j.listparsers.MLSDListParser;
import it.sauronsoftware.ftp4j.listparsers.NetWareListParser;
import it.sauronsoftware.ftp4j.listparsers.UnixListParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import javax.net.ssl.SSLSocketFactory;


public class FtpClient {

    public static final int SECURITY_FTP = 0;
    public static final int SECURITY_FTPS = 1;
    public static final int SECURITY_FTPES = 2;
    public static final int TYPE_AUTO = 0;
    public static final int TYPE_TEXTUAL = 1;
    public static final int TYPE_BINARY = 2;
    public static final int MLSD_IF_SUPPORTED = 0;
    public static final int MLSD_ALWAYS = 1;
    public static final int MLSD_NEVER = 2;
    private static final int SEND_AND_RECEIVE_BUFFER_SIZE = 65536;
    private static final DateFormat MDTM_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final Pattern PASV_PATTERN = Pattern.compile("\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3}");
    private static final Pattern PWD_PATTERN = Pattern.compile("\"/.*\"");
    private FTPConnector connector = new DirectConnector();
    private SSLSocketFactory sslSocketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
    private ArrayList communicationListeners = new ArrayList();
    private ArrayList listParsers = new ArrayList();
    private FTPTextualExtensionRecognizer textualExtensionRecognizer = DefaultTextualExtensionRecognizer.getInstance();
    private FTPListParser parser = null;
    private String host = null;
    private int port = 0;
    private int security = 0;
    private String username;
    private String password;
    private boolean connected = false;
    private boolean authenticated = false;
    private boolean passive = true;
    private int type = 0;
    private int mlsdPolicy = 0;
    private long autoNoopTimeout = 0L;
    private FtpClient.AutoNoopTimer autoNoopTimer;
    private long nextAutoNoopTime;
    private boolean restSupported = false;
    private String charset = null;
    private boolean compressionEnabled = false;
    private boolean utf8Supported = false;
    private boolean mlsdSupported = false;
    private boolean modezSupported = false;
    private boolean modezEnabled = false;
    private boolean dataChannelEncrypted = false;
    private boolean ongoingDataTransfer = false;
    private InputStream dataTransferInputStream = null;
    private OutputStream dataTransferOutputStream = null;
    private boolean aborted = false;
    private boolean consumeAborCommandReply = false;
    private Object lock = new Object();
    private Object abortLock = new Object();
    private FTPCommunicationChannel communication = null;

    public FtpClient() {
        this.addListParser(new UnixListParser());
        this.addListParser(new DOSListParser());
        this.addListParser(new EPLFListParser());
        this.addListParser(new NetWareListParser());
        this.addListParser(new MLSDListParser());
    }

    public FTPConnector getConnector() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.connector;
        }
    }

    public void setConnector(FTPConnector connector) {
        Object var2 = this.lock;
        synchronized(this.lock) {
            this.connector = connector;
        }
    }

    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        Object var2 = this.lock;
        synchronized(this.lock) {
            this.sslSocketFactory = sslSocketFactory;
        }
    }

    public SSLSocketFactory getSSLSocketFactory() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.sslSocketFactory;
        }
    }

    public void setSecurity(int security) throws IllegalStateException, IllegalArgumentException {
        if(security != 0 && security != 1 && security != 2) {
            throw new IllegalArgumentException("Invalid security");
        } else {
            Object var2 = this.lock;
            synchronized(this.lock) {
                if(this.connected) {
                    throw new IllegalStateException("The security level of the connection can\'t be changed while the client is connected");
                } else {
                    this.security = security;
                }
            }
        }
    }

    public int getSecurity() {
        return this.security;
    }

    public Socket ssl(Socket socket, String host, int port) throws IOException {
        return this.sslSocketFactory.createSocket(socket, host, port, true);
    }

    public void setPassive(boolean passive) {
        Object var2 = this.lock;
        synchronized(this.lock) {
            this.passive = passive;
        }
    }

    public void setType(int type) throws IllegalArgumentException {
        if(type != 0 && type != 2 && type != 1) {
            throw new IllegalArgumentException("Invalid type");
        } else {
            Object var2 = this.lock;
            synchronized(this.lock) {
                this.type = type;
            }
        }
    }

    public int getType() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.type;
        }
    }

    public void setMLSDPolicy(int mlsdPolicy) throws IllegalArgumentException {
        if(this.type != 0 && this.type != 1 && this.type != 2) {
            throw new IllegalArgumentException("Invalid MLSD policy");
        } else {
            Object var2 = this.lock;
            synchronized(this.lock) {
                this.mlsdPolicy = mlsdPolicy;
            }
        }
    }

    public int getMLSDPolicy() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.mlsdPolicy;
        }
    }

    public String getCharset() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.charset;
        }
    }

    public void setCharset(String charset) {
        Object var2 = this.lock;
        synchronized(this.lock) {
            this.charset = charset;
            if(this.connected) {
                try {
                    this.communication.changeCharset(this.pickCharset());
                } catch (IOException var5) {
                    var5.printStackTrace();
                }
            }

        }
    }

    public boolean isResumeSupported() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.restSupported;
        }
    }

    public boolean isCompressionSupported() {
        return this.modezSupported;
    }

    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    public boolean isCompressionEnabled() {
        return this.compressionEnabled;
    }

    public FTPTextualExtensionRecognizer getTextualExtensionRecognizer() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.textualExtensionRecognizer;
        }
    }

    public void setTextualExtensionRecognizer(FTPTextualExtensionRecognizer textualExtensionRecognizer) {
        Object var2 = this.lock;
        synchronized(this.lock) {
            this.textualExtensionRecognizer = textualExtensionRecognizer;
        }
    }

    public boolean isAuthenticated() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.authenticated;
        }
    }

    public boolean isConnected() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.connected;
        }
    }

    public boolean isPassive() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.passive;
        }
    }

    public String getHost() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.host;
        }
    }

    public int getPort() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.port;
        }
    }

    public String getPassword() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.password;
        }
    }

    public String getUsername() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.username;
        }
    }

    public void setAutoNoopTimeout(long autoNoopTimeout) {
        Object var3 = this.lock;
        synchronized(this.lock) {
            if(this.connected && this.authenticated) {
                this.stopAutoNoopTimer();
            }

            long oldValue = this.autoNoopTimeout;
            this.autoNoopTimeout = autoNoopTimeout;
            if(oldValue != 0L && autoNoopTimeout != 0L && this.nextAutoNoopTime > 0L) {
                this.nextAutoNoopTime -= oldValue - autoNoopTimeout;
            }

            if(this.connected && this.authenticated) {
                this.startAutoNoopTimer();
            }

        }
    }

    public long getAutoNoopTimeout() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            return this.autoNoopTimeout;
        }
    }

    public void addCommunicationListener(FTPCommunicationListener listener) {
        Object var2 = this.lock;
        synchronized(this.lock) {
            this.communicationListeners.add(listener);
            if(this.communication != null) {
                this.communication.addCommunicationListener(listener);
            }

        }
    }

    public void removeCommunicationListener(FTPCommunicationListener listener) {
        Object var2 = this.lock;
        synchronized(this.lock) {
            this.communicationListeners.remove(listener);
            if(this.communication != null) {
                this.communication.removeCommunicationListener(listener);
            }

        }
    }

    public FTPCommunicationListener[] getCommunicationListeners() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            int size = this.communicationListeners.size();
            FTPCommunicationListener[] ret = new FTPCommunicationListener[size];

            for(int i = 0; i < size; ++i) {
                ret[i] = (FTPCommunicationListener)this.communicationListeners.get(i);
            }

            return ret;
        }
    }

    public void addListParser(FTPListParser listParser) {
        Object var2 = this.lock;
        synchronized(this.lock) {
            this.listParsers.add(listParser);
        }
    }

    public void removeListParser(FTPListParser listParser) {
        Object var2 = this.lock;
        synchronized(this.lock) {
            this.listParsers.remove(listParser);
        }
    }

    public FTPListParser[] getListParsers() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            int size = this.listParsers.size();
            FTPListParser[] ret = new FTPListParser[size];

            for(int i = 0; i < size; ++i) {
                ret[i] = (FTPListParser)this.listParsers.get(i);
            }

            return ret;
        }
    }

    public String[] connect(String host) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        short def;
        if(this.security == 1) {
            def = 990;
        } else {
            def = 21;
        }

        return this.connect(host, def);
    }

    public String[] connect(String host, int port) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var3 = this.lock;
        synchronized(this.lock) {
            if(this.connected) {
                throw new IllegalStateException("Client already connected to " + host + " on port " + port);
            } else {
                Socket connection = null;

                String[] var6;
                try {
                    connection = this.connector.connectForCommunicationChannel(host, port);
                    if(this.security == 1) {
                        connection = this.ssl(connection, host, port);
                    }

                    this.communication = new FTPCommunicationChannel(connection, this.pickCharset());
                    Iterator e = this.communicationListeners.iterator();

                    while(e.hasNext()) {
                        this.communication.addCommunicationListener((FTPCommunicationListener)e.next());
                    }

                    FTPReply e1 = this.communication.readFTPReply();
                    if(!e1.isSuccessCode()) {
                        throw new FTPException(e1);
                    }

                    this.connected = true;
                    this.authenticated = false;
                    this.parser = null;
                    this.host = host;
                    this.port = port;
                    this.username = null;
                    this.password = null;
                    this.utf8Supported = false;
                    this.restSupported = false;
                    this.mlsdSupported = false;
                    this.modezSupported = false;
                    this.dataChannelEncrypted = false;
                    var6 = e1.getMessages();
                } catch (IOException var17) {
                    throw var17;
                } finally {
                    if(!this.connected && connection != null) {
                        try {
                            connection.close();
                        } catch (Throwable var16) {
                            ;
                        }
                    }

                }

                return var6;
            }
        }
    }

    public void abortCurrentConnectionAttempt() {
        this.connector.abortConnectForCommunicationChannel();
    }

    public void disconnect(boolean sendQuitCommand) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var2 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else {
                if(this.authenticated) {
                    this.stopAutoNoopTimer();
                }

                if(sendQuitCommand) {
                    this.communication.sendFTPCommand("QUIT");
                    FTPReply r = this.communication.readFTPReply();
                    if(!r.isSuccessCode()) {
                        throw new FTPException(r);
                    }
                }

                this.communication.close();
                this.communication = null;
                this.connected = false;
            }
        }
    }

    public void abruptlyCloseCommunication() {
        if(this.communication != null) {
            this.communication.close();
            this.communication = null;
        }

        this.connected = false;
        this.stopAutoNoopTimer();
    }

    public void login(String username, String password) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        this.login(username, password, (String)null);
    }

    public void login(String username, String password, String account) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var4 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            }

            if(this.security == 2) {
                this.communication.sendFTPCommand("AUTH TLS");
                FTPReply passwordRequired = this.communication.readFTPReply();
                if(passwordRequired.isSuccessCode()) {
                    this.communication.ssl(this.sslSocketFactory);
                } else {
                    this.communication.sendFTPCommand("AUTH SSL");
                    passwordRequired = this.communication.readFTPReply();
                    if(!passwordRequired.isSuccessCode()) {
                        throw new FTPException(passwordRequired.getCode(), "SECURITY_FTPES cannot be applied: the server refused both AUTH TLS and AUTH SSL commands");
                    }

                    this.communication.ssl(this.sslSocketFactory);
                }
            }

            this.authenticated = false;
            this.communication.sendFTPCommand("USER " + username);
            FTPReply r = this.communication.readFTPReply();
            boolean accountRequired;
            boolean passwordRequired1;
            switch(r.getCode()) {
                case 230:
                    passwordRequired1 = false;
                    accountRequired = false;
                    break;
                case 331:
                    passwordRequired1 = true;
                    accountRequired = false;
                    break;
                case 332:
                    passwordRequired1 = false;
                    accountRequired = true;
                default:
                    throw new FTPException(r);
            }

            if(passwordRequired1) {
                if(password == null) {
                    throw new FTPException(331);
                }

                this.communication.sendFTPCommand("PASS " + password);
                r = this.communication.readFTPReply();
                switch(r.getCode()) {
                    case 230:
                        accountRequired = false;
                        break;
                    case 332:
                        accountRequired = true;
                        break;
                    default:
                        throw new FTPException(r);
                }
            }

            if(accountRequired) {
                if(account == null) {
                    throw new FTPException(332);
                }

                this.communication.sendFTPCommand("ACCT " + account);
                r = this.communication.readFTPReply();
                switch(r.getCode()) {
                    case 230:
                        break;
                    default:
                        throw new FTPException(r);
                }
            }

            this.authenticated = true;
            this.username = username;
            this.password = password;
        }

        this.postLoginOperations();
        this.startAutoNoopTimer();
    }

    public void postLoginOperations() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var1 = this.lock;
        synchronized(this.lock) {
            this.utf8Supported = false;
            this.restSupported = false;
            this.mlsdSupported = false;
            this.modezSupported = false;
            this.dataChannelEncrypted = false;
            this.communication.sendFTPCommand("FEAT");
            FTPReply r = this.communication.readFTPReply();
            if(r.getCode() == 211) {
                String[] reply = r.getMessages();

                for(int i = 1; i < reply.length - 1; ++i) {
                    String feat = reply[i].trim().toUpperCase();
                    if("REST STREAM".equalsIgnoreCase(feat)) {
                        this.restSupported = true;
                    } else if("UTF8".equalsIgnoreCase(feat)) {
                        this.utf8Supported = true;
                        this.communication.changeCharset("UTF-8");
                    } else if("MLSD".equalsIgnoreCase(feat)) {
                        this.mlsdSupported = true;
                    } else if("MODE Z".equalsIgnoreCase(feat) || feat.startsWith("MODE Z ")) {
                        this.modezSupported = true;
                    }
                }
            }

            if(this.utf8Supported) {
                this.communication.sendFTPCommand("OPTS UTF8 ON");
                this.communication.readFTPReply();
            }

            if(this.security == 1 || this.security == 2) {
                this.communication.sendFTPCommand("PBSZ 0");
                this.communication.readFTPReply();
                this.communication.sendFTPCommand("PROT P");
                FTPReply var8 = this.communication.readFTPReply();
                if(var8.isSuccessCode()) {
                    this.dataChannelEncrypted = true;
                }
            }

        }
    }

    public void logout() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var1 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("REIN");
                FTPReply r = this.communication.readFTPReply();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                } else {
                    this.stopAutoNoopTimer();
                    this.authenticated = false;
                    this.username = null;
                    this.password = null;
                }
            }
        }
    }

    public void noop() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var1 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                try {
                    this.communication.sendFTPCommand("NOOP");
                    FTPReply r = this.communication.readFTPReply();
                    if(!r.isSuccessCode()) {
                        throw new FTPException(r);
                    }
                } finally {
                    this.touchAutoNoopTimer();
                }

            }
        }
    }

    public FTPReply sendCustomCommand(String command) throws IllegalStateException, IOException, FTPIllegalReplyException {
        Object var2 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else {
                this.communication.sendFTPCommand(command);
                this.touchAutoNoopTimer();
                return this.communication.readFTPReply();
            }
        }
    }

    public FTPReply sendSiteCommand(String command) throws IllegalStateException, IOException, FTPIllegalReplyException {
        Object var2 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else {
                this.communication.sendFTPCommand("SITE " + command);
                this.touchAutoNoopTimer();
                return this.communication.readFTPReply();
            }
        }
    }

    public void changeAccount(String account) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var2 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("ACCT " + account);
                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            }
        }
    }

    public String currentDirectory() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var1 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("PWD");
                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                } else {
                    String[] messages = r.getMessages();
                    if(messages.length != 1) {
                        throw new FTPIllegalReplyException();
                    } else {
                        Matcher m = PWD_PATTERN.matcher(messages[0]);
                        if(m.find()) {
                            return messages[0].substring(m.start() + 1, m.end() - 1);
                        } else {
                            throw new FTPIllegalReplyException();
                        }
                    }
                }
            }
        }
    }

    public void changeDirectory(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var2 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("CWD " + path);
                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            }
        }
    }

    public void changeDirectoryUp() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var1 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("CDUP");
                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            }
        }
    }

    public Date modifiedDate(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var2 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("MDTM " + path);
                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                } else {
                    String[] messages = r.getMessages();
                    if(messages.length != 1) {
                        throw new FTPIllegalReplyException();
                    } else {
                        Date var10000;
                        try {
                            var10000 = MDTM_DATE_FORMAT.parse(messages[0]);
                        } catch (ParseException var7) {
                            throw new FTPIllegalReplyException();
                        }

                        return var10000;
                    }
                }
            }
        }
    }

    public long fileSize(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var2 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("TYPE I");
                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                } else {
                    this.communication.sendFTPCommand("SIZE " + path);
                    r = this.communication.readFTPReply();
                    this.touchAutoNoopTimer();
                    if(!r.isSuccessCode()) {
                        throw new FTPException(r);
                    } else {
                        String[] messages = r.getMessages();
                        if(messages.length != 1) {
                            throw new FTPIllegalReplyException();
                        } else {
                            long var10000;
                            try {
                                var10000 = Long.parseLong(messages[0]);
                            } catch (Throwable var7) {
                                throw new FTPIllegalReplyException();
                            }

                            return var10000;
                        }
                    }
                }
            }
        }
    }

    public void rename(String oldPath, String newPath) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var3 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("RNFR " + oldPath);
                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(r.getCode() != 350) {
                    throw new FTPException(r);
                } else {
                    this.communication.sendFTPCommand("RNTO " + newPath);
                    r = this.communication.readFTPReply();
                    this.touchAutoNoopTimer();
                    if(!r.isSuccessCode()) {
                        throw new FTPException(r);
                    }
                }
            }
        }
    }

    public void deleteFile(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var2 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("DELE " + path);
                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            }
        }
    }

    public void deleteDirectory(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var2 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("RMD " + path);
                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            }
        }
    }

    public void createDirectory(String directoryName) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var2 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("MKD " + directoryName);
                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            }
        }
    }

    public String[] help() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var1 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("HELP");
                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                } else {
                    return r.getMessages();
                }
            }
        }
    }

    public String[] serverStatus() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Object var1 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("STAT");
                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                } else {
                    return r.getMessages();
                }
            }
        }
    }

    public FTPFile[] list(String fileSpec) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        Object var2 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("TYPE A");
                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                } else {
                    FTPDataTransferConnectionProvider provider = this.openDataTransferChannel();
                    boolean mlsdCommand;
                    if(this.mlsdPolicy == 0) {
                        mlsdCommand = this.mlsdSupported;
                    } else if(this.mlsdPolicy == 1) {
                        mlsdCommand = true;
                    } else {
                        mlsdCommand = false;
                    }

                    String command = mlsdCommand?"MLSD":"LIST";
                    if(fileSpec != null && fileSpec.length() > 0) {
                        command = command + " " + fileSpec;
                    }

                    ArrayList lines = new ArrayList();
                    boolean wasAborted = false;
                    this.communication.sendFTPCommand(command);

                    try {
                        Socket size;
                        try {
                            size = provider.openDataTransferConnection();
                        } finally {
                            provider.dispose();
                        }

                        Object list = this.abortLock;
                        synchronized(this.abortLock) {
                            this.ongoingDataTransfer = true;
                            this.aborted = false;
                            this.consumeAborCommandReply = false;
                        }

                        NVTASCIIReader var88 = null;

                        try {
                            this.dataTransferInputStream = size.getInputStream();
                            if(this.modezEnabled) {
                                this.dataTransferInputStream = new InflaterInputStream(this.dataTransferInputStream);
                            }

                            var88 = new NVTASCIIReader(this.dataTransferInputStream, mlsdCommand?"UTF-8":this.pickCharset());

                            String var90;
                            while((var90 = var88.readLine()) != null) {
                                if(var90.length() > 0) {
                                    lines.add(var90);
                                }
                            }
                        } catch (IOException var83) {
                            IOException ret = var83;
                            Object i = this.abortLock;
                            synchronized(this.abortLock){}

                            try {
                                if(this.aborted) {
                                    throw new FTPAbortedException();
                                }

                                throw new FTPDataTransferException("I/O error in data transfer", ret);
                            } finally {
                                ;
                            }
                        } finally {
                            if(var88 != null) {
                                try {
                                    var88.close();
                                } catch (Throwable var77) {
                                    ;
                                }
                            }

                            try {
                                size.close();
                            } catch (Throwable var76) {
                                ;
                            }

                            this.dataTransferInputStream = null;
                            Object t = this.abortLock;
                            synchronized(this.abortLock) {
                                wasAborted = this.aborted;
                                this.ongoingDataTransfer = false;
                                this.aborted = false;
                            }
                        }
                    } finally {
                        r = this.communication.readFTPReply();
                        this.touchAutoNoopTimer();
                        if(r.getCode() != 150 && r.getCode() != 125) {
                            throw new FTPException(r);
                        }

                        r = this.communication.readFTPReply();
                        if(!wasAborted && r.getCode() != 226) {
                            throw new FTPException(r);
                        }

                        if(this.consumeAborCommandReply) {
                            this.communication.readFTPReply();
                            this.consumeAborCommandReply = false;
                        }

                    }

                    int var87 = lines.size();
                    String[] var89 = new String[var87];

                    for(int var91 = 0; var91 < var87; ++var91) {
                        var89[var91] = (String)lines.get(var91);
                    }

                    FTPFile[] var92 = null;
                    if(mlsdCommand) {
                        MLSDListParser var93 = new MLSDListParser();
                        var92 = var93.parse(var89);
                    } else {
                        if(this.parser != null) {
                            try {
                                var92 = this.parser.parse(var89);
                            } catch (FTPListParseException var79) {
                                this.parser = null;
                            }
                        }

                        if(var92 == null) {
                            Iterator var94 = this.listParsers.iterator();

                            while(var94.hasNext()) {
                                FTPListParser aux = (FTPListParser)var94.next();

                                try {
                                    var92 = aux.parse(var89);
                                    this.parser = aux;
                                    break;
                                } catch (FTPListParseException var82) {
                                    ;
                                }
                            }
                        }
                    }

                    if(var92 == null) {
                        throw new FTPListParseException();
                    } else {
                        return var92;
                    }
                }
            }
        }
    }

    public FTPFile[] list() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        return this.list((String)null);
    }

    public String[] listNames() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        Object var1 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("TYPE A");
                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                } else {
                    ArrayList lines = new ArrayList();
                    boolean wasAborted = false;
                    FTPDataTransferConnectionProvider provider = this.openDataTransferChannel();
                    this.communication.sendFTPCommand("NLST");

                    try {
                        Socket size;
                        try {
                            size = provider.openDataTransferConnection();
                        } finally {
                            provider.dispose();
                        }

                        Object list = this.abortLock;
                        synchronized(this.abortLock) {
                            this.ongoingDataTransfer = true;
                            this.aborted = false;
                            this.consumeAborCommandReply = false;
                        }

                        NVTASCIIReader var75 = null;

                        try {
                            this.dataTransferInputStream = size.getInputStream();
                            if(this.modezEnabled) {
                                this.dataTransferInputStream = new InflaterInputStream(this.dataTransferInputStream);
                            }

                            var75 = new NVTASCIIReader(this.dataTransferInputStream, this.pickCharset());

                            String var77;
                            while((var77 = var75.readLine()) != null) {
                                if(var77.length() > 0) {
                                    lines.add(var77);
                                }
                            }
                        } catch (IOException var70) {
                            IOException i = var70;
                            Object var9 = this.abortLock;
                            synchronized(this.abortLock){}

                            try {
                                if(this.aborted) {
                                    throw new FTPAbortedException();
                                }

                                throw new FTPDataTransferException("I/O error in data transfer", i);
                            } finally {
                                ;
                            }
                        } finally {
                            if(var75 != null) {
                                try {
                                    var75.close();
                                } catch (Throwable var66) {
                                    ;
                                }
                            }

                            try {
                                size.close();
                            } catch (Throwable var65) {
                                ;
                            }

                            this.dataTransferInputStream = null;
                            Object t = this.abortLock;
                            synchronized(this.abortLock) {
                                wasAborted = this.aborted;
                                this.ongoingDataTransfer = false;
                                this.aborted = false;
                            }
                        }
                    } finally {
                        r = this.communication.readFTPReply();
                        if(r.getCode() != 150 && r.getCode() != 125) {
                            throw new FTPException(r);
                        }

                        r = this.communication.readFTPReply();
                        if(!wasAborted && r.getCode() != 226) {
                            throw new FTPException(r);
                        }

                        if(this.consumeAborCommandReply) {
                            this.communication.readFTPReply();
                            this.consumeAborCommandReply = false;
                        }

                    }

                    int var74 = lines.size();
                    String[] var76 = new String[var74];

                    for(int var78 = 0; var78 < var74; ++var78) {
                        var76[var78] = (String)lines.get(var78);
                    }

                    return var76;
                }
            }
        }
    }

    public void upload(File file) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        this.upload(file, 0L, (FTPDataTransferListener)null);
    }

    public void upload(File file, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        this.upload(file, 0L, listener);
    }

    public void upload(File file, long restartAt) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        this.upload(file, restartAt, (FTPDataTransferListener)null);
    }

    public void upload(File file, long restartAt, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        if(!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        } else {
            FileInputStream inputStream = null;

            try {
                inputStream = new FileInputStream(file);
            } catch (IOException var27) {
                throw new FTPDataTransferException(var27);
            }

            try {
                this.upload(file.getName(), inputStream, restartAt, restartAt, listener);
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
    }

    public void upload(String fileName, InputStream inputStream, long restartAt, long streamOffset, FTPDataTransferListener listener) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        Object var8 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                int tp = this.type;
                if(tp == 0) {
                    tp = this.detectType(fileName);
                }

                if(tp == 1) {
                    this.communication.sendFTPCommand("TYPE A");
                } else if(tp == 2) {
                    this.communication.sendFTPCommand("TYPE I");
                }

                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                } else {
                    FTPDataTransferConnectionProvider provider = this.openDataTransferChannel();
                    boolean wasAborted;
                    if(this.restSupported || restartAt > 0L) {
                        wasAborted = false;

                        try {
                            this.communication.sendFTPCommand("REST " + restartAt);
                            r = this.communication.readFTPReply();
                            this.touchAutoNoopTimer();
                            if(r.getCode() != 350 && (r.getCode() != 501 && r.getCode() != 502 || restartAt > 0L)) {
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
                    this.communication.sendFTPCommand("STOR " + fileName);

                    try {
                        Socket dtConnection;
                        try {
                            dtConnection = provider.openDataTransferConnection();
                        } finally {
                            provider.dispose();
                        }

                        Object e = this.abortLock;
                        synchronized(this.abortLock) {
                            this.ongoingDataTransfer = true;
                            this.aborted = false;
                            this.consumeAborCommandReply = false;
                        }

                        try {
                            inputStream.skip(streamOffset);
                            this.dataTransferOutputStream = dtConnection.getOutputStream();
                            if(this.modezEnabled) {
                                this.dataTransferOutputStream = new DeflaterOutputStream(this.dataTransferOutputStream);
                            }

                            if(listener != null) {
                                listener.started();
                            }

                            if(tp == 1) {
                                InputStreamReader e2 = new InputStreamReader(inputStream);
                                OutputStreamWriter l2 = new OutputStreamWriter(this.dataTransferOutputStream, this.pickCharset());
                                char[] buffer = new char[65536];

                                int l1;
                                while((l1 = e2.read(buffer)) != -1) {
                                    l2.write(buffer, 0, l1);
                                    l2.flush();
                                    if(listener != null) {
                                        listener.transferred(l1);
                                    }
                                }
                            } else if(tp == 2) {
                                byte[] e3 = new byte[65536];

                                int l3;
                                while((l3 = inputStream.read(e3)) != -1) {
                                    this.dataTransferOutputStream.write(e3, 0, l3);
                                    this.dataTransferOutputStream.flush();
                                    if(listener != null) {
                                        listener.transferred(l3);
                                    }
                                }
                            }
                        } catch (IOException var93) {
                            IOException e1 = var93;
                            Object l = this.abortLock;
                            synchronized(this.abortLock){}

                            try {
                                if(this.aborted) {
                                    if(listener != null) {
                                        listener.aborted();
                                    }

                                    throw new FTPAbortedException();
                                }

                                if(listener != null) {
                                    listener.failed();
                                }

                                throw new FTPDataTransferException("I/O error in data transfer", e1);
                            } finally {
                                ;
                            }
                        } finally {
                            if(this.dataTransferOutputStream != null) {
                                try {
                                    this.dataTransferOutputStream.close();
                                } catch (Throwable var89) {
                                    ;
                                }
                            }

                            try {
                                dtConnection.close();
                            } catch (Throwable var88) {
                                ;
                            }

                            this.dataTransferOutputStream = null;
                            Object t = this.abortLock;
                            synchronized(this.abortLock) {
                                wasAborted = this.aborted;
                                this.ongoingDataTransfer = false;
                                this.aborted = false;
                            }
                        }
                    } finally {
                        r = this.communication.readFTPReply();
                        this.touchAutoNoopTimer();
                        if(r.getCode() != 150 && r.getCode() != 125) {
                            throw new FTPException(r);
                        }

                        r = this.communication.readFTPReply();
                        if(!wasAborted && r.getCode() != 226) {
                            throw new FTPException(r);
                        }

                        if(this.consumeAborCommandReply) {
                            this.communication.readFTPReply();
                            this.consumeAborCommandReply = false;
                        }

                    }

                    if(listener != null) {
                        listener.completed();
                    }

                }
            }
        }
    }

    public void append(File file) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        this.append(file, (FTPDataTransferListener)null);
    }

    public void append(File file, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        if(!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        } else {
            FileInputStream inputStream = null;

            try {
                inputStream = new FileInputStream(file);
            } catch (IOException var25) {
                throw new FTPDataTransferException(var25);
            }

            try {
                this.append(file.getName(), inputStream, 0L, listener);
            } catch (IllegalStateException var19) {
                throw var19;
            } catch (IOException var20) {
                throw var20;
            } catch (FTPIllegalReplyException var21) {
                throw var21;
            } catch (FTPException var22) {
                throw var22;
            } catch (FTPDataTransferException var23) {
                throw var23;
            } catch (FTPAbortedException var24) {
                throw var24;
            } finally {
                if(inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable var18) {
                        ;
                    }
                }

            }

        }
    }

    public void append(String fileName, InputStream inputStream, long streamOffset, FTPDataTransferListener listener) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        Object var6 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                int tp = this.type;
                if(tp == 0) {
                    tp = this.detectType(fileName);
                }

                if(tp == 1) {
                    this.communication.sendFTPCommand("TYPE A");
                } else if(tp == 2) {
                    this.communication.sendFTPCommand("TYPE I");
                }

                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                } else {
                    boolean wasAborted = false;
                    FTPDataTransferConnectionProvider provider = this.openDataTransferChannel();
                    this.communication.sendFTPCommand("APPE " + fileName);

                    try {
                        Socket dtConnection;
                        try {
                            dtConnection = provider.openDataTransferConnection();
                        } finally {
                            provider.dispose();
                        }

                        Object e = this.abortLock;
                        synchronized(this.abortLock) {
                            this.ongoingDataTransfer = true;
                            this.aborted = false;
                            this.consumeAborCommandReply = false;
                        }

                        try {
                            inputStream.skip(streamOffset);
                            this.dataTransferOutputStream = dtConnection.getOutputStream();
                            if(this.modezEnabled) {
                                this.dataTransferOutputStream = new DeflaterOutputStream(this.dataTransferOutputStream);
                            }

                            if(listener != null) {
                                listener.started();
                            }

                            if(tp == 1) {
                                InputStreamReader e2 = new InputStreamReader(inputStream);
                                OutputStreamWriter l2 = new OutputStreamWriter(this.dataTransferOutputStream, this.pickCharset());
                                char[] buffer = new char[65536];

                                int l1;
                                while((l1 = e2.read(buffer)) != -1) {
                                    l2.write(buffer, 0, l1);
                                    l2.flush();
                                    if(listener != null) {
                                        listener.transferred(l1);
                                    }
                                }
                            } else if(tp == 2) {
                                byte[] e3 = new byte[65536];

                                int l3;
                                while((l3 = inputStream.read(e3)) != -1) {
                                    this.dataTransferOutputStream.write(e3, 0, l3);
                                    this.dataTransferOutputStream.flush();
                                    if(listener != null) {
                                        listener.transferred(l3);
                                    }
                                }
                            }
                        } catch (IOException var76) {
                            IOException e1 = var76;
                            Object l = this.abortLock;
                            synchronized(this.abortLock){}

                            try {
                                if(this.aborted) {
                                    if(listener != null) {
                                        listener.aborted();
                                    }

                                    throw new FTPAbortedException();
                                }

                                if(listener != null) {
                                    listener.failed();
                                }

                                throw new FTPDataTransferException("I/O error in data transfer", e1);
                            } finally {
                                ;
                            }
                        } finally {
                            if(this.dataTransferOutputStream != null) {
                                try {
                                    this.dataTransferOutputStream.close();
                                } catch (Throwable var72) {
                                    ;
                                }
                            }

                            try {
                                dtConnection.close();
                            } catch (Throwable var71) {
                                ;
                            }

                            this.dataTransferOutputStream = null;
                            Object t = this.abortLock;
                            synchronized(this.abortLock) {
                                wasAborted = this.aborted;
                                this.ongoingDataTransfer = false;
                                this.aborted = false;
                            }
                        }
                    } finally {
                        r = this.communication.readFTPReply();
                        this.touchAutoNoopTimer();
                        if(r.getCode() != 150 && r.getCode() != 125) {
                            throw new FTPException(r);
                        }

                        r = this.communication.readFTPReply();
                        if(!wasAborted && r.getCode() != 226) {
                            throw new FTPException(r);
                        }

                        if(this.consumeAborCommandReply) {
                            this.communication.readFTPReply();
                            this.consumeAborCommandReply = false;
                        }

                    }

                    if(listener != null) {
                        listener.completed();
                    }

                }
            }
        }
    }

    public void download(String remoteFileName, File localFile) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        this.download(remoteFileName, (File)localFile, 0L, (FTPDataTransferListener)null);
    }

    public void download(String remoteFileName, File localFile, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        this.download(remoteFileName, localFile, 0L, listener);
    }

    public void download(String remoteFileName, File localFile, long restartAt) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        this.download(remoteFileName, (File)localFile, restartAt, (FTPDataTransferListener)null);
    }

    public void download(String remoteFileName, File localFile, long restartAt, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(localFile, restartAt > 0L);
        } catch (IOException var28) {
            throw new FTPDataTransferException(var28);
        }

        try {
            this.download(remoteFileName, (OutputStream)outputStream, restartAt, listener);
        } catch (IllegalStateException var22) {
            throw var22;
        } catch (IOException var23) {
            throw var23;
        } catch (FTPIllegalReplyException var24) {
            throw var24;
        } catch (FTPException var25) {
            throw var25;
        } catch (FTPDataTransferException var26) {
            throw var26;
        } catch (FTPAbortedException var27) {
            throw var27;
        } finally {
            if(outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable var21) {
                    ;
                }
            }

        }

    }

    public void download(String fileName, OutputStream outputStream, long restartAt, FTPDataTransferListener listener) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        Object var6 = this.lock;
        synchronized(this.lock) {
            if(!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if(!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                int tp = this.type;
                if(tp == 0) {
                    tp = this.detectType(fileName);
                }

                if(tp == 1) {
                    this.communication.sendFTPCommand("TYPE A");
                } else if(tp == 2) {
                    this.communication.sendFTPCommand("TYPE I");
                }

                FTPReply r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(!r.isSuccessCode()) {
                    throw new FTPException(r);
                } else {
                    FTPDataTransferConnectionProvider provider = this.openDataTransferChannel();
                    boolean wasAborted;
                    if(this.restSupported || restartAt > 0L) {
                        wasAborted = false;

                        try {
                            this.communication.sendFTPCommand("REST " + restartAt);
                            r = this.communication.readFTPReply();
                            this.touchAutoNoopTimer();
                            if(r.getCode() != 350 && (r.getCode() != 501 && r.getCode() != 502 || restartAt > 0L)) {
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
                    this.communication.sendFTPCommand("RETR " + fileName);

                    try {
                        Socket dtConnection;
                        try {
                            dtConnection = provider.openDataTransferConnection();
                        } finally {
                            provider.dispose();
                        }

                        Object e = this.abortLock;
                        synchronized(this.abortLock) {
                            this.ongoingDataTransfer = true;
                            this.aborted = false;
                            this.consumeAborCommandReply = false;
                        }

                        try {
                            this.dataTransferInputStream = dtConnection.getInputStream();
                            if(this.modezEnabled) {
                                this.dataTransferInputStream = new InflaterInputStream(this.dataTransferInputStream);
                            }

                            if(listener != null) {
                                listener.started();
                            }

                            if(tp == 1) {
                                InputStreamReader e2 = new InputStreamReader(this.dataTransferInputStream, this.pickCharset());
                                OutputStreamWriter l2 = new OutputStreamWriter(outputStream);
                                char[] buffer = new char[65536];

                                int l1;
                                while((l1 = e2.read(buffer, 0, buffer.length)) != -1) {
                                    l2.write(buffer, 0, l1);
                                    l2.flush();
                                    if(listener != null) {
                                        listener.transferred(l1);
                                    }
                                }
                            } else if(tp == 2) {
                                byte[] e3 = new byte[65536];

                                int l3;
                                while((l3 = this.dataTransferInputStream.read(e3, 0, e3.length)) != -1) {
                                    outputStream.write(e3, 0, l3);
                                    if(listener != null) {
                                        listener.transferred(l3);
                                    }
                                }
                            }
                        } catch (IOException var91) {
                            IOException e1 = var91;
                            Object l = this.abortLock;
                            synchronized(this.abortLock){}

                            try {
                                if(this.aborted) {
                                    if(listener != null) {
                                        listener.aborted();
                                    }

                                    throw new FTPAbortedException();
                                }

                                if(listener != null) {
                                    listener.failed();
                                }

                                throw new FTPDataTransferException("I/O error in data transfer", e1);
                            } finally {
                                ;
                            }
                        } finally {
                            if(this.dataTransferInputStream != null) {
                                try {
                                    this.dataTransferInputStream.close();
                                } catch (Throwable var87) {
                                    ;
                                }
                            }

                            try {
                                dtConnection.close();
                            } catch (Throwable var86) {
                                ;
                            }

                            this.dataTransferInputStream = null;
                            Object t = this.abortLock;
                            synchronized(this.abortLock) {
                                wasAborted = this.aborted;
                                this.ongoingDataTransfer = false;
                                this.aborted = false;
                            }
                        }
                    } finally {
                        r = this.communication.readFTPReply();
                        this.touchAutoNoopTimer();
                        if(r.getCode() != 150 && r.getCode() != 125) {
                            throw new FTPException(r);
                        }

                        r = this.communication.readFTPReply();
                        if(!wasAborted && r.getCode() != 226) {
                            throw new FTPException(r);
                        }

                        if(this.consumeAborCommandReply) {
                            this.communication.readFTPReply();
                            this.consumeAborCommandReply = false;
                        }

                    }

                    if(listener != null) {
                        listener.completed();
                    }

                }
            }
        }
    }

    public int detectType(String fileName) throws IOException, FTPIllegalReplyException, FTPException {
        int start = fileName.lastIndexOf(46) + 1;
        int stop = fileName.length();
        if(start > 0 && start < stop - 1) {
            String ext = fileName.substring(start, stop);
            ext = ext.toLowerCase();
            return this.textualExtensionRecognizer.isTextualExt(ext)?1:2;
        } else {
            return 2;
        }
    }

    public FTPDataTransferConnectionProvider openDataTransferChannel() throws IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException {
        FTPReply r;
        if(this.modezSupported && this.compressionEnabled) {
            if(!this.modezEnabled) {
                this.communication.sendFTPCommand("MODE Z");
                r = this.communication.readFTPReply();
                this.touchAutoNoopTimer();
                if(r.isSuccessCode()) {
                    this.modezEnabled = true;
                }
            }
        } else if(this.modezEnabled) {
            this.communication.sendFTPCommand("MODE S");
            r = this.communication.readFTPReply();
            this.touchAutoNoopTimer();
            if(r.isSuccessCode()) {
                this.modezEnabled = false;
            }
        }

        return this.passive?this.openPassiveDataTransferChannel():this.openActiveDataTransferChannel();
    }

    public FTPDataTransferConnectionProvider openActiveDataTransferChannel() throws IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException {
        FTPDataTransferServer server = new FTPDataTransferServer() {
            public Socket openDataTransferConnection() throws FTPDataTransferException {
                Socket socket = super.openDataTransferConnection();
                if(FtpClient.this.dataChannelEncrypted) {
                    try {
                        socket = FtpClient.this.ssl(socket, socket.getInetAddress().getHostName(), socket.getPort());
                    } catch (IOException var5) {
                        try {
                            socket.close();
                        } catch (Throwable var4) {
                            ;
                        }

                        throw new FTPDataTransferException(var5);
                    }
                }

                return socket;
            }
        };
        int port = server.getPort();
        int p1 = port >>> 8;
        int p2 = port & 255;
        int[] addr = this.pickLocalAddress();
        this.communication.sendFTPCommand("PORT " + addr[0] + "," + addr[1] + "," + addr[2] + "," + addr[3] + "," + p1 + "," + p2);
        FTPReply r = this.communication.readFTPReply();
        this.touchAutoNoopTimer();
        if(!r.isSuccessCode()) {
            server.dispose();

            try {
                Socket t = server.openDataTransferConnection();
                t.close();
            } catch (Throwable var8) {
                ;
            }

            throw new FTPException(r);
        } else {
            return server;
        }
    }

    public FTPDataTransferConnectionProvider openPassiveDataTransferChannel() throws IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException {
        this.communication.sendFTPCommand("PASV");
        FTPReply r = this.communication.readFTPReply();
        this.touchAutoNoopTimer();
        if(!r.isSuccessCode()) {
            throw new FTPException(r);
        } else {
            String addressAndPort = null;
            String[] messages = r.getMessages();

            int b2;
            int b3;
            for(int st = 0; st < messages.length; ++st) {
                Matcher b1 = PASV_PATTERN.matcher(messages[st]);
                if(b1.find()) {
                    b2 = b1.start();
                    b3 = b1.end();
                    addressAndPort = messages[st].substring(b2, b3);
                    break;
                }
            }

            if(addressAndPort == null) {
                throw new FTPIllegalReplyException();
            } else {
                StringTokenizer var14 = new StringTokenizer(addressAndPort, ",");
                int var15 = Integer.parseInt(var14.nextToken());
                b2 = Integer.parseInt(var14.nextToken());
                b3 = Integer.parseInt(var14.nextToken());
                int b4 = Integer.parseInt(var14.nextToken());
                int p1 = Integer.parseInt(var14.nextToken());
                int p2 = Integer.parseInt(var14.nextToken());
                final String pasvHost = var15 + "." + b2 + "." + b3 + "." + b4;
                final int pasvPort = p1 << 8 | p2;
                FTPDataTransferConnectionProvider provider = new FTPDataTransferConnectionProvider() {
                    public Socket openDataTransferConnection() throws FTPDataTransferException {
                        try {
                            String e = FtpClient.this.connector.getUseSuggestedAddressForDataConnections()?pasvHost:FtpClient.this.host;
                            Socket dtConnection = FtpClient.this.connector.connectForDataTransferChannel(e, pasvPort);
                            if(FtpClient.this.dataChannelEncrypted) {
                                dtConnection = FtpClient.this.ssl(dtConnection, e, pasvPort);
                            }

                            return dtConnection;
                        } catch (IOException var3) {
                            throw new FTPDataTransferException("Cannot connect to the remote server", var3);
                        }
                    }

                    public void dispose() {
                    }
                };
                return provider;
            }
        }
    }

    public void abortCurrentDataTransfer(boolean sendAborCommand) throws IOException, FTPIllegalReplyException {
        Object var2 = this.abortLock;
        synchronized(this.abortLock) {
            if(this.ongoingDataTransfer && !this.aborted) {
                if(sendAborCommand) {
                    this.communication.sendFTPCommand("ABOR");
                    this.touchAutoNoopTimer();
                    this.consumeAborCommandReply = true;
                }

                if(this.dataTransferInputStream != null) {
                    try {
                        this.dataTransferInputStream.close();
                    } catch (Throwable var6) {
                        ;
                    }
                }

                if(this.dataTransferOutputStream != null) {
                    try {
                        this.dataTransferOutputStream.close();
                    } catch (Throwable var5) {
                        ;
                    }
                }

                this.aborted = true;
            }

        }
    }

    public String pickCharset() {
        return this.charset != null?this.charset:(this.utf8Supported?"UTF-8":System.getProperty("file.encoding"));
    }

    public int[] pickLocalAddress() throws IOException {
        int[] ret = this.pickForcedLocalAddress();
        if(ret == null) {
            ret = this.pickAutoDetectedLocalAddress();
        }

        return ret;
    }

    public int[] pickForcedLocalAddress() {
        int[] ret = null;
        String aux = System.getProperty("ftp4j.activeDataTransfer.hostAddress");
        if(aux != null) {
            boolean valid = false;
            StringTokenizer st = new StringTokenizer(aux, ".");
            if(st.countTokens() == 4) {
                valid = true;
                int[] arr = new int[4];

                for(int i = 0; i < 4; ++i) {
                    String tk = st.nextToken();

                    try {
                        arr[i] = Integer.parseInt(tk);
                    } catch (NumberFormatException var9) {
                        arr[i] = -1;
                    }

                    if(arr[i] < 0 || arr[i] > 255) {
                        valid = false;
                        break;
                    }
                }

                if(valid) {
                    ret = arr;
                }
            }

            if(!valid) {
                System.err.println("WARNING: invalid value \"" + aux + "\" for the " + "ftp4j.activeDataTransfer.hostAddress" + " system property. The value should " + "be in the x.x.x.x form.");
            }
        }

        return ret;
    }

    public int[] pickAutoDetectedLocalAddress() throws IOException {
        InetAddress addressObj = InetAddress.getLocalHost();
        byte[] addr = addressObj.getAddress();
        int b1 = addr[0] & 255;
        int b2 = addr[1] & 255;
        int b3 = addr[2] & 255;
        int b4 = addr[3] & 255;
        int[] ret = new int[]{b1, b2, b3, b4};
        return ret;
    }

    public String toString() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(this.getClass().getName());
            buffer.append(" [connected=");
            buffer.append(this.connected);
            if(this.connected) {
                buffer.append(", host=");
                buffer.append(this.host);
                buffer.append(", port=");
                buffer.append(this.port);
            }

            buffer.append(", connector=");
            buffer.append(this.connector);
            buffer.append(", security=");
            switch(this.security) {
                case 0:
                    buffer.append("SECURITY_FTP");
                    break;
                case 1:
                    buffer.append("SECURITY_FTPS");
                    break;
                case 2:
                    buffer.append("SECURITY_FTPES");
            }

            buffer.append(", authenticated=");
            buffer.append(this.authenticated);
            int communicationListeners;
            if(this.authenticated) {
                buffer.append(", username=");
                buffer.append(this.username);
                buffer.append(", password=");
                StringBuffer listParsers = new StringBuffer();

                for(communicationListeners = 0; communicationListeners < this.password.length(); ++communicationListeners) {
                    listParsers.append('*');
                }

                buffer.append(listParsers);
                buffer.append(", restSupported=");
                buffer.append(this.restSupported);
                buffer.append(", utf8supported=");
                buffer.append(this.utf8Supported);
                buffer.append(", mlsdSupported=");
                buffer.append(this.mlsdSupported);
                buffer.append(", mode=modezSupported");
                buffer.append(this.modezSupported);
                buffer.append(", mode=modezEnabled");
                buffer.append(this.modezEnabled);
            }

            buffer.append(", transfer mode=");
            buffer.append(this.passive?"passive":"active");
            buffer.append(", transfer type=");
            switch(this.type) {
                case 0:
                    buffer.append("TYPE_AUTO");
                    break;
                case 1:
                    buffer.append("TYPE_TEXTUAL");
                    break;
                case 2:
                    buffer.append("TYPE_BINARY");
            }

            buffer.append(", textualExtensionRecognizer=");
            buffer.append(this.textualExtensionRecognizer);
            FTPListParser[] var8 = this.getListParsers();
            if(var8.length > 0) {
                buffer.append(", listParsers=");

                for(communicationListeners = 0; communicationListeners < var8.length; ++communicationListeners) {
                    if(communicationListeners > 0) {
                        buffer.append(", ");
                    }

                    buffer.append(var8[communicationListeners]);
                }
            }

            FTPCommunicationListener[] var9 = this.getCommunicationListeners();
            if(var9.length > 0) {
                buffer.append(", communicationListeners=");

                for(int i = 0; i < var9.length; ++i) {
                    if(i > 0) {
                        buffer.append(", ");
                    }

                    buffer.append(var9[i]);
                }
            }

            buffer.append(", autoNoopTimeout=");
            buffer.append(this.autoNoopTimeout);
            buffer.append("]");
            return buffer.toString();
        }
    }

    public void startAutoNoopTimer() {
        if(this.autoNoopTimeout > 0L) {
            this.autoNoopTimer = new FtpClient.AutoNoopTimer();
            this.autoNoopTimer.start();
        }

    }

    public void stopAutoNoopTimer() {
        if(this.autoNoopTimer != null) {
            this.autoNoopTimer.interrupt();
            this.autoNoopTimer = null;
        }

    }

    public void touchAutoNoopTimer() {
        if(this.autoNoopTimer != null) {
            this.nextAutoNoopTime = System.currentTimeMillis() + this.autoNoopTimeout;
        }

    }

    public class AutoNoopTimer extends Thread {
        private AutoNoopTimer() {
        }

        public void run() {
            synchronized(FtpClient.this.lock) {
                if(FtpClient.this.nextAutoNoopTime <= 0L && FtpClient.this.autoNoopTimeout > 0L) {
                    FtpClient.this.nextAutoNoopTime = System.currentTimeMillis() + FtpClient.this.autoNoopTimeout;
                }

                while(!Thread.interrupted() && FtpClient.this.autoNoopTimeout > 0L) {
                    long delay = FtpClient.this.nextAutoNoopTime - System.currentTimeMillis();
                    if(delay > 0L) {
                        try {
                            FtpClient.this.lock.wait(delay);
                        } catch (InterruptedException var7) {
                            break;
                        }
                    }

                    if(System.currentTimeMillis() >= FtpClient.this.nextAutoNoopTime) {
                        try {
                            FtpClient.this.noop();
                        } catch (Throwable var6) {
                            ;
                        }
                    }
                }

            }
        }
    }

    //getters I added

    public static int getSecurityFtp() {
        return SECURITY_FTP;
    }

    public static int getSecurityFtps() {
        return SECURITY_FTPS;
    }

    public static int getSecurityFtpes() {
        return SECURITY_FTPES;
    }

    public static int getTypeAuto() {
        return TYPE_AUTO;
    }

    public static int getTypeTextual() {
        return TYPE_TEXTUAL;
    }

    public static int getTypeBinary() {
        return TYPE_BINARY;
    }

    public static int getMlsdIfSupported() {
        return MLSD_IF_SUPPORTED;
    }

    public static int getMlsdAlways() {
        return MLSD_ALWAYS;
    }

    public static int getMlsdNever() {
        return MLSD_NEVER;
    }

    public static int getSendAndReceiveBufferSize() {
        return SEND_AND_RECEIVE_BUFFER_SIZE;
    }

    public static DateFormat getMdtmDateFormat() {
        return MDTM_DATE_FORMAT;
    }

    public static Pattern getPasvPattern() {
        return PASV_PATTERN;
    }

    public static Pattern getPwdPattern() {
        return PWD_PATTERN;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public FTPListParser getParser() {
        return parser;
    }

    public int getMlsdPolicy() {
        return mlsdPolicy;
    }

    public AutoNoopTimer getAutoNoopTimer() {
        return autoNoopTimer;
    }

    public long getNextAutoNoopTime() {
        return nextAutoNoopTime;
    }

    public boolean isRestSupported() {
        return restSupported;
    }

    public boolean isUtf8Supported() {
        return utf8Supported;
    }

    public boolean isMlsdSupported() {
        return mlsdSupported;
    }

    public boolean isModezSupported() {
        return modezSupported;
    }

    public boolean isModezEnabled() {
        return modezEnabled;
    }

    public boolean isDataChannelEncrypted() {
        return dataChannelEncrypted;
    }

    public boolean isOngoingDataTransfer() {
        return ongoingDataTransfer;
    }
    public void setOngoingDataTransfer(boolean isOngoingDataTransfer){this.ongoingDataTransfer = isOngoingDataTransfer;}

    public InputStream getDataTransferInputStream() {
        return dataTransferInputStream;
    }

    public OutputStream getDataTransferOutputStream() {
        return dataTransferOutputStream;
    }
    public void setDataTransferOutputStream(OutputStream outputStream){this.dataTransferOutputStream = outputStream;}

    public boolean isAborted() {
        return aborted;
    }
    public void setAborted(boolean isAborted){this.aborted = isAborted;}

    public boolean isConsumeAborCommandReply() {
        return consumeAborCommandReply;
    }
    public void setConsumeAborCommandReply(boolean isConsumeAborCommandReply){this.consumeAborCommandReply = isConsumeAborCommandReply;}


    public Object getLock() {
        return lock;
    }

    public Object getAbortLock() {
        return abortLock;
    }

    public FTPCommunicationChannel getCommunication() {
        return communication;
    }
}
