package se.redfield.arxnode.config.pmodels;

import java.io.IOException;
import java.util.Collection;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.OrderedDistanceTCloseness;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.knime.core.node.NodeLogger;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.ui.pmodels.PrivacyModelEditor;
import se.redfield.arxnode.ui.pmodels.TClosenessEditor;

public class TClosenessConfig extends ColumnPrivacyModelConfig {
	private static final NodeLogger logger = NodeLogger.getLogger(TClosenessConfig.class);

	private double t;
	private String hierarchy;
	private TClosenessMeasure measure;

	public TClosenessConfig() {
		t = 0.001;
		hierarchy = "";
		measure = TClosenessMeasure.EQUAL;
	}

	public double getT() {
		return t;
	}

	public void setT(double t) {
		this.t = t;
	}

	public String getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}

	public TClosenessMeasure getMeasure() {
		return measure;
	}

	public void setMeasure(TClosenessMeasure measure) {
		this.measure = measure;
	}

	@Override
	public PrivacyModelEditor createEditor(Collection<ColumnConfig> columns) {
		return new TClosenessEditor(this, columns);
	}

	@Override
	public PrivacyCriterion createCriterion(Data data) {
		switch (measure) {
		case EQUAL:
			return new EqualDistanceTCloseness(getColumn(), t);
		case HIERARCHICAL:
			try {
				DataHandle handle = data.getHandle();
				return new HierarchicalDistanceTCloseness(getColumn(), t, HierarchyBuilder.create(hierarchy)
						.build(handle.getDistinctValues(handle.getColumnIndexOf(getColumn()))));
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		case ORDERED:
			return new OrderedDistanceTCloseness(getColumn(), t);
		}
		return null;
	}

	@Override
	public String toString() {
		return String.format("TCloseness for [%s] with t=%.3f and %s", getColumn(), t, measure);
	}

	public static enum TClosenessMeasure {
		EQUAL("EMD with equal ground-distance"), //
		HIERARCHICAL("EMD with hierarchical ground-distance"), //
		ORDERED("EMD with ordered ground-distance");
		private String title;

		private TClosenessMeasure(String title) {
			this.title = title;
		}

		@Override
		public String toString() {
			return title;
		}
	}
}
