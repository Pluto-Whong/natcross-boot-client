#!/bin/bash

BASE_DIR=`dirname $(readlink -f $0)`

## 日志配置，logback日志引擎
export LOG_CONFIG_PATH=classpath:config/logback-spring.xml
export LOG_DIR=${BASE_DIR}/logs
export LOG_LEVEL=info

## 服务端放置的IP地址
export NATCROSS_CLIENT_SERVER_IP='127.0.0.1'
## 客户端服务端口，即内网穿透客户端交互的主要服务端口
export NATCROSS_CLIENT_SERVICE_PORT=10010

## 服务端web服务地址，和服务端放置的IP:${SERVER_PORT}相同，且IP和NATCROSS_CLIENT_SERVER_IP相同
export NATCROSS_HTTP_SERVER='http://${NATCROSS_CLIENT_SERVER_IP}:10020'

## 服务端的签名key，散列算法，约等于对称密钥，客户端使用签名方式获取接口状态
export NATCROSS_HTTP_SIGN_KEY=serverSignKey

## 交互密钥和签名key
export NATCROSS_AES_KEY='1qV/BYx14agUrPFyw4ow+Q=='
export NATCROSS_TOKEN_KEY=tokenKey

JAVA_OPTS="-XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=20"
nohup java ${JAVA_OPTS} -jar ./natcross-boot-client.jar 2>&1 > /dev/null &

APPID=$!
echo $APPID > ./app.pid
