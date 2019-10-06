/*
 * Copyright (c) 2019 Redfield AB.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *  
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package se.redfield.arxnode.config;

import java.util.List;
import java.util.stream.Collectors;

import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.MetricDescription;

public class MetricDescriptionWrap {

	private MetricDescription description;

	private MetricDescriptionWrap(MetricDescription description) {
		this.description = description;
	}

	public MetricDescription getDescription() {
		return description;
	}

	@Override
	public int hashCode() {
		return description.getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MetricDescriptionWrap) {
			return description.getName().equals(((MetricDescriptionWrap) obj).getDescription().getName());
		}
		return false;
	}

	@Override
	public String toString() {
		return description.getName();
	}

	public static List<MetricDescriptionWrap> list() {
		return Metric.list().stream().map(MetricDescriptionWrap::new).collect(Collectors.toList());
	}

	public static MetricDescriptionWrap fromString(String str) {
		return new MetricDescriptionWrap(findDescription(str));
	}

	private static MetricDescription findDescription(String str) {
		List<MetricDescription> descriptions = Metric.list();
		try {
			return descriptions.get(Integer.valueOf(str));
		} catch (Exception e) {
			// ignore
		}
		return descriptions.stream().filter(m -> m.getName().equals(str)).findFirst().orElse(descriptions.get(0));
	}
}
