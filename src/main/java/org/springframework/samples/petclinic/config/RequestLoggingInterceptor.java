package org.springframework.samples.petclinic.config;

import java.util.Objects;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 요청마다 traceId(WEB이 보낸 X-Trace-Id, 없으면 새로 생성)와 requestId를 MDC·응답 헤더에 넣는다.
 */
@Component
class RequestLoggingInterceptor implements HandlerInterceptor {

	private static final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(RequestLoggingInterceptor.class));

	static final String TRACE_ID_HEADER = "X-Trace-Id";

	static final String REQUEST_ID_HEADER = "X-Request-Id";

	static final String MDC_TRACE_ID = "traceId";

	static final String MDC_REQUEST_ID = "requestId";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String traceId = request.getHeader(TRACE_ID_HEADER);
		if (traceId == null || traceId.isBlank()) {
			traceId = UUID.randomUUID().toString();
		}
		String requestId = UUID.randomUUID().toString();
		MDC.put(MDC_TRACE_ID, traceId);
		MDC.put(MDC_REQUEST_ID, requestId);
		response.setHeader(TRACE_ID_HEADER, traceId);
		response.setHeader(REQUEST_ID_HEADER, requestId);
		logger.debug("method={} path={}", request.getMethod(), request.getRequestURI());
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable Exception ex) {
		logger.debug("method={} path={} status={}", request.getMethod(), request.getRequestURI(),
				response.getStatus());
		MDC.remove(MDC_TRACE_ID);
		MDC.remove(MDC_REQUEST_ID);
	}

}
