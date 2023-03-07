/*
 * This script is used to generate a signed urlString
 *
 * @param string format                 User can choose "path" to generate path-based token or "querystring".
 *                                      Default is "querystring".
 * @param string scheme                 The scheme for CDN Resource URL. e.g. "http" or "https". Default is "http".
 * @param string cdnResourceHostname    The CDN resource hostname (without scheme). e.g. "cdn.yourdomain.com". [compulsory]
 * @param string path                   File path of the CDN resource as part of token key. e.g. "/files", "/files/file.html"
 *                                      Default is "/".
 *                                      **note: for HLS, it is better to put path instead of .m3u8 file,
 *                                          so that all the chunk of the hls will be authenticated as well.
 * @param string secretKey              The secret key as part of token key. [compulsory]
 * @param int expiryTimestamp           UNIX timestamp format, specify how long the url signed link is accessible to the public.
 *                                      By default will be accessible forever. [optional]
 * @param string clientIp               Client IP as part of token key. Can be retrieved from _SERVER['REMOTE_ADDR'].
 *                                      By default the url signed link is not restricted to any IP. [optional]
 * @return string urlString             URL with designated format to access the resource.
 *
 * How to use this script:
 * - Place this script "UrlSigning.java" inside a directory
 * - Open terminal or command prompt
 * - cd to the directory
 * - Execute this command in terminal 'javac UrlSigning.java'
 * - A file naming 'UrlSigning.class' is created
 * - run this command to execute the java program :
 *   'java UrlSigning -s <schema> -r <cdnResourceHostname> -p <path> -k <secretKey> -e <expiryTimestamp> -i <clientIp> -f <format>'
 *   or
 *   'java UrlSigning -r <cdnResourceHostname> -p <path> -k <secretKey>'
 *
 * Example:
 * Generate url signed link for resource https://www.example.com/images/photo.png, expires January 1, 2019 12:00:00 AM, Epoch timestamp: 1546300800
 * java UrlSigning -r example.com -p images/photo.png -k 123abc -e 1546300800 -i 1.3.2.2 -s http -f path
 *
 * Querystring:
 * http://example.com/images/photo.png?secure=YzdhMDRiMDU3ZWQxNmZhYmI2Y2M3NGVhODIzN2Q2OTc&expires=1546300800&ip=1.3.2.2
 * Path-based token:
 * http://example.com/secure=YzdhMDRiMDU3ZWQxNmZhYmI2Y2M3NGVhODIzN2Q2OTc&expires=1546300800&ip=1.3.2.2/images/photo.png
 */

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.math.*;
import javax.xml.bind.DatatypeConverter;

public class UrlSigning {

    public static void main(String[] args) throws NoSuchAlgorithmException {

        String format = "querystring";
        String schema = "http";
        String cdnResourceHostname = "";
        String path = "/";
        String secretKey = "";
        String expiryTimestamp = "";
        String clientIp = "";

        for (int i=0; i < args.length; i++) {

            switch (args[i]) {
                case "-f":
                    format = args[i+1];
                    break;
                case "-s":
                    schema = args[i+1];
                    break;
                case "-r":
                    cdnResourceHostname = args[i+1];
                    break;
                case "-p":
                    path = args[i+1];
                    break;
                case "-k":
                    secretKey = args[i+1];
                    break;
                case "-e":
                    expiryTimestamp = args[i+1];
                    break;
                case "-i":
                    clientIp = args[i+1];
            }
        }

        System.out.print(generateSignedUrl(format, schema, cdnResourceHostname, path, secretKey, expiryTimestamp, clientIp));
    }

    public static String generateSignedUrl(String format, String schema, String cdnResourceHostname, String path, String secretKey, String expiryTimestamp, String clientIp) throws NoSuchAlgorithmException {

        String message = "";

        if (secretKey.isEmpty()) {
            message = "URL Signing Key not given. ";
        }
        if (cdnResourceHostname.isEmpty()) {
            message =  message + "Resource hostname not given. ";
        }
        if (!message.isEmpty()) {
            return message;
        }

        // 1. Setup Token Key
        // 1.1 Append leading slash if missing
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // 1.2 Extract uri and path, ignore query string
        cdnResourceHostname = cdnResourceHostname.split("\\?")[0];
        path = path.split("\\?")[0];

        // 1.3 Formulate the token key
        String tokenKey = expiryTimestamp + path + secretKey + clientIp;

        // 1.3.1 Create MD5 Hash for the tokenKey
        MessageDigest md = MessageDigest.getInstance("MD5");

        // 1.3.2 Base64 encode of the tokenKey Hash
        final byte[] tokenKeyBytes = md.digest(tokenKey.getBytes(StandardCharsets.UTF_8));
        String token = DatatypeConverter.printBase64Binary(tokenKeyBytes);
        token = "secure=" + token.replace("+", "-").replace("/", "_").replace("=", "").replace("\n", "");

        // 2. Setup Optional URL
        // 2.1 Append argument - expiryTimestamp
        if (!expiryTimestamp.isEmpty()){
            expiryTimestamp = "&expires=" + expiryTimestamp;
        }
        // 2.2 Append argument - clientIp
        if (!clientIp.isEmpty()) {
            clientIp = "&ip=" + clientIp;
        }

        // 3. Setup URL based on format (compulsory)
        String urlString;
        if (format.equals("path")) {
            urlString = schema + "://" + cdnResourceHostname + "/" + token + expiryTimestamp + clientIp + path ;
        } else {
            urlString = schema + "://" + cdnResourceHostname + path + "?" + token + expiryTimestamp + clientIp;
        }

        return urlString;
    }
}
