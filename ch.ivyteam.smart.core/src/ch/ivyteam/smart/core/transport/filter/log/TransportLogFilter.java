package ch.ivyteam.smart.core.transport.filter.log;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.ivyteam.log.Logger;

public class TransportLogFilter implements Filter {

  private static final Logger LOGGER = Logger.getLogger(TransportLogFilter.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (!LOGGER.isDebugEnabled() || !(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
      chain.doFilter(request, response);
      return;
    }

    var cachedRequest = new CachedHttpServletRequest(httpRequest);
    var cachedResponse = new CachedHttpServletResponse(httpResponse);
    logRequest(cachedRequest);
    chain.doFilter(cachedRequest, cachedResponse);
    logResponse(cachedResponse);
  }

  private static void logRequest(CachedHttpServletRequest request) {
    LOGGER.debug("""
      --- HTTP Request ---
      {0} {1}
      Headers: {2}
      Body: {3}""",
        request.getMethod(),
        request.getRequestURI(),
        headers(Collections.list(request.getHeaderNames()), request::getHeader),
        body(request.cachedContent()));
  }

  private static void logResponse(CachedHttpServletResponse response) {
    LOGGER.debug("""
      --- HTTP Response ---
      Status: {0}
      Headers: {1}
      Body: {2}""",
        response.getStatus(),
        headers(response.getHeaderNames(), response::getHeader),
        body(response.cachedContent()));
  }

  private static Map<String, String> headers(Collection<String> headerNames, Function<String, String> headerAccessor) {
    return headerNames.stream().collect(Collectors.toMap(Function.identity(), headerAccessor));
  }

  private static String body(String content) {
    return content.isEmpty() ? "<empty>" : content;
  }
}
