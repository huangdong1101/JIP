# Java Interfaces Parser

### 使用方法
* 编译源码
    ```
    mvn clean package
    ```
* 解析jar
    ```
    java -jar JIP.jar /xxx/xxx.jar
    ```
    注：“/xxx/xxx.jar”为含interface的jar文件
* 解析classpath
    ```
    java -jar JIP.jar /xxx/xxx/
    ```
    注：“/xxx/xxx/”为interface编译后class文件包路径
* 例：解析结果
    ```
    {
        "interfaces":[
            {
                "name":"com.xxxx.dubbo.face.GreetingsService",
                "methods":[
                    {
                        "name":"add",
                        "parameterTypes":[
                            "int",
                            "int"
                        ],
                        "returnType":"java.lang.Integer"
                    },
                    {
                        "name":"sayGoodBye",
                        "parameterTypes":[
                            "java.lang.String"
                        ],
                        "returnType":"java.lang.String"
                    },
                    {
                        "name":"hello",
                        "parameterTypes":[
    
                        ],
                        "returnType":"java.lang.String"
                    },
                    {
                        "name":"sayHi",
                        "parameterTypes":[
                            "java.lang.String",
                            "java.lang.String"
                        ],
                        "returnType":"java.lang.String"
                    }
                ]
            }
        ]
    }
    ```