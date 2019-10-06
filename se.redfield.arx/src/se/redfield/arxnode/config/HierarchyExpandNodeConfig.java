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
package se.redfield.arxnode.config;

import java.util.List;

public class HierarchyExpandNodeConfig extends ListSettingsModelConfig<HierarchyBinding> {

	@Override
	public String getKey() {
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
