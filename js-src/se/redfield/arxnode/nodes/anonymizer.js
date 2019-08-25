/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/jQuery/jquery-3.3.1.js" />
/// <reference path="../../../../../../knime-src/knime-js-core/org.knime.js.core/js-lib/bootstrap/4_1_3/debug/js/bootstrap.js" />

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
		var thead = $('<thead></thead>')
			.append($('<tr></tr>')
				//.append('<th scope="col">Active</th>')
				.append('<th scope="col">Transformation</th>')
				.append('<th scope="col">Anonymity</th>')
			);
		var tbody = $('<tbody></tbody');

		table.append(thead);
		table.append(tbody);

		for (let i in partition.levels) {
			var level = partition.levels[i];
			for (var j in level) {
				let node = level[j];
				console.log('node', node, j);
				let tr = $('<tr></tr>')
					//.append('<th scope="row"></th>')
					.append($(`<td>${node.transformation}</td>`))
					.append($(`<td>${node.anonymity}</td>`));
				
				if(node.transformation.toString() == model.selectedTransformations[index].toString()) {
					tr.addClass('success');
				}

				tr.on('click', function (e) {
					console.log(table.children('tr'));
					table.find('tr').removeClass('success');
					tr.addClass('success');
					model.selectedTransformations[index] = node.transformation;
				});

				tbody.append(tr);
			}
		}

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
		if(model.selectedTransformations) {
			initUI(rep);
		}else{
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