<?php 

/**
 * Generate URL signed CDN resource
 * @param string $format                User can choose "path" to generate path-based token or "querystring".
 *                                      Default is "querystring".
 * @param string $scheme                The scheme for CDN Resource URL. Default is "http". e.g. "http" or "https".
 * @param string $cdnResourceHostname   The CDN resource hostname (without scheme). e.g. "cdn.yourdomain.com". [compulsory]
 * @param string $path                  File path of the CDN resource as part of token key. e.g. "/files", "/files/file.html".
 *                                      Default is "/".
 *                                      **note: for HLS, it is better to put path instead of .m3u8 file,
 *                                          so that all the chunk of the hls will be authenticated as well.
 * @param string $secretKey             The secret key as part of token key. [compulsory]
 * @param int $expiryTimestamp          UNIX timestamp format, specify how long the url signed link is accessible to the public.
 *                                      By default will be accessible forever. [optional]
 * @param string $clientIp              Client IP as part of token key. Can be retrieved from $_SERVER['REMOTE_ADDR'].
 *                                      By default the url signed link is not restricted to any IP. [optional]
 * @return string $urlString            URL with designated format to access the resource.
 *
 * Example:
 * Generate url signed link for resource https://www.example.com/images/photo.png, expires January 1, 2019 12:00:00 AM, Epoch timestamp: 1546300800
 * php UrlSigning.php -r example.com -p images/photo.png -k 123abc -e 1546300800 -i 1.3.2.2 -s http -f path
 *
 * Querystring:
 * http://example.com/images/photo.png?secure=YzdhMDRiMDU3ZWQxNmZhYmI2Y2M3NGVhODIzN2Q2OTc&expires=1546300800&ip=1.3.2.2
 * Path-based token:
 * http://example.com/secure=YzdhMDRiMDU3ZWQxNmZhYmI2Y2M3NGVhODIzN2Q2OTc&expires=1546300800&ip=1.3.2.2/images/photo.png
 */
function generateSignedUrl($format, $scheme, $cdnResourceHostname, $path, $secretKey, $expiryTimestamp, $clientIp) {

    if (empty($secretKey)) {
        $message ="URL Signing Key not given. ";
    }
    if (empty($cdnResourceHostname)) {
        $message = $message . "Resource hostname not given. ";
    }
    if (!empty($message)){
        exit($message);
    }

    // NOTE: We adhere to ngx_secure_link_module hashing strategy
    // Ref: http://nginx.org/en/docs/http/ngx_http_secure_link_module.html#secure_link
    $searchChars = array('+', '/', '=', '\n');
    $replaceChars = array('-', '_', '', '');

    // 1. Setup Token Key
    // 1.1 Append leading slash if missing
    if ($path[0] != '/') {
        $path = "/{$path}";
    }

    // 1.2 Extract uri and path, ignore query string
    if ($pos = strpos($cdnResourceHostname, '?')) {
        $cdnResourceHostname = substr($cdnResourceHostname, 0, $pos);
    }
    if ($pos = strpos($path, '?')) {
        $path = substr($path, 0, $pos);
    }

    // 1.3 Formulate the token key
    $tokenKey = $expiryTimestamp . $path . $secretKey . $clientIp;
    $token = "secure=" . str_replace($searchChars, $replaceChars, base64_encode(md5($tokenKey, TRUE)));

    # 2. Setup Optional URL
    # 2.1 Append argument - expiryTimestamp
    if (!empty($expiryTimestamp)){
        $expiryTimestamp = "&expires={$expiryTimestamp}";
    }

    // 2.2 Append argument - clientIp
    if (!empty($clientIp)) {
        $clientIp = "&ip={$clientIp}";
    }

    # 3. Setup URL based on format (compulsory)
    if ($format == "path") {
        $urlString = "{$scheme}://{$cdnResourceHostname}/{$token}{$expiryTimestamp}{$clientIp}{$path}";
    }
    else {
        $urlString = "{$scheme}://{$cdnResourceHostname}{$path}?{$token}{$expiryTimestamp}{$clientIp}";
    }
    return $urlString;
}

$options = getopt("f:r:p:k:s:e:i:");
$f = isset($options["f"]) ? $options["f"] : "querystring";
$r = isset($options["r"]) ? $options["r"] : null;
$p = isset($options["p"]) ? $options["p"] : "/";
$k = isset($options["k"]) ? $options["k"] : null;
$s = isset($options["s"]) ? $options["s"] : "http";
$e = isset($options["e"]) ? $options["e"] : null;
$i = isset($options["i"]) ? $options["i"] : null;

echo generateSignedUrl($f, $s, $r, $p, $k, $e, $i), PHP_EOL;
?>
