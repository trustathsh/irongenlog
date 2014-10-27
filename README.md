irongenlog
=======

irongenlog is a *highly experimental* integration of diverse Logfile
Systems into a MAP-Infrastructure. The integration
aims to share security related informations, given by many logfile systems
with other network components in the [TNC architecture] [1]
via IF-MAP.

irongenlog consists of one element:

* The "publisher" - simply fetches the latest informations provided by
  an Logfile and converts the informations in it into IF-MAP metadata that finally will
  be published into a MAP server.
  
  Irongenlog will update the Metadata informations every time the Logsystem "logstash"
  (or any other Logsystem that delivers json messages over a Websocket) will call the 
  irongenlog WebSocket.
  In other words this means that irongenlog always tries to reflect the current/latest
  knowledge of an Logfile provided by logstash in a MAP server.
  
  The metadata that irongenlog publishs depends on the mapping you define by your self with 
  the javacc irongenlog programming language. Every logfile that can be read by logstash 
  could be used to publish. How it works you can read in the configuration section.


The binary package (`irongenlog-x.x.x-bundle.zip`) of irongenlog
is ready to run, all you need is to configure it to your needs.
If you like to build irongenlog by your own you can use the
latest code from the [GitHub repository][githubrepo].


Requirements
============
To use the binary package of irongenlog you need the following components:

* OpenJDK Version 1.6 or higher
* Logstash 1.4.2 [2] environment (message queue like redis required)
* MAP server implementation (e.g. [irond] [3])
* optionally ironGui to see whats going on

If you have downloaded the source code and want to build irongenlog by
yourself Maven 3 is also needed.


Configuration
=============
To setup the binary package you need to import the Irongenlog and MAP server
certificates into `irongenlog.jks`.
If you want to use irongenlog with irond the keystores of both are configured 
with ready-to-use testing certificates.

The remaining configuration parameters can be done through the
`irongenlog.properties` file in the irongenlog package.
In general you have to specify:

* the logstash server websocket URL,
* the MAPS URL and credentials.

Have a look at the comments in `irongenlog.properties`

Secondly you have to setup logstash:

* define the server config
* define the shipper config
* define system rights for logstash to access the log files
* install message queue server like redis

For example here are the config files to log dnsmask dhcp events:

server config:

	input {
		redis {
			host => "192.168.0.104"
			type => "redis"
			data_type => "list"
			key => "logstash"
		}
	}

	output {
		websocket {
			codec => "json"
			port => 3232
		}
	}

shipper config:

	input {
		file {
			type => "syslog"
			path => ["/var/log/syslog"]
			exclude => ["*.gz", "shipper.log"]
			sincedb_path => "/path/logstash/.sincedb"
		}
	}

	filter {

		grok {
			add_tag => "grepped"
			add_field => [ "strategy", "dnsmasq-dhcp" ]
			match => [
					"message", "%{SYSLOGTIMESTAMP:DATETIME} %{HOST:DHCPSERVERNAME} dnsmasq-dhcp\[%{POSINT:pid}\]: %{WORD:METHOD}\(%{WORD:INTERFACE}\) %{MAC:MAC}",
					"message", "%{SYSLOGTIMESTAMP:DATETIME} %{HOST:DHCPSERVERNAME} dnsmasq-dhcp\[%{POSINT:pid}\]: %{WORD:METHOD}\(%{WORD:INTERFACE}\) %{IP:IP} %{MAC:MAC}",
					"message", "%{SYSLOGTIMESTAMP:DATETIME} %{HOST:DHCPSERVERNAME} dnsmasq-dhcp\[%{POSINT:pid}\]: %{WORD:METHOD}\(%{WORD:INTERFACE}\) %{IP:IP} %{MAC:MAC} %{HOST:CLIENTNAME}"
				 ]
	  	}

		if ! ("grepped" in [tags]) {
	    		drop{}
		}
	  
	}

	output {

		redis {
			host => "192.168.0.104"
			data_type => "list"
			key => "logstash"
		}
	}


Building
========
You can build irongenlog by executing:

	$ mvn package

in the root directory of the irongenlog project.
Maven should download all further needed dependencies for you. After a successful
build you should find the `irongenlog-x.x.x-bundle.zip` in the `target` sub-directory.


Running
=======
To run the binary package of irongenlog simply execute:

	$ ./start.sh


Feedback
========
If you have any questions, problems or comments, please contact
	<trust@f4-i.fh-hannover.de>


LICENSE
=======
ironflgenlog is licensed under the [Apache License, Version 2.0] [4].


Note
====

ironfenlog is an experimental prototype and is not suitable for actual use.

Feel free to fork/contribute.


[1]: http://www.trustedcomputinggroup.org/developers/trusted_network_connect
[2]: http://logstash.net/docs/1.4.2/
[3]: https://github.com/trustathsh/irond
[4]: http://www.apache.org/licenses/LICENSE-2.0.html
[githubrepo]: https://github.com/trustathsh/irongenlog
