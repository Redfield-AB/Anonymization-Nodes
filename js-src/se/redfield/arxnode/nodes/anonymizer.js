/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/jQuery/jquery-3.3.1.js" />
/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/bootstrap/3_3_6/debug/js/bootstrap.js" />
/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/dataTables/1_10_11/bootstrap/datatables.js" />

se_redfield_arxnode_nodes_anonymizer = function () {

	class View {
		constructor(rep, val) {
			this.rep = rep;
			this.val = val;
			this.initPartitionsTabs()
		}

		initPartitionsTabs() {
			var tabsHeader = $('<ul class="nav nav-tabs"></ul>');
			var tabsContent = $('<div class="tab-content"></div>');

			$('body').append(tabsHeader);
			$('body').append(tabsContent);

			this.partitions = [];
			for (var i in this.rep.partitions) {
				this.partitions.push(new Partition(this, this.rep.partitions[i], i, tabsHeader, tabsContent));
			}
		}
	}

	class Partition {
		constructor(view, partition, index, tabsHeader, tabsContent) {
			console.log('partition.constructor', partition);
			this.view = view;
			this.index = index;
			this.processNodes(partition);

			var link = $(`<a class="nav-link" role="tab" href="#partiton-tab-${index}">${index}</a>`);
			link.on('click', e => {
				e.preventDefault();
				console.log(e);
				$(e.target).tab('show');
			});

			var header = $('<li class="nav-item"></li>')
				.append(link);
			tabsHeader.append(header);
			tabsContent.append(this.createTabContent());

			if (this.index == 0) {
				link.tab('show');
			}
		}

		processNodes(partition) {
			this.levels = partition.levels;
			this.nodes = {};

			let activeUid = this.uidForTransformation(this.view.val.selectedTransformations[this.index]);

			for (let i in this.levels) {
				let level = this.levels[i];
				for (let j in level) {
					let node = level[j];
					let uid = this.uidForTransformation(node.transformation);

					node.$uid = uid;
					node.$selected = (uid == activeUid);
					if(node.$selected) {
						this.selectedNode = node;
					}

					this.nodes[uid] = node;
				}
			}
		}

		uidForTransformation(transformation) {
			return `node-${this.index}-` + transformation.join('-');
		}

		createTabContent() {
			var content = $(`<div class="tab-pane" role="tabpanel" id="partiton-tab-${this.index}"></div>`);
			this.filter = new TransformationFilter(this, content);
			this.transformationsTable = new TransformationsTable(this, content);
			this.transformationsTable.reloadData();
			return content;
		}

		selectTransformation(node) {
			if(this.selectedNode) {
				this.selectedNode.$selected = false;
			}
			this.selectedNode = node;
			this.selectedNode.$selected = true;

			this.view.val.selectedTransformations[this.index] = node.transformation;
			this.transformationsTable.updateSelected(node);
		}

		filtersUpdated() {
			console.log('filtersUpdated', this.filter);
			this.transformationsTable.reloadData();
		}
	}

	class TransformationFilter {
		constructor(partition, container) {
			this.partition = partition;

			let modesFilter = $('<div class="btn-group" data-toggle="buttons" role="group"></div>');

			this.modes = [
				new AnonymityMode('Anonymous', ['ANONYMOUS', 'PROBABLY_ANONYMOUS'], true),
				new AnonymityMode('Not Anonymous', ['NOT_ANONYMOUS', 'PROBABLY_NOT_ANONYMOUS']),
				new AnonymityMode('Unknown', ['UNKNOWN'])
			];

			for (var i in this.modes) {
				modesFilter.append(this.createModeCheckbox(this.modes[i]));
			}

			container.append(
				$('<div class="form-group"></div>')
					.append($('<label>Modes: </label>'))
					.append(modesFilter)
			);
		}

		createModeCheckbox(mode) {
			let cb = $('<input type="checkbox" checked>');
			cb.prop('checked', mode.active);

			let thisRef = this;
			cb.change(function () {
				mode.active = this.checked;
				console.log('change', mode);
				thisRef.partition.filtersUpdated();
			})

			let active = mode.active ? 'active' : '';
			return $(`<label class="btn ${active}"></label>`)
				.append(cb)
				.append(mode.title);
		}

		isVisible(node) {
			if(node.$selected) {
				return true;
			}
			
			let matchMode = false;
			for (var i = 0; i < this.modes.length && !matchMode; i++) {
				let mode = this.modes[i];
				if(mode.active) {
					matchMode = this.modes[i].match(node.anonymity);
				}
			}
			return matchMode;
		}
	}

	class AnonymityMode {
		constructor(title, anonymities, active) {
			this.title = title;
			this.anonymities = anonymities;
			this.active = !!active;
		}

		match(anonymity) {
			for (var i in this.anonymities) {
				if (this.anonymities[i] == anonymity) {
					return true;
				}
			}
			return false;
		}
	}

	class TransformationsTable {
		constructor(partition, container) {
			this.partition = partition;

			var table = $('<table class="table table-bordered"></table>');
			var thead = $('<thead></thead>')
				.append($('<tr></tr>')
					.append('<th scope="col">Active</th>')
					.append('<th scope="col">Transformation</th>')
					.append('<th scope="col">Anonymity</th>')
					.append('<th scope="col">Min Score</th>')
					.append('<th scope="col">Max Score</th>')
				);
			table.append(thead);

			//let data = this.populateData();
			this.dt = table.DataTable({
				data: [],
				select: true,
				order: [[0, 'desc']],
				rowId: '$uid',
				paging: false,
				columns: [
					{
						data: '$selected',
						defaultContent: "",
						render: function (data, type, row) {
							if (type === 'display') {
								return data ? '<i class="glyphicon glyphicon-ok"></i>' : '';
							}
							return data;
						}
					},
					{ data: 'transformation' },
					{ 
						data: 'anonymity',
						render: function (data, type, row) {
							if (type == 'display') {
								let icon = 'glyphicon-question-sign';
								let color = '';
								if(data == 'ANONYMOUS' || data == 'PROBABLY_ANONYMOUS') {
									icon = 'glyphicon-ok-sign';
									color = 'text-success';
								}
								if(data == 'NOT_ANONYMOUS' || data == 'PROBABLY_NOT_ANONYMOUS') {
									icon = 'glyphicon-remove-sign';
									color = 'text-danger';
								}
								return `<span class="${color}"><i class="glyphicon ${icon}"></i>${data}</span>`
							}
							return data;
						}
					},
					{ data: 'minScore', render: renderScore },
					{ data: 'maxScore', render: renderScore },
				]
			});

			function renderScore(data, type, row) {
				if (type == 'display') {
					return data.value + '(' + (data.relative * 100).toFixed(2) + '%)';
				}
				return data.relative;
			}

			let thisRef = this;
			table.on('click', 'tr', function () {
				var node = thisRef.dt.row(this).data();
				console.log('data', node);
				if (node) {
					thisRef.partition.selectTransformation(node);
				}
			});

			container.append(table);
		}

		populateData() {
			let data = [];
			for (let key in this.partition.nodes) {
				let node = this.partition.nodes[key];
				if(this.partition.filter.isVisible(node)) {
					data.push(node);
				}
			}
			return data;
		}

		updateSelected(selected) {
			console.log('updateSelected');
			this.dt.rows().nodes().to$().removeClass('success');
			$(this.dt.row('#' + selected.$uid).node()).addClass('success');
			this.dt.rows().invalidate();
		}

		reloadData() {
			this.dt.clear();
			this.dt.rows.add(this.populateData());
			this.dt.draw();
			this.updateSelected(this.partition.selectedNode);
		}
	}

	var view;
	var anonymizer = {
		verson: "0.1.0"
	}
	anonymizer.name = "Transformation View (JS)";
	anonymizer.init = function (rep, val) {
		console.log('init', rep, val);
		console.log(JSON.stringify(rep));

		if (val.selectedTransformations) {
			view = new View(rep, val);
		} else {
			$('body').append('<h1>Please restart node</h1>');
		}
	}
	anonymizer.getComponentValue = function () {
		console.log('getComponentValue', view);
		return view ? view.val : null;
	}
	anonymizer.validate = function (arg) {
		console.log('validate', arg);
		return true;
	}
	anonymizer.setValidationError = function (message) {

	}

	return anonymizer;
}();