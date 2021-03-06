/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.announcements.web.internal.display.context;

import com.liferay.announcements.kernel.util.AnnouncementsUtil;
import com.liferay.announcements.web.constants.AnnouncementsPortletKeys;
import com.liferay.announcements.web.internal.display.context.util.AnnouncementsRequestHelper;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.service.permission.PortletPermissionUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PrefsParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

import java.text.DateFormat;
import java.text.Format;

import java.util.LinkedHashMap;
import java.util.UUID;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletURL;

/**
 * @author Adolfo Pérez
 */
public class DefaultAnnouncementsDisplayContext
	implements AnnouncementsDisplayContext {

	public DefaultAnnouncementsDisplayContext(
		AnnouncementsRequestHelper announcementsRequestHelper) {

		_announcementsRequestHelper = announcementsRequestHelper;
	}

	@Override
	public LinkedHashMap<Long, long[]> getAnnouncementScopes()
		throws PortalException {

		LinkedHashMap<Long, long[]> scopes = new LinkedHashMap<>();

		if (isCustomizeAnnouncementsDisplayed()) {
			long[] selectedScopeGroupIdsArray = GetterUtil.getLongValues(
				StringUtil.split(getSelectedScopeGroupIds()));
			long[] selectedScopeOrganizationIdsArray = GetterUtil.getLongValues(
				StringUtil.split(getSelectedScopeOrganizationIds()));
			long[] selectedScopeRoleIdsArray = GetterUtil.getLongValues(
				StringUtil.split(getSelectedScopeRoleIds()));
			long[] selectedScopeUserGroupIdsArray = GetterUtil.getLongValues(
				StringUtil.split(getSelectedScopeUserGroupIds()));

			if (selectedScopeGroupIdsArray.length != 0) {
				scopes.put(
					PortalUtil.getClassNameId(Group.class.getName()),
					selectedScopeGroupIdsArray);
			}

			if (selectedScopeOrganizationIdsArray.length != 0) {
				scopes.put(
					PortalUtil.getClassNameId(Organization.class.getName()),
					selectedScopeOrganizationIdsArray);
			}

			if (selectedScopeRoleIdsArray.length != 0) {
				scopes.put(
					PortalUtil.getClassNameId(Role.class.getName()),
					selectedScopeRoleIdsArray);
			}

			if (selectedScopeUserGroupIdsArray.length != 0) {
				scopes.put(
					PortalUtil.getClassNameId(UserGroup.class.getName()),
					selectedScopeUserGroupIdsArray);
			}
		}
		else {
			scopes = AnnouncementsUtil.getAnnouncementScopes(
				_announcementsRequestHelper.getUser());
		}

		scopes.put(Long.valueOf(0), new long[] {0});

		return scopes;
	}

	@Override
	public Format getDateFormatDate() {
		ThemeDisplay themeDisplay =
			_announcementsRequestHelper.getThemeDisplay();

		return FastDateFormatFactoryUtil.getDate(
			DateFormat.FULL, themeDisplay.getLocale(),
			themeDisplay.getTimeZone());
	}

	@Override
	public int getPageDelta() {
		PortletPreferences portletPreferences =
			_announcementsRequestHelper.getPortletPreferences();

		return GetterUtil.getInteger(
			portletPreferences.getValue(
				"pageDelta", String.valueOf(SearchContainer.DEFAULT_DELTA)));
	}

	@Override
	public String getTabs1Names() {
		return "new,previous";
	}

	@Override
	public String getTabs1PortletURL() {
		LiferayPortletResponse liferayPortletResponse =
			_announcementsRequestHelper.getLiferayPortletResponse();

		PortletURL tabs1URL = liferayPortletResponse.createRenderURL();

		tabs1URL.setParameter("mvcRenderCommandName", "/announcements/view");
		tabs1URL.setParameter("tabs1", _announcementsRequestHelper.getTabs1());

		return tabs1URL.toString();
	}

	@Override
	public UUID getUuid() {
		return _UUID;
	}

	@Override
	public boolean isCustomizeAnnouncementsDisplayed() {
		Group scopeGroup = _announcementsRequestHelper.getScopeGroup();

		return PrefsParamUtil.getBoolean(
			_announcementsRequestHelper.getPortletPreferences(),
			_announcementsRequestHelper.getRequest(),
			"customizeAnnouncementsDisplayed", !scopeGroup.isUser());
	}

	@Override
	public boolean isScopeGroupSelected(Group scopeGroup) {
		String selectedScopeGroupIds = getSelectedScopeGroupIds();

		return selectedScopeGroupIds.contains(
			String.valueOf(scopeGroup.getGroupId()));
	}

	@Override
	public boolean isScopeOrganizationSelected(Organization organization) {
		String selectedScopeOrganizationIds = getSelectedScopeOrganizationIds();

		return selectedScopeOrganizationIds.contains(
			String.valueOf(organization.getOrganizationId()));
	}

	@Override
	public boolean isScopeRoleSelected(Role role) {
		String selectedScopeRoleIds = getSelectedScopeRoleIds();

		return selectedScopeRoleIds.contains(String.valueOf(role.getRoleId()));
	}

	@Override
	public boolean isScopeUserGroupSelected(UserGroup userGroup) {
		String selectedScopeUserGroupIds = getSelectedScopeUserGroupIds();

		return selectedScopeUserGroupIds.contains(
			String.valueOf(userGroup.getUserGroupId()));
	}

	@Override
	public boolean isShowPreviousEntries() {
		String tabs1 = _announcementsRequestHelper.getTabs1();

		return tabs1.equals("previous");
	}

	@Override
	public boolean isShowScopeName() {
		String mvcRenderCommandName = ParamUtil.getString(
			_announcementsRequestHelper.getRequest(), "mvcRenderCommandName");

		return mvcRenderCommandName.equals("/announcements/edit_entry");
	}

	@Override
	public boolean isTabs1Visible() {
		String portletName = _announcementsRequestHelper.getPortletName();

		ThemeDisplay themeDisplay =
			_announcementsRequestHelper.getThemeDisplay();

		try {
			if (!portletName.equals(AnnouncementsPortletKeys.ALERTS) ||
				(portletName.equals(AnnouncementsPortletKeys.ALERTS) &&
				 PortletPermissionUtil.hasControlPanelAccessPermission(
					 _announcementsRequestHelper.getPermissionChecker(),
					 themeDisplay.getScopeGroupId(),
					 AnnouncementsPortletKeys.ANNOUNCEMENTS_ADMIN))) {

				return true;
			}
		}
		catch (PortalException pe) {
			_log.error(pe, pe);
		}

		return false;
	}

	protected String getSelectedScopeGroupIds() {
		Layout layout = _announcementsRequestHelper.getLayout();

		return PrefsParamUtil.getString(
			_announcementsRequestHelper.getPortletPreferences(),
			_announcementsRequestHelper.getRequest(), "selectedScopeGroupIds",
			String.valueOf(layout.getGroupId()));
	}

	protected String getSelectedScopeOrganizationIds() {
		return PrefsParamUtil.getString(
			_announcementsRequestHelper.getPortletPreferences(),
			_announcementsRequestHelper.getRequest(),
			"selectedScopeOrganizationIds", StringPool.BLANK);
	}

	protected String getSelectedScopeRoleIds() {
		return PrefsParamUtil.getString(
			_announcementsRequestHelper.getPortletPreferences(),
			_announcementsRequestHelper.getRequest(), "selectedScopeRoleIds",
			StringPool.BLANK);
	}

	protected String getSelectedScopeUserGroupIds() {
		return PrefsParamUtil.getString(
			_announcementsRequestHelper.getPortletPreferences(),
			_announcementsRequestHelper.getRequest(),
			"selectedScopeUserGroupIds", StringPool.BLANK);
	}

	private static final UUID _UUID = UUID.fromString(
		"CD705D0E-7DB4-430C-9492-F1FA25ACE02E");

	private static final Log _log = LogFactoryUtil.getLog(
		DefaultAnnouncementsDisplayContext.class);

	private final AnnouncementsRequestHelper _announcementsRequestHelper;

}