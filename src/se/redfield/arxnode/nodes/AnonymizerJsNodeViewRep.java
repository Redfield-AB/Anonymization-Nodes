package se.redfield.arxnode.nodes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.web.WebViewContent;

public class AnonymizerJsNodeViewRep implements WebViewContent {

	@Override
	public void loadFromStream(InputStream viewContentStream) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public OutputStream saveToStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveToNodeSettings(NodeSettingsWO settings) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub

	}

}
