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
