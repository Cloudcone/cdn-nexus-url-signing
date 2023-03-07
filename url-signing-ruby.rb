#!/usr/bin/env ruby

 # Generate URL signed CDN resource
 # @param string format                 User can choose "path" to generate path-based token or "querystring".
 #                                      Default is "querystring".
 # @param string scheme                 The scheme for CDN Resource URL. e.g. "http" or "https". Default is "http".
 # @param string cdn_resource_hostname  The CDN resource hostname (without scheme). e.g. "cdn.yourdomain.com". [compulsory]
 # @param string path                   File path of the CDN resource as part of token key. e.g. "/files", "/files/file.html".
 #                                      Default is "/".
 #                                      **note: for HLS, it is better to put path instead of .m3u8 file,
 #                                          so that all the chunk of the hls will be authenticated as well.
 # @param string secret_key             The secret key as part of token key. [compulsory]
 # @param int expiry_timestamp          UNIX timestamp format, specify how long the url signed link is accessible to the public.
 #                                      By default will be accessible forever. [optional]
 # @param string client_ip              Client IP as part of token key. Can be retrieved from _SERVER['REMOTE_ADDR'].
 #                                      By default the url signed link is not restricted to any IP. [optional]
 # @return string URL                   Generated token URL with designated format to access the resource.
 #
 # Example:
 # Generate url signed link for resource https://www.example.com/images/photo.png, expires January 1, 2019 12:00:00 AM, Epoch timestamp: 1546300800
 # ruby UrlSigning.rb -r example.com -p images/photo.png -k 123abc -e 1546300800 -i 1.3.2.2 -s http -f path
 #
 # Querystring:
 # http://example.com/images/photo.png?secure=YzdhMDRiMDU3ZWQxNmZhYmI2Y2M3NGVhODIzN2Q2OTc&expires=1546300800&ip=1.3.2.2
 # Path-based token:
 # http://example.com/secure=YzdhMDRiMDU3ZWQxNmZhYmI2Y2M3NGVhODIzN2Q2OTc&expires=1546300800&ip=1.3.2.2/images/photo.png

require 'optparse'
require 'digest/md5'
require 'base64'

def url_sign(format, cdn_resource_hostname, path, secret_key, scheme, expiry_timestamp, client_ip)

  #  1. Setup Token Key
  #  1.1 Append leading slash if missing
  path = File.join("/", path) unless path[0] == "/"

  # 1.2 Extract uri and path, ignore query string arguments
  cdn_resource_hostname = cdn_resource_hostname.split("?")[0]
  path = path.split("?")[0]

  # 1.3 Formulate the token key
  token_key = expiry_timestamp + path + secret_key + client_ip
  token = "secure=" + Base64.encode64(Digest::MD5.digest(token_key)).gsub("+", "-").gsub("/","_").gsub("=","").split("\n")[0]

  # 2. Setup Optional URL
  # 2.1 Append argument - expiry_timestamp
  expiry_timestamp = "&expires=" + expiry_timestamp unless expiry_timestamp.empty?

  # 2.2 Append argument - client_ip
  client_ip = "&ip=" + client_ip unless client_ip.empty?

  # 3. Setup URL based on format (compulsory)
  if format == "path"
    return scheme + "://" + cdn_resource_hostname + "/" + token + expiry_timestamp + client_ip + path
  else
    return scheme + "://" + cdn_resource_hostname + path + "?" + token + expiry_timestamp + client_ip
  end

end

# Default value
options = {:format=>"querystring", :cdn_resource_hostname=>"", :expiry_timestamp=>"", :path=>"/", :secret_key=>"",
 :client_ip=>"", :scheme=>"http", }

parser = OptionParser.new do |opts|

    opts.on('-f', '--format format', 'Format, optional' ) do |format|
        options[:format] = format;
    end

    opts.on('-r', '--resource cdn_resource_hostname', 'Resource hostname') do |cdn_resource_hostname|
        options[:cdn_resource_hostname] = cdn_resource_hostname;
    end

    opts.on('-p', '--path path', 'path') do |path|
      options[:path] = path;
    end

    opts.on('-k', '--key secret_key', 'URL Signing Key') do |secret_key|
      options[:secret_key] = secret_key;
    end

    opts.on('-e', '--expires expiry_timestamp', 'Expires, optional') do |expiry_timestamp|
      options[:expiry_timestamp] = expiry_timestamp;
    end

    opts.on('-i', '--ip client_ip', 'IP, optional') do |client_ip|
      options[:client_ip] = client_ip;
    end

    opts.on('-s', '--scheme scheme', 'Scheme, http or https, default: http') do |scheme|
      options[:scheme] = scheme;
    end

end

parser.parse!

message =""

if options[:cdn_resource_hostname].empty?
    message = message + "Resource hostname not given. "
end
if options[:secret_key].empty?
    message = message + "URL Signing Key not given. "
end

if !message.empty?
    abort(message)
end

puts url_sign(options[:format], options[:cdn_resource_hostname], options[:path], options[:secret_key],
 options[:scheme], options[:expiry_timestamp], options[:client_ip])
