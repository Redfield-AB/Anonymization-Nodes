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
package se.redfield.arxnode.nodes;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.interactive.InteractiveNodeFactoryExtension;

import se.redfield.arxnode.nodes.AnonymizerNodeView.AnonymizerNodeViewValue;

public class AnonymizerNodeFactory extends NodeFactory<AnonymizerNodeModel> implements
		InteractiveNodeFactoryExtension<AnonymizerNodeModel, AnonymizerNodeViewValue, AnonymizerNodeViewValue> {

	@Override
	public AnonymizerNodeModel createNodeModel() {
		return new AnonymizerNodeModel();
	}

	@Override
	public int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<AnonymizerNodeModel> createNodeView(final int viewIndex, final AnonymizerNodeModel nodeModel) {
		return null;
	}

	@Override
	public boolean hasDialog() {
		return true;
	}

	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new AnonymizerNodeDialog();
	}

	@SuppressWarnings("unchecked")
	@Override
	public AnonymizerNodeView createInteractiveView(AnonymizerNodeModel model) {
		return new AnonymizerNodeView(model);
	}

}
