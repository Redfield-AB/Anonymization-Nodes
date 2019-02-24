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
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import se.redfield.arxnode.config.ColumnConfig;
import se.redfield.arxnode.config.Config;
import se.redfield.arxnode.ui.pmodels.PrivacyModelEditor;
import se.redfield.arxnode.ui.pmodels.TClosenessEditor;
import se.redfield.arxnode.util.TitledEnum;

public class TClosenessConfig extends ColumnPrivacyModelConfig {
	private static final NodeLogger logger = NodeLogger.getLogger(TClosenessConfig.class);

	public static final String CONFIG_T = "t";
	public static final String CONFIG_HIERACHY_FILE = "hierarchyFile";
	public static final String CONFIG_MEASURE = "measure";

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
	public PrivacyCriterion createCriterion(Data data, Config config) {
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
	public String getName() {
		return "t-Closeness";
	}

	@Override
	protected String getToStringPrefix() {
		return String.format("%.3f-Closeness (%s)", t, measure.suffix);
	}

	public static enum TClosenessMeasure implements TitledEnum {
		EQUAL("EMD with equal ground-distance", "equal ground-distance"), //
		HIERARCHICAL("EMD with hierarchical ground-distance", "hierarchical ground-distance"), //
		ORDERED("EMD with ordered distance", "ordered distance");
		private String title;
		private String suffix;

		private TClosenessMeasure(String title, String suffix) {
			this.title = title;
			this.suffix = suffix;
		}

		@Override
		public String toString() {
			return title;
		}

		@Override
		public String getTitle() {
			return title;
		}

		public static TClosenessMeasure fromString(String str) {
			return TitledEnum.fromString(values(), str, EQUAL);
		}

	}

	@Override
	public void save(NodeSettingsWO settings) {
		super.save(settings);
		settings.addDouble(CONFIG_T, t);
		settings.addString(CONFIG_HIERACHY_FILE, hierarchy);
		settings.addString(CONFIG_MEASURE, measure.getTitle());
	}

	@Override
	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		super.load(settings);
		t = settings.getDouble(CONFIG_T);
		hierarchy = settings.getString(CONFIG_HIERACHY_FILE);
		measure = TClosenessMeasure.fromString(settings.getString(CONFIG_MEASURE));
	}
}
