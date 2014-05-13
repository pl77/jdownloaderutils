package org.appwork.utils.net.httpconnection;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.appwork.utils.IO;
import org.appwork.utils.Regex;
import org.appwork.utils.StringUtils;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.logging.Log;
import org.appwork.utils.processes.ProcessBuilderFactory;

public class HTTPProxy {

    public static enum TYPE {
        NONE,
        DIRECT,
        SOCKS4,
        SOCKS5,
        HTTP
    }

    // private static final int KEY_READ = 0x20019;

    public static final HTTPProxy NONE = new HTTPProxy(TYPE.NONE) {

                                           @Override
                                           public void setConnectMethodPrefered(final boolean value) {
                                           }

                                           @Override
                                           public void setLocalIP(final InetAddress localIP) {
                                           }

                                           @Override
                                           public void setPass(final String pass) {
                                           }

                                           @Override
                                           public void setPort(final int port) {
                                           }

                                           @Override
                                           public void setType(final TYPE type) {
                                           }

                                           @Override
                                           public void setUser(final String user) {
                                           }

                                       };

    public static List<HTTPProxy> getFromSystemProperties() {
        final java.util.List<HTTPProxy> ret = new ArrayList<HTTPProxy>();
        try {
            {
                /* try to parse http proxy from system properties */
                final String host = System.getProperties().getProperty("http.proxyHost");
                if (!StringUtils.isEmpty(host)) {
                    int port = 80;
                    final String ports = System.getProperty("http.proxyPort");
                    if (!StringUtils.isEmpty(ports)) {
                        port = Integer.parseInt(ports);
                    }
                    final HTTPProxy pr = new HTTPProxy(HTTPProxy.TYPE.HTTP, host, port);
                    final String user = System.getProperty("http.proxyUser");
                    final String pass = System.getProperty("http.proxyPassword");
                    if (!StringUtils.isEmpty(user)) {
                        pr.setUser(user);
                    }
                    if (!StringUtils.isEmpty(pass)) {
                        pr.setPass(pass);
                    }
                    ret.add(pr);
                }
            }
            {
                /* try to parse socks5 proxy from system properties */
                final String host = System.getProperties().getProperty("socksProxyHost");
                if (!StringUtils.isEmpty(host)) {
                    int port = 1080;
                    final String ports = System.getProperty("socksProxyPort");
                    if (!StringUtils.isEmpty(ports)) {
                        port = Integer.parseInt(ports);
                    }
                    final HTTPProxy pr = new HTTPProxy(HTTPProxy.TYPE.SOCKS5, host, port);
                    ret.add(pr);
                }
            }
        } catch (final Throwable e) {
            Log.exception(e);
        }
        return ret;
    }

