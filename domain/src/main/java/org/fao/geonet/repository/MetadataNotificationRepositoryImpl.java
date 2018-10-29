/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.MetadataNotification;
import org.fao.geonet.domain.MetadataNotificationAction;
import org.fao.geonet.domain.MetadataNotificationId;
import org.fao.geonet.domain.MetadataNotificationId_;
import org.fao.geonet.domain.MetadataNotification_;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation for methods in MetadataNotificationRepositoryCustom.
 * <p/>
 * User: Jesse Date: 9/7/13 Time: 8:30 PM
 */
public class MetadataNotificationRepositoryImpl
		extends GeonetRepositoryImpl<MetadataNotification, MetadataNotificationId>
		implements MetadataNotificationRepositoryCustom {

	@PersistenceContext
	EntityManager _entityManager;

	@Override
	public List<MetadataNotification> findAllNotNotifiedForNotifier(int notifierId,
			MetadataNotificationAction... actions) {
		final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
		final CriteriaQuery<MetadataNotification> cbQuery = cb.createQuery(MetadataNotification.class);
		final Root<MetadataNotification> notificationRoot = cbQuery.from(MetadataNotification.class);
		final Path<Integer> notifierIdPath = notificationRoot.get(MetadataNotification_.id)
				.get(MetadataNotificationId_.notifierId);
		final Predicate correctNotifier = cb.equal(notifierIdPath, notifierId);

		final Path<Character> notifiedPath = notificationRoot.get(MetadataNotification_.notified_JPAWorkaround);
		final Predicate notifiedIsNull = cb.isNull(notifiedPath);
		final Predicate notNotified = cb.equal(notifiedPath, Constants.YN_FALSE);

		final Path<MetadataNotificationAction> actionPath = notificationRoot.get(MetadataNotification_.action);

		final Predicate fullClause;
		if (actions != null && actions.length > 0) {
			Predicate actionsPredicate = actionPath.in(actions);
			fullClause = cb.and(correctNotifier, cb.or(notifiedIsNull, notNotified), actionsPredicate);
		} else {
			fullClause = cb.and(correctNotifier, cb.or(notifiedIsNull, notNotified));

		}
		cbQuery.where(fullClause);
		return _entityManager.createQuery(cbQuery).getResultList();
	}

	@Override
	@Transactional
	public int deleteAllWithNotifierId(int notifierId) {
		final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
		final CriteriaDelete<MetadataNotification> delete = cb.createCriteriaDelete(MetadataNotification.class);
		final Root<MetadataNotification> notificationRoot = delete.from(MetadataNotification.class);

		delete.where(cb.equal(notificationRoot.get(MetadataNotification_.id).get(MetadataNotificationId_.notifierId),
				notifierId));
		return _entityManager.createQuery(delete).executeUpdate();
	}

}
