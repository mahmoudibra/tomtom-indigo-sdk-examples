/*
 * Copyright © 2020 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */

package com.example.ivi.example.telephony.customcontacts

import com.tomtom.ivi.platform.contacts.api.common.model.ContactId
import com.tomtom.ivi.platform.contacts.api.service.contacts.ContactsDataSourceElement
import com.tomtom.ivi.platform.contacts.api.service.contacts.ContactsDataSourceQuery
import com.tomtom.ivi.platform.contacts.api.service.contacts.ContactsDataSourceQuery.ContactOrderBy.ContactItemOrder.PRIMARY_SORT_KEY
import com.tomtom.ivi.platform.contacts.api.service.contacts.ContactsDataSourceQuery.ContactOrderBy.ContactItemOrderBy
import com.tomtom.ivi.platform.framework.api.testing.ipc.iviservice.datasource.assertIviDataSourceEquals
import com.tomtom.ivi.platform.telecom.api.common.model.PhoneBookSynchronizationStatus
import com.tomtom.ivi.platform.tools.api.testing.unit.IviTestCase
import com.tomtom.tools.android.testing.mock.niceMockk
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class CustomContactsServiceTest : IviTestCase() {

    private val sut = CustomContactsService(niceMockk())

    @Before
    fun createSut() {
        sut.onCreate()
    }

    @Test
    fun initialization() {
        // GIVEN-WHEN
        val contactsDataSource = mutableListOf(
            ContactsDataSourceElement.ContactItem(contact = sut.contactsSource[0]),
            ContactsDataSourceElement.ContactItem(contact = sut.contactsSource[1]),
        )
        val allContactsQuery = ContactsDataSourceQuery(
            ContactsDataSourceQuery.ContactSelection.All,
            ContactItemOrderBy(PRIMARY_SORT_KEY)
        )

        // THEN
        assertEquals(2, sut.contacts.size)
        assertIviDataSourceEquals(contactsDataSource, sut.contactsDataSource, allContactsQuery)
        assertEquals("John Smith", sut.contacts[ContactId("1")]?.displayName)
        assertEquals("Kelly Goodwin", sut.contacts[ContactId("2")]?.displayName)
        assertEquals(
            PhoneBookSynchronizationStatus.SYNCHRONIZATION_IN_PROGRESS,
            sut.phoneBookSynchronizationStatus
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `only contact 1 has an image`() = runTest {
        // GIVEN-WHEN
        sut.onCreate()

        // THEN
        assertNotNull(sut.getImage(ContactId("1")))
        assertNull(sut.getImage(ContactId("2")))
    }
}
