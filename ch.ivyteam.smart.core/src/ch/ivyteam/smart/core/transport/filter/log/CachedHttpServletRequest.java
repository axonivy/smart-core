package ch.ivyteam.smart.core.transport.filter.log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import ch.ivyteam.log.Logger;

class CachedHttpServletRequest extends HttpServletRequestWrapper {

  private byte[] cachedPayload;

  public CachedHttpServletRequest(HttpServletRequest request) throws IOException {
    super(request);
    try (var in = request.getInputStream()) {
      this.cachedPayload = in.readAllBytes();
    }
  }

  @Override
  public ServletInputStream getInputStream() {
    return new CachedServletInputStream(this.cachedPayload);
  }

  @Override
  public BufferedReader getReader() {
    var in = new ByteArrayInputStream(this.cachedPayload);
    return new BufferedReader(new InputStreamReader(in));
  }

  String cachedContent() {
    return new String(this.cachedPayload, StandardCharsets.UTF_8);
  }

  private static class CachedServletInputStream extends ServletInputStream {

    private static final Logger LOGGER = Logger.getLogger(CachedServletInputStream.class);

    private final InputStream in;

    public CachedServletInputStream(byte[] cachedBody) {
      this.in = new ByteArrayInputStream(cachedBody);
    }

    @Override
    public boolean isFinished() {
      try {
        return in.available() == 0;
      } catch (IOException ex) {
        LOGGER.error(ex.getMessage());
      }
      return false;
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int read() throws IOException {
      return in.read();
    }
  }
}
