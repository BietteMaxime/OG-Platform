/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.AnalyticsNodeJsonWriter;
import com.opengamma.web.analytics.GridColumnsJsonWriter;
import com.opengamma.web.analytics.PortfolioGridStructure;

/**
 * Writes an instance of {@link PortfolioGridStructure} to JSON.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class PortfolioGridStructureMessageBodyWriter implements MessageBodyWriter<PortfolioGridStructure> {

  private final GridColumnsJsonWriter _writer;

  public PortfolioGridStructureMessageBodyWriter(GridColumnsJsonWriter writer) {
    ArgumentChecker.notNull(writer, "writer");
    _writer = writer;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return PortfolioGridStructure.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(PortfolioGridStructure gridStructure,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType) {
    // TODO this means unknown size. is it worth encoding it twice to find out the size?
    return -1;
  }

  @Override
  public void writeTo(PortfolioGridStructure gridStructure,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders,
                      OutputStream entityStream) throws IOException, WebApplicationException {
    String rootNodeJson = AnalyticsNodeJsonWriter.getJson(gridStructure.getRootNode());
    entityStream.write(("{\"columnSets\":" + columnsJson(gridStructure) + "," +
        "\"rootNode\":" + rootNodeJson + "}").getBytes());
  }

  private String columnsJson(PortfolioGridStructure gridStructure) {
    return _writer.getJson(gridStructure.getColumnStructure().getGroups());
  }
}
