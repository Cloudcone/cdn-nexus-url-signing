#!/usr/bin/env python

import md5
import base64
from optparse import OptionParser
import os

# Example: Generate url signed link for resource https://www.example.com/images/photo.png
# python UrlSigning.py -f path -r example.com -p images/photo.png -k abc123 -s http -e 1546300800 i- 1.3.2.2
def sign_url(format, cdn_resource_hostname, path, secret_key, scheme, expiry_timestamp, client_ip):
    """
    Generate URL signed CDN resource
    :param format: User can choose "path" to generate path-based token or "querystring". Default is "querystring"
    :param cdn_resource_hostname: The CDN resource (without scheme). e.g. "cdn.yourdomain.com". [compulsory]
    :param path: File path of the CDN resource as part of token key. e.g. "/files", "/files/file.html". Default is "/"
                 **note: for HLS, it is better to put path instead of .m3u8 file, so that all the chunk of the hls will be authenticated as well.
    :param secret_key: The URL signing key [compulsory]
    :param scheme: The scheme for CDN Resource URL. e.g. "http" or "https". Default is "http"
    :param expiry_timestamp: The expiration of the URL. This is in Unix timestamp format. [optional]
    :param client_ip: The IP that allow to access. [optional]
    :return: string URL with generated token.
    Example:
        Generate url signed link for resource https://www.example.com/images/photo.png, expires January 1, 2019 12:00:00 AM, Epoch timestamp: 1546300800
        python UrlSigning.py -r example.com -p images/photo.png -k 123abc -e 1546300800 -i 1.3.2.2 -s http -f path
        Querystring:
        http://example.com/images/photo.png?secure=YzdhMDRiMDU3ZWQxNmZhYmI2Y2M3NGVhODIzN2Q2OTc&expires=1546300800&ip=1.3.2.2
        Path-based token:
        http://example.com/secure=YzdhMDRiMDU3ZWQxNmZhYmI2Y2M3NGVhODIzN2Q2OTc&expires=1546300800&ip=1.3.2.2/images/photo.png
    """

    #  1. Setup Token Key
    #  1.1 Append leading slash if missing
    path = os.path.join("/", path) if path[0] != "/" else path

    # 1.2 Extract uri and path, ignore query string
    cdn_resource_hostname = cdn_resource_hostname.split("?")[0]
    path = path.split("?")[0]

    # 1.3 Formulate the token key
    token_key = expiry_timestamp + path + secret_key + client_ip
    token = ''.join(["secure=",
                     base64.encodestring(md5.md5(token_key).digest()).replace("+", "-").replace("/", "_").replace(
                         "=", "").split("\n")[0]])

    # 2. Setup Optional URL
    # 2.1 Append argument - expiry_timestamp
    expiry_timestamp = "&expires=" + expiry_timestamp if expiry_timestamp else ""

    # 2.2 Append argument - client_ip
    client_ip = "&ip=" + client_ip if client_ip else ""

    # 3. Setup URL based on format (compulsory)
    if format == "path":
        return scheme + "://" + cdn_resource_hostname + "/" + token + expiry_timestamp + client_ip + path

    return scheme + "://" + cdn_resource_hostname + path + "?" + token + expiry_timestamp + client_ip


def main():
    parser = OptionParser()
    parser.add_option("-f", "--format", dest="format", default="querystring")
    parser.add_option("-r", "--resource", dest="cdn_resource_hostname")
    parser.add_option("-e", "--expires", dest="expiry_timestamp", default="")
    parser.add_option("-p", "--path", dest="path", default="/")
    parser.add_option("-k", "--key", dest="secret_key", default="")
    parser.add_option("-i", "--ip", dest="client_ip", default="")
    parser.add_option("-s", "--scheme", dest="scheme", default="http")

    (option, _) = parser.parse_args()

    message= ""
    if not option.secret_key:
        message = "URL Signing Key not given. "

    if not option.cdn_resource_hostname:
        message = message + "Resource hostname not given. "

    if message:
        parser.error(message)

    print sign_url(format=option.format,
                   cdn_resource_hostname=option.cdn_resource_hostname,
                   path=option.path,
                   secret_key=option.secret_key,
                   scheme=option.scheme,
                   expiry_timestamp=option.expiry_timestamp,
                   client_ip=option.client_ip)


if __name__ == '__main__':
    main()
