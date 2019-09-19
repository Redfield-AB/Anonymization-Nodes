/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/jQuery/jquery-3.3.1.js" />
/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/bootstrap/3_3_6/debug/js/bootstrap.js" />
/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/dataTables/1_10_11/bootstrap/datatables.js" />
/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/d3/4_13_0/d3.js" />

se_redfield_arxnode_nodes_anonymizer = function () {

	class View {
		constructor(rep, val) {
			this.rep = rep;
			this.val = val;

			this.container = $('<div clss="row"></div>');
			$('body').append($('<div class="container-fluid"></div>').append(this.container));

			this.filter = new TransformationFilter(this, this.container);
			this.initPartitionsTabs();
			this.filtersUpdated();
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

			$('.nav-link').on('click', e => {
				e.preventDefault();
				$(e.target).tab('show');
			});
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
			var header = $('<li class="nav-item"></li>')
				.append(link);
			tabsHeader.append(header);
			this.createTabContent(tabsContent);

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
					node.$visible = true;

					this.nodes[uid] = node;
				}
			}
		}

		uidForTransformation(transformation) {
			return `node-${this.index}-` + transformation.join('-');
		}

		createTabContent(container) {
			var tableContent = $(`<div class="tab-pane" role="tabpanel", id="table-view-${this.index}"></div>"`);
			var graphContent = $(`<div class="tab-pane active" role="tabpanel", id="graph-view-${this.index}"></div>"`);

			var content = $(`<div class="tab-pane" role="tabpanel" id="partiton-tab-${this.index}"></div>`).append(
				$('<ul class="nav nav-pills"></ul>').append(
					$('<li class="nav-item"></li>').append(
						$(`<a class="nav-link" role="tab" href="#table-view-${this.index}">Table</a>`)
					),
					$('<li class="nav-item active"></li>').append(
						$(`<a class="nav-link" role="tab" href="#graph-view-${this.index}">Graph</a>`)
					)
				),
				$('<div class="tab-content"></div>').append(tableContent, graphContent)
			);
			container.append(content);

			this.transformationsTable = new TransformationsTable(this, tableContent);
			this.transformationsGraph = new TransformationsGraph(this, graphContent);

			$('body').on('shown.bs.tab', e => {
				this.transformationsGraph.update();
			});
		}

		selectTransformation(node) {
			if (this.selectedNode) {
				this.selectedNode.$selected = false;
			}
			this.selectedNode = node;
			this.selectedNode.$selected = true;

			this.view.val.selectedTransformations[this.index] = node.transformation;

			this.transformationsTable.updateSelected(node);
			this.transformationsGraph.update();
		}

		filtersUpdated() {
			for (var i in this.nodes) {
				this.nodes[i].$visible = this.view.filter.isVisible(this.nodes[i]);
			}
			this.transformationsTable.reloadData();
			this.transformationsGraph.update();
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
					var td = $(`<td id="${attr.index}-${i}-level-cell" style="text-align:center"></td>`);
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

			var td = $(`#${attr.index}-${index}-level-cell`);
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
				if (node.$visible) {
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

	class TransformationsGraph {
		constructor(partition, container) {
			this.partition = partition;

			this.NODE_FACTOR = 0.8;
			this.LABEL_FACTOR = 0.4;
			this.Y_OFFSET = 30;

			this.svgContainer = $(`<div id="svg-container-${partition.index}"></div>`);
			container.append(this.svgContainer);

			this.svg = d3.select("#svg-container-" + partition.index).append("svg")
				.attr('width', '100%');

			let zoomRect = this.svg.append("rect")
				.attr("fill", "#f8f8f8")
				.attr("pointer-events", "all")
				.attr("width", '100%')
				.attr("height", '100%');
			let parentGroup = this.svg.append('g');
			zoomRect.call(d3.zoom().on('zoom', function () {
				parentGroup.attr('transform', d3.event.transform);
			}))


			this.edgesGroup = parentGroup.append('g');
			this.ovalsGroup = parentGroup.append('g');
			this.labelsGroup = parentGroup.append('g');

			var thisRef = this;
			$(window).resize(function () {
				thisRef.update();
			});
		}

		update() {
			if (!this.svgContainer.width()) {
				return;
			}

			this.processNodes();
			this.drawEdges();
			this.drawOvals();
			this.drawLabels();
		}

		processNodes() {
			var visibleLevels = [];
			var maxLevelLength = 0;
			for (var i in this.partition.levels) {
				var level = this.partition.levels[i].filter(n => n.$visible);
				if (level.length > 0) {
					visibleLevels.push(level);
					if (level.length > maxLevelLength) {
						maxLevelLength = level.length;
					}
				}
			}

			var nodeWidth = 150;
			var nodeHeight = 45;
			var screenWidth = this.svgContainer.width();

			var factor = screenWidth / (maxLevelLength * nodeWidth);
			if (factor < 1) {
				nodeWidth *= factor;
				nodeHeight *= factor;
			}

			this.nodeWidth = nodeWidth;
			this.nodeHeight = nodeHeight;

			var screenHeight = nodeHeight * visibleLevels.length;
			this.svg = this.svg.attr('height', screenHeight + 2 * this.Y_OFFSET);

			var offsetX = (screenWidth - maxLevelLength * nodeWidth) / 2;
			var positionY = visibleLevels.length - 1;
			this.nodesData = [];
			this.edgesData = [];

			for (var i in visibleLevels) {
				var level = visibleLevels[i];
				var centerY = (positionY * nodeHeight) + nodeHeight / 2 + this.Y_OFFSET;
				var positionX = 0;
				for (var j in level) {
					var node = level[j];
					var offset = (maxLevelLength - level.length) * nodeWidth;
					var centerX = offsetX + (positionX * nodeWidth) + nodeWidth / 2 + offset / 2;
					node.$centerX = centerX;
					node.$centerY = centerY;
					this.nodesData.push(node);
					positionX += 1;
				}
				positionY -= 1;
			}

			visibleLevels.forEach(level => {
				level.forEach(node => {
					node.successors.forEach(uid => {
						var toNode = this.partition.nodes[uid];
						if (toNode.$visible) {
							this.edgesData.push({
								fromX: node.$centerX,
								fromY: node.$centerY,
								toX: toNode.$centerX,
								toY: toNode.$centerY,
								uid: node.$uid + ':' + toNode.$uid
							});
						}
					});
				});
			});

		}

		drawEdges() {
			var edge = this.edgesGroup.selectAll('line').data(this.edgesData, d => d.uid);
			edge.exit().remove();
			edge.enter()
				.append('line')
				.attr('stroke', 'white')
				.attr('stroke-width', 2)
				.attr('x1', d => d.fromX)
				.attr('y1', d => d.fromY)
				.attr('x2', d => d.toX)
				.attr('y2', d => d.toY)
				.merge(edge)
				.transition()
				.attr('stroke', 'black')
				.attr('x1', d => d.fromX)
				.attr('y1', d => d.fromY)
				.attr('x2', d => d.toX)
				.attr('y2', d => d.toY);
		}

		drawOvals() {
			var oval = this.ovalsGroup.selectAll("ellipse").data(this.nodesData, d => d.$uid);
			oval.exit()
				.transition()
				.attr('rx', 0)
				.attr('ry', 0)
				.remove();

			oval.enter()
				.append('ellipse')
				.style('cursor', 'pointer')
				.attr('stroke-width', 3)
				.attr('rx', 0)
				.attr('ry', 0)
				.attr('cx', d => d.$centerX)
				.attr('cy', d => d.$centerY)
				.attr('fill', d => {
					if (d.optimum) {
						return 'yellow';
					}
					if (d.anonymity == 'ANONYMOUS' || d.anonymity == 'PROBABLY_ANONYMOUS') {
						return 'lightgreen';
					}
					return 'red';
				})
				.attr('stroke', d => d.$selected ? 'blue' : 'black')
				.on('click', node => {
					this.partition.selectTransformation(node);
				})
				.merge(oval)
				.transition()
				.attr('rx', this.nodeWidth * this.NODE_FACTOR / 2)
				.attr('ry', this.nodeHeight * this.NODE_FACTOR / 2)
				.attr('cx', d => d.$centerX)
				.attr('cy', d => d.$centerY)
				.attr('stroke', d => d.$selected ? 'blue' : 'black')
		}

		drawLabels() {
			var label = this.labelsGroup.selectAll('text').data(this.nodesData, d => d.$uid);
			var targetLabelWidth = this.nodeWidth * this.NODE_FACTOR * this.LABEL_FACTOR;
			label.exit().remove();
			label.enter()
				.append('text')
				.text(d => d.transformation)
				.attr("dy", ".35em")
				.style('cursor', 'pointer')
				.attr('transform', function (d) {
					var x = d.$centerX - targetLabelWidth / 2;
					var y = d.$centerY;
					return `translate(${x}, ${y}) scale(0) `;
				})
				.on('click', node => {
					this.partition.selectTransformation(node);
				})
				.merge(label)
				.transition()
				.attr('transform', function (d) {
					var factor = (targetLabelWidth) / this.getComputedTextLength();
					var x = d.$centerX - targetLabelWidth / 2;
					var y = d.$centerY;
					return `translate(${x}, ${y}) scale(${factor}) `;
				})
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