#!/bin/bash
mvn clean
mvn install
cp target/xmpp-proxy-0.0.1-SNAPSHOT-jar-with-dependencies.jar xmpp-proxy.jar