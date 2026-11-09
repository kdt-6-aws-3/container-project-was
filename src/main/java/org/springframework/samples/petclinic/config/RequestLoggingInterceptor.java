package org.springframework.samples.petclinic.config;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
class RequestLoggingInterceptor implements HandlerInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

	static final String TRACE_ID_HEADER = "X-Trace-Id";

	static final String REQUEST_ID_HEADER = "X-Request-Id";

	static final String MDC_TRACE_ID = "traceId";

	static final String MDC_REQUEST_ID = "requestId";

	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull Object handler) {
		String traceId = request.getHeader(TRACE_ID_HEADER);
		if (traceId == null || traceId.isBlank()) {
			traceId = UUID.randomUUID().toString();
		}
		String requestId = UUID.randomUUID().toString();

		MDC.put(MDC_TRACE_ID, traceId);
		MDC.put(MDC_REQUEST_ID, requestId);
		request.setAttribute(MDC_TRACE_ID, traceId);
		request.setAttribute(MDC_REQUEST_ID, requestId);

		response.setHeader(TRACE_ID_HEADER, traceId);
		response.setHeader(REQUEST_ID_HEADER, requestId);

		if (logger.isDebugEnabled()) {
			logger.debug("method={} path={} traceId={}", request.getMethod(), request.getRequestURI(), traceId);
		}
		return true;
	}

	@Override
	public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull Object handler, ModelAndView modelAndView) {
		if (logger.isDebugEnabled()) {
			logger.debug("method={} path={} status={}", request.getMethod(), request.getRequestURI(),
					response.getStatus());
		}
	}

	@Override
	public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull Object handler, Exception ex) {
		MDC.remove(MDC_TRACE_ID);
		MDC.remove(MDC_REQUEST_ID);
	}

}
