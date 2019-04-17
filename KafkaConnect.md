# Kafka Connect

### 啟動Kafka Connect
1. connect啟動有分standalone以及distributed兩種模式，一般都會建議用後者進行配置(這邊也以distributed作範例)，首先準備一個配置檔(kafka-connect.properties)，內容如下:

        bootstrap.servers=192.168.56.21:9092,192.168.56.22:9092,192.168.56.23:9092
        group.id=KafkaConnectGroup1
        offset.storage.topic=center-db-offset
        config.storage.topic=center-db-config
        status.storage.topic=center-db-status
        key.converter=org.apache.kafka.connect.json.JsonConverter
        value.converter=org.apache.kafka.connect.json.JsonConverter
將檔案放在${KAFKA_HOME}/config/kafka-connect.properties

2. 在 `${KAFKA_HOME}/bin/` 找到 `connect-distributed.sh` ，執行下列指令就可以啟動KafkaConnect

        ${KAFKA_HOME}/bin/connect-distributed.sh ${KAFKA_HOME}/config/kafka-connect.properties

### 加入plugins的方法
1. 將plugin的jar檔整理在 `${KAFKA_HOME}/plugins` 中，並將kafka-connect.properties配置中增加plugins的路徑，然後重啟KafkaConnect

        plugin.path=${KAFKA_HOME}/plugins

2. 透過下方的REST API將plugin新增到Kafka Connect中

### Connect’s REST API
可以透過kafka connect的REST API來完成加入connectors
預設的KafkaConnect URL是 http://localhost:8083
1. 查看使用中的connectors清單

        GET /connectors

2. 查看特定的connector資訊

        GET /connectors/{name}
3. 建立新的connector，request body必須是JSON，且必須包含兩個成員name以及config

        POST /connectors
    參考的request body(以debezium為例)

        {
            "name": "dbz",
            "config": {
                "connector.class": "io.debezium.connector.mysql.MySqlConnector",
                "tasks.max": "1",
                "database.hostname": "192.168.56.21",
                "database.port": "3306",
                "database.user": "user",
                "database.password": "pwd",
                "database.server.id": "6666",
                "database.server.name": "ThisIsTopicOfKafka",
                "database.history.kafka.topic": "center-db-history",
                "database.history.kafka.bootstrap.servers": "192.168.56.21:9092,192.168.56.22:9092,192.168.56.23:9092",
                "database.history.skip.unparseable.ddl": "true",
                "database.serverTimezone": "UTC",
                "include.schema.changes": "false",
                "poll.interval.ms": "10"
            }
        }
4. 取得某個connector的當前狀態，包含正在執行、暫停或是停止資訊，以及所有tasks的狀態

        GET /connectors/{name}/status
5. 刪除connector以及相關配置

        DELETE /connectors/{name}
6. 列出kafka connect所有安裝的plugins

        GET /connector-plugins
