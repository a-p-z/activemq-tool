# ActiveMQ Tool
**ActiveMQ Tool** is a Java based Desktop Application for monitoring and managing the behavior of an ActiveMQ broker via JMX MBeans.
### Features
ActiveMQ-Tool has the following features organized into tabs:

|           |                                                                                         |
| --------- | --------------------------------------------------------------------------------------- |
| Broker    | displays info such as broker name, version, uptime and usage                            |
| Queues    | menages following operations on queues:<br/>- order by name, pending messages, etc<br/>- browse, purge, delete<br/>- show/hide default and custom headers/body<br/>- order messages of a queue by field<br/>- search through messages of a queue in all visible fields<br/>- copy/move/delete multiple messages                                                    |
| Processes | create consumers and producers on the fly                                               |

![demo](/images/demo.gif?raw=true "Demo")

### How to build
```sh
$ mvn jfx:native
````
or
```sh
$ mvn jfx:jar
````
