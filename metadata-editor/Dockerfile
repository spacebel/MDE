FROM tomcat:8-alpine

# Maintainer
MAINTAINER Minh Nguyen Quang Spacebel

RUN rm -rf /usr/local/tomcat/webapps/docs
RUN rm -rf /usr/local/tomcat/webapps/host-manager
RUN rm -rf /usr/local/tomcat/webapps/manager
RUN rm -rf /usr/local/tomcat/webapps/examples

## Upload directory that is used for Primefaces FileUpload
RUN mkdir -p /editor/uploaded-temp

## Directory where temporary ZIP files (use to insert metadata records into the catalogue) will be stored
RUN mkdir -p /editor/zip-files-temp

## Directory where all configuration files are located
COPY config /config

# copy markdown editor to the autodeploy directory
COPY src/md-editor /usr/local/tomcat/webapps/md-editor

# copy the web app to the autodeploy directory
COPY target/metadata-editor.war /usr/local/tomcat/webapps/editor.war
