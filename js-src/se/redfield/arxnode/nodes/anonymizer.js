/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/jQuery/jquery-3.3.1.js" />
/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/bootstrap/3_3_6/debug/js/bootstrap.js" />
/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/dataTables/1_10_11/bootstrap/datatables.js" />

se_redfield_arxnode_nodes_anonymizer = function () {

	function initUI(rep) {
		var body = document.getElementsByTagName('body')[0];

		var tabsHeader = $('<ul class="nav nav-tabs"></ul>');
		var tabsContent = $('<div class="tab-content"></div>');

		$('body').append(tabsHeader);
		$('body').append(tabsContent);

		for (var i in rep.partitions) {
			var partition = rep.partitions[i];
			console.log('partition', partition);

			var link = $(`<a class="nav-link" role="tab" href="#partiton-tab-${i}">${i}</a>`);
			link.on('click', e => {
				e.preventDefault();
				console.log(e);
				$(e.target).tab('show');
			});

			var header = $('<li class="nav-item"></li>')
				.append(link);
			tabsHeader.append(header);

			var content = $(`<div class="tab-pane" role="tabpanel" id="partiton-tab-${i}"></div>`);
			tabsContent.append(content.append(createPartitionTabContent(partition, i)));

			if (i == 0) {
				link.tab('show');
			}
		}
	}

	function createPartitionTabContent(partition, index) {
		var table = $('<table class="table table-bordered"></table>');
		let data = [];

		for (let i in partition.levels) {
			let level = partition.levels[i];
			for (let j in level) {
				let node = level[j];
				data.push(node);
				if (node.transformation.toString() == model.selectedTransformations[index].toString()) {
					node.$active = true;
				}
			}
		}

		var thead = $('<thead></thead>')
			.append($('<tr></tr>')
				.append('<th scope="col">Active</th>')
				.append('<th scope="col">Transformation</th>')
				.append('<th scope="col">Anonymity</th>')
				.append('<th scope="col">Min Score</th>')
				.append('<th scope="col">Max Score</th>')
			);

		table.append(thead);

		let dt = table.DataTable({
			data: data,
			select: true,
			order: [[0, 'desc']],
			columns: [
				{
					data: '$active',
					defaultContent: "",
					render: function (data, type, row) {
						if (type === 'display' && data) {
							return '<i class="glyphicon glyphicon-ok"></i>';
						}
						return data;
					}
				},
				{ data: 'transformation' },
				{ data: 'anonymity' },
				{ data: 'minScore', render: renderScore},
				{ data: 'maxScore', render: renderScore},
			]
		});

		function renderScore(data, type, row) {
			if (type == 'display') {
				return data.value + '(' + (data.relative * 100).toFixed(2) + '%)';
			}
			return data.relative;
		}

		table.on('click', 'tr', function (e) {
			var data = dt.row(this).data();
			console.log('data', data);
			if (data) {
				table.find('tr').removeClass('success');
				$(this).addClass('success');
				model.selectedTransformations[index] = data.transformation;
			}
		});

		return table;
	}

	var model = {};
	var anonymizer = {
		verson: "0.1.0"
	}
	anonymizer.name = "Transformation View (JS)";
	anonymizer.init = function (rep, val) {
		console.log('init', rep, val);
		console.log(JSON.stringify(rep));
		model = val;
		if (model.selectedTransformations) {
			initUI(rep);
		} else {
			$('body').append('<h1>Please restart node</h1>');
		}
		console.log('model', model);
	}
	anonymizer.getComponentValue = function () {
		console.log('getComponentValue', model);
		return model;
	}
	anonymizer.validate = function () {
		console.log('validate');
		return true;
	}
	anonymizer.setValidationError = function (message) {

	}

	return anonymizer;
}();