package com.tengyue.im.spring;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;

/**
 * Created by bingbing on 2017/8/3.
 */
public class MyHttpMessageConverter extends MappingJackson2HttpMessageConverter{

    public MyHttpMessageConverter() {
        super();
    }

    public MyHttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void setJsonPrefix(String jsonPrefix) {
        super.setJsonPrefix(jsonPrefix);
    }

    @Override
    public void setPrefixJson(boolean prefixJson) {
        super.setPrefixJson(prefixJson);
    }

    @Override
    protected void writePrefix(JsonGenerator generator, Object object) throws IOException {
        super.writePrefix(generator, object);
    }

    @Override
    protected void writeSuffix(JsonGenerator generator, Object object) throws IOException {
        super.writeSuffix(generator, object);
    }
}
