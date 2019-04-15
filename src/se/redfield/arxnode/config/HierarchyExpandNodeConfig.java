package se.redfield.arxnode.config;

import java.util.List;

public class HierarchyExpandNodeConfig extends ListSettingsModelConfig<HierarchyBinding> {

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected HierarchyBinding createChild() {
		return new HierarchyBinding();
	}

	public List<HierarchyBinding> getBindings() {
		return getChildred();
	}
}
