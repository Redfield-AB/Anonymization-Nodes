/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/jQuery/jquery-3.3.1.js" />
/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/bootstrap/3_3_6/debug/js/bootstrap.js" />
/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/dataTables/1_10_11/bootstrap/datatables.js" />

se_redfield_arxnode_nodes_anonymizer = function () {

	class View {
		constructor(rep, val) {
			this.rep = rep;
			this.val = val;

			this.container = $('<div clss="row"></div>');
			$('body').append($('<div class="container-fluid"></div>').append(this.container));

			this.filter = new TransformationFilter(this, this.container);
			this.initPartitionsTabs()
		}

		initPartitionsTabs() {
			var tabsHeader = $('<ul class="nav nav-tabs"></ul>');
			var tabsContent = $('<div class="tab-content"></div>');

			this.container.append(tabsHeader);
			this.container.append(tabsContent);

			this.partitions = [];
			for (var i in this.rep.partitions) {
				this.partitions.push(new Partition(this, this.rep.partitions[i], i, tabsHeader, tabsContent));
			}

			if (this.partitions.length <= 1) {
				tabsHeader.addClass('hide');
			}
		}

		filtersUpdated() {
			this.partitions.forEach(p => p.filtersUpdated());
		}
	}

	class Partition {
		constructor(view, partition, index, tabsHeader, tabsContent) {
			this.view = view;
			this.index = index;
			this.processNodes(partition);

			var link = $(`<a class="nav-link" role="tab" href="#partiton-tab-${index}">${index}</a>`);
			link.on('click', e => {
				e.preventDefault();
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
					if (node.$selected) {
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
			this.transformationsTable = new TransformationsTable(this, content);
			this.transformationsTable.reloadData();
			return content;
		}

		selectTransformation(node) {
			if (this.selectedNode) {
				this.selectedNode.$selected = false;
			}
			this.selectedNode = node;
			this.selectedNode.$selected = true;

			this.view.val.selectedTransformations[this.index] = node.transformation;
			this.transformationsTable.updateSelected(node);
		}

		filtersUpdated() {
			this.transformationsTable.reloadData();
		}
	}

	class TransformationFilter {
		constructor(view, container) {
			this.view = view;

			var panel = $('<div class="panel panel-default"></div>');
			container.append(panel);

			var panelBody = $('<div class="panel-body"></div>');
			var form = $('<div class="form-inline"></div>');
			panelBody.append(form);

			panel.append(
				$('<div class="panel-heading" style="cursor:pointer" data-toggle="collapse" data-target="#filtersCollapsible"></div>').append(
					$('<h5>Filters</h5>')
				),
				$('<div id="filtersCollapsible" class="panel-collapse collapse"></div>').append(
					panelBody
				)
			);
			
			this.scoreFilter = new ScoreFilter(this, form);
			this.anonymityFilter = new AnonymityFilter(this, form);
			this.levelsTable = new TransformationLevelsFilter(this, panelBody);
		}

		filtersUpdated() {
			this.view.filtersUpdated();
		}

		isVisible(node) {
			if (node.$selected) {
				return true;
			}

			return this.anonymityFilter.match(node) 
				&& this.scoreFilter.match(node)
				&& this.levelsTable.match(node);
		}
	}

	class AnonymityFilter {
		constructor(filter, container) {
			this.filter = filter;
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
				$('<div class="form-group"></div>').append(
					'<label>Modes: </label>',
					modesFilter
				)
			);
		}

		createModeCheckbox(mode) {
			let cb = $('<input type="checkbox" checked>');
			cb.prop('checked', mode.active);

			let thisRef = this;
			cb.change(function () {
				mode.active = this.checked;
				thisRef.filter.filtersUpdated();
			})

			let active = mode.active ? 'active' : '';
			return $(`<label class="btn ${active}"></label>`)
				.append(cb)
				.append(mode.title);
		}

		match(node) {
			let matchMode = false;
			for (var i = 0; i < this.modes.length && !matchMode; i++) {
				let mode = this.modes[i];
				if (mode.active) {
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

	class ScoreFilter {
		constructor(filter, container) {
			this.filter = filter;
			this.minScore = 0;
			this.maxScore = 100;

			var minInput = this.createInput('min-score-input');
			var maxInput = this.createInput('max-score-input');

			container.append(
				$('<div class="form-group"></div>').append(
					$('<div class="input-group" style="margin-right:10px"></div>').append(
						'<span class="input-group-addon">Min Score</span>',
						minInput,
						'<div class="input-group-addon">%</div>'
					),
					$('<div class="input-group" style="margin-right:10px"></div>').append(
						'<span class="input-group-addon">Max Score</span>',
						maxInput,
						'<div class="input-group-addon">%</div>'
					),
				)

			);

			this.updateInputs();
		}

		createInput(id) {
			var input = $('<input class="form-control" type="number" min="0" max="100" step="1"/>');
			input.prop('id', id);
			input.change(e => this.onChange());
			return input;
		}

		onChange() {
			this.minScore = $('#min-score-input').val();
			this.maxScore = $('#max-score-input').val();
			this.updateInputs();
			this.filter.filtersUpdated();
		}

		updateInputs() {
			var minInput = $('#min-score-input');
			minInput.val(this.minScore);
			minInput.prop('max', this.maxScore);

			var maxInput = $('#max-score-input');
			maxInput.val(this.maxScore);
			maxInput.prop('min', this.minScore);
		}

		match(node) {
			return node.maxScore.relativePercent > this.minScore && node.minScore.relativePercent < this.maxScore;
		}
	}

	class TransformationLevelsFilter {
		constructor(filter, container) {
			this.filter = filter;
			this.initModel(filter.view.rep);
			this.initUI(container);
		}

		initModel(rep) {
			this.maxLevel = rep.maxLevel;
			this.attributes = [];

			for (var i in rep.attributes) {
				var name = rep.attributes[i];
				var levels = rep.levels[i];

				var attr = {
					name: name,
					index: i,
					availableLevels: new Array(this.maxLevel + 1).fill(false),
					selectedLevels: new Array(this.maxLevel + 1).fill(false)
				};

				for (var j in levels) {
					attr.availableLevels[levels[j]] = true;
					attr.selectedLevels[levels[j]] = true;
				}

				this.attributes.push(attr);
			}
		}

		initUI(container) {
			var table = $('<table class="table table-bordered" style="width:100%;margin-top: 20px;"></table>');
			container.append($('<div></div>').append(table));

			var headTr = $('<tr></tr>');
			table.append($('<thead></thead>').append(headTr));
			headTr.append('<th>Attribute</th>');
			for (var i = 0; i < this.maxLevel + 1; i++) {
				headTr.append(`<th style="text-align:center">${i}</th>`);
			}

			var tbody = $('<tbody></tbody>');
			table.append(tbody);

			this.attributes.forEach(attr => {
				var tr = $('<tr></tr>');
				tbody.append(tr);

				tr.append(`<td>${attr.name}</td>`);

				for (let i in attr.availableLevels) {
					var td = $(`<td id="${attr.name}-${i}-level-cell" style="text-align:center"></td>`);
					tr.append(td);

					if (attr.availableLevels[i]) {
						var content = this.getCellContents(attr, i);
						td.append(content);
						td.css('cursor', 'pointer');

						let thisRef = this;
						td.on('click', () => {
							thisRef.onLevelClicked(attr, i);
						});
					}
				}
			});
		}

		getCellContents(attr, i) {
			var icon = attr.selectedLevels[i] ?
				'glyphicon glyphicon-ok text-success' :
				'glyphicon glyphicon-remove text-danger';
			return `<i class="${icon}"></i>`;
		}

		onLevelClicked(attr, index) {
			attr.selectedLevels[index] = !attr.selectedLevels[index];

			var td = $(`#${attr.name}-${index}-level-cell`);
			td.empty();
			td.append(this.getCellContents(attr, index));

			this.filter.filtersUpdated();
		}

		match(node) {
			var transformation = node.transformation;
			for (var i in this.attributes) {
				var attr = this.attributes[i];
				if (!attr.selectedLevels[transformation[attr.index]]) {
					return false;
				}
			}
			return true;
		}
	}

	class TransformationsTable {
		constructor(partition, container) {
			this.partition = partition;

			var table = $('<table class="table table-bordered" style="width:100%"></table>');
			var thead = $('<thead></thead>')
				.append($('<tr></tr>')
					.append('<th scope="col">Active</th>')
					.append('<th scope="col">Transformation</th>')
					.append('<th scope="col">Anonymity</th>')
					.append('<th scope="col">Min Score</th>')
					.append('<th scope="col">Max Score</th>')
				);
			table.append(thead);

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
								if (data == 'ANONYMOUS' || data == 'PROBABLY_ANONYMOUS') {
									icon = 'glyphicon-ok-sign';
									color = 'text-success';
								}
								if (data == 'NOT_ANONYMOUS' || data == 'PROBABLY_NOT_ANONYMOUS') {
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
				if (this.partition.view.filter.isVisible(node)) {
					data.push(node);
				}
			}
			return data;
		}

		updateSelected(selected) {
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
		//console.log(JSON.stringify(rep));

		if (val.selectedTransformations && rep.partitions && rep.partitions.length > 0) {
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