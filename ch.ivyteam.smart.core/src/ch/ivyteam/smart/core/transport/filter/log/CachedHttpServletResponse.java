package ch.ivyteam.smart.core.transport.filter.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

class CachedHttpServletResponse extends HttpServletResponseWrapper {

  private final ByteArrayOutputStream cachedPayload = new ByteArrayOutputStream();
  private final ServletOutputStream out;

  public CachedHttpServletResponse(HttpServletResponse response) throws IOException {
    super(response);
    out = new CachedServletOutputStream(cachedPayload, response.getOutputStream());
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return out;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    return new PrintWriter(out, true, StandardCharsets.UTF_8);
  }

  String cachedContent() {
    return cachedPayload.toString(StandardCharsets.UTF_8);
  }

  private static class CachedServletOutputStream extends ServletOutputStream {

    private final OutputStream cached;
    private final ServletOutputStream original;

    public CachedServletOutputStream(OutputStream cached, ServletOutputStream original) {
      this.cached = cached;
      this.original = original;
    }

    @Override
    public boolean isReady() {
      return original.isReady();
    }

    @Override
    public void setWriteListener(WriteListener listener) {
      original.setWriteListener(listener);
    }

    @Override
    public void write(int b) throws IOException {
      cached.write(b);
      original.write(b);
    }

    @Override
    public void flush() throws IOException {
      cached.flush();
      original.flush();
    }

    @Override
    public void close() throws IOException {
      cached.close();
      original.close();
    }
  }
}
