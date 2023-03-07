# CDN URL Signing Example Codes
Example code to sign URLs with the URL signing setting enabled on the CDN Nexus

URL Signing protects your CDN endpoint content from unauthorized access, the following guide will take you through the parameters which you can control via URL Signing

### Signing options and URL Formats

A URL can be signed with any of these parameters

* _Query string format:_ http://example.com/filename?secure=FjnSIKndiNXInxkl&expires=1747605200&ip=127.0.0.1
* _Path format:_ http://example.com/secure=FjnSIKndiNXInxkl&expires=1747605200&ip=127.0.0.1/filename

| Parameter | Description                 | Type                 | Importance |
|-----------|-----------------------------|----------------------|------------|
| expires   | Expiration time of the URL  | UNIX Timestamp       | Optional   |
| filename      | Path of a file of directory | String               | Optional   |
| secure    | Generated public token      | String               | Required   |
| ip        | Allowed IP address          | IPv4 or IPv6 address | Optional   |

The following code examples are available

* Ruby (url-signing-ruby.rb)
* Python (url-signing-python.py)
* Python3 (url-signing-python3.py)
* PHP (url-signing.php)
* Java (url-signing.java)

```
Ruby: 
ruby url-signing-ruby.rb -f path -s https -r example.com -p images/photo.png -k abc123 -e 1546300800 -i 1.2.3.4 

Python: 
python url-signing-python.py -f path -s https -r example.com -p images/photo.png -k abc123 -e 1546300800 -i 1.2.3.4 
python3 url-signing-python3.py -f path -s https -r example.com -p images/photo.png -k abc123 -e 1546300800 -i 1.2.3.4

PHP: 
php url-signing.php -f path -s https -r example.com -p images/photo.png -k abc123s -e 1546300800 -i 1.2.3.4

Java:
javac url-signing.java
java UrlSigning -f path -s https -r example.com -p images/photo.png -k abc123 -e 1546300800 -i 1.2.3.4
```

```
Available options:
-f: format, path or querystring, default = querystring
-s: scheme for resource URL, http or https, default = http
-r: resource hostname (compulsory)
-p: file path of the resource, default = /
-k: URL signing key (compulsory)
-e: expiration of the URL (optional)
-i: IP that allow to access (optional)
```

Code examples provided by Virtuozzo
