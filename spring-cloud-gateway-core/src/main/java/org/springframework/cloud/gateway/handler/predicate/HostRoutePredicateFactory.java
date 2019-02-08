/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gateway.handler.predicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author Spencer Gibb
 */
public class HostRoutePredicateFactory extends AbstractRoutePredicateFactory<HostRoutePredicateFactory.Config> {

	private PathMatcher pathMatcher = new AntPathMatcher(".");

	public HostRoutePredicateFactory() {
		super(Config.class);
	}

	public void setPathMatcher(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher;
	}

	@Override
	public List<String> shortcutFieldOrder() {
		return Collections.singletonList("patterns");
	}

	@Override
	public ShortcutType shortcutType() {
		return ShortcutType.GATHER_LIST;
	}

	@Override
	public Predicate<ServerWebExchange> apply(Config config) {
		return exchange -> {
			String host = exchange.getRequest().getHeaders().getFirst("Host");
			Optional<String> optionalPattern = config.getPatterns().stream()
					.filter(pattern -> this.pathMatcher.match(pattern, host))
					.findFirst();

			if (optionalPattern.isPresent()) {
				Map<String, String> variables = this.pathMatcher
						.extractUriTemplateVariables(optionalPattern.get(), host);
				ServerWebExchangeUtils.putUriTemplateVariables(exchange, variables);
				return true;
			}

			return false;
		};
	}

	@Validated
	public static class Config {
		private List<String> patterns = new ArrayList<>();

		@Deprecated
		public String getPattern() {
			if (!CollectionUtils.isEmpty(this.patterns)) {
				return patterns.get(0);
			}
			return null;
		}

		@Deprecated
		public Config setPattern(String pattern) {
			this.patterns = new ArrayList<>();
			this.patterns.add(pattern);
			return this;
		}

		public List<String> getPatterns() {
			return patterns;
		}

		public void setPatterns(List<String> patterns) {
			this.patterns = patterns;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this)
					.append("patterns", patterns)
					.toString();
		}
	}
}
