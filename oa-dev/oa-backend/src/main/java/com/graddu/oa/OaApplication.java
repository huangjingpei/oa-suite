package com.graddu.oa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 现代企业协同办公系统 (OA Suite) 启动入口
 * @author graddu
 */
@SpringBootApplication
@MapperScan("com.graddu.oa.**.mapper")
public class OaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaApplication.class, args);
        System.out.println("=================================================");
        System.out.println(">>> 现代协同办公平台 (OA Suite Backend) 启动成功!");
        System.out.println(">>> Swagger API 文档地址: http://localhost:8080/api/swagger-ui.html");
        System.out.println("=================================================");
    }
}
