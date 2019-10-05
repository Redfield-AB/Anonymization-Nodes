package se.redfield.arxnode.nodes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

public class ArxPortObject extends AbstractPortObject {
	private static final NodeLogger logger = NodeLogger.getLogger(ArxPortObject.class);

	public static final class Serializer extends AbstractPortObjectSerializer<ArxPortObject> {
	}

	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(ArxPortObject.class);
	public static final PortType TYPE_OPTIONAL = PortTypeRegistry.getInstance().getPortType(ArxPortObject.class, true);

	private HashMap<String, HierarchyBuilder<?>> hierarchies;
	private ArxPortObjectSpec spec;

	public ArxPortObject() {

	}

	public ArxPortObject(HashMap<String, HierarchyBuilder<?>> hierarchies, ArxPortObjectSpec spec) {
		this.hierarchies = hierarchies;
		this.spec = spec;
	}

	public Map<String, HierarchyBuilder<?>> getHierarchies() {
		return hierarchies;
	}

	@Override
	public String getSummary() {
		return "summary";
	}

	@Override
	public ArxPortObjectSpec getSpec() {
		return spec;
	}

	@Override
	public JComponent[] getViews() {
		return new JComponent[] {};
	}

	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		out.putNextEntry(new ZipEntry("arx"));
		ObjectOutputStream objOut = new ObjectOutputStream(out);
		objOut.writeObject(hierarchies);
		out.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		in.getNextEntry();
		ObjectInputStream objIn = new ObjectInputStream(in);
		try {
			hierarchies = (HashMap<String, HierarchyBuilder<?>>) objIn.readObject();
			this.spec = (ArxPortObjectSpec) spec;

		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		} finally {
			in.close();
		}
	}

	public static ArxPortObject create(ArxPortObjectSpec spec, ArxPortObject src) {
		HashMap<String, HierarchyBuilder<?>> hierarchies = new HashMap<>();
		if (src != null) {
			hierarchies.putAll(src.getHierarchies());
		}
		return new ArxPortObject(hierarchies, spec);
	}
}