    public static HTTPProxy getHTTPProxy(final HTTPProxyStorable storable) {
        if (storable == null || storable.getType() == null) { return null; }
        HTTPProxy ret = null;
        switch (storable.getType()) {
        case NONE:
            return HTTPProxy.NONE;
        case DIRECT:
            ret = new HTTPProxy(TYPE.DIRECT);
            if (storable.getAddress() != null) {
                try {
                    final InetAddress ip = InetAddress.getByName(storable.getAddress());
                    ret.setLocalIP(ip);
                } catch (final Throwable e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
            break;
        case HTTP:
            ret = new HTTPProxy(TYPE.HTTP);
            ret.setHost(storable.getAddress());
            break;
        case SOCKS4:
            ret = new HTTPProxy(TYPE.SOCKS4);
            ret.setHost(storable.getAddress());
        case SOCKS5:
            ret = new HTTPProxy(TYPE.SOCKS5);
            ret.setHost(storable.getAddress());
            break;
        }
        ret.setPreferNativeImplementation(storable.isPreferNativeImplementation());
        ret.setConnectMethodPrefered(storable.isConnectMethodPrefered());
        ret.setPass(storable.getPassword());
        ret.setUser(storable.getUsername());
        ret.setPort(storable.getPort());
        return ret;
    }

    private static String[] getInfo(final String host, final String port) {
        final String[] info = new String[2];
        if (host == null) { return info; }
        final String tmphost = host.replaceFirst("http://", "").replaceFirst("https://", "");
        String tmpport = new org.appwork.utils.Regex(host, ".*?:(\\d+)").getMatch(0);
        if (tmpport != null) {
            info[1] = "" + tmpport;
        } else {
            if (port != null) {
                tmpport = new Regex(port, "(\\d+)").getMatch(0);
            }
            if (tmpport != null) {
                info[1] = "" + tmpport;
            } else {
                Log.L.severe("No proxyport defined, using default 8080");
                info[1] = "8080";
            }
        }
        info[0] = new Regex(tmphost, "(.*?)(:|/|$)").getMatch(0);
        return info;
    }

    public static HTTPProxyStorable getStorable(final HTTPProxy proxy) {
        if (proxy == null || proxy.getType() == null) { return null; }
        final HTTPProxyStorable ret = new HTTPProxyStorable();
        switch (proxy.getType()) {
        case NONE:
            ret.setType(HTTPProxyStorable.TYPE.NONE);
            ret.setAddress(null);
            break;
        case DIRECT:
            ret.setType(HTTPProxyStorable.TYPE.DIRECT);
            if (proxy.getLocalIP() != null) {
                final String ip = proxy.getLocalIP().getHostAddress();
                ret.setAddress(ip);
            } else {
                ret.setAddress(null);
            }
            break;
        case HTTP:
            ret.setType(HTTPProxyStorable.TYPE.HTTP);
            ret.setAddress(proxy.getHost());
            break;
        case SOCKS4:
            ret.setType(HTTPProxyStorable.TYPE.SOCKS4);
            ret.setAddress(proxy.getHost());
            break;
        case SOCKS5:
            ret.setType(HTTPProxyStorable.TYPE.SOCKS5);
            ret.setAddress(proxy.getHost());
            break;
        }
        ret.setConnectMethodPrefered(proxy.isConnectMethodPrefered());
        ret.setPreferNativeImplementation(proxy.isPreferNativeImplementation());
        ret.setPort(proxy.getPort());
        ret.setPassword(proxy.getPass());
        ret.setUsername(proxy.getUser());
        return ret;
    }

    /**
     * Checks windows registry for proxy settings
     */
    public static List<HTTPProxy> getWindowsRegistryProxies() {

        final java.util.List<HTTPProxy> ret = new ArrayList<HTTPProxy>();
        try {
            final ProcessBuilder pb = ProcessBuilderFactory.create(new String[] { "reg", "query", "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings" });

            final Process process = pb.start();
            final String result = IO.readInputStreamToString(process.getInputStream());

            process.destroy();
            try {
                final String autoProxy = new Regex(result, "AutoConfigURL\\s+REG_SZ\\s+([^\r\n]+)").getMatch(0);

                if (!StringUtils.isEmpty(autoProxy)) {
                    Log.L.info("AutoProxy.pac Script found: " + autoProxy);
                }
            } catch (final Exception e) {

            }
            final String enabledString = new Regex(result, "ProxyEnable\\s+REG_DWORD\\s+(\\d+x\\d+)").getMatch(0);
            if ("0x0".equals(enabledString)) {
                // proxy disabled
                return ret;
            }
            final String val = new Regex(result, " ProxyServer\\s+REG_SZ\\s+([^\r\n]+)").getMatch(0);
            if (val != null) {
                for (final String vals : val.split(";")) {
                    if (vals.toLowerCase(Locale.ENGLISH).startsWith("ftp=")) {
                        continue;
                    }
                    if (vals.toLowerCase(Locale.ENGLISH).startsWith("https=")) {
                        continue;
                    }
                    /* parse ip */
                    String proxyurl = new Regex(vals, "(\\d+\\.\\d+\\.\\d+\\.\\d+)").getMatch(0);
                    if (proxyurl == null) {
                        /* parse domain name */
                        proxyurl = new Regex(vals, ".+=(.*?)($|:)").getMatch(0);
                        if (proxyurl == null) {
                            /* parse domain name */
                            proxyurl = new Regex(vals, "=?(.*?)($|:)").getMatch(0);
                        }
                    }
                    final String port = new Regex(vals, ":(\\d+)").getMatch(0);
                    if (proxyurl != null) {

                        if (vals.trim().contains("socks")) {
                            final int rPOrt = port != null ? Integer.parseInt(port) : 1080;
                            final HTTPProxy pd = new HTTPProxy(HTTPProxy.TYPE.SOCKS5);
                            pd.setHost(proxyurl);
                            pd.setPort(rPOrt);
                            ret.add(pd);
                        } else {
                            final int rPOrt = port != null ? Integer.parseInt(port) : 8080;
                            final HTTPProxy pd = new HTTPProxy(HTTPProxy.TYPE.HTTP);
                            pd.setHost(proxyurl);
                            pd.setPort(rPOrt);
                            ret.add(pd);
                        }
                    }
                }
            }
        } catch (final Throwable e) {
            Log.exception(e);
        }
        return ret;
    }

    public static HTTPProxy parseHTTPProxy(final String s) {
        if (StringUtils.isEmpty(s)) { return null; }
        final String type = new Regex(s, "(https?|socks(5|4)|direct)://").getMatch(0);
        final String auth = new Regex(s, "://(.+)@").getMatch(0);
        final String host = new Regex(s, "://(.+@)?(.*?)(/|$)").getMatch(1);
        HTTPProxy ret = null;
        if ("http".equalsIgnoreCase(type) || "https".equalsIgnoreCase(type)) {
            ret = new HTTPProxy(TYPE.HTTP);
            ret.setPort(8080);
        } else if ("socks5".equalsIgnoreCase(type)) {
            ret = new HTTPProxy(TYPE.SOCKS5);
            ret.setPort(1080);
        } else if ("socks4".equalsIgnoreCase(type)) {
            ret = new HTTPProxy(TYPE.SOCKS4);
            ret.setPort(1080);
        } else if ("direct".equalsIgnoreCase(type)) {
            ret = new HTTPProxy(TYPE.DIRECT);
            final String hostname = new Regex(host, "(.*?)(:|$)").getMatch(0);
            if (!StringUtils.isEmpty(hostname)) {
                try {
                    final InetAddress ip = InetAddress.getByName(hostname.trim());
                    ret.setLocalIP(ip);
                    return ret;
                } catch (final Throwable e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        if (ret != null) {
            final String hostname = new Regex(host, "(.*?)(:|$)").getMatch(0);
            final String port = new Regex(host, ".*?:(\\d+)").getMatch(0);
            if (!StringUtils.isEmpty(hostname)) {
                ret.setHost(hostname);
            }
            if (!StringUtils.isEmpty(port)) {
                ret.setPort(Integer.parseInt(port));
            }
            final String username = new Regex(auth, "(.*?)(:|$)").getMatch(0);
            final String password = new Regex(auth, ".*?:(.+)").getMatch(0);
            if (!StringUtils.isEmpty(username)) {
                ret.setUser(username);
            }
            if (!StringUtils.isEmpty(password)) {
                ret.setPass(password);
            }
            if (!StringUtils.isEmpty(ret.getHost())) { return ret; }
        }
        return null;
    }

    private static byte[] toCstr(final String str) {
        final byte[] result = new byte[str.length() + 1];
        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        result[str.length()] = 0;
        return result;
    }

    private InetAddress localIP                    = null;

    private String      user                       = null;

    private String      pass                       = null;

    private int         port                       = 80;

    protected String    host                       = null;
    private TYPE        type                       = TYPE.DIRECT;

    private boolean     useConnectMethod           = false;

    private boolean     preferNativeImplementation = false;

    protected HTTPProxy() {
    }

    public HTTPProxy(final HTTPProxy proxy) {
        this.set(proxy);
    }

    public HTTPProxy(final InetAddress direct) {
        this.setType(TYPE.DIRECT);
        this.setLocalIP(direct);
    }

    public HTTPProxy(final TYPE type) {
        this.setType(type);
    }

    public HTTPProxy(final TYPE type, final String host, final int port) {
        this.setPort(port);
        this.setType(type);
        this.setHost(HTTPProxy.getInfo(host, "" + port)[0]);
    }

    public String _toString() {
        if (this.type == TYPE.NONE) {
            return _AWU.T.proxy_none();
        } else if (this.type == TYPE.DIRECT) {
            return _AWU.T.proxy_direct(this.localIP.getHostAddress());
        } else if (this.type == TYPE.HTTP) {
            String ret = _AWU.T.proxy_http(this.getHost(), this.getPort());
            if (this.isPreferNativeImplementation()) {
                ret = ret + "(prefer native)";
            }
            return ret;
        } else if (this.type == TYPE.SOCKS5) {
            return _AWU.T.proxy_socks5(this.getHost(), this.getPort());
        } else if (this.type == TYPE.SOCKS4) {
            return _AWU.T.proxy_socks4(this.getHost(), this.getPort());
        } else {
            return "UNKNOWN";
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public HTTPProxy clone() {
        final HTTPProxy ret = new HTTPProxy();
        ret.cloneProxy(this);
        return ret;
    }

    protected void cloneProxy(final HTTPProxy proxy) {
        if (proxy == null) { return; }
        this.set(proxy);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) { return true; }
        if (obj == null || !(obj instanceof HTTPProxy)) { return false; }
        final HTTPProxy p = (HTTPProxy) obj;
        if (this.type != p.type) { return false; }
        switch (this.type) {
        case DIRECT:
            if (this.localIP == null && p.localIP == null) { return true; }
            if (this.localIP != null && this.localIP.equals(p.localIP)) { return true; }
            return false;
        default:
            return StringUtils.equals(this.host, p.host) && StringUtils.equals(StringUtils.isEmpty(this.user) ? null : this.user, StringUtils.isEmpty(p.user) ? null : p.user) && StringUtils.equals(StringUtils.isEmpty(this.pass) ? null : this.pass, StringUtils.isEmpty(p.pass) ? null : p.pass) && this.port == p.port;
        }
    }

    public String getHost() {
        return this.host;
    }

    /**
     * @return the localIP
     */
    public InetAddress getLocalIP() {
        return this.localIP;
    }

    public String getPass() {
        return this.pass;
    }

    public int getPort() {
        return this.port;
    }

    public TYPE getType() {
        return this.type;
    }

    public String getUser() {
        return this.user;
    }

    @Override
    public int hashCode() {
        return HTTPProxy.class.hashCode();
    }

    public boolean isConnectMethodPrefered() {
        return this.useConnectMethod;
    }

    /**
     * this proxy is DIRECT = using a local bound IP
     * 
     * @return
     */
    public boolean isDirect() {
        return this.type == TYPE.DIRECT;
    }

    public boolean isLocal() {
        return this.isDirect() || this.isNone();
    }

    /**
     * this proxy is NONE = uses default gateway
     * 
     * @return
     */
    public boolean isNone() {
        return this.type == TYPE.NONE;
    }

    /**
     * @return the preferNativeImplementation
     */
    public boolean isPreferNativeImplementation() {
        return this.preferNativeImplementation;
    }

    /**
     * this proxy is REMOTE = using http,socks proxy
     * 
     * @return
     */
    public boolean isRemote() {
        return !this.isDirect() && !this.isNone();
    }

    protected void set(final HTTPProxy proxy) {
        if (proxy == null) { return; }
        this.setUser(proxy.getUser());
        this.setHost(proxy.getHost());
        this.setLocalIP(proxy.getLocalIP());
        this.setPass(proxy.getPass());
        this.setPort(proxy.getPort());
        this.setType(proxy.getType());
        this.setConnectMethodPrefered(proxy.isConnectMethodPrefered());
        this.setPreferNativeImplementation(proxy.isPreferNativeImplementation());
    }

    public void setConnectMethodPrefered(final boolean value) {
        this.useConnectMethod = value;
    }

    public void setHost(String host) {
        if (host != null) {
            host = host.trim();
        }
        this.host = host;
    }

    /**
     * @param localIP
     *            the localIP to set
     */
    public void setLocalIP(final InetAddress localIP) {
        this.localIP = localIP;
    }

    public void setPass(final String pass) {
        this.pass = pass;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * @param preferNativeImplementation
     *            the preferNativeImplementation to set
     */
    public void setPreferNativeImplementation(final boolean preferNativeImplementation) {
        this.preferNativeImplementation = preferNativeImplementation;
    }

    public void setType(final TYPE type) {
        this.type = type;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return this._toString();
    }

}
